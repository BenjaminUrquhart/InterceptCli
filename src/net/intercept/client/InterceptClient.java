package net.intercept.client;

import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

import org.json.JSONObject;

public class InterceptClient {

	private static String IP = "209.97.136.54";
	private static String TOKEN = null;
	private static final int PORT = 13373;
	
	public static final String SHELL = "root@%s~# ";
	public static boolean MUTE = false, OGG = false, DEBUG = false, RECONNECTING = false;
	
	private static boolean showShell = false;
	
	private static Socket conn;
	private static BufferedReader input;
	private static PrintWriter output;
	private static ReceiveHandler listener;
	
	public static String shell(){
		return showShell ? String.format(SHELL, EventHandler.connectedIP) : "";
	}
	public static void debug(Object text) {
		if(DEBUG) {
			System.out.printf("%s%s%s[DEBUG] %s%s\n%s", ColorUtil.RESET_CURSOR, ColorUtil.CLEAR_LINE, ColorUtil.CYAN, String.valueOf(text), ColorUtil.RESET, shell());
		}
	}
	private static String pad(String in) {
		int limit = 120 + (in.length() - in.replaceAll("\u001b\\[....m", "").length());
		while(in.length() < limit) {
			in += " ";
		}
		return in;
	}
	public static void reconnect() {
		int tries = 0;
		boolean prevDebug = DEBUG;
		showShell = false;
		RECONNECTING = true;
		DEBUG = true;
		JSONObject response, json = new JSONObject()
				.put("request", "connect")
				.put("token", TOKEN);
		while(true) {
			try {
				debug("Attempting to reconnect... (" + (tries+1) + "/10)");
				conn = new Socket(IP, PORT);
				input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				output = new PrintWriter(conn.getOutputStream());
				input.readLine();
				output.println(json);
				output.flush();
				response = new JSONObject(input.readLine());
				if((response.has("sucess") && response.getBoolean("sucess")) || (response.has("success") && response.getBoolean("success"))) {
					showShell = true;
					debug(ColorUtil.GREEN + "Reconnected");
					RECONNECTING = false;
					listener = new ReceiveHandler(input, 0.0);
					listener.start();
					DEBUG = prevDebug;
					return;
				}
				else {
					throw new IllegalArgumentException("Got bad response from server: " + response);
				}
			}
			catch(Exception e){
				debug(ColorUtil.YELLOW + e);
				Arrays.stream(e.getStackTrace()).forEach((trace) -> debug(ColorUtil.YELLOW + trace));
				if(tries == 10) {
					debug(ColorUtil.RED + "Failed to reconnect.");
					System.exit(1);
				}
				tries++;
				debug("Waiting " + tries + " second(s) before next attempt.");
				try {Thread.sleep(1000*tries);}catch(Exception exec) {}
			}
		}
	}
	public static void main(String[] args) throws Exception {
		boolean triedToANSI = false;
		//Reset ANSI on shutdown
		Thread.currentThread().setName("Intercept Main Loop");
		Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
			boolean oldDebug = DEBUG;
			DEBUG = true;
			debug(ColorUtil.RED + "An exception was thrown in the thread " + ColorUtil.YELLOW + thread.getName() + ":");
			debug(ColorUtil.RED + e.toString());
			Arrays.stream(e.getStackTrace()).forEach((element) -> debug(ColorUtil.RED + "at " + element));
			DEBUG = oldDebug;
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("\nlogout\u001b[0m")));
		if(args.length > 0){
			for(String arg : args){
				if(arg.equalsIgnoreCase("noansi")) {
					triedToANSI = true;
				}
				if(arg.equalsIgnoreCase("local")){
					IP = "127.0.0.1";
				}
				if(arg.equalsIgnoreCase("mute")){
					MUTE = true;
				}
				if(arg.equalsIgnoreCase("ogg")) {
					OGG = true;
				}
				if(arg.equalsIgnoreCase("debug")) {
					DEBUG = true;
				}
			}
		}
		System.out.print(ColorUtil.CLEAR_SCREEN + ColorUtil.RESET);
		ColorUtil.setCursorPos(0,0);
		if(triedToANSI) {
			System.out.println(ColorUtil.RED
					+ "#############################################\n"
					+ "#                                           #\n"
					+ "# ANSI is always enabled now, deal with it. #\n"
					+ "#                                           #\n"
					+ "#############################################\n" 
					+ ColorUtil.RESET);
		}
		if(MUTE){
			System.out.println("Sound disabled");
		}
		if(IP.equals("127.0.0.1")){
			System.out.println("Local mode enabled");
		}
		conn = new Socket(IP, PORT);
		input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		output = new PrintWriter(conn.getOutputStream());
		JSONObject json = new JSONObject(input.readLine());
		JSONObject auth = new JSONObject();
		Scanner sc = new Scanner(System.in);
		debug("Client ID: " + json.getString("client_id"));
		debug("Client type: " + json.getString("client_type"));
		debug("Date: " + new Date(json.getLong("date")));
		debug("OS: " + System.getProperty("os.name"));
		System.out.print(ColorUtil.GREEN + "Login/Register: ");
		boolean reg = sc.nextLine().toLowerCase().trim().equals("register");
		boolean success = false;
		System.out.println(reg ? "Creating a new account..." : "Logging in...");
		while(!success){
			json = new JSONObject();
			auth = new JSONObject();
			json.put("request", "auth");
			System.out.print("Username: ");
			auth.put("username", sc.nextLine());
			if(System.console() == null){System.out.print("Password: ");}
			auth.put("password", System.console() == null ? sc.nextLine() : new String(System.console().readPassword("Password: ")));
			if(reg) {
				if(System.console() == null){System.out.print("Retype password: ");}
				if(!auth.getString("password").equals(System.console() == null ? sc.nextLine() : new String(System.console().readPassword("Retype password: ")))) {
					System.out.println("Passwords do not match");
					continue;
				}
			}
			if(auth.getString("password").isEmpty()) {
				System.out.println("No password provided");
				continue;
			}
			json.put(reg ? "register" : "login", auth);
			output.println(json);
			output.flush();
			debug(json.toString().replace(auth.getString("password"), "[CENSORED]"));
			json = new JSONObject(input.readLine());
			debug(json.has("token") ? json.toString().replace(json.getString("token"), "[CENSORED]") : json);
			if(json.has("success")){
				success = json.getBoolean("success");
			}
			if(!success){
				System.out.println(json.getString("error"));
			}
		}
		auth.remove("password");
		json.put("request", "connect");
		json.remove("cfg");
		json.remove("event");
		json.remove("success");
		TOKEN = json.getString("token");
		output.println(json);
		output.flush();
		debug(json.toString().replace(json.getString("token"), "[CENSORED]"));
		json = new JSONObject(input.readLine());
		if((json.has("sucess") && json.getBoolean("sucess")) || (json.has("success") && json.getBoolean("success"))){ //Not a typo, dev of Intercept did a goof
			double volume = 1;
			if(json.has("cfg")) {
				volume = json.getJSONObject("cfg").getDouble("vol")/10.0;
			}
			output.println(new JSONObject().put("request", "command").put("cmd", "pass -l see"));
			output.flush();
			String ip = null, pass = new JSONObject(input.readLine()).getString("msg");
			listener = new ReceiveHandler(input, volume);
			if(json.has("player")){
				JSONObject player = json.getJSONObject("player");
				ip = player.getString("ip");
				if(!player.getString("ip").equals(player.getString("conn"))){
					String msg = ColorUtil.CYAN + "You are connected to an external system." + ColorUtil.RESET;
					EventHandler.connectedIP = player.getString("conn");
					System.out.println(msg);
				}
				else {
					EventHandler.connectedIP = "localhost";
				}
				
			}
			debug("Self: " + ip + " " + pass);
			showShell = true;
			listener.handle(json);
			listener.start();
			json = new JSONObject().put("request", "command");
			String line;
			while(true){
				line = sc.nextLine();
				if(line.equals("clear")){
					System.out.print(ColorUtil.CLEAR_SCREEN);
					ColorUtil.setCursorPos(0,0);
					System.out.print(shell());
					continue;
				}
				else if(line.startsWith("track")) {
					if(!line.trim().contains(" ")) {
						System.out.println("Usage: track <breach|peace|peace2|breach_loop>");
						System.out.printf("%sCurrect track: %s%s%s\n", ColorUtil.WHITE, ColorUtil.GREEN, listener.getSoundHandler().getTrack(), ColorUtil.RESET);
					}
					else if(MUTE || listener.getSoundHandler().getTrack().equals("None")) {
						System.out.printf("%sCannot set a track when audio is disabled%s\n", ColorUtil.YELLOW, ColorUtil.RESET);
						MUTE = true;
					}
					else {
						try {
							listener.getSoundHandler().setTrack(line.split(" ")[1].toLowerCase().trim());
							System.out.printf("%sNow playing: %s%s%s\n", ColorUtil.WHITE, ColorUtil.GREEN, listener.getSoundHandler().getTrack(), ColorUtil.RESET);
						}
						catch(Exception e) {
							System.out.println("Failed to load track. Defaulting to \"peace\"");
							listener.getSoundHandler().setTrack("peace");
						}
					}
					System.out.print(shell());
				}
				else if(line.trim().equals("")){
					System.out.print(shell());
				}
				else if(line.trim().equals("tracedump")) {
					if(DEBUG) {
						Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
						traces.forEach((thread, trace) -> {
							debug("----------------------------------------------------------------------------------------------------------------------- #");
							debug(pad(
									String.format("%sThread: %s%s%s - ID: %s%d%s - State: %s%s%s - Group: %s%s", 
									ColorUtil.BLUE, 
									ColorUtil.GREEN, 
									thread.getName(),
									ColorUtil.BLUE, 
									ColorUtil.GREEN,
									thread.getId(),
									ColorUtil.BLUE, 
									ColorUtil.GREEN, 
									String.valueOf(thread.getState()), 
									ColorUtil.BLUE, 
									ColorUtil.GREEN, 
									String.valueOf(thread.getThreadGroup())))
									+ ColorUtil.CYAN + "#");
							
							if(trace.length == 0) {
								debug(pad(ColorUtil.YELLOW + "(no trace available)") + ColorUtil.CYAN + "#");
								return;
							}
							Arrays.stream(trace).forEach((element) -> debug(pad(ColorUtil.YELLOW + element) + ColorUtil.CYAN + "#"));
						});
						debug("----------------------------------------------------------------------------------------------------------------------- #");
					}
					else {
						System.out.println(ColorUtil.YELLOW + "Please enable debug mode first");
					}
				}
				else if(line.trim().equals("debug")) {
					DEBUG = !DEBUG;
					System.out.println("Debug mode " + ColorUtil.GREEN + (DEBUG ? "enabled" : "disabled") + ColorUtil.RESET);
					System.out.print(shell());
				}
				else{
					if(line.trim().matches("software transfer (\\d+) self")) {
						line = line.replace("self", ip + " " + pass);
					}
					else if(line.trim().matches("bits transfer self (\\d+)")) {
						line = line.replace("self", auth.getString("username"));
					}
					json.put("cmd", line);
					try {
						output.println(json);
						output.flush();
					}
					catch(Exception e) {
						if(RECONNECTING) {
							while(RECONNECTING) {}
						}
						else {
							reconnect();
						}
						output.println(json);
						output.flush();
					}
					debug(json);
				}
			}
		}
		else{
			if(json.getString("event").equals("error")){
				System.out.println("An error occurred while logging in!");
				System.out.println(json.getString("error"));
				conn.close();
				sc.close();
				System.exit(1);
			}
			else{
				System.out.println("Received an unknown response from the server!");
				System.out.println(json);
				conn.close();
				sc.close();
				System.exit(1);
			}
		}
	}
}

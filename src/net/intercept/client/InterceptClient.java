package net.intercept.client;

import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

import org.json.JSONObject;

import static net.intercept.client.ANSI.*;

public class InterceptClient {

	private static String IP = "209.97.136.54";
	private static String TOKEN = null;
	private static final int PORT = 13373;
	
	public static final String SHELL = RESET + "root@%s~# ";
	public static boolean MUTE = false, OGG = false, DEBUG = false, RECONNECTING = false;
	
	public static ColorMode colorMode = ColorMode.EXTENDED;
	
	private static boolean showShell = false;
	
	private static Socket conn;
	private static BufferedReader input;
	private static PrintWriter output;
	private static ReceiveHandler listener;
	
	private static JSONObject auth;
	
	private static String ip, pass;
	
	public static void setIP(String IP) {
		ip = IP;
	}
	public static void setPass(String password) {
		pass = password;
	}
	public synchronized static JSONObject send(JSONObject json) {
		if(json != null) {
			output.println(json);
			output.flush();
		}
		try {
			return new JSONObject(input.readLine());
		}
		catch(Exception e) {
			return null;
		}
	}
	public static String shell(){
		return showShell ? String.format(SHELL, EventHandler.connectedIP) : "";
	}
	public static void debug(Object text) {
		if(DEBUG) {
			System.out.printf("%s%s%s[DEBUG] %s%s\n%s", RESET_CURSOR, CLEAR_LINE, CYAN, String.valueOf(text), RESET, shell());
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
					debug(GREEN + "Reconnected");
					RECONNECTING = false;
					listener = new ReceiveHandler(input, 0.0);
					listener.start();
					DEBUG = prevDebug;
					return;
				}
				else {
					if(json.getString("error").equals("Unauthorised")) {
						output.println(new JSONObject().put("request", "auth").put("login", auth));
						output.flush();
						TOKEN = new JSONObject(input.readLine()).getString("token");
						tries--;
						throw new IllegalStateException("Token expired, logging in again...");
					}
					else {
						throw new IllegalArgumentException("Got bad response from server: " + response);
					}
				}
			}
			catch(Exception e){
				debug(YELLOW.toString() + e);
				//Arrays.stream(e.getStackTrace()).forEach((trace) -> debug(YELLOW + trace));
				tries++;
				if(tries == 10) {
					debug(RED + "Failed to reconnect.");
					System.exit(1);
				}
				debug("Waiting " + tries + " second(s) before next attempt.");
				try {Thread.sleep(1000*tries);}catch(Exception exec) {}
			}
		}
	}
	public static void main(String[] arguments) throws Exception {
		Thread.currentThread().setName("Intercept Main Loop");
		Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
			System.out.println();
			boolean oldDebug = DEBUG;
			DEBUG = true;
			debug(RED + "An exception was thrown in the thread " + YELLOW + thread.getName() + ":");
			debug(RED + e.toString());
			Arrays.stream(e.getStackTrace()).forEach((element) -> debug(RED + "at " + element));
			DEBUG = oldDebug;
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("\nlogout\u001b[0m"))); //Reset ANSI on shutdown
		System.out.print(CLEAR_SCREEN + RESET);
		setCursorPos(0,0);
		if(arguments.length > 0){
			for(String arg : arguments){
				if(arg.equalsIgnoreCase("noansi")) {
					System.out.println(RED
							+ "#############################################\n"
							+ "#                                           #\n"
							+ "# ANSI is always enabled now, deal with it. #\n"
							+ "#                                           #\n"
							+ "#############################################\n" 
							+ RESET);
				}
				if(arg.equalsIgnoreCase("local")){
					System.out.println("Local mode enabled");
					IP = "127.0.0.1";
				}
				if(arg.equalsIgnoreCase("mute")){
					System.out.println("Sound disabled");
					MUTE = true;
				}
				if(arg.equalsIgnoreCase("ogg")) {
					OGG = true;
				}
				if(arg.equalsIgnoreCase("debug")) {
					DEBUG = true;
				}
				if(arg.toLowerCase().startsWith("color:")) {
					try {
						String color = arg.split("\\:")[1].toLowerCase();
						switch(color) {
						case "basic": colorMode = ColorMode.BASIC; break;
						case "8": colorMode = ColorMode.BASIC; break;
						case "extended": colorMode = ColorMode.EXTENDED; break;
						case "24bit": colorMode = ColorMode.EXTENDED; break;
						case "256": colorMode = ColorMode.EXTENDED; break;
						case "truecolor": colorMode = ColorMode.TRUECOLOR; break;
						case "true": colorMode = ColorMode.TRUECOLOR; break;
						default: System.out.println("Unknown color mode: " + color + ".\nAvailable modes: " + Arrays.toString(ColorMode.values()));
						}
					}
					catch(Exception e) {
						System.out.println(YELLOW + "Failed to parse color mode: " + e + RESET);
						Arrays.stream(e.getStackTrace()).forEach((trace) -> debug(YELLOW + "at " + trace));
					}
				}
			}
		}
		try {
			String colorTerm = System.getenv("COLORTERM");
			if(colorTerm.equals("24bit") && colorMode.equals(ColorMode.TRUECOLOR)) {
				System.out.println(YELLOW.toExtended() + "Warning: TrueColor support not detected. COLORTERM environemnt variable set to " + colorTerm);
				System.out.println("Recommended mode: " + ColorMode.EXTENDED + RESET);
			}
		}
		catch(Exception e) {}
		System.out.println("Color mode: " + colorMode);
		conn = new Socket(IP, PORT);
		input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		output = new PrintWriter(conn.getOutputStream());
		JSONObject json = new JSONObject(input.readLine());
		Scanner sc = new Scanner(System.in);
		debug("Client ID: " + json.getString("client_id"));
		debug("Client type: " + json.getString("client_type"));
		debug("Date: " + new Date(json.getLong("date")));
		debug("OS: " + System.getProperty("os.name"));
		System.out.print(GREEN + "Login/Register: ");
		boolean reg = sc.nextLine().toLowerCase().trim().equals("register");
		boolean success = false;
		System.out.println(reg ? "Creating a new account..." : "Logging in...");
		auth = new JSONObject();
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
			debug(json.toString().replace(auth.getString("password"), "[CENSORED]"));
			json = send(json);
			debug(json.has("token") ? json.toString().replace(json.getString("token"), "[CENSORED]") : json);
			if(json.has("success")){
				success = json.getBoolean("success");
			}
			if(!success){
				System.out.println(json.getString("error"));
			}
		}
		json.put("request", "connect");
		json.remove("cfg");
		json.remove("event");
		json.remove("success");
		TOKEN = json.getString("token");
		debug(json.toString().replace(json.getString("token"), "[CENSORED]"));
		json = send(json);
		if((json.has("sucess") && json.getBoolean("sucess")) || (json.has("success") && json.getBoolean("success"))){ //Not a typo, dev of Intercept did a goof
			double volume = 1;
			if(json.has("cfg")) {
				volume = json.getJSONObject("cfg").getDouble("vol");
			}
			pass = send(new JSONObject().put("request", "command").put("cmd", "pass -l see")).getString("msg");
			listener = new ReceiveHandler(input, volume);
			if(json.has("player")){
				JSONObject player = json.getJSONObject("player");
				ip = player.getString("ip");
				if(!ip.equals(player.getString("conn"))){
					EventHandler.connectedIP = player.getString("conn");
					System.out.println(CYAN + "You are connected to an external system." + RESET);
				}
				else {
					EventHandler.connectedIP = "localhost";
				}
				
			}
			debug("Self: " + ip + " " + pass);
			MacroManager.loadMacros();
			showShell = true;
			listener.handle(json);
			listener.start();
			json = new JSONObject().put("request", "command");
			String line;
			while(true){
				line = sc.nextLine().trim();
				if(MacroManager.getMacro(line.split(" ")[0]) != null) {
					line = MacroManager.getMacro(line.split(" ")[0]) + (line.contains(" ") ? " " + line.split(" ", 2)[1] : "");
				}
				if(line.equals("clear")){
					System.out.print(CLEAR_SCREEN);
					setCursorPos(0,0);
					System.out.print(shell());
					continue;
				}
				else if(line.startsWith("track")) {
					if(!line.trim().contains(" ")) {
						System.out.println("Usage: track [breach|peace|peace2|breach_loop]");
						System.out.printf("%sCurrect track: %s%s%s\n", WHITE, GREEN, listener.getSoundHandler().getTrack(), RESET);
					}
					else if(MUTE || listener.getSoundHandler().getTrack().equals("None")) {
						System.out.printf("%sCannot set a track when audio is disabled%s\n", YELLOW, RESET);
						MUTE = true;
					}
					else {
						try {
							listener.getSoundHandler().setTrack(line.split(" ")[1].toLowerCase().trim());
							System.out.printf("%s%s%sNow playing: %s%s%s\n", CLEAR_LINE, RESET_CURSOR, WHITE, GREEN, listener.getSoundHandler().getTrack(), RESET);
						}
						catch(Exception e) {
							System.out.println("Failed to load track. Defaulting to \"peace\"");
							listener.getSoundHandler().setTrack("peace");
						}
					}
					System.out.print(shell());
				}
				else if(line.equals("")){
					System.out.print(shell());
				}
				else if(line.equals("mute")){
					MUTE = !MUTE;
					System.out.println("Audio " + GREEN + (MUTE ? "disabled" : "enabled") + RESET);
					if(MUTE){
						listener.getSoundHandler().setVolume(0.0);
					}
					else{
						if(listener.getSoundHandler().getTrack().equals("None")){
							listener.getSoundHandler().start(volume);
						}
						else{
							listener.getSoundHandler().setVolume(volume);
						}
					}
					System.out.print(shell());
				}
				else if(line.equals("tracedump")) {
					if(DEBUG) {
						Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
						traces.forEach((thread, trace) -> {
							debug("----------------------------------------------------------------------------------------------------------------------- #");
							debug(pad(
									String.format("%sThread: %s%s%s - ID: %s%d%s - State: %s%s%s - Group: %s%s", 
									BLUE.toBasic(), 
									GREEN.toBasic(), 
									thread.getName(),
									BLUE.toBasic(), 
									GREEN.toBasic(),
									thread.getId(),
									BLUE.toBasic(), 
									GREEN.toBasic(), 
									String.valueOf(thread.getState()), 
									BLUE.toBasic(), 
									GREEN.toBasic(), 
									String.valueOf(thread.getThreadGroup())))
									+ CYAN + "#");
							
							if(trace.length == 0) {
								debug(pad(YELLOW.toBasic() + "(no trace available)") + CYAN + "#");
								return;
							}
							Arrays.stream(trace).forEach((element) -> debug(pad(YELLOW.toBasic() + element) + CYAN + "#"));
						});
						debug("----------------------------------------------------------------------------------------------------------------------- #");
					}
					else {
						System.out.println(YELLOW + "Please enable debug mode first");
						System.out.print(shell());
					}
				}
				else if(line.equals("debug")) {
					DEBUG = !DEBUG;
					System.out.println("Debug mode " + GREEN + (DEBUG ? "enabled" : "disabled") + RESET);
					System.out.print(shell());
				}
				else if(line.startsWith("macros")) {
					if(!line.contains(" ")) {
						System.out.println("Usage: macros [list/add/remove/reload] [name] [action]");
					}
					else {
						String[] args = line.split(" ", 4);
						if(args[1].equals("list")) {
							int[] index = new int[1];
							MacroManager.getMacros().forEach((name, cmd) -> {
								index[0]++;
								System.out.printf("%d: %s%s%s -> %s%s%s\n", index[0], GREEN, name, CYAN, GREEN, cmd.replace("\u001b", "\\u001b"), RESET);
							});
						}
						else if(args[1].equals("add")) {
							if(args.length < 4) {
								System.out.println("Usage: macros add [name] [command]");
							}
							else {
								if(MacroManager.addMacro(args[2], args[3])) {
									System.out.printf("%s[SUCCESS]%s macro %s%s%s set to %s%s%s\n", GREEN, RESET, CYAN, args[2], RESET, CYAN, args[3], RESET);
								}
								else {
									System.out.printf("%s[WARN] failed to set macro %s%s%s. More details can be found by using debug mode.%s\n", YELLOW, CYAN, args[2], YELLOW, RESET);
								}
							}
						}
						else if(args[1].equals("remove")) {
							if(args.length < 3) {
								System.out.println("Usage: macros remove [name]");
							}
							else {
								if(MacroManager.removeMacro(args[2])) {
									System.out.printf("%s[SUCCESS]%s macro %s%s%s removed\n", GREEN, RESET, CYAN, args[2], RESET);
								}
								else {
									System.out.printf("%s[WARN] failed to remove macro %s%s%s. More details can be found by using debug mode.%s\n", YELLOW, CYAN, args[2], YELLOW, RESET);
								}
							}
						}
						else if(args[1].equals("reload")) {
							MacroManager.loadMacros();
						}
						else {
							System.out.println("Usage: macros <list/add/remove> [name] [action]");
						}
					}
					System.out.print(shell());
				}
				else{
					if(line.matches("software transfer (\\d+) self")) {
						line = line.replace("self", ip + " " + pass);
					}
					else if(line.matches("bits transfer self (\\d+)")) {
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

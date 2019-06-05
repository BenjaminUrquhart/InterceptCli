package net.intercept.client;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static net.intercept.client.color.ANSI.*;

import org.json.JSONArray;
import org.json.JSONObject;

import net.intercept.client.audio.JOrbisPlayer;
import net.intercept.client.color.ANSI;
import net.intercept.client.color.ColorMode;
import net.intercept.client.macros.MacroManager;
import net.intercept.client.networking.Connection;
import net.intercept.client.networking.EventHandler;
import net.intercept.client.networking.ReceiveHandler;
import net.intercept.client.storage.StorageManager;
import net.intercept.client.util.Input;
import net.intercept.client.util.Timestamps;
import okhttp3.Request;
import okhttp3.Response;

public class InterceptClient {

	//private static String IP = "209.97.136.54";
	//private static String TOKEN = null;
	//private static final int PORT = 13373;
	
	public static Connection conn;
	
	public static ColorMode colorMode = ColorMode.EXTENDED;
	
	private static String username;
	private static String ITCH_TOKEN;
	public static String SHELL = "%s@%s~# ";
	public static boolean MUTE = false, OGG = false, DEBUG = false, RECONNECTING = false;
	
	private static boolean showShell = false;
	
	private static String ip;
	
	public static JSONObject send(JSONObject json) {
		if(json != null) {
			debug(json);
			conn.send(json);
		}
		try {
			return new JSONObject(conn.readLine());
		}
		catch(Exception e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
		}
		return null;
	}
	public static void setIP(String IP) {
		ip = IP;
	}
	public static String shell(){
		return showShell && colorMode != ColorMode.GUI ? String.format(GREEN.toBasic() + SHELL + RESET, username, EventHandler.connectedIP) : "";
	}
	public static void debug(Object text) {
		debug(text, true);
	}
	public static void debug(Object text, boolean method) {
		if(DEBUG) {
			StackTraceElement[] trace =  Thread.currentThread().getStackTrace();
			System.out.printf("%s%s%s[DEBUG] %s%s%s%s%s: %s%s%s\n%s", RESET_CURSOR, CLEAR_LINE, CYAN, GRAY, trace[method ? 3 : 2].getClassName(), method ? ORANGE + "." : "", method ? trace[3].getMethodName() + "()" : "", RESET, CYAN, String.valueOf(text).replace(ITCH_TOKEN, "[CENSORED]"), RESET, shell());
		}
	}
	private static String pad(String in) {
		int limit = 120 + (in.length() - in.replaceAll("\u001b\\[....m", "").length());
		while(in.length() < limit) {
			in += " ";
		}
		return in;
	}
	public static boolean isTokenValid() {
		try {
			Response response = Connection.CLIENT.newCall(
					new Request.Builder()
							   .url("https://itch.io/api/1/"+ITCH_TOKEN+"/credentials/info")
							   .build()
			).execute();
			if(response.code() == 200) {
				JSONObject json = new JSONObject(response.body().string());
				return json.has("scopes") && json.getJSONArray("scopes").toList().contains("profile:me");
			}
		}
		catch(Exception e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
		}
		return false;
	}
	/*
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
	}*/
	public static void main(String[] arguments) throws Exception {
		Thread.currentThread().setName("Intercept Main Loop");
		Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
			if((thread instanceof JOrbisPlayer) && (e instanceof NullPointerException)) {
				debug(ORANGE + "Supressed an NPE from an audio thread");
				return;
			}
			boolean oldDebug = DEBUG;
			DEBUG = true;
			System.out.println();
			debug(RED + "An exception was thrown in the thread " + YELLOW + thread.getName() + ":");
			debug(RED + e.toString());
			Arrays.stream(e.getStackTrace()).forEach((element) -> debug(RED + "at " + element));
			DEBUG = oldDebug;
			if(thread.getName().equals("Intercept Main Loop")) {
				System.exit(1);
			}
		});
		//Reset ANSI on shutdown and disable time-stamping
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Timestamps.disable();
			System.out.println("\nlogout\u001b[0m");
		}));
		System.out.print(CLEAR_SCREEN + "" + RESET);
		setCursorPos(0,0);
		Input sc = null;
		if(arguments.length > 0){
			for(String arg : arguments){
				if(arg.toLowerCase().equalsIgnoreCase("timestamps")) {
					Timestamps.enable();
					System.out.println("Timestamps enabled");
				}
				if(arg.toLowerCase().equals("gui")) {
					try {
						sc = new Input(net.intercept.gui.InterceptX.enable());
						colorMode = ColorMode.GUI;
					}
					catch(Exception e) {
						System.out.println("Failed to start GUI. Defaulting to CLI");
						debug(e);
						Arrays.stream(e.getStackTrace()).forEach((trace) -> debug(YELLOW + "at " + trace));
					}
				}
				if(arg.toLowerCase().startsWith("color:")) {
					if(colorMode == ColorMode.GUI) {
						System.out.println("Can't set the color mode when the GUI is enabled");
						continue;
					}
					try {
						String color = arg.split("\\:")[1].toLowerCase();
						switch(color) {
						case "none": colorMode = ColorMode.NONE; break;
						case "basic": colorMode = ColorMode.BASIC; break;
						case "8": colorMode = ColorMode.BASIC; break;
						case "extended": colorMode = ColorMode.EXTENDED; break;
						case "24bit": colorMode = ColorMode.EXTENDED; break;
						case "256": colorMode = ColorMode.EXTENDED; break;
						case "truecolor": colorMode = ColorMode.TRUECOLOR; break;
						case "true": colorMode = ColorMode.TRUECOLOR; break;
						default: colorMode = ColorMode.EXTENDED; System.out.println("Unknown color mode: " + color + ".\nAvailable modes: " + Arrays.toString(ColorMode.values()));
						}
					}
					catch(Exception e) {
						System.out.println(YELLOW + "Failed to parse color mode: " + e + RESET);
						Arrays.stream(e.getStackTrace()).forEach((trace) -> debug(YELLOW + "at " + trace));
					}
				}
				if(arg.equalsIgnoreCase("noansi")) {
					System.out.println(RED
							+ "#############################################\n"
							+ "#                                           #\n"
							+ "# noansi has been replaced with color:none  #\n"
							+ "#                                           #\n"
							+ "#############################################\n" 
							+ RESET);
				}
				/*
				if(arg.equalsIgnoreCase("local")){
					System.out.println("Local mode enabled");
					IP = "127.0.0.1";
				}*/
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
			}
		}
		try {
			String colorTerm = System.getenv("COLORTERM");
			if(colorTerm.equals("24bit") && colorMode.equals(ColorMode.TRUECOLOR)) {
				System.out.println(YELLOW.toExtended() + "Warning: TrueColor support not detected. COLORTERM environemnt variable set to " + colorTerm);
				System.out.println("Recommended mode: " + ColorMode.EXTENDED + RESET);
			}
		}
		catch(Exception e) {
			System.out.println(YELLOW + "Failed to get COLORTERM environment variable." + RESET);
		}
		System.out.println("Color mode: " + colorMode);
		conn = new Connection("wss://intercept.mudjs.net/ws");
		Connection input = conn;
		JSONObject json = new JSONObject(input.readLine());
		if(sc == null) {
			sc = new Input(System.in);
		}
		debug("Client ID: " + json.getString("client_id"));
		debug("Client type: " + json.getString("client_type"));
		debug("Date: " + new Date(json.getLong("date")));
		debug("OS: " + System.getProperty("os.name"));
		JSONObject auth = new JSONObject();
		File tokenFile = new File(StorageManager.getStorageDir(), "auth_token");
		boolean hasToken = tokenFile.exists();
		if(hasToken) {
			ITCH_TOKEN = new String(Files.readAllBytes(tokenFile.toPath()));
			hasToken = isTokenValid();
		}
		if(!hasToken) {
			do {
				System.out.println(tokenFile.exists() ? ORANGE + "Invalid auth token" : GREEN + "Intercept requires an Itch.io API token for authentication.");
				System.out.print(GREEN + "Please paste your token here: ");
				ITCH_TOKEN = sc.nextLine();
				Files.write(tokenFile.toPath(), ITCH_TOKEN.getBytes());
			} while(!isTokenValid());
		}
		System.out.println(GREEN + "Logging in...");
		auth.put("request", "auth");
		auth.put("key", ITCH_TOKEN);
		json = send(auth);
		auth.put("token", json.getString("token"));
		auth.remove("key");
		json = send(auth);
		if(json.has("success") && json.getBoolean("success")){ 
			json = send(new JSONObject().put("request", "systems"));
			JSONArray systems = json.getJSONArray("systems");
			JSONObject system;
			int i = 0;
			for(Object obj : systems) {
				system = (JSONObject) obj;
				System.out.println(GREEN + "" + i + ": " + system.getString("hostname") + "@" + system.getString("ip") + " (" + system.getString("type") + ")");
				i++;
			}
			String selection = null;
			int tmp = -1;
			do {
				System.out.print(GREEN + "Enter system index: ");
				selection = sc.nextLine();
				if(!selection.matches("\\d+")) {
					System.out.println(ORANGE + "That's not a number.");
				}
				else {
					tmp = Integer.parseInt(selection);
					if(tmp < 0 || tmp >= i) {
						System.out.println(ORANGE + "Invalid index.");
						continue;
					}
				}
			} while(tmp < 0);
			system = (JSONObject) systems.get(tmp);
			ip = system.getString("ip");
			json = send(new JSONObject().put("request", "connect").put("system", system.getString("id")));
			double volume = 1;
			if(json.has("cfg")) {
				volume = json.getJSONObject("cfg").getDouble("vol");
			}
			ReceiveHandler listener = new ReceiveHandler(input, volume);
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
			System.out.print(ANSI.CLEAR_SCREEN);
			ANSI.setCursorPos(0, 0);
			username = new JSONObject(input.readLine()).getString("user");
			debug("Self: " + ip);
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
					try {
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
					}
					catch(Exception e) {
						System.out.printf("%s%s%sSomething really went wrong while setting that track.", CLEAR_LINE, RESET_CURSOR, RED);
						Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
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
							debug("----------------------------------------------------------------------------------------------------------------------- #", false);
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
									+ CYAN + "#", false);
							
							if(trace.length == 0) {
								debug(pad(YELLOW.toBasic() + "(no trace available)") + CYAN + "#", false);
								return;
							}
							Arrays.stream(trace).forEach((element) -> debug(pad(YELLOW.toBasic() + element) + CYAN + "#", false));
						});
						debug("----------------------------------------------------------------------------------------------------------------------- #", false);
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
					/*
					if(line.matches("software transfer (\\d+) self")) {
						line = line.replace("self", ip + " " + pass);
					}*/
					if(line.matches("bits transfer self (\\d+)")) {
						line = line.replace("self", auth.getString("username"));
					}
					json.put("cmd", line);
					conn.send(json);
				}
			}
		}
		else{
			if(json.getString("event").equals("error")){
				System.out.println("An error occurred while logging in!");
				System.out.println(json.getString("error"));
				conn.close();
				System.exit(1);
			}
			else{
				System.out.println("Received an unknown response from the server!");
				System.out.println(json);
				conn.close();
				System.exit(1);
			}
		}
	}
}

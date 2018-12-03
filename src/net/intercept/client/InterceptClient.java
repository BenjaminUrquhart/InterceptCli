package net.intercept.client;

import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.io.*;

import org.json.JSONObject;

public class InterceptClient {

	private static String IP = "209.97.136.54";
	private static final int PORT = 13373;
	
	public static final String SHELL = "root@%s~# ";
	
	public static boolean ANSI = !System.getProperty("os.name").startsWith("Windows");
	public static boolean MUTE = false;
	public static boolean OGG = false;
	
	public static String shell(){
		return String.format(SHELL, EventHandler.connectedIP);
	}
	public static void main(String[] args) throws Exception {
		//Reset ANSI on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("\nlogout\u001b[0m")));
		if(args.length > 0){
			for(String arg : args){
				if(arg.equalsIgnoreCase("local")){
					IP = "127.0.0.1";
					System.out.println("Local mode enabled");
				}
				if(arg.equalsIgnoreCase("noansi")){
					ANSI = false;
				}
				if(arg.equalsIgnoreCase("forceansi")) {
					ANSI = true;
				}
				if(arg.equalsIgnoreCase("mute")){
					MUTE = true;
				}
				if(arg.equalsIgnoreCase("ogg")) {
					OGG = true;
				}
			}
		}
		if(ANSI){
			System.out.print(ColorUtil.CLEAR_SCREEN + ColorUtil.RESET);
			ColorUtil.setCursorPos(0,0);
		}
		else{
			System.out.println("ANSI disabled");
		}
		if(MUTE){
			System.out.println("Sound disabled");
		}
		if(IP.equals("127.0.0.1")){
			System.out.println("Local mode enabled");
		}
		Socket conn = new Socket(IP, PORT);
		BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		PrintWriter output = new PrintWriter(conn.getOutputStream());
		JSONObject json = new JSONObject(input.readLine());
		JSONObject login = new JSONObject();
		Scanner sc = new Scanner(System.in);
		System.out.println("Client ID: " + json.getString("client_id"));
		System.out.println("Client type: " + json.getString("client_type"));
		System.out.println("Date: " + new Date(json.getLong("date")));
		System.out.println("OS: " + System.getProperty("os.name"));
		System.out.println("Ready to log in.");
		boolean success = false;
		while(!success){
			json = new JSONObject();
			login = new JSONObject();
			json.put("request", "auth");
			System.out.print("Username: ");
			login.put("username", sc.nextLine());
			if(System.console() == null){
				System.out.print("Password: ");
			}
			login.put("password", System.console() == null ? sc.nextLine() : new String(System.console().readPassword("Password: ")));
			json.put("login", login);
			output.println(json);
			output.flush();
			json = new JSONObject(input.readLine());
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
		output.println(json);
		output.flush();
		json = new JSONObject(input.readLine());
		if((json.has("sucess") && json.getBoolean("sucess")) || (json.has("success") && json.getBoolean("success"))){ //Not a typo, dev of Intercept did a goof
			double volume = 1;
			if(json.has("cfg")) {
				volume = json.getJSONObject("cfg").getDouble("vol")/10.0;
			}
			ReceiveHandler listener = new ReceiveHandler(input, volume);
			if(json.has("player")){
				JSONObject player = json.getJSONObject("player");
				if(!player.getString("ip").equals(player.getString("conn"))){
					String msg = "You are connected to an external system.";
					if(ANSI) {
						msg = ColorUtil.CYAN + msg + ColorUtil.RESET;
					}
					EventHandler.connectedIP = player.getString("conn");
					System.out.println(msg);
				}
				else {
					EventHandler.connectedIP = "localhost";
				}
				
			}
			listener.handle(json);
			listener.start();
			json = new JSONObject().put("request", "command");
			String line;
			while(true){
				line = sc.nextLine();
				if(line.equals("clear")){
					if(ANSI){
						System.out.print(ColorUtil.CLEAR_SCREEN);
						ColorUtil.setCursorPos(0,0);
					}
					else{
						System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
					}
					System.out.print(shell());
					continue;
				}/*
				Apparently volume is handled server-side...
				else if(line.startsWith("vol")) {
					if(!line.trim().contains(" ")) {
						System.out.println("Usage: vol [level (0.0-1.0)]");
					}
					else {
						try {
							volume = Double.parseDouble(line.split(" ")[1]);
							if(volume > 10.0 || volume < 0.0) {
								throw new NumberFormatException("Volume out of bounds, must be >= 0 and <= 10");
							}
							listener.getSoundHandler().setVolume(volume);
							JSONObject vol = new JSONObject()
									.put("request", "cfg")
									.put("cfg", new JSONObject().put("vol", volume));
							//System.out.println(vol);
							output.println(vol);
							output.flush();
						}
						catch(NumberFormatException e) {
							System.out.println(e.toString() + "\nUsage: vol [level (0.0-10.0)]");
						}
					}
					System.out.print(shell());
				}*/
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
				else if(line.equals("")){
					System.out.print(shell());
				}
				else{
					json.put("cmd", line);
					output.println(json);
					output.flush();
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

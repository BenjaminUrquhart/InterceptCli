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
			ReceiveHandler listener = new ReceiveHandler(input);
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
				
			}
			listener.start();
			String line;
			json = new JSONObject();
			json.put("request", "command");
			System.out.print(shell());
			while(true){
				line = sc.nextLine();
				if(line.equals("clear")){
					if(ANSI){
						System.out.print(ColorUtil.CLEAR_SCREEN);
					}
					else{
						System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
					}
					System.out.print(shell());
					continue;
				}
				if(line.equals("")){
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

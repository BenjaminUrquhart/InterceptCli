package net.intercept.client;

import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.io.*;

import org.json.JSONObject;

public class InterceptClient {

	//private static final String IP = "209.97.136.54";
	private static final String IP = "127.0.0.1";
	private static final int PORT = 13373;
	
	public static void main(String[] args) throws Exception {
		Socket conn = new Socket(IP, PORT);
		BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		PrintStream output = new PrintStream(conn.getOutputStream());
		JSONObject json = new JSONObject(input.readLine());
		JSONObject login = new JSONObject();
		Scanner sc = new Scanner(System.in);
		System.out.println("Client ID: " + json.getString("client_id"));
		System.out.println("Client type: " + json.getString("client_type"));
		System.out.println("Logged in on " + new Date(json.getLong("date")));
		System.out.println("Ready to log in.");
		json = new JSONObject();
		json.put("request", "auth");
		boolean success = false;
		while(!success){
			System.out.print("Username: ");
			login.put("username", sc.nextLine());
			System.out.print("Password: ");
			login.put("password", sc.nextLine());
			json.put("login", login);
			output.println(json);
			output.flush();
			json = new JSONObject(input.readLine());
			success = json.getBoolean("success");
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
		if(json.has("sucess") && json.getBoolean("sucess")){ //Not a typo, dev of Intercept did a goof
			System.out.println("Logged in and ready.");
			ReceiveHandler listener = new ReceiveHandler(input);
			listener.start();
			json = new JSONObject();
			json.put("request", "command");
			while(true){
				json.put("cmd", sc.nextLine());
				output.println(json);
				output.flush();
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

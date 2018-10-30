package net.intercept.client;

import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.io.*;

import org.json.JSONObject;

public class InterceptClient {

	private static final String IP = "209.97.136.54";
	private static final int PORT = 13373;
	
	@SuppressWarnings("resource")
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
		json = new JSONObject(input.readLine());
		if(json.has("success") && json.getBoolean("success")){
			System.out.println("Logged in and ready.");
			ReceiveHandler listener = new ReceiveHandler(input);
			listener.start();
			json = new JSONObject();
			json.put("request", "command");
			while(true){
				json.put("cmd", sc.nextLine());
				output.println(json);
			}
		}
		else{
			if(json.getString("event").equals("error")){
				System.out.println("An error occurred when logging in!");
				System.out.println(json.getString("error"));
				System.exit(1);
			}
		}
	}
}

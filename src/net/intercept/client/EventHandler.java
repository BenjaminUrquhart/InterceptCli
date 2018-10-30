package net.intercept.client;

import org.json.JSONObject;

public class EventHandler {

	public static void handleEvent(JSONObject json){
		String event = json.getString("event");
		String msg = "(no content)";
		boolean broadcast = event.equals("broadcast");
		if(event.equals("error")){
			msg = json.getString("error");
		}
		if(json.has("msg")){
			msg = json.getString("msg");
		}
		if(json.has("success")){
			msg = (json.getBoolean("success") ? "[SUCCESS] " : "[ERROR] ") + msg;
		}
		if(broadcast){
			msg = "[BROADCAST] " + msg;
		}
		System.out.println(msg);
	}
}

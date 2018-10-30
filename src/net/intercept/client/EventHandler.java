package net.intercept.client;

import org.json.JSONObject;

public class EventHandler {

	public static void handleEvent(JSONObject json){
		String event = json.getString("event");
		String msg = "(no content)";
		String local, remote = null;
		JSONObject conn = null;
		boolean broadcast = event.equals("broadcast");
		if(event.equals("error")){
			msg = json.getString("error");
		}
		if(event.equals("connected")){
			conn = json.getJSONObject("player");
			local = conn.getString("ip");
			remote = conn.getString("conn");
			if(local.equals(remote)){
				msg = "[INFO] Disconnected from server";
			}
			else{
				msg = String.format("[INFO] %s -> %s", local, remote);
			}
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
		System.out.println(ColorUtil.colorfy(msg));
	}
}

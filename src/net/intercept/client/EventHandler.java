package net.intercept.client;

import org.json.JSONObject;

public class EventHandler {

	public static String connectedIP = "system";
	
	public static void handleEvent(JSONObject json){
		if(!json.has("event")){
			System.out.println(
					String.format(ColorUtil.BODY, ColorUtil.RED)
					+ json.getString("error")
					+ String.format(ColorUtil.BODY, ColorUtil.RESET));
			System.out.print(InterceptClient.shell());
			return;
		}
		String event = json.getString("event");
		String msg = "(no content)";
		String local, remote = null;
		JSONObject conn = null;
		boolean broadcast = event.equals("broadcast");
		if(json.has("panic")){
			if(json.getBoolean("panic")){
				System.out.println(
						String.format(ColorUtil.BODY, ColorUtil.RED)
						+ " You are now in panic mode"
						+ String.format(ColorUtil.BODY, ColorUtil.RESET));
			}
			else{
				System.out.println(
						String.format(ColorUtil.BODY, ColorUtil.GREEN)
						+ " You are no longer in panic mode"
						+ String.format(ColorUtil.BODY, ColorUtil.RESET));
			}
		}
		if(json.has("msg")){
			msg = json.getString("msg");
		}
		if(event.equals("error")){
			msg = json.getString("error");
		}
		else if(event.equals("connected")){
			conn = json.getJSONObject("player");
			local = conn.getString("ip");
			remote = conn.getString("conn");
			if(local.equals(remote)){
				msg = "[INFO] Disconnected from server";
				connectedIP = "localhost";
			}
			else{
				connectedIP = remote;
				msg = String.format("[INFO] %s -> %s", local, remote);
			}
		}
		else if(event.equals("command") || broadcast){} //To suppress the Unknown Event message
		else if(event.equals("traceStart")){
			System.out.println("You are being traced! Remote IP: " + json.getString("system"));
		}
		else if(event.equals("chat")){
			msg = String.format(ColorUtil.BODY + "[CHAT] ", ColorUtil.GREEN) + msg + ColorUtil.RESET_STR;
		}
		else{
			System.out.println("Unknown event from data: " + json + "\n");
		}
		if(json.has("success")){
			msg = (json.getBoolean("success") ? "" : String.format(ColorUtil.BODY, ColorUtil.RED) + "[ERROR] ") + ColorUtil.RESET_STR + msg;
		}
		if(broadcast){
			msg = String.format(ColorUtil.BODY, ColorUtil.BLUE) + "[BROADCAST] " + ColorUtil.RESET_STR + msg;
		}
		System.out.println(ColorUtil.removePrefixedSpaces(ColorUtil.CLEAR_LINE + ColorUtil.RESET_CURSOR + ColorUtil.colorfy(msg)));
		System.out.print(InterceptClient.shell());
	}
}

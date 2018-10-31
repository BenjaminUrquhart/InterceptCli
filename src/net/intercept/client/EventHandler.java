package net.intercept.client;

import org.json.JSONObject;

public class EventHandler {

	public static void handleEvent(JSONObject json){
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
			msg = (json.getBoolean("success") ? "" : String.format(ColorUtil.BODY, ColorUtil.RED) + "[ERROR] ") + ColorUtil.RESET_STR + msg;
		}
		if(broadcast){
			msg = String.format(ColorUtil.BODY, ColorUtil.BLUE) + "[BROADCAST] " + ColorUtil.RESET_STR + msg;
		}
		if(event.equals("traceStart")){
			System.out.println("You are being traced! Remote IP: " + json.getString("system"));
		}
		System.out.println();
		System.out.println(ColorUtil.colorfy(msg));
		System.out.print(">> ");
	}
}

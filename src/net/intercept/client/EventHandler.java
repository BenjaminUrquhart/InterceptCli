package net.intercept.client;

import org.json.JSONObject;

import static net.intercept.client.ColorUtil.*;

public class EventHandler {

	public static String connectedIP = "system";
	private static Sound sound;
	private boolean panic;
	
	public EventHandler(double volume) {
		if(sound == null) {
			sound = InterceptClient.OGG ? new SoundHandlerOgg() : new SoundHandler(volume);
			this.panic = false;
		}
	}
	public Sound getSoundHandler() {
		return sound;
	}
	public void handleEvent(JSONObject json){
		InterceptClient.debug(json);
		if(!json.has("event")){
			System.out.println(
					RED
					+ json.getString("error")
					+ RESET);
			
			System.out.print(InterceptClient.shell());
			return;
		}
		String event = json.getString("event");
		String msg = "(no content)";
		String local, remote = null;
		JSONObject conn = null;
		boolean broadcast = event.equals("broadcast");
		if(json.has("msg")){
			msg = json.getString("msg").replace("\u001b", "\\u001b");
			InterceptClient.debug(msg);
			msg = toANSI(msg);
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
				if(panic){
					json.put("panicEnd", true);
				}
			}
			else{
				connectedIP = remote;
				msg = String.format("[INFO] %s -> %s", local, remote);
			}
		}
		else if(event.equals("command") || broadcast || event.equals("connect")){} //To suppress the Unknown Event message
		else if(event.equals("traceStart")){
			System.out.println(ANSI.RESET_CURSOR + ANSI.CLEAR_LINE + ANSI.YELLOW + "You are being traced! Remote IP: " + json.getString("system") + ANSI.RESET);
		}
		else if(event.equals("traceComplete")){
			System.out.println(ANSI.RESET_CURSOR + ANSI.CLEAR_LINE + ANSI.RED + "You have been traced from " + json.getString("system") + ANSI.RESET);
		}
		else if(event.equals("chat")){
			msg = GREEN + "[CHAT] " + msg + RESET_STR;
		}
		else if(event.equals("cfg")) {
			json.put("msg", BubColor.YELLOW + "Received config update event." + BubColor.RESET);
			msg = toANSI(json.getString("msg"));
			sound.setVolume(json.getJSONObject("cfg").getDouble("vol"));
		}
		else{
			System.out.println("Unknown event from data: " + json + "\n");
		}
		if(json.has("success")){
			msg = (json.getBoolean("success") ? "" : RED + "[ERROR] ") + RESET_STR + msg;
		}
		if(broadcast){
			msg = BLUE + "[BROADCAST] " + RESET_STR + msg;
		}
		if(json.has("panic")){
			msg = RED
					+ "You are in panic mode"
					+ RESET
					+ "\n" + msg;
			panic = true;
			sound.setTrack("breach_loop_concat");
		}
		if(json.has("panicEnd")){
			msg = GREEN
					+ "You are no longer in panic mode"
					+ RESET
					+ "\n" + msg;
			panic = false;
			sound.setTrack("peace2");
		}
		System.out.print(RESET + CLEAR_LINE + RESET_CURSOR);
		msg = msg.replace("\u200b", " ").replace("\t", " ").replace("\u00C2", "");
		System.out.println(GREEN + msg);
		System.out.print(InterceptClient.shell());
	}
}

package net.intercept.client;

import org.json.JSONObject;

public class EventHandler {

	public static String connectedIP = "system";
	private Sound sound;
	private boolean panic;
	
	public EventHandler(double volume) {
		this.sound = InterceptClient.OGG ? new SoundHandlerOgg() : new SoundHandler(volume);
		this.panic = false;
	}
	public Sound getSoundHandler() {
		return sound;
	}
	public void handleEvent(JSONObject json){
		if(!json.has("event")){
			System.out.println(
					ColorUtil.RED
					+ json.getString("error")
					+ ColorUtil.RESET);
			
			System.out.print(InterceptClient.shell());
			return;
		}
		String event = json.getString("event");
		String msg = "(no content)";
		String local, remote = null;
		JSONObject conn = null;
		boolean broadcast = event.equals("broadcast");
		if(json.has("msg")){
			msg = json.getString("msg");
			msg = ColorUtil.toANSI(msg);
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
			msg = ColorUtil.GREEN + "[CHAT] " + msg + ColorUtil.RESET_STR;
		}
		else if(event.equals("cfg")) {
			json.put("msg", BubColor.YELLOW + "Received config update event." + BubColor.RESET);
			msg = ColorUtil.toANSI(json.getString("msg"));
			sound.setVolume(json.getJSONObject("cfg").getDouble("vol"));
		}
		else{
			System.out.println("Unknown event from data: " + json + "\n");
		}
		if(json.has("success")){
			msg = (json.getBoolean("success") ? "" : ColorUtil.RED + "[ERROR] ") + ColorUtil.RESET_STR + msg;
		}
		if(broadcast){
			msg = ColorUtil.BLUE + "[BROADCAST] " + ColorUtil.RESET_STR + msg;
		}
		if(json.has("panic")){
			msg = ColorUtil.RED
					+ "You are in panic mode"
					+ ColorUtil.RESET
					+ "\n" + msg;
			panic = true;
			sound.setTrack("breach");
		}
		if(json.has("panicEnd")){
			msg = ColorUtil.GREEN
					+ "You are no longer in panic mode"
					+ ColorUtil.RESET
					+ "\n" + msg;
			panic = false;
			sound.setTrack("peace2");
		}
		if(InterceptClient.ANSI){
			System.out.print(ColorUtil.RESET + ColorUtil.CLEAR_LINE + ColorUtil.RESET_CURSOR);
		}
		else{
			if(json.has("msg")){
				msg = ColorUtil.stripBubColor(json.getString("msg"));
			}
		}
		msg = msg.replace("\u200b", " ").replace("\t", " ").replace("\u00C2", "");
		System.out.println(msg);
		System.out.print(InterceptClient.shell());
	}
}

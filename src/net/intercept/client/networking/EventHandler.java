package net.intercept.client.networking;

import static net.intercept.client.color.ANSI.*;
import static net.intercept.client.color.ColorUtil.*;

import org.json.JSONObject;

import net.intercept.client.InterceptClient;
import net.intercept.client.audio.Sound;
import net.intercept.client.audio.SoundHandler;
import net.intercept.client.audio.SoundHandlerOgg;
import net.intercept.client.color.ANSI;
import net.intercept.client.color.BubColor;

public class EventHandler {

	public static String connectedIP = "system";
	private static Sound sound;
	private boolean panic;
	
	public EventHandler(double volume) {
		if(sound == null) {
			sound = InterceptClient.OGG ? new SoundHandlerOgg(volume) : new SoundHandler(volume);
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
			//InterceptClient.debug(msg);
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
			System.out.println(ANSI.RESET_CURSOR + "" + ANSI.CLEAR_LINE + ANSI.YELLOW + "You are being traced! Remote IP: " + json.getString("system") + ANSI.RESET);
		}
		else if(event.equals("traceComplete")){
			System.out.println(ANSI.RESET_CURSOR + "" + ANSI.CLEAR_LINE + ANSI.RED + "You have been traced from " + json.getString("system") + ANSI.RESET);
		}
		else if(event.equals("chat")){
			msg = GREEN + "[CHAT] " + msg + RESET;
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
			msg = (json.getBoolean("success") ? "" : RED + "[ERROR] ") + RESET + msg;
		}
		if(broadcast){
			msg = BLUE + "[BROADCAST] " + RESET + msg;
			if(msg.contains(
					"Generating filesystem...\n" + 
					"Updating...\n" + 
					"Complete."
					)) {
				System.out.printf("%s%s%sAbandon detected.%s", CLEAR_LINE, RESET_CURSOR, YELLOW, RESET);
				String ip = msg.split(" ", 3)[2].replace("...", "").split("\n")[0], pass = InterceptClient.send(new JSONObject().put("request", "command").put("cmd", "pass see")).getString("msg");
				if(connectedIP.equals("localhost")) {
					InterceptClient.setIP(ip);
					InterceptClient.setPass(pass);
					InterceptClient.debug("Self: " + ip + " " + pass);
					System.out.println(CYAN + "Main system abandoned. Updated " + GREEN + "self" + CYAN + " info" + RESET);
				}
				else {
					System.out.printf("%s%s%sOwner of this system has abandoned. New IP: %s%s%s", CLEAR_LINE, RESET_CURSOR, ORANGE, GREEN, ip, RESET);
				}
			}
		}
		if(json.has("panic")){
			msg = RED
					+ "You are in panic mode"
					+ RESET
					+ "\n" + msg;
			panic = true;
			sound.setTrack(sound instanceof SoundHandlerOgg ? "breach" : "breach_loop_concat");
		}
		if(json.has("panicEnd")){
			msg = GREEN
					+ "You are no longer in panic mode"
					+ RESET
					+ "\n" + msg;
			panic = false;
			sound.setTrack("peace2");
		}
		System.out.print(RESET + "" + CLEAR_LINE + RESET_CURSOR);
		msg = msg.replace("\u200b", " ").replace("\t", " ").replace("\u00C2", "");
		System.out.println(GREEN + msg);
		System.out.print(InterceptClient.shell());
	}
}

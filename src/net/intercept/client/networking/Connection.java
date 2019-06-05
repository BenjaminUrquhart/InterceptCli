package net.intercept.client.networking;

import java.net.SocketException;

import org.json.JSONObject;

import net.intercept.client.InterceptClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class Connection {

	private static final WSListener listener = new WSListener();
	
	public static final OkHttpClient CLIENT = new OkHttpClient();
	
	private Request request;
	private WebSocket ws;
	
	public Connection(String address) {
		request = new Request.Builder().url(address).build();
		ws = CLIENT.newWebSocket(request, listener);
	}
	public void send(JSONObject json) {
		if(json.has("token")) {
			InterceptClient.debug(json.toString().replace(json.getString("token"), "[CENSORED]"));
		}
		else {
			InterceptClient.debug(json);
		}
		ws.send(json.toString());
	}
	public String readLine() throws SocketException {
		return listener.next();
	}
	public synchronized void close() {
		ws.close(0, "");
	}
}

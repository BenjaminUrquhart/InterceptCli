package net.intercept.client.networking;

import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import net.intercept.client.InterceptClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WSListener extends WebSocketListener {
	
	private WebSocket ws;
	private Queue<String> queue;
	private Throwable error;
	private boolean failed;
	
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	public WSListener() {
		queue = new ConcurrentLinkedQueue<>();
		failed = false;
		executor.scheduleWithFixedDelay(() -> {
			if(ws != null) {
				String json = new JSONObject().put("request", "ping").put("ts",System.currentTimeMillis()).toString();
				InterceptClient.debug(json);
				ws.send(json);
			}
		}, 25, 25, TimeUnit.SECONDS);
	}
	
	@Override
	public void onOpen(WebSocket ws, Response response) {
		this.ws = ws;
		failed = false;
		error = null;
	}
	@Override
	public void onMessage(WebSocket ws, String text) {
		JSONObject json = new JSONObject(text);
		if(json.has("token")) {
			InterceptClient.debug(text.replace(json.getString("token"), "[CENSORED]"));
		}
		else {
			InterceptClient.debug(text);
		}
		queue.add(text);
	}
	@Override
	public void onClosing(WebSocket ws, int code, String reason) {
		failed = true;
	}
	@Override
	public void onFailure(WebSocket ws, Throwable t, Response response) {
		error = t;
		failed = true;
	}
	
	public synchronized String next() throws SocketException {
		while(queue.size() == 0) {
			if(failed) {
				throw error != null ? new SocketException(error.getMessage()) : new SocketException("Websocket closed");
			}
			else {
				try {
					Thread.sleep(10);
				}
				catch(Exception e) {}
			}
		}
		return queue.poll();
	}
}

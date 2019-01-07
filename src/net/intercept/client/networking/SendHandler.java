package net.intercept.client.networking;

import java.io.PrintWriter;

import org.json.JSONObject;
@Deprecated
public class SendHandler {

	private static PrintWriter stream;
	
	public static void setWriter(PrintWriter writer) throws Exception{
		stream = writer;
	}
	public static void send(JSONObject json){
		System.out.println(json);
		stream.println(json.toString());
	}
}

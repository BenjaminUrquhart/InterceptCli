package net.intercept.client;

import java.io.BufferedReader;

import org.json.JSONObject;

public class ReceiveHandler extends Thread{

	private BufferedReader reader;
	
	public ReceiveHandler(BufferedReader reader) throws Exception{
		this.reader = reader;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run(){
		try{
			while(true)
			EventHandler.handleEvent(new JSONObject(reader.readLine()));
		}
		catch(NullPointerException e){
			this.stop(e);
		}
		catch(Exception e){
			e.printStackTrace();
			this.stop(e);
		}
	}
}

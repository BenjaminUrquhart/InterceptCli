package net.intercept.client;

import java.io.BufferedReader;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceiveHandler extends Thread{

	private BufferedReader reader;
	private EventHandler handler;
	
	public ReceiveHandler(BufferedReader reader, double volume) throws Exception{
		this.reader = reader;
		this.handler = new EventHandler(volume);
	}
	public Sound getSoundHandler() {
		return this.handler.getSoundHandler();
	}
	public void handle(JSONObject json){
		this.handler.handleEvent(json);
	}
	@Override
	public void run(){
		try{
			while(true){
				try{
					handler.handleEvent(new JSONObject(reader.readLine()));
					if(InterceptClient.ANSI)
					System.out.print(ColorUtil.RESET);
				}
				catch(ArrayIndexOutOfBoundsException e){
					System.out.println(InterceptClient.shell());
				}
				catch(StringIndexOutOfBoundsException e){
					System.out.println(InterceptClient.shell());
				}
				catch(JSONException e){
					System.out.println("An error occured while parsing data:\n" + e.getMessage());
				}
			}
		}
		catch(NullPointerException e){
			System.out.println("Connection to server dropped unexpectedly");
			System.exit(0);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
}

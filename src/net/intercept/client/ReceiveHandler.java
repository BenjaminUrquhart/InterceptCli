package net.intercept.client;

import java.io.BufferedReader;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceiveHandler extends Thread{

	private BufferedReader reader;
	private EventHandler handler;
	
	public ReceiveHandler(BufferedReader reader, double volume) throws Exception{
		this.reader = reader;
		this.handler = new EventHandler(volume);
		this.setName("Intercept Receive Handler");
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
		catch(NullPointerException | SocketException e){
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(this, e);
			System.out.println(ANSI.RESET_CURSOR + ANSI.CLEAR_LINE + ANSI.YELLOW + "Connection to server dropped unexpectedly");
			InterceptClient.reconnect();
			return;
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
}

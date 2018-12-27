package net.intercept.client;

import java.io.BufferedReader;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

import static net.intercept.client.ANSI.*;

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
					System.out.print(RESET);
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
			System.out.println(RESET_CURSOR + "" + CLEAR_LINE + YELLOW + "Connection to server dropped unexpectedly");
			InterceptClient.reconnect();
			return;
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
}

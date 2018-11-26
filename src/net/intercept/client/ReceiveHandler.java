package net.intercept.client;

import java.io.BufferedReader;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceiveHandler extends Thread{

	private BufferedReader reader;
	private EventHandler handler;
	
	public ReceiveHandler(BufferedReader reader) throws Exception{
		this.reader = reader;
		this.handler = new EventHandler();
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
				catch(JSONException e){
					System.out.println("An error occured while parsing data:\n" + e.getMessage());
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println();
		}
		catch(StringIndexOutOfBoundsException e){
			System.out.println();
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

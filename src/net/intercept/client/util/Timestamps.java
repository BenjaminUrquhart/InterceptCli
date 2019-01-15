package net.intercept.client.util;

import java.lang.reflect.Method;

public class Timestamps {

	private static Method enable, disable;
	
	public static void enable(){
		try{
			if(enable == null){
				enable = Timestamps.class.getClassLoader().loadClass("net.benjaminurquhart.stdout.STDIOPlus").getDeclaredMethod("enable");
			}
			enable.invoke(null);
		}
		catch(Exception e){
			System.err.println("Failed to enable timestamping: " + e);
		}
	}
	public static void disable(){
		try{
			if(disable == null){
				disable = Timestamps.class.getClassLoader().loadClass("net.benjaminurquhart.stdout.STDIOPlus").getDeclaredMethod("disable");
			}
			disable.invoke(null);
		}
		catch(Exception e){
			System.err.println("Failed to disable timestamping: " + e);
		}
	}
}

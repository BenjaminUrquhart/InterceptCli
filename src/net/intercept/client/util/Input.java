package net.intercept.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import net.intercept.gui.STDIN;

public class Input {

	private Scanner sc;
	private STDIN stream;
	
	private volatile String buff;
	
	public Input(InputStream stream){
		if(stream instanceof STDIN){
			this.stream = (STDIN)stream;
			this.buff = "";
		}
		else{
			this.sc = new Scanner(stream);
		}
	}
	public String nextLine(){
		if(sc != null){
			return sc.nextLine();
		}
		while(!buff.contains("\n")){
			try {
				buff += (char)stream.read();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		String[] out = buff.split("\n");
		buff = out.length == 1 ? "" : out[1];
		return out[0];
	}
}

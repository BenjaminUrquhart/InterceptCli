package net.intercept.client.util;

import java.io.InputStream;
import java.util.Scanner;

import net.intercept.gui.STDIN;

public class Input {

	private Scanner sc;
	private STDIN stream;
	
	public Input(InputStream stream){
		this.sc = new Scanner(stream);
	}
	public Input(STDIN stdin) {
		this.stream = stdin;
	}
	public String nextLine(){
		if(sc != null){
			return sc.nextLine();
		}
		return stream.nextLine();
	}
}

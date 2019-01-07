package net.intercept.client.color;

import java.util.Random;

public enum BubColor {
	
	RESET("*"),
	GRAY("W"),
	WHITE("w"),
	RED("R"),
	LIGHT_RED("r"),
	GREEN("G"),
	LIGHT_GREEN("g"),
	BLUE("B"),
	LIGHT_BLUE("b"),
	YELLOW("y"),
	ORANGE("o"),
	PINK("P"),
	LIGHT_PINK("p"),
	VIOLET("V"),
	LIGHT_VIOLET("v"),
	RANDOM("!"),
	CORRUPT("?");
	
	private final String color;
	private final Random rng;
	
	private BubColor(String color){
		this.color = ANSI.SPLIT + color;
		this.rng = new Random();
	}
	public String toString() {
		return color;
	}
	public String toANSI(){
		if(this.equals(RANDOM)) {
			BubColor out = null; 
			do {
				out = values()[rng.nextInt(values().length)];
			}
			while(out.equals(RANDOM) || out.equals(RESET)); 
			return out.toANSI();
		}
		else if(this.equals(RESET)) {
			return ANSI.RESET.toString();
		}
		else {
			try {
				return ANSI.valueOf(super.toString()).toString();
			}
			catch(Exception e) {
				return "";
			}
		}
	}
}

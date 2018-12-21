package net.intercept.client;

import java.awt.Color;
import java.util.Random;

public enum BubColor {
	
	RESET("*", null),
	GRAY("W", new Color(204,204,204)),
	WHITE("w", new Color(255,255,255)),
	RED("R", new Color(255,0,0)),
	LIGHT_RED("r", new Color(255, 135, 135)),
	GREEN("G", new Color(0,255,0)),
	LIGHT_GREEN("g", new Color(135,255,135)),
	BLUE("B", new Color(38,102,255)),
	LIGHT_BLUE("b", new Color(0,255,255)),
	YELLOW("y", new Color(255,255,0)),
	ORANGE("o", new Color(255,135,0)),
	PINK("P", new Color(255,0,255)),
	LIGHT_PINK("p", new Color(255,135,255)),
	VIOLET("V", new Color(138,41,224)),
	LIGHT_VIOLET("v", new Color(99,82,148)),
	RANDOM("?", null);
	
	private final String color;
	private final Random rng;
	private final Color colorAWT;
	
	private BubColor(String color, Color colorAWT){
		this.color = ANSI.SPLIT + color;
		this.colorAWT = colorAWT;
		this.rng = new Random();
	}
	@Override
	public String toString(){
		return color;
	}
	public String toTrueColor() {
		if(this.equals(RESET) || this.equals(RANDOM)) {
			return toANSI();
		}
		return String.format("\u001b[38;2;%d;%d;%dm", colorAWT.getRed(), colorAWT.getGreen(), colorAWT.getBlue());
	}
	public String toANSI(){
		switch(this){
		case BLUE:
			return ANSI.BLUE;
		case GRAY:
			return ANSI.WHITE;
		case GREEN:
			return ANSI.GREEN;
		case LIGHT_BLUE:
			return ANSI.BLUE;
		case LIGHT_GREEN:
			return ANSI.GREEN;
		case LIGHT_PINK:
			return ANSI.MAGENTA;
		case LIGHT_RED:
			return ANSI.RED;
		case LIGHT_VIOLET:
			return ANSI.MAGENTA;
		case ORANGE:
			return ANSI.YELLOW;
		case PINK:
			return ANSI.MAGENTA;
		case RED:
			return ANSI.RED;
		case VIOLET:
			return ANSI.MAGENTA;
		case WHITE:
			return ANSI.WHITE;
		case YELLOW:
			return ANSI.YELLOW;
		case RESET:
			return ANSI.GREEN;
		case RANDOM:
			BubColor out = null; do {out = values()[rng.nextInt(values().length)];}while(out.equals(RANDOM)); return out.toANSI();
		default:
			return "";
		}
	}
}

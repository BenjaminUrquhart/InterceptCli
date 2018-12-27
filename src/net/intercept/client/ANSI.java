package net.intercept.client;

import java.awt.Color;

public enum ANSI {
	RESET("\u001b[0;1m"),
	CLEAR_LINE("\u001b[2K"),
	CLEAR_SCREEN("\u001b[2J"),
	RESET_CURSOR("\u001b[1000D"),
	BLACK("\u001b[30;1m", "\u001b[38;5;0m", new Color(0,0,0)),
	RED("\u001b[31;1m", "\u001b[38;5;1m", new Color(255,0,0)),
	GREEN("\u001b[32;1m", "\u001b[38;5;40m", new Color(0,255,0)),
	YELLOW("\u001b[33;1m", "\u001b[38;5;226m", new Color(255,255,0)),
	BLUE("\u001b[34;1m", "\u001b[38;5;27m", new Color(38,102,255)),
	//Internal colors//
	MAGENTA("\u001b[35;1m"),
	CYAN("\u001b[36;1m"),
	//--------------//
	WHITE("\u001b[37;1m", "\u001b[38;5;15m", new Color(255,255,255)),
	GRAY(null, "\u001b[38;5;250m", new Color(204,204,204), WHITE),
	LIGHT_RED(null, "\u001b[38;5;9m", new Color(255, 135, 135), RED),
	LIGHT_GREEN(null, "\u001b[38;5;120m", new Color(135,255,135), GREEN),
	LIGHT_BLUE(null, "\u001b[38;5;51m", new Color(0,255,255), BLUE),
	ORANGE(null, "\u001b[38;5;202m", new Color(255,135,0), YELLOW),
	PINK(null, "\u001b[38;5;5m", new Color(255,0,255), MAGENTA),
	LIGHT_PINK(null, "\u001b[38;5;13m", new Color(255,135,255), MAGENTA),
	VIOLET(null, "\u001b[38;5;57m", new Color(138,41,224), MAGENTA),
	LIGHT_VIOLET(null, "\u001b[38;5;60m", new Color(99,82,148), MAGENTA);
	
	public static final String SPLIT = "\u00AC";
	
	private final String basic,extended,truecolor;
	private final ANSI substitute;
	
	private ANSI(String basic, String extended, Color truecolor, ANSI substitute) {
		this.basic = basic;
		this.extended = extended;
		this.truecolor = truecolor == null ? null : String.format("\u001b[38;2;%d;%d;%dm", truecolor.getRed(), truecolor.getGreen(), truecolor.getBlue());
		this.substitute = substitute;
	}
	private ANSI(String basic, String extended, Color truecolor) {
		this(basic, extended, truecolor, null);
	}
	private ANSI(String basic, String extended) {
		this(basic, extended, null);
	}
	private ANSI(String basic) {
		this(basic, null);
	}
	
	public String toString() {
		switch(InterceptClient.colorMode) {
		case BASIC: return this.toBasic();
		case EXTENDED: return this.toExtended();
		case TRUECOLOR: return this.toTrueColor();
		case NONE: return this.equals(CLEAR_LINE) ? "\n" : "";
		default: return "";
		}
	}
	public String toBasic() {
		return basic == null ? this.getSubstitute().toBasic() : basic;
	}
	public String toExtended() {
		return extended == null ? this.toBasic() : extended;
	}
	public String toTrueColor() {
		return truecolor == null ? this.toExtended() : truecolor;
	}
	public ANSI getSubstitute() {
		return substitute;
	}
	public static void setCursorPos(int line, int column) {
		if(!InterceptClient.colorMode.equals(ColorMode.NONE)) 
			System.out.printf("\033[%d;%dH%s", line, column, RESET_CURSOR);
	}
}

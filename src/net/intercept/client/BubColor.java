package net.intercept.client;

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
	RANDOM("?");
	
	private final String color;
	
	private BubColor(String color){
		this.color = ANSI.SPLIT + color;
	}
	@Override
	public String toString(){
		return color;
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
			return ANSI.RESET_STR;
		case RANDOM:
			BubColor out = null; do {out = values()[(int)(Math.random()*values().length)];}while(out.equals(RANDOM)); return out.toANSI();
		default:
			return "";
		}
	}
}

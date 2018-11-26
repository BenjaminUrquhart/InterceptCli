package net.intercept.client;

public enum BubColor {
	
	RESET(ANSI.SPLIT + "*"),
	GRAY(ANSI.SPLIT + "W"),
	WHITE(ANSI.SPLIT + "w"),
	RED(ANSI.SPLIT + "R"),
	LIGHT_RED(ANSI.SPLIT + "r"),
	GREEN(ANSI.SPLIT + "G"),
	LIGHT_GREEN(ANSI.SPLIT + "g"),
	BLUE(ANSI.SPLIT + "B"),
	LIGHT_BLUE(ANSI.SPLIT + "b"),
	YELLOW(ANSI.SPLIT + "y"),
	ORANGE(ANSI.SPLIT + "o"),
	PINK(ANSI.SPLIT + "P"),
	LIGHT_PINK(ANSI.SPLIT + "p"),
	VIOLET(ANSI.SPLIT + "V"),
	LIGHT_VIOLET(ANSI.SPLIT + "v"),
	UNKNOWN(ANSI.SPLIT + "?");
	
	private final String color;
	
	private BubColor(String color){
		this.color = color;
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
		default:
			return "";
		}
	}
}

package net.intercept.client;

public class ColorUtil {
	
	public static final String BODY = "\u001b[%dm";
	public static final String SPLIT = "\u00AC";
	public static final int BLACK = 30;
	public static final int RED = 31;
	public static final int GREEN = 32;
	public static final int YELLOW = 33;
	public static final int BLUE = 34;
	public static final int MAGENTA = 35;
	public static final int CYAN = 36;
	public static final int WHITE = 37;
	public static final int RESET = 0;
	
	public static String colorfy(String text){
		if(!text.contains(SPLIT)){
			return text;
		}
		return addColors(text.split(SPLIT));
	}
	private static String addColors(String[] strings){
		String out = "";
		for(int i = 1; i < strings.length; i++){
			out += String.format(BODY, getColorInt(strings[i].substring(0, 1))) + strings[i].substring(1);
		}
		out = strings[0] + out + String.format(BODY, RESET);
		return out;
	}
	private static int getColorInt(String s){
		switch(s.toLowerCase()){
		case "r": return RED;
		case "b": return BLUE;
		case "*": return RESET;
		case "y": return YELLOW;
		case "v": return MAGENTA;
		default: return GREEN;
		}
	}
}

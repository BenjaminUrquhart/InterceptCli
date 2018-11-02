package net.intercept.client;

public class ColorUtil {
	
	public static final String BODY = "\u001b[%sm";
	public static final String SPLIT = "\u00AC";
	public static final String BLACK = "30;1";
	public static final String RED = "31;1";
	public static final String GREEN = "32;1";
	public static final String YELLOW = "33;1";
	public static final String BLUE = "34;1";
	public static final String MAGENTA = "35;1";
	public static final String CYAN = "36;1";
	public static final String WHITE = "37;1";
	public static final String RESET = "0;1";
	public static final String CLEAR_LINE = "\u001b[2K";
	public static final String CLEAR_SCREEN = "\u001b[2J";
	public static final String RESET_CURSOR = "\u001b[1000D";
	
	public static final String RESET_STR = String.format(BODY, RESET);
	
	public static String colorfy(String text){
		if(!text.contains(SPLIT)){
			return text;
		}
		return addColors(text.split(SPLIT));
	}
	public static String removePrefixedSpaces(String string){
		for(int i = 0; i < string.length(); i++){
			if(string.charAt(i) == ' '){
				continue;
			}
			return string.substring(i);
		}
		return string;
	}
	private static String addColors(String[] strings){
		String out = "";
		for(int i = 1; i < strings.length; i++){
			if(InterceptClient.ANSI){
				out += String.format(BODY, getColorCode(strings[i].substring(0, 1))) + strings[i].substring(1);
			}
			else{
				out += strings[i].substring(1);
			}
		}
		out = strings[0] + out;
		if(InterceptClient.ANSI){
			 out += String.format(BODY, RESET);
		}
		return out;
	}
	private static String getColorCode(String s){
		switch(s.toLowerCase()){
		case "r": return RED;
		case "o": return RED;
		case "b": return BLUE;
		case "*": return RESET;
		case "w": return WHITE;
		case "y": return YELLOW;
		case "v": return MAGENTA;
		case "p": return MAGENTA;
		default: return GREEN;
		}
	}
}

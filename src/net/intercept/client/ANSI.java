package net.intercept.client;

public class ANSI {
	
	public static final String BODY = "\u001b[%sm";
	public static final String SPLIT = "\u00AC";
	public static final String BLACK = String.format(BODY, "30;1");
	public static final String RED = String.format(BODY, "31;1");
	public static final String GREEN = String.format(BODY, "32;1");
	public static final String YELLOW = String.format(BODY, "33;1");
	public static final String BLUE = String.format(BODY, "34;1");
	public static final String MAGENTA = String.format(BODY, "35;1");
	public static final String CYAN = String.format(BODY, "36;1");
	public static final String WHITE = String.format(BODY, "37;1");
	public static final String RESET = String.format(BODY, "0;1");
	public static final String CLEAR_LINE = "\u001b[2K";
	public static final String CLEAR_SCREEN = "\u001b[2J";
	public static final String RESET_CURSOR = "\u001b[1000D";
	public static final String RESET_STR = RESET; //Backwards compatibility
	
	public static void setCursorPos(int line, int column) {
		System.out.printf("\033[%d;%dH%s", line, column, RESET_CURSOR);
	}
}

package net.intercept.client.color;

import static net.intercept.client.color.ANSI.*;

public enum ColorMode {
	NONE, BASIC, EXTENDED, TRUECOLOR;
	
	public String toString() {
		switch(this) {
		case BASIC: return GREEN + "Basic" + RESET;
		case EXTENDED: return ORANGE + "Extended" + RESET;
		case TRUECOLOR: return String.format("%sT%sr%su%se%sC%so%sl%so%sr%s", RED,LIGHT_RED,ORANGE,YELLOW,LIGHT_GREEN,GREEN,LIGHT_BLUE,BLUE,VIOLET,RESET);
		default: return "None";
		}
	}
}

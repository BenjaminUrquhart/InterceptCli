package net.intercept.client;

import java.util.Arrays;

import static net.intercept.client.ANSI.*;

public class ColorUtil{

	public static String stripBubColor(String text){
		return text.replaceAll(SPLIT + ".", "");
	}
	public static String toANSI(String text){
		for(BubColor color : BubColor.values()){
			if(color.equals(BubColor.RANDOM) && text.contains(BubColor.RANDOM.toString())){
				text = Arrays.stream(text.split(color.toString().replace("?", "\\?"))).reduce("", (out, in) -> out + in + color.toANSI());
			}
			else{
				text = text.replace(color.toString(), color.toANSI());
			}
		}
		return text;
	}
	public static String replaceWeirdPeriod(String text) {
		return text.replace(BubColor.RANDOM + ".", "");
	}
	public static String RED() {
		return null;
	}
}

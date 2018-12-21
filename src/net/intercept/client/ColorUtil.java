package net.intercept.client;

import java.util.Arrays;

public class ColorUtil extends ANSI{

	public static String stripBubColor(String text){
		return text.replaceAll(SPLIT + ".", "");
	}
	public static String toANSI(String text){
		for(BubColor color : BubColor.values()){
			if(color.equals(BubColor.RANDOM) && text.contains(BubColor.RANDOM.toString())){
				text = Arrays.stream(text.split(color.toString().replace("?", "\\?"))).reduce("", (out, in) -> out + in + color.toANSI());
			}
			else{
				text = text.replace(color.toString(), InterceptClient.TRUECOLOR ? color.toTrueColor() : color.toANSI());
			}
		}
		return text;
	}
	public static String replaceWeirdPeriod(String text) {
		return text.replace(BubColor.RANDOM + ".", "");
	}
}

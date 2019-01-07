package net.intercept.client.color;

import static net.intercept.client.color.ANSI.*;

import java.util.Arrays;

public class ColorUtil{

	public static String stripBubColor(String text){
		return text.replaceAll(SPLIT + ".", "");
	}
	public static String toANSI(String text){
		for(BubColor color : BubColor.values()){
			if(color.equals(BubColor.RANDOM) && text.contains(BubColor.RANDOM.toString())){
				text = Arrays.stream(text.split(color.toString().replace("!", "\\!"))).reduce("", (out, in) -> out + in + color.toANSI());
			}
			else{
				text = text.replace(color.toString(), color.toANSI());
			}
		}
		return text;
	}
	public static String RED() {
		return null;
	}
}

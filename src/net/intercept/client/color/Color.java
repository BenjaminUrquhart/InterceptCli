package net.intercept.client.color;

public class Color {

	private int red;
	private int green;
	private int blue;
	private int alpha;
	
	private String rgb;
	
	public Color(int red, int green, int blue){
		this(red, green, blue, 255);
	}
	public Color(int red, int green, int blue, int alpha){
		if(red > 255 || red < 0){
			throw new IllegalArgumentException("red must be <= 255 && >= 0");
		}
		if(green > 255 || green < 0){
			throw new IllegalArgumentException("green must be <= 255 && >= 0");
		}
		if(blue > 255 || blue < 0){
			throw new IllegalArgumentException("blue must be <= 255 && >= 0");
		}
		if(alpha > 255 || alpha < 0){
			throw new IllegalArgumentException("alpha must be <= 255 && >= 0");
		}
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.rgb = Integer.toHexString(alpha) + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
	}
	public int getRed(){
		return red;
	}
	public int getGreen(){
		return green;
	}
	public int getBlue(){
		return blue;
	}
	public int getAlpha(){
		return alpha;
	}
	public int getRGB(){
		return Integer.parseInt(rgb, 16);
	}
	public String getRGBHex(){
		return rgb;
	}
}

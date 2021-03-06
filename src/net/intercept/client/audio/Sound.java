package net.intercept.client.audio;

import java.io.BufferedInputStream;

import net.intercept.client.InterceptClient;
import net.intercept.client.color.ANSI;

public interface Sound {

	default BufferedInputStream getStream(String name){
		if(name.equals("/breach_loop_concat.wav")) {
			InterceptClient.debug("Stream breach_loop_concat requested, getting concatonated stream...");
			return WAVUtil.concat(name, getStream("/breach.wav"), getStream("/breach_loop.wav"));
		}
		InterceptClient.debug("Loaded stream " + ANSI.GREEN + name);
		return new BufferedInputStream(this.getClass().getResourceAsStream(name));
	}
	default void setVolume(double volume) {}
	void setTrack(String track);
	String getTrack();
	void start(double vol);
}

package net.intercept.client;

import java.io.BufferedInputStream;

public interface Sound {

	default BufferedInputStream getStream(String name){
		InterceptClient.debug("Loaded stream " + ANSI.GREEN + name);
		return new BufferedInputStream(this.getClass().getResourceAsStream(name));
	}
	default void setVolume(double volume) {}
	void setTrack(String track);
	String getTrack();
	void start(double vol);
}

package net.intercept.client;

import java.io.BufferedInputStream;

public interface Sound {

	default BufferedInputStream getStream(String name){
		return new BufferedInputStream(this.getClass().getResourceAsStream(name));
	}
	void setTrack(String track);
	void start();
}

package net.intercept.client;

import static net.intercept.client.ANSI.CLEAR_LINE;
import static net.intercept.client.ANSI.GREEN;
import static net.intercept.client.ANSI.RESET;
import static net.intercept.client.ANSI.RESET_CURSOR;
import static net.intercept.client.ANSI.YELLOW;

import javax.sound.sampled.FloatControl;

public class SoundHandlerOgg implements Sound{

	private String track;
	private JOrbisPlayer player;
	private FloatControl volume;
	private JOrbisCleanupThread cleanup;
	
	private double vol;
	
	public boolean set, gain;
	
	public SoundHandlerOgg(double vol) {
		track = "None";
		this.vol = vol;
		this.cleanup = new JOrbisCleanupThread();
		this.cleanup.start();
		start(vol);
	}
	@Override
	public void setTrack(String track) {
		setTrack(track, true);
	}
	public void setTrack(String track, boolean set) {
		this.set = set;
		this.track = track;
		player.cleanUp();
		play();
	}
	public void nextTrack() {
		this.track = this.getNext();
		play();
	}
	@Override
	public String getTrack() {
		return track;
	}

	@Override
	public void start(double vol) {
		track = "peace";
		play();
	}
	public double getVolume() {
		return vol;
	}
	protected void setVolumeControl(FloatControl volume, boolean gain) {
		this.volume = volume;
		this.gain = gain;
		InterceptClient.debug(YELLOW + "Volume control type: " + (gain ? "Master Gain" : "Volume"));
	}
	public void setVolume(double volume) {
		InterceptClient.debug("Setting volume to " + volume);
		if(this.volume != null) {
			float vol = (float)(gain ? (Math.pow(2.0, (10.0*Math.log(volume/10.0))/6.0))*46.0206 - 40.0 : volume*10.0);
			if(vol > this.volume.getMaximum()) {
				vol = this.volume.getMaximum();
			}
			if(vol < this.volume.getMinimum() || (int)vol == -40) {
				vol = this.volume.getMinimum();
			}
			this.volume.setValue(vol);
			InterceptClient.debug("Level: " + this.volume.getValue());
			System.out.printf("%s%s%sVolume: %s%s\n", RESET_CURSOR, CLEAR_LINE, GREEN, (volume*10.0 + "%"), RESET);
		}
		else {
			System.out.println(RESET_CURSOR + "" + CLEAR_LINE + YELLOW + "Volume control not supported on this system." + RESET);
		}
	}
	public void play() {
		player = new JOrbisPlayer(getStream("/" + track + ".ogg"), this);
		player.start();
	}
	public String getNext() {
		switch(track) {
		case "peace": return "peace2";
		case "peace2": return "peace";
		case "breach": return "breach_loop";
		case "breach_loop": return "breach";
		//case "breach_loop_concat": return track;
		default: return "peace";
		}
	}
}

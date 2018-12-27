package net.intercept.client;

public class SoundHandlerOgg implements Sound{

	private String track;
	private JOrbisPlayer player;
	
	public boolean set;
	
	public SoundHandlerOgg() {
		track = "None";
		start(1.0);
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
	@Override
	public String getTrack() {
		return track;
	}

	@Override
	public void start(double vol) {
		track = "peace";
		play();
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

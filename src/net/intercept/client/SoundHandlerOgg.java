package net.intercept.client;

public class SoundHandlerOgg implements Sound{

	private String track;
	
	protected SoundHandlerOgg(){
		try {
			
		}
		catch(Exception e) {
			
		}
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public String getTrack() {
		return this.track;
	}
	public void start(double vol) {}
}

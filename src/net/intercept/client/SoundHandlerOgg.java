package net.intercept.client;

import java.util.Map;

import main.java.goxr3plus.javastreamplayer.stream.*;

public class SoundHandlerOgg implements Sound{
	
	private StreamPlayer player;
	private boolean set;
	private String track;
	
	protected SoundHandlerOgg(){
		try{
			set = false;
			track = "peace";
			player = new StreamPlayer();
			player.addStreamPlayerListener(new StreamPlayerListener(){

				@Override
				public void opened(Object dataSource, Map<String, Object> properties) {}
				@Override
				public void progress(int nEncodedBytes, long microsecondPosition, byte[] pcmData, Map<String, Object> properties) {}
				@Override
				public void statusUpdated(StreamPlayerEvent event) {
					if(event.getPlayerStatus().equals(Status.STOPPED)){
						if(!set) {
							if(track.equals("peace")){
								track = "peace2";
							}
							else if(track.equals("peace2")){
								track = "peace";
							}
							else if(track.equals("breach")){
								track = "breach_loop";
							}
							else if(track.equals("breach_loop")){
								track = "breach";
							}
						}
						try {
							set = false;
							player.open(SoundHandler.getStream("/" + track + ".ogg"));
							player.play();
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				}});
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void setTrack(String track) {
		this.track = track;
		set = true;
		player.stop();
	}

	@Override
	public void start() {
		try{
			player.open(SoundHandler.getStream("/peace.ogg"));
			player.play();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}

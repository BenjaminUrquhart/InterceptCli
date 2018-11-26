package net.intercept.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundHandler{
	
	private boolean muted = false, set = false;
	private AudioInputStream stream;
	private Clip clip = null;
	private String track;
	private File path;
	
	private LineListener listener = (event) ->{
		if(event.getType().equals(Type.STOP)){
			if(!set){
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
			else{
				clip.flush();
			}
			try {
				set = false;
				clip.close();
				stream = AudioSystem.getAudioInputStream(new File(path, track + ".wav"));
				clip.open(stream);
				clip.start();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				muted = true;
			}
		}
	};
	
	protected SoundHandler(){
		URL source = getClass().getResource("/sound/");
		path = source == null ? new File("sound/") : new File(source.getFile());
		muted = !path.exists();
		track = "peace";
		try{
			clip = AudioSystem.getClip();
			clip.addLineListener(listener);
		}
		catch(Exception e){
			muted = true;
		}
		start();
	}
	protected void setTrack(String track){
		this.track = track;
		this.set = true;
		this.clip.stop();
	}
	public void start(){
		if(muted){
			return;
		}
		try{
			stream = AudioSystem.getAudioInputStream(new File(path, track + ".wav"));
			clip.open(stream);
			clip.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}

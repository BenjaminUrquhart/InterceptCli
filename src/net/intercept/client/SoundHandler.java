package net.intercept.client;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundHandler implements Sound{
	
	private boolean muted = false, set = false;
	private AudioInputStream stream;
	private FloatControl volume;
	private Clip clip = null;
	private String track;
	
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
				stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
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
	
	protected SoundHandler(double vol){
		if(!InterceptClient.MUTE){
			track = "peace";
			try{
				clip = AudioSystem.getClip(null);
				clip.addLineListener(listener);
				volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				this.setVolume(vol);
			}
			catch(IllegalArgumentException e) {}
			catch(Exception e){
				e.printStackTrace();
				muted = true;
			}
			start();
		}
	}
	public void setVolume(double volume) {
		if(this.volume != null) {
			this.volume.setValue((float) (Math.log(volume) / Math.log(10.0) * 20.0));
			System.out.printf("Volume: %s\n", (volume*100.0 + "%"));
		}
		else {
			System.out.println(ANSI.RESET_CURSOR + ANSI.CLEAR_LINE + ANSI.YELLOW + "Volume control not supported on this system." + ANSI.RESET);
		}
	}
	public void setTrack(String track){
		if(muted) return;
		this.track = track;
		this.set = true;
		this.clip.stop();
	}
	public void start(){
		if(muted || InterceptClient.MUTE){
			return;
		}
		try{
			stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
			clip.open(stream);
			clip.start();
		}
		catch(FileNotFoundException | IllegalArgumentException e){
			System.out.println(e + "\nSound disabled");
			muted = true;
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}

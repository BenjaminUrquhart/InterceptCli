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
	private AudioInputStream stream, next;
	private FloatControl volume;
	private Clip clip = null;
	private String track;
	
	private LineListener listener = (event) ->{
		if(event.getType().equals(Type.STOP)){
			try {
				if(!set){
					track = getNext();
					stream = next;
				}
				else {
					clip.flush();
					stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
				}
				set = false;
				clip.close();
				clip.open(stream);
				clip.start();
				next = AudioSystem.getAudioInputStream(getStream("/" + getNext() + ".wav"));
			} catch (UnsupportedAudioFileException | IOException e) {
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				muted = true;
			}
		}
	};
	
	protected SoundHandler(double vol){
		if(!InterceptClient.MUTE){
			track = "None";
			try{
				clip = AudioSystem.getClip(null);
				clip.addLineListener(listener);
				volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				this.setVolume(vol);
			}
			catch(IllegalArgumentException e) {}
			catch(NullPointerException e) {
				InterceptClient.MUTE = true;
				muted = true;
			}
			catch(Exception e){
				e.printStackTrace();
				InterceptClient.MUTE = true;
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
		if(muted || clip == null) return;
		this.track = track;
		this.set = true;
		this.clip.stop();
	}
	public String getTrack() {
		return new String(track.toCharArray());
	}
	public String getNext() {
		switch(track) {
		case "peace": return "peace2";
		case "peace2": return "peace";
		case "breach": return "breach_loop";
		case "breach_loop": return "breach";
		default: return "peace";
		}
	}
	public void start(){
		if(muted || InterceptClient.MUTE){
			return;
		}
		try{
			track = "peace";
			stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
			next = AudioSystem.getAudioInputStream(getStream("/" + getNext() + ".wav"));
			clip.open(stream);
			clip.start();
		}
		catch(FileNotFoundException | IllegalArgumentException e){
			System.out.println(e + "\nSound disabled");
			InterceptClient.MUTE = true;
			muted = true;
		}
		catch(Exception e){
			e.printStackTrace();
			InterceptClient.MUTE = true;
			muted = true;
		}
	}
}

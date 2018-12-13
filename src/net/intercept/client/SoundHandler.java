package net.intercept.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundHandler implements Sound{
	
	private boolean set = false, gain = false;
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
					InterceptClient.debug("Track set to " + track);
					stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
				}
				set = false;
				clip.close();
				clip.open(stream);
				clip.start();
				InterceptClient.debug("Now playing: " + ANSI.GREEN + track);
				next = AudioSystem.getAudioInputStream(getStream("/" + getNext() + ".wav"));
			} catch (UnsupportedAudioFileException | IOException e) {
				try{
					clip.close();
				}
				catch(Exception exec) {
					Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
				track = "None";
				InterceptClient.MUTE = true;
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
			} catch (LineUnavailableException e) {
				InterceptClient.debug(e.toString());
				InterceptClient.MUTE = true;
			}
		}
	};
	private LineListener loopLineListener = (event) -> {
		if(event.getType().equals(Type.STOP)){
			try {
				if(!set){
					track = getNext();
					stream = next;
				}
				else {
					clip.flush();
					InterceptClient.debug("Track set to " + track);
					stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
				}
				next = AudioSystem.getAudioInputStream(getStream("/" + getNext() + ".wav"));
				if(track.equals("breach_loop_concat")) {
					stream = AudioSystem.getAudioInputStream(WAVUtil.concat("breach_loop_concat", stream, next));
				}
				set = false;
				clip.close();
				clip.open(stream);
				clip.start();
				InterceptClient.debug("Now playing: " + ANSI.GREEN + track);
			} catch (UnsupportedAudioFileException | IOException e) {
				try{
					clip.close();
				}
				catch(Exception exec) {
					Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
				track = "None";
				InterceptClient.MUTE = true;
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
			} catch (LineUnavailableException e) {
				InterceptClient.debug(e.toString());
				InterceptClient.MUTE = true;
			}
		}
	};
	
	protected SoundHandler(double vol){
		track = "None";
		if(!InterceptClient.MUTE){
			InterceptClient.debug("Starting sound handler...");
			start(vol);
		}
		else {
			InterceptClient.debug("Client is muted, not starting SoundHandler");
		}
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
			System.out.printf("%s%s%sVolume: %s%s\n", ANSI.RESET_CURSOR, ANSI.CLEAR_LINE, ANSI.GREEN, (volume*10.0 + "%"), ANSI.RESET);
		}
		else {
			System.out.println(ANSI.RESET_CURSOR + ANSI.CLEAR_LINE + ANSI.YELLOW + "Volume control not supported on this system." + ANSI.RESET);
		}
	}
	public void setTrack(String track){
		if(InterceptClient.MUTE || clip == null) return;
		if(track.equals("breach_loop_concat")) {
			clip.removeLineListener(listener);
			clip.addLineListener(loopLineListener);
		}
		this.track = track;
		this.set = true;
		this.clip.stop();
	}
	public String getTrack() {
		return track;
	}
	public String getNext() {
		switch(track) {
		case "peace": return "peace2";
		case "peace2": return "peace";
		case "breach": return "breach_loop";
		case "breach_loop": return "breach";
		case "breach_loop_concat": return track;
		default: return "peace";
		}
	}
	public void start(double vol){
		try{
			track = "peace";
			stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
			next = AudioSystem.getAudioInputStream(getStream("/" + getNext() + ".wav"));
			clip = AudioSystem.getClip(null);
			clip.open(stream);
			try{
				Info[] mixers = AudioSystem.getMixerInfo();
				InterceptClient.debug("Mixers: ");
				Arrays.stream(mixers).map((info) -> AudioSystem.getMixer(info)).forEach((mixerinfo) -> InterceptClient.debug(String.format("%s%s (%s)", ANSI.YELLOW, mixerinfo.getMixerInfo().toString(), mixerinfo.toString())));
				InterceptClient.debug("Clip: " + ANSI.YELLOW + clip.getLineInfo());
				InterceptClient.debug("Controls: " + ANSI.YELLOW + Arrays.toString(clip.getControls()));
				clip.addLineListener(listener);
			}
			catch(Exception e){
				InterceptClient.debug(e);
				InterceptClient.MUTE = true;
			}
			try {
				volume = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
			}
			catch(IllegalArgumentException e) {
				InterceptClient.debug(e);
				InterceptClient.debug("Control type " + ANSI.GREEN + "Volume" + ANSI.CYAN + " unsupported, trying " + ANSI.GREEN + "Master Gain...");
				gain = true;
				try {
					volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				}
				catch(IllegalArgumentException exec) {
					InterceptClient.debug(exec);
					InterceptClient.debug("Volume control disabled");
				}
			}
			this.setVolume(vol);
			clip.start();
			InterceptClient.debug("Now playing: " + ANSI.GREEN + track);
			
		}
		catch(FileNotFoundException | IllegalArgumentException e){
			InterceptClient.debug(e);
			System.out.println(e + "\nSound disabled");
			InterceptClient.MUTE = true;
		}
		catch(Exception e){
			e.printStackTrace();
			InterceptClient.MUTE = true;
		}
	}
}

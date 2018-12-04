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
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				InterceptClient.MUTE = true;
			}
		}
	};
	
	protected SoundHandler(double vol){
		track = "None";
		if(!InterceptClient.MUTE){
			InterceptClient.debug("Starting sound handler...");
			try{
				Info mixer = null;
				Info[] mixers = AudioSystem.getMixerInfo();
				InterceptClient.debug("Mixers: ");
				Arrays.stream(mixers).forEach((info) -> InterceptClient.debug(ANSI.YELLOW + info));
				for(Info info : mixers) {
					if(AudioSystem.getMixer(info).isControlSupported(FloatControl.Type.VOLUME) || AudioSystem.getMixer(info).isControlSupported(FloatControl.Type.MASTER_GAIN)) {
						mixer = info;
						break;
					}
				}
				InterceptClient.debug("Selected mixer: " + ANSI.GREEN + mixer);
				clip = AudioSystem.getClip(mixer);
				InterceptClient.debug("Clip: " + ANSI.YELLOW + clip);
				InterceptClient.debug("Line: " + ANSI.YELLOW + clip.getLineInfo());
				InterceptClient.debug("Controls: " + ANSI.YELLOW + Arrays.toString(clip.getControls()));
				clip.addLineListener(listener);
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
			}
			catch(Exception e){
				InterceptClient.debug(e);
				InterceptClient.MUTE = true;
			}
			start();
		}
		else {
			InterceptClient.debug("Client is muted, not starting SoundHandler");
		}
	}
	public void setVolume(double volume) {
		InterceptClient.debug("Setting volume to " + volume);
		if(this.volume != null) {
			this.volume.setValue((float)(gain ? (Math.log(volume) / Math.log(10.0) * 20.0) : volume*100.0));
			InterceptClient.debug("Level: " + this.volume.getValue());
			System.out.printf("Volume: %s\n", (volume*100.0 + "%"));
		}
		else {
			System.out.println(ANSI.RESET_CURSOR + ANSI.CLEAR_LINE + ANSI.YELLOW + "Volume control not supported on this system." + ANSI.RESET);
		}
	}
	public void setTrack(String track){
		if(InterceptClient.MUTE || clip == null) return;
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
		default: return "peace";
		}
	}
	public void start(){
		if(InterceptClient.MUTE || clip == null) return;
		try{
			track = "peace";
			stream = AudioSystem.getAudioInputStream(getStream("/" + track + ".wav"));
			next = AudioSystem.getAudioInputStream(getStream("/" + getNext() + ".wav"));
			clip.open(stream);
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

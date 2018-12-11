package net.intercept.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WAVUtil {
	
	private static File tempFile = null;
	
	public static BufferedInputStream concat(String name, InputStream clip1, InputStream clip2) {
		try {
			return concat(name, AudioSystem.getAudioInputStream(clip1), AudioSystem.getAudioInputStream(clip2));
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
    public static BufferedInputStream concat(String name, AudioInputStream clip1, AudioInputStream clip2) {
        try {
        	if(tempFile == null) {
        		tempFile = File.createTempFile(name, ".wav");
                AudioInputStream appendedFiles = new AudioInputStream(new SequenceInputStream(clip1, clip2), clip1.getFormat(), clip1.getFrameLength() + clip2.getFrameLength());
                AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, tempFile);
        	}
            return new BufferedInputStream(new FileInputStream(tempFile));
        } 
        catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }
}

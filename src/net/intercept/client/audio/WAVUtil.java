package net.intercept.client.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.intercept.client.InterceptClient;
import net.intercept.client.color.ANSI;

public class WAVUtil {
	
	private static File tempFile = null;
	
	public static BufferedInputStream concat(String name, InputStream clip1, InputStream clip2) {
		try {
			return concat(name, AudioSystem.getAudioInputStream(clip1), AudioSystem.getAudioInputStream(clip2));
		}
		catch(Exception e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
			return null;
		}
	}
    public static BufferedInputStream concat(String name, AudioInputStream clip1, AudioInputStream clip2) {
        try {
        	if(tempFile == null || !tempFile.exists()) {
            	InterceptClient.debug(String.format("Clip 1: %s%s (Size: %d bytes)", ANSI.GREEN, clip1.getFormat().toString(), clip1.available()));
            	InterceptClient.debug(String.format("Clip 2: %s%s (Size: %d bytes)", ANSI.GREEN, clip2.getFormat().toString(), clip2.available()));
        		InterceptClient.debug("Temporary file does not exist, creating a new one...");
        		tempFile = File.createTempFile(name.split("\\.", 2)[0], "." + name.split("\\.", 2)[1]);
        		InterceptClient.debug("Created temporary file at " + ANSI.GREEN + tempFile.getAbsolutePath());
        		InterceptClient.debug("Concatonating...");
                AudioInputStream appendedFiles = new AudioInputStream(new SequenceInputStream(clip1, clip2), clip1.getFormat(), clip1.getFrameLength() + clip2.getFrameLength());
                InterceptClient.debug("Concatonated streams. Writing to temporary file...");
                InterceptClient.debug("Bytes to write: " + ANSI.GREEN + (clip1.available() + clip2.available()));
                AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, tempFile);
                InterceptClient.debug("Done.");
        	}
        	else {
        		InterceptClient.debug("Temporary file found, returning stream...");
        	}
            return new BufferedInputStream(new FileInputStream(tempFile));
        } 
        catch (Exception e) {
        	Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            return null;
        }
    }
}

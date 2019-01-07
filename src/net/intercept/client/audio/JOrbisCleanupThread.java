package net.intercept.client.audio;

import static net.intercept.client.color.ANSI.*;

import java.util.LinkedHashSet;
import java.util.Set;

import net.intercept.client.InterceptClient;

public class JOrbisCleanupThread extends Thread {

	private volatile boolean run;
	private Set<Thread> toKill;
	
	public JOrbisCleanupThread() {
		this.setName("Intercept Audio Cleanup");
		this.toKill = new LinkedHashSet<>();
	}
	@Override
	public void start() {
		InterceptClient.debug(GREEN + "Starting JOrbis cleanup thread...");
		this.run = true;
		super.start();
	}
	@Override
	public void run() {
		InterceptClient.debug(GREEN + "Started");
		while(run) {
			try {
				toKill.clear();
				Thread.getAllStackTraces().forEach((thread, trace) -> {
					if(thread instanceof JOrbisPlayer) {
						toKill.add(thread);
					}
				});
				if(toKill.size() > 1) {
					InterceptClient.debug(ORANGE + "Found " + (toKill.size() - 1) + " excess JOrbis threads");
				}
				while(toKill.size() > 1) {
					try {
						new LinkedHashSet<Thread>(toKill).forEach((thread) -> {
							if(toKill.size() == 1) {
								return;
							}
							((JOrbisPlayer)thread).cleanUp(true);
							toKill.remove(thread);
						});
					}
					catch(NullPointerException e) {
						toKill.clear();
					}
					catch(Exception e) {
						InterceptClient.debug("Failed to kill excess process: " + e);
					}
				}
				Thread.sleep(500);
			}
			catch(Exception e) {}
		}
		run = this.isInterrupted();
	}
}

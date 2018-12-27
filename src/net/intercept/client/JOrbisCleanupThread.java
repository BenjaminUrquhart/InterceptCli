package net.intercept.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JOrbisCleanupThread extends Thread {

	private volatile boolean run;
	private List<Thread> toKill;
	
	public JOrbisCleanupThread() {
		this.setName("Intercept Audio Thread Cleanup");
		this.toKill = new ArrayList<>();
	}
	@Override
	public void start() {
		this.run = true;
		super.start();
	}
	@Override
	public void run() {
		while(run) {
			try {
				Thread.getAllStackTraces().forEach((thread, trace) -> {
					toKill.add(thread);
				});
				toKill = toKill.stream().filter((thread) -> thread instanceof JOrbisPlayer).collect(Collectors.toList());
				if(toKill.size() > 1) {
					InterceptClient.debug("Found " + (toKill.size() - 1) + " excess JOrbis threads");
				}
				while(toKill.size() > 1) {
					try {
						((JOrbisPlayer)toKill.remove(0)).cleanUp();
					}
					catch(Exception e) {
						InterceptClient.debug("Failed to kill excess process: " + e);
					}
				}
				Thread.sleep(500);
			}
			catch(Exception e) {}
		}
	}/*
	@Override
	public void stop() {
		this.run = false;
	}*/
}

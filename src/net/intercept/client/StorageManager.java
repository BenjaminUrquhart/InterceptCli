package net.intercept.client;

import java.io.File;

public class StorageManager {
	private static final File HOME = new File(System.getenv("HOME"));
	private static File STORAGE_DIR;
	
	public static File getStorageDir(){
		if(STORAGE_DIR == null){
			STORAGE_DIR = new File(HOME, ".interceptcli");
		}
		if(!STORAGE_DIR.exists()){
			STORAGE_DIR.mkdirs();
		}
		return STORAGE_DIR;
	}
}

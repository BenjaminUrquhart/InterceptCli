package net.intercept.client;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;

public class MacroManager {

	private static HashMap<String, JSONObject> macros;
	
	protected static void loadMacros(){
		InterceptClient.debug("Loading macros...");
		macros = new HashMap<>();
		File storage = StorageManager.getStorageDir();
		File macroStorage = new File(storage, "macros.json");
		try{
			if(!macroStorage.exists()){
				InterceptClient.debug("Macros file missing, creating a new one...");
				macroStorage.createNewFile();
				InterceptClient.debug("Created macros.json");
			}
			JSONObject json = new JSONObject(Files.lines(macroStorage.toPath()).reduce("", (out, in) -> out + in));
		}
		catch(Exception e){
			System.out.println(ANSI.YELLOW + "Failed to load macros:\n" + e);
			Arrays.stream(e.getStackTrace()).forEach((trace) -> InterceptClient.debug(ColorUtil.YELLOW + trace));
			System.out.print(ANSI.RESET);
		}
	}
}

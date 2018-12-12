package net.intercept.client;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class MacroManager {

	private static HashMap<String, String> macros;
	private static File macroStorage;
	
	protected static void loadMacros(){
		InterceptClient.debug("Loading macros...");
		macros = new HashMap<>();
		macroStorage = new File(StorageManager.getStorageDir(), "macros.json");
		try{
			if(!macroStorage.exists()){
				InterceptClient.debug("Macros file missing, creating a new one...");
				Files.write(macroStorage.toPath(), "[]".getBytes());
				InterceptClient.debug("Created macros.json");
			}
			JSONArray jsonArr = new JSONArray(Files.lines(macroStorage.toPath()).reduce("", (out, in) -> out + in));
			InterceptClient.debug(jsonArr);
			jsonArr.forEach((obj) -> {
				try {
					@SuppressWarnings("unchecked")
					JSONObject json = new JSONObject((HashMap<String, Object>)obj);
					macros.put(json.getString("name"), json.getString("cmd"));
				}
				catch(Exception e) {
					InterceptClient.debug("Failed to load macro " + obj + ":\n" + e);
				}
			});
			System.out.println("Loaded " + macros.values().size() + " macros.");
		}
		catch(Exception e){
			System.out.println(ANSI.YELLOW + "Failed to load macros:\n" + e);
			Arrays.stream(e.getStackTrace()).forEach((trace) -> InterceptClient.debug(ColorUtil.YELLOW + trace));
			System.out.print(ANSI.RESET);
		}
	}
	public String getMacro(String macro) {
		return macros.get(macro);
	}
	public void addMacro(String name, String cmd) {
		macros.put(name, cmd);
		try {
			Files.write(macroStorage.toPath(), new JSONArray(
					Files.lines(macroStorage.toPath()).reduce("", (out, in) -> out + in))
					.put(new JSONObject()
					.put("name", name)
					.put("cmd", cmd))
				.toString()
				.getBytes());
		}
		catch(Exception e) {}
	}
}

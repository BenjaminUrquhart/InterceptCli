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
			System.out.println(ANSI.YELLOW + "Failed to load macros: " + e);
			Arrays.stream(e.getStackTrace()).forEach((trace) -> InterceptClient.debug(ColorUtil.YELLOW + trace));
			System.out.print(ANSI.RESET);
		}
	}
	public String getMacro(String macro) {
		return macros.get(macro);
	}
	public boolean removeMacro(String macro) {
		if(getMacro(macro) == null) {
			return false;
		}
		macros.remove(macro);
		try {
			JSONArray arr = new JSONArray(Files.lines(macroStorage.toPath()).reduce("", (out, in) -> out + in));
			int index = 0;
			for(int i = 0; i < arr.length(); i++) {
				if(arr.getJSONObject(i).getString("name").equals(macro)) {
					index = i;
					break;
				}
			}
			arr.remove(index);
			Files.write(macroStorage.toPath(), arr.toString().getBytes());
			return true;
		}
		catch(Exception e) {
			InterceptClient.debug(e);
		}
		return false;
	}
	public boolean setMacro(String name, String cmd) {
		macros.put(name, cmd);
		try {
			Files.write(macroStorage.toPath(), new JSONArray(
					Files.lines(macroStorage.toPath()).reduce("", (out, in) -> out + in))
					.put(new JSONObject()
					.put("name", name)
					.put("cmd", cmd))
				.toString()
				.getBytes());
			return true;
		}
		catch(Exception e) {
			InterceptClient.debug(e);
		}
		return false;
	}
}

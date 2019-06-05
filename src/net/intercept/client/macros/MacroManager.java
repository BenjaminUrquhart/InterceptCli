package net.intercept.client.macros;

import static net.intercept.client.color.ANSI.*;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import net.intercept.client.InterceptClient;
import net.intercept.client.storage.StorageManager;

public class MacroManager {

	private static HashMap<String, String> macros;
	private static File macroStorage;
	
	public static void loadMacros(){
		InterceptClient.debug("Loading macros...");
		macros = new HashMap<>();
		try{
			macroStorage = new File(StorageManager.getStorageDir(), "macros.json");
			if(!macroStorage.exists()){
				InterceptClient.debug("Macros file missing, creating a new one...");
				Files.write(macroStorage.toPath(), "[]".getBytes());
				InterceptClient.debug("Created macros.json");
			}
			JSONArray jsonArr = new JSONArray(Files.lines(macroStorage.toPath(), Charset.defaultCharset()).reduce("", (out, in) -> out + in));
			InterceptClient.debug(jsonArr);
			jsonArr.forEach((obj) -> {
				try {
					JSONObject json = (JSONObject)obj;
					macros.put(json.getString("name"), json.getString("cmd"));
				}
				catch(Exception e) {
					InterceptClient.debug("Failed to load macro " + obj + ":\n" + e);
				}
			});
			System.out.println(CLEAR_LINE + "" + RESET_CURSOR + GREEN + "Loaded " + macros.values().size() + " macros.");
		}
		catch(ExceptionInInitializerError e) {
			System.out.println(YELLOW + "Failed to load macros: " + e);
			Arrays.stream(e.getStackTrace()).forEach((trace) -> InterceptClient.debug(YELLOW.toString() + trace));
			System.out.print(RESET);
		}
		catch(Exception e){
			System.out.println(YELLOW + "Failed to load macros: " + e);
			Arrays.stream(e.getStackTrace()).forEach((trace) -> InterceptClient.debug(YELLOW.toString() + trace));
			System.out.print(RESET);
		}
	}
	public static String getMacro(String macro) {
		return macros.get(macro);
	}
	public static HashMap<String, String> getMacros(){
		return new HashMap<String, String>(macros);
	}
	public static boolean removeMacro(String macro) {
		if(getMacro(macro) == null) {
			InterceptClient.debug("Failed to remove macro: Macro does not exist.");
			return false;
		}
		macros.remove(macro);
		try {
			JSONArray arr = new JSONArray(Files.lines(macroStorage.toPath(), Charset.defaultCharset()).reduce("", (out, in) -> out + in));
			int index = -1;
			for(int i = 0; i < arr.length(); i++) {
				if(arr.getJSONObject(i).getString("name").equals(macro)) {
					index = i;
					break;
				}
			}
			if(index == -1) {
				InterceptClient.debug("Failed to remove macro: Macro is not saved to disk.");
				return false;
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
	public static boolean addMacro(String name, String cmd){
		return setMacro(name, cmd, false);
	}
	public static boolean setMacro(String name, String cmd, boolean force) {
		if((!force) && macros.get(name) != null){
			InterceptClient.debug("Failed to add macro: Macro already exists.");
			return false;
		}
		macros.put(name, cmd);
		try {
			Files.write(macroStorage.toPath(), new JSONArray(
					Files.lines(macroStorage.toPath(), Charset.defaultCharset()).reduce("", (out, in) -> out + in))
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

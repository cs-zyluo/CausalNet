package edu.sjtu.copa.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
	public static final String MAIN_CONFIG = "light-copa-config.ini";
	
	private static Map<String, IniParser> config = new HashMap<String, IniParser>();
	
	public static String getConfig(String file, String section, String key) {
		if (config.get(file) == null) {
			config.put(file, new IniParser(file));
		}
		return config.get(file).getConfig(section, key);
	}
}

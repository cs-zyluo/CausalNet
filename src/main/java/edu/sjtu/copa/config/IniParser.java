package edu.sjtu.copa.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IniParser {
	private static final String EMPTY = "";

	private String path_;
	private Map<String, Map<String, String>> section_;

	public IniParser(String path) {
		this.path_ = path;
		this.section_ = new HashMap<String, Map<String, String>>();
		update();
	}

	public String getConfig(String section, String key) {
		if (section_.get(section) == null
				|| section_.get(section).get(key) == null) {
			return EMPTY;
		}
		return section_.get(section).get(key);
	}

	public void update() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.path_));
			String line = null;
			Map<String, String> curSection = null;
			int eqPos = 0;

			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				} else if (line.startsWith("[") && line.endsWith("]")) {
					curSection = new HashMap<String, String>();
					section_.put(line.substring(1, line.length() - 1),
							curSection);
				} else {
					eqPos = line.indexOf('=');
					if (eqPos == -1) {
						curSection.put(line, EMPTY);
					} else {
						curSection.put(line.substring(0, eqPos).trim(), line
								.substring(eqPos + 1).trim());
					}
				}
			}
			
			br.close();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// should never get here
		System.exit(1);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String sec : section_.keySet()) {
			sb.append("[").append(sec).append("]").append("\n");
			for (String key : section_.get(sec).keySet()) {
				sb.append(key).append("=").append(section_.get(sec).get(key)).append("\n");
			}
		}
		return sb.toString();
	}
	
	public static void main(String[] argv) {
		IniParser parser = new IniParser("light-copa-config.ini");
		System.out.println(parser);
		System.out.println(parser.getConfig("doc-dir", "COPA_ALL"));
	}
}

package edu.sjtu.copa.fix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.sjtu.copa.config.ConfigManager;

public class AsymmetricPMI {
	//public final static String freqFile = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "BING_WORD_COPA");
	//public final static String freqFile = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "BING_WORD_COPA");
			//"/home/zhiyi/code/baseaaai/recoverbase/w5_Bing_word_COPA.txt";//"/home/yuchen/data/copa_rebuttal/word.txt";// /home/yuchen/data/copa_rebuttal/word.txt
	// Result/Bing_word_COPA.txt /home/yuchen/data/copa_rebuttal3w/word.txt
	public final static String coocurFile = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "FREQ_CORPUS");
			//"/home/zhiyi/data/copacp.txt"; //"Bing_co_COPA.txt";//"/home/yuchen/data/copa_rebuttal/pair2.txt";// /home/yuchen/data/copa_rebuttal/pair2.txt
	// Result/Bing_co_COPA.txt /home/yuchen/data/copa_rebuttal3w/pair2.txt
	public final static double corpusSize =  986837084.0 ;//1227034465.0 ; // 690243422.0 // 1823279666.0
	// 690243422000.0 (Bing)
	static HashMap<String, HashMap<String,Double>> coocurMap = new HashMap<String, HashMap<String,Double>>();
	static HashMap<String,Double> freqMap = new HashMap<String,Double>();
	static Set<String> removeSet = ImmutableSet.of("xxxx");
	
	static {
		
		/*
		try {
			// load freqMap
			BufferedReader reader = new BufferedReader(new InputStreamReader
					(new FileInputStream(freqFile)));
			String line = null;
			while ((line = reader.readLine()) != null){
				String[] linePart = line.split("\t");
				String word = linePart[0];
				if(removeSet.contains(word)){
					continue;
				}
				Double freq = Double.parseDouble(linePart[1]);
				freqMap.put(word,freq);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		try {
			// load freqMap
			BufferedReader reader = new BufferedReader(new InputStreamReader
					(new FileInputStream(coocurFile)));
			
			String line = null;
			
			while ((line = reader.readLine()) != null){
				String[] linePart = line.split("\t");
				String cause = linePart[0];
				String effect = linePart[1];
				Double cofreq = Double.parseDouble(linePart[2]);
				
				if(cofreq == 0.0){continue;}
				
				if (!coocurMap.containsKey(cause)){
					coocurMap.put(cause, new HashMap<String,Double>());
				}
				
				if (!coocurMap.get(cause).containsKey(effect)){
					coocurMap.get(cause).put(effect, cofreq);
				}
				
				if (!freqMap.containsKey(cause)) freqMap.put(cause, 0.0);
				freqMap.put(cause,freqMap.get(cause)+cofreq);
				if (!freqMap.containsKey(effect)) freqMap.put(effect, 0.0);
				freqMap.put(effect,freqMap.get(effect)+cofreq);
			}
			
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static double calPMI(String x, String y, BufferedWriter writer) throws IOException{
		
		if (!freqMap.containsKey(x) || !freqMap.containsKey(y)){
			
			return 0;
		}
		
		double p_xy = 0;
		
		if (coocurMap.containsKey(x) && coocurMap.get(x).containsKey(y)){
			p_xy = coocurMap.get(x).get(y);
			
		} else{
			
			return 0;
		}
		
		double p_x = freqMap.get(x);
		double p_y = freqMap.get(y);
		double s = p_xy/(p_x*p_y) * corpusSize;
		
		writer.write(x+"\t"+"p_x:"+p_x+"\t"+y+"\tp_y:"+p_y+"\tp_xy:"+p_xy+"\n");
		
		//return s;
		return Math.log(s)/Math.log(2);
	}
	
	
}

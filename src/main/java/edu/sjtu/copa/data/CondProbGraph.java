package edu.sjtu.copa.data;

import java.util.*;
import java.io.*;

public class CondProbGraph {
	
	HashMap<String, HashMap<String, Double>> condprobGraph1; 
	HashMap<String, HashMap<String, Double>> condprobGraph2;
	
	public CondProbGraph(String filename1,String filename2) throws Exception{
		this.condprobGraph1 = loadCondprobGraph(filename1);
		this.condprobGraph2 = loadCondprobGraph(filename2);
	}
	
	public HashMap<String, HashMap<String, Double>> getCondProbGraph1(){
		return condprobGraph1;
	}
	
	public HashMap<String, HashMap<String, Double>> getCondProbGraph2(){
		return condprobGraph2;
	}
	
	@SuppressWarnings("resource")
	public HashMap<String, HashMap<String, Double>> loadCondprobGraph(String inputPath) throws Exception{
		File file = new File(inputPath);
		HashMap<String, HashMap<String, Double>> condprobGraph = 
				new HashMap<String,HashMap<String,Double>>();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), "UTF-8"));
		
		String line = null;
		while((line = bufferedReader.readLine()) != null){
			String lineParts[] = line.split("\t");
			String word1 = lineParts[0];
			String word2 = lineParts[1];
			double weights = Double.parseDouble(lineParts[2]);
			if (!condprobGraph.containsKey(word1)){
				condprobGraph.put(word1, new HashMap<String, Double>());
			}
			if (!condprobGraph.get(word1).containsKey(word2)) {
				condprobGraph.get(word1).put(word2, weights);
			}
		}
		
		System.out.println("Conditional probability graph loading is done! (?/2)");
		return condprobGraph;
	}
	
}

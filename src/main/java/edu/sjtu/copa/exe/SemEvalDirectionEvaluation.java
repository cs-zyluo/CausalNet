package edu.sjtu.copa.exe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class SemEvalDirectionEvaluation {
	
	static HashMap<String, HashMap<String,Double>> ruvmap;
	static BufferedWriter logger;
	static {
		try {
			ruvmap = loadRUVMap(new File("/home/jessie/code/copa/cs.txt"));
			logger = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/jessie/code/copa/semeval/log.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, HashMap<String,Double>> loadRUVMap(File file)
			throws IOException {
		System.out.println(file.getAbsolutePath());
		HashMap<String, HashMap<String,Double>> map = new HashMap<String, HashMap<String,Double>>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null;
		
		while((line = reader.readLine()) != null){
			String[] linePart = line.split("\t");
			String A = linePart[0];
			String C = linePart[1];
			double ruv = Double.parseDouble(linePart[2]);
			
			if (!map.containsKey(A)){
				map.put(A, new HashMap<String,Double>());
			}
			map.get(A).put(C, ruv);
		}
		
		reader.close();
		return map;
	}
	
	public static void evaluateDirection(String testfile) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testfile)));
		String line = null;
		int correct = 0;
		int cnt = 0;
		while((line = reader.readLine()) != null){
			String[] linePart = line.split("\t");
			String cause = linePart[0];
			String effect = linePart[1];
			double s1 = 0;
			double s2 = 0;
			if(ruvmap.containsKey(cause) && ruvmap.get(cause).containsKey(effect)){
				s1 = ruvmap.get(cause).get(effect);
			}
			if(ruvmap.containsKey(effect) && ruvmap.get(effect).containsKey(cause)){
				s2 = ruvmap.get(effect).get(cause);
			}
			
			if(s1>=s2){
				correct += 1;
				logger.write(" true ");
			} else {
				logger.write(" false ");
			}
			
			logger.write(cnt+" ");
			logger.write(line+" ");
			logger.write(s1+","+s2);
			logger.newLine();
			logger.flush();
			
			cnt += 1;
		}
		reader.close();
		double acc = (double)(correct)/cnt;
		System.out.println("Accuracy: "+ acc);
		logger.write("Accuracy: "+String.valueOf(acc));
		logger.flush();
	}
	
	public static void main(String[] args) throws IOException{
		evaluateDirection("causalTermPairs.txt");
		logger.close();
	}
	
}

package edu.sjtu.copa.exe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.sjtu.copa.config.ConfigManager;
import edu.sjtu.copa.ling.Datum;
import edu.sjtu.copa.ling.DepTree;
import edu.sjtu.copa.ling.DepTreeNode;
import edu.sjtu.copa.util.BasicHashSetOperator;
import edu.sjtu.copa.util.DepBranchUtil;

public class ConceptNetRQ1 {

	// here, we tune the alpha, beta as the cikm paper described
		static int Ncause = 0;
		static int Neffect = 0;
		static String cp_path = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "FREQ_CORPUS");
		//static String conceptQ1_pos = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "CONCEPTNET_RQ1_POS");
		//static String conceptQ1_neg = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "CONCEPTNET_RQ1_NEG");
		static String conceptQ1 = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "CONCEPTNET_RQ1");
		
		static String logdir = ConfigManager.getConfig("light-copa-config.ini", "output-dir", "RQ1_LOG_PATH");//"testLog_conceptnetRQ2/";
		
		static double alpha = 0.0;
		static double beta = 0.0;
		
		static String path;
		static Datum nltkdatum ;
		public static BufferedWriter resulter;
		
		static HashMap<String, Integer> causeFreqMap = new HashMap<String,Integer>();
		static HashMap<String, Integer> effectFreqMap = new HashMap<String,Integer>();
		static HashMap<String, HashMap<String,Integer>> cpmap = new HashMap<String,HashMap<String,Integer>>();
		
		//static CondProbGraph cpg;
		
		static {
			System.out.println("static...");
			String resultfile = ConfigManager.getConfig("light-copa-config.ini", "output-dir", "RESULT_SUMMARY");
			
			try {
				resulter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(resultfile)));
				path = ConfigManager.getConfig("light-copa-config.ini", "doc-dir", "CORRECT_COPA_ALL");
				nltkdatum = new Datum(ConfigManager.getConfig("light-copa-config.ini", "doc-dir", "CORRECT_NLTK_COPA_ALL"));
				
				// build causeFreqMap, effectFreqMap
				loadcpmap(cp_path);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		public static void loadcpmap(String filename) throws IOException{
			
			cpmap.clear();causeFreqMap.clear();effectFreqMap.clear();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line = null;
			
			while((line = reader.readLine()) != null){
				String[] linePart = line.split("\t");
				String cw = linePart[0]; String ew = linePart[1]; int freq = Integer.parseInt(linePart[2]);
				if (!cpmap.containsKey(cw)) cpmap.put(cw, new HashMap<String,Integer>());
				if (!cpmap.get(cw).containsKey(ew)) cpmap.get(cw).put(ew, freq);
				
				if (!causeFreqMap.containsKey(cw)) causeFreqMap.put(cw, 0);
				causeFreqMap.put(cw,causeFreqMap.get(cw)+freq);
				if (!effectFreqMap.containsKey(ew)) effectFreqMap.put(ew, 0);
				effectFreqMap.put(ew,effectFreqMap.get(ew)+freq);
				
			}
			reader.close();
			
			// calculate Ncause, Neffect
			for (String word : causeFreqMap.keySet()){
				Ncause += causeFreqMap.get(word);
			}
			
			for (String word : effectFreqMap.keySet()){
				Neffect += effectFreqMap.get(word);
			}
			
			System.out.println("Ncause: " + Ncause);
			System.out.println("Neffect: " + Neffect);
			
		}
		
		public static double calWordCauseProb(String word){
			if (causeFreqMap.containsKey(word)){
				
				return (double)causeFreqMap.get(word)/Ncause;
						
			} else {
				return 0.0;
			}
		}
		
		public static double calWordEffectProb(String word){
			
			if (effectFreqMap.containsKey(word)){
				
				return (double)effectFreqMap.get(word)/Neffect;
						
			} else {
				return 0.0;
			}
		}
		
		
		public static double getScore(String A, String C){
			double score = 0.0;
			if (cpmap.containsKey(A) && cpmap.get(A).containsKey(C)){
				score = cpmap.get(A).get(C) / ( 62675002 * Math.pow(calWordCauseProb(A), alpha) * Math.pow(calWordEffectProb(C), beta));
			}
			return score;
		}
		
		public static void apply(String logdir, String logfilename, String filename) throws IOException{
			ArrayList<Double> scores = new ArrayList<Double>();
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename)));
			
			File logfile = new File(logdir + logfilename);
			BufferedWriter logger = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(logfile)));
			
			String line1 = null;
			String line2 = null;
			
			int cnt = 0;
			String causeline = null;
			String effectline = null;
			
			while((line1 = reader.readLine()) != null &&
					(line2 = reader.readLine()) != null){
				if (cnt < 518){ // 222个binary choice 算444次分 259+153
					causeline = line1;
					effectline = line2;
				}
				else {
					causeline = line2;
					effectline = line1;
				}
				
				DepTree ctree = DepBranchUtil.depParseSentence(causeline);
				DepTree etree = DepBranchUtil.depParseSentence(effectline);
				List<String> cwordlist = Datum.lemmatizeSen(causeline);
				List<String> ewordlist = Datum.lemmatizeSen(effectline);
				HashSet<String> cwordset= new HashSet<String>(cwordlist);
				HashSet<String> ewordset= new HashSet<String>(ewordlist);
				HashSet<String> cset = BasicHashSetOperator.difference(cwordset, ewordset);
				HashSet<String> eset = BasicHashSetOperator.difference(ewordset, cwordset);
				
				DepTree processedctree = DepBranchUtil.
						processedDepTree(ctree, cwordlist, cset); // qwordlist 已经用 stanford-nltk lemmatize过了, 
																// 只要用processedDepTree来setDustbin就行了
				DepTree processedetree = DepBranchUtil.
						processedDepTree(etree, ewordlist, eset); // qwordlist 已经用 stanford-nltk lemmatize过了, 
																// 只要用processedDepTree来setDustbin就行了
				
				
				double s = 0.0;
				s = calScore(processedctree, processedetree);
				
				scores.add(s);
				logger.write(String.valueOf(s));
				logger.newLine();
				logger.flush();
				cnt += 1;
				
			}
			// evaluate
			int correct = 0;
			for ( int i = 0; i < scores.size(); i+=2){
				if (scores.get(i) >= scores.get(i+1)){
					correct += 1;
				}
			}
			double acc = (2.0*correct) / cnt;
			resulter.write("alpha="+String.valueOf(alpha)+", beta="+String.valueOf(beta)+"\t");
			resulter.write(String.valueOf(acc)+"\n");
			resulter.flush();
			reader.close();
			logger.close();
		}
		
		public static double calScore(DepTree causeTree, DepTree effectTree) 
				throws IOException{
			
			double score = 0.0;
			score += calModelWeightSingleDirection(causeTree,effectTree);
			
			return score;
			
		}
		
		public static double calModelWeightSingleDirection(DepTree causeTree, DepTree effectTree) throws IOException{
			
			double mdwSingleScore = 0.0;
			int normalizer = 0;
			System.out.println("normalizer: ");
			for (int i = 1; i < causeTree.getSentence().length; i++){
				DepTreeNode rootnode = causeTree.getNode(i);
				if ((!rootnode.getDustbin()) && causeFreqMap.containsKey(rootnode.getWord())){
					normalizer += 1;
				}
			}
			
			for (int i = 1; i < effectTree.getSentence().length; i++){
				DepTreeNode childnode = effectTree.getNode(i);
				if ((!childnode.getDustbin()) && effectFreqMap.containsKey(childnode.getWord())){
					normalizer += 1;
				}
			}
			
			if (normalizer == 0) normalizer = 1;
			
			// same to single direction now, treat rootTree as singalModel's causeTree, childTree as its effectTree
			for (int i = 1; i < causeTree.getSentence().length; i++){
				DepTreeNode rootnode = causeTree.getNode(i);
				
				if (rootnode.getDustbin()){
					continue;
				}
				String rootword = rootnode.getWord();
				
				for (int j = 1; j < effectTree.getSentence().length; j ++){
					DepTreeNode childnode = effectTree.getNode(j);
					if (childnode.getDustbin()){
						continue;
					}
					
					String childword = childnode.getWord();
					double s = getScore(rootword,childword);
					
					mdwSingleScore += s;
				}
				
			}
			
			return mdwSingleScore/normalizer;
		}
			
		

		public static double f(double para){
			return (1+para) / 2;
		}
		
		public static void main(String[] args) throws IOException{
			
			for (int i=0; i<=10; i++){
				for( int j=0; j<=10; j++){
					alpha = f(i/10.0);
					beta = f(j/10.0);
					String logfilename = "conceptnetRQ1-ruv"+String.valueOf(alpha)+String.valueOf(beta)+"-log";
					apply(logdir, logfilename+".txt", conceptQ1);
				}
			}
			
			alpha = 0.83;
			beta = 0.83;
			String logfilename = "conceptnetRQ1-ruv"+String.valueOf(alpha)+String.valueOf(beta)+"-log";
			apply(logdir, logfilename+".txt", conceptQ1);
			
			alpha = 0.83;
			beta = 0.83;
			logfilename = "conceptnetRQ1-ruv"+String.valueOf(alpha)+String.valueOf(beta)+"-log";
			apply(logdir, logfilename+".txt", conceptQ1);
			
			alpha = 1.0;
			beta = -1.0;
			logfilename = "conceptnetRQ1-ruv"+String.valueOf(alpha)+String.valueOf(beta)+"-log";
			apply(logdir, logfilename+".txt", conceptQ1);
			
			alpha = -1.0;
			beta = 1.0;
			logfilename = "conceptnetRQ1-ruv"+String.valueOf(alpha)+String.valueOf(beta)+"-log";
			apply(logdir, logfilename+".txt", conceptQ1);
			
			/*
			for (int i=0; i<=10;i++){
				for (int j=0; j<=10; j++){
					alpha = i/10.0; beta = j/10.0;
					String logfilename = "conceptnetRQ1-ruv"+String.valueOf(i)+String.valueOf(j)+"-log";
					apply(logdir, logfilename+".txt", conceptQ1);
					
				}
			}
			*/
			
			resulter.close();
			
		}
		
}

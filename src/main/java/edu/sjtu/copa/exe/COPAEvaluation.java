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

import org.jdom2.Element;

import edu.sjtu.copa.config.ConfigManager;
import edu.sjtu.copa.ling.Coreferencer;
import edu.sjtu.copa.ling.Datum;
import edu.sjtu.copa.ling.DepTree;
import edu.sjtu.copa.ling.DepTreeNode;
import edu.sjtu.copa.util.BasicHashSetOperator;
import edu.sjtu.copa.util.DepBranchUtil;

public class COPAEvaluation {

	// here, we tune the alpha, beta as the cikm paper described
	static int Ncause = 0;
	static int Neffect = 0;
	static String cp_path = ConfigManager.getConfig("light-copa-config.ini", "input-dir", "FREQ_CORPUS");
	
	static double alpha = 0.66;
	static double beta = 0.66;
	
	static String path;
	static HashMap<String,HashMap<String,ArrayList<String>>> whoNeedNLTK;
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
			
			LoadWhoNeedNLTK(ConfigManager.getConfig("light-copa-config.ini", "doc-dir", "CORRECT_ERROR_ID"));
			
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
	
	@SuppressWarnings("resource")
	public static void LoadWhoNeedNLTK(String erroridfile) throws IOException{
		whoNeedNLTK = new HashMap<String,HashMap<String,ArrayList<String>>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(erroridfile)));
		
		String line = null;
		while ((line = br.readLine()) != null){
			String[] parts = line.split(" ");
			String id = parts[0];
			String cerr = parts[1];
			String[] cparts = cerr.split(":");
			String eerr = parts[2];
			String[] eparts = eerr.split(":");
			
			if (!whoNeedNLTK.containsKey(id)){
				whoNeedNLTK.put(id, new HashMap<String, ArrayList<String>>());
			}
			if(!whoNeedNLTK.get(id).containsKey(cparts[0])){
				whoNeedNLTK.get(id).put(cparts[0], new ArrayList<String>());
			}
			whoNeedNLTK.get(id).get(cparts[0]).add(cparts[1]);
			
			if(!whoNeedNLTK.get(id).containsKey(eparts[0])){
				whoNeedNLTK.get(id).put(eparts[0], new ArrayList<String>());
			}
			whoNeedNLTK.get(id).get(eparts[0]).add(eparts[1]);
		}
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

	
	public static void run(String ruvfilename, String logdir, String logfilename) throws Exception{
		File logfile = new File(logdir + logfilename);
		BufferedWriter logger = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(logfile)));
		logger.write(ruvfilename);
		logger.newLine();
		Datum document = new Datum(path);
		
		// start
		Element item;
		int correct = 0; int correctDev = 0; int correctTest = 0; int cnt = 0;
		while((item = document.getNextElement()) != null){
			cnt += 1;
			double s1 = 0.0; double s2 = 0.0;
			String direction = Datum.getDirection(item);
			String label = Datum.getLabel(item);
			String text = Datum.getPackSentences(item);
			String id = Datum.getID(item);
			
			ArrayList<String> sentences = Coreferencer.getdCoreferencedText(text);
			
			DepTree qtree = DepBranchUtil.depParseSentence(sentences.get(0));
			DepTree a1tree = DepBranchUtil.depParseSentence(sentences.get(1));
			DepTree a2tree = DepBranchUtil.depParseSentence(sentences.get(2));
			
			List<String> qwordlist = Datum.lemmatizeSen(sentences.get(0));
			List<String> a1wordlist = Datum.lemmatizeSen(sentences.get(1));
			List<String> a2wordlist = Datum.lemmatizeSen(sentences.get(2));
			
			if (whoNeedNLTK.containsKey(id)){
				for (String sentag: whoNeedNLTK.get(id).keySet()){
					
					for (String ind : whoNeedNLTK.get(id).get(sentag)){
						
						int errindx = Integer.valueOf(ind);
						Element elem = nltkdatum.getElement(Integer.valueOf(id)-1);
						
						if (sentag.equals("p")){
							List<String> nltklemSen = Datum.lemmatizeSen(Datum.getQuestion(elem));
							qwordlist.set(errindx, nltklemSen.get(errindx));
							qtree.getNode(errindx+1).setWord(nltklemSen.get(errindx));
							
						} else if (sentag.equals("a1")){
							List<String> nltklemSen = Datum.lemmatizeSen(Datum.getAnswerOne(elem));
							a1wordlist.set(errindx, nltklemSen.get(errindx));
							a1tree.getNode(errindx+1).setWord(nltklemSen.get(errindx));
							
						} else {
							List<String> nltklemSen = Datum.lemmatizeSen(Datum.getAnswerTwo(elem));
							a2wordlist.set(errindx, nltklemSen.get(errindx));
							a2tree.getNode(errindx+1).setWord(nltklemSen.get(errindx));
						}
					}
				}
			}
			
			HashSet<String> qwordset= new HashSet<String>(qwordlist);
			HashSet<String> a1wordset= new HashSet<String>(a1wordlist);
			HashSet<String> a2wordset= new HashSet<String>(a2wordlist);
			
			HashSet<String> a1set = BasicHashSetOperator.difference(
					BasicHashSetOperator.difference(a1wordset, qwordset),
					a2wordset);
			HashSet<String> a2set = BasicHashSetOperator.difference(
					BasicHashSetOperator.difference(a2wordset, qwordset),
					a1wordset);
			HashSet<String> qset = BasicHashSetOperator.difference( 
					BasicHashSetOperator.difference(qwordset,a1wordset),
					a2wordset);
			
			DepTree processedqtree = DepBranchUtil.
					processedDepTree(qtree, qwordlist, qset); // qwordlist 已经用 stanford-nltk lemmatize过了, 
															// 只要用processedDepTree来setDustbin就行了
			DepTree processeda1tree = DepBranchUtil.
					processedDepTree(a1tree, a1wordlist, a1set);
			DepTree processeda2tree = DepBranchUtil.
					processedDepTree(a2tree, a2wordlist, a2set);
			
			if (direction.equals("cause")){ // a -> q
				
				s1 = calScore(processeda1tree, processedqtree, logger, "cause");
				logger.write("--------");
				logger.newLine();
				s2 = calScore(processeda2tree, processedqtree, logger, "cause");
				
			} else {// q -> a
				
				s1 = calScore(processedqtree, processeda1tree, logger, "effect");
				logger.write("--------");
				logger.newLine();
				s2 = calScore(processedqtree, processeda2tree, logger, "effect");
			}

			logger.write("s1: "+s1);
			logger.newLine();
			logger.write("s2: "+s2);
			logger.newLine();

			// evaluate
			if (s1 > s2){
				if (label.equals("1")){
					correct += 1;
					if (cnt<=500){
						correctDev += 1;
					} else {
						correctTest += 1;
					}
					System.out.println(Datum.getID(item)+":"+"true");
					logger.write(Datum.getID(item)+":"+"true");
					logger.newLine();
					
				} else {
					System.out.println(Datum.getID(item)+":"+"false");
					logger.write(Datum.getID(item)+":"+"false");
					logger.newLine();
				}
				
			} else {
				if (label.equals("2")){
					correct += 1;
					if (cnt<=500){
						correctDev += 1;
					} else {
						correctTest += 1;
					}
					System.out.println(Datum.getID(item)+":"+"true");
					logger.write(Datum.getID(item)+":"+"true");
					logger.newLine();
					
				}else {
					System.out.println(Datum.getID(item)+":"+"false");
					logger.write(Datum.getID(item)+":"+"false");
					logger.newLine();
				}
			}
		
		}
		
		double acc = correct / 1000.0;
		System.out.println("Dev Accurate: " + correctDev/500.0);
		System.out.println("Test Accurate: " + correctTest/500.0);
		System.out.println("Accurate: " + acc);
		logger.write("Dev Accurate: " + correctDev/500.0+" ");
		logger.write("Test Accurate: " + correctTest/500.0+" ");
		logger.write("Accurate: " + acc+" ");
		
		resulter.write("Dev Accurate: " + correctDev/500.0 +" ");
		resulter.write("Test Accurate: " + correctTest/500.0+" ");
		resulter.write("Accurate: " + acc+" ");
		resulter.newLine();
		
		// end
		
		resulter.flush();
		logger.close();
	}
	
	public static double calScore(DepTree causeTree, DepTree effectTree, BufferedWriter logger, String type) 
			throws IOException{
		
		double score = 0.0;
		score += calModelWeightSingleDirection(causeTree,effectTree,logger,type);
		
		return score;
		
	}
	
	public static double calModelWeightSingleDirection(DepTree causeTree, DepTree effectTree, BufferedWriter logger, String type) throws IOException{
		
		double mdwSingleScore = 0.0;
		int normalizer = 0;
		System.out.println("normalizer: ");
		for (int i = 1; i < causeTree.getSentence().length; i++){
			DepTreeNode rootnode = causeTree.getNode(i);
			if ((!rootnode.getDustbin()) && causeFreqMap.containsKey(rootnode.getWord())){
				normalizer += 1;
				logger.write(rootnode.getWord()+", ");
			}
		}
		
		for (int i = 1; i < effectTree.getSentence().length; i++){
			DepTreeNode childnode = effectTree.getNode(i);
			if ((!childnode.getDustbin()) && effectFreqMap.containsKey(childnode.getWord())){
				normalizer += 1;
				logger.write(childnode.getWord()+", ");
			}
		}
		
		logger.write(String.valueOf(normalizer)+". \n");
		
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
				logger.write(rootword + " " + childword + " " + String.valueOf(s));
				logger.newLine();
				
				mdwSingleScore += s;
			}
			
		}
		
		return mdwSingleScore/normalizer;
	}
	
	public static double f(double para){
		return (1+para) / 2;
	}
	
	public static double falpha(double lambda){
		return lambda*0.66 + 1 - lambda;
		//return lambda*0.5 + 1 - lambda;
	}
	
	public static double fbeta(double lambda){
		return 0.66 - 0.66*lambda + lambda;
		//return 0.5 - 0.5*lambda + lambda;
	}
	
	public static void main(String[] args) throws Exception{
		
		for (int k = 0; k <= 10; k++){
			
			double lambda = k / 10.0;
			alpha = falpha(lambda);
			beta = fbeta(lambda);
			
			String logdir = ConfigManager.getConfig(
					"light-copa-config.ini", "output-dir", "LOG_PATH"); ////

			String filename = "ruv"+String.valueOf(alpha)+String.valueOf(beta);
			resulter.write(filename+"\t");
			
			String logfilename = filename+"log-weighted-" + "allpairs" + ".txt";
			
			run(filename, logdir, logfilename);
			resulter.flush();
			
		}
		
	}
	
}

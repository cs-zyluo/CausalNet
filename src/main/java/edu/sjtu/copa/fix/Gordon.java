package edu.sjtu.copa.fix;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jdom2.Element;

import edu.sjtu.copa.config.ConfigManager;
import edu.sjtu.copa.ling.Datum;
import edu.sjtu.copa.ling.DepTree;
import edu.sjtu.copa.ling.DepTreeNode;
import edu.sjtu.copa.util.DepBranchUtil;

public class Gordon {
	Datum datum;
	BufferedWriter logger;
	String path;
	
	public Gordon(String section, String key) throws Exception{
		path = ConfigManager.getConfig("light-copa-config.ini", section, key);
		datum = new Datum(path);
		logger = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(ConfigManager.getConfig("light-copa-config.ini", "output-dir", "LOG_PATH"))));//nltk-stanford-idf-log1.txt
		// Result/gordon-bing-log.txt; logGu251w/gordon-Gu1w-log.txt
	}
	
	public List<String> validlist(DepTree processedTree){
		
		List<String> l = new ArrayList<String>();
		
		for (int i = 1; i < processedTree.getSentence().length; i++){
			DepTreeNode node = processedTree.getNode(i);
			if (!node.getDustbin()){
				l.add(node.getWord());
			}
		}

		return l;
	}
	
	public double calCS(List<String> causelist, List<String> effectlist) 
			throws IOException{
		
		double cs = 0.0;
		
		logger.write(causelist.toString());
		logger.newLine();
		logger.write(effectlist.toString());
		logger.newLine();
		
		for (String cause : causelist){
			for (String effect : effectlist){
				double tmp = AsymmetricPMI.calPMI(cause, effect,logger);
				//double tmp = CausalityPMI.calPMI(cause, effect,logger);
				
				cs += tmp;
				logger.write("\t"+cause+"\t"+effect+"\t"+tmp+"\n");
			}
		}
		
		return cs / (causelist.size() * effectlist.size());
	}
	
	public void test1() throws Exception{
		
		Element item;
		int correct = 0;
		int correctDev = 0;
		int correctTest = 0;
		int cnt = 0;
		
		while ((item = datum.getNextElement()) != null){
			cnt += 1;
			
			double s1 = 0.0;
			double s2 = 0.0;
			
			String direction = Datum.getDirection(item);
			String label = Datum.getLabel(item);
			List<String> qwordlist = Datum.lemmatizeSen(Datum.getQuestion(item));
			List<String> a1wordlist = Datum.lemmatizeSen(Datum.getAnswerOne(item));
			List<String> a2wordlist = Datum.lemmatizeSen(Datum.getAnswerTwo(item));
			
			HashSet<String> qset= new HashSet<String>(qwordlist);
			HashSet<String> a1set= new HashSet<String>(a1wordlist);
			HashSet<String> a2set= new HashSet<String>(a2wordlist);
			
			DepTree qtree = DepBranchUtil.depParseSentence(Datum.getQuestion(item));
			DepTree a1tree = DepBranchUtil.depParseSentence(Datum.getAnswerOne(item));
			DepTree a2tree = DepBranchUtil.depParseSentence(Datum.getAnswerTwo(item));
			
			
			DepTree processedqtree = DepBranchUtil.
					processedDepTree(qtree, qwordlist, qset); // qwordlist 已经用 stanford-nltk lemmatize过了, 
															// 只要用processedDepTree来setDustbin就行了
			DepTree processeda1tree = DepBranchUtil.
					processedDepTree(a1tree, a1wordlist, a1set);
			
			DepTree processeda2tree = DepBranchUtil.
					processedDepTree(a2tree, a2wordlist, a2set);
			
			List<String> qlist = validlist(processedqtree);
			List<String> a1list = validlist(processeda1tree);
			List<String> a2list = validlist(processeda2tree);
			
			if (direction.equals("cause")){ // a -> q
				s1 = calCS(a1list, qlist);
				s2 = calCS(a2list, qlist);
				
			} else {// q -> a
				s1 = calCS(qlist, a1list);
				s2 = calCS(qlist, a2list);
				
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
		logger.write("Dev Accurate: " + correctDev/500.0);
		logger.write("Test Accurate: " + correctTest/500.0);
		logger.write("Accurate: " + acc);
		
	}
	
	
	public static void main(String[] args) throws Exception{
		Gordon ins = new Gordon("doc-dir", "COPA_ALL");
		ins.test1();
		ins.logger.close();
	}
	
	
}

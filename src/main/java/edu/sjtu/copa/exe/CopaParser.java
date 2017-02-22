package edu.sjtu.copa.exe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public class CopaParser {
	
	static String path;
	static HashMap<String,HashMap<String,ArrayList<String>>> whoNeedNLTK;
	static Datum nltkdatum ;
	static String section = "doc-dir";
	static String key = "CORRECT_COPA_ALL"; //"CORRECT_COPA_ALL"
	public static BufferedWriter copainswriter;
	
	static {
		String copainsfile = ConfigManager.getConfig("light-copa-config.ini", "output-dir", "COPA_EXAMPLES");
		try {
			copainswriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(copainsfile)));
			path = ConfigManager.getConfig("light-copa-config.ini", section, key);
			nltkdatum = new Datum(ConfigManager.getConfig("light-copa-config.ini", section, "CORRECT_NLTK_COPA_ALL"));
			
			LoadWhoNeedNLTK(ConfigManager.getConfig("light-copa-config.ini", section, "CORRECT_ERROR_ID"));
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	/*
	 * public void generateNewCopaExample(String direction, String label, HashSet<String> qset, HashSet<String> a1set, HashSet<String> a2set) throws IOException{
		copainswriter.write(direction+' '+label);
		copainswriter.newLine();
		for(String qw : qset) copainswriter.write(qw+' ');
		copainswriter.newLine();
		for (String a1w : a1set) copainswriter.write(a1w+' ');
		copainswriter.newLine();
		for (String a2w : a2set) copainswriter.write(a2w+' ');
		copainswriter.newLine();
	}
	 */
	
	public void generateNewCopaExample(String direction, String label, DepTree qtree, DepTree a1tree, DepTree a2tree) throws IOException{
		copainswriter.write(direction+' '+label);
		copainswriter.newLine();
		
		for (int i = 1; i < qtree.getSentence().length; i++){
			DepTreeNode node = qtree.getNode(i);
			if (!node.getDustbin()) copainswriter.write(node.getWord()+' ');
		}
		copainswriter.newLine();
		for (int i = 1; i < a1tree.getSentence().length; i++){
			DepTreeNode node = a1tree.getNode(i);
			if (!node.getDustbin()) copainswriter.write(node.getWord()+' ');
		}
		copainswriter.newLine();
		for (int i = 1; i < a2tree.getSentence().length; i++){
			DepTreeNode node = a2tree.getNode(i);
			if (!node.getDustbin()) copainswriter.write(node.getWord()+' ');
		}
		copainswriter.newLine();
		copainswriter.flush();
	}
	
	public void test1(Datum datum) throws Exception{
		
		Element item;
		int cnt = 0;
		
		while ((item = datum.getNextElement()) != null){
			cnt += 1;
			
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
			
			generateNewCopaExample(direction, label, processedqtree, processeda1tree, processeda2tree);
			
			
			copainswriter.flush();
			
				
		}
		
		System.out.println("cnt: "+cnt);

	}
	
	public static void main(String[] args) throws Exception{
		
		CopaParser copaparser = new CopaParser();
		Datum document = new Datum(path);
		copaparser.test1(document);
		copainswriter.flush();
		copainswriter.close();
		
	}
	

}

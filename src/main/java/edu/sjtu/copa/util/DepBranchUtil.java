package edu.sjtu.copa.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.sjtu.copa.config.ConfigManager;
import edu.sjtu.copa.ling.Datum;
import edu.sjtu.copa.ling.DepBranchTemplate;
import edu.sjtu.copa.ling.DepBranchTemplateNode;
import edu.sjtu.copa.ling.DepSet;
import edu.sjtu.copa.ling.DepTree;
import edu.sjtu.copa.ling.DepTreeNode;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;

public class DepBranchUtil {
	static String path;
	static DependencyParser parser;
	static MaxentTagger tagger;
	
	static{
		path = ConfigManager.getConfig("light-copa-config.ini", "doc-dir", "COPA_ALL");
		tagger = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
		parser = DependencyParser
				.loadFromModelFile(DependencyParser.DEFAULT_MODEL);
	}
	
	public static DepTree depParseSentence(String sentence){
		DepTree tree = null;
		DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(
				sentence));
		List<TaggedWord> tagged;

		for (List<HasWord> sen : dp) { // 只有一句话，只循环一次
			tagged = tagger.tagSentence(sen);
			GrammaticalStructure gs = parser.predict(tagged);
			tree = new DepTree(tagged, gs);
			//tree.printDepBranch(tree.getNode(0), 2);
		}
		
		return tree;
	}
	
	public static DepTree processedDepTree(DepTree tree, List<String> sen, HashSet<String> validset){
		
		for (int i = 1; i < tree.getSentence().length; i++){
			DepTreeNode node = tree.getSentence()[i];
			node.setWord(sen.get(i-1));
			if (!DepSet.real_postag.contains(node.getPostag()) ||
					!validset.contains(node.getWord())){
				node.setDustbin(true);
			}
		}
		
		tree.setSetDustbin(true);
		tree.adjustTreeLevel(); // set dustbin 之后就可以重新调整 tree nodes 的 level了
		
		// set dustbin 与 adjustTreeLevel 应该一起调用
		return tree;
	}
	
	public static DepTree processedDepTree(DepTree tree, String sentence, HashSet<String> validset){
		// lemmatize , set in/or not in dustbin
		List<String> sen = Datum.lemmatizeSen(sentence);
		
		for (int i = 1; i < tree.getSentence().length; i++){
			tree.getSentence()[i].setWord(sen.get(i-1));
			
			if (!validset.contains(tree.getSentence()[i].getWord())){
				tree.getSentence()[i].setDustbin(true);
			}
		}
		
		tree.setSetDustbin(true);
		tree.adjustTreeLevel(); // set dustbin 之后就可以重新调整 tree nodes 的 level了
		
		// set dustbin 与 adjustTreeLevel 应该一起调用
		return tree;
	}
	
	public static DepTree getValidStructureDepTree(DepTree tree, String sentence, HashSet<String> prevalidset){
		// lemmatize , set in/or not in dustbin
		 List<String> sen = Datum.lemmatizeSen(sentence);
		
		for (int i = 1; i < tree.getSentence().length; i++){
			tree.getSentence()[i].setWord(sen.get(i-1));
			
			if (!prevalidset.contains(tree.getSentence()[i].getWord())){
				tree.getSentence()[i].setDustbin(true);
			} else if(!DepSet.valid_rel.contains(tree.getSentence()[i].getReln()) ||
					!DepSet.real_postag.contains(tree.getSentence()[i].getPostag())){ 
				tree.getSentence()[i].setDustbin(true); // dep rel/pos tag 不合格也不行
			}
		}
		
		tree.setSetDustbin(true);
		tree.adjustTreeLevel(); // set dustbin 之后就可以重新调整 tree nodes 的 level了
		
		// set dustbin 与 adjustTreeLevel 应该一起调用
		return tree;
	}
	
	public static HashSet<String> getValidStructureDepTreeWords(DepTree tree){

		HashSet<String> validwordset = new HashSet<String>();
		
		if (tree.getSetDustbin()){
			for (int i = 1; i < tree.getSentence().length; i++){
				if (!tree.getSentence()[i].getDustbin()){
					validwordset.add(tree.getSentence()[i].getWord());
				}
			}
		}
		return validwordset;
	}
	
	public static DepBranchTemplate getDepBranchTemplate(ArrayList<Set<String>> param){
		int size = param.size() / 2;
		
		DepBranchTemplateNode[] nodearr= new DepBranchTemplateNode[size + 1];
		nodearr[0] = null;
		for (int i=0; i<param.size(); i+=2){
			nodearr[i/2 + 1] = new DepBranchTemplateNode(param.get(i), param.get(i+1), nodearr[i/2]);
		}
		DepBranchTemplate template = new DepBranchTemplate(nodearr);
		
		return template;
	}
	
}

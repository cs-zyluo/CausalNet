package edu.sjtu.copa.ling;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.util.IntPair;

public class Coreferencer {
	static StanfordCoreNLP pipeline;

	static{
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
		pipeline = new StanfordCoreNLP(props);
	}
		
	public static void perClusterUpdateSen(ArrayList<List<HasWord>> processedText,
			int common_sentNum, int representative_sentNum,
		int coreStartIndex, int coreEndIndex,
		int commonStartIndex, int commonEndIndex){
		
		List<HasWord> representative_sentence = 
				processedText.get(representative_sentNum-1);
		List<HasWord> common_sentence = 
				processedText.get(common_sentNum-1);
		
		HasWord replace = new Word();
		String replaceStr = "";
		for (int i = coreStartIndex-1; i < coreEndIndex - 1; i++){
			replaceStr += representative_sentence.get(i).toString();
			replaceStr += " ";
		}
		replace.setWord(replaceStr.trim());
		for (int i=commonStartIndex-1; i < commonEndIndex-1; i++){
			common_sentence.set(i,new Word());
			common_sentence.get(i).setWord("");
		}
		common_sentence.set(commonStartIndex-1, replace);
		
	}

	public static ArrayList<String> getdCoreferencedText(String text){
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		ArrayList<String> sentences = new ArrayList<String>();
		DocumentPreprocessor dp = new DocumentPreprocessor(
			new StringReader(text));
		ArrayList<List<HasWord>> processedText = new ArrayList<List<HasWord>>();
		for (List<HasWord> sentence : dp){
			processedText.add(sentence);
		}
		
		//用 representative mention 把 mention替换掉
		Map<Integer, CorefChain> graph = 
		document.get(CorefChainAnnotation.class);
		for (Map.Entry<Integer, CorefChain> entry : graph.entrySet()){
			CorefChain c = entry.getValue();
			
			CorefMention cm = c.getRepresentativeMention();
			for (Entry<IntPair, Set<CorefMention>> e : 
				c.getMentionMap().entrySet()){
				if (cm.endIndex - cm.startIndex >2){
					continue; //如果representative mention 词数大于2 就不换了
				}
				for(CorefMention mention : e.getValue()){
					perClusterUpdateSen(processedText,
							mention.sentNum,cm.sentNum,
						cm.startIndex,cm.endIndex,
						mention.startIndex,mention.endIndex);
				}
			}
		}
		
		for (List<HasWord> senlist : processedText){
			sentences.add("");
			for (HasWord word:senlist){
				if (!word.toString().equals("")){
					//System.out.print(word.toString()+" ");
					String str = sentences.
							get(sentences.size()-1) + word.toString().toLowerCase()+" ";
					sentences.set(sentences.size()-1, str);
				}
			}
			
			//System.out.println();
		}
		for (int i=0; i < sentences.size(); i++){
			String s = sentences.get(i);
			sentences.set(i, (""+s.charAt(0)).toUpperCase() + s.substring(1)) ;
		}
		return sentences;
	}
	
	
	public static void main(String[] args){
//		String text="The women met for coffee ."
//				+ "The cafe reopened in a new location ."
//				+ "They wanted to catch up with each other . ";
		
//		String text = "The animal species became endangered ."
//				+ "Their habitat was destroyed ."
//				+ "Predators went extinct.";
		
		String text = "The man wrote a will. "
					+ "He was dying. "
					+ "He was a widower. ";
		
		ArrayList<String> sentences = Coreferencer.getdCoreferencedText(text);
		
		for (String s:sentences){
			System.out.println(s);
		}
	}

}

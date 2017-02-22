package edu.sjtu.copa.ling;

import java.util.*;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordLemmatizer {
	StanfordCoreNLP pipeline;
	
	public StanfordLemmatizer(){
		Properties props = new Properties();
		props.put("annotators", "tokenize,ssplit,pos,lemma");
		this.pipeline = new StanfordCoreNLP(props);
	}
	
	public List<String> lemmatize(String text){
		List<String> lemmas = new LinkedList<String>();
		Annotation document = new Annotation(text);
		this.pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences){
			for (CoreLabel token : sentence.get(TokensAnnotation.class)){
				lemmas.add(token.get(LemmaAnnotation.class));
			}
		}
		return lemmas;
	}
	
	public ArrayList<String> lemmatize2ArrayList(String text){
		ArrayList<String> lemmas = new ArrayList<String>();
		Annotation document = new Annotation(text);
		this.pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences){
			for (CoreLabel token : sentence.get(TokensAnnotation.class)){
				lemmas.add(token.get(LemmaAnnotation.class));
			}
		}
		return lemmas;
	}
	
	public String lemmatize2text(String text){
		String result = "";
		ArrayList<String> l = lemmatize2ArrayList(text);
		for (String word : l){
			result += word;
			result += " ";
		}
		return result.trim();
	}
}

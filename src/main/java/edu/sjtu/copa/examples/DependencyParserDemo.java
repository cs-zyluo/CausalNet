package edu.sjtu.copa.examples;

import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.ling.CoreLabel;

public class DependencyParserDemo {

	public static void main(String[] args) {
	    String modelPath = DependencyParser.DEFAULT_MODEL;
	    String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

	    for (int argIndex = 0; argIndex < args.length; ) {
	      switch (args[argIndex]) {
	        case "-tagger":
	          taggerPath = args[argIndex + 1];
	          argIndex += 2;
	          break;
	        case "-model":
	          modelPath = args[argIndex + 1];
	          argIndex += 2;
	          break;
	        default:
	          throw new RuntimeException("Unknown argument " + args[argIndex]);
	      }
	    }

	    String text = "I can almost always tell when movies use fake dinosaurs. And you?";

	    MaxentTagger tagger = new MaxentTagger(taggerPath);
	    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

	    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
	    Pattern p = Pattern.compile("(.*\\)\n?)");
	    Pattern nodedepp = Pattern.compile("(.*?)\\(");
		Pattern nodePostagp = Pattern.compile("\\(.*/(.*)\\-.*/(.*)\\-");
		Pattern nodewordp = Pattern.compile("\\((.*)/.* (.*)/");
		Pattern nodeindexp = Pattern.compile("\\-(\\d+).*\\-(\\d+)");
		
		for (List<HasWord> sentence : tokenizer) {
			String parsedSen = "";
			// for each sentence
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			GrammaticalStructure gs = parser.predict(tagged);
//			System.out.println(gs.typedDependencies());
//			System.out.println("======");
	        
			String parsedNodeStr = ""; 
	      
			for (TypedDependency td : gs.allTypedDependencies()) {
				// for each node in one sentence
				String nodestr = td.toString(CoreLabel.OutputFormat.VALUE_TAG_INDEX);
				String deplabel = null;
				String postag = null;
				String headpostag = null;
				String head = null;
				String word = null;
				String headindex = null;
				String index = null;
	    	    
				if(nodestr.contains("ROOT-0")){
					nodestr = nodestr.replace("ROOT-0", "null/null-0");
				}
//				System.out.println(nodestr+"##");
				Matcher nodeDepMatcher = nodedepp.matcher(nodestr);
				Matcher nodePostagMatcher = nodePostagp.matcher(nodestr);
				Matcher nodewordMatcher = nodewordp.matcher(nodestr);
				Matcher nodeindexMatcher = nodeindexp.matcher(nodestr);
				
				if(nodeDepMatcher.find() && nodePostagMatcher.find()
						&& nodewordMatcher.find()
						&& nodeindexMatcher.find()) {
					deplabel = nodeDepMatcher.group(1);
					postag = nodePostagMatcher.group(2);
					headpostag = nodePostagMatcher.group(1);
					head = nodewordMatcher.group(1);
					word = nodewordMatcher.group(2);
					headindex = nodeindexMatcher.group(1);
					index = nodeindexMatcher.group(2);
	    		  
				} else {
					System.out.println(nodestr);
				}
	    	  
				//parsedNodeStr = String.join("/", index, word, postag, deplabel, headindex);
				parsedSen += parsedNodeStr;
				parsedSen += "\t";
			}
	    	  
	      
//	      GrammaticalStructure.printDependencies(gs,
//					gs.allTypedDependencies(), gs.root(), false, false);
//		  System.out.println("===");
			
	      // Print typed dependencies
//	      System.out.println(gs.typedDependencies());
//	      System.out.println(gs.allTypedDependencies());
//			System.out.println("--------");
			System.out.println(parsedSen);
	    }
	  }
	
}

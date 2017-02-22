package edu.sjtu.copa.ling;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.mit.jwi.item.POS;

public class DepSet {
	
	public static Set<String> eventReln = ImmutableSet.of("nsubj","dobj","amod","nn");
	public static Set<String> all_rel = ImmutableSet.of("root",
			"dep", "aux", "auxpass", "cop", "arg",
			"agent", "comp", "acomp", "attr", "ccomp", "xcomp", "compl", 
			"obj", "dobj", "iobj", "pobj", "mark", "rel", 
			"subj", "nsubj", "csubj", "csubj", "csubjpass",
			"cc", "conj", "expl",
			"mod", "abbrev", "amod", "appos", "advcl", "purpcl", "det", "predet", "preconj", 
			"infmod", "partmod", "advmod", "neg", "rcmod", "quantmod", "tmod", "measure", "nn",
			"num", "number", "prep", "poss", "possessive", "prt", 
			"parataxis", "punct", "ref", "sdep", "xsubj");
	
//	public static Set<String> all_postag = ImmutableSet.of(
//			);
	
	public static Set<String> verb_postag = ImmutableSet.of("verb","VB","VBN","VBD","VBG","VBP","VBZ");
	public static Set<String> real_noun_postag = ImmutableSet.of("noun","NN", "NNS", "NNP", "NNPS");
	public static Set<String> adj_postag = ImmutableSet.of("adjective","JJ", "JJS", "JJR");
	public static Set<String> adv_postag = ImmutableSet.of("adverb","RB", "RBS", "RBR");
	
	public static POS getJWIStylePos(String postag){
		
		if (verb_postag.contains(postag)){
			return POS.VERB;
		} 
		else if (real_noun_postag.contains(postag)){
			return POS.NOUN;
		} 
		else if  (adj_postag.contains(postag)){
			return POS.ADJECTIVE;
		} 
		else if (adv_postag.contains(postag)){
			return POS.ADVERB;
		} 
		else {
			return POS.NOUN;
		}
	}
	
	public static String getWNStylePostag(String postag){// postag tree bank pos tag
		
		if (verb_postag.contains(postag)){
			return "V";
		} 
		else if (real_noun_postag.contains(postag)){
			return "N";
		} 
		else if  (adj_postag.contains(postag)){
			return "A";
		} 
		else if (adv_postag.contains(postag)){
			return "R";
		} 
		else {
			return postag;
		}
	}
	
	public static Set<String > real_postag = Sets.union(
			Sets.union(adj_postag, real_noun_postag), Sets.union(verb_postag, adv_postag));
	
	public static Set<String> subject_rel = ImmutableSet.of("nsubj","nsubjpass");
	public static Set<String> verbObject_rel = ImmutableSet.of("dobj", "nsubjpass");
	public static Set<String> prepositionObject_rel = ImmutableSet.of("pobj");
	
	public static Set<String> object_rel = Sets.union(verbObject_rel, prepositionObject_rel);
	
	public static Set<String> verb_mod_rel = ImmutableSet.of("amod", "nn", "abbrev", "appos",
			"advcl", "infmod", "partmod", "advmod", "neg", "rcmod");
	public static Set<String> verb_important_mod_rel = ImmutableSet.of("amod", "nn");
	
	public static Set<String> acompany_rel = ImmutableSet.of("acomp", "ccomp", "xcomp",
			"compl"); //补语
	public static Set<String> acompany_important_rel = ImmutableSet.of("acomp", "ccomp", "xcomp"); //补语
	
	public static Set<String> event_important_rel = Sets.union(acompany_important_rel, 
			Sets.union(verb_important_mod_rel, verbObject_rel));
	
	public static Set<String> extractEventStart_rel = Sets.union(object_rel, 
			Sets.union(verb_mod_rel, acompany_rel));
	
	public static Set<String> valid_rel = Sets.union(verbObject_rel, 
			Sets.union(prepositionObject_rel, Sets.union(verb_mod_rel, 
					Sets.union(ImmutableSet.of("root"), acompany_rel))));
	
	public static void main(String[] args){
		System.out.println(valid_rel.size());
	}
}

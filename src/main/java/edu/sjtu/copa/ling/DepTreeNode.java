package edu.sjtu.copa.ling;

import java.util.ArrayList;
import java.util.List;

public class DepTreeNode {
	int index;
	int level;
	boolean inDustbin;
	boolean isleaf;
	boolean lemmatizeErr;
	String docID; // wordnet sense ID: e.g. "11475067-N"
	DepTreeNode head;
	String reln;

	String postag;
	String word;

	public DepTreeNode(int index, int level, String word, String postag) {
		this.index = index;
		this.level = level;
		this.inDustbin = false;
		this.lemmatizeErr = false;
		this.isleaf = true;
		this.docID = ""; // don't care its sense
		this.word = word;
		this.postag = postag;
	}
	
	public boolean getLemmatizeError(){
		
		return this.lemmatizeErr;
	}
	
	public void setLemmatizeError(boolean iserror){
		this.lemmatizeErr = iserror;
	}
	
	public void setDocID(String docID){
		this.docID = docID;
	}
	
	public String getDocID(){
		return this.docID;
	}
	
	public void setIsleaf(boolean isleaf){
		this.isleaf = isleaf;
	}
	
	public boolean getIsleaf(){
		return this.isleaf;
	}
	
	public void setDustbin(boolean isInDustbin){
		this.inDustbin = isInDustbin;
	}
	
	public boolean getDustbin(){
		return this.inDustbin;
	}
	
	public void setHead(DepTreeNode head, String reln) {
		this.head = head;
		this.reln = reln;
	}

	public int getHeadIndex() {
		return head.index;
	}

	/**
	 * Get index of the path from root to this node
	 * 
	 * @return
	 */
	public List<Integer> getPathFromRoot() {
		List<Integer> res = null;
		if (head == null) {
			res = new ArrayList<Integer>();
		} else {
			res = head.getPathFromRoot();
		}
		
		res.add(index);
		return res;
	}

	public int getIndex() {
		return index;
	}
	
	public int getLevel(){
		return level;
	}
	
	public DepTreeNode getHead() {
		return head;
	}

	public String getReln() {
		return reln;
	}

	public String getPostag() {
		return postag;
	}

	public String getWord() {
		return word;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public void setWord(String word){
		this.word = word;
	}

	public String toString() {
		//return reln + ":" + word + "/" + postag + "-" + index;
		if (this.head == null){ // is "root"
			return "";
		} 
		else if (this.head.index == 0){ // is root word
			return word + "/" + postag + " ";
		}
		return "-" + reln + "- " + word + "/" + postag + " ";
	}
	
	public String toString(boolean isStart){
		if (!isStart){
			return this.toString();
		}
		else {
			return word + "/" + postag + " ";
		}
	}

}

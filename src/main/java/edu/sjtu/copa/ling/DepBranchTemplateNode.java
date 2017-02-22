package edu.sjtu.copa.ling;

import java.util.Set;

public class DepBranchTemplateNode {
	String rel;
	String postag;
	DepBranchTemplateNode head;
	
	Set<String> relSet;
	Set<String> postagSet;
	
	public DepBranchTemplateNode(String rel, String postag, DepBranchTemplateNode head){
		this.rel = rel;
		this.postag = postag;
		this.head = head;
	}
	
	public DepBranchTemplateNode(Set<String> relSet, Set<String> postagSet, DepBranchTemplateNode head){
		this.relSet = relSet;
		this.postagSet = postagSet;
		this.head = head;
	}
	
	public void setHead(DepBranchTemplateNode head){
		this.head = head;
	}
	
	public DepBranchTemplateNode getHead(){
		return this.head;
	}
	
	public void setPostag(String postag){
		this.postag = postag;
	}
	
	public String getPostag(){
		return postag;
	}
	
	public String getRel(){
		return rel;
	}
	
	public void setPostagSet(Set<String> postagSet){
		this.postagSet = postagSet;
	}
	
	public Set<String> getPostagSet(){
		return postagSet;
	}
	
	public Set<String> getRelSet(){
		return relSet;
	}
}

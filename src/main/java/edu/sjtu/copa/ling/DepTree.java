package edu.sjtu.copa.ling;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class DepTree {

	DepTreeNode[] sentence;
	String treestr;
	int treedepth; // deepest level, tree 的实际 depth+1
	int deepestLevel; // deepest valid level
	boolean setDustbin;
	boolean adjustLevel;
	boolean setWSD;
	
	public DepTree(List<TaggedWord> tag, GrammaticalStructure gs) {
		treestr = "";
		treedepth = 0;
		deepestLevel = 0;
		setDustbin = false;
		adjustLevel = false;
		
		sentence = new DepTreeNode[tag.size() + 1];

		sentence[0] = new DepTreeNode(0, 0, "ROOT", "-");
		for (int i = 0; i < tag.size(); ++i) {
			sentence[i + 1] = new DepTreeNode(i + 1, 1, tag.get(i).word(), tag.get(i)
					.tag());
		}

		for (TypedDependency td : gs.allTypedDependencies()) {
			int head = td.gov().index();
			int son = td.dep().index();
			sentence[son].setHead(sentence[head], td.reln().getShortName());
		}
		
		// set tree node's level
		for (int i = 1;i < sentence.length; i++){
			DepTreeNode cur = sentence[i];
			while (cur.head != null){
				sentence[i].level ++ ;
				cur = cur.head;
			}
		}

		for (int i = 1; i < sentence.length; i++){
			if (treedepth < sentence[i].level){
				treedepth = sentence[i].level;
			}
		}
		
	}
	
	public void setSetWSD(boolean setWSD){
		this.setWSD = setWSD;
	}
	
	public boolean getSetWSD(){
		return this.setWSD;
	}
	
	public void setSetDustbin(boolean setDustbin){
		this.setDustbin = setDustbin;
	}
	
	public boolean getSetDustbin(){
		return setDustbin;
	}
	
	public void adjustTreeLevel(){
		// adjustTreeLevel顺便也 set了leaf
		if (setDustbin){
			
			for (int i = 1; i < sentence.length; i++){
				sentence[i].setLevel(0);// root的level 是 0
				
				if (!sentence[i].inDustbin){
					DepTreeNode cur = sentence[i];
					while (cur.head != null){
						if (!cur.inDustbin){
							sentence[i].level ++ ;
						}
						cur.head.setIsleaf(false); // set leaf
						cur = cur.head;
					}
				}
			}
			adjustLevel = true;
		}
	}
	
	public boolean getAdjustLevel(){
		return adjustLevel;
	}
	
	public int getTreeDepth(){
		return treedepth;
	}
	
	public int getDeepestLevel(){
		// adjustTreeLevel()
		if (adjustLevel){
			for (int i = 1; i < sentence.length; i++){
				if (deepestLevel < sentence[i].level){
					deepestLevel = sentence[i].level;
				}
			}
			
			return deepestLevel;
			
		} else {
			return treedepth;
		}
		
	}
	
	public DepTreeNode getNode(int i) {
		return sentence[i];
	}
	
	public DepTreeNode[] getSentence(){
		return this.sentence;
	}
	
	public String getTreeStr(){
		return "["+treestr+"]";
	}
	
	public String toString() {
		return sentence.toString();
	}
	
	public void printSpace(int num){
		System.out.println();
		treestr += "\n";
		for (int i = 0; i < num; i++){
			System.out.print("  ");
			treestr += "  ";
		}
	}
	
	public void printDepTree(DepTreeNode treeroot, int level){
		
		//DepTreeNode treeroot = sentence[0]; // level 0
		System.out.print(treeroot.toString());
		treestr += treeroot.toString();
		
		for (int i = 1;i < sentence.length; i++){
			if (sentence[i].head.index == treeroot.index){
				printSpace(level+1);
				System.out.print("[");
				treestr += "[";
				printDepTree(sentence[i],level+1);
				System.out.print("]");
				treestr += "]";
			}
		}
	}
	
	public void printDepBranch(DepTreeNode start, int length){
		ArrayList<DepTreeNode> endCandidates = new ArrayList<DepTreeNode>();
		
		int endlevel = start.level + length;
//System.out.println("start level: " + start.level + ", endlevel: " + endlevel);
		for (int i = 1;i < sentence.length; i++){
			if (sentence[i].level == endlevel){
				endCandidates.add(sentence[i]); // store end candidate's child
			}
		}
		
		for (DepTreeNode node : endCandidates){
			boolean filter = false;
			ArrayList<DepTreeNode> branch = new ArrayList<DepTreeNode>();
//			if (node == start) {
//				branch.add(node);
//			}
			while (node != start){
				if (node == sentence[0]){// filter out this candidate
					filter = true;
					break;
				}
				branch.add(node);
				node = node.head;
			}
			if (!filter){
				branch.add(start);
				Collections.reverse(branch);
				
				// print this valid branch
				for (DepTreeNode elem : branch){
					System.out.print(elem.toString());
				}
			}
			System.out.println();
		}
	}
	
	public void printDepBranch(DepTreeNode start, int length, BufferedWriter writer)
			throws IOException{
		ArrayList<DepTreeNode> endCandidates = new ArrayList<DepTreeNode>();
		
		int endlevel = start.level + length;
//System.out.println("start level: " + start.level + ", endlevel: " + endlevel);
		for (int i = 1;i < sentence.length; i++){
			if (sentence[i].level == endlevel){
				endCandidates.add(sentence[i]); // store end candidate's child
			}
		}
		
		for (DepTreeNode node : endCandidates){
			boolean filter = false;
			ArrayList<DepTreeNode> branch = new ArrayList<DepTreeNode>();
//			if (node == start) {
//				branch.add(node);
//			}
			while (node != start){
				if (node == sentence[0]){// filter out this candidate
					filter = true;
					break;
				}
				branch.add(node);
				node = node.head;
			}
			if (!filter){
				branch.add(start);
				Collections.reverse(branch);
				
				// print this valid branch
				for (DepTreeNode elem : branch){
					System.out.print(elem.toString());
					writer.write(elem.toString());
				}
				System.out.println();
				writer.newLine();
			}
		}
	}

	public void printDepTemplatedBranch(DepTreeNode start, DepBranchTemplate template,
			BufferedWriter writer) throws IOException {
		int length = template.getLength();
		ArrayList<DepTreeNode> endCandidates = new ArrayList<DepTreeNode>();
		
		int endlevel = start.level + length;
		for (int i = 1;i < sentence.length; i++){
			if (sentence[i].level == endlevel){
				endCandidates.add(sentence[i]); // store end candidate's child
			}
		}
		
		for (DepTreeNode node : endCandidates){
			boolean filter = false;
			ArrayList<DepTreeNode> branch = new ArrayList<DepTreeNode>();

			while (node != start){
				if (node == sentence[0]){// filter out this candidate
					filter = true;
					break;
				}
				branch.add(node);
				node = node.head;
			}
			boolean flag = true;
			
			if (!filter){
				branch.add(start);
				Collections.reverse(branch);
				
				// 判断是否符合 template
				if (branch.size() != length + 1){
					flag = false;
					
				} else {
					for (int index = 0; index < length + 1; index ++){
//						if (!branch.get(index).getReln().equals(template.template[index+1].getRel()) || //因为nodearr[0]是null所以下标要从1开始 故 index+1
//								!branch.get(index).getPostag().equals(template.template[index+1].getPostag())){
						if (!template.template[index+1].getRelSet().contains(branch.get(index).getReln()) || //因为nodearr[0]是null所以下标要从1开始 故 index+1
								!template.template[index+1].getPostagSet().contains(branch.get(index).getPostag())){
							flag = false; // 不满足 template
							break;
						}
					}
				}
				
				if (flag){
					// print this valid branch
					for (DepTreeNode elem : branch){
						System.out.print(elem.toString());
						writer.write(elem.toString());
					}
					System.out.println();
					writer.newLine();
					writer.flush();
				}
			}
		}
	}
	
}

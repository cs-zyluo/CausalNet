package edu.sjtu.copa.util;

import java.util.HashSet;

public class BasicHashSetOperator {
	
	public static HashSet<String> intersection(
			HashSet<String> set1, HashSet<String> set2){ //交集
		HashSet<String> set = new HashSet<String>(set1);
		set.retainAll(set2);
		return set;
	}
	
	public static HashSet<String> union(
			HashSet<String> set1, HashSet<String> set2){ // 并集
		HashSet<String> set = new HashSet<String>(set1);
		set.addAll(set2);
		return set;
	}
	
	public static HashSet<String> difference(
			HashSet<String> set1, HashSet<String> set2){ // 差集: set1 - set2
		HashSet<String> set = new HashSet<String>(set1);
		set.removeAll(set2);
		return set;
	}
	
	public static void printWordSet(HashSet<String> wordset){
		for (String word : wordset){
			System.out.print(word + " ");
		}
		System.out.println();
	}
	
	public static void main(String[] args){
		HashSet<String> set1 = new HashSet<String>();
		HashSet<String> set2 = new HashSet<String>();
		set1.add("set");
		set1.add("setOne");
		set2.add("set");
		set2.add("setTwo");
		HashSet<String> set = difference(set1, set2);
		printWordSet(set);
		printWordSet(set1);
		printWordSet(set2);
		
		
	}
}

package edu.sjtu.copa.examples;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaRegex {
	
	public static void test0(){
		String gloss = "\"hihello\"adsfjal\"fad\"dfad";
		System.out.println(gloss);
		Pattern p = Pattern.compile("\"(.??)\"");
		Matcher matcher = p.matcher(gloss);
		
		while (matcher.find()){
			System.out.println("[[" +matcher.group(1) +"]]");
//			exampleSentences.add(matcher.group(1));
		}
	}
	
	public static void test1(){
		String info = "nsubj(tell/VB-5, I/PRP-1)\naux(tell/VB-5, can/MD-2)\nadvmod(always/RB-4, almost/RB-3)";
		
		Pattern p = Pattern.compile("(.*\\)\n?)");
		Matcher matcher = p.matcher(info);
		while(matcher.find()){
			System.out.println("[["+matcher.group(1).trim()+"]]");
		}
	}
	
	public static void test2(){
		String node1 = "advmod(use/VBP-8, when/WRB-6)";
		String node = "root(ROOT-0, tell/VB-5)";
		Pattern nodep = Pattern.compile("(.*?)\\(");
		Pattern nodePostagp = Pattern.compile("\\(.*/(.*)\\-.*/(.*)\\-");
		Pattern nodewordp = Pattern.compile("\\((.*)/.* (.*)/");
		Pattern nodeindexp = Pattern.compile("\\-(\\d).*\\-(\\d)");
		
		Matcher matcher = //nodeindexp.matcher(node);
				nodewordp.matcher(node);
				//nodePostagp.matcher(node);
				//nodep.matcher(node);
		if(matcher.find()){
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
		}
	}
	public static void test3(){
		String nodestr = "root(ROOT-0, tell/VB-5)";
		System.out.println(nodestr.contains("ROOT-0"));
		nodestr.replace("ROOT", "hello");
		String newnodestr = nodestr.replace("ROOT-0", "null/null-0");
		System.out.println(newnodestr);
	}
	
	//public static void test4(){
	//	System.out.println(String.join("/", "hello","world"));
	//}
	
	public static void main(String[] args){
		test3();
	}
}

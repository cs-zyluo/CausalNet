package edu.sjtu.copa.examples;

public class Test {
	
	public static double falpha(double lambda){
		return lambda*0.66 + 1 - lambda;
	}
	
	public static double fbeta(double lambda){
		return 0.66 - 0.66*lambda + lambda;
	}

	public static void main(String[] args){
		
		System.out.println(falpha(0.5));
		System.out.println(fbeta(0.5));
	}
	
}

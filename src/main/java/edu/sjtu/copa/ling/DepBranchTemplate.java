package edu.sjtu.copa.ling;

public class DepBranchTemplate {
	int length;
	DepBranchTemplateNode[] template;
	
	public DepBranchTemplate(DepBranchTemplateNode[] template){
		this.template = template;
		this.length = template.length - 2; // nodearr[0]是null应减去 并且length应该比 
	}
	
	public int getLength(){
		return this.length;
	}
	
	public String toString(){
		String templateStr = "";
		
		for (DepBranchTemplateNode node : template){
			if (node.head == null){
				templateStr += node.getPostag();
				templateStr += " ";
			} else {
				templateStr += " -";
				templateStr += node.getRel();
				templateStr += "- ";
				templateStr += node.getPostag();
				templateStr += " ";
			}
		}
		
		return templateStr;
	}
}

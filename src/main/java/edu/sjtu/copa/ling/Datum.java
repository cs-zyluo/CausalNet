package edu.sjtu.copa.ling;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class Datum {
	private Document doc;
	private Element root;
	private List<Element> itemList;
	private Iterator<Element> itemIterator;
	
	public static StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
	
	public Datum(String filename) throws Exception{
		File file = new File(filename);
		SAXBuilder sax = new SAXBuilder();
		this.doc = sax.build(file);
		this.root = doc.getRootElement();
		this.itemList = root.getChildren("item"); // for copa Datum
		this.itemIterator = itemList.iterator();
	}
	
	public Element getNextElement(){
		if (itemIterator.hasNext()){
			return itemIterator.next();
		}
		else {
			return null;
		}
	}
	
	public Element getElement(int indx){
		if (indx < itemList.size()){
			return itemList.get(indx);
		} else {
			return null;
		}
	}
	
	public static List<String> lemmatizeSen(String sentence){
		List<String> lemmatizedwordlist = lemmatizer.lemmatize(sentence);
		return lemmatizedwordlist;
	}
	
	public static String lemmatizeSen2Sen(String sentence){
		String lemmaSen = "";
		for (String word : lemmatizeSen(sentence)){
			lemmaSen += word;
			lemmaSen += " ";
		}
		return lemmaSen;
	}
	
	public static String getID(Element item){
		return item.getAttributeValue("id");
	}
	public static String getDirection(Element item){
		return item.getAttributeValue("asks-for");
	}
	public static String getLabel(Element item){
		return item.getAttributeValue("most-plausible-alternative");
	}
	
	public static String getQuestion(Element item){
		return item.getChildText("p");
	}
	public static String getAnswerOne(Element item){
		return item.getChildText("a1");
	}
	public static String getAnswerTwo(Element item){
		return item.getChildText("a2");
	}
	public static String getPackSentences(Element item){
		String text = "";
		text += getQuestion(item);
		text += " ";
		text += getAnswerOne(item);
		text += " ";
		text += getAnswerTwo(item);
		text += " ";
		return text;
	}
	
}

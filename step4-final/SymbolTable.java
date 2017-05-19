/*
 * @author Kincade Pavich, Tiana Smith
 * @version FINAL
 * @date 28 April 2017
 */

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class SymbolTable {

	//INSTANCE VARIABLES
	ArrayList<ArrayList<String>> allData = new ArrayList<ArrayList<String>>(); //List of 3-tuples, which are in their own list
	ArrayList<String> dataEntry; //3-tuples
	String name; 

	//name is the name of the Symbol Table
	public SymbolTable(String inName) {
		name = inName;
	}

	//Constructor for SymbolTable
	public void addEntry(String inName, String inType, String inValue) {
		dataEntry = new ArrayList<String>(); //3-tuple of name, type, value in symbol table
		dataEntry.add(inName);
		dataEntry.add(inType);
		dataEntry.add(inValue);
		allData.add(dataEntry); //adds entry to full list of 3-tuples
	}
	
	//Returns the name
	public String getName() {
		return name;
	}

	//Returns the Data in the Symbol table
	public ArrayList<ArrayList<String>> getData() {
		return allData;
	}

}

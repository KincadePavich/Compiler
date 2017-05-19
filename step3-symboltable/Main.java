/*
 * @author Tiana Smith, Kincade Pavich
 * @version 2.0
 * @date 17 February 2017
 */

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import java.util.*;

public class Main {

	//INSTANCE VARIABLES
	static Stack<SymbolTable> symbolTables; //for printing a valid set of tables
	static Stack<SymbolTable> validTablesTest; //for testing validity

	public static void main(String[] args) {
		try{
			LITTLEParser parser = new LITTLEParser(new CommonTokenStream(
					      new LITTLELexer(new ANTLRFileStream(args[0]))));
			Listener listener = new Listener(); //makes new listener to handle important actions
			new ParseTreeWalker().walk(listener, parser.program()); //starts at root and walks parse tree
			symbolTables = new Stack<SymbolTable>(); //new stack of symbol tables
			validTablesTest = new Stack<SymbolTable>();
			symbolTables = listener.getSymbolTables(); //gets full Stack of tables in order from listener
			validTablesTest = (Stack<SymbolTable>)symbolTables.clone(); //copies to test validity
			if(valid(validTablesTest)) { //if deemed valid
				prettyPrint(symbolTables); //prints symbol tables
			}
					
		}catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}
	public static void prettyPrint(Stack<SymbolTable> inTables) { //prints all symbol tables with vars, blocks, etc.
		while(!inTables.empty()) { //runs while stack is not empty
			SymbolTable current = (SymbolTable)inTables.pop(); //gets current table
			System.out.println("Symbol table " + current.getName()); //prints table name
			ArrayList<ArrayList<String>> tableData = current.getData(); //gets all 3 tuples for current table
			for(int j=0; j<tableData.size(); j++) { //runs through all 3 tuples
				ArrayList<String> variable = tableData.get(j); //gets current 3 tuple
				System.out.print("name " + variable.get(0) + " type " + variable.get(1)); //prints 3 tuple	
				if(variable.get(2) != null) {
					System.out.print(" value " + variable.get(2));	//value only if not null
				}
				System.out.println(); //spacing
			}
			if(!inTables.empty()) { //no newline at EOF
				System.out.println();
			}
		}
	}
	public static boolean valid(Stack<SymbolTable> inTables) { //tests validity of input
		List validNames = new LinkedList<String>(); 
		while(!inTables.empty()) { //runs while stack is not empty
			SymbolTable current = (SymbolTable)inTables.pop(); //gets current table
			ArrayList<ArrayList<String>> tableData = current.getData(); //gets all 3 tuples for current table
			for(int i=0; i<tableData.size(); i++) { //gets current 3 tuple
				ArrayList<String> variable = tableData.get(i); 
				if(!validNames.contains(variable.get(0))) { //if the var name is not already defined in the program, add it to the list
					validNames.add(variable.get(0));
				} else { //the same name is being used twice, invalid, throw error
					System.out.println("DECLARATION ERROR " + variable.get(0));
					return false;
				}
			}
			validNames.clear(); //clears for next scope because it could be valid
		}
		return true;
	}

}


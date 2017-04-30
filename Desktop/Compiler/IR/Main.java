/*
 * @author Kincade Pavich, Tiana Smith
 * @version FINAL
 * @date 28 April 2017
 */

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import java.util.*;

public class Main {

	//INSTANCE VARIABLES
	static Stack<SymbolTable> symbolTables; //for printing a valid set of tables
	static Stack<SymbolTable> validTablesTest; //for testing validity
	static LinkedList<String[]> irCode; //linked list of IR code representation
	static LinkedList<String[]> tinyCode; //linked list of tiny code representation
	static int highReg; //high register available

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
			highReg = 100;
			if(valid(validTablesTest)) { //if deemed valid
				irCode = listener.getIR(); //gets IR code from listener
				tinyCode = new LinkedList<String[]>(); //creates new linked list for tiny code
				System.out.println(";IR code"); //prints all IR code
				for(int i=0; i<irCode.size(); i++) {
					if(i==1) System.out.println(";LINK");
					String[] current = irCode.get(i);
					System.out.print(";");
					for(int j=0; j<=3; j++) {
						if(current[j] != null) {
							System.out.print(current[j] + " ");
						}
					}
					System.out.println();
				}
			}
			System.out.println(";tiny code");
			SymbolTable currentTable = symbolTables.peek(); //adds all global variables to tiny code
			ArrayList<ArrayList<String>> tableData = currentTable.getData(); //gets global symbol table
			for(int i=0; i<tableData.size(); i++) { //runs through each var in table
				ArrayList<String> variable = tableData.get(i);
				String[] entry = new String[3];
				if(variable.get(1).equals("STRING")) {
					entry[0] = "str";
				} else {
					entry[0] = "var";
				}
				entry[1] = variable.get(0);
				if(variable.get(2) != null) {
					entry[2] = variable.get(2);
				}
				tinyCode.add(entry); //adds var to tiny code
			}

			for(int i=0; i<irCode.size(); i++) { //goes through every IR entry, converts to tiny code
				String[] currentIR = irCode.get(i); //gets the current IR entry
				String[] entry; //defines tiny entry
				if(currentIR[0].equals("LABEL")) { //if LABEL
					entry = new String[3];
					entry[0] = "label";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				} else if(currentIR[0].contains("STORE")) { //if STOREI, STOREF, STORES
					entry = new String[3];
					entry[0] = "move";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("JUMP")) { //if JUMP
					entry = new String[3];
					entry[0] = "jmp";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("WRITE")) { //if WRITEI, WRITEF, WRITES
					entry = new String[3];
					if(currentIR[0].contains("WRITES")) entry[0] = "sys writes";
					else if(currentIR[0].contains("WRITEF")) entry[0] = "sys writer";
					else if(currentIR[0].contains("WRITEI")) entry[0] = "sys writei";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("READ")) { //if READI, READF, READS
					entry = new String[3];
					if(currentIR[0].contains("READS")) entry[0] = "sys reads";
					else if(currentIR[0].contains("READF")) entry[0] = "sys readr";
					else if(currentIR[0].contains("READI")) entry[0] = "sys readi";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("MULT")) { //if MULTI, MULTF
					entry = new String[3];
					entry[0] = "move";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
					entry = new String[3];
					if(currentIR[0].equals("MULTI")) entry[0] = "muli";
					else entry[0] = "mulr";
					entry[1] = cleanRegister(currentIR[2]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
		 		} else if(currentIR[0].contains("ADD")) { //if ADDI, ADDF
					entry = new String[3];
					entry[0] = "move";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
					entry = new String[3];
					if(currentIR[0].equals("ADDI")) entry[0] = "addi";
					else entry[0] = "addr";
					entry[1] = cleanRegister(currentIR[2]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
				} else if(currentIR[0].contains("DIV")) { //if DIVI, DIVF
					entry = new String[3];
					entry[0] = "move";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
					entry = new String[3];
					if(currentIR[0].equals("DIVI")) entry[0] = "divi";
					else entry[0] = "divr";
					entry[1] = cleanRegister(currentIR[2]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
				} else if(currentIR[0].contains("SUB")) { //if SUBI, SUBF
					entry = new String[3];
					entry[0] = "move";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
					entry = new String[3];
					if(currentIR[0].equals("SUBI")) entry[0] = "subi";
					else entry[0] = "subr";
					entry[1] = cleanRegister(currentIR[2]);
					entry[2] = cleanRegister(currentIR[3]);
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("EQ")) { //if EQI, EQF
					if(!currentIR[2].contains("$T")) {
						entry = new String[3];
						entry[0] = "move";
						entry[1] = currentIR[2];
						entry[2] = "r" + highReg;
						currentIR[2] = entry[2];
						tinyCode.add(entry);
						highReg--;
					}
					entry = new String[3];
					if(currentIR[0].contains("EQI")) entry[0] = "cmpi";
					else entry[0] = "cmpr";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[2]);
					tinyCode.add(entry);
					entry = new String[3];
					entry[0] = "jeq";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				 }  else if(currentIR[0].contains("NE")) { //if NEI, NEF
					if(!currentIR[2].contains("$T")) {
						entry = new String[3];
						entry[0] = "move";
						entry[1] = currentIR[2];
						entry[2] = "r" + highReg;
						currentIR[2] = entry[2];
						tinyCode.add(entry);
						highReg--;
					}
					entry = new String[3];
					if(currentIR[0].contains("NEI")) entry[0] = "cmpi";
					else entry[0] = "cmpr";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[2]);
					tinyCode.add(entry);
					entry = new String[3];
					entry[0] = "jne";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("GT")) { //if GTI, GTF
					if(!currentIR[2].contains("$T")) {
						entry = new String[3];
						entry[0] = "move";
						entry[1] = currentIR[2];
						entry[2] = "r" + highReg;
						currentIR[2] = entry[2];
						tinyCode.add(entry);
						highReg--;
					}
					entry = new String[3];
					if(currentIR[0].contains("GTI")) entry[0] = "cmpi";
					else entry[0] = "cmpr";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[2]);
					tinyCode.add(entry);
					entry = new String[3];
					entry[0] = "jgt";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				}  else if(currentIR[0].contains("GE")) { //if GEI, GEF
					if(!currentIR[2].contains("$T")) {
						entry = new String[3];
						entry[0] = "move";
						entry[1] = currentIR[2];
						entry[2] = "r" + highReg;
						currentIR[2] = entry[2];
						tinyCode.add(entry);
						highReg--;
					}
					entry = new String[3];
					if(currentIR[0].contains("GEI")) entry[0] = "cmpi";
					else entry[0] = "cmpr";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[2]);
					tinyCode.add(entry);
					entry = new String[3];
					entry[0] = "jge";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				}  else if(currentIR[0].contains("LT")) { //if LTI, LTF
					if(!currentIR[2].contains("$T")) {
						entry = new String[3];
						entry[0] = "move";
						entry[1] = currentIR[2];
						entry[2] = "r" + highReg;
						currentIR[2] = entry[2];
						tinyCode.add(entry);
						highReg--;
					}
					entry = new String[3];
					if(currentIR[0].contains("LTI")) entry[0] = "cmpi";
					else entry[0] = "cmpr";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[2]);
					tinyCode.add(entry);
					entry = new String[3];
					entry[0] = "jlt";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("LE")) { //if LEI, LEF
					if(!currentIR[2].contains("$T")) {
						entry = new String[3];
						entry[0] = "move";
						entry[1] = currentIR[2];
						entry[2] = "r" + highReg;
						currentIR[2] = entry[2];
						tinyCode.add(entry);
						highReg--;
					}
					entry = new String[3];
					if(currentIR[0].contains("LEI")) entry[0] = "cmpi";
					else entry[0] = "cmpr";
					entry[1] = cleanRegister(currentIR[1]);
					entry[2] = cleanRegister(currentIR[2]);
					tinyCode.add(entry);
					entry = new String[3];
					entry[0] = "jle";
					entry[2] = currentIR[3];
					tinyCode.add(entry);
				 } else if(currentIR[0].contains("RET")) { //if RET
					entry = new String[3];
					entry[0] = "sys halt";
					tinyCode.add(entry);
				 }

			}
			for(int i=0; i<tinyCode.size(); i++) { //goes through list of tiny code entries
				String[] current = tinyCode.get(i); //gets current entry
				if(current[0] != null) {	//if not null
					System.out.print(current[0] + " "); //print
				}
				if(current[1] != null) { //if not null
					System.out.print(current[1] + " "); //print
				}
				if(current[2] != null) { //if not null
					System.out.print(current[2]); //print
				}
				System.out.println(); //print line after every entry
			}
					
		}catch (IOException e) {
			System.out.println("ERROR: " + e); //print if there are any errors in trying to run
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

	public static String cleanRegister(String inVal) { //takes in IR register in format "%TX"
		String val = inVal;
		if(val.contains("$T")) {
			val = val.replaceAll("\\$T", ""); //replaces "$T" with nothing
			int reg = Integer.parseInt(val) - 1; //decrements register number since IR starts at 1 and tiny starts at 0
			val = "r" + reg; //appends r befor register number
		} 
		return val; //returns tiny register
	}

}


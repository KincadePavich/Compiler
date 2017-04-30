/*
 * @authors Kincade Pavich, Tiana Smith
 * @version FINAL
 * @project Tiny Compiler
*/

import java.util.*;

public class Listener extends LITTLEBaseListener {

	//INSTANCE VARIABLES
	Stack order; //keeps track of correct order of symbol tables and nesting
	Stack scope; //stack to keep track of current scope for var order etc.
	int block; //keeps track of BLOCK for IF, ELSE, WHILE
	LinkedList ir;// = new LinkedList<Str ing>(); //linked list of intermediate representations
	String[] entry; //instance variable entry to store 4-tuple
    int register; //keeps track of register number for IR Code	
	int level; //keeps track of label number
	int exitLevel; //keeps track of level to exit a while loop
	int exitIfLevel; //keeps track of level to exit an if statement
	boolean inAssign; //true if in assign statement, false if not
	ArrayList<String> calculation; //list of numbers and operations for any calculation
	String priority; //priority of calculation: parentheses sections
	int numVars; //keeps track of number of vars to be added to calculation based on length
	int numAdded; //keeps track of how many vars have actually been added to calculation
	int nesting; //keeps track of level of nesting for nested "IF" label numbers
	int traversal; //keeps track of current label based on entry of conditional statement
	boolean needsClose; //handles case where close parentheses is last character in calculation
	boolean hasClose; //lets us know if the calculation has been closed
	boolean lastChar; //used to reduce register by 1 for the case of needsClose && !hasClose

	public Listener() {
		order = new Stack<SymbolTable>(); //initialize all instance variables
		scope = new Stack<SymbolTable>();
		block = 0 ;
		register = 1;
		level = 0;
		needsClose = false;
		hasClose = false;
		lastChar = false;
		priority = "";
		inAssign = false;
		exitLevel = 0;
		exitIfLevel = 0;
		numVars = 0;
		numAdded = 0;
		nesting = 0;
		traversal = 0;
		SymbolTable firstTable = new SymbolTable("GLOBAL"); //1st table is always GLOBAL
		order.push(firstTable); //GLOBAL is pushed onto both stacks
		scope.push(firstTable);
		ir = new LinkedList<String[]>(); //initialize linked list of intermediate representations
	}

	@Override
	public void enterRead_stmt(LITTLEParser.Read_stmtContext ctx) {
		String readStmt = ctx.getText(); //gets entire write statement text
		readStmt = readStmt.replaceAll("READ", "");
		readStmt = readStmt.replaceAll("\\s", "");
		readStmt = readStmt.replaceAll("\\(", "");
		readStmt = readStmt.replaceAll("\\);", "");
		String[] vars = readStmt.split("\\,"); //gets all vars in read statement
		String type = "NONE"; //initializes type as none as an error checker
		for(int j=0; j<vars.length; j++) { //runs through each variable
			entry = new String[4]; //creates a new IR entry
			type = getVarType(vars[j]); //gets type of current variable
			if(type.equals("STRING")) {
				entry[0] = "READS";
			} else if(type.equals("INT")) {
				entry[0] = "READI";
			} else if(type.equals("FLOAT")) {
				entry[0] = "READF";
			}	
			entry[3] = vars[j];
			ir.add(entry); //adds entry to IR list
			type = "NONE";
		}
	}

	@Override
	public void enterWrite_stmt(LITTLEParser.Write_stmtContext ctx) {
		String writeStmt = ctx.getText(); //gets entire write statement text
		writeStmt = writeStmt.replaceAll("WRITE", "");
		writeStmt = writeStmt.replaceAll("\\s", "");
		writeStmt = writeStmt.replaceAll("\\(", "");
		writeStmt = writeStmt.replaceAll("\\);", "");
		String[] vars = writeStmt.split("\\,"); //gets all vars in write statement
		String type = "NONE";
		for(int j=0; j<vars.length; j++) { //runs through each variable
			entry = new String[4]; //creates a new IR entry
			type = getVarType(vars[j]); //gets type of current variable
			if(type.equals("STRING")) {
				entry[0] = "WRITES";
			} else if(type.equals("INT")) {
				entry[0] = "WRITEI";
			} else if(type.equals("FLOAT")) {
				entry[0] = "WRITEF";
			}	
			entry[3] = vars[j];
			ir.add(entry); //adds entry to IR list
			type = "NONE";
		}
	}

	@Override
	public void enterAssign_stmt(LITTLEParser.Assign_stmtContext ctx) {
		inAssign = true;
		needsClose = false; //initializes to false every time we enter an assign statement
		hasClose = false;
		lastChar = false;
		calculation = new ArrayList<String>(); //creates a new calculation arraylist
	}

	public String getVarType(String name) { //method gets type of a variable from symbol tables
		boolean found = false; //lets us know if var has been found
		Stack<SymbolTable> tempStack = (Stack<SymbolTable>)scope.clone(); //creates clone of symbol table stack
		String type = "NULL"; //initializes type to NULL
		while(!tempStack.empty() && found == false) { //runs until stack is empty or variable is found
			SymbolTable current = (SymbolTable)tempStack.pop(); //gets current symbol table
			ArrayList<ArrayList<String>> allData = current.getData(); //gets data from symbol table
			for(int i=0; i<allData.size(); i++) { //runs through each entry in table
				ArrayList<String> variable = allData.get(i); //gets current table entry
				if(variable.get(0).equals(name)) { //if variable is found
					type = variable.get(1); //gets its type
					found = true; //mark variable as found
				}
			}
		}
		return type; //return the type of the given variable
	}

	@Override 
	public void exitAssign_stmt(LITTLEParser.Assign_stmtContext ctx) {
		inAssign = false; //no longer in an assign statement
		if(hasClose==false && needsClose==true) { //handles case where calc ends in paren.
			calculation.add(")"); //if it doesn't have a ")" but needs one, add it to end of list
			lastChar = true; //flag to show ")" is the last char in calculation
		}
		String assignment = ctx.getText();
		String[] nameSplit = assignment.split(":=");
		String name = nameSplit[0].replaceAll("\\s", ""); //gets variable name being assigned to
		String finalType = getVarType(name); //gets type of variable being assigned to
		String type = "NULL";
		if(finalType.equals("INT")) {
			type = "STOREI";
		} else if(finalType.equals("FLOAT")) {
			type = "STOREF";
		} else {
			System.out.println("VALUE IS NULL - CHECK METHODS");
		}
		String calcType = "NULL";
		while(calculation.contains("(")) { //processes all parentheses first
			int openParen = calculation.indexOf("("); //gets index of 1st open paren
			int closeParen = calculation.indexOf(")"); //gets index of 1st close paren
			List<String> priority = calculation.subList(openParen+1, closeParen); //gets sublist inside parens
			while(priority.contains("*")) { //process multiplication
				if(finalType.equals("INT")) {
					calcType = "MULTI";
				} else if(finalType.equals("FLOAT")) {
					calcType = "MULTF";
				}
				int op = priority.indexOf("*"); //gets index of operator
				String var1 = priority.get(op-1); //gets 1st variable (before operator)
				String var2 = priority.get(op+1); //gets 2nd variable (after operator)
				if(Character.isDigit(var1.charAt(0))) { //if variable is a number rather than a variable or register
					entry = new String[4]; //add number to a register
					entry[0] = type;
					entry[1] = var1;
					entry[3] = "$T" + register;
					var1 = entry[3];
					register++;
					ir.add(entry);
				}
				if(Character.isDigit(var2.charAt(0))) { //if variable is a number rather than a variable or register
					entry = new String[4]; //add number to register
					entry[0] = type;
					entry[1] = var2;
					entry[3] = "$T" + register;
					var2 = entry[3];
					register++;
					ir.add(entry);
				}
				entry = new String[4]; //create a new entry
				entry[0] = calcType;
				entry[1] = var1;
				entry[2] = var2;
				entry[3] = "$T" + register;
				ir.add(entry); //add operation performed to IR list
				priority.set(op, "$T" + register); //set register to location operator was
				priority.remove(op+1); //remove variables. Thus "A op B" has been reduced to "$TX"
				priority.remove(op-1);
				calcType = "NULL";
				if(priority.size() > 1) { //as long as we are not down to the last register within parens
					register++; //increment register for next iteration
				}
			}
			while(priority.contains("/")) { //process division - see comments from lines 164-204 for code explanation
				if(finalType.equals("INT")) {
					calcType = "DIVI";
				} else if(finalType.equals("FLOAT")) {
					calcType = "DIVF";
				}
				int op = priority.indexOf("/");
				String var1 = priority.get(op-1);
				String var2 = priority.get(op+1);
				if(Character.isDigit(var1.charAt(0))) {
					entry = new String[4];
					entry[0] = type;
					entry[1] = var1;
					entry[3] = "$T" + register;
					var1 = entry[3];
					register++;
					ir.add(entry);
				}
				if(Character.isDigit(var2.charAt(0))) {
					entry = new String[4];
					entry[0] = type;
					entry[1] = var2;
					entry[3] = "$T" + register;
					var2 = entry[3];
					register++;
					ir.add(entry);
				}
				entry = new String[4];
				entry[0] = calcType;
				entry[1] = var1;
				entry[2] = var2;
				entry[3] = "$T" + register;
				ir.add(entry);
				priority.set(op, "$T" + register);
				priority.remove(op+1);
				priority.remove(op-1);
				calcType = "NULL";
				if(priority.size() > 1) {
					register++;
				}
			}	
			while(priority.contains("-")) { //process subtraction - see comments from lines 164-204 for code explanation
				if(finalType.equals("INT")) {
					calcType = "SUBI";
				} else if(finalType.equals("FLOAT")) {
					calcType = "SUBF";
				}
				int op = priority.indexOf("-");
				String var1 = priority.get(op-1);
				String var2 = priority.get(op+1);
				if(Character.isDigit(var1.charAt(0))) {
					entry = new String[4];
					entry[0] = type;
					entry[1] = var1;
					entry[3] = "$T" + register;
					var1 = entry[3];
					register++;
					ir.add(entry);
				}
				if(Character.isDigit(var2.charAt(0))) {
					entry = new String[4];
					entry[0] = type;
					entry[1] = var2;
					entry[3] = "$T" + register;
					var2 = entry[3];
					register++;
					ir.add(entry);
				}
				entry = new String[4];
				entry[0] = calcType;
				entry[1] = var1;
				entry[2] = var2;
				entry[3] = "$T" + register;
				ir.add(entry);
				priority.set(op, "$T" + register);
				priority.remove(op+1);
				priority.remove(op-1);
				calcType = "NULL";
				if(priority.size() > 1) {
					register++;
				}
			}		
			while(priority.contains("+")) { //process addition - see comments from lines 164-204 for code explanation
				if(finalType.equals("INT")) {
					calcType = "ADDI";
				} else if(finalType.equals("FLOAT")) {
					calcType = "ADDF";
				}
				int op = priority.indexOf("+");
				String var1 = priority.get(op-1);
				String var2 = priority.get(op+1);
				if(Character.isDigit(var1.charAt(0))) {
					entry = new String[4];
					entry[0] = type;
					entry[1] = var1;
					entry[3] = "$T" + register;
					var1 = entry[3];
					register++;
					ir.add(entry);
				}
				if(Character.isDigit(var2.charAt(0))) {
					entry = new String[4];
					entry[0] = type;
					entry[1] = var2;
					entry[3] = "$T" + register;
					var2 = entry[3];
					register++;
					ir.add(entry);
				}
				entry = new String[4];
				entry[0] = calcType;
				entry[1] = var1;
				entry[2] = var2;
				entry[3] = "$T" + register;
				ir.add(entry);
				priority.set(op, "$T" + register);
				priority.remove(op+1);
				priority.remove(op-1);
				calcType = "NULL";
				if(priority.size() > 1) {
					register++;
				}
			}		
			calculation.set(openParen, "$T" + register); //sets final result of priority set to current register
			register++; //increments register for next used
			calculation.remove(openParen+1); //priority has been narrowed down to 3 values, thus we remove 2 values
			calculation.remove(openParen+1); //from calculation. Priority processing results in 1 register in calculation
		}
		while(calculation.contains("*")) { //process multiplication - see comments from lines 164-204 for code explanation
			if(finalType.equals("INT")) {
				calcType = "MULTI";
			} else if(finalType.equals("FLOAT")) {
				calcType = "MULTF";
			}
			int op = calculation.indexOf("*");
			String var1 = calculation.get(op-1);
			String var2 = calculation.get(op+1);
			if(Character.isDigit(var1.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var1;
				entry[3] = "$T" + register;
				var1 = entry[3];
				register++;
				ir.add(entry);
			}
			if(Character.isDigit(var2.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var2;
				entry[3] = "$T" + register;
				var2 = entry[3];
				register++;
				ir.add(entry);
			}
			entry = new String[4];
			entry[0] = calcType;
			entry[1] = var1;
			entry[2] = var2;
			entry[3] = "$T" + register;
			ir.add(entry);
			calculation.set(op, "$T" + register);
			calculation.remove(op+1);
			calculation.remove(op-1);
			calcType = "NULL";
			if(calculation.size() > 1) {
				register++;
			}
		}
		while(calculation.contains("/")) { //process division - see comments from lines 164-204 for code explanation
			if(finalType.equals("INT")) {
				calcType = "DIVI";
			} else if(finalType.equals("FLOAT")) {
				calcType = "DIVF";
			}
			int op = calculation.indexOf("/");
			String var1 = calculation.get(op-1);
			String var2 = calculation.get(op+1);
			if(Character.isDigit(var1.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var1;
				entry[3] = "$T" + register;
				var1 = entry[3];
				register++;
				ir.add(entry);
			}
			if(Character.isDigit(var2.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var2;
				entry[3] = "$T" + register;
				var2 = entry[3];
				register++;
				ir.add(entry);
			}
			entry = new String[4];
			entry[0] = calcType;
			entry[1] = var1;
			entry[2] = var2;
			entry[3] = "$T" + register;
			ir.add(entry);
			calculation.set(op, "$T" + register);
			calculation.remove(op+1);
			calculation.remove(op-1);
			calcType = "NULL";
			if(calculation.size() > 1) {
				register++;
			}
		}	
		while(calculation.contains("-")) { //process subtraction - see comments from lines 164-204 for code explanation
			if(finalType.equals("INT")) {
				calcType = "SUBI";
			} else if(finalType.equals("FLOAT")) {
				calcType = "SUBF";
			}
			int op = calculation.indexOf("-");
			String var1 = calculation.get(op-1);
			String var2 = calculation.get(op+1);
			if(Character.isDigit(var1.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var1;
				entry[3] = "$T" + register;
				var1 = entry[3];
				register++;
				ir.add(entry);
			}
			if(Character.isDigit(var2.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var2;
				entry[3] = "$T" + register;
				var2 = entry[3];
				register++;
				ir.add(entry);
			}
			entry = new String[4];
			entry[0] = calcType;
			entry[1] = var1;
			entry[2] = var2;
			entry[3] = "$T" + register;
			ir.add(entry);
			calculation.set(op, "$T" + register);
			calculation.remove(op+1);
			calculation.remove(op-1);
			calcType = "NULL";
			if(calculation.size() > 1) {
				register++;
			}
		}		
		while(calculation.contains("+")) { //process addition - see comments from lines 164-204 for code explanation
			if(finalType.equals("INT")) {
				calcType = "ADDI";
			} else if(finalType.equals("FLOAT")) {
				calcType = "ADDF";
			}
			int op = calculation.indexOf("+");
			String var1 = calculation.get(op-1);
			String var2 = calculation.get(op+1);
			if(Character.isDigit(var1.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var1;
				entry[3] = "$T" + register;
				var1 = entry[3];
				register++;
				ir.add(entry);
			}
			if(Character.isDigit(var2.charAt(0))) {
				entry = new String[4];
				entry[0] = type;
				entry[1] = var2;
				entry[3] = "$T" + register;
				var2 = entry[3];
				register++;
				ir.add(entry);
			}
			entry = new String[4];
			entry[0] = calcType;
			entry[1] = var1;
			entry[2] = var2;
			entry[3] = "$T" + register;
			ir.add(entry);
			calculation.set(op, "$T" + register);
			calculation.remove(op+1);
			calculation.remove(op-1);
			calcType = "NULL";
			if(calculation.size() > 1) {
				register++;
			}
		}	
		if(calculation.size() == 1) { //base case: calculation is down to 1 register or there was 1 value to begin with
			if(!calculation.get(0).contains("$T")) { //if value is not a register
				entry = new String[4]; //store the value in a register
				entry[0] = type;
				entry[1] = calculation.get(0);
				entry[3] = "$T" + register;
				ir.add(entry);
			}
			entry = new String[4]; //add register to variable being assigned
			entry[0] = type;
			if(lastChar) register--; //in case where ")" was last char, decrement register
			entry[1] = "$T" + register;
			entry[3] = name;
			ir.add(entry);
			register++;
		}	
	}

	@Override
	public void enterPrimary(LITTLEParser.PrimaryContext ctx) {
		if(inAssign) { //run only if within an assign statement to avoid comparisons etc.
			String primary = ctx.getText();
			if(!primary.contains("(")) { //if no parens
				calculation.add(primary); //primary is just a single var/number. Add it to calculation
				if(numVars > 0) { //if there are parens in the calcuation
					needsClose = true; //a close paren is needed
					numAdded++; //increment number of variables added
					if(numAdded == numVars) { //if we have added all variables
						calculation.add(")"); //add our close paren
						hasClose = true; //mark that it has a close
						numVars = 0; //reset values
						numAdded = 0;
					}
				}
			
			} else { //if primary does have parentheses
				priority = primary; //set priority to primary
				calculation.add("("); //add an open paren
				String[] varSplit = priority.split("[+-/*]"); //split variables on all operators
				numVars = varSplit.length; //gets the number of variables
				numAdded = 0; //0 have been added so far
			}
		}
	}

	@Override
	public void enterAddop(LITTLEParser.AddopContext ctx) {
		if(inAssign) { //if we're in an assign statement
			String addop = ctx.getText();
			calculation.add(addop); //add the operation to calculation
		}
	}
	
	@Override
	public void enterMulop(LITTLEParser.MulopContext ctx) {
		if(inAssign) { //if we're in an assign statement
			String mulop = ctx.getText();
			calculation.add(mulop); //add the operation to calculation
		}
	}
		
	@Override
	public void enterFunc_decl(LITTLEParser.Func_declContext ctx) {
		String funcDecl = ctx.getText(); //Gets entire function declaration text
		funcDecl = funcDecl.replaceAll("FLOAT", ""); //cleans to only function name
		funcDecl = funcDecl.replaceAll("INT", "");
		funcDecl = funcDecl.replaceAll("VOID", "");
		funcDecl = funcDecl.replaceAll("FUNCTION", "");
		String[] name = funcDecl.split("\\(");
		name[0] = name[0].replaceAll("\\s", "");
		SymbolTable newTable = new SymbolTable(name[0]); //creates new table with function name
		order.push(newTable); //new table pushed onto stacks
		scope.push(newTable);
		entry = new String[4];
		entry[0] = "LABEL";
		entry[3] = name[0];
		ir.add(entry); //add function name as label to IR entry
	}

	@Override
	public void exitFunc_decl(LITTLEParser.Func_declContext ctx) {
		scope.pop(); //pop from current scope as we exit a function declaration
	}

	@Override
	public void enterWhile_stmt(LITTLEParser.While_stmtContext ctx) {
		String whileStmt = ctx.getText(); //gets entire while statement text
		String tableName = "BLOCK " + block;
		SymbolTable newTable = new SymbolTable(tableName); //new table created
		order.push(newTable); //new table pushed onto stacks
		scope.push(newTable);
		entry = new String[4];
		level++; //sets label number
		exitLevel = level + 1; //sets while exit label number
		entry[0] = "LABEL";
		entry[3] = "label" + level;
		ir.add(entry); //adds entry to IR list
		level++; //increments label number
		traversal++; //increments where our current traversal of labels is for conditionals
	}

	@Override
	public void exitWhile_stmt(LITTLEParser.While_stmtContext ctx) {
		entry = new String[4]; //adds label entry for exit while
		entry[0] = "LABEL";
		entry[3] = "label" + exitLevel;
		String[] entry2 = new String[4]; //adds jump label for top of while statement
		entry2[0] = "JUMP";
		entry2[3] = "label" + (exitLevel-1);
		ir.add(entry2);
		ir.add(entry);
		scope.pop(); //pops current symbol table off stack
	}

	@Override
	public void enterIf_stmt(LITTLEParser.If_stmtContext ctx) {
		String ifStmt = ctx.getText(); //get entire if stmt text
		block++;
		String tableName = "BLOCK " + block;
		SymbolTable newTable = new SymbolTable(tableName);
		order.push(newTable);
		scope.push(newTable);
		if(ifStmt.contains("ELSE")) { //if there is actually an else statement
			level++; //increment label
			exitIfLevel = level; //set exit if level
		} else if(nesting==0) { //if we are not within a nested if statement
			level++; //increment
			exitIfLevel = level; //set exit of if statement to next level
		}
		nesting++; //increment nesting anytime we enter an if statement
	}

	@Override
	public void exitIf_stmt(LITTLEParser.If_stmtContext ctx) {
		scope.pop(); //pop current table off stack
		nesting--; //decrement nesting anytime we exit an if statement
		if(nesting==0) { //if we are not nested
			entry = new String[4];
			entry[0] = "LABEL";
			entry[3] = "label" + exitIfLevel;
			ir.add(entry); //add our exit label to IR list
		} else { //if we are within a nested if statement
			level++;
			entry = new String[4];
			entry[0] = "LABEL";
			entry[3] = "label" + level;
			ir.add(entry); //add our current label to IR list
		}
	}

	@Override
	public void enterElse_part(LITTLEParser.Else_partContext ctx) {
		String exception = ctx.getText(); //gets text from an else part
		if(exception.contains("ELSE")) { //determines if there is really an else or if it was empty	
			block++; //if there was an else create a new block, table, and scoping for it
			String tableName = "BLOCK " + block;
			SymbolTable newTable = new SymbolTable(tableName);
			order.push(newTable); //push new table onto stacks
			scope.push(newTable);
			entry = new String[4];
			entry[0] = "LABEL";
			entry[3] = "label" + exitIfLevel; //if there is an ELSE statement, add its label to IR
			level++;
			exitIfLevel = level; //sets the exit level of current if statement to 1 higher than else
			String[] entry2 = new String[4];
			entry2[0] = "JUMP"; //adds a jump label for exit if statement
			entry2[3] = "label" + exitIfLevel;
			ir.add(entry2); //adds jump and label entries
			ir.add(entry);
		}
	}

	@Override
	public void exitElse_part(LITTLEParser.Else_partContext ctx) {
		String exception = ctx.getText();
		if(exception.contains("ELSE")) { //if else actually has an "ELSE"
			scope.pop(); //pop symbol table off stack
		}
	}
	
	@Override
	public void enterParam_decl(LITTLEParser.Param_declContext ctx) {
		String typeName = ctx.getText(); //gets text from declaration
		boolean typeFloat = typeName.contains("FLOAT"); //determines type
		if(typeFloat) {
			typeName = typeName.replaceAll("FLOAT", "");
		} else {
			typeName = typeName.replaceAll("INT", "");
		}
		SymbolTable current = (SymbolTable)scope.peek(); //gets current symbol table
		if(typeFloat) { //adds appropriate type and name to current table
			current.addEntry(typeName, "FLOAT", null);
		} else {
			current.addEntry(typeName, "INT", null);
		}
	}

	@Override
	public void exitProgram(LITTLEParser.ProgramContext ctx) {
		entry = new String[4]; //when program ends add a return command
		entry[0] = "RET";
		ir.add(entry);
	}

	@Override
	public void enterString_decl(LITTLEParser.String_declContext ctx) {
		String stringDecl = ctx.getText(); //gets text from string declaration
		String nameValue = stringDecl.replaceAll("STRING", ""); //cleans text
		String[] split = nameValue.split(":="); //splits on :=
		String name = split[0]; //the name is before the split
		String value = split[1].substring(0, split[1].length()-1); //the value is after the split minus a ;
		SymbolTable current = (SymbolTable)scope.peek(); //adds entry to current table
		current.addEntry(name, "STRING", value);
	}

	@Override
	public void enterVar_decl(LITTLEParser.Var_declContext ctx) {
		String varDecl = ctx.getText(); //gets text from var declarations 
		boolean typeFloat = varDecl.contains("FLOAT"); //determines types
		if(typeFloat) { //cleans
			varDecl = varDecl.replaceAll("FLOAT", "");
		} else {
			varDecl = varDecl.replaceAll("INT", "");
		}
		varDecl = varDecl.replaceAll("\\;", "");
		String[] variables = varDecl.split("\\,"); //gets names of all variables created
		SymbolTable current = (SymbolTable)scope.peek(); //gets current table
		for(int i=0; i<variables.length; i++) { //runs through each variable
			if(typeFloat) { //adds to symbol table with appropriate name and type
				current.addEntry(variables[i], "FLOAT", null);
			} else {
				current.addEntry(variables[i], "INT", null);
			}	
		}
	}

	@Override
	public void enterCond(LITTLEParser.CondContext ctx) {
		String comparison = ctx.getText(); //gets conditional statement
		String type = "NULL";
		String varType = "NULL";
		String[] variables = new String[2]; //initializes string of the 2 vars being compared
		comparison.replaceAll("\\s", "");
		String operator = "NULL";
		if(comparison.contains(">=")) { //if greater than or equal
			variables = comparison.split(">="); //split accordingly
			type = "LT"; 
		} else if(comparison.contains(">")) { //if greater than
			variables = comparison.split(">"); //split accordingly
			type = "LE";
		} else if(comparison.contains("<=")) { //if less than or equal to
			variables = comparison.split("<="); //split accordingly
			type = "GT";
		} else if(comparison.contains("<")) { //if less than
			variables = comparison.split("<"); //split accordingly
			type = "GE";
		} else if(comparison.contains("!=")) { //if not equal
			variables = comparison.split("!="); //split accordingly
			type = "EQ";
		} else if(comparison.contains("=")) { //if equal
			variables = comparison.split("="); //split accordingly
			type = "NE";
		}
		if(getVarType(variables[0]).equals("FLOAT")) { //add appropriate F or I to type
			varType = "F";
			type += varType;
		} else if(getVarType(variables[0]).equals("INT")) {
			varType = "I";
			type += varType;
		}
		if(variables[1].contains("(")) { //if rhs variable is complex operation
			variables[1] = variables[1].replaceAll("[()]", ""); //handle operation
			variables[1] = variables[1].replaceAll("\\s", "");
			String[] rhSplit = variables[1].split("-");
			entry = new String[4];
			entry[0] = "STOREF";
			entry[1] = rhSplit[0];
			entry[3] = "$T" + register;
			register++;
			ir.add(entry);
			String[] entry2 = new String[4];
			entry2[0] = "SUBF";
			entry2[1] = entry[3];
		        entry2[2] = rhSplit[1];
			entry2[3] = "$T" + register;
			register++;
			ir.add(entry2);
			variables[1] = entry2[3];	
		}
		if(Character.isDigit(variables[1].charAt(0))) { //if comparison is a number
			entry = new String[4]; //add it to a register
			entry[0] = "STORE" + varType;
			entry[1] = variables[1];
			entry[3] = "$T" + register;
			register++;
			ir.add(entry);
			variables[1] = entry[3];
		}	
		entry = new String[4]; //create new entry in IR list for comparison
		entry[0] = type;
		entry[1] = variables[0];
		entry[2] = variables[1];
		traversal++;
		entry[3] = "label" +  traversal; //tells where to go to if operator not satisfied
		ir.add(entry);
	}

	public Stack<SymbolTable> getSymbolTables() { //returns the finalized ordered stack of symbol tables
		Stack<SymbolTable> correctOrder = new Stack<SymbolTable>(); 
		while(!order.empty()) {
			correctOrder.push((SymbolTable)order.pop()); //reverses order of stack so its in order
		}
		return correctOrder; //returns correctly ordered Stack of tables
	}

	public LinkedList<String[]> getIR() {
		return ir; //return final list of IR Code
	}
}

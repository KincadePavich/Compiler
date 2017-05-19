import java.util.*;

public class Listener extends LITTLEBaseListener {

	//INSTANCE VARIABLES
	Stack order; //keeps track of correct order of symbol tables and nesting
	Stack scope; //stack to keep track of current scope for var order etc.
	int block; //keeps track of BLOCK for IF, ELSE, WHILE
//	int level; //not currently used, keeps track of current scoping level

	public Listener() {
		order = new Stack<SymbolTable>(); //initialization
		scope = new Stack<SymbolTable>();
		block = 0 ; //block set to 0 initially
//		level = 0;
		SymbolTable firstTable = new SymbolTable("GLOBAL"); //1st table is always GLOBAL
		order.push(firstTable); //GLOBAL is pushed onto both stacks
		scope.push(firstTable);
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
//		level++;
	}

//	@Override
//	public void exitFunc_decl(LITTLEParser.Func_declContext ctx) {
//		level--;
//	}

	@Override
	public void enterWhile_stmt(LITTLEParser.While_stmtContext ctx) {
		block++; //block incremented for table name
		String tableName = "BLOCK " + block;
		SymbolTable newTable = new SymbolTable(tableName); //new table created
		order.push(newTable); //new table pushed onto stacks
		scope.push(newTable);
//		level++;
	}

//	@Override
//	public void exitWhile_stmt(LITTLEParser.While_stmtContext ctx) {
//		level--;
//	}

	@Override
	public void enterIf_stmt(LITTLEParser.If_stmtContext ctx) {
		//exact same functionality as entering a while statement
		block++;
		String tableName = "BLOCK " + block;
		SymbolTable newTable = new SymbolTable(tableName);
		order.push(newTable);
		scope.push(newTable);
//		level++;
	}

//	@Override
//	public void exitIf_stmt(LITTLEParser.If_stmtContext ctx) {
//		level--;
//	}

	@Override
	public void enterElse_part(LITTLEParser.Else_partContext ctx) {
		String exception = ctx.getText(); //gets text from an else part
		if(exception.contains("ELSE")) { //determines if there is really an else or if it was empty	
			block++; //if there was an else create a new block, table, and scoping for it
			String tableName = "BLOCK " + block;
			SymbolTable newTable = new SymbolTable(tableName);
			order.push(newTable); //push new table onto stacks
			scope.push(newTable);
//			level++;
		}
	}

//	@Override
//	public void exitElse_part(LITTLEParser.Else_partContext ctx) {
//		String exception = ctx.getText();
//		if(exception.contains("ELSE")) {
//			level--;
//		}
//	}
	
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

//	@Override
//	public void exitProgram(LITTLEParser.ProgramContext ctx) {
//
//	}

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

	public Stack<SymbolTable> getSymbolTables() { //returns the finalized ordered stack of symbol tables
		Stack<SymbolTable> correctOrder = new Stack<SymbolTable>(); 
		while(!order.empty()) {
			correctOrder.push((SymbolTable)order.pop()); //reverses order of stack so its in order
		}
		return correctOrder; //returns correctly ordered Stack of tables
	}
}

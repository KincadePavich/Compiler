/*
 * @author Tiana Smith, Kincade Pavich
 * @version 2.0
 * @date 17 February 2017
 */

import java.io.*;
import org.antlr.v4.runtime.*;

class Main {

	public static void main(String[] args) {
		try{
			FileInputStream file = new FileInputStream(args[0]);
			CharStream stream = new ANTLRInputStream(file);
			LITTLELexer lexer = new LITTLELexer(stream);
			LITTLEParser parser = new LITTLEParser(new CommonTokenStream(lexer));
		
			parser.program();
			int errors = parser.getNumberOfSyntaxErrors();
		
			if(errors == 0) {
				System.out.println("Accepted");
			} else {
				System.out.println("Not accepted");
			}		
		}catch (Exception e) {
			System.out.println("Parsing failed:" + e);
		}
	
	}

}


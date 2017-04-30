/*
 * This is the driver program for the lexer. It's only purpose
 * is to collect input from some stream, and apply the ANTLR
 * generated lexer on it.
 *
 * Note: You must add the antler jar file to your class path
 *       (using the -cp flag) before running javac or java
 *       on this file.
 *
 */

import java.io.*;
import org.antlr.v4.runtime.*;

class Main {

  public static void main(String[] args) {

    try {
      // The lexer takes in a 'CharStream' object, so we must
      // make one for our input stream. Note: You may want to
      // replace the 'System.in' with a file pointer to work
      // with the grading script.
      FileInputStream file = new FileInputStream(args[0]);
      CharStream stream = new ANTLRInputStream(file);

      // Create a new lexer on the specified 'CharStream'
      LITTLELexer lexer = new LITTLELexer(stream);
     
      // Lexer types are enumerated, so we need to create a
      // 'Vocabulary' to lookup the symbol names from the
      // enumerated value. Note: The 'EOF' token has a value
      // of -1, and all other reules are enumerated from 0.
      Vocabulary vocab = lexer.getVocabulary();

      // A simple loop that prints out all token symbols and
      // their literal values. This is not quite set up as you
      // want for the grading script - you will need to change
      // the format a bit.
      Token token = lexer.nextToken();
          
      while (token.getType() != Token.EOF) {
          System.out.println("Token Type: " + vocab.getSymbolicName(token.getType()));
          System.out.println("Value: " + token.getText());
	  token = lexer.nextToken();
      } 

    } catch (Exception e) {
      // General catch to handle any faulty lexing
      System.out.println("Lexing failed: " + e);
    }
  }

}


/**
 * The driver package holds the main driver
 */
package driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import code_generation.CodeGeneration;
import lexical.Tokenizer;
import sematic.Semantic;
import syntactic.Parser;

/**
 * The driver holds the main method too launch the compiler
 * @author Karan
 * @version 1.0
 */
public class Driver {
	
	private static Map<Integer, ArrayList<String>> map;

	/**
	 * Main Method. the lexical analyzer is initiated here.
	 * @param args (No arguments are needed to launch the compiler
	 */
    public static void main(String[] args) throws IOException {
       	
    	Tokenizer.lexicalAnalyzer();
    	Parser.parser();
    	new Semantic().initializeSematicAnalysis();
    	map = Semantic.getMap();
    	
    	if (map.size() == 0) {	new CodeGeneration().intializeCodeGeneration();								 } 
    	else 				 {	System.out.println("Unable to generate code. Please check the error log file");}    	
    }
}

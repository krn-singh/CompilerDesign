/**
 * The driver package holds the main driver
 */
package compiler.driver;

import java.io.IOException;
import compiler.lexical.Tokenizer;

/**
 * The driver holds the main method too launch the compiler
 * @author Karan
 * @version 1.0
 */
public class Driver {

	/**
	 * Main Method. the lexical analyzer is initiated here.
	 * @param args (No arguments are needed to launch the compiler
	 */
    public static void main(String[] args) throws IOException {
       	
    	Tokenizer.lexicalAnalyzer();
    }
}

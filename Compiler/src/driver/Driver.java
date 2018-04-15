/**
 * The driver package holds the main driver
 */
package driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import code_generation.CodeGeneration;
import helper.DataReadWrite;
import lexical.Tokenizer;
import sematic.Semantic;
import syntactic.Parser;

/**
 * The driver holds the main method too launch the compiler
 * 
 * @author krn-singh
 */
public class Driver {

	private static Map<Integer, ArrayList<String>> map;

	/**
	 * Main Method. Initiates the compiler.
	 * 
	 * @param args (No arguments are needed to launch the compiler)
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void main(String[] args) throws IOException {

		Scanner scanner = new Scanner(System.in);
		try {
			String phase = new String();
			phase += "********Make the following selections*******\n";
			phase += "1: Lexical Analyzer\n";
			phase += "2: Syntactic Analyzer\n";
			phase += "3: Semantic Analyzer\n";
			phase += "4: Code Generation\n";
			phase += "5: Exit\n";
			System.out.println(phase);
			
			switch (scanner.nextLine()) {
			case "1":
				System.out.println("Provide the path for input file (say, data/inputFiles/input.txt )\n");
				Tokenizer.lexicalAnalyzer(scanner.nextLine());
				DataReadWrite.writeErrors(Tokenizer.getMap());
				break;
				
			case "2":
				System.out.println("Provide the path for input file (say, data/inputFiles/input.txt )\n");
				Tokenizer.lexicalAnalyzer(scanner.nextLine());
				Parser.intializeParser();
				DataReadWrite.writeErrors(Parser.getMap());
				break;
				
			case "3":
				System.out.println("Provide the path for input file (say, data/inputFiles/input.txt )\n");
				Tokenizer.lexicalAnalyzer(scanner.nextLine());
				Parser.intializeParser();
				new Semantic().initializeSematicAnalysis();
				break;
				
			case "4":
				System.out.println("Provide the path for input file (say, data/inputFiles/input.txt )\n");
				Tokenizer.lexicalAnalyzer(scanner.nextLine());
				Parser.intializeParser();
				new Semantic().initializeSematicAnalysis();
				map = Semantic.getMap();

				if (map.size() == 0) {
					new CodeGeneration().intializeCodeGeneration();
				} else {
					System.out.println("Unable to generate code. Please check the error log file");
				}
				break;
				
			case "5":
				System.out.println("Terminating.....");
				System.exit(0);
				break;				
			default:
				break;
			}
			
			

		} finally {
			scanner.close();
		}
	}
}

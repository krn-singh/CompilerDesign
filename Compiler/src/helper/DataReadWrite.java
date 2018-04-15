package helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import constants.Constants;
import lexical.Token;

/**
 * Class acts as a medium between the data files and all the operations of the compiler.
 * 
 * @author krn-singh
 */
public class DataReadWrite {

	
	private static BufferedReader read;
	private static BufferedWriter write;
	private static ArrayList<String> productionWithEpsilon = new ArrayList<>();
	private static ArrayList<String> productionWithEpsilonInFirstSet = new ArrayList<>();
	
	/**
	 * Reads the input and omits the the slash-slash and slash-star comments
	 * 
	 * @return a collection of source code with their respective line number in the input file
	 * @throws IOException handles the I/O related interruptions
	 */
	public static TreeMap<Integer, String> readInput(String inputPath) throws IOException {

		read = new BufferedReader(new InputStreamReader(new FileInputStream("data/inputFiles/input.txt")));
		String line = "";
		int lineNumber = 0;
		TreeMap<Integer, String> input = new TreeMap<>();
		try {

			while ((line = read.readLine()) != null) {

				lineNumber++;
				exit: for (int currentChar = 0; currentChar < line.length(); currentChar++) {

					switch (line.charAt(currentChar)) {

					case '/':
						currentChar++;
						if (line.charAt(currentChar) == '/') {

							line = line.substring(0, currentChar - 1);

						} else if (line.charAt(currentChar) == '*') {

							String intialLine = line;
							int nestedBlock  = 1;
							int currentLineLength = line.length();

							// Consuming Input before the start of slash-star comment
							line = line.substring(0, currentChar - 1);
							input.put(lineNumber, line);
							lineNumber++;

							// Consuming the input after the slash-star comment in the same line
							if (intialLine.substring(currentChar + 1, currentLineLength).contains("*/")) {
								line = line + " " + intialLine.substring(intialLine.indexOf("*/") + 2);
								lineNumber--;
								break;
							}

							while ((line = read.readLine()) != null && (nestedBlock > 0)) {
								if 		(line.contains("/*")) {	nestedBlock++;	}
								else if (line.contains("*/")) {	nestedBlock--;	}
								lineNumber++;
							}

							if (line != null) {

								if (line.contains("*/")) {

									line = line.substring(line.indexOf("*/") + 2);
								}
							} else {
								break exit;
							}
						}
						break;

					default:
						break;
					}
				}

				input.put(lineNumber, line);
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			read.close();
		}

		return input;
	}

	/**
	 * Writes the output
	 * 
	 * @param tokens Data structure containing the token information (token type, token value, location in source code)
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void writeOutput(List<Token> tokens) throws IOException {
	
		write = new BufferedWriter(new FileWriter(new File(Constants.TOKEN_FILE_PATH)));
		try {
	
			for (Token token : tokens) {
				write.write(token.toString());
				write.newLine();
			}
		} catch (Exception e) {
	
			e.printStackTrace();
		} finally {
	
			write.close();
		}
	
	}

	/**
	 * Outputs the token type in AToCc format
	 * 
	 * @param aToCcFormat List containing the token type in AToCc format
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void writeAToCc(List<String> aToCcFormat) throws IOException {

		write = new BufferedWriter(new FileWriter(new File(Constants.ATOCC_FILE_PATH)));
		try {

			for (String aToCc : aToCcFormat) {

				write.write(aToCc);
				write.newLine();
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			write.close();
		}

	}
	
	/**
	 * Prints the error messages
	 * @param map errors encountered in the given input
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void writeErrors(Map<Integer, ArrayList<String>> map) throws IOException {

		write = new BufferedWriter(new FileWriter(new File(Constants.ERROR_FILE_PATH)));
		try {

			Integer lineNumber;
			Iterator<Map.Entry<Integer, ArrayList<String>>> entrySet = map.entrySet().iterator();
			Map.Entry<Integer, ArrayList<String>> entry;
			while (entrySet.hasNext()) {

				entry = entrySet.next();
				lineNumber = entry.getKey();
				if (lineNumber != -1) {
					for (String string : entry.getValue()) {

						if (lineNumber == 0) {
							write.write(string);
							write.newLine();
						} else {
							write.write(string+lineNumber);
							write.newLine();
						}					
					}
				}				
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			write.close();
		}
	}

	/**
	 * Reads the grammar
	 * 
	 * @return rules (productions) of the grammar required for parsing
	 * @throws IOException handles the I/O related interruptions
	 */
	public static TreeMap<String, String> readGrammar() throws IOException {
		
		read = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.GRAMMAR_FILE_PATH)));
		int productionNumber = 0;
		String production = "";
		String LHS = "";
		String RHS = "";
		TreeMap<String, String> grammar = new TreeMap<>();
		
		try {
			
			while ((production = read.readLine()) != null) {
				
				productionNumber++;
				grammar.put(Integer.toString(productionNumber), production);
				LHS = production.split("->")[0].replaceAll("\\s","");
				RHS = production.split("->")[1].replaceAll("\\s","");
				
				if (RHS.equals("EPSILON")) {
					
					productionWithEpsilon.add(LHS);
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			
			read.close();
		}
		
		return grammar;
	}
	
	/**
	 * Reads the first sets of all non-terminals
	 * 
	 * @return collection of all non-terminals and their corresponding first sets.
	 * @throws IOException handles the I/O related interruptions
	 */
	public static TreeMap<String, ArrayList<String>> readFirstSets() throws IOException {
		
		read = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.FIRSTSETS_FILE_PATH)));
		String set = "";
		TreeMap<String, ArrayList<String>> firstSets = new TreeMap<>();
		
		try {
			
			String key="";
			String value="";
			ArrayList<String> valueSet;
			while ((set = read.readLine()) != null) {
				
				key = set.split("->")[0].replaceAll("\\s", "");
				value = set.split("->")[1].replaceAll("\\s", "");
				valueSet = new ArrayList<>();
				
				for (int i = 0; i < value.split("\\|").length; i++) {
					
					if (value.split("\\|")[i].equals("EPSILON")) {
						
						productionWithEpsilonInFirstSet.add(key);
					} else {

						valueSet.add(value.split("\\|")[i]);
					}				
				}
				
				firstSets.put(key, valueSet);
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			
			read.close();
		}
		return firstSets;
	}
	
	/**
	 * Reads the follow sets of non-terminals
	 * 
	 * @return collection of non-terminals and their corresponding follow sets.
	 * @throws IOException handles the I/O related interruptions
	 */
	public static TreeMap<String, ArrayList<String>> readFollowSets() throws IOException {
		
		read = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.FOLLOWSETS_FILE_PATH)));
		String set = "";
		TreeMap<String, ArrayList<String>> followSets = new TreeMap<>();
		
		try {
			
			String key="";
			String value="";
			ArrayList<String> valueSet;
			while ((set = read.readLine()) != null) {
				
				key = set.split("->")[0].replaceAll("\\s", "");
				value = set.split("->")[1].replaceAll("\\s", "");
				valueSet = new ArrayList<>();
				
				for (int i = 0; i < value.split("\\|").length; i++) {
					
					valueSet.add(value.split("\\|")[i]);
				}
				
				followSets.put(key, valueSet);
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			
			read.close();
		}
		return followSets;
	}
	
	/**
	 * Productions with epsilon
	 * 
	 * @return rules with epsilon
	 */
	public static ArrayList<String> productionsWithEpsilon() {
		
		return productionWithEpsilon;
	}
	
	/**
	 * Productions with epsilon
	 * 
	 * @return rules with epsilon in the first set
	 */
	public static ArrayList<String> productionsWithEpsilonInFirstSets() {
		
		return productionWithEpsilonInFirstSet;
	}
	
	/**
	 * Writes the derivation after parsing
	 * 
	 * @param derivation list of step-by-step derivation of the input program
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void writeDerivation(List<String> derivation) throws IOException {
		
		write = new BufferedWriter(new FileWriter(new File(Constants.DERIVATION_FILE_PATH)));
		try {
	
			for (String string : derivation) {
				
				write.write(string);
				write.newLine();
			}
			
		} catch (Exception e) {
	
			e.printStackTrace();
		} finally {
	
			write.close();
		}
	
	}

	/**
	 * Writes the symbol table
	 * 
	 * @param symbolTables list of all entries of the symbol table
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void writeSymbolTables(List<String> symbolTables) throws IOException {
		
		write = new BufferedWriter(new FileWriter(new File(Constants.SYMTABLE_FILE_PATH)));
		try {
	
			for (String string : symbolTables) {
				
				write.write(string);
				write.newLine();
			}
			
		} catch (Exception e) {
	
			e.printStackTrace();
		} finally {
	
			write.close();
		}
	
	}
	
	/**
	 * Outputs the moon code
	 * 
	 * @param moonCode moon code instructions
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void writeMoonCode(String moonCode) throws IOException {
		
		write = new BufferedWriter(new FileWriter(new File(Constants.MOONCODE_FILE_PATH)));
		try {
	
			write.write(moonCode);
			
		} catch (Exception e) {
	
			e.printStackTrace();
		} finally {
	
			write.close();
		}
	
	}
	
	/**
	 * Outputs the Abstract Syntax Tree
	 * 
	 * @param abstractTree level-by-level collection of AST nodes
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void writeAST(String abstractTree) throws IOException {
		
		write = new BufferedWriter(new FileWriter(new File(Constants.AST_FILE_PATH)));
		try {
	
			write.write(abstractTree);

		} catch (Exception e) {
	
			e.printStackTrace();
		} finally {
	
			write.close();
		}
	
	}
}

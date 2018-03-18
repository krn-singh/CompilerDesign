package compiler.helper;

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
import java.util.Stack;
import java.util.TreeMap;

import compiler.lexical.Token;

/**
 * class acts as a medium between lexical analyzer and the data files.
 * @author Karan
 * @version 1.0
 */
public class DataReadWrite {

	
	private static BufferedReader read;
	private static BufferedWriter write;
	private static ArrayList<String> productionWithEpsilon = new ArrayList<>();
	private static ArrayList<String> productionWithEpsilonInFirstSet = new ArrayList<>();
	private static final String INPUT_FILE_PATH = "data/inputFiles/input.txt";
	private static final String ERROR_FILE_PATH = "data/outputFiles/error.txt";
	private static final String TOKEN_FILE_PATH = "data/outputFiles/tokenOutput.txt";
	private static final String ATOCC_FILE_PATH = "data/outputFiles/aToCc.txt";
	private static final String GRAMMAR_FILE_PATH = "data/inputFiles/grammar.txt";
	private static final String FIRSTSETS_FILE_PATH = "data/inputFiles/firstSets.txt";
	private static final String FOLLOWSETS_FILE_PATH = "data/inputFiles/followSets.txt";
	private static final String DERIVATION_FILE_PATH = "data/outputFiles/derivation.txt";
	private static final String SYMTABLE_FILE_PATH = "data/outputFiles/sym-table.txt";
	
	/**
	 * Reads the input and omits the the slash-slash and slash-star comments
	 * @return a collection of source code with their respective line number in the input file
	 * @throws IOException
	 */
	public static TreeMap<Integer, String> readInput() throws IOException {

		read = new BufferedReader(new InputStreamReader(new FileInputStream(INPUT_FILE_PATH)));
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

							while ((line = read.readLine()) != null && (!line.contains("*/"))) {
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
	 * Writes the output to the output.txt file
	 * @param tokens Data structure containing the token information (token type, token value, location in source code)
	 * @throws IOException
	 */
	public static void writeOutput(List<Token> tokens) throws IOException {
	
		write = new BufferedWriter(new FileWriter(new File(TOKEN_FILE_PATH)));
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
	 * @param aToCcFormat List containing the token type in AToCc format
	 * @throws IOException
	 */
	public static void writeAToCc(List<String> aToCcFormat) throws IOException {

		write = new BufferedWriter(new FileWriter(new File(ATOCC_FILE_PATH)));
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
	 * Prints the error messages in the error.txt file
	 * @param errors List of errors encountered in the given input
	 * @throws IOException
	 */
	public static void writeErrors(Map<String, ArrayList<String>> hashMap) throws IOException {

		write = new BufferedWriter(new FileWriter(new File(ERROR_FILE_PATH)));
		try {

			String lineNumber;
			Iterator<Map.Entry<String, ArrayList<String>>> entrySet = hashMap.entrySet().iterator();
			Map.Entry<String, ArrayList<String>> entry;
			while (entrySet.hasNext()) {

				entry = entrySet.next();
				lineNumber = entry.getKey();
				if (!lineNumber.equals("-1")) {
					for (String string : entry.getValue()) {

						write.write(string+lineNumber);
						write.newLine();
					}
				}				
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			write.close();
		}
	}

	public static TreeMap<String, String> readGrammar() throws IOException {
		
		read = new BufferedReader(new InputStreamReader(new FileInputStream(GRAMMAR_FILE_PATH)));
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
	
	public static TreeMap<String, ArrayList<String>> readFirstSets() throws IOException {
		
		read = new BufferedReader(new InputStreamReader(new FileInputStream(FIRSTSETS_FILE_PATH)));
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
	
	public static TreeMap<String, ArrayList<String>> readFollowSets() throws IOException {
		
		read = new BufferedReader(new InputStreamReader(new FileInputStream(FOLLOWSETS_FILE_PATH)));
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
	
	public static Stack<String> readTokensInAToCcFormat() throws IOException {
		
		read = new BufferedReader(new InputStreamReader(new FileInputStream(ATOCC_FILE_PATH)));
		String aToCc = "";
		Stack<String> tokenStack = new Stack<>();
		
		try {
			
			if ((aToCc = read.readLine()) != null) {
				
				aToCc+="$";
			}
			
			for (int i = aToCc.split("\\s").length-1; i >= 0; i--) {
				
				tokenStack.push(aToCc.split("\\s")[i]);
			}
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			
			read.close();
		}
		
		return tokenStack;
	}
	
	public static ArrayList<String> productionsWithEpsilon() {
		
		return productionWithEpsilon;
	}
	
	public static ArrayList<String> productionsWithEpsilonInFirstSets() {
		
		return productionWithEpsilonInFirstSet;
	}
	
	public static void writeDerivation(List<String> derivation) throws IOException {
		
		write = new BufferedWriter(new FileWriter(new File(DERIVATION_FILE_PATH)));
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

	public static void writeSymbolTables(List<String> symbolTables) throws IOException {
		
		write = new BufferedWriter(new FileWriter(new File(SYMTABLE_FILE_PATH)));
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
}

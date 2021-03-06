/**
 * The syntactic package holds the classes required for parsing and later on,
 * building The Abstract Syntax Tree.
 */
package syntactic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import constants.CompilerEnum;
import constants.CompilerEnum.TokenType;
import helper.DataReadWrite;
import helper.Util;
import lexical.Token;
import lexical.Tokenizer;

/**
 * Provides all the operations required for parsing
 * 
 * @author krn-singh
 */
public class Parser {

	private static ArrayList<String> terminals;
	private static TreeMap<String, Integer> rowHeaders;
	private static TreeMap<String, Integer> columnHeaders;
	private static TreeMap<String, ArrayList<String>> firstSets;
	private static TreeMap<String, ArrayList<String>> followSets;
	private static TreeMap<String, String> grammar;
	private static String[][] parseTable;
	private static List<Token> tokenList;
	private static List<String> derivationList;
	private static Stack<String> stack;
	private static Stack<String> ruleStack;
	private static Stack<AstNode> contextStack;
	private static int tokenCounter;
	private static String tokenName;
	private static int lineNumber;
	private static String tokenValue;
	private static TokenType type;
	private static AstNode root;
	public static Map<Integer, ArrayList<String>> map;
	
	/**
	 * Getter for all the errors up till syntactic phase
	 * @return collection of errors
	 */
	public static Map<Integer, ArrayList<String>> getMap() {
		return map;
	}

	/**
	 * Initializes the data members of the class and calls the parsing function.
	 * 
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void intializeParser() throws IOException {
		// list of all terminals in the grammar
		terminals = new ArrayList<>();
		// collections of non-terminals as row headers 
		rowHeaders = new TreeMap<>();
		// collections of terminals as column headers
		columnHeaders = new TreeMap<>();
		// list of all valid tokens in the input 
		tokenList = new ArrayList<Token>();
		// list of step-by-step derivation of the input 
		derivationList = new ArrayList<>();
		// Stack for pushing the grammar rules
		stack = new Stack<>();
		// Stack for storing the symbols corresponding to a specific node of syntax tree
		ruleStack = new Stack<>();
		// Stack for storing the nodes corresponding to a specific node of syntax tree
		contextStack = new Stack<>();
		tokenCounter = 0;
		tokenName = "";
		lineNumber = -1;
		tokenValue = "";
		grammar = DataReadWrite.readGrammar();
		map = Tokenizer.getMap();
		parser();
	}
	
	/**
	 * Main function that controls the parsing mechanism.
	 * 
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void parser() throws IOException {

		System.out.println("Reading grammar and generating the first and follow sets......");
		// Read the first sets of non-terminals
		firstSets();
		// Read the follow sets of non-terminals
		followSets();
		System.out.println("Building the parse table");
		// Initialize the parsing table with row and column headers
		intializeTable();
		// Build the parsing table
		buildTable();
		//printTable();
		System.out.println("Parsing in progress...... Building the Abstract Syntax tree");
		System.out.println();
		// Perform the table driven parsing
		parsing();
		// Print the Abstract Syntax Tree
		new AstNode().print(Parser.getRoot());
		System.out.println();
	}

	/**
	 * Fetch the first sets of all non-terminals
	 * 
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void firstSets() throws IOException {

		firstSets = DataReadWrite.readFirstSets();

		try {

			Iterator<Map.Entry<String, ArrayList<String>>> entrySet = firstSets.entrySet().iterator();
			Map.Entry<String, ArrayList<String>> entry;
			while (entrySet.hasNext()) {

				entry = entrySet.next();
				for (String string : entry.getValue()) {

					if (!terminals.contains(string)) {
						terminals.add(string);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No content in input file");
		}
	}

	/**
	 * Fetch the follow sets
	 * 
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void followSets() throws IOException {

		followSets = DataReadWrite.readFollowSets();

		try {

			Iterator<Map.Entry<String, ArrayList<String>>> entrySet = followSets.entrySet().iterator();
			Map.Entry<String, ArrayList<String>> entry;
			while (entrySet.hasNext()) {

				entry = entrySet.next();
				for (String string : entry.getValue()) {

					if (!terminals.contains(string)) {
						terminals.add(string);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No content in input file");
		}
	}

	/**
	 * Initialize the parsing table with row and column headers
	 */
	public static void intializeTable() {

		try {

			parseTable = new String[followSets.size()+1][terminals.size()+1];
			int rowCounter = 0;
			int columnCounter = 0;
			parseTable[0][0] = "#";

			List<String> rowValues = new ArrayList<>(followSets.keySet());
			for (String string : rowValues) {

				rowCounter++;
				parseTable[rowCounter][0] = string;
				rowHeaders.put(string, rowCounter);
			}

			for (String string : terminals) {

				columnCounter++;
				parseTable[0][columnCounter] = string;
				columnHeaders.put(string, columnCounter);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No content in input file");
		}
	}

	/**
	 * Build the parsing table
	 */
	public static void buildTable() {

		try {

			Iterator<Map.Entry<String, String>> entrySet = grammar.entrySet().iterator();
			Map.Entry<String, String> entry;
			String LHS = "";
			String RHS = "";
			ArrayList<String> set;
			String productionNumber;
			ArrayList<String> rulesWithEpsilon = DataReadWrite.productionsWithEpsilon();
			while (entrySet.hasNext()) {

				entry = entrySet.next();
				productionNumber = entry.getKey();
				LHS = entry.getValue().split("->")[0].replaceAll("\\s", "");
				
				int index = 0;
				while (entry.getValue().split("->")[1].trim().split("\\s")[index].startsWith("#")) {
					index++;
				}
				
				RHS = entry.getValue().split("->")[1].trim().split("\\s")[index];
				
				if (RHS.equals("EPSILON")) {
					
					set = followSets.get(LHS);
					for (String string : set) {
						
						parseTable[rowHeaders.get(LHS)][columnHeaders.get(string)] = productionNumber;
					}
				} else {

					set = firstSets.get(RHS);
					for (String string : set) {
						
						parseTable[rowHeaders.get(LHS)][columnHeaders.get(string)] = productionNumber;
					}
					
					if (rulesWithEpsilon.contains(RHS)) {
						
						set = firstSets.get(LHS);
						for (String string : set) {
							
							if (parseTable[rowHeaders.get(LHS)][columnHeaders.get(string)] == null) {
								
								parseTable[rowHeaders.get(LHS)][columnHeaders.get(string)] = productionNumber;
							}							
						}
						
						set = followSets.get(LHS);
						for (String string : set) {
							
							if (parseTable[rowHeaders.get(LHS)][columnHeaders.get(string)] == null) {
								
								parseTable[rowHeaders.get(LHS)][columnHeaders.get(string)] = productionNumber;
							}							
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No content in input file");
		}
	}
	
	/**
	 * Print the parsing table
	 */
	public static void printTable() {
		
		for (int i = 0; i < parseTable.length; i++) {
			
			for (int j = 0; j < parseTable[i].length; j++) {
				
				System.out.print(parseTable[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	/**
	 * Performs the parsing
	 * 
	 * @throws IOException handles the I/O related interruptions
	 */
	public static void parsing() throws IOException {
		
		try {
			
			stack.push("$");
			stack.push(CompilerEnum.START_SYMBOL);
			
			tokenList = Tokenizer.outputTokens;
			tokenList.add(new Token(TokenType.eof, "$", -1));
			
			String ruleLHS="";
			String ruleRHS="";
			nextToken();
			String top = "";
			boolean error = false;
			String rhsDerivation;
			String derivation = "prog";			
			derivationList.add(derivation);
			while (stack.peek() != "$") {
				
				top = stack.peek();
				
				if (terminals.contains(top)) {
					
					if (top.equals(tokenName)) {
						
						stack.pop();
						nextToken();
					} else {
						
						
						skipErrors(true);
						error = true;
					}
				} else {
					
					if (top.equals("#BEGIN_varDecl")) {
						Stack<String> currentContext = new Stack<>();
						for (int i = 0; i < 2; i++) {
							currentContext.push(ruleStack.peek());
							ruleStack.pop();
						}
						ruleStack.push(top);
						for (int j = 0; j < 2; j++) {
							ruleStack.push(currentContext.peek());
							currentContext.pop();
						}
						stack.pop();
						currentContext = null;
					} else if (top.equals("#BEGIN_funcDecl")) {
						Stack<String> currentContext = new Stack<>();
						for (int i = 0; i < 2; i++) {
							currentContext.push(ruleStack.peek());
							ruleStack.pop();
						}
						ruleStack.push(top);
						for (int j = 0; j < 2; j++) {
							ruleStack.push(currentContext.peek());
							currentContext.pop();
						}
						stack.pop();
						currentContext = null;
					} else if (top.equals("#BEGIN_scopeSpec")) {
						Stack<String> currentContext = new Stack<>();
						currentContext.push(ruleStack.peek());
						ruleStack.pop();
						ruleStack.push(top);
						ruleStack.push(currentContext.peek());
						currentContext.pop();
						stack.pop();
						currentContext = null;
					} else if (top.equals("#BEGIN_assignStat")) {
						Stack<String> currentContext = new Stack<>();
						currentContext.push(ruleStack.peek());
						ruleStack.pop();
						ruleStack.push(top);
						ruleStack.push(currentContext.peek());
						currentContext.pop();
						stack.pop();
						currentContext = null;
					} else if (top.equals("#BEGIN_var")) {
						Stack<String> currentContext = new Stack<>();
						currentContext.push(ruleStack.peek());
						ruleStack.pop();
						ruleStack.push(top);
						ruleStack.push(currentContext.peek());
						currentContext.pop();
						stack.pop();
						currentContext = null;
					} else if (top.equals("#BEGIN_multOp")) {
						Stack<String> currentContext = new Stack<>();
						currentContext.push(ruleStack.peek());
						ruleStack.pop();
						ruleStack.push(top);
						ruleStack.push(currentContext.peek());
						currentContext.pop();
						stack.pop();
						currentContext = null;
					} else if (top.equals("#BEGIN_addOp")) {
						Stack<String> currentContext = new Stack<>();
						currentContext.push(ruleStack.peek());
						ruleStack.pop();
						ruleStack.push(top);
						ruleStack.push(currentContext.peek());
						currentContext.pop();
						stack.pop();
						currentContext = null;
					} else if (top.equals("#BEGIN_relOp")) {
						Stack<String> currentContext = new Stack<>();
						currentContext.push(ruleStack.peek());
						ruleStack.pop();
						ruleStack.push(top);
						ruleStack.push(currentContext.peek());
						currentContext.pop();
						stack.pop();
						currentContext = null;
					} else if (top.startsWith("#BEGIN_")) {
						ruleStack.push(top);
						stack.pop();
					} else if (top.equals("#END_prog")) {
						AstNode node = new AstNode();
						node.setNodeType(top.substring(5));
						int count = 0;
						Stack<AstNode> currentContext = new Stack<>(); 
						LinkedList<AstNode> childrens = new LinkedList<>();
						while (!ruleStack.peek().equals("#BEGIN_"+top.substring(5))) {
							count++;
							ruleStack.pop();
							currentContext.push(contextStack.peek());
							contextStack.pop();
						}
						for (int i = 0; i < count; i++) {
							childrens.add(currentContext.peek());
							currentContext.pop();
						}
						node.setChildrens(childrens);
						stack.pop();
						contextStack.push(node);
						root = node;
						childrens = null;
						ruleStack = null;
						contextStack = null;
						currentContext = null;
					} else if (top.startsWith("#END_")) {
						AstNode node = new AstNode();
						node.setNodeType(top.substring(5));
						int count = 0;
						Stack<AstNode> currentContext = new Stack<>(); 
						LinkedList<AstNode> childrens = new LinkedList<>();
						while (!ruleStack.peek().equals("#BEGIN_"+top.substring(5))) {
							count++;
							ruleStack.pop();
							currentContext.push(contextStack.peek());
							contextStack.pop();
						}
						for (int i = 0; i < count; i++) {
							childrens.add(currentContext.peek());
							currentContext.pop();
						}
						node.setChildrens(childrens);
						stack.pop();
						contextStack.push(node);
						childrens = null;
						currentContext = null;
					} else if (top.startsWith("#MAKE_NODE")) {
						stack.pop();
						AstNode node = new AstNode();
						if ((type == TokenType.id && (tokenList.get(tokenCounter).type == TokenType.id)) || (type == TokenType.Int) || (type == TokenType.Float)) {
							
							node.setNodeType("typeNode");
							node.setData(tokenValue);
							node.setLineNumber(lineNumber);
						} else if(type == TokenType.id) {
							
							node.setNodeType("idNode");
							node.setData(tokenValue);
							node.setLineNumber(lineNumber);
						} else if((type == TokenType.intNum) || (type == TokenType.floatNum)) {
							
							node.setNodeType("numNode");
							node.setData(tokenValue);
							node.setType(type == TokenType.intNum ? "int" : "float");
							node.setLineNumber(lineNumber);
						} else if (type == TokenType.operator || tokenValue.equals("or") || tokenValue.equals("and")) {
							
							node.setNodeType("operatorNode");
							node.setData(tokenValue);
							node.setLineNumber(lineNumber);
						}
						contextStack.add(node);
						ruleStack.push(stack.peek());
					} else {
						
						if (parseTable[rowHeaders.get(top)][columnHeaders.get(tokenName)] != null) {
							
							stack.pop();
							ruleLHS = grammar.get(parseTable[rowHeaders.get(top)][columnHeaders.get(tokenName)]).split("->")[0].trim();
							ruleRHS = grammar.get(parseTable[rowHeaders.get(top)][columnHeaders.get(tokenName)]).split("->")[1].trim();
							
							
							if (!ruleRHS.equals("EPSILON")) {
								
								rhsDerivation = "";
								for (int i = ruleRHS.split("\\s").length-1; i >= 0; i--) {
									
									stack.push(ruleRHS.split("\\s")[i]);	
								}
								
								for (String string : ruleRHS.split("\\s")) {
									
									if (!string.startsWith("#")) {
										
										rhsDerivation+=string+" ";
									}
								}
								
								derivation = derivation.replaceFirst(ruleLHS, rhsDerivation);
								derivation = derivation.replaceAll("\\s+", " ");
								derivationList.add(derivation);
							} else {
								
								derivation = derivation.replaceFirst(ruleLHS, "");
								derivation = derivation.replaceAll("\\s+", " ");
								derivationList.add(derivation);
							}
							
						} else {
							
							skipErrors(false);
							error = true;
						}
					}
				}			
			}
			
			if (!tokenName.equals("$") || error == true) {
				
				derivationList.clear();
				DataReadWrite.writeDerivation(derivationList);
				System.out.println("Parsing cannot be completed successfully because of errors in the source file. Please check the error file and rectify the given input.");
				System.out.println();
				System.out.println("************* Printing Tree (Incomplete due to unsuccessful parsing) ************");
				System.out.println();
			} else {
				
				DataReadWrite.writeDerivation(derivationList);
				System.out.println("Parsing Successful");
				System.out.println();
				System.out.println("************* Printing Tree ************");
				System.out.println();
			}
		} catch (Exception e) {

			e.printStackTrace();
		}		
	}
	
	/**
	 * Gets the next token in the token list
	 */
	public static void nextToken() {
		
		try {
			if (tokenList.get(tokenCounter).type == TokenType.id) {
				
				tokenName = "id";
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				type = tokenList.get(tokenCounter).type;
				tokenValue = tokenList.get(tokenCounter).tokenValue;
				tokenCounter++;
			} else if(tokenList.get(tokenCounter).type == TokenType.intNum) {
				
				tokenName = "intNum";
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				type = tokenList.get(tokenCounter).type;
				tokenValue = tokenList.get(tokenCounter).tokenValue;
				tokenCounter++;
			} else if(tokenList.get(tokenCounter).type == TokenType.floatNum) {
				
				tokenName = "floatNum";
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				type = tokenList.get(tokenCounter).type;
				tokenValue = tokenList.get(tokenCounter).tokenValue;
				tokenCounter++;
			} else {

				tokenName = tokenList.get(tokenCounter).getTokenValue();
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				type = tokenList.get(tokenCounter).type;
				tokenValue = tokenList.get(tokenCounter).tokenValue;
				tokenCounter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
	}
	
	/**
	 * Error control mechanism during parsing.
	 * 
	 * @param isTerminal true if the top of the stack is a terminal and there is a syntax error
	 */
	public static void skipErrors(boolean isTerminal) {
		
		try {
			Util util = new Util();
			
			if (isTerminal) {
                	
				if (tokenCounter<tokenList.size()) {
					util.setMap(map);
	                map = util.reportError(lineNumber, "Error ("+tokenName+") reported during Syntactic phase in line ");
					nextToken();
				} else {		stack.pop();		}	
				
			} else {			

				ArrayList<String> followSet = followSets.get(stack.peek());
				
				if (followSet.contains(tokenName) || tokenName.equals("$")) {
					
					util.setMap(map);
	                map = util.reportError(lineNumber, "Error ("+tokenName+") reported during Syntactic phase in line ");
					stack.pop();
					
				} else {

					if (tokenCounter<tokenList.size()) {
						util.setMap(map);
		                map = util.reportError(lineNumber, "Error ("+tokenName+") reported during Syntactic phase in line ");
						nextToken();
					} else {	
						util.setMap(map);
		                map = util.reportError(lineNumber, "Error ("+tokenName+") reported during Syntactic phase in line ");
						stack.pop();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * returns the root of the abstract syntax tree
	 */
	public static AstNode getRoot() {
		return root;
	}

	/**
	 * Set the root of AST
	 * 
	 * @param root Root of AST
	 */
	public static void setRoot(AstNode root) {
		Parser.root = root;
	}
}

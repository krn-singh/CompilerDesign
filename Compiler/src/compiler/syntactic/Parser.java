package compiler.syntactic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import compiler.constants.CompilerEnum;
import compiler.constants.CompilerEnum.TokenType;
import compiler.helper.DataReadWrite;
import compiler.lexical.Token;
import compiler.lexical.Tokenizer;

public class Parser {

	public static void parser() throws IOException {

		grammar = DataReadWrite.readGrammar();
		// grammar();
		firstSets();
		followSets();
		intializeTable();
		buildTable();
		printTable();
		parsing();
		
	}

	public static void grammar() throws IOException {

		try {

			Iterator<Map.Entry<String, String>> entrySet = grammar.entrySet().iterator();
			Map.Entry<String, String> entry;
			while (entrySet.hasNext()) {

				entry = entrySet.next();
				System.out.println(entry.getKey() + ") " + entry.getValue());
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No content in input file");
		}
	}

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
				RHS = entry.getValue().split("->")[1].trim().split("\\s")[0];
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
	
	public static void printTable() {
		
		for (int i = 0; i < parseTable.length; i++) {
			
			for (int j = 0; j < parseTable[i].length; j++) {
				
				/*if (parseTable[i][j]==null) {
					
					parseTable[i][j] = "0";
				}*/
				System.out.print(parseTable[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public static void parsing() throws IOException {
		
		try {
			
			stack.push("$");
			stack.push(CompilerEnum.START_SYMBOL);
			
			//Stack<String> tokenStack = new Stack<>();			
			//tokenStack = DataReadWrite.readTokensInAToCcFormat();
			
			tokenList = Tokenizer.outputTokens;
			tokenList.add(new Token(TokenType.eof, "$", -1));
			
			String ruleLHS="";
			String ruleRHS="";
			//String nextToken=tokenStack.peek();
			/*ArrayList<String> token = nextToken();
			String nextToken = token.get(0);
			int lineNumber = Integer.parseInt(token.get(1));*/
			nextToken();
			String top = "";
			boolean error = false;
			String derivation = "prog";			
			derivationList.add(derivation);
			while (stack.peek() != "$") {
				
				top = stack.peek();
				System.out.println(derivation);
				System.out.println(top+" -- "+token);
				System.out.println();
				
				if (terminals.contains(top)) {
					
					if (top.equals(token)) {
						
						stack.pop();
						AbstractSyntaxTree tree = new AbstractSyntaxTree();
						tree.makeNode(token, type);
						//tokenStack.pop();
						//nextToken=tokenStack.peek();
						/*token = nextToken();
						nextToken = token.get(0);
						lineNumber = Integer.parseInt(token.get(1));*/	
						nextToken();
					} else {
						
						
						skipErrors(true);
						error = true;
					}
				} else {
					
					if (parseTable[rowHeaders.get(top)][columnHeaders.get(token)] != null) {
						
						
						stack.pop();
						ruleLHS = grammar.get(parseTable[rowHeaders.get(top)][columnHeaders.get(token)]).split("->")[0].trim();
						ruleRHS = grammar.get(parseTable[rowHeaders.get(top)][columnHeaders.get(token)]).split("->")[1].trim();
						
						
						if (!ruleRHS.equals("EPSILON")) {
							
							for (int i = ruleRHS.split("\\s").length-1; i >= 0; i--) {
								
								stack.push(ruleRHS.split("\\s")[i]);
							}
							
							derivation = derivation.replaceFirst(ruleLHS, ruleRHS);
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
			
			if (!token.equals("$") || error == true) {
				
				derivationList.clear();
				DataReadWrite.writeDerivation(derivationList);
				DataReadWrite.writeSyntacticErrors(syntacticErrors);
				System.out.println("Parsing cannot be completed successfully because of errors in the source file. Please check the error file.");
			} else {
				
				DataReadWrite.writeDerivation(derivationList);
				DataReadWrite.writeSyntacticErrors(syntacticErrors);
				System.out.println("Pass");
			}
		} catch (Exception e) {

			e.printStackTrace();
		}		
	}
	
	public static void nextToken() {
		
		//ArrayList<String> token = new ArrayList<>();
		try {
			if (tokenList.get(tokenCounter).type == TokenType.id) {
				
				token = "id";
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				/*token.add("id");
				token.add(Integer.toString(tokens.get(tokenCounter).getLineNumber()));*/
				tokenCounter++;
			} else if(tokenList.get(tokenCounter).type == TokenType.intNum) {
				
				token = "intNum";
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				/*token.add("intNum");
				token.add(Integer.toString(tokens.get(tokenCounter).getLineNumber()));*/
				tokenCounter++;
			} else if(tokenList.get(tokenCounter).type == TokenType.floatNum) {
				
				token = "floatNum";
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				/*token.add("floatNum");
				token.add(Integer.toString(tokens.get(tokenCounter).getLineNumber()));*/
				tokenCounter++;
			} else {

				token = tokenList.get(tokenCounter).getTokenValue();
				lineNumber = tokenList.get(tokenCounter).getLineNumber();
				/*token.add(tokens.get(tokenCounter).getTokenValue());
				token.add(Integer.toString(tokens.get(tokenCounter).getLineNumber()));*/
				tokenCounter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
	}
	
	public static void skipErrors(boolean isTerminal) {
		
		try {
			if (isTerminal) {
				
				syntacticErrors.add("Phase: Syntactic Analyzer. Error in line number "+lineNumber+". ("+token+") might be an unexpected token.");
				if (tokenCounter<tokenList.size()) {
					nextToken();
				} else {
					
					derivationList.clear();
					DataReadWrite.writeDerivation(derivationList);
					DataReadWrite.writeSyntacticErrors(syntacticErrors);
					System.out.println("Parsing cannot be completed successfully because of errors in the source file. Please check the error file.");
					System.exit(0);
				}				
			} else {

				syntacticErrors.add("Phase: Syntactic Analyzer. Error in line number "+lineNumber+". ("+token+") might be an unexpected token.");
				ArrayList<String> firstSet = firstSets.get(stack.peek());
				ArrayList<String> followSet = followSets.get(stack.peek());
				System.out.println(token);
				if (followSet.contains(token) || token.equals("$")) {
					
					stack.pop();
				} else {

/*					while (!firstSet.contains(token) || (DataReadWrite.productionsWithEpsilon().contains(stack.peek()) && !followSet.contains(token))) {
						
						if (tokenCounter<tokenList.size()) {
							nextToken();
							System.out.println(token);
						} else {
							
							System.out.println("as");
							derivationList.clear();
							DataReadWrite.writeDerivation(derivationList);
							DataReadWrite.writeSyntacticErrors(syntacticErrors);
							System.out.println("Parsing cannot be completed successfully because of errors in the source file. Please check the error file.");
							System.exit(0);
						}
					}*/
					
					if (!firstSet.contains(token)) {
						
						if (tokenCounter<tokenList.size()) {
							nextToken();
							System.out.println(token);
						} else {
							
							System.out.println("as");
							derivationList.clear();
							DataReadWrite.writeDerivation(derivationList);
							DataReadWrite.writeSyntacticErrors(syntacticErrors);
							System.out.println("Parsing cannot be completed successfully because of errors in the source file. Please check the error file.");
							System.exit(0);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static ArrayList<String> terminals = new ArrayList<>();
	private static TreeMap<String, Integer> rowHeaders = new TreeMap<>();
	private static TreeMap<String, Integer> columnHeaders = new TreeMap<>();
	private static TreeMap<String, ArrayList<String>> firstSets;
	private static TreeMap<String, ArrayList<String>> followSets;
	private static TreeMap<String, String> grammar;
	private static String[][] parseTable;
	private static List<Token> tokenList = new ArrayList<Token>();
	private static List<String> syntacticErrors = new ArrayList<String>();
	private static List<String> derivationList = new ArrayList<>();
	private static Stack<String> stack = new Stack<>();
	private static int tokenCounter = 0;
	private static String token = "";
	private static int lineNumber = -1;
	private static TokenType type;
}

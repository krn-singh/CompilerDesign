package compiler.syntactic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import compiler.constants.CompilerEnum;
import compiler.helper.DataReadWrite;

public class Parser {

	public static void parser() throws IOException {

		grammar = DataReadWrite.readGrammar();
		// grammar();
		firstSets();
		followSets();
		intializeTable();
		buildTable();
		//printTable();
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
			
			Stack<String> stack = new Stack<>();
			Stack<String> tokenStack = new Stack<>();
			
			tokenStack = DataReadWrite.readTokensInAToCcFormat();
			stack.push("$");
			stack.push(CompilerEnum.START_SYMBOL);
			
			
			String ruleLHS="";
			String ruleRHS="";
			String nextToken=tokenStack.peek();
			String top = "";
			boolean error = false;
			String derivation = "prog";
			List<String> derivationList = new ArrayList<>();
			while (stack.peek() != "$") {
				
				top = stack.peek();
				/*System.out.println(derivation);
				System.out.println(top+" -- "+nextToken);
				System.out.println();
				*/
				if (terminals.contains(top)) {
					
					if (top.equals(nextToken)) {
						
						stack.pop();
						tokenStack.pop();
						nextToken=tokenStack.peek();
						
					} else {
						
						System.out.println("inside terminal error");
						error = true;
						System.exit(0);
					}
				} else {
					
					if (parseTable[rowHeaders.get(top)][columnHeaders.get(nextToken)] != null) {
						
						
						stack.pop();
						ruleLHS = grammar.get(parseTable[rowHeaders.get(top)][columnHeaders.get(nextToken)]).split("->")[0].trim();
						ruleRHS = grammar.get(parseTable[rowHeaders.get(top)][columnHeaders.get(nextToken)]).split("->")[1].trim();
						
						
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
						
						System.out.println("inside nonterminal error");
						error = true;
						System.exit(0);
					}
				}			
			}
			
			if (!nextToken.equals("$") || error == true) {
				
				System.out.println(error+"  "+nextToken);
				System.out.println("Fail");
			} else {
				
				DataReadWrite.writeDerivation(derivationList);
				System.out.println("Pass");
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		
	}

	public static ArrayList<String> terminals = new ArrayList<>();
	public static TreeMap<String, Integer> rowHeaders = new TreeMap<>();
	public static TreeMap<String, Integer> columnHeaders = new TreeMap<>();
	public static TreeMap<String, ArrayList<String>> firstSets;
	public static TreeMap<String, ArrayList<String>> followSets;
	public static TreeMap<String, String> grammar;
	public static String[][] parseTable;
	public static ArrayList<String> tokenList = new ArrayList<>();
}

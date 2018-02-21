package compiler.syntactic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import compiler.helper.DataReadWrite;

public class Parser {

	public static void parser() throws IOException {

		grammar = DataReadWrite.readGrammar();
		// grammar();
		firstSets();
		followSets();
		intializeTable();
		buildTable();
		printTable();
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

					if (!nonterminals.contains(string)) {
						nonterminals.add(string);
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

					if (!nonterminals.contains(string)) {
						nonterminals.add(string);
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

			parseTable = new String[followSets.size()+1][nonterminals.size()+1];
			int rowCounter = 0;
			int columnCounter = 0;
			parseTable[0][0] = "#";

			List<String> rowValues = new ArrayList<>(followSets.keySet());
			for (String string : rowValues) {

				rowCounter++;
				parseTable[rowCounter][0] = string;
				rowHeaders.put(string, rowCounter);
			}

			for (String string : nonterminals) {

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
			while (entrySet.hasNext()) {

				entry = entrySet.next();
				// System.out.println(entry.getKey() + ") " + entry.getValue());
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
				
				if (parseTable[i][j]==null) {
					
					parseTable[i][j] = "0";
				}
				System.out.print(parseTable[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public static void parsing() {
		
		
	}

	public static ArrayList<String> nonterminals = new ArrayList<>();
	public static TreeMap<String, Integer> rowHeaders = new TreeMap<>();
	public static TreeMap<String, Integer> columnHeaders = new TreeMap<>();
	public static TreeMap<String, ArrayList<String>> firstSets;
	public static TreeMap<String, ArrayList<String>> followSets;
	public static TreeMap<String, String> grammar;
	public static String[][] parseTable;
}

package compiler.sematic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import compiler.constants.CompilerEnum.SymTableEntryCategory;
import compiler.helper.DataReadWrite;
import compiler.helper.Util;
import compiler.syntactic.AstNode;
import compiler.syntactic.Parser;

public class Semantic {
	
	private SymTable currentTable;
	private SymTable functionTable;
	private SymTable globalTable;
	private SymTableEntry currentEntry;
	private LinkedList<SymTable> tables = new LinkedList<SymTable>();
	public int tableLevel = 0;
	public static Map<Integer, ArrayList<String>> map;
	private ArrayList<String> recordSymbolTables = new ArrayList<String>();
	
	public void semanticAnalysis() throws IOException {

		System.out.println("Starting Semantic Analysis phase..........");
		System.out.println("Generating Symbol Tables");
		System.out.println("************* Printing Symbol Tables ************");
		System.out.println();
		map = Parser.getMap();
		buildSymbolTables(Parser.getRoot());
		//checkClassInheritance(Parser.getRoot(), globalTable);
		
		for (SymTable symTable : tables) {
			print(symTable);
			System.out.println();
		}
		
		
		
		DataReadWrite.writeSymbolTables(recordSymbolTables);
		DataReadWrite.writeErrors(map);
		System.out.println("##################### END ######################");
		
	}
	
	public void buildSymbolTables(AstNode root) {
		
		if(root.getChildrens().size() == 0) {
			return;
		}
		
		SymTable table = new SymTable();
		Util util = new Util();
		Integer lineNumber;
		
		if (root.getNodeType().equals("prog")) {
			
			globalTable = table.createGlobalTable();
			tables = table.getTables();
		} else if(root.getNodeType().equals("classDecl")) {
			
			table.setTables(tables);
			currentTable = table.createTable(root.getChildrens().get(0).getData(), globalTable);
			tables = table.getTables();
			
			SymTableEntry entry = new SymTableEntry();
			currentEntry=entry.createEntry(root.getChildrens().get(0).getData(), SymTableEntryCategory.Class, currentTable);
			globalTable.addEntry(currentEntry);
		} else if(root.getNodeType().equals("inheritedList")) {
			
			for (int i = 0; i < root.getChildrens().size(); i++) {				
				for (SymTableEntry entry : globalTable.getEntries()) {
					if (entry.getName().equals(root.getChildrens().get(i).getData())) {
						table.setTables(tables);
						currentEntry.addInheritedClass(table.findTable(root.getChildrens().get(i).getData()));
					}
				}
			}
		} else if (root.getNodeType().equals("varDecl")) {

			if (table.checkDeclInSameScope(currentTable, root.getChildrens().get(1).getData())) {
				
				util.setMap(map);
				lineNumber = table.searchRecord(currentTable, root.getChildrens().get(1).getData()).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Identifier ("+root.getChildrens().get(1).getData()+") already declared in line ");
			} else if (table.checkDeclInParentScope(currentTable, tables, root.getChildrens().get(1).getData())) {
				
				util.setMap(map);
				lineNumber = 0;
				map = util.reportError(lineNumber, "Warning: Identifier ("+root.getChildrens().get(1).getData()+") in "+currentTable.getTableName()+" is already declared in Parent Scope");
				
				SymTableEntry entry = new SymTableEntry();
				currentEntry=entry.createEntry(root.getChildrens().get(1).getData(), SymTableEntryCategory.Variable, root.getChildrens().get(0).getData(), root.getChildrens().get(1).getLineNumber());
				currentTable.addEntry(currentEntry);
			} else if (table.checkDeclInSuperClass(globalTable, currentTable, tables, root.getChildrens().get(1).getData())) {
				
				util.setMap(map);
				lineNumber = 0;
				map = util.reportError(lineNumber, "Warning: Identifier ("+root.getChildrens().get(1).getData()+") in "+currentTable.getTableName()+" is already declared in Parent Scope");
				
				SymTableEntry entry = new SymTableEntry();
				currentEntry=entry.createEntry(root.getChildrens().get(1).getData(), SymTableEntryCategory.Variable, root.getChildrens().get(0).getData(), root.getChildrens().get(1).getLineNumber());
				currentTable.addEntry(currentEntry);
			} else {
				
				SymTableEntry entry = new SymTableEntry();
				currentEntry=entry.createEntry(root.getChildrens().get(1).getData(), SymTableEntryCategory.Variable, root.getChildrens().get(0).getData(), root.getChildrens().get(1).getLineNumber());
				currentTable.addEntry(currentEntry);
			}
		} else if (root.getNodeType().equals("funcDecl")) {
			
			table.setTables(tables);
			functionTable = table.createTable(root.getChildrens().get(1).getData(), currentTable);
			tables = table.getTables();
			
			SymTableEntry entry = new SymTableEntry();
			currentEntry=entry.createEntry(root.getChildrens().get(1).getData(), SymTableEntryCategory.Function, root.getChildrens().get(0).getData(), functionTable);
			currentTable.addEntry(currentEntry);
		} else if(root.getNodeType().equals("arraySizeList")) {
			
			if (currentEntry.getArraySizeList().size() == 0) {
				for (int i = 0; i < root.getChildrens().size(); i++) {				
					currentEntry.addArraySize(root.getChildrens().get(i).getData());
				}
			}
			
		} else if(root.getNodeType().equals("fParam")) {
			
			if (!table.checkDeclInSameScope(functionTable, root.getChildrens().get(1).getData())) {
				SymTableEntry entry = new SymTableEntry();
				currentEntry=entry.createEntry(root.getChildrens().get(1).getData(), SymTableEntryCategory.Parameter, root.getChildrens().get(0).getData());
				functionTable.addEntry(currentEntry);
			}
			
		} else if(root.getNodeType().equals("funcDef")) {
			
			if (root.getChildrens().get(1).getNodeType().equals("scopeSpec")) {
				table.setTables(tables);
				if (table.findTable(root.getChildrens().get(2).getData()) == null) {
					
					util.setMap(map);
					lineNumber = root.getChildrens().get(2).getLineNumber();
					map = util.reportError(lineNumber, "Semantic Phase: Undeclared function ("+root.getChildrens().get(2).getData()+") in line ");
					return;
				} else {
					currentTable = table.findTable(root.getChildrens().get(2).getData());
					functionTable = currentTable;
				}				
			} else {

				table.setTables(tables);
				currentTable = table.createTable(root.getChildrens().get(1).getData(), globalTable);
				tables = table.getTables();
				
				SymTableEntry entry = new SymTableEntry();
				currentEntry=entry.createEntry(root.getChildrens().get(1).getData(), SymTableEntryCategory.Function, root.getChildrens().get(0).getData(), currentTable);
				globalTable.addEntry(currentEntry);
			}			
		} else if(root.getNodeType().equals("mainBody")) {
			
			table.setTables(tables);
			currentTable = table.createTable("program", globalTable);
			tables = table.getTables();
			
			SymTableEntry entry = new SymTableEntry();
			currentEntry=entry.createEntry("program", SymTableEntryCategory.Function, currentTable);
			globalTable.addEntry(currentEntry);			
		}
		
		LinkedList<AstNode> childrens = root.getChildrens();
		for (AstNode child : childrens) { 
			buildSymbolTables(child);
		}
	}
	
	public void printTable(SymTable table) {
		
		if(table.getEntries().size() == 0) {

			return;
		}
		
		ArrayList<SymTableEntry> current = table.getEntries();
		String toprint = "";
		for (int i = 0; i < current.size(); i++) {
			
			toprint = String.format("%-20s" , current.get(i).getName());
			toprint += String.format("%-20s" , " | "+current.get(i).getCategory());
			toprint += String.format("%-20s" , (current.get(i).getType() == null ? " | " : " | "+current.get(i).getType()));
			toprint += String.format("%-20s" , (current.get(i).getLink() == null ? " | " : " | "+current.get(i).getLink().getTableName()));
			if (current.get(i).getInheritedClassList().size() == 0) {
				toprint += String.format("%-20s" , " | ");
			} else {
				String classList = "";
				for (SymTable inherited : current.get(i).getInheritedClassList()) {
					classList+=inherited.getTableName()+", ";
				}
				toprint += String.format("%-20s" , " | "+classList.substring(0, classList.length() - 2));
			}
			if (current.get(i).getArraySizeList().size() == 0) {
				toprint += String.format("%-20s" , " | ");
			} else {
				String arraySizeList = "";
				for (String arraySize : current.get(i).getArraySizeList()) {
					arraySizeList+="["+arraySize+"]";
				}
				toprint += String.format("%-20s" , " | "+arraySizeList);
			}
			
			System.out.println(toprint);
			recordSymbolTables.add(toprint);
			toprint = "";
		}		
	}
	
	public void print(SymTable table) {
		System.out.println("=========================================================================================================================");
		recordSymbolTables.add("=========================================================================================================================");
		String toprint = String.format("%-60s", " TableName: "+table.getTableName());
		toprint+=String.format("%60s", (table.getParent() == null ? " " : "Parent: "+table.getParent().getTableName()));
		System.out.println(toprint);
		recordSymbolTables.add(toprint);
		System.out.println("=========================================================================================================================");
		recordSymbolTables.add("=========================================================================================================================");
		System.out.println(" Name                | Category          | Type              | Link (Table Name) | InheritedList     | ArraySizeList     ");
		recordSymbolTables.add(" Name                | Category          | Type              | Link (Table Name) | InheritedList     | ArraySizeList     ");
		System.out.println("=========================================================================================================================");
		recordSymbolTables.add("=========================================================================================================================");
		printTable(table);
		System.out.println("=========================================================================================================================");
		recordSymbolTables.add("=========================================================================================================================");
		for (int i = 0; i < 4; i++) {
			System.out.println();
			recordSymbolTables.add("");
		}
	}
	
}

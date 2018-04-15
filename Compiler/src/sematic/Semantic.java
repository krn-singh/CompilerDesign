/**
 * Package holds the classes that performs the semantic analysis of the input
 */
package sematic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import constants.CompilerEnum.SymTableEntryCategory;
import helper.DataReadWrite;
import helper.Util;
import syntactic.AstNode;
import syntactic.Parser;

/**
 * This main class that controls the semantic analysis phase.
 * Creates the symbol tables, performs type-checking and error reporting
 * @author krn-singh
 */
public class Semantic {
	
	private SymTable currentTable;
	private SymTable functionTable;
	private SymTable globalTable;
	private SymTableEntry currentEntry;
	private AstNode root;
	private static LinkedList<SymTable> tables;
	private static Map<Integer, ArrayList<String>> map;
	private ArrayList<String> recordSymbolTables;
	private ArrayList<String> validDeclTypes;
	private ArrayList<String> funcList;
	private SymTable tableObj = new SymTable();
	private Util util = new Util();

	/**
	 * Getter for tables
	 * 
	 * @return list of tables
	 */
	public static LinkedList<SymTable> getTables() {
		return tables;
	}

	/**
	 * Getter for all the errors up till semantic phase
	 * 
	 * @return collection of errors
	 */
	public static Map<Integer, ArrayList<String>> getMap() {
		return map;
	}

	/**
	 * Initializes the data members of the class and calls the semantic function.
	 * 
	 * @throws IOException handles the I/O related interruptions
	 */
	public void initializeSematicAnalysis() throws IOException {
		// collection of errors
		map = Parser.getMap();
		//Root of AST
		root = Parser.getRoot();
		// list of tables
		tables = new LinkedList<SymTable>();
		// list of symbol table entries in string format for output
		recordSymbolTables = new ArrayList<String>();
		// list of all valid declaration types
		validDeclTypes = new ArrayList<String>();
		validDeclTypes.add("int");
		validDeclTypes.add("float");
		// list of all functions (member function or free function) in the input program
		funcList = new ArrayList<String>();
		semanticAnalysis();
	}
	
	/**
	 * Main function that controls the semantic mechanism.
	 * 
	 * @throws IOException handles the I/O related interruptions
	 */
	public void semanticAnalysis() throws IOException {

		System.out.println("Starting Semantic Analysis phase..........\n");		
		System.out.println();
		phaseOne(root);
		phaseTwo(root);
		calculateMemorySize();
		
		System.out.println("************* Printing Tree after Type Checking ************");
		System.out.println();
		new AstNode().print(Parser.getRoot());
		System.out.println();
		
		System.out.println("Generating Symbol Tables");
		System.out.println();
		System.out.println("************* Printing Symbol Tables ************");
		System.out.println();
		
		for (SymTable symTable : tables) {
			if (!symTable.getEntries().isEmpty()) {
				print(symTable);
				System.out.println();
			}		
		}
		
		DataReadWrite.writeSymbolTables(recordSymbolTables);
		DataReadWrite.writeErrors(map);		
	}
	
	/**
	 * Performs a depth first search of the AST. Creates the symbol tables and
	 * symbol table entries corresponding to the semantic actions of the visited node
	 * 
	 * @param node current node of AST
	 */
	public void phaseOne(AstNode node) {
		
		if(node.getChildrens().size() == 0) {
			return;
		}
		
		SymTableEntry entry = new SymTableEntry();
		Integer lineNumber;
		
		
		
		String key = node.getNodeType();
		
		switch (key) {
		case "prog":
			
			globalTable = tableObj.createGlobalTable();
			tables = tableObj.getTables();
			break;
			
		case "classDecl":
			
			tableObj.setTables(tables);
			currentTable = tableObj.createTable(node.getChildrens().get(0).getData(), globalTable);
			tables = tableObj.getTables();
					
			currentEntry=entry.createEntry(node.getChildrens().get(0).getData(), SymTableEntryCategory.Class, currentTable);
			globalTable.addEntry(currentEntry);
			validDeclTypes.add(node.getChildrens().get(0).getData());
			break;
			
		case "varDecl":
			
			if (tableObj.checkDeclInSameScope(currentTable, node.getChildrens().get(1).getData())) {
				
				util.setMap(map);
				lineNumber = tableObj.searchRecord(currentTable, node.getChildrens().get(1).getData()).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: variable ("+node.getChildrens().get(1).getData()+") is already declared in line ");
			} else if (tableObj.checkDeclInParentScope(currentTable, tables, node.getChildrens().get(1).getData()) || tableObj.checkDeclInSuperClass(globalTable, currentTable, tables, node.getChildrens().get(1).getData())) {
				
				util.setMap(map);
				lineNumber = 0;
				map = util.reportError(lineNumber, "Warning: variable ("+node.getChildrens().get(1).getData()+") in "+currentTable.getTableName()+" is already declared in Parent Scope");
				
				currentEntry=entry.createEntry(node.getChildrens().get(1).getData(), SymTableEntryCategory.Variable, node.getChildrens().get(0).getData(), node.getChildrens().get(1).getLineNumber());
				currentTable.addEntry(currentEntry);
			} else {
				
				currentEntry=entry.createEntry(node.getChildrens().get(1).getData(), SymTableEntryCategory.Variable, node.getChildrens().get(0).getData(), node.getChildrens().get(1).getLineNumber());
				currentTable.addEntry(currentEntry);
			}
			break;
			
		case "forStat":
			
			String varName = node.getChildrens().get(1).getChildrens().get(0).getChildrens().get(0).getData();
			
			if (tableObj.checkDeclInSameScope(currentTable, varName)) {
				
				util.setMap(map);
				lineNumber = tableObj.searchRecord(currentTable, varName).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: variable ("+varName+") is already declared in line ");
			} else if (tableObj.checkDeclInParentScope(currentTable, tables, varName) || tableObj.checkDeclInSuperClass(globalTable, currentTable, tables, varName)) {
				
				util.setMap(map);
				lineNumber = 0;
				map = util.reportError(lineNumber, "Warning: variable ("+varName+") in "+currentTable.getTableName()+" is already declared in Parent Scope");
				
				currentEntry=entry.createEntry(varName, SymTableEntryCategory.Variable, node.getChildrens().get(0).getData(), node.getChildrens().get(1).getChildrens().get(0).getChildrens().get(0).getLineNumber());
				currentTable.addEntry(currentEntry);
			} else {
				
				currentEntry=entry.createEntry(varName, SymTableEntryCategory.Variable, node.getChildrens().get(0).getData(), node.getChildrens().get(1).getChildrens().get(0).getChildrens().get(0).getLineNumber());
				currentTable.addEntry(currentEntry);
			}
			break;
			
		case "funcDecl":
			
			tableObj.setTables(tables);
			functionTable = tableObj.createTable(node.getChildrens().get(1).getData(), currentTable);
			tables = tableObj.getTables();
			
			currentEntry=entry.createEntry(node.getChildrens().get(1).getData(), SymTableEntryCategory.Function, node.getChildrens().get(0).getData(), functionTable);
			currentTable.addEntry(currentEntry);
			funcList.add(node.getChildrens().get(1).getData());
			break;
			
		case "arraySizeList":
			
			if (currentEntry.getArraySizeList().size() == 0) {
				for (int i = 0; i < node.getChildrens().size(); i++) {				
					currentEntry.addArraySize(node.getChildrens().get(i).getData());
				}
			}
			break;
			
		case "fParam":
			
			if (!tableObj.checkDeclInSameScope(functionTable, node.getChildrens().get(1).getData())) {
				
				currentEntry=entry.createEntry(node.getChildrens().get(1).getData(), SymTableEntryCategory.Parameter, node.getChildrens().get(0).getData());
				functionTable.addEntry(currentEntry);
			}
			break;
			
		case "funcDef":
			
			tableObj.setTables(tables);
			
			switch (node.getChildrens().get(1).getNodeType()) {
			case "scopeSpec":
				
				SymTable tempTable = tableObj.findTable(node.getChildrens().get(2).getData());
				int paramCount = node.getChildrens().get(3).getChildrens().size();
				
				if (tempTable == null || !tableObj.validateParamsCount(tempTable, paramCount)) {
					
					util.setMap(map);
					lineNumber = node.getChildrens().get(2).getLineNumber();
					map = util.reportError(lineNumber, "Semantic Phase: Undeclared function ("+node.getChildrens().get(2).getData()+") in line ");
					return;
				} else {
					currentTable = tableObj.findTable(node.getChildrens().get(2).getData());
					functionTable = currentTable;
					// keeping note of functions that are declared but not defined
					if (funcList.contains(node.getChildrens().get(2).getData())) {
						int index = funcList.indexOf(node.getChildrens().get(2).getData());
						funcList.remove(index);
					}
				}
				break;

			default:
				
				currentTable = tableObj.createTable(node.getChildrens().get(1).getData(), globalTable);
				tables = tableObj.getTables();
				
				currentEntry=entry.createEntry(node.getChildrens().get(1).getData(), SymTableEntryCategory.Function, node.getChildrens().get(0).getData(), currentTable);
				globalTable.addEntry(currentEntry);
				functionTable = currentTable;
				break;
			}
			break;
			
		case "mainBody":
			
			tableObj.setTables(tables);
			currentTable = tableObj.createTable("program", globalTable);
			tables = tableObj.getTables();
			
			currentEntry=entry.createEntry("program", SymTableEntryCategory.Function, currentTable);
			globalTable.addEntry(currentEntry);
			break;

		default:
			break;
		}
		
		LinkedList<AstNode> childrens = node.getChildrens();
		for (AstNode child : childrens) { 
			phaseOne(child);
		}
	}
	
	/**
	 * Iterates through the AST and performs the type checking and other semantic checks.
	 * 
	 * @param node current node of AST
	 */
	public void phaseTwo(AstNode node) {
		
		if(node.getChildrens().size() == 0) {
			return;
		}
	
		Integer lineNumber;
		String type = "";
		
		String key = node.getNodeType();
		
		switch (key) {
		case "classDecl":
			
			currentEntry = tableObj.searchRecord(globalTable, node.getChildrens().get(0).getData(), SymTableEntryCategory.Class);		
			
			break;
		
		case "inheritedList":
			
			SymTable table;
			SymTableEntry entry;
			for (int i = 0; i < node.getChildrens().size(); i++) {				
				for (SymTableEntry tempEntry : globalTable.getEntries()) {
					if (tempEntry.getName().equals(node.getChildrens().get(i).getData())) {
						tableObj.setTables(tables);
						// symbol table of the parent class
						table = tableObj.findTable(node.getChildrens().get(i).getData());
						currentEntry.addInheritedClass(table);
						// global entry of the parent class
						entry = tableObj.searchRecord(globalTable, table.getTableName());
						if (entry.getInheritedClassList().size() > 0) {
							for (SymTable tempTable : entry.getInheritedClassList()) {
								currentEntry.addInheritedClass(tempTable);
							}
						}
					}
				}
			}
			break;
			
		case "varDecl":
			
			if (!validDeclTypes.contains(node.getChildrens().get(0).getData())) {
				
				util.setMap(map);
				lineNumber = node.getChildrens().get(1).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Invalid declaration type for variable ("+node.getChildrens().get(1).getData()+") in line ");
				
				tableObj.deleteRecord(currentTable, node.getChildrens().get(1).getData());
			}
			break;
			
		case "funcDef":
			
			tableObj.setTables(tables);
			
			switch (node.getChildrens().get(1).getNodeType()) {
			case "scopeSpec":
				
				currentTable = tableObj.findTable(node.getChildrens().get(2).getData());
				break;

			default:
				
				currentTable = tableObj.findTable(node.getChildrens().get(1).getData());
				break;
			}			
			break;

		case "mainBody":
			
			tableObj.setTables(tables);
			currentTable = tableObj.findTable("program");			
			break;
			
		case "assignStat":
			
			String leftOperandType = varNode(currentTable, node.getChildrens().get(0));
			String rightOperandType = exprNode(currentTable, node.getChildrens().get(1));
			
			if (!leftOperandType.isEmpty() && !rightOperandType.isEmpty() && !leftOperandType.equals(rightOperandType)) {
				
				util.setMap(map);
				lineNumber = node.getChildrens().get(0).getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Type mismatch in line ");
				node.setType("typeerror");
			} else {
				node.setType(leftOperandType);
			}
			break;
			
		case "returnStat":			
			
			switch (node.getChildrens().get(0).getNodeType()) {
			case "var":
				type	= varNode(currentTable, node.getChildrens().get(0));
				break;

			case "numNode":
				type	= node.getChildrens().get(0).getType();
				break;
				
			default:
				break;
			}
			
			String funcReturnType = tableObj.searchRecord(currentTable.getParent(), currentTable.getTableName(), SymTableEntryCategory.Function).getType();
			if (!type.isEmpty() && !type.equals(funcReturnType)) {
				
				util.setMap(map);
				lineNumber = node.getChildrens().get(0).getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Invalid return type in line ");
				node.setType("typeerror");
			} else {
				node.setType(type);
			}
			break;
			
		case "putStat":
			
			switch (node.getChildrens().get(0).getNodeType()) {
			case "var":
				type	= varNode(currentTable, node.getChildrens().get(0));
				break;

			case "numNode":
				type	= node.getChildrens().get(0).getType();
				break;
				
			default:
				break;
			}
			break;
			
		case "getStat":
			
			type = varNode(currentTable, node.getChildrens().get(0));
			break;
			
		case "ifStat":
			
			type = relOpNode(currentTable, node.getChildrens().get(0));
			if (type.equals("typeerror")) {
				util.setMap(map);
				lineNumber = node.getChildrens().get(0).getChildrens().get(0).getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Type mismatch in line ");
			}
			break;
			
		case "forStat":
			
			type = relOpNode(currentTable, node.getChildrens().get(2));
			if (type.equals("typeerror")) {
				util.setMap(map);
				lineNumber = node.getChildrens().get(1).getChildrens().get(0).getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Type mismatch in line ");
			}
			break;
				
		default:
			break;
		}
		
		LinkedList<AstNode> childrens = node.getChildrens();
		for (AstNode child : childrens) { 
			phaseTwo(child);
		}
	}
	
	/**
	 * Evaluates the type(int or float) of the variable
	 * 
	 * @param table Symbol table for the variable
	 * @param node current node
	 * @return type(int or float) of the variable
	 */
	public String varNode(SymTable table, AstNode node) {
		
		String type = "";
		boolean isFreeFunc = false;
		boolean isArray = false;
		boolean isClassMember = false;
		
		if (node.getChildrens().get(1).getChildrens().size() > 0) {
			
			isArray = true;
			if (node.getChildrens().get(1).getChildrens().get(0).getNodeType().equals("aParamList"))  { isFreeFunc = true; 	}
			else if (node.getChildrens().get(1).getChildrens().get(0).getNodeType().equals("idNode")) { isClassMember = true; }
		}
	
		if (isFreeFunc) 		  	{	type = isFreeFunction(table, node);	}
		else if(isClassMember)   { 	type = isClassMember(table, node);	}
		else if(isArray) 		{	type = isArray(table, node);			} 
		else 					{   type = isLocalVariable(table, node);	}
		
		return type;
	}
	
	/**
	 * Evaluates the type(int or float) of the expression
	 * 
	 * @param table Symbol table for the variable in the expression
	 * @param node current node
	 * @return type(int or float) of the expression
	 */
	public String exprNode(SymTable table, AstNode node) {
		
		String type = "";
		
		switch (node.getChildrens().get(0).getNodeType()) {
		case "numNode":
			
			type = node.getChildrens().get(0).getType();
			node.setType(type);
			break;

		case "addOp":
			
			type = addOpNode(table, node.getChildrens().get(0));
			node.setType(type);
			break;
			
		case "multOp":
			
			type = multOpNode(table, node.getChildrens().get(0));
			node.setType(type);
			break;
			
		case "var":
			
			type = varNode(table, node.getChildrens().get(0));
			node.setType(type);
			break;
			
		case "relOp":
			
			type = relOpNode(table, node.getChildrens().get(0));
			node.setType(type);
			break;
			
		default:
			break;
		}
				
		return type;
	}
	
	/**
	 * Evaluates the type(int or float) of the left and right side of the operator.
	 * Reports error in case of type mismatch
	 * 
	 * @param table Symbol table for the variable in the expression
	 * @param node current node
	 * @return type(int or float) of the expression
	 */
	public String addOpNode(SymTable table, AstNode node) {
		
		String type = "";
		String leftOperandType = "";
		String rightOperandType = "";
		
		switch (node.getChildrens().get(0).getNodeType()) {
		case "numNode":
			
			leftOperandType = node.getChildrens().get(0).getType();
			break;

		case "multOp":
			
			leftOperandType = multOpNode(table, node.getChildrens().get(0));
			break;
			
		case "addOp":
			
			leftOperandType = addOpNode(table, node.getChildrens().get(0));
			break;
			
		case "var":
			
			leftOperandType = varNode(table, node.getChildrens().get(0));
			break;
			
		default:
			break;
		}
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			
			rightOperandType = node.getChildrens().get(2).getType();
			break;

		case "addOp":
			
			rightOperandType = addOpNode(table, node.getChildrens().get(2));
			break;
			
		case "multOp":
			
			rightOperandType = multOpNode(table, node.getChildrens().get(2));
			break;
			
		case "var":
			
			rightOperandType = varNode(table, node.getChildrens().get(2));
			break;
			
		default:
			break;
		}
		
		if (!leftOperandType.isEmpty() && !rightOperandType.isEmpty() && !leftOperandType.equals(rightOperandType)) {
			
			type = "typeerror";
			node.setType(type);
		} else {
			type = leftOperandType;
			node.setType(type);
		}
		
		return type;
	}
	
	/**
	 * Evaluates the type(int or float) of the left and right side of the operator.
	 * Reports error in case of type mismatch
	 * 
	 * @param table Symbol table for the variable in the expression
	 * @param node current node
	 * @return type(int or float) of the expression
	 */
	public String multOpNode(SymTable table, AstNode node) {
		
		String type = "";
		String leftOperandType = "";
		String rightOperandType = "";
		
		switch (node.getChildrens().get(0).getNodeType()) {
		case "numNode":
			
			leftOperandType = node.getChildrens().get(0).getType();
			break;

		case "multOp":
			
			leftOperandType = multOpNode(table, node.getChildrens().get(0));
			break;
			
		case "addOp":
			
			leftOperandType = addOpNode(table, node.getChildrens().get(0));
			break;
			
		case "var":
			
			leftOperandType = varNode(table, node.getChildrens().get(0));
			break;
			
		default:
			break;
		}
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			
			rightOperandType = node.getChildrens().get(2).getType();
			break;

		case "addOp":
			
			rightOperandType = addOpNode(table, node.getChildrens().get(2));
			break;
			
		case "multOp":
			
			rightOperandType = multOpNode(table, node.getChildrens().get(2));
			break;
			
		case "var":
			
			rightOperandType = varNode(table, node.getChildrens().get(2));
			break;
			
		default:
			break;
		}
		
		if (!leftOperandType.isEmpty() && !rightOperandType.isEmpty() && !leftOperandType.equals(rightOperandType)) {
			
			type = "typeerror";
			node.setType(type);
		} else {
			type = leftOperandType;
			node.setType(type);
		}
		
		return type;
	}
	
	/**
	 * Evaluates the type(int or float) of the left and right side of the operator.
	 * Reports error in case of type mismatch
	 * 
	 * @param table Symbol table for the variable in the expression
	 * @param node current node
	 * @return type(int or float) of the expression
	 */
	public String relOpNode(SymTable table, AstNode node) {
		
		String type = "";
		String leftOperandType = "";
		String rightOperandType = "";
		
		switch (node.getChildrens().get(0).getNodeType()) {
		case "numNode":
			
			leftOperandType = node.getChildrens().get(0).getType();
			break;

		case "multOp":
			
			leftOperandType = multOpNode(table, node.getChildrens().get(0));
			break;
			
		case "addOp":
			
			leftOperandType = addOpNode(table, node.getChildrens().get(0));
			break;
			
		case "var":
			
			leftOperandType = varNode(table, node.getChildrens().get(0));
			break;
			
		default:
			break;
		}
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			
			rightOperandType = node.getChildrens().get(2).getType();
			break;

		case "addOp":
			
			rightOperandType = addOpNode(table, node.getChildrens().get(2));
			break;
			
		case "multOp":
			
			rightOperandType = multOpNode(table, node.getChildrens().get(2));
			break;
			
		case "var":
			
			rightOperandType = varNode(table, node.getChildrens().get(2));
			break;
			
		default:
			break;
		}
		
		if (!leftOperandType.isEmpty() && !rightOperandType.isEmpty() && !leftOperandType.equals(rightOperandType)) {
			
			type = "typeerror";
			node.setType(type);
		} else {
			type = leftOperandType;
			node.setType(type);
		}
		
		return type;
	}
	
	/**
	 * Validates the parameters of the function call
	 * 
	 * @param table Symbol table for the function from which another function is called.
	 * @param funcReferenceTble Symbol table for the function
	 * @param node current node
	 * @return true if valid parameters else false
	 */
	public boolean aParamListNode(SymTable table, SymTable funcReferenceTble, AstNode node) {
		
		String type = "";
		
		for (int i = 0; i < node.getChildrens().size(); i++) {
			
			switch (node.getChildrens().get(i).getNodeType()) {
			case "numNode":
				
				type = node.getChildrens().get(i).getType();
				if (type.isEmpty() || !tableObj.validateParams(funcReferenceTble, i, type)) {
					return false;
				}
				break;
				
			case "var":
				
				type = varNode(table, node.getChildrens().get(i));
				if (type.isEmpty() || !tableObj.validateParams(funcReferenceTble, i, type)) {
					return false;
				}
				break;
				
			default:
				break;
			}			
		}
		
		return true;
	}
	
	/**
	 * Validates the number of elements of the array
	 * 
	 * @param table Symbol table for the array variable
	 * @param node current node
	 * @return true if valid number of array elements else false
	 */
	public boolean arraySizeListNode(SymTable table, AstNode node) {
		
		String type = "";
		
		for (int i = 0; i < node.getChildrens().size(); i++) {
			
			switch (node.getChildrens().get(i).getNodeType()) {
			case "numNode":
				
				type = node.getChildrens().get(i).getType();
				if (type.isEmpty() || !type.equals("int")) {
					return false;
				}
				break;
				
			case "var":
				
				type = varNode(table, node.getChildrens().get(i));
				if (type.isEmpty() || !type.equals("int")) {
					return false;
				}
				break;
				
			case "addOp":
				
				type = addOpNode(table, node.getChildrens().get(i));
				if (type.isEmpty() || !type.equals("int")) {
					return false;
				}
				break;
				
			case "multOp":
				
				type = multOpNode(table, node.getChildrens().get(i));
				if (type.isEmpty() || !type.equals("int")) {
					return false;
				}
				break;
				
			default:
				break;
			}			
		}
		
		return true;
	}
	
	/**
	 * Validates the data members for the class
	 * 
	 * @param table Symbol table for the class
	 * @param programTable Symbol table for program function
	 * @param node current node
	 * @return type(int or float) of the class member
	 */
	public String validateIndexListNode(SymTable table, SymTable programTable, AstNode node) {
		
		String type = "";
		String memberName = node.getChildrens().get(0).getData();
		
		if (!tableObj.checkDeclInSameScope(table, memberName) && !tableObj.checkDeclInSuperClass(globalTable, table, tables, memberName)) {
			
			util.setMap(map);
			Integer lineNumber = node.getChildrens().get(0).getLineNumber();
			map = util.reportError(lineNumber, "Semantic Phase: Undeclared data member ("+node.getChildrens().get(0).getData()+") in line ");
		} else {

			// find the symbol table of class(base or the parent class) in which the data member resides
			SymTable classTable = tableObj.searchInSuperClass(globalTable, table, tables, memberName);
			
			if (node.getChildrens().size() > 1) {
				
				type = classMembers(true, node, classTable, programTable);
				return type;
			} else {
				
				type = classMembers(false, node, classTable, programTable);
				return type;
			}		
		}
		
		return type;
	}
	
	/**
	 * Validates the member functions
	 * 
	 * @param isMemberFunction true if the accessible class member is a function
	 * @param node current node
	 * @param table Symbol table for the class
	 * @param programTable Symbol table for program function
	 * @return type(int or float) of the class member function
	 */
	public String classMembers(boolean isMemberFunction, AstNode node, SymTable table, SymTable programTable) {
		
		String type = "";
		
		if (isMemberFunction) {
			
			tableObj.setTables(tables);
			SymTable funcReferenceTble = tableObj.findTable(node.getChildrens().get(0).getData());
			int paramCount = node.getChildrens().get(1).getChildrens().size();
			
			// throwing error if function is declared but not defined
			if (funcList.contains(funcReferenceTble.getTableName())) {
				
				util.setMap(map);
				Integer lineNumber = node.getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Undefined function ("+node.getChildrens().get(0).getData()+") in line ");
			}
			
			if (!tableObj.validateParamsCount(funcReferenceTble, paramCount)) {
				
				util.setMap(map);
				Integer lineNumber = node.getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Function ("+node.getChildrens().get(0).getData()+") with invalid parameters in line ");
			} else {

				if (aParamListNode(programTable, funcReferenceTble, node.getChildrens().get(1))) {
					
					type = tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType();
				} else {
					
					util.setMap(map);
					Integer lineNumber = node.getChildrens().get(0).getLineNumber();
					map = util.reportError(lineNumber, "Semantic Phase: Function ("+node.getChildrens().get(0).getData()+") with invalid parameters in line ");
				}
			}
		} else {
			type = tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType();
		}
		
		return type;
	}
	
	/**
	 * Returns the type of valid free functions
	 * 
	 * @param table Symbol table for the program function
	 * @param node current node
	 * @return type(int or float) of the free function
	 */
	public String isFreeFunction(SymTable table, AstNode node) {
		
		String type = "";
		tableObj.setTables(tables);
		
		if (tableObj.searchRecord(globalTable, node.getChildrens().get(0).getData()) == null) {
			
			util.setMap(map);
			Integer lineNumber = node.getChildrens().get(0).getLineNumber();
			map = util.reportError(lineNumber, "Semantic Phase: Undeclared function ("+node.getChildrens().get(0).getData()+") in line ");
		} else {

			
			SymTable funcReferenceTble = tableObj.findTable(node.getChildrens().get(0).getData());
			int paramCount = node.getChildrens().get(1).getChildrens().get(0).getChildrens().size();
			
			if (!tableObj.validateParamsCount(funcReferenceTble, paramCount)) {
				
				util.setMap(map);
				Integer lineNumber = node.getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Function ("+node.getChildrens().get(0).getData()+") with invalid parameters in line ");
			} else {

				if (aParamListNode(table, funcReferenceTble, node.getChildrens().get(1).getChildrens().get(0))) {
					
					type = tableObj.searchRecord(globalTable, node.getChildrens().get(0).getData()).getType();
					node.setType(type);
					return type;
				} else {
					
					util.setMap(map);
					Integer lineNumber = node.getChildrens().get(0).getLineNumber();
					map = util.reportError(lineNumber, "Semantic Phase: Function ("+node.getChildrens().get(0).getData()+") with invalid parameters in line ");
				}
			}				
		}
		
		return type;
	}
	
	/**
	 * Returns the type of valid class member
	 * 
	 * @param table Symbol table for the program function
	 * @param node current node
	 * @return type(int or float) of the class member
	 */
	public String isClassMember(SymTable table, AstNode node) {
		
		String type = "";
		tableObj.setTables(tables);
		
		if (tableObj.searchRecord(table, node.getChildrens().get(0).getData()) == null) {
			
			util.setMap(map);
			Integer lineNumber = node.getChildrens().get(0).getLineNumber();
			map = util.reportError(lineNumber, "Semantic Phase: Undeclared variable ("+node.getChildrens().get(0).getData()+") in line ");
		} else if(tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType().equals("int") || tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType().equals("float")) {
			
			util.setMap(map);
			Integer lineNumber = node.getChildrens().get(0).getLineNumber();
			map = util.reportError(lineNumber, "Semantic Phase: Invalid class type of variable ("+node.getChildrens().get(0).getData()+") in line ");
		} else {

			// finding the symbol table for the class to which the object belongs
			SymTable classTable = tableObj.findTable(tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType());
			if (tableObj.findTable(tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType()) == null) {
				return type;
			}
			type = validateIndexListNode(classTable, table, node.getChildrens().get(1));
			node.setType(type);
			return type;
		}
		
		return type;
	}
	
	/**
	 * Evaluates the type(int or float) of the array
	 * 
	 * @param table Symbol table for the array
	 * @param node current node
	 * @return type(int or float) of the array
	 */
	public String isArray(SymTable table, AstNode node) {
		
		String type = "";
		tableObj.setTables(tables);
		
		if (tableObj.searchRecord(table, node.getChildrens().get(0).getData()) == null) {
			
			util.setMap(map);
			Integer lineNumber = node.getChildrens().get(0).getLineNumber();
			map = util.reportError(lineNumber, "Semantic Phase: Undeclared array ("+node.getChildrens().get(0).getData()+") in line ");
		} else {

			SymTableEntry entry = tableObj.searchRecord(table, node.getChildrens().get(0).getData());
			if (entry.getArraySizeList().size() != node.getChildrens().get(1).getChildrens().size()) {
				
				util.setMap(map);
				Integer lineNumber = node.getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Array ("+node.getChildrens().get(0).getData()+") with invalid dimensions in line ");
			} else {

				if (arraySizeListNode(table, node.getChildrens().get(1))) {
					
					type = tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType();
					node.setType(type);
					return type;
				} else {
					
					util.setMap(map);
					Integer lineNumber = node.getChildrens().get(0).getLineNumber();
					map = util.reportError(lineNumber, "Semantic Phase: Array ("+node.getChildrens().get(0).getData()+") with invalid dimensions in line ");
				}
			}
		}
		
		return type;
	}
	
	/**
	 * Evaluates the type(int or float) of the variable
	 * 
	 * @param table Symbol table for the variable
	 * @param node current node
	 * @return type(int or float) of the variable
	 */
	public String isLocalVariable(SymTable table, AstNode node) {
		
		String type = "";
		tableObj.setTables(tables);
		
		if (tableObj.searchRecord(table, node.getChildrens().get(0).getData()) == null) {
			
			util.setMap(map);
			Integer lineNumber = node.getChildrens().get(0).getLineNumber();
			map = util.reportError(lineNumber, "Semantic Phase: Undeclared variable ("+node.getChildrens().get(0).getData()+") in line ");
		} else {
			
			type = tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType();
			node.setType(type);
			return type;
		}
		
		return type;
	}
	
	/**
	 * Calculates the memory size of the variables in the symbol table
	 */
	public void calculateMemorySize() {
		
		int size;
		int offset;
		int cellCount = 1;
		String type = "";
		
		for (SymTableEntry globalEntry : globalTable.getEntries()) {
			
			SymTable current = tableObj.findTable(globalEntry.getName());
			offset = 0;
			
			for (SymTableEntry localEntry : current.getEntries()) {
				
				type = localEntry.getType();
				if (localEntry.getCategory() == SymTableEntryCategory.Variable || localEntry.getCategory() == SymTableEntryCategory.Parameter) {
					
					if (localEntry.getArraySizeList().size() > 0) {
						cellCount = calculateMemoryCells(localEntry, type);
					}
					
					size = cellCount*variableSize(type);
					cellCount = 1;
					localEntry.setSize(Integer.toString(size));
					offset+=size;
					localEntry.setOffset("-"+Integer.toString(offset));
				} else if (localEntry.getCategory() == SymTableEntryCategory.Function) {
					// control enters this loop if the entry in a symbol table is a function
					SymTable membFunc = tableObj.findTable(localEntry.getName());
					int tempOffset = 0;
					
					for (SymTableEntry tempLocalEntry : membFunc.getEntries()) {
						
						type = tempLocalEntry.getType();
						if (tempLocalEntry.getCategory() == SymTableEntryCategory.Variable || tempLocalEntry.getCategory() == SymTableEntryCategory.Parameter) {
							
							if (tempLocalEntry.getArraySizeList().size() > 0) {
								cellCount = calculateMemoryCells(tempLocalEntry, type);
							}
							
							size = cellCount*variableSize(type);
							cellCount = 1;
							tempLocalEntry.setSize(Integer.toString(size));
							tempOffset+=size;
							tempLocalEntry.setOffset("-"+Integer.toString(tempOffset));
						}
					}
					
					membFunc.setTableSize(tempOffset);
				}
			}
			
			globalEntry.setSize(Integer.toString(offset));
			current.setTableSize(offset);
		}
	}
	
	/**
	 * Calculates the memory cells of the arrays in the symbol table
	 * 
	 * @param entry Symbol table entry
	 * @param type Variable type
	 * @return
	 */
	public int calculateMemoryCells(SymTableEntry entry, String type) {
		
		int cellCount = 1;
		ArrayList<String> arrayList = entry.getArraySizeList();
		
		for (int i = 0; i < arrayList.size(); i++) {
			cellCount = cellCount*Integer.parseInt(arrayList.get(i));
		}
		
		return cellCount;
	}
	
	/**
	 * Evaluates the variable size
	 * 
	 * @param type Type of the variable
	 * @return size of int, float or class type
	 */
	public int variableSize(String type) {
		
		int size = 0; 
		
		switch (type) {
		case "int":
			size = 4;
			break;
			
		case "float":
			size = 8;
			break;

		default:
			SymTableEntry globalEntry = tableObj.searchRecord(globalTable, type);
			if (globalEntry != null) {	
				// memory size of the class object
				size = Integer.parseInt(globalEntry.getSize());
				if (globalEntry.getInheritedClassList().size() > 0) {
					// computes the size of parent classes
					SymTableEntry localEntry;
					for (SymTable symTable : globalEntry.getInheritedClassList()) {
						localEntry = tableObj.searchRecord(globalTable, symTable.getTableName());
						size += Integer.parseInt(localEntry.getSize());
					}
				}
			}			
			break;
		}
		
		return size;		
	}
	
	/**
	 * Prints the symbol table
	 * 
	 * @param table Symbol table
	 */
	public void printTable(SymTable table) {
		
		if(table.getEntries().size() == 0) {	return;	}
		
		ArrayList<SymTableEntry> current = table.getEntries();
		String toprint = "";
		for (int i = 0; i < current.size(); i++) {
			
			toprint = String.format("%-16s" , current.get(i).getName());
			toprint += String.format("%-16s" , " | "+current.get(i).getCategory());
			toprint += String.format("%-16s" , (current.get(i).getType() == null ? " | " : " | "+current.get(i).getType()));
			toprint += String.format("%-16s" , (current.get(i).getLink() == null ? " | " : " | "+current.get(i).getLink().getTableName()));
			if (current.get(i).getInheritedClassList().size() == 0) {
				toprint += String.format("%-16s" , " | ");
			} else {
				String classList = "";
				for (SymTable inherited : current.get(i).getInheritedClassList()) {
					classList+=inherited.getTableName()+", ";
				}
				toprint += String.format("%-16s" , " | "+classList.substring(0, classList.length() - 2));
			}
			if (current.get(i).getArraySizeList().size() == 0) {
				toprint += String.format("%-16s" , " | ");
			} else {
				String arraySizeList = "";
				for (String arraySize : current.get(i).getArraySizeList()) {
					arraySizeList+="["+arraySize+"]";
				}
				toprint += String.format("%-16s" , " | "+arraySizeList);
			}
			toprint += String.format("%-16s" , (current.get(i).getSize() == null ? " | " : " | "+current.get(i).getSize()));
			toprint += String.format("%-16s" , (current.get(i).getOffset() == null ? " | " : " | "+current.get(i).getOffset()));
			
			System.out.println(toprint);
			recordSymbolTables.add(toprint);
			toprint = "";
		}		
	}
	
	/**
	 * Prints the symbol table
	 * 
	 * @param table Symbol table
	 */
	public void print(SymTable table) {
		System.out.println("==================================================================================================================================");
		recordSymbolTables.add("==================================================================================================================================");
		String toprint = String.format("%-60s", " TableName: "+table.getTableName());
		toprint+=String.format("%60s", (table.getParent() == null ? " " : "Parent: "+table.getParent().getTableName()));
		System.out.println(toprint);
		recordSymbolTables.add(toprint);
		System.out.println("==================================================================================================================================");
		recordSymbolTables.add("==================================================================================================================================");
		System.out.println(" Name            | Category      | Type          | Link          | InheritedList | ArraySizeList | Memory Size   | Offset        ");
		recordSymbolTables.add(" Name            | Category      | Type          | Link          | InheritedList | ArraySizeList | Memory Size   | Offset        ");
		System.out.println("==================================================================================================================================");
		recordSymbolTables.add("==================================================================================================================================");
		printTable(table);
		System.out.println("==================================================================================================================================");
		recordSymbolTables.add("==================================================================================================================================");
		for (int i = 0; i < 4; i++) {
			System.out.println();
			recordSymbolTables.add("");
		}
	}
}

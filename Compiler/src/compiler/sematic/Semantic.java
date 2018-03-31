package compiler.sematic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import compiler.constants.CompilerEnum.SymTableEntryCategory;
import compiler.constants.CompilerEnum.TokenType;
import compiler.helper.DataReadWrite;
import compiler.helper.Util;
import compiler.syntactic.AstNode;
import compiler.syntactic.Parser;

public class Semantic {
	
	private SymTable currentTable;
	private SymTable functionTable;
	private SymTable globalTable;
	private SymTableEntry currentEntry;
	private AstNode root;
	private LinkedList<SymTable> tables = new LinkedList<SymTable>();
	public int tableLevel = 0;
	public static Map<Integer, ArrayList<String>> map;
	private ArrayList<String> recordSymbolTables = new ArrayList<String>();
	private ArrayList<String> validDeclTypes;
	private ArrayList<String> funcList;
	
	public void initialize() {
		
		validDeclTypes = new ArrayList<String>();
		validDeclTypes.add("int");
		validDeclTypes.add("float");
		funcList = new ArrayList<String>();
	}
	
	public void semanticAnalysis() throws IOException {

		System.out.println("Starting Semantic Analysis phase..........");		
		
		map = Parser.getMap();
		root = Parser.getRoot();
		initialize();
		phaseOne(root);
		validateInheritance(root);
		phaseTwo(root);
		
		System.out.println("************* Printing Tree after Type Checking ************");
		System.out.println();
		new AstNode().print(Parser.getRoot());
		System.out.println();
		
		System.out.println("Generating Symbol Tables");
		System.out.println();
		System.out.println("************* Printing Symbol Tables ************");
		System.out.println();
		
		for (SymTable symTable : tables) {
			print(symTable);
			System.out.println();
		}		
		
		DataReadWrite.writeSymbolTables(recordSymbolTables);
		DataReadWrite.writeErrors(map);
		System.out.println("##################### END ######################");
		
		
		
	}
	
	public void phaseOne(AstNode node) {
		
		if(node.getChildrens().size() == 0) {
			return;
		}
		
		SymTable table = new SymTable();
		Util util = new Util();
		SymTableEntry entry = new SymTableEntry();
		Integer lineNumber;
		
		
		
		String key = node.getNodeType();
		
		switch (key) {
		case "prog":
			
			globalTable = table.createGlobalTable();
			tables = table.getTables();
			break;
			
		case "classDecl":
			
			table.setTables(tables);
			currentTable = table.createTable(node.getChildrens().get(0).getData(), globalTable);
			tables = table.getTables();
					
			currentEntry=entry.createEntry(node.getChildrens().get(0).getData(), SymTableEntryCategory.Class, currentTable);
			globalTable.addEntry(currentEntry);
			validDeclTypes.add(node.getChildrens().get(0).getData());
			break;
			
		case "varDecl":
			
			if (table.checkDeclInSameScope(currentTable, node.getChildrens().get(1).getData())) {
				
				util.setMap(map);
				lineNumber = table.searchRecord(currentTable, node.getChildrens().get(1).getData()).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: variable ("+node.getChildrens().get(1).getData()+") is already declared in line ");
			} else if (table.checkDeclInParentScope(currentTable, tables, node.getChildrens().get(1).getData()) || table.checkDeclInSuperClass(globalTable, currentTable, tables, node.getChildrens().get(1).getData())) {
				
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
			
			if (table.checkDeclInSameScope(currentTable, varName)) {
				
				util.setMap(map);
				lineNumber = table.searchRecord(currentTable, varName).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: variable ("+varName+") is already declared in line ");
			} else if (table.checkDeclInParentScope(currentTable, tables, varName) || table.checkDeclInSuperClass(globalTable, currentTable, tables, varName)) {
				
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
			
			table.setTables(tables);
			functionTable = table.createTable(node.getChildrens().get(1).getData(), currentTable);
			tables = table.getTables();
			
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
			
			if (!table.checkDeclInSameScope(functionTable, node.getChildrens().get(1).getData())) {
				
				currentEntry=entry.createEntry(node.getChildrens().get(1).getData(), SymTableEntryCategory.Parameter, node.getChildrens().get(0).getData());
				functionTable.addEntry(currentEntry);
			}
			break;
			
		case "funcDef":
			
			table.setTables(tables);
			
			switch (node.getChildrens().get(1).getNodeType()) {
			case "scopeSpec":
				
				SymTable tempTable = table.findTable(node.getChildrens().get(2).getData());
				int paramCount = node.getChildrens().get(3).getChildrens().size();
				
				if (tempTable == null || !table.validateParamsCount(tempTable, paramCount)) {
					
					util.setMap(map);
					lineNumber = node.getChildrens().get(2).getLineNumber();
					map = util.reportError(lineNumber, "Semantic Phase: Undeclared function ("+node.getChildrens().get(2).getData()+") in line ");
					return;
				} else {
					currentTable = table.findTable(node.getChildrens().get(2).getData());
					functionTable = currentTable;
					// keeping note of functions that are declared but not defined
					if (funcList.contains(node.getChildrens().get(2).getData())) {
						int index = funcList.indexOf(node.getChildrens().get(2).getData());
						funcList.remove(index);
					}
				}
				break;

			default:
				
				currentTable = table.createTable(node.getChildrens().get(1).getData(), globalTable);
				tables = table.getTables();
				
				currentEntry=entry.createEntry(node.getChildrens().get(1).getData(), SymTableEntryCategory.Function, node.getChildrens().get(0).getData(), currentTable);
				globalTable.addEntry(currentEntry);
				functionTable = currentTable;
				break;
			}
			break;
			
		case "mainBody":
			
			table.setTables(tables);
			currentTable = table.createTable("program", globalTable);
			tables = table.getTables();
			
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
	
	public void phaseTwo(AstNode node) {
		
		if(node.getChildrens().size() == 0) {
			return;
		}
	
		SymTable table = new SymTable();
		Util util = new Util();
		SymTableEntry entry = new SymTableEntry();
		Integer lineNumber;
		String type = "";
		
		String key = node.getNodeType();
		
		switch (key) {
		case "varDecl":
			
			if (!validDeclTypes.contains(node.getChildrens().get(0).getData())) {
				
				util.setMap(map);
				lineNumber = node.getChildrens().get(1).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Invalid declaration type for variable ("+node.getChildrens().get(1).getData()+") in line ");
			}
			break;
			
		case "funcDef":
			
			table.setTables(tables);
			
			switch (node.getChildrens().get(1).getNodeType()) {
			case "scopeSpec":
				
				currentTable = table.findTable(node.getChildrens().get(2).getData());
				break;

			default:
				
				currentTable = table.findTable(node.getChildrens().get(1).getData());
				break;
			}			
			break;

		case "mainBody":
			
			table.setTables(tables);
			currentTable = table.findTable("program");			
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
			
			String funcReturnType = table.searchRecord(currentTable.getParent(), currentTable.getTableName(), SymTableEntryCategory.Function).getType();
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
	
	public String varNode(SymTable table, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
		String type = "";
		boolean isFreeFunc = false;
		boolean isArray = false;
		boolean isClassMember = false;
		
		if (node.getChildrens().get(1).getChildrens().size() > 0) {
			
			isArray = true;
			if (node.getChildrens().get(1).getChildrens().get(0).getNodeType().equals("aParamList")) { isFreeFunc = true; }
			else if (node.getChildrens().get(1).getChildrens().get(0).getNodeType().equals("idNode")) { isClassMember = true; }
		}
		
		tableObj.setTables(tables);
		
		if (isFreeFunc) {

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
		} else if(isClassMember) { 
			
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
		} else if(isArray) {
			
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
		} else {

			if (tableObj.searchRecord(table, node.getChildrens().get(0).getData()) == null) {
				
				util.setMap(map);
				Integer lineNumber = node.getChildrens().get(0).getLineNumber();
				map = util.reportError(lineNumber, "Semantic Phase: Undeclared variable ("+node.getChildrens().get(0).getData()+") in line ");
			} else {
				
				type = tableObj.searchRecord(table, node.getChildrens().get(0).getData()).getType();
				node.setType(type);
				return type;
			}
		}
		
		return type;
	}
	
	public String exprNode(SymTable table, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
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
	
	public String addOpNode(SymTable table, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
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
	
	public String multOpNode(SymTable table, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
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
	
	public String relOpNode(SymTable table, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
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
	
	public boolean aParamListNode(SymTable table, SymTable funcReferenceTble, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
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
	
	public boolean arraySizeListNode(SymTable table, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
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
				
			default:
				break;
			}			
		}
		
		return true;
	}
	
	public String validateIndexListNode(SymTable table, SymTable programTable, AstNode node) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
		String type = "";
		String memberName = node.getChildrens().get(0).getData();
		
		System.out.println(table.getTableName()+"--"+memberName);
		
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
	
	public String classMembers(boolean isMemberFunction, AstNode node, SymTable table, SymTable programTable) {
		
		SymTable tableObj = new SymTable();
		Util util = new Util();
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
	
	public void validateInheritance(AstNode node) {
		
		if(node.getChildrens().size() == 0) {
			return;
		}
		
		SymTable table = new SymTable();
		SymTableEntry entry = new SymTableEntry();
		String key = node.getNodeType();
		
		switch (key) {

		case "classDecl":
			
			currentEntry = table.searchRecord(globalTable, node.getChildrens().get(0).getData(), SymTableEntryCategory.Class);		
			
			break;
		
		case "inheritedList":
			
			for (int i = 0; i < node.getChildrens().size(); i++) {				
				for (SymTableEntry tempEntry : globalTable.getEntries()) {
					if (tempEntry.getName().equals(node.getChildrens().get(i).getData())) {
						table.setTables(tables);
						currentEntry.addInheritedClass(table.findTable(node.getChildrens().get(i).getData()));
					}
				}
			}
			break;

		default:
			break;
		}
		
		LinkedList<AstNode> childrens = node.getChildrens();
		for (AstNode child : childrens) { 
			validateInheritance(child);
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

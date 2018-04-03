package compiler.code_generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import compiler.helper.DataReadWrite;
import compiler.sematic.Semantic;
import compiler.sematic.SymTable;
import compiler.sematic.SymTableEntry;
import compiler.syntactic.AstNode;
import compiler.syntactic.Parser;

/**
 * Generate moon code for the source code using a stack-based model.
 * @author karan
 */
public class CodeGeneration {
	
	private AstNode root;
	private LinkedList<SymTable> tables;
	public static Map<Integer, ArrayList<String>> map;
	private SymTable tableObj = new SymTable();
	private SymTable globalTable;
	private SymTable currentTable;
	private String moonExecCode;
	private String moonCodeIndent;
	private Stack<String> registerPool;
	
	/**
	 * Initializes the global objects for the class after which the code generation phase is fired.
	 * @throws IOException
	 */
	public void intializeCodeGeneration() throws IOException {
		
		map = Semantic.getMap();
		root = Parser.getRoot();
		tables = Semantic.getTables();
		moonExecCode = new String();
		moonCodeIndent = new String("		");
		registerPool = new Stack<String>();
		
		tableObj.setTables(tables);
		globalTable = tableObj.findTable("global");
		
		codeGeneration();
		System.out.println(moonExecCode);
	}
	
	/**
	 * Generates the moon code instructions.
	 * @throws IOException
	 */
	public void codeGeneration() throws IOException {
		
		System.out.println("Starting Code Generation phase..........");
		System.out.println();
		
		// create a pool of registers as a stack of Strings
		// assuming only r1, ..., r12 are available
		for (Integer i = 12; i > 0; i--) {
			registerPool.push("r"+i.toString());
		}
		
		// generate moon program's entry point
		moonExecCode += moonCodeIndent + "entry\n";
		// make the stack frame pointer (address stored in r14) point 
		// to the top address allocated to the moon processor 
		moonExecCode += moonCodeIndent + "addi r14,r0,topaddr		% stack pointer\n";
		
		traverseAst(root);
		
		// halt the moon processor
		moonExecCode += moonCodeIndent + "hlt";
		
		// writing moon code instructions to the file
		DataReadWrite.writeMoonCode(moonExecCode);
		
		System.out.println();
		System.out.println("##################### END ######################");
	}
	
	/**
	 * Traverses the Ast and performs the required operations for the generation of the moon code instructions.
	 * @param node current node of the Abstract Syntax tree
	 */
	public void traverseAst(AstNode node) {
		
		if(node.getChildrens().size() == 0) {
			return;
		}
		
		// temporary register whose value will be stored in the moon memory
		String localRegister;
		// offset value of the variable in the symbol table
		String offset;
		String key = node.getNodeType();
		
		switch (key) {
		case "mainBody":			
			currentTable = tableObj.findTable("program");			
			break;
			
		case "assignStat":
			offset = varNode(node.getChildrens().get(0));
			localRegister = exprNode(node.getChildrens().get(1));
			// store the register value in the memory
			moonExecCode += moonCodeIndent + "sw "+offset+"(r14),"+localRegister+"		% storing expression value in the moon memory\n";
			registerPool.push(localRegister);
			break;

		case "putStat":
			offset = varNode(node.getChildrens().get(0));
			localRegister = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister+","+offset+"(r14)		% loading variable value in register\n";
			moonExecCode += moonCodeIndent +"putc "+localRegister+"			% output the variable value\n";
			registerPool.push(localRegister);
			break;
			
		case "":
			
			break;
			
		default:
			break;
		}
		
		for (AstNode child : node.getChildrens()) {
			traverseAst(child);
		}
	}
	
	/**
	 * Evaluates the expression.
	 * @param node current node of the Ast
	 * @return register which contains the value of the expression
	 */
	public String exprNode(AstNode node) {
		
		// temporary register whose value will be stored in the moon memory
		String localRegister = "";
		String offset = "";
		AstNode child = node.getChildrens().get(0);
		
		switch (child.getNodeType()) {
		case "numNode":
			localRegister = registerPool.pop();
			// stores the value in the register
			moonExecCode += moonCodeIndent + "addi "+localRegister+",r0,"+child.getData()+"		% processing value: "+child.getData()+"\n";
			break;
			
		case "addOp":
			localRegister = addOp(child);
			break;
			
		case "multOp":
			localRegister = multOp(child);
			break;
			
		case "var":
			offset = varNode(node.getChildrens().get(0));
			localRegister = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister+","+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		return localRegister;
	}
	
	/**
	 * Finds the offset value of the variable from the symbol table.
	 * @param node current node of the Ast
	 * @return offset value of the variable in the symbol table
	 */
	public String varNode(AstNode node) {
		
		// offset value of the variable in the symbol table
		String offset = "";
		// symbol table entry of the variable
		SymTableEntry entry = currentTable.searchRecord(currentTable, node.getChildrens().get(0).getData());
		offset = entry.getOffset();
		
		return offset;
	}
	
	/**
	 * Evaluates the addition or subtraction expressions
	 * @param node current node of the Ast
	 * @return register which contains the value of the expression
	 */
	public String addOp(AstNode node) {
		
		String localRegister1 = registerPool.pop();
		String localRegister2 = "";
		String localRegister3 = "";
		String offset = "";
		
		switch (node.getChildrens().get(0).getNodeType()) {
		case "numNode":
			localRegister2 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister2+",r0,"+node.getChildrens().get(0).getData()+"		% processing value: "+node.getChildrens().get(0).getData()+"\n";
			break;
			
		case "multOp":
			localRegister2 = multOp(node.getChildrens().get(0));
			break;
			
		case "addOp":
			localRegister2 = addOp(node.getChildrens().get(0));
			break;
			
		case "var":
			offset = varNode(node.getChildrens().get(0));
			localRegister2 = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister2+","+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		String operation = operatorType(node.getChildrens().get(1).getData());
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			localRegister3 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister3+",r0,"+node.getChildrens().get(2).getData()+"		% processing value: "+node.getChildrens().get(2).getData()+"\n";
			break;
			
		case "multOp":
			localRegister3 = multOp(node.getChildrens().get(2));
			break;
			
		case "addOp":
			localRegister3 = addOp(node.getChildrens().get(2));
			break;
			
		case "var":
			offset = varNode(node.getChildrens().get(2));
			localRegister3 = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister3+","+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		moonExecCode += moonCodeIndent + operation +" "+localRegister1+","+localRegister2+","+localRegister3+"\n";
		registerPool.push(localRegister3);
		registerPool.push(localRegister2);
		return localRegister1;
	}
	
	/**
	 * Evaluates the multiplication or division expressions
	 * @param node current node of the Ast
	 * @return register which contains the value of the expression
	 */
	public String multOp(AstNode node) {
		
		String localRegister1 = registerPool.pop();
		String localRegister2 = "";
		String localRegister3 = "";
		String offset = "";
		
		switch (node.getChildrens().get(0).getNodeType()) {
		case "numNode":
			localRegister2 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister2+",r0,"+node.getChildrens().get(0).getData()+"		% processing value: "+node.getChildrens().get(0).getData()+"\n";
			break;
			
		case "multOp":
			localRegister2 = multOp(node.getChildrens().get(0));
			break;
			
		case "addOp":
			localRegister2 = addOp(node.getChildrens().get(0));
			break;
			
		case "var":
			offset = varNode(node.getChildrens().get(0));
			localRegister2 = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister2+","+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		String operation = operatorType(node.getChildrens().get(1).getData());
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			localRegister3 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister3+",r0,"+node.getChildrens().get(2).getData()+"		% processing value: "+node.getChildrens().get(2).getData()+"\n";
			break;
			
		case "multOp":
			localRegister3 = multOp(node.getChildrens().get(2));
			break;
			
		case "addOp":
			localRegister3 = addOp(node.getChildrens().get(2));
			break;
			
		case "var":
			offset = varNode(node.getChildrens().get(2));
			localRegister3 = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister3+","+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		moonExecCode += moonCodeIndent + operation +" "+localRegister1+","+localRegister2+","+localRegister3+"\n";
		registerPool.push(localRegister3);
		registerPool.push(localRegister2);
		return localRegister1;
	}
	
	/**
	 * Returns the operation to be performed for a given operator
	 * @param operator symbol(+,-,/,*)
	 * @return operation to be performed
	 */
	public String operatorType(String operator) {
		
		String operation = "";
		
		switch (operator) {
		case "+":
			operation = "add";
			break;
			
		case "-":
			operation = "sub";
			break;
			
		case "*":
			operation = "mul";
			break;
			
		case "/":
			operation = "div";
			break;
			
		case "":
			
			break;

		default:
			break;
		}
		
		return operation;		
	}
}

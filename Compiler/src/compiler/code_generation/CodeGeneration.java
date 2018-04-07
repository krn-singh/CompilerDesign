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
	private String moonTagBlock;
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
		moonCodeIndent = new String("				");
		moonTagBlock = new String();
		registerPool = new Stack<String>();
		
		tableObj.setTables(tables);
		globalTable = tableObj.findTable("global");
		
		codeGeneration();
		
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
		
		traverseAst(root);
		
		// halt the moon processor
		moonExecCode += moonCodeIndent + "hlt\n";
		moonExecCode += moonCodeIndent + "\n";
		moonExecCode += "temp_var				res	4";
		
		System.out.println(moonExecCode);
		
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
		case "funcDef":			
			switch (node.getChildrens().get(1).getNodeType()) {
			case "scopeSpec":
				currentTable = tableObj.findTable(node.getChildrens().get(2).getData());
				// defining the function tag for passing control during the moon code execution
				moonTagBlock += "f_"+node.getChildrens().get(2).getData();
				moonExecCode += moonTagBlock;
				break;

			default:				
				currentTable = tableObj.findTable(node.getChildrens().get(1).getData());
				// defining the function tag for passing control during the moon code execution
				moonTagBlock += "f_"+node.getChildrens().get(1).getData();
				moonExecCode += moonTagBlock;
				break;
			}			
			break;
			
		case "mainBody":			
			currentTable = tableObj.findTable("program");
			// generate moon program's entry point
			moonExecCode += moonCodeIndent + "entry\n";
			// make the stack frame pointer (address stored in r14) point 
			// to the top address allocated to the moon processor 
			moonExecCode += moonCodeIndent + "addi r14, r0, topaddr	% stack pointer\n";
			break;
			
		case "assignStat":
			offset = varNode(node.getChildrens().get(0));
			localRegister = exprNode(node.getChildrens().get(1));
			// store the register value in the memory
			moonExecCode += moonCodeIndent + "sw "+offset+"(r14), "+localRegister+"		% storing expression value in the moon memory\n";
			registerPool.push(localRegister);
			break;

		case "returnStat":
			offset = varNode(node.getChildrens().get(0));
			localRegister = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister+", "+offset+"(r14)		% loading variable value in register\n";
			moonExecCode += moonCodeIndent +"sw temp_var(r0), "+localRegister+"	% store the return value in a temporary variable\n";
			moonExecCode += moonCodeIndent +"jr r15			% jump back to the calling function\n";
			registerPool.push(localRegister);
			break;
			
		case "putStat":
			offset = varNode(node.getChildrens().get(0));
			localRegister = registerPool.pop();
			moonExecCode += moonCodeIndent +"lw "+localRegister+", "+offset+"(r14)		% loading variable value in register\n";
			moonExecCode += moonCodeIndent +"putc "+localRegister+"			% output the variable value\n";
			registerPool.push(localRegister);
			break;
			
		case "ifStat":
			localRegister = relOp(node.getChildrens().get(0));
			moonExecCode += moonCodeIndent +"bz "+localRegister+", else_block		% jump to else block\n";
			registerPool.push(localRegister);
			traverseAst(node.getChildrens().get(1));
			moonExecCode += moonCodeIndent +"j end_if			% end if-else block\n";
			moonTagBlock += "else_block";
			moonExecCode += moonTagBlock;
			traverseAst(node.getChildrens().get(2));
			moonExecCode += moonCodeIndent +"end_if nop\n";			
			return;
			
		case "forStat":
			traverseAst(node.getChildrens().get(1));
			moonTagBlock += "for_loop";
			moonExecCode += moonTagBlock;
			localRegister = relOp(node.getChildrens().get(2));
			moonExecCode += moonCodeIndent +"bz "+localRegister+", end_for		% end for loop\n";
			registerPool.push(localRegister);
			traverseAst(node.getChildrens().get(3));
			traverseAst(node.getChildrens().get(4));
			moonExecCode += moonCodeIndent +"j for_loop			% iterate the for loop\n";	
			moonExecCode += moonCodeIndent +"end_for nop\n";			
			return;
						
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
			moonExecCode += moonCodeIndent + "addi "+localRegister+", r0, "+child.getData()+"		% processing value: "+child.getData()+"\n";
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
			moonExecCode += moonCodeIndent +"lw "+localRegister+", "+offset+"(r14)		% loading variable value in register\n";
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
		boolean isFreeFunc = false;
		boolean isArray = false;
		boolean isClassMember = false;
		
		if (node.getChildrens().get(1).getChildrens().size() > 0) {
			
			isArray = true;
			if (node.getChildrens().get(1).getChildrens().get(0).getNodeType().equals("aParamList"))  { isFreeFunc = true;    }
			else if (node.getChildrens().get(1).getChildrens().get(0).getNodeType().equals("idNode")) { isClassMember = true; }
		}
	
		if (isFreeFunc) 		  	{	offset = isFreeFunction(node);	}
		else if(isClassMember)   { 	offset = isClassMember(node);	}
		else if(isArray) 		{	offset = isArray(node);			} 
		else 					{   offset = isLocalVariable(node);	}
		
		return offset;
	}
	
	/**
	 * Finds the offset value of the variable from the symbol table.
	 * @param node current node of the Ast
	 * @return offset value of the variable in the symbol table
	 */
	public String isFreeFunction(AstNode node) {
		
		// offset value of the variable in the symbol table
		String offset = "";
		String localRegister = registerPool.pop();
		// the size of current function in the memory
		int funcSize = Integer.parseInt(tableObj.searchRecord(globalTable, currentTable.getTableName()).getSize());
		int paramSize = 0;
		// reserving memory for storing temporary values
		int bufferSize = 4;
		// symbol table of the function to be called
		SymTable funcReferenceTble = tableObj.findTable(node.getChildrens().get(0).getData());
		int paramCount = node.getChildrens().get(1).getChildrens().get(0).getChildrens().size();
		
		for (int i = 0; i < paramCount; i++) {
			offset = varNode(node.getChildrens().get(1).getChildrens().get(0).getChildrens().get(i));
			moonExecCode += moonCodeIndent +"lw "+localRegister+", "+offset+"(r14)		% loading variable value in register\n";
			paramSize = Math.abs(Integer.parseInt(funcReferenceTble.getEntries().get(i).getOffset()));
			moonExecCode += moonCodeIndent + "sw -"+Integer.toString(funcSize+bufferSize+paramSize)+"(r14), "+localRegister+"		% passing argument to the function parameter\n";
		}
		
		offset = Integer.toString(funcSize+bufferSize);
		moonExecCode += moonCodeIndent + "addi r14, r14, -"+offset+"	% updating the stack pointer for the function call\n";
		moonExecCode += moonCodeIndent + "jl r15, f_"+funcReferenceTble.getTableName()+"		% function call\n";
		moonExecCode += moonCodeIndent + "subi r14, r14, -"+offset+"	% updating the stack pointer for the function call\n";
		moonExecCode += moonCodeIndent +"lw "+localRegister+", temp_var(r0)	% value returned by function\n";
		moonExecCode += moonCodeIndent + "sw -"+offset+"(r14), "+localRegister+"		% storing the returned value\n";
		registerPool.push(localRegister);
		
		return "-"+offset;
	}
	
	/**
	 * Finds the offset value of the variable from the symbol table.
	 * @param node current node of the Ast
	 * @return offset value of the variable in the symbol table
	 */
	public String isClassMember(AstNode node) {
		
		// offset value of the variable in the symbol table
		String offset = "";
		
		return offset;
	}
	
	/**
	 * Finds the offset value of the variable from the symbol table.
	 * @param node current node of the Ast
	 * @return offset value of the variable in the symbol table
	 */
	public String isArray(AstNode node) {
		
		// offset value of the variable in the symbol table
		String offset = "";
		
		return offset;
	}
	
	/**
	 * Finds the offset value of the variable from the symbol table.
	 * @param node current node of the Ast
	 * @return offset value of the variable in the symbol table
	 */
	public String isLocalVariable(AstNode node) {
		
		// offset value of the variable in the symbol table
		String offset = "";		
		// symbol table entry of the variable
		SymTableEntry entry = tableObj.searchRecord(currentTable, node.getChildrens().get(0).getData());
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
			moonExecCode += moonCodeIndent + "addi "+localRegister2+", r0, "+node.getChildrens().get(0).getData()+"		% processing value: "+node.getChildrens().get(0).getData()+"\n";
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
			moonExecCode += moonCodeIndent +"lw "+localRegister2+", "+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		String operation = operatorType(node.getChildrens().get(1).getData());
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			localRegister3 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister3+", r0, "+node.getChildrens().get(2).getData()+"		% processing value: "+node.getChildrens().get(2).getData()+"\n";
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
			moonExecCode += moonCodeIndent +"lw "+localRegister3+", "+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		moonExecCode += moonCodeIndent + operation +" "+localRegister1+", "+localRegister2+", "+localRegister3+"\n";
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
			moonExecCode += moonCodeIndent + "addi "+localRegister2+", r0, "+node.getChildrens().get(0).getData()+"		% processing value: "+node.getChildrens().get(0).getData()+"\n";
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
			moonExecCode += moonCodeIndent +"lw "+localRegister2+", "+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		String operation = operatorType(node.getChildrens().get(1).getData());
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			localRegister3 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister3+", r0, "+node.getChildrens().get(2).getData()+"		% processing value: "+node.getChildrens().get(2).getData()+"\n";
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
			moonExecCode += moonCodeIndent +"lw "+localRegister3+", "+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		moonExecCode += moonCodeIndent + operation +" "+localRegister1+", "+localRegister2+", "+localRegister3+"\n";
		registerPool.push(localRegister3);
		registerPool.push(localRegister2);
		return localRegister1;
	}
	
	/**
	 * Evaluates the relational expressions
	 * @param node current node of the Ast
	 * @return register which contains the value of the expression
	 */
	public String relOp(AstNode node) {
		
		String localRegister1 = registerPool.pop();
		String localRegister2 = "";
		String localRegister3 = "";
		String offset = "";
		
		switch (node.getChildrens().get(0).getNodeType()) {
		case "numNode":
			localRegister2 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister2+", r0, "+node.getChildrens().get(0).getData()+"		% processing value: "+node.getChildrens().get(0).getData()+"\n";
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
			moonExecCode += moonCodeIndent +"lw "+localRegister2+", "+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		String operation = operatorType(node.getChildrens().get(1).getData());
		
		switch (node.getChildrens().get(2).getNodeType()) {
		case "numNode":
			localRegister3 = registerPool.pop();
			moonExecCode += moonCodeIndent + "addi "+localRegister3+", r0, "+node.getChildrens().get(2).getData()+"		% processing value: "+node.getChildrens().get(2).getData()+"\n";
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
			moonExecCode += moonCodeIndent +"lw "+localRegister3+", "+offset+"(r14)		% loading variable value in register\n";
			break;

		default:
			break;
		}
		
		moonExecCode += moonCodeIndent + operation +" "+localRegister1+", "+localRegister2+", "+localRegister3+"\n";
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
			
		case "eq":
			operation = "ceq";
			break;
			
		case "neq":
			operation = "cne";
			break;
			
		case "lt":
			operation = "clt";
			break;
			
		case "gt":
			operation = "cgt";
			break;
			
		case "leq":
			operation = "cle";
			break;
			
		case "geq":
			operation = "cge";
			break;

		default:
			break;
		}
		
		return operation;		
	}
}

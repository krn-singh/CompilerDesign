package compiler.syntactic;

import java.util.LinkedList;

import compiler.constants.CompilerEnum.TokenType;
import compiler.visitors.Visitor;

public class AstNode {

	private String data = "";
	private TokenType type;
	private String nodeType = "";
	private Integer lineNumber;
	private LinkedList<AstNode> childrens = new LinkedList<>();
	public int nodeLevel = 0;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public LinkedList<AstNode> getChildrens() {
		return childrens;
	}

	public void setChildrens(LinkedList<AstNode> childrens) {
		this.childrens = childrens;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	
	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void astTraversel(AstNode root) {
		
		if(root.getChildrens().size() == 0) {
			for (int i = 0; i < nodeLevel; i++ )
	    		System.out.print("  ");
			
			String toprint = String.format("%-45s" , "Node."+root.getNodeType()); 
			for (int i = 0; i < nodeLevel; i++ )
	    			toprint = toprint.substring(0, toprint.length() - 2);
			toprint += String.format("%-20s" , (root.getData() == null ? " | " : " | " + root.getData()));    	
			toprint += String.format("%-20s" , (root.getType() == null ? " | " : " | " + root.getType()));
			
			System.out.println(toprint);
			return;
		}
		
		for (int i = 0; i < nodeLevel; i++ )
    		System.out.print("  ");
		
		String toprint = String.format("%-45s" , "Node."+root.getNodeType()); 
		for (int i = 0; i < nodeLevel; i++ )
    			toprint = toprint.substring(0, toprint.length() - 2);
		toprint += String.format("%-20s" , (root.getData() == null ? " | " : " | " + root.getData()));    	
		toprint += String.format("%-20s" , (root.getType() == null ? " | " : " | " + root.getType()));
		
		System.out.println(toprint);
		
		nodeLevel++;
		
		LinkedList<AstNode> childrens = root.getChildrens();
		for (AstNode child : childrens) { 
			astTraversel(child);
		}
		
		nodeLevel--;
		
	}
	
    public void print(AstNode root){
    	System.out.println("=================================================================================");
    	System.out.println("Node type                                     | data              | type         ");
    	System.out.println("=================================================================================");
    	astTraversel(root);
    	System.out.println("=================================================================================");
    }
    
    public void accept(Visitor visitor) {
    		visitor.visit(this);
    }
}

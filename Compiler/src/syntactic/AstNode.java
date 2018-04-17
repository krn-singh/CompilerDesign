/**
 * The syntactic package holds the classes required for parsing and later on,
 * building The Abstract Syntax Tree.
 */
package syntactic;

import java.io.IOException;
import java.util.LinkedList;

import helper.DataReadWrite;

/**
 * Provides the attributes required for the node of the Abstract Syntax Tree.
 * Also provides functions for printing the tree.
 * 
 * @author krn-singh
 */
public class AstNode {

	private String data = "";
	private String type;
	private String nodeType = "";
	private Integer lineNumber;
	private String subtreeString = "";
	private LinkedList<AstNode> childrens = new LinkedList<>();
	private int nodeLevel = 0;
	private String abstractTree = new String();
	

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
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

	public String getSubtreeString() {
		return subtreeString;
	}

	public void setSubtreeString(String subtreeString) {
		this.subtreeString = subtreeString;
	}

	/**
	 * Performs the depth-first search of the node of a tree.
	 * 
	 * @param root current node of the tree
	 */
	public void astTraversel(AstNode root) {
		
		if(root.getChildrens().size() == 0) {
			for (int i = 0; i < nodeLevel; i++ ) {
				System.out.print("  ");
				abstractTree += "  ";
			}
	    		
			
			String toprint = String.format("%-45s" , "Node."+root.getNodeType()); 
			for (int i = 0; i < nodeLevel; i++ )
	    			toprint = toprint.substring(0, toprint.length() - 2);
			toprint += String.format("%-15s" , (root.getData() == null ? " | " : " | " + root.getData()));    	
			toprint += String.format("%-15s" , (root.getType() == null ? " | " : " | " + root.getType()));
			
			System.out.println(toprint);
			abstractTree += toprint+"\n";
			return;
		}
		
		for (int i = 0; i < nodeLevel; i++ ) {
			System.out.print("  ");
			abstractTree += "  ";
		}
		
		String toprint = String.format("%-45s" , "Node."+root.getNodeType()); 
		for (int i = 0; i < nodeLevel; i++ )
    			toprint = toprint.substring(0, toprint.length() - 2);
		toprint += String.format("%-15s" , (root.getData() == null ? " | " : " | " + root.getData()));    	
		toprint += String.format("%-15s" , (root.getType() == null ? " | " : " | " + root.getType()));
		
		System.out.println(toprint);
		abstractTree += toprint+"\n";
		
		nodeLevel++;
		
		LinkedList<AstNode> childrens = root.getChildrens();
		for (AstNode child : childrens) { 
			astTraversel(child);
		}
		
		nodeLevel--;
		
	}
	
	/**
	 * Accepts the root of the AST and calls the astTraversal
	 * 
	 * @param root Root of the AST
	 * @throws IOException handles the I/O related interruptions
	 */
    public void print(AstNode root) throws IOException{
    	System.out.println("============================================================================");
    	abstractTree += "============================================================================\n";
    	System.out.println("Node type                                     | data         | type         ");
    	abstractTree += "Node type                                     | data         | type         \n";
    	System.out.println("============================================================================");
    	abstractTree += "============================================================================\n";
    	astTraversel(root);
    	System.out.println("============================================================================");
    	abstractTree += "============================================================================\n";
    	
    	DataReadWrite.writeAST(abstractTree);
    }
}

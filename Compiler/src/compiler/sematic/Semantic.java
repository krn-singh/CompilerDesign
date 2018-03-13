package compiler.sematic;

import java.util.LinkedList;
import compiler.syntactic.AstNode;

public class Semantic {
	
	public int nodeLevel = 0;
	
	public void astTraversel(AstNode root) {
		
		if(root.getChildrens().size() == 0) {
			return;
		}
		
		for (int i = 0; i < nodeLevel; i++ )
    		System.out.print("  ");
		
		String toprint = String.format("%-35s" , "Node."+root.getNodeType()); 
		for (int i = 0; i < nodeLevel; i++ )
    			toprint = toprint.substring(0, toprint.length() - 2);
		toprint += String.format("%-15s" , (root.getData() == null        ? " | " : " | " + root.getData()));    	
		toprint += String.format("%-15s" , (root.getType() == null        ? " | " : " | " + root.getType()));
		
		System.out.println(toprint);
		
		nodeLevel++;
		
		LinkedList<AstNode> childrens = root.getChildrens();
		for (AstNode child : childrens) { 
			astTraversel(child);
		}
		
		nodeLevel--;
		
	}

	
    public void print(AstNode root){
    	System.out.println("==================================================================");
    	System.out.println("Node type                           | data         | type         ");
    	System.out.println("==================================================================");
    	astTraversel(root);
    	System.out.println("==================================================================");

    }
	
}

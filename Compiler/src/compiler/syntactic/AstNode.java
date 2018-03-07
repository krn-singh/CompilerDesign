package compiler.syntactic;

import java.util.LinkedList;

import compiler.constants.CompilerEnum.TokenType;

public class AstNode {

	AstNode leftMostChild;
	AstNode leftMostSibling;
	AstNode rightSibling;
	AstNode parent;
	
	String value;
	TokenType type;
	
	LinkedList<AstNode> siblings;
	
	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public LinkedList<AstNode> getSiblings() {
		return siblings;
	}

	public void setSiblings(AstNode sibling) {
		siblings.add(sibling);
	}

	public AstNode getLeftMostChild() {
		return leftMostChild;
	}

	public void setLeftMostChild(AstNode leftMostChild) {
		this.leftMostChild = leftMostChild;
	}

	public AstNode getLeftMostSibling() {
		return leftMostSibling;
	}

	public void setLeftMostSibling(AstNode leftMostSibling) {
		this.leftMostSibling = leftMostSibling;
	}

	public AstNode getRightSibling() {
		return rightSibling;
	}

	public void setRightSibling(AstNode rightSibling) {
		this.rightSibling = rightSibling;
	}

	public AstNode getParent() {
		return parent;
	}

	public void setParent(AstNode parent) {
		this.parent = parent;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
	
}

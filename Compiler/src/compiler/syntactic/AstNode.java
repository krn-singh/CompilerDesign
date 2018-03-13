package compiler.syntactic;

import java.util.LinkedList;

import compiler.constants.CompilerEnum.TokenType;

public class AstNode {

	private String data;
	private TokenType type;
	private String nodeType;
	private LinkedList<AstNode> childrens = new LinkedList<>();

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
}

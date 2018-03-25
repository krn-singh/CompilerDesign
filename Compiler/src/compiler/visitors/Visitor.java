package compiler.visitors;

import compiler.syntactic.AssignStat;
import compiler.syntactic.AstNode;

public interface Visitor {

	public void visit(AssignStat node);
	public void visit(AstNode node);
}

package compiler.syntactic;

import compiler.visitors.Visitor;

public class AssignStat {
	
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

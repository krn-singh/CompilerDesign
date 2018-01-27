package compiler.utilities;

public class CompilerEnum {

	public static enum TokenType {
		
		IDENTIFIER, INTEGER, FLOAT, KEYWORD, EQLTO, NOTEQL, LTHEN, GTHEN, LEQL, GEQL, SEMICOL, COMMA, DOT, COL, DBLCOL, PLUS, MINUS, STAR, FSLASH, EQL, LPAREN, RPAREN, LCURL, RCURL, LSQR, RSQR
	}
	
	public static enum TokenValue {
		
		AND, NOT, OR, IF, THEN, ELSE, FOR, CLASS, INT, FLOAT, GET, PUT, RETURN, PROGRAM
	}
}

package compiler.utilities;

public class CompilerEnum {

	public static enum TokenType {
		
		IDENTIFIER, INTEGER, FLOAT, KEYWORD, EQLTO, NOTEQL, LTHEN, GTHEN, LEQL, GEQL, SEMICOL, COMMA, DOT, COL, DBLCOL, PLUS, MINUS, STAR, FSLASH, EQL, LPAREN, RPAREN, LCURL, RCURL, LSQR, RSQR
	}
	
	public static enum TokenValue {

		AND("and"), NOT("not"), OR("or"), IF("if"), THEN("then"), ELSE("else"), FOR("for"), CLASS("class"), INT("int"), FLOAT("float"), GET("get"), PUT("put"), RETURN("return"), PROGRAM("program");
		
		private final String value;
		
		TokenValue (String value) {
			this.value=value;
		}
		
		public String tokenValue() {
			
			return value;
		}
	}
}

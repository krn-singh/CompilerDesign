package compiler.constants;

/**
 * Contains all the enums and other constants required for the compiler
 * @author Karan
 * @version 1.0
 */
public class CompilerEnum {
	
	public static String START_SYMBOL = "prog";

	public static enum TokenType {
		
		id, intNum, floatNum, keyword, eq, neq, lt, gt, leq, geq, operator, puntuation, sr, eof
	}
	
	public static enum NodeType {
		
		id, intNum, floatNum, keyword, eq, neq, lt, gt, leq, geq, operator, puntuation, sr, eof
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

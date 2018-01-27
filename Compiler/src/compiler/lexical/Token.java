package compiler.lexical;

import compiler.utilities.CompilerEnum.TokenType;

public class Token {

    public final TokenType type;
    public final String tokenValue;
    public final Integer lineNumber;
    
    public Token(TokenType type, String tokenValue, Integer lineNumber) {
        this.type = type;
        this.tokenValue = tokenValue;
        this.lineNumber = lineNumber;
    }
    
    public TokenType getType() {
		return type;
	}

	public String getTokenValue() {
		return tokenValue;
	}
	
	public Integer getLineNumber() {
		return lineNumber;
	}

	public static void createToken() {
		
		
	}
	
	public String toString() {

        return "<TokenType: "+type.toString()+", TokenValue: '"+tokenValue+"', Line Number: "+lineNumber+">";
    }
}

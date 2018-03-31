package compiler.lexical;

import compiler.constants.CompilerEnum.TokenType;

/**
 * The Token class contains the information regarding the token type (ID, EQLTO,.....), token value (lexeme) and its location in the source code
 * @author Karan
 * @version 1.0
 */
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
	
	public String toString() {

        return "<TokenType: "+type.toString()+", TokenValue: '"+tokenValue+"', Line Number: "+lineNumber+">";
    }
	
}

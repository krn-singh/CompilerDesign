package compiler.lexical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import compiler.helper.DataReadWrite;
import compiler.utilities.CompilerEnum.TokenType;
import compiler.utilities.CompilerEnum.TokenValue;

/**
 * Generating tokens from the source code
 * @author Karan
 * @version 1.0
 */
public class Tokenizer {


	public static Token token;
	
	/**
	 * finds the alphanumeric(only identifiers or reserved keywords but not literals, punctuation or other operators) token in the given input 
	 * @param input Input String
	 * @param lexemeBegin Starting index of the lexeme to be generated
	 * @return token
	 */
	public static Token alphaLexeme(String input, int lexemeBegin, Integer lineNumber) {
		
    	int forward = lexemeBegin;
    	forward++;
        for(; forward < input.length(); ) {
            if(Character.isLetter(input.charAt(forward)) || Character.isDigit(input.charAt(forward)) || input.charAt(forward)=='_') {
            	forward++;
            } else {
          
            	for (TokenValue value : TokenValue.values()) {
					
            		if (input.substring(lexemeBegin, forward).equalsIgnoreCase(value.tokenValue())) {
            			return new Token(TokenType.KEYWORD, input.substring(lexemeBegin, forward), lineNumber);
					}
				}
                return new Token(TokenType.ID, input.substring(lexemeBegin, forward), lineNumber);                
            }
        }
        
        for (TokenValue value : TokenValue.values()) {
			
    		if (input.substring(lexemeBegin, forward).equalsIgnoreCase(value.tokenValue())) {
    			return new Token(TokenType.KEYWORD, input.substring(lexemeBegin, forward), lineNumber);
			}
		}
		return new Token(TokenType.ID, input.substring(lexemeBegin, forward), lineNumber);
	}
	
	/**
	 * finds the numeric(only integers and floats) token in the given input 
	 * @param input Input String
	 * @param lexemeBegin Starting index of the lexeme to be generated
	 * @return token
	 */
    public static Token numericLexeme(String input, int lexemeBegin, Integer lineNumber) {
    	
    	int forward = lexemeBegin;
    	forward++;
        for(; forward < input.length(); ) {
        	
        	if(Character.isDigit(input.charAt(forward))) {
            	forward++;
            } else if(input.charAt(forward)=='.' && forward < input.length()-1) { 

            	// the control reaches here if current char is '.' and is not the last character in input.
            	
            	forward++;
            	if (Character.isDigit(input.charAt(forward)) && forward < input.length()-1) {
            		
            		// the control reaches here if current char is a digit after '.' and is not the last character in input.
            		
            		forward++;
            		for ( ; forward < input.length(); ) {
						           			
            			if(Character.isDigit(input.charAt(forward))) {
            				forward++;
            			} else if (input.charAt(forward)=='e' && forward < input.length()-1) {
    						
            				// the control reaches here if current char is 'e' and is not the last character in input.
            				
            				forward++;
            				if (input.charAt(forward)=='0') {
    							
                    			forward++;
                    			return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);
    						} else if(Character.isDigit(input.charAt(forward))) {
    							
    							// the control reaches here if the previous character('e') is preceded by digits.
    							
    							forward++;
    							for ( ; forward < input.length(); ) {
    								
    								if (Character.isDigit(input.charAt(forward))) {
										forward++;
									} else {
										
										return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);
									}
    							}
							
    							return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);
						    } else if((input.charAt(forward)=='+' && (forward<input.length()-1)) || (input.charAt(forward)=='-' && (forward < input.length()-1))) {
						    	
						    	// the control reaches here if the previous character('e') is preceded by +/-.
						    	
						    	forward++;
    							for ( ; forward < input.length(); ) {
    								
    								if (Character.isDigit(input.charAt(forward))) {
										forward++;
									} else {
										forward-=2;
										return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);
									}
    							}
							
    							return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);
						    	
						    } else {
						    	
    							forward--;
    							return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);
						    }
    					} else {
    						
    						return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);
    					}
					}
            		
            		return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);               	
				} else if(Character.isDigit(input.charAt(forward)) && forward == input.length()-1) {
					
					// the control reaches here if the previous character('.') is preceded by digit, which is last in the input.
					
					forward++;
					return new Token(TokenType.FLOAT, input.substring(lexemeBegin, forward), lineNumber);					
				} else {
					
					// the control reaches here if the previous character('.') is not preceded by required characters for the FLOAT token.
					
					forward--;
					return new Token(TokenType.INTEGER, input.substring(lexemeBegin, forward), lineNumber);
				}
            } else {
          
                return new Token(TokenType.INTEGER, input.substring(lexemeBegin, forward), lineNumber);                
            }
        }
        return new Token(TokenType.INTEGER, input.substring(lexemeBegin, forward), lineNumber);
    }

    public static List<Token> nextToken(Integer lineNumber, String input) {
        List<Token> tokens = new ArrayList<Token>();
        
        for(int currentChar = 0; currentChar < input.length(); ) {
        	
            switch(input.charAt(currentChar)) {
            
            case '=':
                currentChar++;
                if (input.charAt(currentChar)=='=') {
					
                	tokens.add(new Token(TokenType.EQLTO, "==", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.EQL, "=", lineNumber));
				}
                break;
                
            case '<':
                currentChar++;
                if (input.charAt(currentChar)=='>') {
					
                	tokens.add(new Token(TokenType.NOTEQL, "<>", lineNumber));
                	currentChar++;
				} else if(input.charAt(currentChar)=='=') {
					
                	tokens.add(new Token(TokenType.LEQL, "<=", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.LTHEN, "<", lineNumber));
				}
                break;
                
            case '>':
                currentChar++;
                if (input.charAt(currentChar)=='=') {
					
                	tokens.add(new Token(TokenType.GEQL, ">=", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.GTHEN, ">", lineNumber));
				}
                break;
                
            case ';':
                tokens.add(new Token(TokenType.SEMICOL, ";", lineNumber));
                currentChar++;
                break;
                
            case ',':
                tokens.add(new Token(TokenType.COMMA, ",", lineNumber));
                currentChar++;
                break;
                
            case '.':
                tokens.add(new Token(TokenType.DOT, ".", lineNumber));
                currentChar++;
                break;
                
            case ':':
                currentChar++;
                if (input.charAt(currentChar)==':') {
					
                	tokens.add(new Token(TokenType.DBLCOL, "::", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.COL, ":", lineNumber));
				}
                break;
                
            case '(':
                tokens.add(new Token(TokenType.LPAREN, "(", lineNumber));
                currentChar++;
                break;
                
            case ')':
                tokens.add(new Token(TokenType.RPAREN, ")", lineNumber));
                currentChar++;
                break;
                
            case '{':
                tokens.add(new Token(TokenType.LCURL, "{", lineNumber));
                currentChar++;
                break;
                
            case '}':
                tokens.add(new Token(TokenType.RCURL, "}", lineNumber));
                currentChar++;
                break;
                
            case '[':
                tokens.add(new Token(TokenType.LSQR, "[", lineNumber));
                currentChar++;
                break;
                
            case ']':
                tokens.add(new Token(TokenType.RSQR, "]", lineNumber));
                currentChar++;
                break;
                
            case '+':
                tokens.add(new Token(TokenType.PLUS, "+", lineNumber));
                currentChar++;
                break;
               
            case '-':
                tokens.add(new Token(TokenType.MINUS, "-", lineNumber));
                currentChar++;
                break;
                
            case '*':
                tokens.add(new Token(TokenType.STAR, "*", lineNumber));
                currentChar++;
                break;
                
            case '/':
            	tokens.add(new Token(TokenType.FSLASH, "/", lineNumber));
                currentChar++;
                break;
                
            default:
                if(Character.isWhitespace(input.charAt(currentChar))) {
                    currentChar++;
                } else if (Character.isLetter(input.charAt(currentChar))) {
                	
                    token = alphaLexeme(input, currentChar, lineNumber);
                    currentChar += token.getTokenValue().length();
                    tokens.add(token);
                } else if (Character.isDigit(input.charAt(currentChar)) && input.charAt(currentChar)=='0') {
                	
                	currentChar++;
                    tokens.add(new Token(TokenType.INTEGER, "0", lineNumber));
                } else if (Character.isDigit(input.charAt(currentChar))) {

                    token = numericLexeme(input, currentChar, lineNumber);
                    currentChar += token.getTokenValue().length();
                    tokens.add(token);
                }
                break;
            }
        }
        return tokens;
    }

    public static void main(String[] args) throws IOException {
   	
    	TreeMap<Integer, String> inputList = DataReadWrite.readInput();
    	List<Token> tokens=null;
    	int tokenCount=0;
    	try {

    		Iterator<Map.Entry<Integer, String>> entrySet = inputList.entrySet().iterator();
    		Map.Entry<Integer, String> entry;
    		while (entrySet.hasNext()) {
				
    			entry = entrySet.next();
    			if (entry.getValue()!=null) {
    				
    				String aToCc="";
        			tokens = nextToken(entry.getKey(), entry.getValue());
                    for(Token token : tokens) {
                        System.out.println(token);
                        aToCc+=token.type.toString()+"+";
                        //DataReadWrite.writeOutput(token);
                        tokenCount++;
                    }
                    if (aToCc!="") {
                    	System.out.println(aToCc.substring(0, aToCc.length()-1));
					}          
				}
			}        	
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No content in input file");
		}
        System.out.println("Total tokens: "+tokenCount);
    }
}

package lexical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import constants.CompilerEnum.TokenType;
import constants.CompilerEnum.TokenValue;
import helper.DataReadWrite;
import helper.Util;

/**
 * Generating tokens from the source code
 * @author Karan
 * @version 1.0
 */
public class Tokenizer {
	

	public static Token token;
	public static List<Token> outputTokens = new ArrayList<Token>();
	public static List<String> aToCcFormat = new ArrayList<String>();
	public static Map<Integer, ArrayList<String>> map;

	public static Map<Integer, ArrayList<String>> getMap() {
		return map;
	}

	/**
	 * finds the alphanumeric(only identifiers or reserved keywords but not literals, punctuation or other operators) token in the given input 
	 * @param input Input String
	 * @param lexemeBegin Starting index of the lexeme to be generated
	 * @param lineNumber Line number in the source program
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
            			if (input.substring(lexemeBegin, forward).equalsIgnoreCase("int")) {
            				return new Token(TokenType.Int, input.substring(lexemeBegin, forward), lineNumber);
					} else if(input.substring(lexemeBegin, forward).equalsIgnoreCase("float")) {
							return new Token(TokenType.Float, input.substring(lexemeBegin, forward), lineNumber);
					} else {
							return new Token(TokenType.keyword, input.substring(lexemeBegin, forward), lineNumber);
					}	
					}
				}
                return new Token(TokenType.id, input.substring(lexemeBegin, forward), lineNumber);                
            }
        }
        
    	for (TokenValue value : TokenValue.values()) {
			
    		if (input.substring(lexemeBegin, forward).equalsIgnoreCase(value.tokenValue())) {
    			if (input.substring(lexemeBegin, forward).equalsIgnoreCase("int")) {
    				return new Token(TokenType.Int, input.substring(lexemeBegin, forward), lineNumber);
			} else if(input.substring(lexemeBegin, forward).equalsIgnoreCase("float")) {
					return new Token(TokenType.Float, input.substring(lexemeBegin, forward), lineNumber);
			} else {
					return new Token(TokenType.keyword, input.substring(lexemeBegin, forward), lineNumber);
			}	
			}
		}
		return new Token(TokenType.id, input.substring(lexemeBegin, forward), lineNumber);
	}
	
	/**
	 * finds the numeric(only integers and floats) token in the given input 
	 * @param input Input String
	 * @param lexemeBegin Starting index of the lexeme to be generated
	 * @param lineNumber Line number in the source program
	 * @return token
	 */
    public static Token numericLexeme(String input, int lexemeBegin, Integer lineNumber) {
    	
    	int forward = lexemeBegin;
    	forward++;
    	int checkPoint;
        for(; forward < input.length(); ) {
        	
        	if(Character.isDigit(input.charAt(forward))) {
            	forward++;
            } else if(input.charAt(forward)=='.' && forward < input.length()-1) { 

            	// the control reaches here if current char is '.' and is not the last character in input.
            	
            	forward++;
            	if (Character.isDigit(input.charAt(forward)) && forward < input.length()-1) {
            		
            		// the control reaches here if current char is a digit after '.' and is not the last character in input.
            		
            		checkPoint = forward;
            		forward++;            		
            		for ( ; forward < input.length(); ) {						
            			
            			if(Character.isDigit(input.charAt(forward))) {
            				
            				if (input.charAt(forward)!='0') {
								
            					checkPoint = forward;
							}
            				forward++;
            			} else if (input.charAt(forward)=='e' && forward < input.length()-1) {
    						
            				// the control reaches here if current char is 'e' and is not the last character in input.
            				
            				forward++;
            				if (input.charAt(forward)=='0') {
    							
                    			forward++;
                    			return new Token(TokenType.floatNum, input.substring(lexemeBegin, forward), lineNumber);
    						} else if(Character.isDigit(input.charAt(forward))) {
    							
    							// the control reaches here if the previous character('e') is preceded by digits.
    							
    							forward++;
    							for ( ; forward < input.length(); ) {
    								
    								if (Character.isDigit(input.charAt(forward))) {
										forward++;
									} else {
										
										return new Token(TokenType.floatNum, input.substring(lexemeBegin, forward), lineNumber);
									}
    							}
							
    							return new Token(TokenType.floatNum, input.substring(lexemeBegin, forward), lineNumber);
						    } else if((input.charAt(forward)=='+' && (forward<input.length()-1)) || (input.charAt(forward)=='-' && (forward < input.length()-1))) {
						    	
						    	// the control reaches here if the previous character('e') is preceded by +/-.
						    	
						    	forward++;
    							for ( ; forward < input.length(); ) {
    								
    								if (Character.isDigit(input.charAt(forward))) {
										forward++;
									} else {
										forward-=2;
										return new Token(TokenType.floatNum, input.substring(lexemeBegin, forward), lineNumber);
									}
    							}
							
    							return new Token(TokenType.floatNum, input.substring(lexemeBegin, forward), lineNumber);
						    	
						    } else {
						    	
    							forward--;
    							return new Token(TokenType.floatNum, input.substring(lexemeBegin, forward), lineNumber);
						    }
    					} else {
    						
    						return new Token(TokenType.floatNum, input.substring(lexemeBegin, checkPoint+1), lineNumber);
    					}
					}
            		
            		return new Token(TokenType.floatNum, input.substring(lexemeBegin, checkPoint+1), lineNumber);               	
				} else if(Character.isDigit(input.charAt(forward)) && forward == input.length()-1) {
					
					// the control reaches here if the previous character('.') is preceded by digit, which is last in the input.
					
					forward++;
					return new Token(TokenType.floatNum, input.substring(lexemeBegin, forward), lineNumber);					
				} else {
					
					// the control reaches here if the previous character('.') is not preceded by required characters for the FLOAT token.
					
					forward--;
					return new Token(TokenType.intNum, input.substring(lexemeBegin, forward), lineNumber);
				}
            } else {
          
                return new Token(TokenType.intNum, input.substring(lexemeBegin, forward), lineNumber);                
            }
        }
        return new Token(TokenType.intNum, input.substring(lexemeBegin, forward), lineNumber);
    }

    /**
     * Iterates through the given input until n unless a valid token is found
     * @param lineNumber Line number in the source program
     * @param input Input String
     * @return list of valid tokens in the provided input
     */
    public static List<Token> nextToken(Integer lineNumber, String input) {
        List<Token> tokens = new ArrayList<Token>();
        Util util = new Util();
        
        for(int currentChar = 0; currentChar < input.length(); ) {
        	
            switch(input.charAt(currentChar)) {
            
            case '=':
                currentChar++;
                if (input.charAt(currentChar)=='=') {
					
                	tokens.add(new Token(TokenType.operator, "eq", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.operator, "=", lineNumber));
				}
                break;
                
            case '<':
                currentChar++;
                if (input.charAt(currentChar)=='>') {
					
                	tokens.add(new Token(TokenType.operator, "neq", lineNumber));
                	currentChar++;
				} else if(input.charAt(currentChar)=='=') {
					
                	tokens.add(new Token(TokenType.operator, "leq", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.operator, "lt", lineNumber));
				}
                break;
                
            case '>':
                currentChar++;
                if (input.charAt(currentChar)=='=') {
					
                	tokens.add(new Token(TokenType.operator, "geq", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.operator, "gt", lineNumber));
				}
                break;
                
            case ';':
                tokens.add(new Token(TokenType.puntuation, ";", lineNumber));
                currentChar++;
                break;
                
            case ',':
                tokens.add(new Token(TokenType.puntuation, ",", lineNumber));
                currentChar++;
                break;
                
            case '.':
                tokens.add(new Token(TokenType.puntuation, ".", lineNumber));
                currentChar++;
                break;
                
            case ':':
                currentChar++;
                if (input.charAt(currentChar)==':') {
					
                	tokens.add(new Token(TokenType.operator, "sr", lineNumber));
                	currentChar++;
				} else {
					tokens.add(new Token(TokenType.puntuation, ":", lineNumber));
				}
                break;
                
            case '(':
                tokens.add(new Token(TokenType.puntuation, "(", lineNumber));
                currentChar++;
                break;
                
            case ')':
                tokens.add(new Token(TokenType.puntuation, ")", lineNumber));
                currentChar++;
                break;
                
            case '{':
                tokens.add(new Token(TokenType.puntuation, "{", lineNumber));
                currentChar++;
                break;
                
            case '}':
                tokens.add(new Token(TokenType.puntuation, "}", lineNumber));
                currentChar++;
                break;
                
            case '[':
                tokens.add(new Token(TokenType.puntuation, "[", lineNumber));
                currentChar++;
                break;
                
            case ']':
                tokens.add(new Token(TokenType.puntuation, "]", lineNumber));
                currentChar++;
                break;
                
            case '+':
                tokens.add(new Token(TokenType.operator, "+", lineNumber));
                currentChar++;
                break;
               
            case '-':
                tokens.add(new Token(TokenType.operator, "-", lineNumber));
                currentChar++;
                break;
                
            case '*':
                tokens.add(new Token(TokenType.operator, "*", lineNumber));
                currentChar++;
                break;
                
            case '/':
            	tokens.add(new Token(TokenType.operator, "/", lineNumber));
                currentChar++;
                break;
                
            default:
                if(Character.isWhitespace(input.charAt(currentChar))) {
                    currentChar++;
                } else if (Character.isLetter(input.charAt(currentChar))) {
                	
                    token = alphaLexeme(input, currentChar, lineNumber);
                    currentChar += token.getTokenValue().length();
                    tokens.add(token);
                } else if (Character.isDigit(input.charAt(currentChar)) && input.charAt(currentChar)=='0' && currentChar<input.length()-1) {
                	
                	currentChar++;
                	if (input.charAt(currentChar)=='.') {
						
                		currentChar--;
                        token = numericLexeme(input, currentChar, lineNumber);
                        currentChar += token.getTokenValue().length();
                        tokens.add(token);
					} else {

						tokens.add(new Token(TokenType.intNum, "0", lineNumber));
					}                  
                } else if (Character.isDigit(input.charAt(currentChar))) {

                    token = numericLexeme(input, currentChar, lineNumber);
                    currentChar += token.getTokenValue().length();
                    tokens.add(token);
                } else {
                	util.setMap(map);
                map = util.reportError(lineNumber, "Error ("+Character.toString(input.charAt(currentChar))+") reported during lexical phase in line ");
                	currentChar++;
                }
                break;
            }
        }
        return tokens;
    }
    
    /**
     * The main function that calls the nextToken() function repeatedly to generate all the tokens for the given input. Once it hits the end of source program, it outputs all the tokens in the output file and encountered errors in the error file.
     * @throws IOException
     */
    public static void lexicalAnalyzer() throws IOException {
    	
    	System.out.println("Fetching input......");
    	System.out.println("Recording tokens......");
    	TreeMap<Integer, String> inputList = DataReadWrite.readInput();
    	map = new Util().getMap();
    	List<Token> receivedTokens=null;
    	int tokenCount=0;
    	
    	try {
    		
    		Iterator<Map.Entry<Integer, String>> entrySet = inputList.entrySet().iterator();
    		Map.Entry<Integer, String> entry;
    		String aToCc="";
    		while (entrySet.hasNext()) {
				
    			
    			entry = entrySet.next();
    			if (entry.getValue()!=null) {
    				
    				receivedTokens = nextToken(entry.getKey(), entry.getValue());
                    for(Token token : receivedTokens) {
                        //System.out.println(token);
                        if (token.type.toString().equalsIgnoreCase("id") || token.type.toString().equalsIgnoreCase("floatNum") || token.type.toString().equalsIgnoreCase("intNum")) {
                        	aToCc+=token.type.toString()+" ";
						} else {
							aToCc+=token.tokenValue+" ";
						}
                        
                        tokenCount++;
                        outputTokens.add(token);
                    }                  
				}
			}
    		
    		System.out.println("Token Stream: ");
    		System.out.println(aToCc.substring(0, aToCc.length()));
    		aToCcFormat.add(aToCc.substring(0, aToCc.length()));
    		DataReadWrite.writeOutput(outputTokens);
    		DataReadWrite.writeAToCc(aToCcFormat);
    		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No content in input file");
		}
        System.out.println("Total valid tokens: "+tokenCount);
        System.out.println();
        System.out.println("End of Lexical Phase....... Initiating Syntactic Analyzer");
        System.out.println();
    }    

}

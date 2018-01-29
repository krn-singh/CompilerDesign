package compiler.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeMap;

import compiler.lexical.Token;

/**
 * class acts as a medium between lexical analyzer and the data files.
 * @author Karan
 * @version 1.0
 */
public class DataReadWrite {

	/**
	 * Reads the input and omits the the slash-slash and slash-star comments
	 * @return a collection of source code with their respective line number in the input file
	 * @throws IOException
	 */
	public static TreeMap<Integer, String> readInput() throws IOException {

		readInput = new BufferedReader(new InputStreamReader(new FileInputStream(INPUT_FILE_PATH)));
		String line = "";
		int lineNumber = 0;
		TreeMap<Integer, String> input = new TreeMap<>();
		try {

			while ((line = readInput.readLine()) != null) {

				lineNumber++;
				exit: for (int currentChar = 0; currentChar < line.length(); currentChar++) {

					switch (line.charAt(currentChar)) {

					case '/':
						currentChar++;
						if (line.charAt(currentChar) == '/') {

							line = line.substring(0, currentChar - 1);

						} else if (line.charAt(currentChar) == '*') {

							String intialLine = line;
							int currentLineLength = line.length();

							// Consuming Input before the start of slash-star comment
							line = line.substring(0, currentChar - 1);
							input.put(lineNumber, line);
							lineNumber++;

							// Consuming the input after the slash-star comment in the same line
							if (intialLine.substring(currentChar + 1, currentLineLength).contains("*/")) {
								line = line + " " + intialLine.substring(intialLine.indexOf("*/") + 2);
								lineNumber--;
								break;
							}

							while ((line = readInput.readLine()) != null && (!line.contains("*/"))) {
								lineNumber++;
							}

							if (line != null) {

								if (line.contains("*/")) {

									line = line.substring(line.indexOf("*/") + 2);
								}
							} else {
								break exit;
							}
						}
						break;

					default:
						break;
					}
				}

				input.put(lineNumber, line);
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			readInput.close();
		}

		return input;
	}

	/**
	 * Writes the output to the output.txt file
	 * @param tokens Data structure containing the token information (token type, token value, location in source code)
	 * @throws IOException
	 */
	public static void writeOutput(List<Token> tokens) throws IOException {
	
		printOutput = new PrintWriter(new FileOutputStream(OUTPUT_FILE_PATH));
		try {
	
			for (Token token : tokens) {
	
				printOutput.println(token);
			}
		} catch (Exception e) {
	
			e.printStackTrace();
		} finally {
	
			printOutput.close();
		}
	
	}

	/**
	 * Outputs the token type in AToCc format
	 * @param aToCcFormat List containing the token type in AToCc format
	 * @throws IOException
	 */
	public static void writeAToCc(List<String> aToCcFormat) throws IOException {

		printAToCc = new PrintWriter(new FileOutputStream(ATOCC_FILE_PATH));
		try {

			for (String aToCc : aToCcFormat) {

				printAToCc.println(aToCc);
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			printAToCc.close();
		}

	}
	
	/**
	 * Prints the error messages in the error.txt file
	 * @param errors List of errors encountered in the given input
	 * @throws IOException
	 */
	public static void writeErrors(List<String> errors) throws IOException {

		printError = new PrintWriter(new FileOutputStream(ERROR_FILE_PATH));
		try {

			for (String error : errors) {

				printError.println(error);
			}
		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			printError.close();
		}

	}

	private static BufferedReader readInput;
	private static PrintWriter printError;
	private static PrintWriter printOutput;
	private static PrintWriter printAToCc;
	private static final String INPUT_FILE_PATH = "data\\input.txt";
	private static final String ERROR_FILE_PATH = "data\\error.txt";
	private static final String OUTPUT_FILE_PATH = "data\\output.txt";
	private static final String ATOCC_FILE_PATH = "data\\aToCc.txt";

	
}

package compiler.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.TreeMap;

import compiler.lexical.Token;

public class DataReadWrite {

	private static BufferedReader readInput;
	private static PrintWriter printError;
	private static PrintWriter printOutput;
	private static final String INPUT_FILE_PATH = "data\\input.txt";
	private static final String ERROR_FILE_PATH = "data\\error.txt";
	private static final String OUTPUT_FILE_PATH = "data\\output.txt";

	/*
	 * public static List<String> readInput() throws IOException {
	 * 
	 * String input = ""; readInput = new BufferedReader(new InputStreamReader(new
	 * FileInputStream(INPUT_FILE_PATH))); List<String> inputList = new
	 * ArrayList<String>(); try {
	 * 
	 * while ((input = readInput.readLine()) != null) { inputList.add(input); } }
	 * catch (Exception e) {
	 * 
	 * e.printStackTrace(); } finally {
	 * 
	 * readInput.close(); }
	 * 
	 * return inputList; }
	 */

	public static TreeMap<Integer, String> readInput() throws IOException {

		readInput = new BufferedReader(new InputStreamReader(new FileInputStream(INPUT_FILE_PATH)));
		String line = "";
		int lineNumber = 0;
		TreeMap<Integer, String> input = new TreeMap<>();
		try {

			while ((line = readInput.readLine()) != null) {

				lineNumber++;
				exit:
				for (int currentChar = 0; currentChar < line.length(); currentChar++) {

					switch (line.charAt(currentChar)) {

					case '/':
						currentChar++;
						if (line.charAt(currentChar) == '/') {

							line = line.substring(0, currentChar - 1);

						} else if (line.charAt(currentChar) == '*') {

							String intialLine = line;
							int currentLineLength = line.length();
							
							line = line.substring(0, currentChar - 1);
							input.put(lineNumber, line);
							lineNumber++;
							
							line = intialLine.substring(currentChar +1, currentLineLength);
							if (line.contains("*/")) {
								line = line.substring(line.indexOf("*/") + 2);
								lineNumber--;
								break;
							}
							
							while ((line = readInput.readLine()) != null && (!line.contains("*/"))) {
								lineNumber++;
							}

							if (line!=null) {
								
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

	public static void writeOutput(Token token) throws IOException {

		printOutput = new PrintWriter(new FileOutputStream(OUTPUT_FILE_PATH));
		try {

			printOutput.println(token);

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			printOutput.close();
		}

	}

}

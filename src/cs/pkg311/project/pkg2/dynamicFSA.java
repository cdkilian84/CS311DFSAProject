/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.pkg311.project.pkg2;

/**
 *
 * @author Chris
 */
//Christopher Kilian
//CS 311 - Winter 2017
//Project #2 - Dynamic Finite State Automata

/*

*/


//Christopher Kilian
//CS 311 - Winter 2017
//Project #2 - Dynamic Finite State Automata

/*
To compile this program from the command line, use the command:
javac dynamicFSA.java

To run this program from a command line, use the statement:
java dynamicFSA <Input file 1> <Input file 2>

For the provided input files this will read:
java dynamicFSA Proj2_Input1.txt Proj2_Input2.txt

Ensure of course that the input text files are in the same directory as the class file being run.

This program will read in two files and save their transition data for the valid identifiers into three separate arrays. The program can recognize whether an identifier has been seen before or not,
and will output the identifier with an appropriate endmarker based on what type it is. The first input file is assumed to be the "reserved" words file for the language at hand, and any time one of these
words is read from a later file, it will be output with the endmarker "*". Any identifier which are newly seen in the second input file will initially have the endmarker "?". If these identifiers are seen again
at any point, they will from that point forward have the endmarker "@".

*/


import java.io.*;
import java.util.*;
import java.util.regex.*;

public class dynamicFSA {
	int INIT_ALPHABET_LENGTH = 54; //length of all possible initial characters for an identifier (a...z both upper and lower case, underscore, and $)
	int INIT_ARR_SIZE = 5000;
	String CHAR_MAP_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_$";
	int[] switchVals = new int[INIT_ALPHABET_LENGTH];
	char[] symbol = new char[INIT_ARR_SIZE]; //array to store the "seen" identifiers so far
	int[] next = new int[INIT_ARR_SIZE]; //index values in "symbol" and "next" are paired together
	Map<Character, Integer> switchMap = new HashMap<Character, Integer>(); //maps character values to index values in the "switchVals" list
	Pattern validIdentPattern = Pattern.compile("[a-zA-Z_$][a-zA-Z_0-9$]*"); //pattern for checking if a given token is a valid identifier - use a Pattern declaration here to save on runtime by only compiling the regex once
	Pattern tokenizer = Pattern.compile("[^a-zA-Z_0-9$]+"); //pattern to be used as the delimiter to tokenize the input file, eliminating whitespaces, punctuation, and other unwanted input values
	int symbolEndPointer = 0; //this global value will always point to the next available open space in the "symbol" array (for ease of access and checking when to double size)
	
	
	//main just grabs the input file names and passes them to the constructor
	public static void main(String[] args){
		if(args.length != 2){
			exit();
		}else{
			new dynamicFSA(args[0], args[1]);
		}
	} 
	//end main
	
	
	//Constructor handles the primary method calls for the functioning of this program, as well as initialization processes.
	public dynamicFSA(String inputFile1, String inputFile2){
		//initialize the switchVals array and next array so all entries have value -1:
		Arrays.fill(switchVals, 0, switchVals.length, -1);
		Arrays.fill(next, 0, next.length, -1);
		initCharMap(); //initialization of character map tied to the "switchVals" array
		String identifierOutput = "";
		
		//read in the first file of identifiers:
		identifierOutput = readInput(inputFile1, '*'); //first file expects reserved identifiers, so pass "*" as marker
		System.out.print(identifierOutput);
		System.out.println();
		//read in second file of identifiers
		identifierOutput = readInput(inputFile2, '?'); //second file new markers identified with a "?"
		System.out.print(identifierOutput);
		System.out.println();
		
		outputArrays();
	}
	//end constructor dynamicFSA
	
	
	//method to initialize the charMap which maps character values to their index values in the "switchVals" array
	private void initCharMap(){
		for(int i = 0; i < CHAR_MAP_STRING.length(); i++){
			switchMap.put(CHAR_MAP_STRING.charAt(i), i);
		}
	}
	//end method initCharMap
	
	
	//Method which reads in the files line by line, tokenizes the lines using a scanner and the regex's defined in the class level variables, and then passes those tokenized identifiers
	//on to the methods which will check whether these identifiers have been seen before or not.
	//The method will return a string which contains the identifiers that were discovered along with their appropriate endmarkers --> the passed char value to this method
	//is the expected "new" identifier endmarker (for this project, either "*" or "?").
	private String readInput(String inputFileName, char newMarker){
		Scanner inputScanner = null;
		String readLine = "";
		String output = "";
		
		try{
			inputScanner = new Scanner(new File(inputFileName));
		} catch(FileNotFoundException x){
			System.out.println("The reserved words input file could not be found!");
			exit();
		}
		
		while(inputScanner.hasNextLine()){
			readLine = inputScanner.nextLine();
			Scanner lineScanner = new Scanner(readLine);
			lineScanner.useDelimiter(tokenizer);
			String outputLine = "";
			while (lineScanner.hasNext()){
				String token = lineScanner.next();
				//check if the token is a valid marker (IE, does it start with a-z, A-Z, _, or $) --> if it doesn't, checkToken method takes no action since token is outside of the vocabulary and should not be extracted from the input file
				if(validIdentPattern.matcher(token).matches()){ 
					char marker = checkToken(token, newMarker);
					outputLine += token + marker + " ";
				}
			}
			if(outputLine.length() != 0){
				output += outputLine + "\n";
			}
			lineScanner.close();
		}
		inputScanner.close();
		
		return output;
	}
	//end method readInput
	
	
	//returns the next symbol in the token string. If there are no more values in the string past the provided index, & is returned as a default "end of string" marker
	private char getNextSymbol(String token, int index){
		char nextSymbol = '&';
		if(index < token.length()){
			nextSymbol = token.charAt(index);
		}
		return nextSymbol;
	}
	//end method getNextSymbol
	
	
	//Method which checks the given token string to see if it's an identifier that has been seen before or not. If not, it will call the method to create a new identifier entry in the transition
	//lists. This method returns the character which is the appropriate endmarker for whether an identifier has been seen before or not.
	private char checkToken(String token, char newMarker){
		int ptr = switchVals[switchMap.get(token.charAt(0))];
		char endMarker = '&'; //& is a dummy value here, since endMarker should only be returned as "*", "?" or "@"
		
		if(ptr == -1){
			createEntry(token, newMarker, ptr, 0);
			endMarker = newMarker;
		}else{
			int i = 1;
			char nextChar = getNextSymbol(token, i);
			boolean exit = false;
			while(!exit){
				if(symbol[ptr] == nextChar){
					ptr++;
					i++;
					nextChar = getNextSymbol(token, i);
				}else if(nextChar == '&'){ //check for end of string marker on the token string
					if(symbol[ptr] == '*'){
						endMarker = '*';
						exit = true;
					}else if(symbol[ptr] == '?'){
						endMarker = '@';
						exit = true;
					}else if(next[ptr] != -1){
						ptr = next[ptr];
					}else{
						createEntry(token, newMarker, ptr, i);
						endMarker = newMarker;
						exit = true;
					}
				}else if(next[ptr] != -1){
					ptr = next[ptr];
				}else{
					createEntry(token, newMarker, ptr, i);
					endMarker = newMarker;
					exit = true;
				}
			}
		}
		
		return endMarker;
	}
	//end method checkToken
	
	
	//Method to create a new entry in the transition lists. When this is called, the pointer and tokenIndex values passed to it should be in the correct positions for adding to the list.
	//IE, if the identifier "adjust" needs to be added, but "adjacent" already exists in the transition list, the tokenIndex should be pointing to "u" in "adjust", and the pointer should be pointing
	//to the next available entry space in the transition lists. These actions are handled by the "check" method, however they are double checked for in this method to ensure proper pointer positioning.
	private void createEntry(String token, char marker, int ptr, int tokenIndex){
	
		if(ptr == -1){ //case where entirely new identifier needs to be added, including setting the initial char value in the switch array
			switchVals[switchMap.get(token.charAt(0))] = symbolEndPointer; //symbolEndPointer always points to next available space in "symbol" array, so reference that index in switchVals for a new initial char value
			tokenIndex = 1;
			ptr = symbolEndPointer;
		}else if(symbol[ptr] != 0){ //check that ptr is pointing to the next available open position
			boolean exit = false;
			while(!exit){
				if(next[ptr] != -1){
					ptr = next[ptr];
				}else{
					if(symbol[ptr] != 0){
						next[ptr] = symbolEndPointer;
						ptr = symbolEndPointer;
					}
					exit = true;
				}
			}
		}
		
		boolean exit = false;
		while(!exit){ //no need to check for symbol[ptr] == 0. By the time code execution reaches here ptr must be pointing to a symbol value of 0 --> the end of the filled portion of the symbol array in other words
			char tokenSymbol = getNextSymbol(token, tokenIndex);
			if(tokenSymbol != '&'){
				symbol[ptr] = tokenSymbol; //add remainder of identifier that doesn't already exist in transition list to end of transition list
				tokenIndex++;
			}else{
				symbol[ptr] = marker;
				exit = true;
			}
			ptr++;
		}
		
		symbolEndPointer = ptr; //after creation operation is complete, ptr is pointing to the next empty space in the "symbol" array - ensure "symbolEndPointer" always points to that position
		
		//after each addition of an identifier to the symbol array, check to see if the array size needs to be increased - never let it go beyond 75% full without increasing size
		if(symbolEndPointer > (symbol.length * 0.75)){
			increaseArraySize(); //double size of both "next" and "symbol" arrays so they never run out of space
		}
		
	}
	//end method createEntry
	
	
	//method to output a formatted version of the 3 storage arrays for the dynamicFSA
	//Prints out 20 array elements to a row for easier reading
	private void outputArrays(){
		boolean exit = false;
		int i = 0;
		String lineNameBlank = "";
		String lineNameSymbol = "symbol:";
		String lineNameNext = "next:";
		
		printSwitchArray();
		
		while(i < symbol.length && symbol[i] != 0){
			System.out.println();
			System.out.printf("%7s", lineNameBlank);
			for(int j = 0; j < 20; j++){
				if((i+j) < symbol.length){
					System.out.printf("%6d", (i+j));
				}
			}
			System.out.println();
			System.out.printf("%-7s", lineNameSymbol);
			for(int j = 0; j < 20; j++){
				if((i+j) < symbol.length){
					System.out.printf("%6c", symbol[i+j]);
				}
			}
			System.out.println();
			System.out.printf("%-7s", lineNameNext);
			for(int j = 0; j < 20; j++){
				if((i+j) < symbol.length){
					if(next[i+j] == -1){
						System.out.printf("%6s", lineNameBlank);
					}else{
						System.out.printf("%6d", next[i+j]);
					}
				}
			}
			System.out.println();
			i+= 20;//increment i by 20 for each iteration of the loop ---> 20 items printed per line
		}
		
	}
	//end method outputArrays
	
	
	//helper method for the "outputArrays" method which just prints out the switch array and the values it maps to.
	//This helper method is used since the switch array printout requires very specific formatting, and this just splits it out of the main "outputArrays" method
	//to keep from clogging it up.
	private void printSwitchArray(){
		String lineNameBlank = "";
		String lineNameSwitch = "switch:";
		
		//first print "switch" array which is a known small size:
		System.out.printf("%7s", lineNameBlank);
		for(int j = 0; j < 20; j++){
			System.out.printf("%6c", CHAR_MAP_STRING.charAt(j));
		}
		System.out.println();
		System.out.printf("%-5s", lineNameSwitch);
		for(int j = 0; j < 20; j++){
			System.out.printf("%6d", switchVals[j]);
		}
		System.out.println();
		System.out.printf("%7s", lineNameBlank);
		for(int j = 20; j < 40; j++){
			System.out.printf("%6c", CHAR_MAP_STRING.charAt(j));
		}
		System.out.println();
		System.out.printf("%-5s", lineNameSwitch);
		for(int j = 20; j < 40; j++){
			System.out.printf("%6d", switchVals[j]);
		}
		System.out.println();
		System.out.printf("%7s", lineNameBlank);
		for(int j = 40; j < switchVals.length; j++){
			System.out.printf("%6c", CHAR_MAP_STRING.charAt(j));
		}
		System.out.println();
		System.out.printf("%-5s", lineNameSwitch);
		for(int j = 40; j < switchVals.length; j++){
			System.out.printf("%6d", switchVals[j]);
		}
		System.out.println();
	}
	//end method printSwitchArray
	
	
	
	//method to double the size of both the "next" and "symbol" arrays (both because they must be the same size)
	private void increaseArraySize(){
		System.out.println("Doubling size of arrays!!!");
		int newSize = symbol.length * 2;
		char[] tempSymbol = new char[newSize];
		int[] tempNext = new int[newSize];
		Arrays.fill(tempNext, 0, tempNext.length, -1); //make sure bigger version of "next" will still have -1 as default values
		
		System.arraycopy(symbol, 0, tempSymbol, 0, symbol.length); //copy the entire "symbol" array into the new "tempSymbol" array in the same index positions.
		symbol = tempSymbol;
		System.arraycopy(next, 0, tempNext, 0, next.length); //copy the entire "next" array into the new "tempNext" array in the same index positions.
		next = tempNext;
	}
	//end method increaseArraySize
	
	
	//exit method displays proper program usage before exiting program
	public static void exit(){
		System.out.println("Usage: java dynamicFSA <Input txt file 1> <Input txt file 2>");
		System.exit(0);
	} 
	//end method exit
	
}
//End class dynamicFSA


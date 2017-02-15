package havabol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Scanner
{

	public String sourceFileNm 				= null;
	public ArrayList<String> sourceLineM 	= new ArrayList<String>();
	public SymbolTable symbolTable 			= null;
	public char[] textCharM 				= null;
	public int iSourceLineR 				= 1;
	public int iColPos 						= 0;
	public Token currentToken  				= new Token();
	public Token nextToken 					= null;
	
	private final static String DELIMITERS = " \t;:()\'\"=!<>+-*/[]#^\n,";
	private final static String OPERATORS = "<>+-*/=!#^";
	private static final String[] ACCEPTED_OPERATORS = {"++", "--", "+=", "-=", "*=", "/=", "<=", ">=", "==", "!=", "//"};
	
	public Scanner(String string, SymbolTable symbolTable) throws IOException, ScannerTokenFormatException
	{
		// Set the values passed.
		this.sourceFileNm = string;
		this.symbolTable = symbolTable;
		
		// Open the file and read lines into the sourceLineM.
		java.util.Scanner in = new java.util.Scanner(new File(string));
		while(in.hasNext())
		{
			this.sourceLineM.add(in.nextLine());
		}
		in.close();
		
		// We need to set our first line character array assuming we have lines.
		setTextCharM();
		
		// Set nextToken
		this.nextToken = createNextToken();
	}

	/**
	 * This takes no arguments and returns a String representation of the next token.
	 * It should copy the nextTOken field to currentToken and create the next token.
	 * @return String representing next token
	 * @throws Exception
	 */
	public String getNext() throws ScannerTokenFormatException
	{
		// If the nextToken which is about to replace our currentToken marks
		// the beginning of a new line, then we print all lines between our currentToken
		// source line up to and including the source line with our nextToken.
		//
		// This _should_ result in blank source lines being printed resulting in
		// something like the following appearing in our scanner output.
		//
		// SEPARATOR -      ;
		//  4
		//  5 Int x = 3;
		// OPERAND IDENTIFIER Int
		if(this.currentToken.iSourceLineNr < this.nextToken.iSourceLineNr &&
		   this.nextToken.iSourceLineNr <= this.sourceLineM.size())
		{
			for(int lineNum = this.currentToken.iSourceLineNr; lineNum <= this.nextToken.iSourceLineNr - 1; lineNum++)
			{
				System.out.println("  " + (lineNum + 1) + " " + this.sourceLineM.get(lineNum));	
			}
		}
		this.currentToken = copyToken(this.nextToken);
		this.nextToken = createNextToken();
		return this.currentToken.tokenStr;
	}
	
	/**
	 * This takes the token passed as a parameter and creates a copy of that token 
	 * and returns it.
	 * @param oldToken
	 * @return
	 */
	private Token copyToken(Token oldToken)
	{
		Token scRtToken = new Token();
		
		scRtToken.tokenStr 		= oldToken.tokenStr;
		scRtToken.primClassif 	= oldToken.primClassif;
		scRtToken.subClassif 	= oldToken.subClassif;
		scRtToken.iColPos		= oldToken.iColPos;
		scRtToken.iSourceLineNr	= oldToken.iSourceLineNr;
		
		return scRtToken;
	}
	
	/**
	 * This function scans input for a new Token and returns it. It returns a token
	 * representing EOF if no more lines can be scanned. It throws an exception for
	 * malformed tokens, i.e. a string literal which is not closed the line on which
	 * it begins. 
	 * 
	 * @return Token scRtToken representing the next token we've found.
	 * @throws Exception 
	 */
	private Token createNextToken() throws ScannerTokenFormatException
	{
		// The token object we're returning.
		Token scRtToken = new Token();
		
		// As long as we have more tokens to scan, find and format them.
		if(findNextToken())
		{
			// Set the column and line number of the found token.
			scRtToken.iColPos = this.iColPos;
			scRtToken.iSourceLineNr = this.iSourceLineR;
							
			// It's time to determine what we do based on a bit of context and knowledge
			// of our language.
							
			// First let's handle Strings.
			if(this.textCharM[this.iColPos] == '\'' || this.textCharM[this.iColPos] == '\"')
			{
				formatStringToken(scRtToken);
			}
			// Now let's handle numbers we find. It's important to note that variable and
			// function names cannot begin with numeric characters. If the first character
			// of a token is an integer then it must be an Int or Float. Any other characters that aren't delimiters which are found
			// represent an error.
			else if(Character.isDigit(this.textCharM[this.iColPos]))
			{
				formatNumericToken(scRtToken);
			}
			// Now let's handle operators.
			else if(Scanner.OPERATORS.indexOf(this.textCharM[this.iColPos]) >= 0)
			{
				formatOperatorToken(scRtToken);
				if(scRtToken.tokenStr.equals("//")){
					scRtToken.tokenStr = "";
					this.iColPos = 0;
					this.iSourceLineR++;
					setTextCharM();
					return(createNextToken());
					
				}
			}
			// Now let's handle our separatorsS.
			else if(Scanner.DELIMITERS.indexOf(this.textCharM[this.iColPos]) >= 0)
			{
				formatDelimiterToken(scRtToken);
			}
			// Now we can handle operands. These must begin with a letter which may be
			// followed by any number of characters. These are terminated at end of line
			// or by a delimiter.
			else if(Character.isLetter(this.textCharM[this.iColPos]))
			{
				formatOperandToken(scRtToken);
			}
			// We cannot classify this token and must alert the user.
			else
			{
				throw new ScannerTokenFormatException("SCANNER ERROR: token on line " + 
						scRtToken.iSourceLineNr + " at column " + scRtToken.iColPos +
						" cannot be classified");
			}
		} // No new tokens could be scanned (end of file).
		else
		{
			// No token was found so we can make this an EOF token, set its fields and return it.
			scRtToken.primClassif = Token.EOF;
			scRtToken.iSourceLineNr = this.iSourceLineR;
			scRtToken.iColPos = this.iColPos;
		}
		return scRtToken;
	}

	/**
	 * Increment through unnecessary whitespace and lines. After hitting end of line,
	 * we need to populate the textCharM character array with the next one.
	 **/
	private Boolean findNextToken()
	{
		// Loop through lines.
		while(this.iSourceLineR <= this.sourceLineM.size())
		{
			// Loop through whitespace.
			while(this.iColPos < this.textCharM.length)
			{
				// "Eat" unnecessary whitespace. 
				if(this.textCharM[this.iColPos] == ' ' 	||
				   this.textCharM[this.iColPos] == '\t' ||
				   this.textCharM[this.iColPos] == '\n')
				{
					this.iColPos++;
				}
				// Return when we find the beginning of a token.
				else
				{
					return true;
				}
			}
			// Increment the line count, reseet iColPos and copy the next line to textCharM
			// if there's more to scan.
			this.iSourceLineR++;
			this.iColPos = 0;
			setTextCharM();
		}
		// Represents that no more tokens could be located.
		return false;
	}
	
	/**
	 * Format a String token.
	 * @param token
	 * @throws Exception
	 */
	private void formatStringToken(Token token) throws ScannerTokenFormatException
	{
		// Set the classification fields fields.
		token.primClassif	= Token.OPERAND;
		token.subClassif 	= Token.STRING;
						
		// Set the delimiter for this String.
		char scStrTokenDelim = this.textCharM[this.iColPos];
				
		// Initialize the StringBuilder we'll use for building our token String.
		StringBuilder scTokenStrBldr = new StringBuilder();
					
		// Now we need to loop and add characters to our StringBuilder. We can loop under the
		// following conditions.
		//
		// 1. We hit end of line. If our String has not been terminated by then, this is an error.
		// 2. We hit our String's delimiter AND it has not been escaped.
		this.iColPos++;
		while(true)
		{
			// We've hit end of line and need to throw an error.
			if(this.iColPos >= this.textCharM.length)
			{
				throw new ScannerTokenFormatException("SCANNER ERROR: String token which begins on line " + 
					token.iSourceLineNr + " at column " + token.iColPos +
					" is not terminated in-line");
			}
			// We've hit our delimiter and need to check if it's escaped.
			else if(this.textCharM[this.iColPos] == scStrTokenDelim && this.textCharM[this.iColPos - 1] != '\\')
			{
				this.iColPos++;
				break;
			}
			// We're not at EOF and our delimiter was escaped so we can append whatever character we've
			// found to our String.
			else
			{
				scTokenStrBldr.append(this.textCharM[this.iColPos]);
				this.iColPos++;
			}
		}
		// Copy the token string.
		token.tokenStr = scTokenStrBldr.toString();	
	}
	
	/**
	 * Format a numeric token.
	 * @param token
	 * @throws Exception
	 */
	private void formatNumericToken(Token token) throws ScannerTokenFormatException
	{
		// We're at a numeric value so we can classify this Token as an operand and set its
		// sub classification to INTEGER. This can be changed later if we encounter a period.
		token.primClassif = Token.OPERAND;
		token.subClassif = Token.INTEGER;
						
		// Initialize the StringBuilder we'll use for building our token String.
		StringBuilder scTokenStrBldr = new StringBuilder();
						
		// Now we need to loop until we hit end of line or a delimiter.
		while(true)
		{
			// We're at end of line or find a delimieter, thenand can return the token
			// string.
			if(this.iColPos >= this.textCharM.length || 
			   Scanner.DELIMITERS.indexOf(this.textCharM[this.iColPos]) >= 0)
			{
				break;
			}
			// A second period indicates a malformed token.
			else if(this.textCharM[this.iColPos] == '.' && token.subClassif == Token.FLOAT)
			{
				throw new ScannerTokenFormatException("SCANNER ERROR: Float token which begins on line " + 
					token.iSourceLineNr + " at column " + token.iColPos +
					" is malformed");
			}
			// Our first period means We can change the type to Token.FLOAT.
			else if(this.textCharM[this.iColPos] == '.')
			{
				token.subClassif = Token.FLOAT;
				scTokenStrBldr.append('.');
				this.iColPos++;
			}
			// Any digits can simply be appended.
			else if(Character.isDigit(this.textCharM[this.iColPos]))
			{
				scTokenStrBldr.append(this.textCharM[this.iColPos]);
				this.iColPos++;
			}
			// Any characters which aren't caught by the above cases are invalid.
			// These cold be an alphabetic characters or any character our language
			// specification is not designed to handle.
			else
			{
				throw new ScannerTokenFormatException("SCANNER ERROR: identifer token on line " + 
					token.iSourceLineNr + " at column " + token.iColPos +
					" begins with numeric characters");
			}
		}
		// Copy the token string.
		token.tokenStr = scTokenStrBldr.toString();
	}
	
	/**
	 * Format an operator token.
	 * @param token
	 */
	private void formatOperatorToken(Token token)
	{
		// Set the primary classification.
		token.primClassif = Token.OPERATOR;
		
		
		
		//check if next column position is not the end of string and whether it is a operator
		if((this.iColPos + 1) < this.textCharM.length && Scanner.OPERATORS.indexOf(this.textCharM[this.iColPos + 1]) >= 0 )
		{
			
			//create the compare string that is used to pass into allowedOperators, the 2 on the end is the size of the new string
			String scCompareSt = new String(this.textCharM, this.iColPos, 2);
			
			//checks if the compare string is an allowed operator, if true set the token string
			if(allowedOperator(scCompareSt))
			{
			
				token.tokenStr = scCompareSt;
				//advance the column position for the newly added character to token string 
				this.iColPos++;
				
			}else
			{
				// else the operator was not allowed set token string to the single character
				token.tokenStr = Character.toString(this.textCharM[this.iColPos]);
			}
		}else{
			
		// Copy the character to our token string.
			token.tokenStr = Character.toString(this.textCharM[this.iColPos]);
			
		}
		// Increment the column position.
		this.iColPos++;
	}
	
	/**
	 * Format a separator token.
	 * @param token
	 */
	private void formatDelimiterToken(Token token)
	{
		// Set the primary classification.
		token.primClassif = Token.SEPARATOR;
						
		// Copy the character to our token string.
		token.tokenStr = Character.toString(this.textCharM[this.iColPos]);
					
		// Increment the column position.
		this.iColPos++;
	}
	
	/**
	 * Format an operand token.
	 * @param token
	 */
	private void formatOperandToken(Token token)
	{
		// Set the primary classification.
		token.primClassif = Token.OPERAND;
					
		// Set the sub-classification.
		token.subClassif = Token.IDENTIFIER;
					
		// Initialize the StringBuilder we'll use for building our token String.
		StringBuilder scTokenStrBldr = new StringBuilder();
						
		// Append everything in the identifier to our string builder object.
		while(this.iColPos < this.textCharM.length && Scanner.DELIMITERS.indexOf(this.textCharM[this.iColPos]) == -1)
		{
			scTokenStrBldr.append(this.textCharM[this.iColPos]);
			this.iColPos++;
		}
		// Copy the string token.
		token.tokenStr = scTokenStrBldr.toString();
	}
	
	/**
	 * Fill the textCharM field with the next line to be scanned and print that line to
	 * standard out.
	 */
	private void setTextCharM()
	{
		// This action should only be taken if there are more lines.
		if(this.iSourceLineR <= this.sourceLineM.size())
		{
			this.textCharM = this.sourceLineM.get(iSourceLineR - 1).toCharArray();
		}
	}
	
	/**
	 * this function is used to check if two operators, that appear next to each other,
	 * are actually one operator
	 * @param possibleOperator
	 * @return
	 */
	private boolean allowedOperator(String possibleOperator){
		for(int i = 0; i < ACCEPTED_OPERATORS.length; i++){
			if(possibleOperator.equals(ACCEPTED_OPERATORS[i]))
				return true;
		}
		return false;
	}
}
	

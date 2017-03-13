package havabol;

import java.util.HashMap;

public class Parse 
{
	private Scanner scan;
	
	// TODO: this should and will be made into a separate class as its functionality is
	// fleshed out but for now it simply maps keys (strings representing variable names)
	// to Objects (what they represent). Their type can be gathered with instanceof (I think).
	private HashMap<String, Object> storage;
	
	public Parse(Scanner s)
	{
		this.scan = s;
		this.storage = new HashMap<String, Object>();
	}

	public void parseStmt() throws Exception
	{
		// continue until end of file
		while (this.scan.currentToken.primClassif != Token.EOF)
		{
			this.scan.getNext();
			
			// check the primary class of the current token
			switch (this.scan.currentToken.primClassif)
			{
			// in the case of debug statement set the appropriate variables
				case Token.DEBUG:
					//check first token of debug statement
					if (this.scan.currentToken.tokenStr.equals("debug")) 
					{
						this.scan.getNext();
						// parseDebugStmt will check the next two tokens and set the variables
						// it will return false if the statement is not formatted correctly
						if (parseDebugStmt())
						{
							this.scan.getNext();
							//check if the last token on the line is a ';'
							if(this.scan.currentToken.tokenStr.equals(";"))
							{
								// making sure there is no token on the line after ';'
								if(this.scan.nextToken.iSourceLineNr == this.scan.currentToken.iSourceLineNr)
								{
									throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
										this.scan.nextToken.iSourceLineNr + " at column " + this.scan.nextToken.iColPos +
										" appears after a ';' which designates the end of a statement");
								}	
							}
							else
							{
								throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
									this.scan.currentToken.iSourceLineNr + " at column " + this.scan.currentToken.iColPos +
									" expected ';' at the end of a Debug statement");
							}
						}
						else
						{
							throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
								this.scan.nextToken.iSourceLineNr + " at column " + this.scan.nextToken.iColPos +
								" is formatted incorrectly; format: Debug <type> <on/off>");
						}
					}
					else
					{
						throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
							this.scan.nextToken.iSourceLineNr + " at column " + this.scan.nextToken.iColPos +
							" is formatted incorrectly; format: Debug <type> <on/off>");
					}
					break;
				case Token.CONTROL:
					if(scan.currentToken.subClassif == Token.DECLARE)
					{
						declareStmt();
					}
					break;
			}
		}
	}

	/**
	 * This is called to parse declare statements. It matches the first two rules in
	 * the following part of our formal grammar definition:
	 * 
	 * declareStmt		:= 'Token.DECLARE' assignmentStmt
	 * assignmentStmt 	:= 'Token.IDENTIFIER' assignmentStmt'
	 * assignmentStmt' 	:= 'Token.OPERATOR' expressionStmt ';'
	 * 					|  ';'
	 * 
	 * and will return after a storage object and symbol table entry are created from
	 * the Token.DECLARE and Token.IDENTIFIER. assignmentStmt() will handle assignmentStmt'.
	 * 
	 * In order for assignmentStm() to be called and parsed correctly this function
	 * needs to increment the scanner so scan.currentToken is the identifier.
	 * 
	 * @return a boolean true if it succeeds or false otherwise
	 * @throws Exception representing if there is an error in parsing or thrown by the
	 * scanner
	 */
	private boolean declareStmt() throws Exception 
	{
		// See if we have a valid variable name to perform the declare
		// or throw an exception.
		if(scan.nextToken.primClassif != Token.IDENTIFIER)
		{
			error("expected a valid variable name for declar");
		}

		// Check to see if the identifier is linked to an existing variable.
		if(scan.symbolTable.getEntry(scan.nextToken.tokenStr) != null)
		{
			error("variable name already in use at tiem of declaration");
		}
		
		// Create the object we'll use for storage.
		//
		// TODO: for now we're using a Numeric for numeric Int and Float values,
		// Boolean for Bool, and StringBuilder for Strings. This does not 
		// handle arrays.
		Object stObject = null;
		STIdentifier stEntry = new STIdentifier(scan.nextToken.tokenStr, Token.IDENTIFIER);
		if(scan.currentToken.tokenStr.equals("Bool"))
		{
			stObject = new Boolean(false);
			stEntry.dclType = Token.BOOLEAN;
		}
		else if(scan.currentToken.tokenStr.equals("Int"))
		{
			stObject = new Numeric();
			stEntry.dclType = Token.INTEGER;
		}
		else if(scan.currentToken.tokenStr.equals("Float"))
		{
			stObject = new Numeric();
			stEntry.dclType = Token.FLOAT;
		}
		else if(scan.currentToken.tokenStr.equals("String"))
		{
			stObject = new StringBuilder();
			stEntry.dclType = Token.STRING;
		}
		else
		{
			// TODO: This represents either an invalid or unsupported type and
			// is likely an error because I didn't code the interpreter correclty.
			error("attempted to make declaration using invalid datatype");
		}
		
		storage.put(scan.nextToken.tokenStr, stObject);
		scan.symbolTable.putSymbol(scan.nextToken.tokenStr, stEntry);
		scan.getNext();
		
		return true;
	}

	/**
	 * This is passed an error message and reference objects and throws a corresponding
	 * ParserException which contains the message, location of the error, and source file
	 * name.
	 * @param msg
	 * @param args
	 * @throws ParserException
	 */
	private void error(String msg, Object...args) throws ParserException
	{
		String errorMsg = String.format(msg, args);
		throw new ParserException(scan.iSourceLineR, errorMsg, scan.sourceFileNm);
	}

	/**
	 * this will check the format of a debug statement and set the appropriate 
	 * debug values. 
	 * @return rStmt; equals true if the statement is valid
	 * and false if it is invalid.
	 * @throws ScannerTokenFormatException
	 */
	private boolean parseDebugStmt() throws ScannerTokenFormatException {
		// return statement
		boolean rStmt = false;

		if (this.scan.currentToken.tokenStr.equals("bShowToken")) {
			this.scan.getNext();
			if (this.scan.currentToken.tokenStr.equals("on")) {
				this.scan.dBug.bShowToken = true;
				System.out.println(" this is bShowtoken: " + scan.dBug.bShowToken);
				rStmt = true;
			} else if (this.scan.currentToken.tokenStr.equals("off")) {
				this.scan.dBug.bShowToken = false;
				rStmt = true;
			}
		}
		if (this.scan.currentToken.tokenStr.equals("bShowExpr")) {
			this.scan.getNext();
			if (this.scan.currentToken.tokenStr.equals("on")) {
				this.scan.dBug.bShowExpr = true;
				rStmt = true;
			} else if (this.scan.currentToken.tokenStr.equals("off")) {
				this.scan.dBug.bShowExpr = false;
				rStmt = true;
			}
		}
		if (this.scan.currentToken.tokenStr.equals("bShowAssign")) {
			this.scan.getNext();
			if (this.scan.currentToken.tokenStr.equals("on")) {
				this.scan.dBug.bShowAssign = true;
				rStmt = true;
			} else if (this.scan.currentToken.tokenStr.equals("off")) {
				this.scan.dBug.bShowAssign = false;
				rStmt = true;
			}
		}
		return rStmt;

	}
	
	/*public static class Debug{

		// these variables are set to private out of habit 
		// we can change to public and get rid of the getters and setters
		public boolean bShowToken;
		public boolean bShowExpr;
		public boolean bShowAssign;
		
		public Debug(){
			this.bShowToken = false;
			this.bShowExpr = false;
			this.bShowAssign = false;
		}
	}*/
}

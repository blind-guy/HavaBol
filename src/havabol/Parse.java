package havabol;

//import havabol.HavaBol.Debug;

public class Parse {
	// Create the SymbolTable
	//private SymbolTable symbolTable = new SymbolTable();
	// scanner is passed in from havabol class
	private Scanner scan;
	// the debug will be created in constructor if it is null
	//private static Debug dBug = new HavaBol.Debug();

	public Parse(Scanner s) {
		this.scan = s;
		
	}

	public void parseStmt() throws ScannerTokenFormatException {
		// continue until end of file
		while (this.scan.currentToken.primClassif != Token.EOF) {
			this.scan.getNext();
			// check the primary class of the current token
			switch (this.scan.currentToken.primClassif) {

			// in the case of debug statement set the appropriate variables
			case Token.DEBUG:
				//check first token of debug statement
				if (this.scan.currentToken.tokenStr.equals("debug")) {
					this.scan.getNext();
					// parseDebugStmt will check the next two tokens and set the variables
					// it will return false if the statement is not formatted correctly
					if (parseDebugStmt()) {
						this.scan.getNext();
						//check if the last token on the line is a ';'
						if(this.scan.currentToken.tokenStr.equals(";")){
							// making sure there is no token on the line after ';'
							if(this.scan.nextToken.iSourceLineNr == this.scan.currentToken.iSourceLineNr){
								throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
										this.scan.nextToken.iSourceLineNr + " at column " + this.scan.nextToken.iColPos +
										" appears after a ';' which designates the end of a statement");
							}
						}else{
							throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
									this.scan.currentToken.iSourceLineNr + " at column " + this.scan.currentToken.iColPos +
									" expected ';' at the end of a Debug statement");
						}
						
					}else{
						throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
								this.scan.nextToken.iSourceLineNr + " at column " + this.scan.nextToken.iColPos +
								" is formatted incorrectly; format: Debug <type> <on/off>");
					}
				}else{
					throw new ScannerTokenFormatException("PARSER ERROR: token on line " + 
							this.scan.nextToken.iSourceLineNr + " at column " + this.scan.nextToken.iColPos +
							" is formatted incorrectly; format: Debug <type> <on/off>");
				}
				break;

			}
		}

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

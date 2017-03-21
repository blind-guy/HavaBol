package havabol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Parse 
{
	private Scanner scan;
	public static int localScope = 0;
	
	// TODO: this should and will be made into a separate class as its functionality is
	// fleshed out but for now it simply maps keys (strings representing variable names)
	// to Objects (what they represent). Their type can be gathered with instanceof (I think).
	private HashMap<String, Object> storage;
	
	public Parse(Scanner s)
	{
		this.scan = s;
		this.storage = new HashMap<String, Object>();
	}


	/**
	 * this is the parser for statements. bExecute will determine whether we execute or not
	 * returnVal will be used to return. this is used to accomplish recursive descent
	 * @param bExecute
	 * @return returnVal
	 * @throws Exception
	 */

	public ResultValue parseStmt(boolean bFlag) throws Exception

	{
		ResultValue returnVal = new ResultValue();
		// continue until end of file
		while (this.scan.currentToken.primClassif != Token.EOF)
		{
			// check the primary class of the current token
			switch (this.scan.currentToken.primClassif)
			{
				// Handle all control tokens and their sub-classifications.
				//
				// It's important to note that we MUST call scan.getNext() to iterate
				// through tokens which match this primary classification but do not
				// yet have their sub-classifications handled.
				case Token.CONTROL:
					if(scan.currentToken.subClassif == Token.DECLARE)
					{
						declareStmt(bFlag);
					}
					else if(scan.currentToken.subClassif == Token.FLOW &&
							scan.currentToken.tokenStr.equals("while"))
					{
						whileStmt(bFlag);				
					}
					// in the case of an if statement we call ifStmt
					else if(scan.currentToken.subClassif == Token.FLOW &&
							scan.currentToken.tokenStr.equals("if"))
					{
						// make sure the next token is the conditional statement for the if statement
						scan.getNext();
						ifStmt(bFlag);								
					}
					else if(scan.currentToken.subClassif == Token.END &&
							scan.currentToken.tokenStr.equals("endwhile"))
					{
						returnVal.terminatingStr = scan.currentToken.tokenStr;
						returnVal.value = scan.currentToken;
						returnVal.iDataType = Token.END;
						return returnVal;
					}
					// if the token is an endif, we will return the endif
					else if(scan.currentToken.subClassif == Token.END &&
						   (scan.currentToken.tokenStr.equals("endif") || scan.currentToken.tokenStr.equals("else")))
					{
						// i wanted to set returnVal and use a break here but the  
						// switch nested in a while forced me to return from here
							
						returnVal.terminatingStr = scan.currentToken.tokenStr;
						returnVal.value = scan.currentToken;
						returnVal.iDataType = Token.END;
						return returnVal; 
					}
					else
					{
						// TODO: remove once all control cases are handled
						scan.getNext();
					}
					break;
				// in the case of debug statement set the appropriate variables
				case Token.DEBUG:
					//check first token of debug statement
					parseDebugStmt();
					break;
				case Token.FUNCTION:
					if (this.scan.currentToken.tokenStr.equals("print")) 
					{
						callBuiltInFunction("print", bFlag);
					}
					else
					{
						scan.getNext(); // at present, not handling case of other functions	
					}
					break;
				// TODO: for now we assume any IDENTIFIER at the beginning of a statement
				// means we are doing an assignment.
				//
				// It's important to note that we MUST call scan.getNext() to iterate
				// through tokens which match this primary classification but do not
				// yet have their sub-classifications or sub-cased handled.
				//
				// For now we follow the pattern of any identifier not followed by a separator
				// is an assignment.
				case Token.IDENTIFIER:
					// TODO: this might not be quite right but I need to not
					// call this on statements such as:
					//
					// var; 
					// // this could be valid syntax even though it does nothing
					if(scan.nextToken.primClassif != Token.SEPARATOR )
					{
						// System.out.println("FOUND TOKEN FOR ASSIGNMENT");
						assignmentStmt(bFlag);
					}
					else
					{
						// TODO: remove once all IDENTIFIER cases are handled
						scan.getNext();
					}
					break;
				// Check to see if we have an unclassified identifier. It's fair to say this
				// probably means failure to initialize but we should try to classify it and
				// let assignmentStmt() handle errors.
				case Token.OPERAND:
					if(scan.currentToken.subClassif == Token.IDENTIFIER)
					{
						reclassifyCurrentTokenIdentifier();
						assignmentStmt(bFlag);
					}
					else
					{
						scan.getNext();
					}
					break;
				default:
					// TODO: remove once all cases are handled or possibly replace with error
					// for poorly classified tokens.
					scan.getNext();
			}
		}
		returnVal.terminatingStr = scan.currentToken.tokenStr;
		returnVal.value = scan.currentToken;
		returnVal.iDataType = scan.currentToken.subClassif;
		return returnVal;
	}

	private void reclassifyCurrentTokenIdentifier() throws ParserException
	{
		// Prevent naughty programmers from trying to reclassify tokens they
		// shouldn't touch.
		if(scan.currentToken.primClassif != Token.OPERAND && 
		   scan.currentToken.subClassif  != Token.IDENTIFIER)
		{
			error("current token is not an identifier and cannot be classified as such");
		}
		
		STEntry stEntry = null;
		stEntry = scan.symbolTable.getEntry(scan.currentToken.tokenStr);
		if(stEntry == null)
		{
			error("attempted to perform an operation with an uninitialized variable or undefined symbol");
		}
		else
		{
			scan.currentToken.primClassif = stEntry.primClassif;
			scan.currentToken.subClassif = ((STIdentifier) stEntry).dclType;
		}
	}

	/**
	 * This handles assignments and will throw an Exception if a variable is undefined,
	 * Improperly classified or if the programmer who wrote this function did something
	 * wrong.
	 * @param bFlag 
	 * @return
	 * @throws Exception
	 */
	private ResultValue assignmentStmt(boolean bFlag) throws Exception
	{
		ResultValue res  = null;
		ResultValue res2 = null;
		Token identifier = null;
		Token operator   = null;
		
		// Are we at an identifier? If no, throw an exception.
		if(scan.currentToken.primClassif != Token.IDENTIFIER)
		{
			error("variable expected for assignment");
		}
		identifier = Token.copyToken(scan.currentToken);
		scan.getNext();
		
		// Verify the current token is now an operator and store it.
		if(scan.currentToken.primClassif != Token.OPERATOR)
		{
			error("assignment expects operator");
		}
		operator = Token.copyToken(scan.currentToken);
		scan.getNext();
		
		// We now have our identifier and operator. A few things can happen.
		// 
		// To put it simply, we call need to know whether the operator is a simple
		// or complex (combined) assignment operator. If it is complex, we need to verify that
		// the left-hand side is a Numeric type.
		//
		// In both cases, we need to set its value, however, in the case of numeric types
		// we should call expression().
		if(operator.tokenStr.equals("="))
		{
			res2 = expr(bFlag);
			if(res2 == null)
			{
				// TODO: this should never equal null actually so something should be done
			}
			else
			{
				if(bFlag)
				{
					res = assign(identifier.tokenStr, res2);
				}
			}
		}
		else if(operator.tokenStr.equals("+=") ||
				operator.tokenStr.equals("-=") ||
				operator.tokenStr.equals("/=") ||
				operator.tokenStr.equals("*="))
		{
			if(identifier.subClassif == Token.STRING)
			{
				error("attempated invalid assignment operator on string");
			}
			else
			{
				res = getValueOfToken(identifier);
				res2 = expr(bFlag);
				
				res = res.performOperation(res2, operator.tokenStr.substring(0, 1));
				if(bFlag)
				{
					res = assign(identifier.tokenStr, res);
				}
			}
		}
		else
		{
			// TODO: this needs to be added after we've handled prints
			// error("attempt invalid assignment operation: " + operator.tokenStr);
		}
		
		// TODO: check valid separator after assignment
		/**
		if(scan.currentToken.primClassif != Token.SEPARATOR &&
		   !scan.currentToken.tokenStr.equals(";"))
		{
			error("expected separator ';' at end of assignment");
		}**/
		
		scan.getNext();
		
		// Print the assignment results if debugging is on.
		// TODO: res should never be null and should be handled above if we attempt
		// to make such an assignment.
		if(bFlag && scan.dBug.bShowAssign == true && res != null)
		{
			System.out.println(res.toString() + " ASSIGNED TO VARIABLE \"" + identifier.tokenStr + "\"");
		}
		
		return res;
	}

	private ResultValue getValueOfToken(Token identifier) throws Exception
	{
		STIdentifier stEntry = null;
		ResultValue res = null;
		if(identifier.primClassif == Token.IDENTIFIER)
		{	
			stEntry = (STIdentifier) scan.symbolTable.getEntry(identifier.tokenStr);
			if(stEntry == null)
			{
				error("variable is undeclared or undefined in this scope");
			}
			res = new ResultValue(identifier.subClassif, storage.get(identifier.tokenStr));
		}
		else if(identifier.primClassif == Token.OPERAND)
		{
			switch(identifier.subClassif)
			{
				case Token.INTEGER:
					res = new ResultValue(identifier.subClassif, new Numeric(identifier.tokenStr, Token.INTEGER));
					break;
				case Token.FLOAT:
					res = new ResultValue(identifier.subClassif, new Numeric(identifier.tokenStr, Token.FLOAT));
					break;
				case Token.BOOLEAN:
					if(identifier.tokenStr.equals("T"))
					{
						res = new ResultValue(identifier.subClassif, new Boolean(true));
					}
					else if(identifier.tokenStr.equals("F"))
					{
						res = new ResultValue(identifier.subClassif, new Boolean(false));
					}
					else
					{
						error("token's primClassif is OPERAND and subClassif is BOOLEAN but " +
							"tokenStr " + identifier.tokenStr + " could not be resolved " +
							"to a boolean value");
					}
					break;
				case Token.STRING:
					res = new ResultValue(identifier.subClassif, new StringBuilder(identifier.tokenStr));
					break;
				default:
					error("operand is of unhandled type");
			}
		}
		return res;
	}

	/**
	 * Assigns a result value to a key by storing it in our current storage scheme.
	 * Any attempt to store a value if it matches no object currently being stored results
	 * in an error being raised.
	 * 
	 * @param key - a String representing a variable name
	 * @param res2 - the result value we wish to store
	 * @return - a result value containing the result of this operation
	 * @throws Exception
	 */
	private ResultValue assign(String key, ResultValue res2) throws Exception
	{
		Object storedObject = null;
		STIdentifier stEntry = null;
		ResultValue res = null;
		
		// Get the STEntry for this key.
		stEntry = (STIdentifier) scan.symbolTable.getEntry(key);
		if(stEntry == null)
		{
			error("attempted to make an assignment to an undeclared variable");
		}

		
		// Get the storage object associated with this key.
		storedObject = storage.get(key);
		if(storedObject == null)
		{
			error("identifier could not be mapped to an existing item in storage");
		}
		
		// We need to check to make sure all these fields match.
		// If not, the value can't be updated and we need to raise an error.
		//
		// TODO: this should be updated in the future to accommodate future types
		// and it should really shrunk down since the code in each if statement
		// is repeated.
		if(storedObject instanceof  Boolean && 
		   res2.iDataType  == Token.BOOLEAN &&
		   stEntry.dclType == Token.BOOLEAN)
		{
			storage.put(key, res2.value);
			res = new ResultValue(res2.iDataType, res2.value);
		}
		else if(storedObject instanceof Numeric &&
			    res2.isNum &&
			   (stEntry.dclType == Token.INTEGER || stEntry.dclType == Token.FLOAT))
		{
			storage.put(key, res2.value);
			res = new ResultValue(res2.iDataType, res2.value);
		}
		else if(storedObject instanceof StringBuilder &&
				res2.iDataType  == Token.STRING &&
				stEntry.dclType == Token.STRING)	
		{
			storage.put(key, res2.value);
			res = new ResultValue(res2.iDataType, res2.value);
		}
		else
		{
			/**System.out.println("CURRENT KEY WHICH FAILED LOOKUP: " + key);
			if(storedObject instanceof Boolean)
			{
				System.out.println("WTF M8");
			}
			System.out.println(storedObject.getClass().getSimpleName());
			System.out.println(res2);
			System.out.println(stEntry);**/
			error("could not match storage object, symbol table entry, and data type of value to store");
		}
		
		return res;
	}

	/**
	 * This evaluates some expression and returns the result of that evaluation.
	 * 
	 * At its simplest it might evaluate something like:
	 * 		foo; // where foo is some variable
	 * or something far more complex.
	 * 
	 * @return ResultValue object containing the result of the expression evaluation.
	 * @throws Exception
	 */
	private ResultValue expr(boolean exec) throws Exception
	{
		// TODO: there's a lot this should do but right now I'm writing it so it sets
		// res to the first thing it sees if it's viable then loops until it hits a separator.
		//
		// GOOD LUCK NICK

		ResultValue res = null;
		
		Stack<ResultValue> operandStack = new Stack<ResultValue>();
		Stack<String> operatorStack = new Stack<String>();
//		if (scan.currentToken.primClassif == Token.SEPARATOR)
//			scan.getNext();
		

		while(scan.currentToken.primClassif != Token.SEPARATOR)
		{
			res = convertCurrentTokenToResultValue();
//			if (res != null)
//				System.out.println(res.toString() + " primClasif " + scan.currentToken.primClassif);
			
			if (scan.currentToken.primClassif == Token.OPERAND 
					|| scan.currentToken.primClassif == Token.IDENTIFIER)
				operandStack.push(res);
			else if (scan.currentToken.primClassif == Token.OPERATOR)
				operatorStack.push(scan.currentToken.tokenStr);
			else if (scan.currentToken.primClassif == Token.FUNCTION)
			{
				ResultValue temp = callBuiltInFunction(scan.currentToken.tokenStr, exec);
				if (temp != null)
					operandStack.push(temp);
			}
			scan.getNext();
		}
		
		//System.out.println("operand size " + operandStack.size() + " operator size " + operatorStack.size());

		if (operandStack.size() == 1 && operatorStack.size() == 1)
		{
			res = (operandStack.pop()).performOperation(null, operatorStack.pop());
		}	
		else if (operandStack.size() == 2 && operatorStack.size() == 1) 
		{
			ResultValue rightOp = operandStack.pop();
			ResultValue leftOp = operandStack.pop();
			res = leftOp.performOperation(rightOp, operatorStack.pop());
		}
		else if (operandStack.size() == 1)
			res = operandStack.pop();
		else
			error("Improper expression");
		
		// Call get next to eat the very last separator.
		//scan.getNext();

		if(scan.dBug.bShowExpr)
		{
			System.out.println("EVALUATION OF EXPRESSION RETURNED: \n\t" + res);
		}
		return res;
	}

	/**
	 * This converts the current token to a result value and increments the scanner.
	 * If it cannot do so either because an identifier doesn't map to a declared variable
	 * or one of the prime or sub-classifications is improperly set, it throws an Exception.
	 * @return
	 * @throws Exception
	 */
	private ResultValue convertCurrentTokenToResultValue() throws Exception
	{
		STIdentifier identifier = null;
		ResultValue res = null;
		if(scan.currentToken.primClassif == Token.IDENTIFIER)
		{	
			identifier = (STIdentifier) scan.symbolTable.getEntry(scan.currentToken.tokenStr);
			if(identifier == null)
			{
				error("variable is undeclared or undefined in this scope");
			}
			res = new ResultValue(scan.currentToken.subClassif, storage.get(scan.currentToken.tokenStr));
			//res = new ResultValue(identifier.dclType, storage.get(scan.currentToken.tokenStr));
		}
		else if(scan.currentToken.primClassif == Token.OPERAND)
		{
			switch(scan.currentToken.subClassif)
			{
				case Token.INTEGER:
					res = new ResultValue(scan.currentToken.subClassif, new Numeric(scan.currentToken.tokenStr, Token.INTEGER));
					break;
				case Token.FLOAT:
					res = new ResultValue(scan.currentToken.subClassif, new Numeric(scan.currentToken.tokenStr, Token.FLOAT));
					break;
				case Token.BOOLEAN:
					if(scan.currentToken.tokenStr.equals("T"))
					{
						res = new ResultValue(scan.currentToken.subClassif, new Boolean(true));
					}
					else if(scan.currentToken.tokenStr.equals("F"))
					{
						res = new ResultValue(scan.currentToken.subClassif, new Boolean(false));
					}
					else
					{
						error("token's primClassif is OPERAND and subClassif is BOOLEAN but " +
							"tokenStr " + scan.currentToken.tokenStr + " could not be resolved " +
							"to a boolean value");
					}
					break;
				case Token.STRING:
					res = new ResultValue(scan.currentToken.subClassif, new StringBuilder(scan.currentToken.tokenStr));
					break;
				default:
					error("operand is of unhandled type");
			}
		}

		return res;
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
	 * @param bFlag 
	 * 
	 * @return a boolean true if it succeeds or false otherwise
	 * @throws Exception representing if there is an error in parsing or thrown by the
	 * scanner
	 */
	private boolean declareStmt(boolean bFlag) throws Exception 
	{
		// See if we have a valid variable name to perform the declare
		// or throw an exception.
		if(scan.nextToken.primClassif != Token.OPERAND &&
		   scan.nextToken.subClassif != Token.IDENTIFIER)
		{
			scan.nextToken.printToken();
			error("expected a valid variable name for declare");
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
			// is likely an error because I didn't code the interpreter correctly.
			error("attempted to make declaration using invalid datatype");
		}
		stEntry.nonLocal = localScope;
		
		if(bFlag)
		{
			storage.put(scan.nextToken.tokenStr, stObject);
			scan.symbolTable.putSymbol(scan.nextToken.tokenStr, stEntry);
			// System.out.println("\tSTENTRY CREATED FOR KEY: " + scan.nextToken.tokenStr + "\n\tITS TYPE IS: " + Token.strSubClassifM[stEntry.dclType]);
		}
		
		// Set the next token and update its prime and sub-classifications now that we've
		// identified it (necessary for in-line assignments like: Int foo = 4;)
		scan.nextToken.primClassif = stEntry.primClassif;
		scan.nextToken.subClassif = stEntry.dclType;
		scan.getNext();
		// scan.currentToken.printToken();
		return true;
	}
	
	/**
	 * 
	 * @param bExec
	 * @throws Exception
	 */
	private void ifStmt(boolean bExec) throws Exception {
		ResultValue resCond;
		// if we are executing
		if(bExec){
			//we are executing 
			resCond = expr(bExec);
			// check the data type of the return value
			if(resCond.iDataType != Token.BOOLEAN){
				error("result of the argument was not a true or false");
			}
			if(! scan.currentToken.tokenStr.equals(":")){
				error("expected ':' after if statement");
			}
			// if the condition evaluated to true
			if (((Boolean)resCond.value).booleanValue()){
				incrementScope();
				resCond = parseStmt(true);
				decrementScope();
				// if there is an else statement
				if(resCond.terminatingStr.equals("else")){
					if(! scan.getNext().equals(":")){
						error("expected ':' after 'else'");
						
					}
					// the else is not executed
					resCond = parseStmt(false);
					if (! resCond.terminatingStr.equals("endif")){
						error("expected endif");
					}
				}
				// another check for endif, this is necessary if there is no else
				if (! resCond.terminatingStr.equals("endif")){
					error("expected endif");
				}
				// error check endif should end with a ';'
				if(! scan.getNext().equals(";")){
					error("expected ';' after endif");
				}
			//condition evaluated to false
			}else{
				// do not execute, if condition was false
				resCond = parseStmt(false);
				// if there is an else statement
				if(resCond.terminatingStr.equals("else")){
					if(!scan.getNext().equals(":")){
						error("expected ':' after 'else'");
						
					}
					// the else is executed because if condition was false
					incrementScope();
					resCond = parseStmt(true);
					decrementScope();
					if (! resCond.terminatingStr.equals("endif")){
						error("expected endif");
					}
				}
				// another check for endif, this is necessary if there is no else
				if (! resCond.terminatingStr.equals("endif")){
					error("expected endif");
				}
				// error check endif should end with a ';'
				if(! scan.getNext().equals(";")){
					error("expected ';' after endif");
				}
			}
			
			
		// we are not executing	
		}else{
			//we are ignoring everything the conditional, true part, false part
			// nothing will execute but the error checking is necessary so we iterate
			// through to find any syntax errors 
			skipTo("if", ":");
			resCond = parseStmt(false);
			// if there is an else statement
			if(resCond.terminatingStr.equals("else")){
				if(!scan.getNext().equals(":")){
					error("expected ':' after 'else'");
					
				}
				// the else is not executed
				resCond = parseStmt(false);
				if (! resCond.terminatingStr.equals("endif")){
					error("expected endif");
				}
			}
			// another check for endif, this is necessary trust me. actually tracing this out in my head i don't see the need for the 
			// check for endif until the end of the function. i'm going to do some tests and i'll clean it up if it's not necessary 
			if (! resCond.terminatingStr.equals("endif")){
				error("expected endif");
			}
			// error check endif should end with a ';'
			if(! scan.getNext().equals(";")){
				error("expected ';' after endif");
			}
		}
		// making sure there is no token on the line after ';'
		if(this.scan.nextToken.iSourceLineNr == this.scan.currentToken.iSourceLineNr)
		{
			throw new ParserException(this.scan.nextToken.iSourceLineNr, "PARSER ERROR: token on line " + 
				this.scan.nextToken.iSourceLineNr + " at column " + this.scan.nextToken.iColPos +
				" appears after a ';' which designates the end of a statement", "file name");
		}
		scan.getNext();
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
	private void parseDebugStmt() throws Exception {
		boolean format = false;
		
		if (this.scan.currentToken.tokenStr.equals("debug")) 
		{
			this.scan.getNext();
		}
		else
		{
			throw new ParserException(this.scan.nextToken.iSourceLineNr,
					"PARSER ERROR: token at column " + this.scan.nextToken.iColPos +
					" is formatted incorrectly; format: debug <type> <on/off>", "file name");
		}
		if (this.scan.currentToken.tokenStr.equals("bShowToken")) {
			this.scan.getNext();
			if (this.scan.currentToken.tokenStr.equals("on")) {
				this.scan.dBug.bShowToken = true;
				// System.out.println(" this is bShowtoken: " + scan.dBug.bShowToken);
				format = true;
			} else if (this.scan.currentToken.tokenStr.equals("off")) {
				this.scan.dBug.bShowToken = false;
				format = true;
			}
		}
		else if(this.scan.currentToken.tokenStr.equals("bShowExpr"))
		{
			this.scan.getNext();
			if (this.scan.currentToken.tokenStr.equals("on")) {
				this.scan.dBug.bShowExpr = true;
				format = true;
			} else if (this.scan.currentToken.tokenStr.equals("off")) {
				this.scan.dBug.bShowExpr = false;
				format = true;
			}
		}
		else if (this.scan.currentToken.tokenStr.equals("bShowAssign"))
		{
			this.scan.getNext();
			if (this.scan.currentToken.tokenStr.equals("on")) {
				this.scan.dBug.bShowAssign = true;
				format = true;
			} else if (this.scan.currentToken.tokenStr.equals("off")) {
				this.scan.dBug.bShowAssign = false;
				format = true;
			}
		
		}
		if(! format){
			throw new ParserException(this.scan.nextToken.iSourceLineNr,
					"PARSER ERROR: token at column " + this.scan.nextToken.iColPos +
					" is formatted incorrectly; format: debug <type> <on/off>", "file name");
		}
		scan.getNext();
		
		if(this.scan.currentToken.tokenStr.equals(";"))
		{
			// making sure there is no token on the line after ';'
			if(this.scan.nextToken.iSourceLineNr == this.scan.currentToken.iSourceLineNr)
			{
				throw new ParserException(this.scan.nextToken.iSourceLineNr,
					"PARSER ERROR: token at column " + this.scan.nextToken.iColPos +
					" appears after a ';' which designates the end of a statement", "file name");
			}	
		}
		else
		{
			throw new ParserException(this.scan.currentToken.iSourceLineNr,
					"PARSER ERROR: token at column " + this.scan.nextToken.iColPos +
					" expected ';' at the end of a debug statement", "file name");
		}
			
		scan.getNext();
	}
		
	
	/**
	 * this class will skip to the next occurrence of the terminal symbol.
	 * 
	 * @param type
	 * @param terminal
	 * @throws Exception
	 */
	private void skipTo(String type, String terminal) throws Exception{
		// i think we need to save off the column position and line number for error statements
		while(! scan.getNext().isEmpty()){
			if(type.equals("if")){
				// there needs to be some checks here to tell if
				// the terminal is within an expression
				if(scan.currentToken.tokenStr.equals(terminal)){ 
					break;
					
				}
				if(scan.currentToken.primClassif == Token.CONTROL){
					error("cannot have a control statement within an if argument");
				}
				if(scan.currentToken.primClassif == Token.DEBUG){
					error("cannot have a debug statement within an if argument");
				}
				if(scan.currentToken.primClassif == Token.RT_PAREN){
					error("cannot have an RT paren within an if argument");
				}
				if(scan.currentToken.tokenStr.equals("print")){
					error("cannot evaluate print function within an if argument");
				}
			}
			
		}
		// might need to check to see if we are at the end of file or the function that calls skip to will check for end of file
	}
	

	private ResultValue callBuiltInFunction(String func, boolean bFlag) throws Exception
	{
		if (func.equals("print"))
		{
			print(bFlag);
			return null;
		}
		
		return new ResultValue();
	}
	
	private void print(boolean exec) throws Exception
	{
		scan.getNext();
		int parenthesisCounter = 0;
		if (!scan.currentToken.tokenStr.equals("("))
			error("Missing left parenthesis for print function");
		parenthesisCounter++;
		ArrayList<ResultValue> results = new ArrayList<ResultValue>();
		scan.getNext();
		
		if (exec == true) 
		{
			while (parenthesisCounter > 0)
			{
				//System.out.println("loop " + scan.currentToken.tokenStr);
				results.add(expr(exec));
				
				if (scan.currentToken.tokenStr.equals(")"))
					parenthesisCounter--;
				else if (scan.currentToken.tokenStr.equals("("))
					parenthesisCounter++;
				scan.getNext();
			}
		}
		else 
		{
			while (parenthesisCounter > 0)
			{
				if (scan.currentToken.tokenStr.equals(";"))
					error("Malformed print statement");
				
				if (scan.currentToken.tokenStr.equals(")"))
					parenthesisCounter--;
				else if (scan.currentToken.tokenStr.equals("("))
					parenthesisCounter++;
				if (scan.currentToken.primClassif == Token.SEPARATOR
						&& scan.nextToken.primClassif == Token.SEPARATOR)
				{
					if (!scan.currentToken.tokenStr.equals(")")
							&& scan.nextToken.tokenStr.equals(";"))
					{
						error("No argument betweeen " + scan.currentToken.tokenStr
								+ scan.nextToken.tokenStr);
					}
				}
				
				scan.getNext();
			}
		}
		//System.out.println("results size " + results.size());
		
		if (exec == true) {
			for (ResultValue rs : results)
				System.out.print(rs.value.toString().concat(" "));
			System.out.println();
		}
		
		
		if (scan.currentToken.tokenStr.equals(";"))
		{
			scan.getNext();
		}
		else
			error("No semicolon at the end of this line");
	}
	
	private void incrementScope()
	{
		localScope++;
	}
	
	/**
	 * This function evaluates symbols in the symbol table, finds those
	 * which are identifiers which match the current local scope and removes them
	 * from the symbol table and their corresponding storage objects fromt he storage.
	 */
	private void decrementScope()
	{
		ArrayList<String> keys = new ArrayList<String>();
		
		// Loop through the entries in our symbol table.
		// Locate all entries that are identifiers and if their local scope matches our
		// current local scope, we remove their entries from storage and from the 
		// Symbol Table.
		Iterator<Entry<String, STEntry>> it = scan.symbolTable.ht.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, STEntry> entry = it.next();
			STEntry stEntry = entry.getValue();
			if(stEntry.primClassif == Token.IDENTIFIER &&
			   ((STIdentifier) stEntry).nonLocal == localScope)
			{
				keys.add(entry.getKey());
			}
		}
		for(String key: keys)
		{
			storage.remove(key);
			scan.symbolTable.ht.remove(key);
		}
		
		// Decrement the scope variable.
		localScope--;
	}
	
	private void whileStmt(boolean bFlag) throws Exception
	{
		Token conditionToken = null;
		ResultValue conditionResult = null;
		
		// Set current to the condition we wish to evaluate.
		scan.getNext();
		
		// Copy the condition Token so we have our position saved and get the result value
		// for its evaluation.
		conditionToken  = Token.copyToken(scan.currentToken);
		conditionResult = expr(bFlag);
		
		// See if we were given a result which is the proper value type.
		if(conditionResult.iDataType != Token.BOOLEAN)
		{
			error("while condition could not be evaluated to boolean");
		}
		
		// Check to see if we're on the correct separator for syntax reasons.
		if(scan.currentToken.primClassif != Token.SEPARATOR || 
		   !scan.currentToken.tokenStr.equals(":"))
		{
			error("invalid syntax in while statement: expected ':' and was given " + scan.currentToken.tokenStr);
		}
		else
		{
			scan.getNext();
		}
		
		// If we're executing, perform our loop.
		if(bFlag)
		{
			// Loop until our condition is evaluated to false.
			while(((Boolean) conditionResult.value).booleanValue())
			{
				// Increment scope.
				incrementScope();
		
				// Call parseStmt(). This should return here when we find an
				// endwhile control flow token.
				parseStmt(bFlag);
				
				// Decrement scope which will also handle cleaning up values we don't want to
				// stay around.
				decrementScope();
				
				// Set our scanner position to the conditional again.
				scan.setPos(conditionToken);
				
				// Reevaluate the conditional and increment the scanner.
				// We've error checked this before so there's no need to see if
				// it's still a valid separator.
				conditionResult = expr(bFlag);
				scan.getNext();
			}

			parseStmt(false);
		}
		else
		{ 
			parseStmt(bFlag);
		}
		
		// We've finished executing the loop and need to check to make sure the last
		// separator is valid for syntax reasons.
		if(scan.nextToken.primClassif != Token.SEPARATOR ||
		   !scan.nextToken.tokenStr.equals(";"))
		{
			error("invalid syntax at end of while loop: expected ';' but received '" +
				scan.nextToken.tokenStr + "'");
		}
		else
		{
			scan.getNext();
			scan.getNext();
		}
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

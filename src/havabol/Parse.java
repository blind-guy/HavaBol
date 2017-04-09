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
	Precedence precedence = null;
	
	public Parse(Scanner s)
	{
		this.scan = s;
		this.storage = new HashMap<String, Object>();
		// set precedence for operators
		precedence = new Precedence();
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
					
					callBuiltInFunction(bFlag);
					
//					else
//					{
//						scan.getNext(); // at present, not handling case of other functions	
//					}
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
					if(scan.nextToken.primClassif == Token.SEPARATOR && 
					   scan.nextToken.tokenStr.equals("["))
					{
						arrayAssignmentStmt(bFlag);
					}
					else
					{
						assignmentStmt(bFlag);
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

	/**
	 * Handle complex array assignments including determining if this
	 * is a single value or full array assignment.
	 * 
	 * The logic here is as follows:
	 * 		-if the current identifier maps to a declared array maxSize > 0
	 * 		 then we are making a single variable assignment to some index
	 *       within the array
	 *      -else if the array size is not > 0, this array is uninitialized
	 *       and we must be either initializing its size and/or assigning values
	 * @throws Exception 
	 */
	private void arrayAssignmentStmt(boolean bFlag) throws Exception 
	{
		// Get the STEntry and storage objects for the array.
		STEntry stEntry = scan.symbolTable.getEntry(scan.currentToken.tokenStr);
		STIdentifier stIdentifier = null;
		Object stObject = storage.get(scan.currentToken.tokenStr);
		HavabolArray array = null;
		ResultValue sizeResVal = null;
		int arrayIndex = -1;
		
		// If either of these are empty, there is an error.
		if(stEntry == null)
		{
			error("attempting to make an assignment with an undeclared variable");
		}
		if(stObject == null)
		{
			error("attempting to make an assignment using an undeclared varaible");
		}
		
		// Make sure the STEntry is an identifier and the storage object
		// is an ArrayList
		if(!(stEntry instanceof STIdentifier))
		{
			error("symbol table for this token is not an identifier");
		}
		if((((STIdentifier) stEntry).structure != HavabolStructureType.ARRAY) ||
		   (!(stObject instanceof HavabolArray)))
		{
			error("this variable was not instantiated as an array");
		}
		stIdentifier = (STIdentifier) stEntry;
		array = (HavabolArray) stObject;
		
		// Check to see if the stEntry and storage object types are correct.
		if(stIdentifier.dclType != array.dclType)
		{
			error("identifier and array type do not match");
		}

		// Now to start doing the work.
		
		// If the array size is not set, we are at the very least doing
		// an initialization.
		if(array.maxSize == 0)
		{
			// We are still on our identifier.
			scan.getNext();
			scan.getNext();
			
			// If the current token is not a separator, we expect an expression.
			if(!(scan.currentToken.primClassif == Token.SEPARATOR &&
		       scan.currentToken.tokenStr.equals("]")))
			{
				sizeResVal = expr(bFlag);
				if(sizeResVal.iDataType != Token.INTEGER)
				{
					error("size expression expects type INT, type given is " + Token.strSubClassifM[sizeResVal.iDataType]);
				}
				else
				{
					array.maxSize = Integer.parseInt(((Numeric) sizeResVal.value).stringValue);
				}
			}
			
			if(scan.currentToken.primClassif != Token.SEPARATOR || 
			   !scan.currentToken.tokenStr.equals("]"))
			{
				scan.currentToken.printToken();
				error("invalid expression given for array size");
			}
			scan.getNext();
			
			
			// If the array size was set to an invalid integer value or
			// is still zero and we are not inferring its size, we should
			// print an appropriate error message.
			if(array.maxSize < 0)
			{
				error("array size cannot be negative");
			}
			else if(array.maxSize == 0)
			{
				if(scan.currentToken.primClassif != Token.OPERATOR &&
				   !scan.currentToken.tokenStr.equals("="))
				{
					error("array must be declared with a fixed size or be given an assignment");
				}
			}
			
			// If we're at an operator, we must start doing assignments
			// otherwise we must be at a semicolon and can return.
			if(scan.currentToken.primClassif == Token.OPERATOR)
			{
				if(!scan.currentToken.tokenStr.equals("="))
				{
					error("invalid assignment operator made in array assignment");
				}
				scan.getNext();
				
				
				while(true)
				{
					if(scan.currentToken.primClassif == Token.SEPARATOR &&
					   scan.currentToken.tokenStr.equals(";"))
					{
						break;
					}
					else if(scan.currentToken.primClassif == Token.SEPARATOR &&
							scan.currentToken.tokenStr.equals(","))
					{
						arrayIndex++;
						array.unsafeAppend(null);
						scan.getNext();
						continue;
					}
					
					arrayIndex++;
					ResultValue resValToStore = expr(bFlag);
					switch(array.dclType)
					{
						case Token.INTEGER:
							if(resValToStore.iDataType == Token.BOOLEAN)
							{
								error("cannot convert Bool to integer type");
							}
							else if(resValToStore.iDataType == Token.INTEGER)
							{
								resValToStore = new ResultValue(
														resValToStore.iDataType,
														new Numeric(
																((Numeric) resValToStore.value).stringValue,
																resValToStore.iDataType
															)
														);
							}
							else if(resValToStore.iDataType == Token.FLOAT)
							{
								resValToStore = new ResultValue(
														Token.INTEGER, 
														"" + ((Numeric) resValToStore.value).intValue
													);
							}
							else if(resValToStore.iDataType == Token.STRING)
							{
								// convert the string to an integer if possible
								resValToStore = new ResultValue(
														Token.INTEGER, 
														"" + new Numeric(
																"" + new Numeric(
																		((StringBuilder) resValToStore.value).toString(), 
																		Token.FLOAT
																	).intValue,
																Token.INTEGER
															).intValue
														);
							}
							break;
						case Token.FLOAT:
							if(resValToStore.iDataType == Token.BOOLEAN)
							{
								error("cannot convert Bool to float type");
							}
							else if(resValToStore.iDataType == Token.FLOAT)
							{
								resValToStore = new ResultValue(
													resValToStore.iDataType,
													new Numeric(
															((Numeric) resValToStore.value).stringValue,
															resValToStore.iDataType
														)
													);
							}
							else if(resValToStore.iDataType == Token.INTEGER)
							{
								resValToStore = new ResultValue(Token.FLOAT, "" + ((Numeric) resValToStore.value).doubleValue);
							}
							else if(resValToStore.iDataType == Token.STRING)
							{
								// convert the string to an integer if possible
								resValToStore = new ResultValue(
														Token.FLOAT, 
														"" + new Numeric(
																"" + new Numeric(
																		((StringBuilder) resValToStore.value).toString(), 
																		Token.FLOAT
																	).doubleValue,
																Token.FLOAT
															).doubleValue
														);
							}
							break;
						case Token.STRING:
							if(resValToStore.iDataType == Token.STRING)
							{
								resValToStore = new ResultValue(
														Token.STRING,
														new StringBuilder(
																((StringBuilder) resValToStore.value).toString()	
															)
														);	
							}
							else if(resValToStore.iDataType == Token.BOOLEAN)
							{
								resValToStore = new ResultValue(
														Token.STRING,
														new StringBuilder(((Boolean) resValToStore.value).toString())
													);
							}
							else if(resValToStore.iDataType == Token.FLOAT ||
									resValToStore.iDataType == Token.INTEGER)
							{
								resValToStore = new ResultValue(
														Token.STRING,
														new StringBuilder(
																((Numeric) resValToStore.value).stringValue
															)
													);
							}
							break;
						case Token.BOOLEAN:
							if(resValToStore.iDataType == Token.BOOLEAN)
							{
								resValToStore = new ResultValue(
														Token.BOOLEAN,
														new Boolean(((Boolean) resValToStore.value).booleanValue())
													);
										
							}
							else
							{
								error("cannot convert a " + Token.strSubClassifM[resValToStore.iDataType] + " to a boolean value");
							}
							break;
						default:
							resValToStore = null;
							break;
					}
					array.unsafeAppend(resValToStore);
					if(scan.currentToken.primClassif == Token.SEPARATOR &&
					   scan.currentToken.tokenStr.equals(","))
					{
						scan.getNext();
					}
				}
				if(array.maxSize == 0)
				{
					array.maxSize = arrayIndex + 1;
				}
				else if(array.maxSize < arrayIndex + 1)
				{
					error("assigned more values to array than its declared size");
				}
			}
			else if(scan.currentToken.primClassif != Token.SEPARATOR ||
					!scan.currentToken.tokenStr.equals(";"))
			{
				error("invalid array declaration expression");
			}
			else
			{
				return;
			}
		}
		// If size is already set, we might be doing a scalar
		// assignment.
		//
		// We need to check if we have an expression to parse.
		else
		{
			scan.getNext();
			scan.getNext();
			
			// If we do not see the right side bracket,
			// this should be where an expression is.
			if((scan.currentToken.primClassif != Token.SEPARATOR) || 
			   (scan.currentToken.primClassif == Token.SEPARATOR && 
			    !scan.currentToken.tokenStr.equals("]")))
			{
				ResultValue indexResVal = expr(bFlag);
				if(indexResVal.iDataType != Token.INTEGER)
				{
					error("index expression when making assignment to array must evaluate to integer");
				}
				else if(scan.currentToken.primClassif != Token.SEPARATOR || 
						!scan.currentToken.tokenStr.equals("]") ||
						scan.nextToken.primClassif != Token.OPERATOR ||
						!scan.nextToken.tokenStr.equals("="))
				{
					error("invalid expression given in scalar array assignment");
				}
				
				scan.getNext();
				scan.getNext();
				
				ResultValue resValToStore = expr(bFlag);
				
				switch(array.dclType)
				{
					case Token.INTEGER:
						if(resValToStore.iDataType == Token.BOOLEAN)
						{
							error("cannot convert Bool to integer type");
						}
						else if(resValToStore.iDataType == Token.INTEGER)
						{
							resValToStore = new ResultValue(
													resValToStore.iDataType,
													new Numeric(
															((Numeric) resValToStore.value).stringValue,
															resValToStore.iDataType
														)
													);
						}
						else if(resValToStore.iDataType == Token.FLOAT)
						{
							resValToStore = new ResultValue(
													Token.INTEGER, 
													"" + ((Numeric) resValToStore.value).intValue
												);
						}
						else if(resValToStore.iDataType == Token.STRING)
						{
							// convert the string to an integer if possible
							resValToStore = new ResultValue(
													Token.INTEGER, 
													"" + new Numeric(
															"" + new Numeric(
																	((StringBuilder) resValToStore.value).toString(), 
																	Token.FLOAT
																).intValue,
															Token.INTEGER
														).intValue
													);
						}
						break;
					case Token.FLOAT:
						if(resValToStore.iDataType == Token.BOOLEAN)
						{
							error("cannot convert Bool to float type");
						}
						else if(resValToStore.iDataType == Token.FLOAT)
						{
							resValToStore = new ResultValue(
												resValToStore.iDataType,
												new Numeric(
														((Numeric) resValToStore.value).stringValue,
														resValToStore.iDataType
													)
												);
						}
						else if(resValToStore.iDataType == Token.INTEGER)
						{
							resValToStore = new ResultValue(Token.FLOAT, "" + ((Numeric) resValToStore.value).doubleValue);
						}
						else if(resValToStore.iDataType == Token.STRING)
						{
							// convert the string to an integer if possible
							resValToStore = new ResultValue(
													Token.FLOAT, 
													"" + new Numeric(
															"" + new Numeric(
																	((StringBuilder) resValToStore.value).toString(), 
																	Token.FLOAT
																).doubleValue,
															Token.FLOAT
														).doubleValue
													);
						}
						break;
					case Token.STRING:
						if(resValToStore.iDataType == Token.STRING)
						{
							resValToStore = new ResultValue(
													Token.STRING,
													new StringBuilder(
															((StringBuilder) resValToStore.value).toString()	
														)
													);	
						}
						else if(resValToStore.iDataType == Token.BOOLEAN)
						{
							resValToStore = new ResultValue(
													Token.STRING,
													new StringBuilder(((Boolean) resValToStore.value).toString())
												);
						}
						else if(resValToStore.iDataType == Token.FLOAT ||
								resValToStore.iDataType == Token.INTEGER)
						{
							resValToStore = new ResultValue(
													Token.STRING,
													new StringBuilder(
															((Numeric) resValToStore.value).stringValue
														)
												);
						}
						break;
					case Token.BOOLEAN:
						if(resValToStore.iDataType == Token.BOOLEAN)
						{
							resValToStore = new ResultValue(
													Token.BOOLEAN,
													new Boolean(((Boolean) resValToStore.value).booleanValue())
												);
									
						}
						else
						{
							error("cannot convert a " + Token.strSubClassifM[resValToStore.iDataType] + " to a boolean value");
						}
						break;
					default:
						resValToStore = null;
						break;
				}
				
				array.put(resValToStore, ((Numeric) indexResVal.value).intValue);
				scan.getNext();
			}
		}
		System.out.println(array);
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
		
		// If the stored object is an istance of a Boolean,
		// try to evaluate the right res2 val to a Boolean.
		Object valueToStore = null;
		if(storedObject instanceof  Boolean)
		{
			if(stEntry.dclType != Token.BOOLEAN)
			{
				error("storage object and identifier types do not match");
			}
			
			if(res2.iDataType == Token.BOOLEAN)
			{
				valueToStore = res2.value;
			}
			else if(res2.iDataType == Token.STRING)
			{
				valueToStore = new Boolean(Boolean.parseBoolean(((StringBuilder) res2.value).toString()));
			}
			else if(res2.isNum)
			{
				error("cannot coerce numeric types to boolean values");
			}
			storage.put(key, valueToStore);
			res = new ResultValue(stEntry.dclType, valueToStore);
		}
		else if(storedObject instanceof Numeric)
		{
			if(stEntry.dclType != Token.INTEGER && stEntry.dclType != Token.FLOAT)
			{
				error("storage object and idnetifier types do not match");
			}

			if(res2.iDataType == Token.BOOLEAN)
			{
				error("cannot coerce boolean to numeric type");
			}
			else if(res2.iDataType == Token.STRING)
			{
				if(stEntry.dclType == Token.INTEGER)
				{
					valueToStore = new Numeric("" + Integer.parseInt(((StringBuilder) res2.value).toString()), Token.INTEGER);
				}
				else if(stEntry.dclType == Token.FLOAT)
				{
					valueToStore = new Numeric("" + Double.parseDouble(((StringBuilder) res2.value).toString()), Token.FLOAT);
				}
			}
			else if(res2.isNum)
			{
				valueToStore = new Numeric(((Numeric) res2.value).toString(), stEntry.dclType);
			}
			
			storage.put(key, valueToStore);
			res = new ResultValue(stEntry.dclType, valueToStore);
		}
		else if(storedObject instanceof StringBuilder)
		{
			if(stEntry.dclType != Token.STRING)
			{
				error("storage object and identifier types do not match");
			}
		
			if(res2.iDataType == Token.BOOLEAN)
			{
				if(((Boolean) res2.value).booleanValue())
				{
					valueToStore = new StringBuilder("T");
				}
				else
				{
					valueToStore = new StringBuilder("F");
				}
			}
			else if(res2.iDataType == Token.STRING)
			{
				valueToStore = new StringBuilder(((StringBuilder) res2.value).toString());
			}
			else if(res2.isNum)
			{
				valueToStore = new StringBuilder(((Numeric) res2.value).toString());
			}
			
	
			storage.put(key, valueToStore);
			res = new ResultValue(stEntry.dclType, valueToStore);
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
		Token popped = null;
		
		ArrayList<Token> postfixExpr = new ArrayList<Token>();
		Stack<Token> operatorStack = new Stack<Token>();
		Token lastToken = null;
		
		// loop while the current token is not a separator (unless it's '(' or ')'
		// and is not a control token
		while((scan.currentToken.primClassif != Token.SEPARATOR 
				&& scan.currentToken.primClassif != Token.CONTROL
				|| (scan.currentToken.tokenStr.equals("(") || scan.currentToken.tokenStr.equals(")")))
				) //&& !(scan.currentToken.primClassif != Token.CONTROL))
		{	
//			System.out.println("current token: " + scan.currentToken.tokenStr);
//			System.out.println("stack");
//			for (Token t : operatorStack)
//				System.out.print(t.tokenStr + " prec: " + t.precedence);
//			System.out.println();
//			
//			System.out.println("out");
//			for (Token t : postfixExpr)
//				System.out.print(t.tokenStr + " ");
//			System.out.println();
			
			if (scan.currentToken.primClassif == Token.OPERAND
					|| scan.currentToken.primClassif == Token.IDENTIFIER) 
			{
				postfixExpr.add(scan.currentToken);
			}
			else if (scan.currentToken.primClassif == Token.OPERATOR) 
			{
				// if the token is unary minus, change token's precedence
				if (scan.currentToken.tokenStr.equals("-"))
				{
					if (lastToken == null 
							|| lastToken.primClassif == Token.OPERATOR
							|| lastToken.primClassif == Token.CONTROL
							|| (lastToken.primClassif == Token.SEPARATOR
							&& lastToken.tokenStr.equals("("))) 
					{
						scan.currentToken.precedence = precedence.getTokenPrecedence("u-");
						scan.currentToken.stackPrecedence = precedence.getStackPrecedence("u-");
					}
				}
				// loop while operators of lower precedence are in the stack
				while (!operatorStack.isEmpty()) 
				{
					if (scan.currentToken.precedence > operatorStack.peek().stackPrecedence)
						break;
					
					postfixExpr.add(operatorStack.pop());
				}
				operatorStack.push(scan.currentToken);
			}
			else if (scan.currentToken.primClassif == Token.SEPARATOR) 
			{
				if (scan.currentToken.tokenStr.equals("("))
					operatorStack.push(scan.currentToken);
				else if (scan.currentToken.tokenStr.equals(")")) 
				{
					boolean bFound = false;
					while (!operatorStack.isEmpty()) 
					{
						popped = operatorStack.pop();
						//
//						for (Token t : postfixExpr)
//							System.out.print(t.tokenStr + " ");
//						System.out.println();
//						System.out.println("popped: " + popped.tokenStr);
						//
						if (popped.tokenStr.equals("(")) 
						{
							bFound = true;
							break;
						}
						postfixExpr.add(popped);
					}
					if (!bFound) 
					{
						//error("Missing '('");
						break;
					}
				}
				else 
				{
					error("Invalid separator");
				}
			}
			else if (scan.currentToken.primClassif == Token.FUNCTION)
			{
				// take result value from callBuiltInFunction and convert it to a token
				ResultValue funcResultVal = callBuiltInFunction(exec);
				Token funcResultToken = new Token(funcResultVal.value.toString());
				funcResultToken.primClassif = Token.OPERAND;
				funcResultToken.subClassif = funcResultVal.iDataType;
				//System.out.println("subClassif " + funcResultVal.iDataType);
				postfixExpr.add(funcResultToken);
			}
			else 
			{
				error("Invalid expression. Expecting operands or operators");
			}
			
			lastToken = scan.currentToken;
			scan.getNext();
		}
		
		while (!operatorStack.isEmpty())
		{
			popped = operatorStack.pop();
			if (popped.tokenStr.equals("")) 
				error("Missing ')'");
			postfixExpr.add(popped);
		}

//		for (Token t : postfixExpr)
//			System.out.print(t.tokenStr + " ");
//		System.out.println();
		
		Stack<ResultValue> resultStack = new Stack<ResultValue>();
		ResultValue rightOpResVal, leftOpResVal, endResVal;
		Token tempTok;
		
		for (int i=0; i<postfixExpr.size(); i++)
		{
			tempTok = postfixExpr.get(i);
			//System.out.println("token: " + tempTok.tokenStr);
			if (tempTok.primClassif == Token.OPERAND || tempTok.primClassif == Token.IDENTIFIER)
			{
				//endResVal = convertGivenTokenToResultValue(tempTok);
				//System.out.println(tempTok.tokenStr + " " + tempTok.subClassif);
				endResVal = getValueOfToken(tempTok);
				
				resultStack.push(endResVal);
			}
			else if (tempTok.primClassif == Token.OPERATOR)
			{
				// precedence of 12 indicates unary minus
				if (tempTok.tokenStr.equals("not") || tempTok.precedence == 12)
				{
					if (resultStack.isEmpty())
						error("Invalid expression");
					
					rightOpResVal = resultStack.pop();
					resultStack.push(rightOpResVal.performOperation(null, tempTok.tokenStr));
				}
				else 
				{
					if (resultStack.size() < 2)
						error("Invalid expression");
					
					rightOpResVal = resultStack.pop();
					leftOpResVal = resultStack.pop();
					resultStack.push(leftOpResVal.performOperation(rightOpResVal, tempTok.tokenStr));
				}
			}
			else
			{
				error("Invalid expression. Expected operands or operators in expression.");
			}
		}

		if(scan.dBug.bShowExpr)
		{
			System.out.println("EVALUATION OF EXPRESSION RETURNED: \n\t" + res);
		}
		
		if (resultStack.size() != 1)
			error("Invalid expression.");
		else
			res = resultStack.pop();
		//System.out.println(res.toString());
		
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
		
		// Set the next token and update its prime and sub-classifications now that we've
		// identified it (necessary for in-line assignments like: Int foo = 4;)
		scan.nextToken.primClassif = stEntry.primClassif;
		scan.nextToken.subClassif = stEntry.dclType;
		scan.getNext();
		
		// Save key from current token.
		String key = scan.currentToken.tokenStr;
		
		// Check to see if we're at an array and if so, change the stObject
		// and entry.
		if(scan.nextToken.primClassif == Token.SEPARATOR &&
		   scan.nextToken.tokenStr.equals("["))
		{
			stEntry.structure = HavabolStructureType.ARRAY;
			stObject = new HavabolArray(stEntry.dclType);
		}
		else if(scan.nextToken.primClassif == Token.SEPARATOR &&
				scan.nextToken.tokenStr.equals(";"))
		{
			scan.getNext();
		}
		
		if(bFlag)
		{
			storage.put(key, stObject);
			scan.symbolTable.putSymbol(key, stEntry);
		}
		

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
	

	private ResultValue callBuiltInFunction(boolean bFlag) throws Exception
	{
		if (scan.currentToken.tokenStr.equals("print"))
		{
			print(bFlag);
			return null;
		}
		else if (scan.currentToken.tokenStr.equals("LENGTH"))
		{
			return length(bFlag);
		}
		else if (scan.currentToken.tokenStr.equals("SPACES"))
		{
			return spaces(bFlag);
			//return length(bFlag);
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
				
//				if (scan.currentToken.tokenStr.equals(";"))
//					break;
				
				if (scan.currentToken.tokenStr.equals(")"))
					parenthesisCounter--;
				else if (scan.currentToken.tokenStr.equals("("))
					parenthesisCounter++;
				
				//System.out.println("print current token: " + scan.currentToken.tokenStr);
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
			{
				if (rs == null)
					error("Cannot print null value");
				System.out.print(rs.value.toString().concat(" "));
			}
			System.out.println();
		}
		
		//System.out.println("print current token: " + scan.currentToken.tokenStr);
		
		if (!scan.currentToken.tokenStr.equals(";"))
			error("No semicolon at the end of this line");
		
		scan.getNext();
	}
	
	private ResultValue length(boolean exec) throws Exception {
		scan.getNext();
		
		int parenthesisCounter = 0;
		if (!scan.currentToken.tokenStr.equals("("))
			error("Missing left parenthesis for LENGTH function");
		parenthesisCounter++;
		ResultValue param, result = null;
		scan.getNext();
		
		if (exec == true) 
		{
			param = expr(exec);
			//System.out.println(scan.currentToken.tokenStr);
			if (!scan.currentToken.tokenStr.equals(")"))
				error("Missing right parenthesis for LENGTH function");
			else if (param.iDataType != Token.STRING)
				error("LENGTH only takes String arguments");
			result = new ResultValue(Token.INTEGER, (param.value.toString()).length());
		}
		else 
		{
			while (parenthesisCounter > 0)
			{
				if (scan.currentToken.tokenStr.equals(";"))
					error("Malformed LENGTH statement");
				
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
		//System.out.println(result.iDataType + " " + result.toString());
		
		return result;
	}
	
	private ResultValue spaces(boolean exec) throws Exception {
		scan.getNext();
		
		int parenthesisCounter = 0;
		if (!scan.currentToken.tokenStr.equals("("))
			error("Missing left parenthesis for SPACES function");
		parenthesisCounter++;
		ResultValue param, result = null;
		scan.getNext();
		
		if (exec == true) 
		{
			param = expr(exec);
			//System.out.println(scan.currentToken.tokenStr);
			if (!scan.currentToken.tokenStr.equals(")"))
				error("Missing right parenthesis for SPACES function");
			else if (param.iDataType != Token.STRING)
				error("LENGTH only takes String arguments");
			boolean containsSpaces = true;
			String val = param.value.toString();
			for (int i=0; i<val.length(); i++)
			{
				//System.out.print("'" + val.charAt(i) + "'");
				if (val.charAt(i) != ' ' && val.charAt(i) != '\t' && val.charAt(i) != '\n')
				{
					containsSpaces = false;
				}
			}
			
			if (containsSpaces)
				result = new ResultValue(Token.BOOLEAN, "T");
			else
				result = new ResultValue(Token.BOOLEAN, "F");
		}
		else 
		{
			while (parenthesisCounter > 0)
			{
				if (scan.currentToken.tokenStr.equals(";"))
					error("Malformed LENGTH statement");
				
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
		//System.out.println(result.iDataType + " " + result.toString());
		
		return result;
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
		
		// Each of these keys is associated with an idnetifier in the current scope.
		// Remove their corresponding entries from the storage object and
		// SymbolTable.
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
}

package havabol;

import java.util.HashMap;

public class ResultValue {
	public int iDataType;
	public Object value;
	//Numeric numValue;
	public boolean isNum = false;
	public String terminatingStr;
	
	public ResultValue(){}
	
	public ResultValue(int type, Object val) throws Exception
	{
		iDataType = type;
		value = val;
		if (type == Token.INTEGER || type == Token.FLOAT)
		{
			if(val instanceof Numeric)
			{
				value = val;
			}
			else
			{
				value = new Numeric((String) val, type);
			}
			//numValue = new Numeric(val.toString(), type);
			isNum = true;
		}
		else if (type == Token.IDENTIFIER) 
		{
			if (val instanceof Numeric)
			{
				value = val;
				type = ((Numeric) val).type;
			}
			else if (val instanceof String)
			{
				value = val;
				type = Token.STRING;
			}
			else if (val instanceof Boolean)
			{
				value = val;
				type = Token.BOOLEAN;
			}
			else
				throw new NotANumberException("Not a recognized datatype");
		}
		else
		{
			isNum = false;
		}
		
	}
	
	public ResultValue performOperation(ResultValue rightOperand, String operation) throws Exception
	{
		Object result = 0;
		int resultType = iDataType;
		
		if (isNum == true)
		{
			if (rightOperand == null)
			{
				if (operation.equals("-"))
					result = ((Numeric) value).unaryMinus().toString();
				else
					throw new ImproperOperationException(operation + " is not a valid numeric operation");
			}
			else if (rightOperand.isNum == true)
			{
				if (operation.equals("+"))
					result = ((Numeric) value).add((Numeric) rightOperand.value).toString();
				else if (operation.equals("-"))
					result = ((Numeric) value).subtract((Numeric) rightOperand.value).toString();
				else if (operation.equals("*"))
					result = ((Numeric) value).multiply((Numeric) rightOperand.value).toString();
				else if (operation.equals("/"))
					result = ((Numeric) value).divide((Numeric) rightOperand.value).toString();
				else if (operation.equals("^"))
					result = ((Numeric) value).power((Numeric) rightOperand.value).toString();
				else if (operation.equals("=="))
				{
					if ( ((Numeric) value).equals((Numeric) rightOperand.value) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals("!="))
				{
					if ( ((Numeric) value).notequals((Numeric) rightOperand.value) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals("<"))
				{
					if ( ((Numeric) value).lessthan((Numeric) rightOperand.value) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals("<="))
				{
					if ( ((Numeric) value).lessthanequalto((Numeric) rightOperand.value) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals(">"))
				{
					if ( ((Numeric) value).greaterthan((Numeric) rightOperand.value) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals(">="))
				{
					if ( ((Numeric) value).greaterthanequalto((Numeric) rightOperand.value) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else
					throw new ImproperOperationException(operation + " is not a valid numeric operation");
			}
			else if (rightOperand.iDataType == Token.STRING)
			{
				Numeric tempRightOperand = new Numeric((String) rightOperand.value, rightOperand.iDataType);
				if (operation.equals("+"))
					result = ((Numeric) value).add(tempRightOperand).toString();
				else if (operation.equals("-"))
					result = ((Numeric) value).subtract(tempRightOperand).toString();
				else if (operation.equals("*"))
					result = ((Numeric) value).multiply(tempRightOperand).toString();
				else if (operation.equals("/"))
					result = ((Numeric) value).divide(tempRightOperand).toString();
				else if (operation.equals("^"))
					result = ((Numeric) value).power(tempRightOperand).toString();
				else if (operation.equals("=="))
				{
					if ( ((Numeric) value).equals(tempRightOperand) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals("!="))
				{
					if ( ((Numeric) value).notequals(tempRightOperand) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals("<"))
				{
					if ( ((Numeric) value).lessthan(tempRightOperand) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals("<="))
				{
					if ( ((Numeric) value).lessthanequalto(tempRightOperand) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals(">"))
				{
					if ( ((Numeric) value).greaterthan(tempRightOperand) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else if (operation.equals(">="))
				{
					if ( ((Numeric) value).greaterthanequalto(tempRightOperand) == true)
						result = new Boolean(true);
					else
						result = new Boolean(false);
					resultType = Token.BOOLEAN;
				}
				else
					throw new ImproperOperationException(operation + " is not a valid numeric operation");
			}
			else
			{
				throw new ImproperOperationException("Operand's datatype is inappropriate for " 
						+ operation + " operations");
			}
		}
		else if (iDataType == Token.BOOLEAN)
		{
			if (rightOperand == null)
			{
				if (operation.equals("not"))
				{
					if (value.equals(true))
						result = new Boolean(false);
					else if (value.equals(false))
						result = new Boolean(true);
					else
						throw new ImproperOperationException("Operand's datatype is inappropriate for " 
								+ operation + " operations");
				}
				else
					throw new ImproperOperationException(operation + " is not a valid boolean operation");
			}
			else if (rightOperand.iDataType == Token.BOOLEAN)
			{
				result = performBooleanOperation(rightOperand, operation);
				if (result == null)
					throw new ImproperOperationException("Operand's datatype is inappropriate for " 
							+ operation + " operations");
			}
			else
				throw new ImproperOperationException("Operand's datatype is inappropriate for " 
						+ operation + " operations");
		}
		else if (iDataType == Token.STRING)
		{
			String rightOperStr = "";
			
			if (rightOperand.iDataType == Token.STRING)
				rightOperStr = ((StringBuilder) rightOperand.value).toString();
			else if (rightOperand.iDataType == Token.BOOLEAN)
				rightOperStr = (String) rightOperand.value;
			else if (rightOperand.isNum)
				rightOperStr = ((Numeric) rightOperand.value).toString();
			else
				throw new ImproperOperationException("Operand's datatype is inappropriate for " 
						+ operation + " operations");
			
			if (operation.equals("#"))
				result = new StringBuilder(((StringBuilder) value).toString().concat(rightOperStr));
			else if (operation.equals("=="))
			{
				if (((StringBuilder) value).toString().equals(rightOperStr))
					result = new Boolean(true);
				else
					result = new Boolean(false);
				resultType = Token.BOOLEAN;
			}
			else if (operation.equals("!="))
			{
				if (!((StringBuilder) value).toString().equals(rightOperStr))
					result = new Boolean(true);
				else
					result = new Boolean(false);
				resultType = Token.BOOLEAN;
			}
			else if (operation.equals("<"))
			{
				if (((StringBuilder) value).toString().compareTo(rightOperStr) < 0)
					result = new Boolean(true);
				else
					result = new Boolean(false);
				resultType = Token.BOOLEAN;
			}
			else if (operation.equals("<="))
			{
				if (((StringBuilder) value).toString().compareTo(rightOperStr) <= 0)
					result = new Boolean(true);
				else
					result = new Boolean(false);
				resultType = Token.BOOLEAN;
			}
			else if (operation.equals(">"))
			{
				if (((StringBuilder) value).toString().compareTo(rightOperStr) > 0)
					result = new Boolean(true);
				else
					result = new Boolean(false);
				resultType = Token.BOOLEAN;
			}
			else if (operation.equals(">="))
			{
				if (((StringBuilder) value).toString().compareTo(rightOperStr) >= 0)
					result = new Boolean(true);
				else
					result = new Boolean(false);
				resultType = Token.BOOLEAN;
			}
			else
				throw new ImproperOperationException(operation + " is not a valid string operation");
		}
		else 
		{
			throw new ImproperOperationException("Operand's datatype is inappropriate for " 
					+ operation + " operations");
		}
	
		return new ResultValue(resultType, result);
	}
	
	public Object performBooleanOperation(ResultValue rightOperand, String operation)
	{
//		boolean leftOper, rightOper;
//		if (value.equals("T"))
//			leftOper = true;
//		else
//			leftOper = false;
//		if (rightOperand.value.equals("T"))
//			rightOper = true;
//		else
//			rightOper = false;
		
		if (operation.equals("and"))
		{
			if ((Boolean) value && (Boolean) rightOperand.value)
				return new Boolean(true);
			else
				return new Boolean(false);
		}
		else if (operation.equals("or"))
		{
			if ((Boolean) value || (Boolean) rightOperand.value)
				return new Boolean(true);
			else
				return new Boolean(false);
		}
		else if (operation.equals("=="))
		{
			if (value.equals(rightOperand.value))
				return new Boolean(true);
			else
				return new Boolean(false);
		}
		else if (operation.equals("!="))
		{
			if (!value.equals(rightOperand.value))
				return new Boolean(true);
			else
				return new Boolean(false);
		}
		else
			return null;
	}
	
	@Override
	public String toString() {
		return Token.strSubClassifM[iDataType] + " " + value.toString();
	}
}

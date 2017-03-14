package havabol;

import java.util.HashMap;

public class ResultValue {
	public int iDataType;
	public Object value;
	//Numeric numValue;
	public boolean isNum = false;
	
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
		else
		{
			//numValue = null;
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
						result = "T";
					else
						result = "F";
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
						result = "T";
					else
						result = "F";
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
				if (operation.equals("!"))
				{
					if (value.equals("T"))
						result = "F";
					else if (value.equals("F"))
						result = "T";
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
				if (result.equals(""))
					throw new ImproperOperationException("Operand's datatype is inappropriate for " 
							+ operation + " operations");
			}
			else
				throw new ImproperOperationException("Operand's datatype is inappropriate for " 
						+ operation + " operations");
		}
		else 
		{
			throw new ImproperOperationException("Operand's datatype is inappropriate for " 
					+ operation + " operations");
		}
	
		return new ResultValue(resultType, result);
	}
	
	public String performBooleanOperation(ResultValue rightOperand, String operation)
	{
		boolean o1, o2;
		if (value.equals("T"))
			o1 = true;
		else
			o1 = false;
		if (rightOperand.value.equals("T"))
			o2 = true;
		else
			o2 = false;
		if (operation.equals("and"))
		{
			if (o1 && o2)
				return "T";
			else
				return "F";
		}
		else if (operation.equals("or"))
		{
			if (o1 || o2)
				return "T";
			else
				return "F";
		}
		else
			return "";
	}
	
	@Override
	public String toString() {
		return Token.strSubClassifM[iDataType] + " " + value.toString();
	}
}

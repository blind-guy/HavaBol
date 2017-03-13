package havabol;
/**
 * 
 * @author Elijah Arnold
 *
 * Is the Numeric class used by Havabol for representing and converting numeric
 * values internally. 
 * 
 * See: Parsing Part 2 lecture notes for more information.
 */
public class Numeric
{
	public int intValue;
	public double doubleValue;
	public String stringValue;
	public int type;
	
	// We need to have an empty constructor in parsing with the type set to an invalid
	// number.
	public Numeric()
	{
		stringValue = "";
		int type = -1;
	}
	
	public Numeric(String token, int type) throws Exception
	{
		stringValue = token;
		if(type == Token.INTEGER)
		{
			intValue = Integer.parseInt(token);
			doubleValue = (double) intValue;
		}
		else if(type == Token.FLOAT)
		{
			doubleValue = Double.parseDouble(token);
			intValue = (int) doubleValue;
		}
		else if (type == Token.STRING) 
		{
			if (token.contains("."))
			{
				type = Token.FLOAT;
				doubleValue = Double.parseDouble(token);
				intValue = (int) doubleValue;
			}
			else
			{
				type = Token.INTEGER;
				intValue = Integer.parseInt(token);
				doubleValue = (double) intValue;
			}
		}
		else
		{
			throw new Exception("attempted to instantiate Numeric using invalid type argument");
		}
		this.type = type;
	}
	
	public Numeric add(Numeric rightOperand) throws Exception
	{
		if (type == Token.INTEGER)
			return new Numeric(Integer.toString(intValue + rightOperand.intValue), type);
		else
			return new Numeric(Double.toString(doubleValue + rightOperand.doubleValue), type);
	}
	
	public Numeric subtract(Numeric rightOperand) throws Exception
	{
		if (type == Token.INTEGER)
			return new Numeric(Integer.toString(intValue - rightOperand.intValue), type);
		else
			return new Numeric(Double.toString(doubleValue - rightOperand.doubleValue), type);
	}
	
	public Numeric multiply(Numeric rightOperand) throws Exception
	{
		if (type == Token.INTEGER)
			return new Numeric(Integer.toString(intValue * rightOperand.intValue), type);
		else
			return new Numeric(Double.toString(doubleValue * rightOperand.doubleValue), type);
	}
	
	public Numeric divide(Numeric rightOperand) throws Exception
	{
		if (rightOperand.doubleValue == 0.0)
			throw new DivideByZeroException("Cannot divide by zero");
		
		if (type == Token.INTEGER)
			return new Numeric(Integer.toString(intValue / rightOperand.intValue), type);
		else
			return new Numeric(Double.toString(doubleValue / rightOperand.doubleValue), type);
	}
	
	public Numeric power(Numeric rightOperand) throws Exception
	{
		if (type == Token.INTEGER)
		{
			double tempResult = Math.pow(intValue, rightOperand.intValue);
			return new Numeric(Integer.toString((int) tempResult), type);
		}
		else
			return new Numeric(Double.toString(Math.pow(doubleValue, rightOperand.doubleValue)), type);
	}
	
	public Numeric unaryMinus() throws Exception
	{
		String val = "-" + stringValue;
		return new Numeric(val, type);
	}
	
	public boolean equals(Numeric rightOperand)
	{
		if (type == Token.INTEGER)
		{
			return intValue == rightOperand.intValue;
		}
		else
			return doubleValue == rightOperand.doubleValue;
	}
	
	public boolean lessthan(Numeric rightOperand)
	{
		if (type == Token.INTEGER)
		{
			return intValue < rightOperand.intValue;
		}
		else
			return doubleValue < rightOperand.doubleValue;
	}
	
	public boolean greaterthan(Numeric rightOperand)
	{
		if (type == Token.INTEGER)
		{
			return intValue > rightOperand.intValue;
		}
		else
			return doubleValue > rightOperand.doubleValue;
	}
	
	public boolean lessthanequalto(Numeric rightOperand)
	{
		if (type == Token.INTEGER)
		{
			return intValue <= rightOperand.intValue;
		}
		else
			return doubleValue <= rightOperand.doubleValue;
	}
	
	public boolean greaterthanequalto(Numeric rightOperand)
	{
		if (type == Token.INTEGER)
		{
			return intValue >= rightOperand.intValue;
		}
		else
			return doubleValue >= rightOperand.doubleValue;
	}
	
	@Override
	public String toString() 
	{
		if (type == Token.INTEGER)
			return stringValue;
		else if (type == Token.FLOAT)
			return stringValue;
		else
			return "This is not a number. ???";
	}
	
}

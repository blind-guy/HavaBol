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
	
	public Numeric(String token, int type) throws Exception
	{
		stringValue = token;
		if(type == Token.INTEGER)
		{
			intValue = Integer.parseInt(token);
			doubleValue = (int) intValue;
		}
		else if(type == Token.FLOAT)
		{
			doubleValue = Double.parseDouble(token);
			intValue = (int) doubleValue;
		}
		else
		{
			throw new Exception("attempted to instantiate Numeric using invalid type argument");
		}
		this.type = type;
	}
	
	// TODO: we need to consider the operations and conversations this class requires
	// for maintaining its integrity.
}

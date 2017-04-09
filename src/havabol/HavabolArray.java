package havabol;

import java.util.ArrayList;

public class HavabolArray
{
	private ArrayList<ResultValue> array = new ArrayList<ResultValue>();
	public int dclType = -1;
	public int maxSize = 0;
	private int currentMaxIndex = -1;
	private boolean isUnbounded = false;
	private int elem = -1;
	
	public HavabolArray(int type)
	{
		this.dclType = type;
	}

	public void unsafeAppend(ResultValue resValToStore)
	{
		array.add(resValToStore);
		currentMaxIndex++;
		if(resValToStore != null)
		{
			elem = currentMaxIndex + 1;
		}
	}
	
	@Override
	public String toString()
	{
		String ret = "TYPE: " + Token.strSubClassifM[dclType] + ", " +
					 "MAXELEM: " + maxSize + ", " +
					 "ELEM: " + elem + ", " +
					 "UNBOUNDED: " + isUnbounded;
		for(ResultValue val: array)
		{
			String stringToAppend = "";
			if(val == null)
			{
				stringToAppend = "";
			}
			else if(val.iDataType == Token.BOOLEAN)
			{
				stringToAppend = ((Boolean) val.value).toString();
			}
			else if(val.iDataType == Token.STRING)
			{
				stringToAppend = "\"" + ((StringBuilder) val.value).toString() + "\"";
			}
			else if(val.iDataType == Token.INTEGER ||
					val.iDataType == Token.FLOAT)
			{
				stringToAppend = ((Numeric) val.value).stringValue;
			}
			ret = ret + ", " + stringToAppend;
		}
		
		return ret;
	}
	
	public ResultValue getElem() throws Exception
	{
		return new ResultValue(
						Token.INTEGER,
						new Numeric(Integer.toString(this.elem), Token.INTEGER)
				   );
		
	}
	
	public ResultValue getMaxElem() throws Exception
	{
		return new ResultValue(
					Token.INTEGER,
					new Numeric(Integer.toString(this.maxSize), Token.INTEGER)
					);
	}
	
	public ResultValue getElement(int index)
	{
		if((index < 0) || (!isUnbounded && index >= this.maxSize))
		{
			throw new IndexOutOfBoundsException();
		}
		else
		{
			return array.get(index);
		}
	}
}

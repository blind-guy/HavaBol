package havabol;

import java.util.ArrayList;

public class HavabolArray
{
	private ArrayList<ResultValue> array = new ArrayList<ResultValue>();
	public int dclType = -1;
	public int maxSize = 0;

	private int currentMaxIndex = -1;
	private boolean isUnbounded = false;
	
	public HavabolArray(int type)
	{
		this.dclType = type;
	}

	public void unsafeAppend(ResultValue resValToStore)
	{
		System.out.println("APPENDED " + resValToStore + " to array.");
		array.add(resValToStore);
		currentMaxIndex++;
	}
	
	@Override
	public String toString()
	{
		String ret = "TYPE:      " + Token.strSubClassifM[dclType] + "\n" +
					 "MAXSIZE:   " + maxSize + "\n" +
					 "MAXELEM:   " + currentMaxIndex + "\n" +
					 "UNBOUNDED: " + isUnbounded + "\n";
		for(ResultValue val: array)
		{
			ret = ret + "\t" + val + "\n";
		}
		
		return ret;
	}
}

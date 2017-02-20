package havabol;

public class STControl extends STEntry
{
	// The sub-classification for this STControl.
	public int subClassif;
	
	public STControl(String symbol, int primClassif, int subClassif)
	{
		super(symbol, primClassif);
		this.subClassif = subClassif;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " subClassif: " + subClassif;
	}
}

package havabol;

public class STEntry
{	
	// The String representation of the STEntry.
	public String symbol;
	
	// The prime classification for the STEntry, corresponds to what's 
	// available in havabol.Token;
	public int primClassif;
	
	public STEntry(String symbol, int primClassif)
	{
		this.symbol = symbol;
		this.primClassif = primClassif;
	}
}

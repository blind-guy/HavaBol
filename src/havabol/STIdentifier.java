package havabol;

public class STIdentifier extends STEntry
{
	// TODO: more has to be done here
	public STIdentifier(String symbol, int primClassif) {
		super(symbol, primClassif);
		
	}

	// The declaration type for this STIdentifier.
	public int dclType;
	
	// The data structure type for this STIdentifier.
	public int structure;
	
	// The parameter type.
	public int parm;
	
	// The non-local base address reference. (0 - local, 1 - surrounding... k - surrounding, 99 - global)
	public int nonLocal;
}

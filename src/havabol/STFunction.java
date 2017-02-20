package havabol;

import java.util.ArrayList;

public class STFunction extends STEntry
{
	// TODO: more has to be done here
	public STFunction(String symbol, int primClassif, int definedBy, int returnType) {
		super(symbol, primClassif);
		this.definedBy = definedBy;
		this.returnType = returnType;
	}

	// The return type for this STFunction.
	public int returnType;
	
	// Indicates who/what defined this STFunction.
	public int definedBy;
	
	// The number of arguments for this STFunction.
	public int numArgs;
	
	// A list of parameters for this function.
	public ArrayList<Object> parmList;
	
	// A SymbolTble reference for this STFunction if it is user-defined.
	public SymbolTable symbolTable = null;
	
	@Override
	public String toString()
	{
		return super.toString() + " definedBy: " + definedBy + " return type: " + returnType;
	}
}

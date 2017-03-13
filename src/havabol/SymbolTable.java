package havabol;
//TODO See SymbolTable notes for a full description of this class.

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable
{
	// The SymbolTable which will map all symbols to an entry if they're
	// currently defined.
	//
	// TODO: For now this does not handle non-globals which will be implemented later.
	private HashMap<String, STEntry> ht = new HashMap<String, STEntry>();
	
	// A basic constructor which needs only call initGlobal() to put in
	// all language-defined entries.
	public SymbolTable()
	{
		initGlobal();
	}

	/**
	 * Stores the symbol and its corresponding entry in the SymbolTable.
	 * 
	 * It's important to note that classification of symbols is done by the parser,
	 * not the Scanner or SymbolTable class.
	 * @param symbol
	 * @param entry
	 */
	public void putSymbol(String symbol, STEntry entry)
	{
		ht.put(symbol, entry);
	}
	
	/**
	 * 
	 * @param symbol representing the key for looking up this entry
	 * @return an STEntry object stored for this key
	 */
	public STEntry getEntry(String symbol)
	{
		return ht.get(symbol);
	}
	
	/**
	 * 
	 */
	private void initGlobal()
	{
		java.util.Scanner in = null;
		try 
		{
			in = new java.util.Scanner(new File("docs/lang/symbols.txt"));
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("symbols.txt does not exist in tests folder. Fatal error.");
			System.exit(-1);
		}
		
		String strName, strPrimClassif, strSubClassif;
		int iPrimClassif=0, iSubClassif=0;
		HashMap<String, Integer> hmClassif = new HashMap<String, Integer>();
		for (int i=0; i<Token.strPrimClassifM.length; i++)
			hmClassif.put(Token.strPrimClassifM[i], i);
		for (int i=0; i<Token.strSubClassifM.length; i++)
			hmClassif.put(Token.strSubClassifM[i], i);
		
		while(in.hasNext())
		{
			strName = in.next();
			strPrimClassif = in.next();
			strSubClassif = in.next();
			iPrimClassif = hmClassif.get(strPrimClassif.toUpperCase());
			
			/*
			System.out.println(strName + " strPrimClassif " 
					+ " " + iPrimClassif + " " + strSubClassif);
			*/
			if (iPrimClassif == Token.FUNCTION) // if it's a funciton, set iSubClassif to return type
				iSubClassif = builtInFuncReturnType(strSubClassif);
			else if (!strSubClassif.equals("-"))
				iSubClassif = hmClassif.get(strSubClassif.toUpperCase());
			else
				iSubClassif = 0;
			// System.out.println(iSubClassif);
		
			if (iPrimClassif == Token.FUNCTION) 
			{
				// note that iSubClassif has the return type
				ht.put(strName, new STFunction(strName, iPrimClassif, Token.BUILTIN, iSubClassif));
			}
			else if (iPrimClassif == Token.CONTROL) 
			{
				ht.put(strName, new STControl(strName, iPrimClassif, iSubClassif));
			}
			else if (iPrimClassif == Token.OPERATOR)
			{
				ht.put(strName, new STEntry(strName, iPrimClassif));
			}
			else if (iPrimClassif == Token.DEBUG)
			{
				ht.put(strName, new STEntry(strName, iPrimClassif));
			}
			else
			{
				System.err.println("could not classify token in symbols.txt file");
				System.exit(-1);
			}
			
		}
		
		/* for testing to see if symbols file is working
		for (String key : ht.keySet())
		{
			System.out.println(key + " " + ht.get(key).toString());
		}
		System.exit(-1);
		*/
		
		// TODO: an example of how this should work from the SymbolTable notes
		// which also contain a full list of Havabol-defined entries.
		//
		// Copying from his notes yielded interesting results with punctuation so
		// this example is pretty assy.
		/**
		 * ht.put(&quot;if&quot;, new STControl(&quot;if&quot;,Token.CONTROL,Token.FLOW));
         * ht.put(&quot;endif&quot;, new STControl(&quot;endif&quot;,Token.CONTROL,Token.END));
		 * ht.put(&quot;for&quot;, new STControl(&quot;for&quot;,Token.CONTROL,Token.FLOW));
		 * ...
		 * ht.put(&quot;Int&quot;, new STControl(&quot;Int&quot;,Token.CONTROL,Token.DECLARE));
		 * ht.put(&quot;Float&quot;, new STControl(&quot;Float&quot;,Token.CONTROL,Token.DECLARE));
		 * ...
		 * ht.put(&quot;print&quot;, new STFunction(&quot;print&quot;,Token.FUNCTION,Token.VOID, Token.BUILTIN, VAR_ARGS));
		 * ...
		 * ht.put(&quot;and&quot;, new STEntry(&quot;and&quot;, Token.OPERATOR));
		 * ht.put(&quot;or&quot;, new STEntry(&quot;or&quot;, Token.OPERATOR));
		 * ...
		 */
		// ffs there has to be a better way, what the actual fuck is this nonsense 
	}
	
	/**
	 * builtInFuncReturnType returns the return type of the given builtin function. <p>
	 * If this function is given a non-builtin function, it returns -1.
	 * @param func String containing the name of the builtin function
	 * @return Token.* for corresponding return type, -1 if the function has not been defined
	 * for HavaBol
	 */
	public int builtInFuncReturnType(String func)
	{
		if (func.equals("Void"))
			return Token.VOID;
		else if (func.equals("Int"))
		{
			return Token.INTEGER;
		}
		else
			return -1;
	}
}

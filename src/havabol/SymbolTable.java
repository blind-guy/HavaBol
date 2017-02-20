package havabol;
//TODO See SymbolTable notes for a full description of this class.

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

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

	// still working on initGlobal. don't expect it to run
	private void initGlobal()
	{
		java.util.Scanner in = null;
		try 
		{
			in = new java.util.Scanner(new File("/tests/symbols.txt"));
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
			if (!strSubClassif.equals("-"))
				iSubClassif = hmClassif.get(strSubClassif.toUpperCase());
			else
				iSubClassif = 0;
			
			if (iSubClassif == 0)
			{
				ht.put(strName, new STEntry(strName, Token.OPERATOR));
			}
			else
			{
				if (iPrimClassif == Token.FUNCTION) 
				{
					//ht.put(strName, new STFunction(strName, iPrimClassif, iSubClassif));
				}
				else if (iPrimClassif == Token.CONTROL) 
				{
					ht.put(strName, new STControl(strName, iPrimClassif, iSubClassif));
				}
			}
			
		}
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
}

package havabol;
//TODO See SymbolTable notes for a full description of this class.

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

	private void initGlobal()
	{
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

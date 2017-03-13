/**
 * DESCRIPTION:
 * 
 * Much of this code is taken from Larry Clark's compiler construction course
 * notes "Parsing Part 2". It is an Exception class for the interpreter project 
 * which formats and prints a suitable error message containing a message for the 
 * programmer, location of the error, and the source file with the error.
 */
package havabol;

public class ParserException extends Exception 
{
	/**
	 * This is only here because I want eclipse to stop bothering me abut
	 * something I don't understand.
	 */
	private static final long serialVersionUID = 1L;
	
	public int iLineNr;
	public String diagnostic;
	public String sourceFileName;
	
	public ParserException(int iLineNr, String diagnostic, String sourceFileName)
	{
		this.iLineNr = iLineNr;
	    this.diagnostic = diagnostic;
	    this.sourceFileName = sourceFileName;
	}
	
	public String toString()
	{
	    StringBuffer sb = new StringBuffer();  
	    
	    sb.append("Line ");
	    sb.append(Integer.toString(iLineNr));
	    sb.append(" ");
	    sb.append(diagnostic);
	    sb.append(", File: ");
	    sb.append(sourceFileName);
	
	    return sb.toString();
	}
}

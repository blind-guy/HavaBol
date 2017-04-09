
package havabol;

import java.util.*;

public class Token
{
    public String tokenStr = "";
    public int primClassif = 0;
    public int subClassif = 0;
    public int iSourceLineNr = 0;
    public int iColPos = 0;
    
    public int precedence = 0;
    public int stackPrecedence = 0;
    
    // Constants for primClassif
    public static final int OPERAND = 2;    // constants, identifier
    public static final int OPERATOR = 3;   // + - * / < > = ! 
    public static final int SEPARATOR = 4;  // ( ) , : ; [ ]
    public static final int FUNCTION = 5;   // TBD
    public static final int CONTROL = 6;    // TBD
    public static final int DEBUG = 7;      // DEBUG statement
    public static final int EOF = 8;      	// EOF statement
    public static final int RT_PAREN = 9;   // TBD
    
    // Constants for OPERAND's subClassif
    public static final int IDENTIFIER = 1;
    public static final int INTEGER    = 2; // integer constant
    public static final int FLOAT      = 3; // float constant
    public static final int BOOLEAN    = 4; // boolean constant
    public static final int STRING     = 5; // string constant
    public static final int DATE       = 6; // date constant
    public static final int VOID       = 7; // void
    // Constants for CONTROL's subClassif  (after Pgm 1)
    public static final int FLOW       = 10;// flow statement (e.g., if)
    public static final int END        = 11;// end statement (e.g., endif)
    public static final int DECLARE    = 12;// declare statement (e.g., Int)
    // Constants for FUNCTION's subClassif (definedby)
    public static final int BUILTIN    = 13;// builtin function (e.g., print)
    public static final int USER       = 14;// user defined
    
    // array of primClassif string values for the constants
    public static final String[] strPrimClassifM = 
        {"Undefined"
        	, "IDENTIFIER"  // 1
            , "OPERAND"     // 2
            , "OPERATOR"    // 3
            , "SEPARATOR"   // 4
            , "FUNCTION"    // 5
            , "CONTROL"     // 6
            , "DEBUG"       // 7
            , "EOF"         // 8
        };
    public static final int PRIM_CLASS_MAX = 8;
    // array of subClassif string values for the constants
    public static final String[] strSubClassifM = 
        {"Undefined"
            , "IDENTFIER"   // 1
            , "INTEGER"     // 2
            , "FLOAT"       // 3
            , "BOOLEAN"     // 4
            , "STRING"      // 5
            , "DATE"        // 6
            , "Void"        // 7
            , "**not used**"// 8
            , "**not used**"// 9 
            , "FLOW"        //10
            , "END"         //11
            , "DECLARE"     //12
        }; 
    public static final int OPERAND_SUB_CLASS_MIN = 1;
    public static final int OPERAND_SUB_CLASS_MAX = 7;
    public static final int CONTROL_SUB_CLASS_MIN = 10;
    public static final int CONTROL_SUB_CLASS_MAX = 12;
 
    public Token(String value)
    {
        this.tokenStr = value;
        // ??
        Precedence prec = new Precedence();
        if (prec.containsKey(value)) 
        {
        	precedence = prec.getTokenPrecedence(value);
        	stackPrecedence = prec.getStackPrecedence(value);
        }
        else 
        {
        	precedence = 0;
        	stackPrecedence = 0;
        }
        prec = null;
    }
    public Token()
    {
        this("");   // invoke the other constructor
    }
    
    public void printToken()
    {
        String primClassifStr;
        String subClassifStr;
        // convert the primClassif to a string
        if (primClassif >= 0 
            && primClassif <= PRIM_CLASS_MAX)
            primClassifStr = strPrimClassifM[primClassif];
        else
            primClassifStr = "**garbage**";

        // convert the subClassif to a string
        switch(primClassif)
        {
            case Token.OPERAND:
                if (subClassif >= OPERAND_SUB_CLASS_MIN 
                        && subClassif <= OPERAND_SUB_CLASS_MAX)
                    subClassifStr = strSubClassifM[subClassif];
                else    
                    subClassifStr = "**garbage**";
                break;
            case Token.CONTROL:
                if (subClassif >= CONTROL_SUB_CLASS_MIN 
                        && subClassif <= CONTROL_SUB_CLASS_MAX)
                    subClassifStr = strSubClassifM[subClassif];
                else    
                    subClassifStr = "**garbage**";
                break;
            case Token.FUNCTION:
                if (subClassif == BUILTIN)
                    subClassifStr = "BUILTIN";
                else if (subClassif == USER)
                    subClassifStr = "USER";
                else
                    subClassifStr = "**garbage**";
                break;
            case Token.IDENTIFIER:
            	subClassifStr = strSubClassifM[subClassif];
            	break;	
            default:
                subClassifStr = "-";
        }
        System.out.printf("%-11s %-12s "
            , primClassifStr
            , subClassifStr);
        if(primClassif == Token.OPERAND && subClassif == Token.STRING)
        {
        	hexPrint(25, tokenStr);
        }
        else
        {
        	System.out.printf("%s \n", tokenStr);
        }
    }
    
	/**
	 * This takes the token passed as a parameter and creates a copy of that token 
	 * and returns it.
	 * @param oldToken
	 * @return
	 */
	public static Token copyToken(Token oldToken)
	{
		Token scRtToken = new Token();
		
		scRtToken.tokenStr 		= oldToken.tokenStr;
		scRtToken.primClassif 	= oldToken.primClassif;
		scRtToken.subClassif 	= oldToken.subClassif;
		scRtToken.iColPos		= oldToken.iColPos;
		scRtToken.iSourceLineNr	= oldToken.iSourceLineNr;
		scRtToken.precedence	= oldToken.precedence;
		scRtToken.stackPrecedence = oldToken.stackPrecedence;
		
		return scRtToken;
	}
	
    /**
     * Prints a string that may contain non-printable characters as two lines.  
     * <p>
     * On the first line, it prints printable characters by simply
     * printing the character.  For non-printable characters
     * in the string, it prints ". ".  
     * <p>
     * The second line prints a two character hex value for the non printable
     * characters in the string line.  For the printable characters, it prints 
     * a space.
     * <p>
     * It is sometimes necessary to print the first line on the end of
     * an existing line of output.  This would make it difficult to properly 
     * align the second line of output.  The indent parameter is for indenting 
     * the second line.
     * <p><blockquote><pre>
     * Example for the string "\tTX\tTexas\n"
     *      . TX. Texas.
     *      09  09     0A
     * </pre></blockquote><p>    
     * @param indent  the number of spaces to indent the second printed line
     * @param str     the string to print which may contain non-printable characters
     */
    public void hexPrint(int indent, String str)
    {
        int len = str.length();
        char [] charray = str.toCharArray();
        char ch;
        // print each character in the string
        for (int i = 0; i < len; i++)
        {
            ch = charray[i];
            if (ch > 31 && ch < 127)   // ASCII printable characters
                System.out.printf("%c", ch);
            else
                System.out.printf(". ");
        }
        System.out.printf("\n");
        // indent the second line to the number of specified spaces
        for (int i = 0; i < indent; i++)
        {
            System.out.printf(" ");
        }
        // print the second line.  Non-printable characters will be shown
        // as their hex value.  Printable will simply be a space
        for (int i = 0; i < len; i++)
        {
            ch = charray[i];
            // only deal with the printable characters
            if (ch > 31 && ch < 127)   // ASCII printable characters
                System.out.printf(" ", ch);
            else
                System.out.printf("%02X", (int) ch);
        }    
        System.out.printf("\n");
    }
    
    public void setPrecedence() {
    	Precedence prec = new Precedence();
        if (prec.containsKey(tokenStr)) 
        {
        	//System.out.println("yes" + tokenStr);
        	precedence = prec.getTokenPrecedence(tokenStr);
        	stackPrecedence = prec.getStackPrecedence(tokenStr);
        	//System.out.println("prec: " + precedence + " stackPrec: " + stackPrecedence);
        }
        else 
        {
        	//System.out.println("no" + tokenStr);
        	precedence = 0;
        	stackPrecedence = 0;
        }
        prec = null;
    }
}      
/* 
 * This file is part or JMathLib 
 * 
 * Check it out at http://www.jmathlib.de
 *
 * Author:  stefan@held-mueller.de
 * (c) 2005-2009   
 */
package com.addi.toolbox.jmathlib.system;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;


public class quit extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

	    if ((getNArgIn(operands) == 1)         &&
	        (operands[0] instanceof CharToken)    )
	    {
	            String value = ((CharToken)operands[0]).getElementString(0);

	            // if user calls quit("force") JMathLib will be terminated
	            // immediately without saving anything
	            if (value.equals("force"))
	                System.exit(0);
	    }
	    
	    // run finish script and save local properties
	    globals.getInterpreter().save();
    
	    // exit JMathLib
	    System.exit(0);
	    
		return null;		
	}
}

/*
@GROUP
system
@SYNTAX
quit
@DOC
exits JMathLib
@EXAMPLE
<programlisting>
quit
</programlisting>
@NOTES
quit("force") will terminate JMathLib 
without saving any variables of saving any changed settings.
@SEE
exit
*/

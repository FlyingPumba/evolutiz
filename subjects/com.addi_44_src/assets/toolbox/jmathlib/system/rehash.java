/* 
 * This file is part or JMathLib 
 * 
 * Check it out at http://www.jmathlib.de
 *
 * Author:  
 * (c) 2005-2009   
 */
package com.addi.toolbox.jmathlib.system;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;


public class rehash extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

	    globals.getFunctionManager().checkAndRehashTimeStamps();

        return null;
        
	} // end eval
}

/*
@GROUP
system
@SYNTAX
rehash
@DOC
rehases all m-files in the cache for user functions
@EXAMPLES
<programlisting>
rehash
</programlisting>
@SEE
path, rmpath, addpath 
*/


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
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


public class print_usage extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{	

        throwMathLibException("print_usage");
		
        return  null;
	}
}

/*
@GROUP
system
@SYNTAX
print_usage
@DOC
print out the valid usage of the calling function
@NOTES
@EXAMPLES
@SEE
warning, error
*/


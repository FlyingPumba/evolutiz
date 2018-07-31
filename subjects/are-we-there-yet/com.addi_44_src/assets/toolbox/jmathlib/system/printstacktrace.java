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
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for displaying error messages
aborts the current function being processed*/
public class printstacktrace extends ExternalFunction
{
	/**write operand to main display then abort processing
	@param operand[n] = error messages to display*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{	
        String val = "";
		if(operands[0] instanceof CharToken)
		{
			val = ((CharToken)operands[0]).getElementString(0) + "\n";
		}	
        
        //val += getContextList().getStackTrace();
		return  new CharToken(val);
	}
}

/*
@GROUP
system
@SYNTAX
printstacktrace(message)
@DOC
Displays message and the current execution stack trace.
@NOTES
@EXAMPLES
Error("There has been an error")
There has been an error
@SEE
warning, error
*/


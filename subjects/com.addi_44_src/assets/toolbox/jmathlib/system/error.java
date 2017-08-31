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
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for displaying error messages
aborts the current function being processed*/
public class error extends ExternalFunction
{
	/**write operand to main display then abort processing
	@param operand[n] = error messages to display*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{	
		if(operands[0] instanceof CharToken)
		{
			String val = ((CharToken)operands[0]).getElementString(0);
			if(val.equals(""))
				return new CharToken("");
		}	
		Errors.throwMathLibException(ERR_USER_ERROR, new Object[] {operands[0]});
		return  null;
	}
}

/*
@GROUP
system
@SYNTAX
error(message)
@DOC
Displays message and aborts the current operation.
@NOTES
@EXAMPLES
Error("There has been an error")
There has been an error
@SEE
warning, exit
*/


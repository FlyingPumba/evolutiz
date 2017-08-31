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
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for displaying error messages
aborts the current function being processed*/
public class usage extends ExternalFunction
{
	/**write operand to main display then abort processing
	@param operand[0] = error messages to display*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{	
        Errors.throwUsageException(operands[0].toString());
		//Errors.throwMathLibException(ERR_USER_ERROR, new Object[] {operands[0]});
		return  null;
	}
}

/*
@GROUP
system
@SYNTAX
USAGE(message)
@DOC
Used within script files to display the paramaters for the script.
@NOTES
@EXAMPLES
Usage("Function(paramaters)")
@SEE
error, warning
*/


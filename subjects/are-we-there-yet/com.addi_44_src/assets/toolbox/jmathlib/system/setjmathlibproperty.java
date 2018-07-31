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


/**External function to get a enviroment variable*/
public class setjmathlibproperty extends ExternalFunction
{
	/**Returns an enviroment variable
	@param operand[0] = the name of the variable
	@param operand[1] = a default value (optional)
	@return the enviroment value*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if (getNArgIn(operands)!=2)
            throwMathLibException("setjmathlibproperty: number of arguments != 2");
        

        if ( (!(operands[0] instanceof CharToken)) &&
             (!(operands[1] instanceof CharToken))    )
            throwMathLibException("setjmathlibproperty: arguments must be strings");

			String name = ((CharToken)operands[0]).getElementString(0);
            String prop = ((CharToken)operands[1]).getElementString(0);
		
            globals.setProperty(name, prop);
			
			
		return result;
	}
}

/*
@GROUP
system
@SYNTAX
setjmathlibproperty(property name, value)
@DOC
Returns the value of the enviromental variable variablename.
@NOTES
@EXAMPLES
GETENV("HOME")= "/home/user"
@SEE
getjmathlibproperty, getenv
*/


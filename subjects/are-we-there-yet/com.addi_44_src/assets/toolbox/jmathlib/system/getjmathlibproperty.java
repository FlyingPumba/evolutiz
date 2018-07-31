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
public class getjmathlibproperty extends ExternalFunction
{
	/**Returns an enviroment variable
	@param operand[0] = the name of the variable
	@param operand[1] = a default value (optional)
	@return the enviroment value*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if (getNArgIn(operands)!=1)
            throwMathLibException("getjmathlibproperty: number of arguments != 1");
        

        if (!(operands[0] instanceof CharToken))
            throwMathLibException("getjmathlibproperty: number of arguments != 1");

			String name = ((CharToken)operands[0]).getElementString(0);
			String defaultVal = "";
			
		
			String property = globals.getProperty(name);
			
			result = new CharToken(property);
			
		return result;
	}
}

/*
@GROUP
system
@SYNTAX
getjmathlibproperty(variablename)
@DOC
Returns the value of the enviromental variable variablename.
@NOTES
@EXAMPLES
getlocal("HOME")= "/home/user"
@SEE
getenv, setjmathlibproperty
*/


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
import com.addi.core.interpreter.*;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**External function to get a enviroment variable*/
public class getenv extends ExternalFunction
{
	/**Returns an enviroment variable
	@param operand[0] = the name of the variable
	@param operand[1] = a default value (optional)
	@return the enviroment value*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
		if(getNArgIn(operands) ==0)
		{
		    String s = System.getProperties().toString();
		    return new CharToken(s);
	        // display properties
	        //Enumeration propnames = props.propertyNames();
	        //while (globalPropnames.hasMoreElements())
	        //{
	        //    String propname = (String)globalPropnames.nextElement();
	        //    System.out.println("Property: "+propname+" = "+globalProps.getProperty(propname));
	        //}

		}
		
		if(operands[0] instanceof CharToken)
		{
			String name = ((CharToken)operands[0]).getElementString(0);
			String defaultVal = "";
			
			if(operands.length > 1)
			{
				defaultVal = operands[1].toString();
			}
			
			String property = System.getProperty(name, defaultVal);
			
			result = new CharToken(property);
		}
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[0].getClass().getName()});
			
		return result;
	}
}

/*
@GROUP
system
@SYNTAX
GETENV(variablename)
@DOC
Returns the value of the enviromental variable variablename.
@NOTES
@EXAMPLES
GETENV("HOME")= "/home/user"
@SEE
*/


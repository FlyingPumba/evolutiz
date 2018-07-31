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


/**An example of an external function - it returns the first parameter*/
public class java extends ExternalFunction
{
	/**Executes the function - returning the first parameter
	@param operands - the array of parameters
	@return the result of the function as an OperandToken*/	
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{    
	    
        // load java plugin
	    globals.getPluginsManager().addPlugin("JavaPlugin");
        
        //CCX String result = ((JavaPlugin)globals.getPluginsManager().getPlugin("JavaPlugin")).executeJavaExpression(operands[0].toString());
	    String result = "not supported";
	    
	    return new CharToken(result);
	}
}



/*

java('console.displayText("hello world");')
hello world
ans = null

To access a JMathLib variable enter
x=5
JAVA('global.getVariable("x");')
ans = 5

*/
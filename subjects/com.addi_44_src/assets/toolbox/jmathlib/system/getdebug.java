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
import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;


/**External function for controlling the debug setting for the system*/
public class getdebug extends ExternalFunction
{
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        if (getNArgIn(operands) !=0)
            throwMathLibException("getdebug: number of arguments !=0");
        
       return new LogicalToken(ErrorLogger.getDebug());                
    }
}

/*
@GROUP
system
@SYNTAX
getdebug()
@DOC
returns state of debug logging
@NOTES
@EXAMPLES
getdebug()
@SEE
setdebug
*/


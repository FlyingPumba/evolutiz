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
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**External function for controlling the debug setting for the system*/
public class setdebug extends ExternalFunction
{
    /**Sets the debug flag
    @param operands[0] = 1, show debug info
                         0, to turn debug info off*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        if (getNArgIn(operands) !=1)
            throwMathLibException("setdebug: number of arguments !=1");
        
        if(operands[0] instanceof DoubleNumberToken)
        {
            int debug = ((DoubleNumberToken)operands[0]).getIntValue(0,0);
            
            if(debug == 0)
                ErrorLogger.setDebug(false);
            else
                ErrorLogger.setDebug(true);                                    

            return new DoubleNumberToken(1);                
        }
        else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[0].getClass().getName()});
            
        return null;
    }
}

/*
@GROUP
system
@SYNTAX
setdebug(value)
@DOC
Switches debug output on or off
@NOTES
@EXAMPLES
setdebug(1) turns debug output on
setdebug(0) turns debug output off
@SEE
getdebug
*/


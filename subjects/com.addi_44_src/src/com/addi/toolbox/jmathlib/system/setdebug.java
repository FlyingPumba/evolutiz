// Copyright (C) 2011 Free Software Foundation FSF
//
// This file is part of Addi.
//
// Addi is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or (at
// your option) any later version.
//
// Addi is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Addi. If not, see <http://www.gnu.org/licenses/>.

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


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

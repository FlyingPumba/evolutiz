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

package com.addi.toolbox.general;

import com.addi.core.functions.*;
import com.addi.core.tokens.*;
import com.addi.core.interpreter.GlobalValues;

/**
 * An external function for sending data to AddiPlot
 */
public class clc extends ExternalFunction
{

    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	
    	if (operands.length != 0)
    		throwMathLibException("clc: number of arguements > 0");
    	
    	globals.getInterpreter().displayText("CLEARADDITERMINAL");
        
        return null;
    }
}

/*
@GROUP
general
@SYNTAX
clc()
@DOC
Clear Addi Terminal
@EXAMPLES
<programlisting>
clc()
</programlisting>
@SEE
clc
*/
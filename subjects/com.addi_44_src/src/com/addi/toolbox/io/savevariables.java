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

package com.addi.toolbox.io;


import java.io.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;

public class savevariables extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) > 1)
			throwMathLibException("savevariables: number of arguments > 1");


        String file = "." + File.separator + "variables.mlf";
        
        if (getNArgIn(operands) == 1)
        {
            if (!(operands[0] instanceof CharToken)) 
                throwMathLibException("savevariables: argument must be a string");

            file = ((CharToken)operands[0]).getElementString(0);
        }

        globals.getLocalVariables().saveVariables(file);
        
		return null;		

	} // end eval
}

/*
@GROUP
IO
@SYNTAX
savevariables()
savevariables("name.mlf")
@DOC
saves the variables from the current workspace into a serialized .mlf-file
@EXAMPLES
<programlisting>
savevariables()
</programlisting>
@NOTES
The variables are written as a serialized stream of java objects. Therefor the
format of the .mlf-file is java-specific. The file is also specific to the
versions of MathLib token and classes.
@SEE
loadvariables, csvread, csvwrite
*/


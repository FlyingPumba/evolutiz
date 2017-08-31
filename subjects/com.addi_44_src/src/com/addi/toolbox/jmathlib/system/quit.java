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
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;


public class quit extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

	    if ((getNArgIn(operands) == 1)         &&
	        (operands[0] instanceof CharToken)    )
	    {
	            String value = ((CharToken)operands[0]).getElementString(0);

	            // if user calls quit("force") JMathLib will be terminated
	            // immediately without saving anything
	            if (value.equals("force"))
	                System.exit(0);
	    }
	    
	    // run finish script and save local properties
	    globals.getInterpreter().save();
    
	    // exit JMathLib
	    System.exit(0);
	    
		return null;		
	}
}

/*
@GROUP
system
@SYNTAX
quit
@DOC
exits JMathLib
@EXAMPLE
<programlisting>
quit
</programlisting>
@NOTES
quit("force") will terminate JMathLib 
without saving any variables of saving any changed settings.
@SEE
exit
*/

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

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.VariableToken;


/**An external function for clearing stored variables*/
public class clear extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		Variable var;
		String s=" ";

        if (getNArgIn(operands) != 1)
			throwMathLibException("clear: number of arguments < 1");
            
        // get subcommand or variable name
		if (operands[0] instanceof VariableToken)
		{
			s = ((VariableToken)operands[0]).getName();
			ErrorLogger.debugLine("clear "+s);
		}
		else if(operands[0] instanceof CharToken)
		{
			s = ((CharToken)operands[0]).getValue();
		}

		// check what the user wans to clear
		if (s.equals("variables"))
		{
		    // only clear local variables
            globals.getLocalVariables().clear();
		}
		else if (s.equals("globals"))
		{
		    // clear global variables
		    globals.getGlobalVariables().clear();
		    globals.getLocalVariables().clear();
            //TODO: when removing global variables also remove 
            //     pointers from local to global varaibles in "getLocalVariables"
		}
		else if (s.equals("functions"))
		{
		    // clear cache for m-files, class-files, script-files
		    globals.getFunctionManager().clear();
		}
		else if (s.equals("all"))
		{
		    // clear everything
		    globals.getLocalVariables().clear();
		    globals.getGlobalVariables().clear();
		    globals.getFunctionManager().clear();

		}
		else if (!s.equals(" "))
		{
			// remove one variable from local workspace
		    globals.getLocalVariables().remove(s);
		}
		else
		{
		    // clear without any arguments only clears the local workspace
		    globals.getLocalVariables().clear();
		}

		return null;		
	}
}

/*
@GROUP
general
@SYNTAX
clear(variable)
@DOC
Clears the specified variable or, if blank, clears all variables.
clear()clears local variables
clear("variables") clears local variables
clear("globals") clears global variables
clear("functions"" clear function cache
clear("all") clear local variables, global variables and function cache
@EXAMPLE
<programlisting>
clear('x'); 
clear();
</programlisting>
@NOTES
The variable should be given as a string containing the variable name.
@SEE
who, whos
*/

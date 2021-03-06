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
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for writing to the main display*/
public class disp extends ExternalFunction
{
	/**write operand to main display
	@param operand[n] = items to display*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		for(int index = 0; index < operands.length; index++)
		{
		    globals.getInterpreter().displayText(operands[index].toString());
		}
		
		return null; 
	}
}

/*
@GROUP
system
@SYNTAX
DISP(string1, string2, string3, ...)
@DOC
This displays a set of lines of text.
@NOTES
@EXAMPLES
DISP("Hello", "world")
Hello
world
@SEE
*/

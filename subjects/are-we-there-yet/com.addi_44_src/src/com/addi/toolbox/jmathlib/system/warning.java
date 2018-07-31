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
import com.addi.core.interpreter.Variable;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for writing to the main display
sets the last warning variable to the message displayed*/
public class warning extends ExternalFunction
{
	/**write operand to main display
	@param operand[n] = items to display*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		String message = "";
		for(int index = 0; index < operands.length; index++)
		{
			message = operands[index].toString();
			globals.getInterpreter().displayText(message);
		}
		
		Variable var = globals.createVariable("lastwarning");
		var.assign(new CharToken(message));	

		return new DoubleNumberToken(1);
	}
}

/*
@GROUP
system
@SYNTAX
warning(message)
@DOC
Displays a warning message
@NOTES
This function sets the lastwarning variable
@EXAMPLES
warning("Danger, danger")
Danger, danger
@SEE
error, exit
*/


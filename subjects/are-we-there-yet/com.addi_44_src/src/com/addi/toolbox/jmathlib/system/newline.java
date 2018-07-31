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
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for writing to the main display*/
public class newline extends ExternalFunction
{
	/**write operand to main display
	@param operand[n] = items to display*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		double count = 1;
		
		if((operands.length > 0) && (operands[0] instanceof DoubleNumberToken))
		{
			count = ((DoubleNumberToken)operands[0]).getValueRe();
		}
		
		for(int index = 0; index < count; index++)
		{
		    globals.getInterpreter().displayText("");
		}
		
		return new DoubleNumberToken(1);
	}
}

/*
@GROUP
system
@SYNTAX
newline(lines)
@DOC
Displays number of blank lines equal to the first parameter.
@NOTES
@EXAMPLES
NEWLINE(1)

NEWLINE(2)

@SEE
*/


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

package com.addi.toolbox.string;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for creating random numbers*/
public class lower extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand
		if (operands[0]==null)  return null;

		// operand must be a string
		if (!(operands[0] instanceof CharToken)) return null;

		String data = ((CharToken)operands[0]).getValue().toLowerCase();
		
		return new CharToken(data);		
	}
}

/*
@GROUP
char
@SYNTAX
answer=FINDSTR(string1, string2)
@DOC
Finds all occcurences of the shorter string within the longer
@NOTES
.
@EXAMPLES
<programlisting>
.
</programlisting>
@SEE
strfind, upper
*/


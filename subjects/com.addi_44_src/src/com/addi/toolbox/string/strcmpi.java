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
import com.addi.core.tokens.numbertokens.*;


/**An external function for comparing two strings*/
public class strcmpi extends ExternalFunction
{
	/**compares two strings
	@param operands[0] = first string
	@param operands[1] = second string*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		int result = 0;
		
		if(operands[0] instanceof CharToken)
		{
			if(operands[1] instanceof CharToken)
			{
				String string1 = ((CharToken)operands[0]).getElementString(0).toUpperCase();
				String string2 = ((CharToken)operands[1]).getElementString(0).toUpperCase();
				
				if(string1.equals(string2))
					result = 1;
			}
		}		
		
		return new DoubleNumberToken(result);
	}
}

/*
@GROUP
char
@SYNTAX
STRCMPI(string1, string2)
@DOC
Compares string1 to string2, ignoring case.
@NOTES
@EXAMPLES
STRCMPI("ABcd", "abce")
0
STRCMPI("ABc", "abc")
1
@SEE
strcmp, strncmpi, strncmp
*/


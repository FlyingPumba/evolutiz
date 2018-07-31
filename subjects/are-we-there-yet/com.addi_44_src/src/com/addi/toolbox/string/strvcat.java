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
import com.addi.core.tokens.MatrixToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for concatenating strings into
a vertical vector*/
public class strvcat extends ExternalFunction
{
	/**Concatenates strings into a vertical vector
	@param operands[n] = the strings to concatenate*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken[][] values = new OperandToken[operands.length][1];
		
		for(int index = 0; index < operands.length; index++)
		{
			if(operands[index] instanceof CharToken)
				values[index][0] = ((OperandToken)operands[index]);
			else
				values[index][0] = new CharToken(operands[index].toString());
		}
		
		return new MatrixToken(values);
	}
}

/*
@GROUP
char
@SYNTAX
STRVCAT(string1, string2, ...)
@DOC
A function to join a group of strings into a vertical matrix
@NOTES
@EXAMPLES
STRVCAT("a", "list", "of", "strings")
@SEE
strcat
*/


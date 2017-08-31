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
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.DataToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for getting the size of matrices*/
public class length extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		// at least one operand
        if (getNArgIn(operands) != 1)
			throwMathLibException("length: number of arguments != 1");
            
		int length = 0;
		// first operand must be a number
		if(operands[0] instanceof DataToken)
		{
			// get size or argument
			int y = (int)((DataToken)operands[0]).getSizeY();
			int x = (int)((DataToken)operands[0]).getSizeX();
			
			length = Math.max(y,x);
		}
		else if(operands[0] instanceof CharToken)
		{
			length = ((CharToken)operands[0]).getElementString(0).length();		
		}

		return new DoubleNumberToken(length);		
	}
}


/*
@GROUP
general
@SYNTAX
answer = length(string) 
answer = length(matrix) 
answer = number(matrix) 
@DOC
Returns the length of a matrix or a string.
@EXAMPLES
<programlisting>
length("test") = 4 
length([1, 2, 3]) = 3
</programlisting>
@NOTES
@SEE
size
*/


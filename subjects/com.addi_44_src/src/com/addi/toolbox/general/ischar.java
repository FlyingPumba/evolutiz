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
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function which checks if the argument is a char*/
public class ischar extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1)
			throwMathLibException("ischar: number of arguments != 1");
        
		if (operands[0] instanceof CharToken)   return DoubleNumberToken.one;
		else                                    return DoubleNumberToken.zero;
	}
}

/*
@GROUP
general
@SYNTAX
answer = ischar(value)
@DOC
Returns 1 if the first operand is a struct, else it returns 0.
@EXAMPLES
<programlisting>
ischar("hello") returns 1 
ischar(55)  returns 0
</programlisting>
@NOTES
.
@SEE
ismatrix, isnumeric, isscalar, issquare
*/


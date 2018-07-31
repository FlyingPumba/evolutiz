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
import com.addi.core.interpreter.*;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.*;



/**An external function for returning the length of a string*/
public class strlength extends ExternalFunction
{
	/**Calculate the length of the string
	@param operands[0] the string to get the length for*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;

        if (getNArgIn(operands) !=1)
            throwMathLibException("strlength: number of arguments !=1");
		
		if(operands[0] instanceof CharToken)
		{
			int length = ((CharToken)operands[0]).getElementString(0).length();
			result = new DoubleNumberToken(length);
		}
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[0].getClass().getName()});
		
		return result;

	}
}

/*
@GROUP
char
@SYNTAX
length=STRLENGTH(string)
@DOC
Returns the length of a string.
@NOTES
@EXAMPLES
STRLENGTH("HELLO WORLD") = 11
@SEE
*/


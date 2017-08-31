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


/**An external function for comparing two strings*/
public class strncmpi extends ExternalFunction
{
	/**compares two strings
	@param operands[0] = first string
	@param operands[1] = second string*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		int result = 0;
		
        if (getNArgIn(operands) !=3)
            throwMathLibException("strncmpi: number of arguments !=3");
		
		if(operands[0] instanceof CharToken)
		{
			if(operands[1] instanceof CharToken)
			{
				if(operands[2] instanceof DoubleNumberToken)
				{
					int index = ((DoubleNumberToken)operands[2]).getIntValue(0,0);
					String string1 = ((CharToken)operands[0]).getElementString(0).toUpperCase();				
					if(string1.length() > index)
						string1 = string1.substring(0, index);

					String string2 = ((CharToken)operands[1]).getElementString(0).toUpperCase();
					if(string2.length() > index)
						string2 = string2.substring(0, index);
					
					
					if(string1.equals(string2))
						result = 1;
				}
				else
					Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[2].getClass().getName()});
			}
			else
				Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[1].getClass().getName()});
		}		
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[0].getClass().getName()});
		
		return new DoubleNumberToken(result);
	}
}

/*
@GROUP
char
@SYNTAX
STRNCMP(string1, string2,no. of characters)
@DOC
Compares a number of characters in string1 to string2, ignoring case.
@NOTES
@EXAMPLES
STRNCMP("ABcd", "abce", 3)
1 
STRNCMP("abcd", "abce", 3) 
1 
STRNCMPI("abcd", "abce", 4) 
0 
@SEE
strcmp, strcmpi, strncmp
*/


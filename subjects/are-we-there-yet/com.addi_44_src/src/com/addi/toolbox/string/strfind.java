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


/**An external function for finding a string within another*/
public class strfind extends ExternalFunction
{
	/**finds shorter string within a longer one
	@param operands[0] = first string
	@param operands[1] = second string*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if (getNArgIn(operands) !=2)
            throwMathLibException("strfind: number of arguments !=2");
		
		if(operands[0] instanceof CharToken)
		{
			if(operands[1] instanceof CharToken)
			{
				String string1 = ((CharToken)operands[0]).getElementString(0);
				String string2 = ((CharToken)operands[1]).getElementString(0);
				
				int index = 0;
				int count = 0;
				double lastpos = 0;
				double[][] tempResults = new double[1][string2.length()];

				do
				{
					index = string1.indexOf(string2);
					
					if(index > -1)
					{
						tempResults[0][count] = index + lastpos + 1;
						lastpos = tempResults[0][count];
						count++;
						
						string1 = string1.substring(index + 1, string1.length());
					}
				}while(index > -1);
				
				double[][] results = new double[1][count];
				for(index = 0; index < count; index++)
				{
					results[0][index] = tempResults[0][index];
				}
				result = new DoubleNumberToken(results);
			}
			else
				Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[1].getClass().getName()});
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
STRFIND(string1, string2)
@DOC
finds all occurences of string2 within string1.
@NOTES
@EXAMPLES
STRFIND("This is a test string", "is")
[3,6]
@SEE
findstr
*/


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


/**An external function for returning the length of a string*/
public class substring extends ExternalFunction
{
	/**Calculate the length of the string
	@param operands[0] the string to get the length for*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if ((getNArgIn(operands) < 2) || (getNArgIn(operands) > 3) )
            throwMathLibException("substring: number of arguments <2 or >3");
        
        if (!(operands[0] instanceof CharToken))
            throwMathLibException("subString: only works on chars");

        if (!(operands[1] instanceof DoubleNumberToken))
            throwMathLibException("subString: parameter 2 must be double");

		String argString = ((CharToken)operands[0]).getElementString(0);
		String substring = "";
		if(operands.length < 3 || operands[2] == null)
		{
			int pos = ((int)((DoubleNumberToken)operands[1]).getValueRe());
			substring = argString.substring(pos);
		}
		else
		{
            if (!(operands[2] instanceof DoubleNumberToken))
                throwMathLibException("subString: parameter 3 must be double");

			int pos  = ((int)((DoubleNumberToken)operands[1]).getValueRe());
			int pos2 = ((int)((DoubleNumberToken)operands[2]).getValueRe());
			substring = argString.substring(pos, pos2);
		}
		
		result = new CharToken(substring);
		
		return result;

	}
}

/*
@GROUP
char
@SYNTAX
answer = SUBSTRING(string, start, [length])
@DOC
Returns a portion of a string.
@NOTES
@EXAMPLES
SUBSTRING("HELLO WORLD", 0, 5) = "HELLO"
SUBSTRING("HELLO WORLD", 3, 5) = "LLO W"
@SEE
*/


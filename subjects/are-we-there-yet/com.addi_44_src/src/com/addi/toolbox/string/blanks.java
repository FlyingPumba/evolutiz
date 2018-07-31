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


/**An external function for changing numbers into strings*/
public class blanks extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if (getNArgIn(operands) != 1 )
            throwMathLibException("blanks: number of arguments !=1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("blanks: only works on numbers");
            
		int length = (new Double(((DoubleNumberToken)operands[0]).getValueRe())).intValue();
		
		StringBuffer buffer = new StringBuffer(length);
		
		for(int index = 0; index < length; index++)
		{
			buffer.append(' ');
		}
		String temp = new String(buffer);
		
		result = new CharToken(temp);

		return result;	
	}
}

/*
@GROUP
char
@SYNTAX
BLANKS(number)
@DOC
Outputs a number of spaces equal to number.
@NOTES
@EXAMPLES
"-" + BLANKS(5) + "-" = "-     -"
@SEE
deblank
*/


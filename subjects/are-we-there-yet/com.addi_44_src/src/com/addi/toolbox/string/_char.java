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
public class _char extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        // one operand 
        if (getNArgIn(operands)!=1)
            throwMathLibException("_char: number of input arguments != 1");

        if ( !(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("_char: works only on numbers");
        
        double[][]  x = ((DoubleNumberToken)operands[0]).getReValues();
        
        if ( x.length != 1)
            throwMathLibException("char: works only on row vectors");
        
        String data = "";
        
        // convert numbers into a string
        try {
            for (int i=0; i<x[0].length; i++)
            
            {
            	byte[] b = { new Double(x[0][i]).byteValue() };
                data += new String(b, "UTF8");
            }
        }
        catch (Exception e)
        {
            throwMathLibException("_char: exception");
        }

        return new CharToken(data);		
	}
}

/*
@GROUP
char
@SYNTAX
string = _char(number)
@DOC
Converts a number to a string.
@NOTES
@EXAMPLES
_char([104, 101, 108, 108, 111, 32, 49, 50]) = "hello 12"

@SEE
double
*/


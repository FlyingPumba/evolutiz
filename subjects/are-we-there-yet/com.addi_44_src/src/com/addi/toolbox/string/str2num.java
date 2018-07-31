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


/**An external function for changing strings into numbers */
public class str2num extends ExternalFunction
{
    /**returns a matrix of numbers 
    * @param operands[0] = string (e.g. ["hello"]) 
    * @return a matrix of numbers                                */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {

        // one operand 
        if (getNArgIn(operands)!=1)
            throwMathLibException("str2num: number of input arguments != 1");

        if ( !(operands[0] instanceof CharToken))
            throwMathLibException("str2num: works only on strings");

        // get data from arguments
        String stringValue = ((CharToken)operands[0]).getValue();

        // convert string to array of bytes
        byte[] b = stringValue.getBytes();
        
        double[][] X = new double[1][b.length];
        
        // convert array of byte to array of double
        for (int i=0; i<b.length; i++)
        {
            X[0][i]= (double)b[i];
        } 

        return new DoubleNumberToken( X );

    } // end eval
}


/*
@GROUP
char
@SYNTAX
number = str2num( string )
@DOC
Convert strings into numbers
@EXAMPLES
str2num("hello 12")  returns [104, 101, 108, 108, 111, 32, 49, 50]
@NOTES
.
@SEE
num2str
*/
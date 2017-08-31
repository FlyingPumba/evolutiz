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


/**An external function for checking on whitespaces */
public class isspace extends ExternalFunction
{
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        if (getNArgIn(operands)!=1)
            throwMathLibException("isspace: number of input arguments != 1");

        if ( !(operands[0] instanceof CharToken))
            throwMathLibException("isspace: works only on strings");

        // get data from arguments
        String str = ((CharToken)operands[0]).getValue();

        double[][] ret = new double[1][str.length()];
        
        // find all whitespaces
        for (int i=0; i<str.length(); i++)
        {
            char c = str.charAt(i);
            if ((c==' ')  || 
                (c=='\t') || 
                (c=='\r') ||
                (c=='\n')   )
                ret[0][i]= 1.0;
        } 

        return new DoubleNumberToken( ret );

    } // end eval
}


/*
@GROUP
char
@SYNTAX
isspace()
@DOC
@EXAMPLES
@NOTES
@SEE
letter
*/
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


package com.addi.toolbox.statistics;

import com.addi.core.functions.*;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;




/**
 * An external function for calculating the average
 */
public class average extends ExternalFunction
{

    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        int i;
        OperandToken ot = (OperandToken) operands[0];

        for (i=1; i<operands.length; i++)
            {
                ot.add((OperandToken)operands[i]);
            }
        ot.divide(new DoubleNumberToken(i));

        return ot;
    }
}



/*
@GROUP
statistics
@SYNTAX
average(1,2,3,...,n)
@DOC
Calculates the average of the passed parameters.
@EXAMPLES
<programlisting>
Average(a,b,3,4)
</programlisting>
@SEE
std, mean, var
*/

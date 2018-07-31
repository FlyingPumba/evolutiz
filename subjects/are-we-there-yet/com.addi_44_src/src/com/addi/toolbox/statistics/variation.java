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
 * An external function for calculating the variation of
 */
public class variation extends ExternalFunction
{

    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        // Check if there's only one or zero parameters
        if (operands.length < 2)
        {
            // The variation of a number is 0
            return (OperandToken)(DoubleNumberToken.zero);
        }
        int i;
        OperandToken ot1 = (OperandToken) new DoubleNumberToken(0);
        OperandToken ot2 = (OperandToken) new DoubleNumberToken(0);
        OperandToken ot;

        for (i=0; i<operands.length; i++)
            {
                ot2.add((OperandToken)operands[i]);
                ot = (OperandToken)operands[i];
                ot.multiply((OperandToken)operands[i]);
                ot1.add(ot);
            }
        ot2.divide(new DoubleNumberToken(i));
        ot2.multiply(ot2);
        // Now ot2 is the square of the average of the parameters.
        ot1.divide(new DoubleNumberToken(i));
        // Now ot1 is the average of the squares of parameters

        return ot1.subtract(ot2);
    }
}



/*
@GROUP
statistics
@SYNTAX
Variation(1,2,3,...,n)
@DOC
Calculates the variation of the parameters.
@EXAMPLES
<programlisting>
Variation(0,x,y,z,3,t)
</programlisting>
@SEE
var, std
*/

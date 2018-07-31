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

package com.addi.toolbox.general;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.LogicalToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;



public class logical extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1 )
			throwMathLibException("logical: number of arguments !=1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("logical: only works on numbers");

        DoubleNumberToken num = (DoubleNumberToken)operands[0];
        
        int[] size = num.getSize();
        
        int n = num.getNumberOfElements();
        
        LogicalToken l = new LogicalToken(size, null);

        for (int i=0; i<n; i++)
        {
            if (num.getValueRe(i)!=0)
                l.setValue(i, true);
            else
                l.setValue(i, false);
                
            if (num.getValueIm(i)!=0)
                throwMathLibException("logical: only works on real numbers");
        }
        
        return l;
        
	} // end eval
}

/*
@GROUP
general
@SYNTAX
logical(x)
@DOC
converts a double array into an array of boolean
@EXAMPLES
<programlisting>
logical([1,3,0,4]) -> [true, true, false, true]
</programlisting>
@SEE
true, false, islogical
*/


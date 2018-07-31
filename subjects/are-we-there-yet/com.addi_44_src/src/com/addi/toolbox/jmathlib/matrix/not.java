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

package com.addi.toolbox.jmathlib.matrix;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.LogicalToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for computing NOT a matrix        */
public class not extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return matrix function NOT of each element  */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        // two operands (e.g. not(A) )
        if (getNArgIn(operands) != 1)
            throwMathLibException("not: number of arguments != 1");
        

        if (operands[0] instanceof DoubleNumberToken)
        {
            DoubleNumberToken num = (DoubleNumberToken)operands[0];
            int         n   = num.getNumberOfElements();
            
    		boolean[] values = new boolean[n];
            
    		for (int i=0; i<n ; i++)
    		{
                if (num.getValueIm(i)!=0.0)
                    throwMathLibException("not: only works on REAL numbers");
                    
				if (num.getValueRe(i) != 0.0)
				{
					values[i] = false;
				}
				else
				{
					values[i] = true;
				}
    		}	
    		return new LogicalToken(num.getSize(), values);		
        }
        else if (operands[0] instanceof LogicalToken)
        {
            LogicalToken l   = (LogicalToken)operands[0];
            int          n   = l.getNumberOfElements();
            boolean[]    ret = new boolean[n];
            
            for (int i=0; i<n; i++)
            {
                ret[i] = !l.getValue(i);
            }
            
            return new LogicalToken(l.getSize(), ret);
        }
        else
        {
            throwMathLibException("not: works on numbers and logical only");
            return null;
        }
        
	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer = not(matrix)
@DOC
Returns the boolean NOT of all the elements of a matrix.
@NOTES
@EXAMPLES
<programlisting>
not([false, true]) = [true, false]
not([1,1;1,1])     = [0,0;0,0]
not([0,1,2])       = [1,0,0]
</programlisting>
@SEE
and, or, xor
*/


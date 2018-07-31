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
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function that return 1.0 is all elements of the argument are nonzero  */
public class isfinite extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return 1.0 if all elements of the argument are nonzero  */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// two operands (e.g. not(A) )
        if (getNArgIn(operands) != 1)
			throwMathLibException("isnan: number of arguments != 1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("isnan: works on numbers only");


		// get data from arguments
        double[][] a_r = ((DoubleNumberToken)operands[0]).getValuesRe();
        double[][] a_i = ((DoubleNumberToken)operands[0]).getValuesIm();
		int a_dy       = a_i.length;
        int a_dx       = a_i[0].length;	

        double[][] ret = ((DoubleNumberToken)operands[0]).getValuesIm();
	
		for (int xi=0; xi<a_dx ; xi++)
		{
			for (int yi=0; yi<a_dy ; yi++)
			{
                
				if ( (a_r[yi][xi]!=Double.POSITIVE_INFINITY) &&
                     (a_i[yi][xi]!=Double.POSITIVE_INFINITY) &&   
                     (a_r[yi][xi]!=Double.NEGATIVE_INFINITY) &&
                     (a_i[yi][xi]!=Double.NEGATIVE_INFINITY) &&
                     (!Double.isNaN(a_r[yi][xi]))            &&
                     (!Double.isNaN(a_i[yi][xi]))               )
                    ret[yi][xi] = 1.0;
                else
                    ret[yi][xi] = 0.0;

            }
		}	
        
		return new DoubleNumberToken(ret);		
		
        
	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer = isfinite(matrix)
@DOC
.
@NOTES
@EXAMPLES
<programlisting>
isfinite([5,Inf,-Inf,NaN])  ->  ans = [1 , 0 , 0 , 0]
</programlisting>
@SEE
isreal, isnan, isinf
*/


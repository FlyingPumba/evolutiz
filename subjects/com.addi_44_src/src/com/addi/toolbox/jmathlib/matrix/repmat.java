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


/**An external function for replicating and tiling matrices                  *
*  (e.g. remat(a, m, n) creates a large matrix that consists of              *
*   an m-by-n tiling of matrix a)                                            */
public class repmat extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix to replicate
	* @param operands[1] = number of rows
	* @param operands[2] = number of columns 
	* @return big matrix                             */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        // two operands (e.g. reshape(A,[n,m]) ) or
		// three operands (e.g. reshape(A,n,m) )
        if ((getNArgIn(operands)<2) ||
            (getNArgIn(operands)>3)    )
            throwMathLibException("repmat: number of arguments < 2 or >3");

		if ( (!(operands[0] instanceof DoubleNumberToken)) ||
			 (!(operands[1] instanceof DoubleNumberToken))    )
            throwMathLibException("repmat: works only on DoubleNumberTokens");

        if ( (getNArgIn(operands)==3)                &&
             (!(operands[2] instanceof DoubleNumberToken))    )
            throwMathLibException("repmat: works only on DoubleNumberTokens");


		// get data from arguments
		double[][] a =      ((DoubleNumberToken)operands[0]).getReValues();
        int        m = 0;
        int        n = 0;

        if (getNArgIn(operands)==3)
        {
            // repmat(a,2,3)
            m = (int)((DoubleNumberToken)operands[1]).getValueRe();        
            n = (int)((DoubleNumberToken)operands[2]).getValueRe();
        }
        else
        {
            // repmat(a,[2,3])
            m = (int)((DoubleNumberToken)operands[1]).getValueRe(0);        
            n = (int)((DoubleNumberToken)operands[1]).getValueRe(1);        
        }

		int a_dy     = a.length;
        int a_dx     = a[0].length;	
	
		// m,n must be positive
		if ((m<=0) || (n<=0)) return null;	


		// create matrix
		double[][] values = new double[a_dy * m][a_dx * n];
		for (int ym=0; ym<m ; ym++)
		{
			for (int xn=0; xn<n ; xn++)
			{
				for (int yi=0; yi<a_dy ; yi++)
				{
					for (int xi=0; xi<a_dx ; xi++)
					{
						values[(ym*a_dy)+yi][(xn*a_dx)+xi] = a[yi][xi];
					}
				}	
			}
		}
		return new DoubleNumberToken(values);		

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
matrix = repmat(matrix to copy, no of rows, no of columns)
@DOC
Copies part of a matrix.
@NOTES
@EXAMPLES
<programlisting>
repmat([1,2,3;4,5,6;7,8,9], 2, 2) = [1,2;4,5]
</programlisting>
@SEE
reshape

*/


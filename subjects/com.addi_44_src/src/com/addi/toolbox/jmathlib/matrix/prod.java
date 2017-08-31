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


/**An external function for computing the product of array ellements         */
public class prod extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return scalar or vector                             */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand (e.g. prod(A) )
		if (operands == null)                         return null;
		if (operands.length != 1)                     return null;
		if (operands[0] == null)                      return null;
		if (!(operands[0] instanceof DoubleNumberToken))    return null;


		// get data from arguments
		double[][] a =      ((DoubleNumberToken)operands[0]).getReValues();

		int a_dy     = a.length;
        int a_dx     = a[0].length;	
        double[][] values = null;
        double temp = 1.0;
        
        if (a_dy==1)
        {
        	// only row vector
        	// e.g. prod([1,2,3])  -> 1*2*3=6
			values = new double[1][1];
			for (int xi=0; xi<a_dx ; xi++)
			{
				temp = temp * a[0][xi];
			}
			values[0][0] = temp;
        }
        else
        {
			// e.g. prod([1,2,3; 2,2,2]) -> [2,4,6]
        	// create matrix
			values = new double[1][a_dx];
			for (int xi=0; xi<a_dx ; xi++)
			{
				temp = 1.0;
				for (int yi=0; yi<a_dy ; yi++)
				{
					temp = temp * a[yi][xi];
				}
				values[0][xi] = temp;
			}	
					
        }
        return new DoubleNumberToken(values);
	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer = PROD(matrix)
@DOC
Returns the products of the rows of a matrix.
@NOTES
@EXAMPLES
PROD([1,2;3,4]) = [3,8]
PROD([1,2,3;4,5,6;7,8,9]) = [28, 80, 162]
@SEE
pow2, cumprod
*/


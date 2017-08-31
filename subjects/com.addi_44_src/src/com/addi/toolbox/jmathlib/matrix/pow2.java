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


/**An external function for computing 2 raised to the power of each *
*  element of an array         */
public class pow2 extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return matrix                                 */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand (e.g. pow2(A) )
		if (operands == null)                         return null;
		if (operands.length != 1)                     return null;
		if (operands[0] == null)                      return null;
		if (!(operands[0] instanceof DoubleNumberToken))    return null;


		// get data from arguments
		double[][] a =      ((DoubleNumberToken)operands[0]).getReValues();

		int a_dy     = a.length;
        int a_dx     = a[0].length;	
	
		// create matrix
		double[][] values = new double[a_dy][a_dx];
		for (int xi=0; xi<a_dx ; xi++)
		{
			for (int yi=0; yi<a_dy ; yi++)
			{
				values[yi][xi] = Math.pow(2.0, a[yi][xi]);
			}

		}	
		return new DoubleNumberToken(values);		

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer = POW2(matrix)
@DOC
Returns 2 to the power of all the elements in a matrix.
@NOTES
@EXAMPLES
POW2([1,2;3,4]) = [1,4;8,16]
@SEE
prod, power, mpower

*/


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


/**An external function for computing Or of two matrices        */
public class or extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @param operands[1] = matrix 
	* @return matrix function OR of each element of the first and second argument  */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// two operands (e.g. or(A,B) )
        // two operands (e.g. and(A,B) )
        if (getNArgIn(operands) != 2)
            throwMathLibException("or: number of arguments != 2");
           
        // if boolean: convert LogicalToken to DoubleNumberToken
        if (operands[0] instanceof LogicalToken)
            operands[0]=((LogicalToken)operands[0]).getDoubleNumberToken(); 

        // if boolean: convert LogicalToken to DoubleNumberToken
        if (operands[1] instanceof LogicalToken)
            operands[1]=((LogicalToken)operands[1]).getDoubleNumberToken(); 

        if (!(operands[0] instanceof DoubleNumberToken) || 
            !(operands[1] instanceof DoubleNumberToken)    )
            throwMathLibException("or: works on number and booleans only");


		// get data from arguments
		double[][] a =      ((DoubleNumberToken)operands[0]).getReValues();
		int a_dy     = a.length;
        int a_dx     = a[0].length;	

		double[][] b =      ((DoubleNumberToken)operands[1]).getReValues();
		int b_dy     = b.length;
        int b_dx     = b[0].length;	
	
		// both matrices must have the same size
		if ( (a_dy != b_dy) || (a_dx != b_dx) ) return null; 


		// create matrix
		double[][] values = new double[a_dy][a_dx];
		for (int xi=0; xi<a_dx ; xi++)
		{
			for (int yi=0; yi<a_dy ; yi++)
			{
				if (   (a[yi][xi] != 0.0)
				    || (b[yi][xi] != 0.0) )
				{
					values[yi][xi] = 1.0;
				}
				else
				{
					values[yi][xi] = 0.0;
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
answer = OR(matrix1, matrix2)
@DOC
Returns the boolean or of two matrices.
@NOTES
@EXAMPLES
OR([1,1;1,1],[0,0;0,0]) = [1,1;1,1]
@SEE
and, not

*/


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
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;
import com.addi.toolbox.jmathlib.matrix._private.Jama.CholeskyDecomposition;



/**An external function for computing the cholewsky decomposition of an array  */
public class chol extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return matrix                                 */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand (e.g. chol(A) )
		if (getNArgIn(operands) != 1)
			throwMathLibException("chol: number of arguments != 1");
        
        if (!(operands[0] instanceof DoubleNumberToken))    return null;


		// get data from arguments
		double[][] A =      ((DoubleNumberToken)operands[0]).getReValues();

		CholeskyDecomposition cholDecomp = new CholeskyDecomposition( A );

		double[][] L = cholDecomp.getL();

		return new DoubleNumberToken( L );		

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer=CHOL(matrix)
@DOC
Calculates the Cholewsky decomposition of matrix
@NOTES
@EXAMPLES
CHOL([2,3;3,5])=[1.414,0;2.121,0.707]
@SEE
*/


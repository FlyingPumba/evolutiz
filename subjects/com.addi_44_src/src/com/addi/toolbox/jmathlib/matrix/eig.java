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
import com.addi.toolbox.jmathlib.matrix._private.Jama.EigenvalueDecomposition;


/**An external function for computing eigenvalues and eigenvectors of an array  */
public class eig extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return matrix                                 */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand (e.g. eig(A) )
		if (getNArgIn(operands) != 1)
			throwMathLibException("eig: number of arguments != 1");

		if (!(operands[0] instanceof DoubleNumberToken))
			  throwMathLibException("eig: works only on numbers");

		// get data from arguments
		double[][] A =      ((DoubleNumberToken)operands[0]).getReValues();

		EigenvalueDecomposition eigDecomp = new EigenvalueDecomposition( A );


		if (getNArgOut()==2)
		{
			// 2 arguments on the left side

			// get eigen vectors
			double[][] eigenVectors = eigDecomp.getV();

			// get eigen values block matrix
			double[][] eigenValues = eigDecomp.getD();

			//DoubleNumberToken	 values = {new DoubleNumberToken(eigenVectors),
			//                          new DoubleNumberToken(eigenValues)  };

			OperandToken values[][] = new OperandToken[1][2];
			values[0][0] = new DoubleNumberToken(eigenVectors);
			values[0][1] = new DoubleNumberToken(eigenValues);
			return new MatrixToken( values );

		}
		else
		{
			// 0 or 1 arguments on the left side 

			// get eigenvalues
			double[] eigRealValues = eigDecomp.getRealEigenvalues();
			double[] eigImagValues = eigDecomp.getImagEigenvalues();

			int n = eigRealValues.length;
			double[][] realValues = new double[n][1];
			double[][] imagValues = new double[n][1];

			for (int yi=0; yi<n ; yi++)
			{
				realValues[yi][0] = eigRealValues[yi];
				imagValues[yi][0] = eigImagValues[yi];
			}

			return new DoubleNumberToken(realValues, imagValues);
		}

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
eig(matrix)
@DOC
Caculates the eigenvalues of the matrix.
@NOTES
@EXAMPLES
<programlisting>
eig([1,2;3,4])=[-0.372;5.372]
</programlisting>
@SEE
*/


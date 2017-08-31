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
import com.addi.toolbox.jmathlib.matrix._private.Jama.SingularValueDecomposition;


   /** Singular Value Decomposition.
   <P>
   For an m-by-n matrix A with m >= n, the singular value decomposition is
   an m-by-n orthogonal matrix U, an n-by-n diagonal matrix S, and
   an n-by-n orthogonal matrix V so that A = U*S*V'.
   <P>
   The singular values, sigma[k] = S[k][k], are ordered so that
   sigma[0] >= sigma[1] >= ... >= sigma[n-1].
   <P>
   The singular value decompostion always exists, so the constructor will
   never fail.  The matrix condition number and the effective numerical
   rank can be computed from this decomposition.
   <p>
   usage: s = svd(A) <br>
          [U,S,V]=svd(A)
   */

/**An external function for computing singular values of an array  */
public class svd extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return matrix                                 */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand (e.g. eig(A) )
		if (getNArgIn(operands)!=1)
			throwMathLibException("svd: number of arguments != 1");

		if (!(operands[0] instanceof DoubleNumberToken))
			  throwMathLibException("svd: works only on numbers");


		// get data from arguments
		double[][] A =      ((DoubleNumberToken)operands[0]).getReValues();

		SingularValueDecomposition singularDecomp = new SingularValueDecomposition( A );


		if (getNArgOut()==3)
		{
			// 3 arguments on the left side (e.g. [U,S,V] = svd(A) )

			// get left singular vectors
			double[][] leftVectors = singularDecomp.getU();

			// get diagonal matrix of singular values
			double[][] singularValues = singularDecomp.getS();

			// get right singular vectors
			double[][] rightVectors = singularDecomp.getV();

			OperandToken values[][] = new OperandToken[1][3];
			values[0][0] = new DoubleNumberToken(leftVectors);
			values[0][1] = new DoubleNumberToken(singularValues);
			values[0][2] = new DoubleNumberToken(rightVectors);
			return new MatrixToken( values );

		}
		else
		{
			// 0 or 1 arguments on the left side 

			// get eigenvalues
			double[] singularValues = singularDecomp.getSingularValues();

			int n = singularValues.length;
			double[][] singularValuesMatrix = new double[n][1];

			for (int yi=0; yi<n ; yi++)
			{
				singularValuesMatrix[yi][0] = singularValues[yi];
			}

			return new DoubleNumberToken(singularValuesMatrix);
		}

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
   s = svd(A) 
   [U,S,V]=svd(A)
@DOC
Calculates the single value decomposition of a matrix
@NOTES
   For an m-by-n matrix A with m >= n, the singular value decomposition is
   an m-by-n orthogonal matrix U, an n-by-n diagonal matrix S, and
   an n-by-n orthogonal matrix V so that A = U*S*V'.

   The singular values, sigma[k] = S[k][k], are ordered so that
   sigma[0] >= sigma[1] >= ... >= sigma[n-1].

   The singular value decompostion always exists, so the constructor will
   never fail.  The matrix condition number and the effective numerical
   rank can be computed from this decomposition.

@EXAMPLES
<code>
SVD([1,2,3;4,5,6;7,8,9]) = [16.848; 1.068; 0]
</code>
@SEE
lu, qr
*/


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
import com.addi.core.tokens.MatrixToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;
import com.addi.toolbox.jmathlib.matrix._private.Jama.QRDecomposition;


/** QR Decomposition.
<P>
   For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
   orthogonal matrix Q and an n-by-n upper triangular matrix R so that
   A = Q*R.
<P>
   The QR decompostion always exists, even if the matrix does not have
   full rank, so the constructor will never fail.  The primary use of the
   QR decomposition is in the least squares solution of nonsquare systems
   of simultaneous linear equations.  This will fail if isFullRank()
   returns false.
*/

/**An external function for computing a QR decomposition of a matrix  */
public class qr extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return matrix                                 */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand (e.g. qr(A) )
		if (operands == null)                         return null;
		if ((operands.length < 1) || 
            (operands.length > 2)    )                return null;
		if (operands[0] == null)                      return null;
		if (!(operands[0] instanceof DoubleNumberToken))    return null;


		// get data from arguments
		double[][] A =      ((DoubleNumberToken)operands[0]).getReValues();

		// create LUDecomposition object
		QRDecomposition qrDecomp = new QRDecomposition( A );

		if (operands.length == 2)
		{
			//  solve A*x = B (e.g. x=qr(A,B) )

			// get right side of A*x = B from arguments
			double[][] B = ((DoubleNumberToken)operands[1]).getReValues();

			// solve equation
			double[][] x = qrDecomp.solve(B);

			return new DoubleNumberToken(x);
		}

		// Check number of Arguments
		if (getNoOfLeftHandArguments()==2)
		{
			// 2 arguments on the left side (e.g. [Q, R] = qr(A) )

			// get Q matrix
			double[][] Q = qrDecomp.getQ();

			// get R matrix
			double[][] R = qrDecomp.getR();

			OperandToken values[][] = new OperandToken[1][2];
			values[0][0] = new DoubleNumberToken(Q);
			values[0][1] = new DoubleNumberToken(R);
			return new MatrixToken( values );

		}
		else
		{
			// 0 or 1 arguments on the left side 

			// get R matrix
			double[][] R = qrDecomp.getR();

			return new DoubleNumberToken( R );
		}

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
QR(matrix)
@DOC
Calculates the QR decomposition of matrix.
@NOTES
   For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
   orthogonal matrix Q and an n-by-n upper triangular matrix R so that
   A = Q*R.

   The QR decompostion always exists, even if the matrix does not have
   full rank, so the constructor will never fail.  The primary use of the
   QR decomposition is in the least squares solution of nonsquare systems
   of simultaneous linear equations.  This will fail if isFullRank()
   returns false.
@EXAMPLES
@SEE
chol, lu
*/


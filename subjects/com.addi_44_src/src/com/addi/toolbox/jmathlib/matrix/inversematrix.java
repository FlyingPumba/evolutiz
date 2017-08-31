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
import com.addi.core.functions.Function;
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.FunctionToken;
import com.addi.core.tokens.MatrixToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for determining the determinant of a matrix*/
public class inversematrix extends ExternalFunction
{
	/**Check that the parameter is a square matrix then calculate
	it's inverse
	It uses the Determinant and Adjoint classes to calculate the 
	determinant and the adjoint*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
	    if (getNArgIn(operands) != 1)
			throwMathLibException("InverseMatrix: number of arguments != 1");

		FunctionToken token = null;
		Function determinant = null;
		Function adjoint = null;

		OperandToken result = null;
		Token operand = operands[0];
		
		if(operand instanceof DoubleNumberToken)
		{
			DoubleNumberToken matrix = ((DoubleNumberToken)operand);
			
			if(matrix.getSizeX() == matrix.getSizeY())
			{
				int size = matrix.getSizeX();

				try
				{
					token = new FunctionToken("determinant");
					determinant = globals.getFunctionManager().findFunction(token);
				}
				catch(java.lang.Exception e)
				{}

				try
				{
					token = new FunctionToken("adjoint");
					adjoint = globals.getFunctionManager().findFunction(token);
				}
				catch(java.lang.Exception e)
				{}
			
				DoubleNumberToken matrixDeterminant = (DoubleNumberToken)determinant.evaluate(operands, globals);
				
				if (!matrixDeterminant.equals(new DoubleNumberToken(0.0,0.0)))
				{
					DoubleNumberToken matrixAdjoint = ((DoubleNumberToken)adjoint.evaluate(operands, globals));
					
					result = matrixAdjoint.divide(matrixDeterminant);
				}
		        else
		        {
		            com.addi.core.interpreter.Errors.throwMathLibException(ERR_MATRIX_SINGULAR);
		        }			
			}
	        else
	        {
	            com.addi.core.interpreter.Errors.throwMathLibException(ERR_NOT_SQUARE_MATRIX);
	        }			
		}
		
		return result;
	}
}

/*
@GROUP
matrix
@SYNTAX
answer=INVERSEMATRIX(square matrix)
@DOC
Returns the inverse of the first operand, which must be a square matrix.
@NOTES
@EXAMPLES
INVERSEMATRIX([1,0;0,1]) = [1,0;0,1]
@SEE
adjoint, simultaneouseq
*/


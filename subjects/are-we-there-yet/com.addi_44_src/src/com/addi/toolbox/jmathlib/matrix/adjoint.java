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
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.FunctionToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for determining the determinant of a matrix*/
public class adjoint extends ExternalFunction
{
	/**Check that the parameter is a square matrix then claculate
	it's determinant*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
        OperandToken result = null;
		
        if (getNArgIn(operands) != 1)
			throwMathLibException("Adjoint: number of arguments != 1");

		if(!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("Adjoint: works only on numbers");

        DoubleNumberToken matrix = ((DoubleNumberToken)operands[0]);
		
		if(matrix.getSizeX() == matrix.getSizeY())
		{
			result = calcAdjoint(matrix, globals);
		}
        else
        {
            com.addi.core.interpreter.Errors.throwMathLibException(ERR_NOT_SQUARE_MATRIX);
        }			
		
		return result;
	}
	
	/**Calculates the adjoint of the matrix
	It uses the Determinant class to calculate the determinants of the sub matrices
	values = array of values
	size   = the size of the matrix
	result = the adjoint as a size * size array of double*/
	private DoubleNumberToken calcAdjoint(DoubleNumberToken matrix, GlobalValues globals)
	{
		int size = matrix.getSizeY();
		
		FunctionToken token = null;
		Function function   = null;
		FunctionToken token1 = null;
		Function function1   = null;
        
		try
		{
			token = new FunctionToken("submatrix");
			function = globals.getFunctionManager().findFunction(token);
			token1 = new FunctionToken("determinant");
			function1 = globals.getFunctionManager().findFunction(token1);
		}
		catch(java.lang.Exception e)
		{}

		DoubleNumberToken tempResult;
		double [][] resultRe = new double[size][size];
		double [][] resultIm = new double[size][size];
		
		if (size==1) {
			return(new DoubleNumberToken(1.0,0.0));
		}
		
		for(int rowNumber = 0; rowNumber < size; rowNumber++)
		{
			for(int colNumber = 0; colNumber < size; colNumber++)
			{
				
				DoubleNumberToken [] submatrixOperands = new DoubleNumberToken[3];
				DoubleNumberToken submatrixResult = null;
				double[] rowsRe = new double[size-1];
				double[] rowsIm = new double[size-1];
				double[] columnsRe = new double[size-1];
				double[] columnsIm = new double[size-1];
				int rowIndex = 0;
				int columnIndex = 0;
				for (int rowLoop = 0; rowLoop < size; rowLoop++) {
					if (rowLoop == rowNumber) {
						continue;
					}
					rowsRe[rowIndex] = rowLoop+1;
					rowsIm[rowIndex] = 0;
					rowIndex++;
				}
				for (int columnLoop = 0; columnLoop < size; columnLoop++) {
					if (columnLoop == colNumber) {
						continue;
					}
					columnsRe[columnIndex] = columnLoop+1;
					columnsIm[columnIndex] = 0;
					columnIndex++;
				}
				submatrixOperands[0] = matrix;
				submatrixOperands[1] = new DoubleNumberToken(1,size-1,rowsRe,rowsIm);
				submatrixOperands[2] = new DoubleNumberToken(1,size-1,columnsRe,columnsIm);
				submatrixResult = (DoubleNumberToken)function.evaluate(submatrixOperands, globals);
				
				OperandToken[] operands = new OperandToken[1];
				operands[0] = submatrixResult;

				DoubleNumberToken minor = (DoubleNumberToken)function1.evaluate(operands, globals);
				
				DoubleNumberToken modifier = new DoubleNumberToken(-1.0,0.0);
				if((rowNumber + colNumber) % 2 == 0)
					modifier = new DoubleNumberToken(1.0,0.0);
					
				tempResult = (DoubleNumberToken)modifier.multiply(minor);
					
				resultRe[colNumber][rowNumber] = tempResult.getValueRe();
				resultIm[colNumber][rowNumber] = tempResult.getValueIm();
			}
		}
		
		DoubleNumberToken result = new DoubleNumberToken(resultRe, resultIm);
		
		return result;
	}

}

/*
@GROUP
matrix
@SYNTAX
answer = adjoint(square matrix)
@DOC
Returns the adjoint matrix for the supplied matrix.
@NOTES
@EXAMPLES
<programlisting>
</programlisting>
@SEE
inversematrix, inv

*/

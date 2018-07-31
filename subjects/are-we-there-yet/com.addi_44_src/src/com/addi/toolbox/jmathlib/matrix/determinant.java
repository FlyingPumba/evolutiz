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
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for determining the determinant of a matrix*/
public class determinant extends ExternalFunction
{
	/**Check that the parameter is a square matrix then claculate
	it's determinant*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
        
        if (getNArgIn(operands) != 1)
			throwMathLibException("Determinant: number of arguments != 1");
            
		Token operand = operands[0];
		
		if(operand instanceof DoubleNumberToken)
		{
			DoubleNumberToken matrix = ((DoubleNumberToken)operand);
			
			if(matrix.getSizeX() == matrix.getSizeY())
			{				
				result = calcDeterminant(matrix,globals);
			}
	        else
	        {
	            com.addi.core.interpreter.Errors.throwMathLibException(ERR_NOT_SQUARE_MATRIX);
	        }			
		}
		
		return result;
	}
	
	/**Function to actually calculate the determinant
	values 	= array of values
	size 	= the size of the matrix
	result 	= the determinant */
	private DoubleNumberToken calcDeterminant(DoubleNumberToken matrix, GlobalValues globals)
	{
		int size = matrix.getSizeY();
		
		com.addi.core.interpreter.ErrorLogger.debugLine("calculating determinant - size = " + size);
		DoubleNumberToken result = new DoubleNumberToken(0.0,0.0);
		DoubleNumberToken temp = new DoubleNumberToken(0.0,0.0);
		if(size == 1)		//special case 1, a scalar value
		{
			result = (DoubleNumberToken)matrix.getElement(0, 0);
		}
		else if(size == 2)	//special case 2, a 2*2 matrix
		{
			result = (DoubleNumberToken)matrix.getElement(0, 0);
			result = (DoubleNumberToken)result.multiply((DoubleNumberToken)matrix.getElement(1, 1));
			temp = (DoubleNumberToken)matrix.getElement(1, 0);
			temp = (DoubleNumberToken)temp.multiply((DoubleNumberToken)matrix.getElement(0, 1));
			result = (DoubleNumberToken)result.subtract(temp);
		}
		else				//calculate the determinant of an larger matrix
		{					//using recursion
			for(int colNumber = 0; colNumber < size; colNumber++)
			{
				//construct the sub matrix
				FunctionToken token = null;
				Function function   = null;
				
				try
				{
					token = new FunctionToken("submatrix");
					function = globals.getFunctionManager().findFunction(token);
				}
				catch(java.lang.Exception e)
				{}
				
				DoubleNumberToken [] submatrixOperands = new DoubleNumberToken[3];
				DoubleNumberToken submatrixResult = null;
				double[] rowsRe = new double[size-1];
				double[] rowsIm = new double[size-1];
				double[] columnsRe = new double[size-1];
				double[] columnsIm = new double[size-1];
				int rowIndex = 0;
				int columnIndex = 0;
				for (int rowLoop = 0; rowLoop < size; rowLoop++) {
					if (rowLoop == 0) {
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
				
				DoubleNumberToken modifier = new DoubleNumberToken(-1.0,0.0);

				if(colNumber % 2 == 0)
					modifier = new DoubleNumberToken(1.0,0.0);;
					
				DoubleNumberToken element = (DoubleNumberToken)matrix.getElement(0, colNumber);
								
				result = (DoubleNumberToken)result.add((modifier.multiply(element)).multiply(calcDeterminant(submatrixResult, globals)));
			}
		}
		
		return result;
	}
}
	
/*
@GROUP
matrix
@SYNTAX
answer = DETERMINANT(square matrix)
@DOC
Returns the determinant for the first operand which must be a square matrix.
@NOTES
@EXAMPLES
DETERMINANT([1,0;0,1]) = 1
DETERMINANT([1,2;3,4]) = -2
@SEE
*/


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


/**An external function for converting a matrix into lower 
triangular form*/
public class lowertriangle extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		Token operand = operands[0];
		
		if(operand instanceof DoubleNumberToken)
		{
			DoubleNumberToken matrix = ((DoubleNumberToken)operand);
			
			if(matrix.getSizeX() == matrix.getSizeY())
			{
				int size = matrix.getSizeX();
				
				result = new DoubleNumberToken(calcLowerTriangle(matrix.getReValues(), size));
			}
	        else
	        {
	            com.addi.core.interpreter.Errors.throwMathLibException(ERR_NOT_SQUARE_MATRIX);
	        }			
		}
		
		return result;		
	}
	
	/**Convert the matrix into lower triangular form by swapping
	rows then adding rows together*/
	private double[][] calcLowerTriangle(double[][] values, int size)
	{
		//swap the rows so that the values on the 
		//primary diagonal are non zero
		double[][] result = checkRows(values, size);
		
		for(int i = size-2; i >= 0; i--)
		{
			for(int j = size-1; j > i; j--)
			{
				//find the factor needed to zero this cell
				double factor = result[i][j] / result[j][j];

				//calculate the new values for the row i
				//after adding row j * factor
				for(int k = 0; k < size; k++)
				{
					result[i][k] = result[i][k] - result[j][k] * factor;
				}
			}
		}
		
		return result;		
	}

	/**makes sure that the values on the primary diagonal
	are not zero. swapping rows if nescessary*/	
	private double[][] checkRows(double[][] values, int size)
	{
		for(int i = size-1; i >= 0; i--)
		{	
			if(values[i][i] == 0)	//then swap this row with anothre
			{
				boolean found = false;
				//find a row that fits
				for(int j = size-1; j >= 0; j--)
				{
					if(values[j][i] != 0 && values[i][j] != 0)
					{
						//swap row i and j
						double temp[] = values[i];
						values[i] = values[j];
						values[j] = temp;
						found = true;
						break;
					}
				}
				
				//if a matching row wasnt't found then move
				//the row to the top of the matrix
				if(!found)
				{
					double temp[] = values[i];
					values[i] = values[0];
					values[0] = temp;					
				}
			}
		}
		
		return values;
	}
}

/*
@GROUP
matrix
@SYNTAX
answer = LOWERTRIANGLE(matrix)
@DOC
Converts a square matrix into it's lower triangular form.
@NOTES
@EXAMPLES
LOWERTRIANGLE([1,2;3,4]) = [-0.5, 0; 3, 4]
@SEE
uppertriangle

*/


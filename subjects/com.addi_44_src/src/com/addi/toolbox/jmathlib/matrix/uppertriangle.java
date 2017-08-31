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


/**An external function for converting a matrix into upper 
triangular form*/
public class uppertriangle extends ExternalFunction
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
				
				result = new DoubleNumberToken(calcUpperTriangle(matrix.getReValues(), size));
			}
	        else
	        {
	            com.addi.core.interpreter.Errors.throwMathLibException(ERR_NOT_SQUARE_MATRIX);
	        }			
		}
		
		return result;		
	}
	
	/**Convert the matrix into upper triangular form by swapping
	rows then adding rows together*/
	private double[][] calcUpperTriangle(double[][] values, int size)
	{
		//swap the rows so that the values on the 
		//primary diagonal are non zero
		double[][] result = checkRows(values, size);
		
		for(int i = 1; i < size; i++)
		{
			for(int j = 0; j < i; j++)
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
		for(int i = 0; i < size; i++)
		{	
			if(values[i][i] == 0)	//then swap this row with anothre
			{
				boolean found = false;
				//find a row that fits
				for(int j = 0; j < size; j++)
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
				//the row to the bottom of the matrix
				if(!found)
				{
					double temp[] = values[i];
					values[i] = values[size-1];
					values[size-1] = temp;					
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
answer = UPPERTRIANGLE(matrix)
@DOC
Converts a matrix to upper triangular form.
@NOTES
@EXAMPLES
UPPERTRIANGLE([1,2;3,4]) = [1,2;0,-2]
@SEE
lowertriangle

*/


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


/**An external function for reshaping matrices                   *
*  (e.g. reshape([1,2;3,4;5,6],2,3) return [1,5,4;3,2,6])        *
*  The original matrix is read column for column and rearranged  *
*  to a new dimension                                            */
public class reshape extends ExternalFunction
{
	/**return a  matrix 
	@param operands[0] = matrix to reshape
	@param operands[1] = number of rows
	@param operands[2] = number of columns */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// three operands (e.g. reshape(A,n,m) )
		if (operands == null)                             return null;

		if (operands.length != 3)                         return null;

		if (   (operands[0] == null)       
			|| (operands[1] == null)
			|| (operands[2] == null) )                    return null;

		if (   (!(operands[0] instanceof DoubleNumberToken))
			|| (!(operands[1] instanceof DoubleNumberToken))
			|| (!(operands[2] instanceof DoubleNumberToken))  ) return null;


		// get data from arguments
		double[][] x =      ((DoubleNumberToken)operands[0]).getReValues();
		int        n = (int)((DoubleNumberToken)operands[1]).getReValues()[0][0];		
		int        m = (int)((DoubleNumberToken)operands[2]).getReValues()[0][0];

		int x_dy     = x.length;
        int x_dx     = x[0].length;	
	
		// size(x) == n*m
		if ((x_dy * x_dx) != (n*m))
		{
			com.addi.core.interpreter.ErrorLogger.debugLine("reshape: eval: dimension don't fit");
			return null;
		}
	

		// create matrix
		double[][] values = new double[n][m];
		int yii=0;
		int xii=0;
		for (int xi=0; xi<m ; xi++)
		{
			for (int yi=0; yi<n ; yi++)
			{
				// reshape
				values[yi][xi] = x[yii][xii];
	
				// read original matrix columnwise
				yii++;
				if (yii >= x_dy) 
				{
					yii=0;
					xii++;
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
reshape(matrix to reshape, no of rows, no of columns)
@DOC
Reshapes a matrix.
@NOTES
@EXAMPLES
reshape([1,2,3;4,5,6;7,8,9], 2, 2,) = [1,2;4,5]
@SEE
repmat

*/


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


/**An external function for creating matrices that consist of zeros          *
 * everywhere except in the diagonal. The diagonal consists of ones.         *                        
 * (e.g.: eye(3) will return a 3-by-3 matrix [1,0,0;0,1,0;0,0,1],            *
 *  eye(4,3) will return a 4-by-3 matrix with diagonal set to 1              */
public class eye extends ExternalFunction
{
	/**return a  matrix 
	@param operands[0] = number of rows
	@param operands[1] = number of columns */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		int columns;
		int rows;

		// at least one operands (e.g. eye(n) )
		if (getNArgIn(operands) < 1)
			throwMathLibException("eye: number of arguments < 1");
            
		if (!(operands[0] instanceof DoubleNumberToken)) return null;
		
		rows    = (int)(((DoubleNumberToken)operands[0]).getReValues())[0][0];
		columns = rows;

		// two operands (e.g. eye(n,m) )
		if (operands.length == 2) 
		{
			if (operands[1] == null)                   return null;
			if (!(operands[1] instanceof DoubleNumberToken)) return null;

			columns = (int)((DoubleNumberToken)operands[1]).getReValues()[0][0];
		}
		
		// only positive indices
		if ((rows <= 0) || (columns <= 0)) return null;

		// create matrix
		double[][] values = new double[rows][columns];
		for (int yi=0; yi<=(rows-1) ; yi++)
		{
			for (int xi=0; xi<=(columns-1) ; xi++)
			{
				if (yi==xi) values[yi][xi] = 1.0;
				else        values[yi][xi] = 0.0;
			}
		}
		return new DoubleNumberToken(values);		

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
matrix = eye(size)
@DOC
returns a matrix with the specified size.
@NOTES
@EXAMPLES
<programlisting>
eye(2) = [1,0;0,1]
eye(3) = [1,0,0;0,1,0;0,0,1]
</programlisting>
@SEE
ones, zeros
*/


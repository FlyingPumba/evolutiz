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
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for determining number of non zero elements in a matrix*/
public class nnz extends ExternalFunction
{
	/**Calculate number of non zero elements
	@param operands[0] = the matrix
	@return the number of non zero elements*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		int count = 0;
        
        if (getNArgIn(operands) !=1)
            throwMathLibException("nnz: number of arguments !=1");
		
		if(operands[0] instanceof DoubleNumberToken)
		{
			DoubleNumberToken matrix = ((DoubleNumberToken)operands[0]);
			int sizeX = matrix.getSizeX();
			int sizeY = matrix.getSizeY();
			double[][] values = matrix.getReValues();
			
			for(int yy = 0; yy < sizeY; yy++)
			{
				for(int xx = 0; xx < sizeX; xx++)
				{
					if(values[yy][xx] != 0)
						count++;
				}
			}
		}
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[0].getClass().getName()});

		return new DoubleNumberToken(count);
	}
}

/*
@GROUP
matrix
@SYNTAX
answer=NNZ(matrix)
@DOC
Calculates the number of non zero elements within a matrix.
@NOTES
@EXAMPLES
<programlisting>
NNZ([1,0,2;0,0,5]) = 3
</programlisting>
@SEE
*/


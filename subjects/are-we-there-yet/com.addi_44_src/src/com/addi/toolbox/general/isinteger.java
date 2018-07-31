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

package com.addi.toolbox.general;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**External function to check if a number is a prime*/
public class isinteger extends ExternalFunction
{
	/**@param operands[0] = a matrix of numbers
	@return a matrix the same size with 1 if the number is an integer*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
        if (getNArgIn(operands) != 1)
			throwMathLibException("isinteger: number of arguments != 1");
            
	    if (!(operands[0] instanceof DoubleNumberToken)) 
        	throwMathLibException("isinteger: argument must be a number");
		
		DoubleNumberToken matrix = ((DoubleNumberToken)operands[0]);
		int sizeX = matrix.getSizeX();
		int sizeY = matrix.getSizeY();
		double[][] values = matrix.getReValues();
		double[][] results = new double[sizeY][sizeX];
			
		for(int yy = 0; yy < sizeY; yy++)
		{
			for(int xx = 0; xx < sizeX; xx++)
			{
				results[yy][xx] = isInteger(values[yy][xx]);
			}
		}
		OperandToken result = new DoubleNumberToken(results);
		
        return result;
	}
	
	private double isInteger(double value)
	{
		double result = 0;
		
		if(Math.floor(value) == value) {
			result = 1;
		}
		
		return result;
	}
	
}

/*
@GROUP
general
@SYNTAX
answer=isinteger(value)
@DOC
Checks if value is an integer. returning 1 if it is.
@EXAMPLES
<programlisting>
isinteger(3)=1
isinteger(3.1)=0
</programlisting>
@NOTES
@SEE
CCX
*/


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


/**Display the debug information of an expression*/
public class complex extends ExternalFunction
{
	/**Creates a complex number from a pair of numbers
	@param operand[0] = real part
	@param operand[2] = imaginary part
	@return the complex number*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
        if (getNArgIn(operands) != 2)
			throwMathLibException("complex: number of arguments != 2");
            
		if((operands[0] instanceof DoubleNumberToken) && 
           (operands[1] instanceof DoubleNumberToken)    )
		{
			DoubleNumberToken number1 = ((DoubleNumberToken)operands[0]);
			DoubleNumberToken number2 = ((DoubleNumberToken)operands[1]);
			int sizeX1 = number1.getSizeX();
			int sizeY1 = number1.getSizeY();
			int sizeX2 = number2.getSizeX();
			int sizeY2 = number2.getSizeY();

			//check that the two values are the same size
			if((sizeX1 == sizeX2) && (sizeY1 == sizeY2))
			{
				double [][] valuesR1 = number1.getReValues();			
				double [][] valuesR2 = number2.getReValues();			
	
				double[][] resultR = new double[sizeY1][sizeX1];
				double[][] resultI = new double[sizeY1][sizeX1];
				
				for(int yy = 0; yy < sizeY1; yy++)
				{
					for(int xx = 0; xx < sizeX1; xx++)
					{
						resultR[yy][xx] = valuesR1[yy][xx];
						resultI[yy][xx] = valuesR2[yy][xx];						
					}
				}
				result = new DoubleNumberToken(resultR, resultI);
			}
		}
		return result;
	}
}

/*
@GROUP
general
@SYNTAX
complex(real, imaginary)
@DOC
Creates a complex number from real and imaginary
@EXAMPLES
complex(1,1) = 1+i
@NOTES
@SEE
*/


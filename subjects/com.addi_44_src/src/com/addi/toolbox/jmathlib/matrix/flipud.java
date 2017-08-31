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


/**An external function for flipping matrices from up to down */
public class flipud extends ExternalFunction
{
	/**return a  matrix 
	@param operands[0] = matrix to flip from up to down */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operands (e.g. flipud(A) )
		if (operands == null)                             
            return null;

		if (operands.length != 1)                         
            return null;

		if (operands[0] == null)       
			return null;

		if (!(operands[0] instanceof DoubleNumberToken))
			return null;


		// get data from array
		double[][] real =      ((DoubleNumberToken)operands[0]).getValuesRe();
        double[][] imag =      ((DoubleNumberToken)operands[0]).getValuesIm();

		int dy     = real.length;
        int dx     = real[0].length;	
	
        double re = 0.0;
        double im = 0.0;
    
		// flip matrix
		for (int xi=0; xi<dx ; xi++)
		{
			for (int yi=0; yi<dy/2 ; yi++)
			{
				// flip
				re = real[yi][xi];
	            real[yi][xi]      = real[dy-1-yi][xi];
                real[dy-1-yi][xi] = re; 

				im = imag[yi][xi];
	            imag[yi][xi]      = imag[dy-1-yi][xi];
                imag[dy-1-yi][xi] = im; 
                
			}
        }
		return new DoubleNumberToken(real, imag);		

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
flipud(A)
@DOC
Flips a matrix from up to down.
@NOTES
@EXAMPLES
flipud([1,2,3;4,5,6]) = [4,5,6;1,2,3]
@SEE
fliplr

*/


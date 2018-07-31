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
* (e.g.: zeros(2) will return a 2-by-2 matrix [0,0;0,0],                     *
*  zeros(4,3) will return a 4-by-3 matrix of zeros                           */
public class zeros extends ExternalFunction
{
	/**return matrix of zeros 
	@param operands[0] = number of rows
	@param operands[1] = number of columns */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// at least one operands (e.g. zeros(n) )
		if (getNArgIn(operands)<1)
			throwMathLibException("zeros: number of arguments < 1");

        // number of arguments
        int n = getNArgIn(operands);
        
        // set up dimension array
        int[] dim = new int[n];
        
        // only DoubleNumberTokens accepted
        // each token is one dimension
        for (int i=0; i<n; i++)
        {
            if (!(operands[i] instanceof DoubleNumberToken)) 
                throwMathLibException("zeros: arguments must be numbers");
            
            // get requested dimension
            dim[i] = (int)((DoubleNumberToken)operands[i]).getValueRe();
            
            if (dim[i]<0)
                throwMathLibException("zeros: dimension <0");
        }
        
        // special case for rand(k)  -> rand(k,k)
        if (dim.length==1)
        {
            int d = dim[0];
            dim = new int[]{d,d};
        }
        
        // ceate array of correct size with dimensions "dim"
        DoubleNumberToken num = new DoubleNumberToken(dim, null, null);
        
        // create random value for all values of num
        for (int i=0; i< num.getNumberOfElements(); i++)
        {
            num.setValue(i, 0, 0);
        }
        
        return num;
        

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
zeros(sizey, [sizex])
zeros(n,m,k,l...)
@DOC
Returns a matrix filled with zeros.
@NOTES
@EXAMPLES
<programlisting>
zeros(2,2) = [0,0;0,0]
zeros(2,3) = [0,0,0;0,0,0]
</programlisting>
@SEE
ones
*/


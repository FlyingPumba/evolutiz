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


/**An external function for creating random numbers*/
public class rand extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        //if ( (getNArgIn(operands) < 0) ||
        //     (getNArgIn(operands) > 1)    )
		//	throwMathLibException("rand: number of arguments <0 or >1 ");
        
        // number of arguments
        int n = getNArgIn(operands);

        // in case of 0 arguments return a single random number
        // e.g. rand() -> 0.xxx
        if (n==0)
            return new DoubleNumberToken(Math.random());
            
        // set up dimension array
        int[] dim = new int[n];
        
        // only DoubleNumberTokens accepted
        // each token is one dimension
        for (int i=0; i<n; i++)
        {
            if (!(operands[i] instanceof DoubleNumberToken)) 
                throwMathLibException("rand: arguments must be numbers");
            
            // get requested dimension
            dim[i] = (int)((DoubleNumberToken)operands[i]).getValueRe();

            if (dim[i]<0)
                throwMathLibException("rand: dimension <0");

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
            num.setValue(i, Math.random(), 0);
        }
        
        return num;
        
	}
}

/*
@GROUP
general
@SYNTAX
matrix = rand(size)
matrix = rand(x,y,....)
number = rand()
@DOC
Returns a matrix filled with random numbers.
@EXAMPLES
rand(3,2) = [0.34, 0.45, 0.55
             0.22, 0.33, 0.72]
@NOTES
@SEE
*/


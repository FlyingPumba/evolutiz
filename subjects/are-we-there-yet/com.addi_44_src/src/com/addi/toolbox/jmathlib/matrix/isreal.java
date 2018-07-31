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


/**An external function that return 1.0 is all elements of the argument are non imaginary  */
public class isreal extends ExternalFunction
{
	/**return a  matrix 
	* @param operands[0] = matrix 
	* @return 1.0 if all elements of the argument are nonzero  */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// two operands (e.g. not(A) )
        if (getNArgIn(operands) != 1)
			throwMathLibException("isreal: number of arguments != 1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("isreal: works on numbers only");

        DoubleNumberToken num = (DoubleNumberToken)operands[0];
        boolean realB   = true;
	
        // check all elements
        // if at least one element is imaginary than return FALSE
        for (int n=0; n<num.getNumberOfElements() ; n++)
		{
				if ( num.getValueIm(n) != 0.0)
                    realB = false;
		}	
        
		if (realB)
		    return new DoubleNumberToken(1.0);		
		else
		    return new DoubleNumberToken(0.0);        

        
	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer = isreal(matrix)
@DOC
Returns 1 if any of the elements of the supplied matrix are not zero.
@NOTES
@EXAMPLES
isreal([0,2i]) = 0
isreal([11,0]) = 1
isreal([])     = 1
@SEE
isnan, isimaginary
*/


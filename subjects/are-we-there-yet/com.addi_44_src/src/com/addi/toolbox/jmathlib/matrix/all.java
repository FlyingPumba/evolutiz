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
import com.addi.core.tokens.LogicalToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function that return 1.0 is all elements of the argument are nonzero  */
public class all extends ExternalFunction
{
	/** 
	* @param operands[0] = matrix 
	* @return 1.0 if all elements of the argument are nonzero  */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// two operands (e.g. not(A) )
        if (getNArgIn(operands) != 1)
			throwMathLibException("all: number of arguments != 1");
        
        // if boolean: convert LogicalToken to DoubleNumberToken
        if (operands[0] instanceof LogicalToken)
            operands[0]=((LogicalToken)operands[0]).getDoubleNumberToken(); 

        if (!(operands[0] instanceof DoubleNumberToken))    
            throwMathLibException("all: works on numbers and booleans");


		// get data from arguments
		double[][] a_r = ((DoubleNumberToken)operands[0]).getValuesRe();
        double[][] a_i = ((DoubleNumberToken)operands[0]).getValuesIm();
		int a_dy       = a_r.length;
        int a_dx       = a_r[0].length;	
        boolean allB   = true;
	
		for (int xi=0; xi<a_dx ; xi++)
		{
			for (int yi=0; yi<a_dy ; yi++)
			{
				if ( (a_r[yi][xi] == 0.0) &&
                     (a_i[yi][xi] == 0.0)    ) 
                    allB = false;
			}
		}	
        
		if (allB)
		    return new DoubleNumberToken(1.0);		
		else
		    return new DoubleNumberToken(0.0);        

        
	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer = all(matrix)
@DOC
Returns 1 if all of the elements of the supplied matrix are not zero.
@NOTES
@EXAMPLES
all([0,0;0,0]) = 0
all([1,2;2i,-6]) = 1
@SEE
any, and, not, abs
*/


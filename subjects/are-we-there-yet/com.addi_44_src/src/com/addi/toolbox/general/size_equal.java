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
import com.addi.core.tokens.DataToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for getting the size of matrices*/
public class size_equal extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		int numArgs = getNArgIn(operands);
        
		// operands must all be DataTokens
        for (int i = 0; i < numArgs; i++) {
        	if (!(operands[i] instanceof DataToken)) {
        		throwMathLibException("size_equal: arguments must all be numeric, or numeric arrays");
        	}
        }
        
        // if no operand or just 1 operand, just return true
        if (numArgs < 2) {
        	return new DoubleNumberToken(1.0);
        }
		
        // get size of 1st array (e.g. [2,3,1] for 2x3x1 array
        int[] refsize =  ((DataToken)operands[0]).getSize();
        
		// check ref against others
        for (int j = 1; j < numArgs; j++) {
        	int[] size =  ((DataToken)operands[j]).getSize();
        	if (size.length >= refsize.length) {
        		for (int k = 0; k < size.length; k++) {
        			if (k < refsize.length) {
        				if (refsize[k] != size[k]) {
        					return new DoubleNumberToken(0.0); 
        				}
        			} else {
        				if (size[k] != 1) {
        					return new DoubleNumberToken(0.0);
        				}
        			}
        		}
        	} else {
        		for (int k = 0; k < refsize.length; k++) {
        			if (k < size.length) {
        				if (refsize[k] != size[k]) {
        					return new DoubleNumberToken(0.0); 
        				}
        			} else {
        				if (refsize[k] != 1) {
        					return new DoubleNumberToken(0.0);
        				}
        			}
        		}
        	}
        }
        return new DoubleNumberToken(1.0);
    }
}

/*
@GROUP
general
@SYNTAX
[y, x] = size(matrix)
 z     = size(matrix, n)
@DOC
returns the size of a matrix.
@EXAMPLES
<programlisting>
size([1,2;3,4]) = [2,2]
size([1,2,3;4,5,6]) = [2,3]

a=rand(4,4,2)
size(a)  -> [4,4,2)

size(a,3) -> 2
</programlisting>
@NOTES
@SEE
rows, columns, row, col, ndims
*/


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
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for checking if a matrix is empty (no number or string)  */
public class isempty extends ExternalFunction
{
	/**return a  matrix 
	* @param  operands[0] = matrix 
	* @return position of nonzero elements  */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		if (getNArgIn(operands) != 1)
			throwMathLibException("isEmpty: number of arguments != 1");

		if ((operands[0] instanceof DoubleNumberToken) || 
            (operands[0] instanceof CharToken)   )   
		{
			// check for something like a=[]
            if ( (((DataToken)operands[0]).getSizeY()==0) &&
                 (((DataToken)operands[0]).getSizeX()==0)  )
                return new DoubleNumberToken(1.0);
            
            // token is not empty
			return new DoubleNumberToken(0.0);		
		}
		else
		{
			// token is empty
            // empty means: the token has no number or is no string
			return new DoubleNumberToken(1.0);		
		}

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
answer=ISEMPTY(value)
@DOC
Checks if a matrix is empty (no number or string)
@NOTES
@EXAMPLES
<programlisting>
isempty([]) -> 1

a=[]
isempty(a) -> 1
</programlisting>
@SEE
isfinite, isimaginary, isreal, isnan, isinf
*/


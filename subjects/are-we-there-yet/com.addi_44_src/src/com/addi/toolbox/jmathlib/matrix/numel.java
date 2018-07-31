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
import com.addi.core.tokens.DataToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**number of elements  */
public class numel extends ExternalFunction
{
	/**  */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// two operands (e.g. not(A) )
        if (getNArgIn(operands) != 1)
			throwMathLibException("numel: number of arguments != 1");
        
        if (!(operands[0] instanceof DataToken))
            throwMathLibException("numel: no data token");

        int x = ((DataToken)operands[0]).getSizeX();
        int y = ((DataToken)operands[0]).getSizeY();
        
		return new DoubleNumberToken(x*y);		

	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
numel(array)
@DOC
returns the number of elements in array
@NOTES
@EXAMPLES
numel([2,5,3]) -> 3
@SEE
*/


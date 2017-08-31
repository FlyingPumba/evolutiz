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


/**External function to calculate the number of permutations
when k objects are taken from a set of k*/
public class permutations extends ExternalFunction
{
	/**calculate the number of permutations
	@param operand[0] = The the number of objects taken
	@param operand[1] = The total number of objects
	@return the number of permutations
	*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = new DoubleNumberToken(0);

        if (getNArgIn(operands) != 2)
			throwMathLibException("permutations: number of arguments != 2");

		if(operands.length >= 2 && operands[0] instanceof DoubleNumberToken && operands[1] instanceof DoubleNumberToken)
		{
			//perm(x y) = y!/(y-x)!
			DoubleNumberToken objects = ((DoubleNumberToken)operands[0]);
			DoubleNumberToken total   = ((DoubleNumberToken)operands[1]);
			
			//temp = y-x
			OperandToken temp = ((OperandToken)total.clone());
			temp = temp.subtract(objects);

			//temp = (y-x)!
			temp = temp.factorial();

			//result = y! / (y-x)!
			result = total.factorial();

			result = result.divide(temp);
		}
		return result;
	}
}

/*
@GROUP
general
@SYNTAX
answer = PERMUTATIONS(items, total)
@DOC
Returns the number of permutations when taking count items from a set of total items.
@EXAMPLES
PERMUTATIONS(3,5) = 60
PERMUTATIONS(3,6) = 120
@NOTES

@SEE
combinations

*/


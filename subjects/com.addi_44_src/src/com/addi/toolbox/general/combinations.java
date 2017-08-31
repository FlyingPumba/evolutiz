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


/**External function to calculate the number of combinations
when k objects are taken from a set of k*/
public class combinations extends ExternalFunction
{
	/**calculate the number of combinations
	@param operand[0] = The the number of objects taken
	@param operand[1] = The total number of objects
	@return the number of combinations
	*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = new DoubleNumberToken(0);
		
        if (getNArgIn(operands)!= 2)
			throwMathLibException("combinations: number of arguments != 2");
            
	    if (!(operands[0] instanceof DoubleNumberToken) && 
            !(operands[1] instanceof DoubleNumberToken)	 )
        	throwMathLibException("combinations: arguments must be numbers");

		//comb(x y) = y!/(x! * (y-x)!)
		DoubleNumberToken objects = ((DoubleNumberToken)operands[0]);
		DoubleNumberToken total   = ((DoubleNumberToken)operands[1]);
			
		//result = x!
		result = objects.factorial();

		//temp = y-x
		OperandToken temp = ((OperandToken)total.clone());
		temp = temp.subtract(objects);

		//temp = (y-x)!
		temp = temp.factorial();

		//result = x! * (y-x)!
		result = result.multiply(temp);

		//result = y! / (x! * (y-x)!)
		temp = total.factorial();

		result = temp.divide(result);

		return result;
	}
}

/*
@GROUP
general
@SYNTAX
answer = combinations(items, total)
@DOC
Returns the number of combinations when taking count items from a set of total items.
@EXAMPLES
combinations(3,5) = 10
combinations(3,6) = 20
@NOTES

@SEE
permutations

*/

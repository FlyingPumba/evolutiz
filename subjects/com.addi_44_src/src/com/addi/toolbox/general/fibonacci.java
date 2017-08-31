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


/**External function to calculate the nth fibonacci number*/
public class fibonacci extends ExternalFunction
{
	/**calculate the Fibonacci number
	@param operand[0] = The index of the Fibonacci number
	@return the Fibonacci number
	*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		if (getNArgIn(operands) != 1)
			throwMathLibException("fibonacci: number of arguments != 1");
            
	    if (!(operands[0] instanceof DoubleNumberToken)) 
        	throwMathLibException("fibonacci: first argument must be a number");

		//harmonic(n) = 1 + 1/2 + 1/3 + ... + 1/n
		double index = ((DoubleNumberToken)operands[0]).getValueRe();
			
		double total1 = 0;
		double total2 = 1;
			
		for(int count = 1; count <= index; count++)
		{
			double temp = total2 + total1;
			total1 = total2;
			total2 = temp;
		}
		OperandToken result = new DoubleNumberToken(total1);
		
        return result;
	}
}


/*
@GROUP
general
@SYNTAX
answer = fibonacci(value)
@DOC
Returns the fibonacci number with an index of value.
@EXAMPLES
<programlisting>
fibonacci(5) = 5 
fibonacci(10) = 55
</programlisting>
@NOTES
@SEE
*/


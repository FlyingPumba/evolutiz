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

package com.addi.toolbox.polynomial;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**External function to calculate the set of binomial
   coefficents for the equation (x+y)^r*/
public class binomial extends ExternalFunction
{
	/**calculate the number of permutations
	@param operand[0] = the order of the equation
	@return the coefficients as a vector
	*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = new DoubleNumberToken(0);
		if(operands.length >= 1 && operands[0] instanceof DoubleNumberToken)
		{
			double val = ((DoubleNumberToken)operands[0]).getValueRe();
			int order = (new Double(val)).intValue();

			double[][] results = new double[1][order + 1];
			
			DoubleNumberToken total   = ((DoubleNumberToken)operands[0]);
			for(int count = 0; count <= order; count++)
			{
				//comb(x y) = y!/(x! * (y-x)!)
				DoubleNumberToken objects = new DoubleNumberToken(count);
				
				//result = x!
				OperandToken temp = objects.factorial();
	
				//temp2 = y-x
				OperandToken temp2 = ((OperandToken)total.clone());
				temp2 = temp2.subtract(objects);
	
				//temp2 = (y-x)!
				temp2 = temp2.factorial();
	
				//temp = x! * (y-x)!
				temp = temp.multiply(temp2);
	
				//temp2 = y! / (x! * (y-x)!)
				temp2 = total.factorial();
	
				temp2 = temp2.divide(temp);

				results[0][count] = ((DoubleNumberToken)temp2).getValueRe();
			}
			result = new DoubleNumberToken(results);
		}
		return result;
	}
}

/*
@GROUP
polynomial
@SYNTAX
answer = binomial(value)
@DOC
Calculates the binomial coefficients of (x+y)^value.
@NOTES
@EXAMPLES
<programlisting>
binomial(3) = [1, 3, 3, 1]
binomial(4) = [1, 4, 6, 4, 1]
</programlisting>
@SEE
poly, roots
*/


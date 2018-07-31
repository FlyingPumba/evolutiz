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


/**External function to calculate the nth harmonic number*/
public class harmonic extends ExternalFunction
{
	/**calculate the harmonic number
	@param operand[0] = The index of the harmonic number
	@return the harmonic number
	*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = new DoubleNumberToken(0);
		if(operands.length >= 1 && operands[0] instanceof DoubleNumberToken)
		{
			//harmonic(n) = 1 + 1/2 + 1/3 + ... + 1/n
			double index = ((DoubleNumberToken)operands[0]).getValueRe();
			
			double total = 0;
			for(int count = 1; count <= index; count++)
			{
				total += (1.0/ count);
			}
			result = new DoubleNumberToken(total);
		}
		return result;
	}
}


/*
@GROUP
general
@SYNTAX
answer = harmonic(value)
@DOC
Returns the harmonic number with an index of value.
@EXAMPLES
<programlisting>
harmonic(5) = 2.283333333333333
harmonic(10) = 2.9289682539682538
</programlisting>
@NOTES
This calculates sum(1..value){1/n}
@SEE
*/


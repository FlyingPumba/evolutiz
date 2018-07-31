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


/**External function to create a vector of the prime
factors of a number*/
public class factor extends ExternalFunction
{
	/**@param operands[0] = the number
	@return a vector containing the prime factors*/
	@Override
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		DoubleNumberToken result = null;

        if (getNArgIn(operands) != 1)
			throwMathLibException("factor: number of arguments != 1");

	    if (!(operands[0] instanceof DoubleNumberToken))
        	throwMathLibException("factor: first argument must be a number");

		double maxValue = ((DoubleNumberToken)operands[0]).getValueRe();

		int temp = (new Double(maxValue/2)).intValue();
		double[][] tempResults = new double[1][temp];

		int count = 0;
		double end = maxValue;
		double tempVal = maxValue / 2;
		while (java.lang.Math.ceil(tempVal) == tempVal)
		{
			tempResults[0][count] = 2;
			count++;
			maxValue = tempVal;
			tempVal = maxValue / 2;
		}

        for(int index = 3; index < end; index += 2)
		{
			if(isPrime(index) == 1)
			{
				tempVal = maxValue / index;
				while (java.lang.Math.ceil(tempVal) == tempVal)
				{
					tempResults[0][count] = index;
					count++;
					maxValue = tempVal;
		            tempVal = maxValue / index;
				}
			}
		}

		double[][] results = new double[1][count];

		for(int index = 0; index < count; index++)
		{
			results[0][index] = tempResults[0][index];
		}
		result	= new DoubleNumberToken(results);

        return result;
	}

	private double isPrime(double value)
	{
		double result = 0;

		if(value == 2)
			result = 1;
		else if((java.lang.Math.ceil(value/2) != (value/2)) && (value > 2))
		{
			result = 1;
			for(int index = 3; index < value && result != 0; index += 2)
			{
				double temp = value/index;
				if(java.lang.Math.ceil(temp) == temp)
					result = 0;
			}
		}

		return result;
	}
}

/*
@GROUP
general
@SYNTAX
answer=factor(values)
@DOC
Calculates all the prime factors of value.
@EXAMPLES
<programlisting>
factor(10)=[2,5]
factor(30)=[2,3,5]
</programlisting>
@NOTES
This function will take a long time when factorising large values
@SEE
*/


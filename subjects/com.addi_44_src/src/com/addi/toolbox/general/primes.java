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
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**External function to create a vector of primes*/
public class primes extends ExternalFunction
{
	/**@param operands[0] = the maximum number
	@return a vector containing the prime values*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		DoubleNumberToken result = null;
        
		// at least one operand
        if (getNArgIn(operands) != 1)
			throwMathLibException("primes: number of arguments != 1");

		
		if(operands[0] instanceof DoubleNumberToken)
		{
			double maxValue = ((DoubleNumberToken)operands[0]).getValueRe();
			
			if(maxValue < 2)
			{
				result = new DoubleNumberToken(0);
			}
			else if(maxValue == 2)
			{
				result = new DoubleNumberToken(2);
			}
			else
			{
				int temp = (new Double(maxValue/2)).intValue();
				double[][] tempResults = new double[1][temp];
				
				tempResults[0][0] = 2;
				int count = 1;
				for(int index = 3; index < maxValue; index += 2)
				{
					if(isPrime(index) == 1)
					{					
						tempResults[0][count] = index;
						count++;
					}
				}

				double[][] results = new double[1][count];
				
				for(int index = 0; index < count; index++)
				{
					results[0][index] = tempResults[0][index];
				}
				result	= new DoubleNumberToken(results);			
			}
		}
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[0].getClass().getName()});

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
PRIMES(value)
@DOC
Calculates all the primes up to value.
@EXAMPLES
PRIMES(10) = [2, 3, 5, 7]
@NOTES
@SEE
isprime
*/


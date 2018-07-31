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

import com.addi.core.functions.*;
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.*;


/**An external function to compute the binary and of two numbers*/
public class bitand extends ExternalFunction
{
	/**Returns the binary and of two numbers
	@param operands[0] - The first number
	@param operands[1] - The second number
	@return the result of the function as an OperandToken*/	
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
        DoubleNumberToken result = DoubleNumberToken.zero;
        if (getNArgIn(operands) != 2)
			throwMathLibException("BitAnd: number of arguments != 2");
            

        if(operands[0] instanceof DoubleNumberToken)
        {
            if(operands[1] instanceof DoubleNumberToken)
            {
                int value1 = ((DoubleNumberToken)operands[0]).getIntValue(0,0);   
                int value2 = ((DoubleNumberToken)operands[1]).getIntValue(0,0);   
                
                result = new DoubleNumberToken(value1 & value2);
            }
            else
			     Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[1].getClass().getName()});
        }
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[0].getClass().getName()});

        return result;
	}
}

/*
@GROUP
general
@DOC
Calcuates the bitwise AND of number1 and number2
@SYNTAX
bitand(number1, number2);
@EXAMPLES
<programlisting>
bitand(5, 9) = 1;
bitand(5, 7) = 5;
</programlisting>
@SEE
bitor, bitshift, bitxor
*/

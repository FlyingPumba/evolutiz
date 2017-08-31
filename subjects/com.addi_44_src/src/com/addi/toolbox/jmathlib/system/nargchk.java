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

package com.addi.toolbox.jmathlib.system;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for checking the number of arguments*/
public class nargchk extends ExternalFunction
{
	/**check the number of arguments for a script file
	@param operand[0] = the lowest number of arguments
	@param operand[1] = the highest number of arguments
	@param operand[2] = the actual number of arguments
	@return an error string if the number of arguments is
	not between the lowest and highest values
	*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;

        if (getNArgIn(operands) !=3)
            throwMathLibException("nargchk: number of arguments !=3");
		
		if(operands[0] instanceof DoubleNumberToken)
		{
			if(operands[1] instanceof DoubleNumberToken)
			{
				if(operands[2] instanceof DoubleNumberToken)
				{
					String message = "";
					double min = ((DoubleNumberToken)operands[0]).getValueRe();
					double max = ((DoubleNumberToken)operands[1]).getValueRe();
					double val = ((DoubleNumberToken)operands[2]).getValueRe();
					
					if(val < min)
						message = Errors.getErrorText(ERR_INSUFFICIENT_PARAMETERS, new Object[] {operands[0]});
					else if(val > max)
						message = Errors.getErrorText(ERR_TOO_MANY_PARAMETERS, new Object[] {operands[1]});
					
					result = new CharToken(message);
				}
				else
					Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[2].getClass().getName()});
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
system
@SYNTAX
nargchk(min, max, no of input args)
@DOC
Checks that the number of input args is between min and max
@NOTES
@EXAMPLES
NARGOUTCHK(1, 3, NARGIN)
@SEE
nargoutchk
*/


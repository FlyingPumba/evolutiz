/* 
 * This file is part or JMathLib 
 * 
 * Check it out at http://www.jmathlib.de
 *
 * Author:  
 * (c) 2005-2009   
 */
package com.addi.toolbox.jmathlib.system;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for checking the number of arguments*/
public class nargoutchk extends ExternalFunction
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
            throwMathLibException("nargoutchk: number of arguments !=3");
		
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
nargoutchk(min, max, no of output args)
@DOC
Checks that the number of output args is between min and max
@NOTES
@EXAMPLES
<programlisting>
NARGOUTCHK(1, 3, NARGOUT)
</programlisting>
@SEE
nargchk
*/


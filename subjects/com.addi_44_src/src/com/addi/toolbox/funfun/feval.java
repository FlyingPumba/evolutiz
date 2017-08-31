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

package com.addi.toolbox.funfun;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.FunctionToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for evaluating functions*/
public class feval extends ExternalFunction
{
	/**Perform the named function on the operands
	@param operand[0] = the name of the function*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		FunctionToken function = null;

		if(operands[0] instanceof CharToken)
		{
			String name = ((CharToken)operands[0]).getElementString(0);
			function = new FunctionToken(name);
		}
		else if(operands[0] instanceof FunctionToken)
		{
			function = ((FunctionToken)operands[0]);
		}
		else
		{
			//error, unsupported type
            throwMathLibException("feval: unknown type of argument");
		}
		
        OperandToken[] op = new OperandToken[operands.length-1];
        
		for(int operandNo = 0; operandNo < operands.length -1; operandNo++)
		{
			op[operandNo] = (OperandToken)(operands[operandNo + 1].clone());
		}
		
        function.setOperands(op);
        
		return function.evaluate(null, globals);
	}
}

/*
@GROUP
FunFun
@SYNTAX
feval(function, parameters)
@DOC
evaluates the function on the supplies parameters.
@EXAMPLES
<programlisting>
feval('cos', 1) = 0
feval("max",1,3) = 3
</programlisting>
@NOTES
@SEE
*/

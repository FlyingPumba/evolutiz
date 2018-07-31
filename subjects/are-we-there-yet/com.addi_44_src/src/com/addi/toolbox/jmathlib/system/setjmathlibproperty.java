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
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**External function to get a enviroment variable*/
public class setjmathlibproperty extends ExternalFunction
{
	/**Returns an enviroment variable
	@param operand[0] = the name of the variable
	@param operand[1] = a default value (optional)
	@return the enviroment value*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if (getNArgIn(operands)!=2)
            throwMathLibException("setjmathlibproperty: number of arguments != 2");
        

        if ( (!(operands[0] instanceof CharToken)) &&
             (!(operands[1] instanceof CharToken))    )
            throwMathLibException("setjmathlibproperty: arguments must be strings");

			String name = ((CharToken)operands[0]).getElementString(0);
            String prop = ((CharToken)operands[1]).getElementString(0);
		
            globals.setProperty(name, prop);
			
			
		return result;
	}
}

/*
@GROUP
system
@SYNTAX
setjmathlibproperty(property name, value)
@DOC
Returns the value of the enviromental variable variablename.
@NOTES
@EXAMPLES
GETENV("HOME")= "/home/user"
@SEE
getjmathlibproperty, getenv
*/


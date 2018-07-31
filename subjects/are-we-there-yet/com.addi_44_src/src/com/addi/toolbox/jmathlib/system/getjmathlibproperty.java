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
public class getjmathlibproperty extends ExternalFunction
{
	/**Returns an enviroment variable
	@param operand[0] = the name of the variable
	@param operand[1] = a default value (optional)
	@return the enviroment value*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if (getNArgIn(operands)!=1)
            throwMathLibException("getjmathlibproperty: number of arguments != 1");
        

        if (!(operands[0] instanceof CharToken))
            throwMathLibException("getjmathlibproperty: number of arguments != 1");

			String name = ((CharToken)operands[0]).getElementString(0);
			String defaultVal = "";
			
		
			String property = globals.getProperty(name);
			
			result = new CharToken(property);
			
		return result;
	}
}

/*
@GROUP
system
@SYNTAX
getjmathlibproperty(variablename)
@DOC
Returns the value of the enviromental variable variablename.
@NOTES
@EXAMPLES
getlocal("HOME")= "/home/user"
@SEE
getenv, setjmathlibproperty
*/


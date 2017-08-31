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


/**An external function for displaying error messages
aborts the current function being processed*/
public class printstacktrace extends ExternalFunction
{
	/**write operand to main display then abort processing
	@param operand[n] = error messages to display*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{	
        String val = "";
		if(operands[0] instanceof CharToken)
		{
			val = ((CharToken)operands[0]).getElementString(0) + "\n";
		}	
        
        //val += getContextList().getStackTrace();
		return  new CharToken(val);
	}
}

/*
@GROUP
system
@SYNTAX
printstacktrace(message)
@DOC
Displays message and the current execution stack trace.
@NOTES
@EXAMPLES
Error("There has been an error")
There has been an error
@SEE
warning, error
*/


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


/**An example of an external function - it returns the first parameter*/
public class java extends ExternalFunction
{
	/**Executes the function - returning the first parameter
	@param operands - the array of parameters
	@return the result of the function as an OperandToken*/	
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{    
	    
        // load java plugin
	    globals.getPluginsManager().addPlugin("JavaPlugin");
        
        //CCX String result = ((JavaPlugin)globals.getPluginsManager().getPlugin("JavaPlugin")).executeJavaExpression(operands[0].toString());
	    String result = "not supported";
	    
	    return new CharToken(result);
	}
}



/*

java('console.displayText("hello world");')
hello world
ans = null

To access a JMathLib variable enter
x=5
JAVA('global.getVariable("x");')
ans = 5

*/
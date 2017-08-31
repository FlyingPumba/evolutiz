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

/* This file is part or JMATHLIB */

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for enabling/disabling of caching of p-files  */
public class getpfilecaching extends ExternalFunction
{
	/**status of caching of p-files 
	* @return whether or not caching of p-files is enabled/disabled 
    */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		if (getNArgIn(operands)!=0)
			throwMathLibException("getPFileCaching: number of input arguments != 0");

		boolean cachingEnabled = globals.getFunctionManager().getPFileCaching();  	

		if (cachingEnabled)
		    return new DoubleNumberToken(1);
        else
            return new DoubleNumberToken(0);

	} // end eval
}


/*
@GROUP
General
@SYNTAX
x = getPFileCaching()
@DOC
Returns whether or not caching of p-files is enabled or disabled. 
Returns 1 if caching is enabled. 
Returns 0 if caching is disabled.
@EXAMPLES
x = getPFileCaching()
@NOTES
@SEE
setpfilecaching
*/

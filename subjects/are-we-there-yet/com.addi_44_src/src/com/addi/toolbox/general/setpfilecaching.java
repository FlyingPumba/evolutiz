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
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for enabling/disabling of caching of p-files  */
public class setpfilecaching extends ExternalFunction
{
	/**enable or disable caching of p-files 
	* @param operands[0] 1, 0 , 'on', 'off' 
    */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		// one operand (e.g. setPFileCaching(1)
		// one operand (e.g. setPFileCaching('on')
		
		if (getNArgIn(operands)!=1)
			throwMathLibException("setPFileCaching: number of input arguments != 1");

		if (operands[0] instanceof DoubleNumberToken)
		{
			if ( ((DoubleNumberToken)operands[0]).getValueRe()==0)
			    globals.getFunctionManager().setPFileCaching(false);
			else
			    globals.getFunctionManager().setPFileCaching(true);  	
		}
		else if (operands[0] instanceof CharToken)
		{
			if ( ((CharToken)operands[0]).getValue().equals("on"))
			    globals.getFunctionManager().setPFileCaching(true);
			else
			    globals.getFunctionManager().setPFileCaching(false);  	
		}
       
		return null;

	} // end eval
}


/*
@GROUP
General
@SYNTAX
setPFileCaching(value)
@DOC
enables or disables caching of p-files
@EXAMPLES
<programlisting>
setPFileCaching(1)      enable caching 

setPFileCaching('on')   enable caching 

setPFileCaching(0)      disable caching 

setPFileCaching('off')  disable caching 
</programlisting>
@NOTES
@SEE
getpfilecaching
*/

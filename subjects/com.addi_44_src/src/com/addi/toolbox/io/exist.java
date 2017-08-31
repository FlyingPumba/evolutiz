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

package com.addi.toolbox.io;


import java.io.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**An external function used to check if a file exists*/
public class exist extends ExternalFunction
{
	/**Check if file exists
	@param 0 = filename
	@return 1 if the file exists*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
    
    	// at least one operand
        if (getNArgIn(operands) != 1)
			throwMathLibException("exist: number of arguments != 1");

    
		String fileName = ((CharToken)operands[0]).getElementString(0);
		File testFile = null;
		if((fileName.indexOf(":") > -1))
			testFile = new File(fileName);
		else
		{
			if (fileName.startsWith("/")) {
				testFile = new File(fileName);
			} else {
				testFile = new File(globals.getWorkingDirectory(), fileName);
			}
		}
		
		OperandToken result = null;
		
		if(testFile.exists())
			result = new DoubleNumberToken(1);
		else
			result = new DoubleNumberToken(0);
			
		return result;
	}
}

/*
@GROUP
IO
@SYNTAX
exist(filename)
@DOC
Checks if a file exists
@EXAMPLES
<programlisting>
exist("bar.txt")
</programlisting>
@SEE
cd, createnewfile, dir, mkdir, rmdir, delete, isfile, isdirectory, ishidden, lastmodified
*/


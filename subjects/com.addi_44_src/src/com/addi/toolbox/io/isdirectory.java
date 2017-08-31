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
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

public class isdirectory extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1)
			throwMathLibException("isdirectory: number of arguments != 1");

		if (!(operands[0] instanceof CharToken)) 
            throwMathLibException("isdirectory: argument must be a string");

        String name = ((CharToken)operands[0]).getElementString(0);

		try
		{
			File dir;
			if (name.startsWith("/")) {
				dir = new File(name);
			} else {
				dir = new File(globals.getWorkingDirectory(), name);
			}
			if (dir.isDirectory())
			    return new DoubleNumberToken(1);
            else
                return new DoubleNumberToken(0);
                
		}
		catch (Exception e)
		{
            throwMathLibException("isdirectory: IO exception");
		}		    

		return null;		

	} // end eval
}

/*
@GROUP
IO
@SYNTAX
isdirectory(name)
@DOC
checks if the given name is a directory
@EXAMPLES
<programlisting>
isdirectory("bar");
</programlisting>
@SEE
cd, createnewfile, dir, exist, mkdir, rmdir, delete, isfile, ishidden, lastmodified
*/


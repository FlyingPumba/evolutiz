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

public class lastmodified extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1)
			throwMathLibException("lastmodified: number of arguments != 1");

		if (!(operands[0] instanceof CharToken)) 
            throwMathLibException("lastmodified: argument must be a string");

        String name = ((CharToken)operands[0]).getElementString(0);

		try
		{
			File file;
			if (name.startsWith("/")) {
				file = new File(name);
			} else {
				file = new File(globals.getWorkingDirectory(), name);
			}
			
            // check if file exists in current workspace
            if (!file.exists())
                throwMathLibException("lastmodified: file does not exist");
            
            // return date of last modification: seconds since 1970
            //   -> may need some better return value :-)
			return new DoubleNumberToken(file.lastModified());
		}
		catch (Exception e)
		{
            throwMathLibException("lastmodified: file does not exist");
		}		    

		return null;		

	} // end eval
}

/*
@GROUP
IO
@SYNTAX
lastmodified(filename)
@DOC
Returns the date of the last modification of given file
@EXAMPLES
<programlisting>
lastmodified("barfoo.txt");
</programlisting>
@NOTES
@SEE
cd, createnewfile, dir, exist, mkdir, rmdir, delete, isfile, isdirectory, ishidden
*/


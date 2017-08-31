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

/**An external function for getting a directory listing         */
public class dir extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 0)
            throwMathLibException("dir: number of arguments != 0");

		String path=".";

		File dir = new File(globals.getWorkingDirectory(), path);
		String[] files = dir.list();

		
		for (int i=0; i<files.length ; i++)
		{

			String name = files[i]; 
			File   f    = new File(name);
			if (f.isDirectory())
			    globals.getInterpreter().displayText(files[i]+"/");
			else
			    globals.getInterpreter().displayText(files[i]);

		}


		return null;		

	} // end eval
}

/*
@GROUP
IO
@SYNTAX
dir()
dir(directory)
@DOC
Displays the file list for the current directory.
@EXAMPLES
<programlisting>
dir()
MathLib.MathLib.html
MathLib.log
MathLib.bat
object.txt
Demos.class
MathLib/
CVS/
Demos$1.class
1.0
</programlisting>
@SEE
cd, createnewfile, exist, mkdir, rmdir, delete, isfile, isdirectory, ishidden, lastmodified
*/


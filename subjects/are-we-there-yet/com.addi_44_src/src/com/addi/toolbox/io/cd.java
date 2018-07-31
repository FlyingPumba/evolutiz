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
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;

/**An external function for changing to another directory         */
public class cd extends ExternalFunction
{

    /* @param operands[0] string which specifies the directory               *
     *   to change to (optional).                                            *
	 *   If invoked with now paramenter the current directory is returned.   */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		String path=".";

		// at least one operand
        if (getNArgIn(operands) > 1)
			throwMathLibException("cd: number of arguments > 1");

        if (getNArgIn(operands) == 1)
        {
    		// check if a directory is specified
    		if ((operands[0] instanceof CharToken)) 
    		{
    			path = ((CharToken)operands[0]).getElementString(0);
    		}
    		else
            	throwMathLibException("cd: argument must be a string");
        }
        
		try
		{
			File dir;
			if (path.startsWith("/")) {
				dir = new File(path);
			} else {
			    dir = new File(globals.getWorkingDirectory(), path);
			}
			//getInterpreter().displayText("canonical path = "+dir.getCanonicalPath());		

			if (dir.isDirectory())
			{
			    globals.setWorkingDirectory(dir);		

				if (getNoOfLeftHandArguments()==1)
					return new CharToken(dir.getCanonicalPath());
				else 
				    globals.getInterpreter().displayText(dir.getCanonicalPath());

			}
		}
		catch (Exception e)
		{
			ErrorLogger.debugLine("cd: IO exception");
		}		    

		return null;		

	} // end eval
}

/*
@GROUP
IO
@SYNTAX
cd(directory)
@DOC
Sets the working directory to directory. Also switches between directories.
@EXAMPLES
<programlisting>
cd("C:\barfoo");
</programlisting>
@SEE
dir, cd, isdirectory
*/


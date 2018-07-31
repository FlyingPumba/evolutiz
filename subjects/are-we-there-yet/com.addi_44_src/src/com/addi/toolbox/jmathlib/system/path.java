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


import java.io.File;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.functions.FileFunctionLoader;
import com.addi.core.functions.FunctionLoader;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;

/**External function to display the current search path*/
public class path extends ExternalFunction
{
    /**return the search path as a string token*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        StringBuffer pathString = new StringBuffer();
        
        for (int i=0; i<globals.getFunctionManager().getFunctionLoaderCount(); i++) 
        {
            // get one of the function loaders
            FunctionLoader loader = globals.getFunctionManager().getFunctionLoader(i);
            
            // check if loader is loading files
            if (loader instanceof FileFunctionLoader) 
            {
                FileFunctionLoader ffl = (FileFunctionLoader)loader;
                
                // get paths from current function loader
                for (int pathIdx=0; pathIdx<ffl.getPathCount(); pathIdx++) 
                {
                    File path = ffl.getPath(pathIdx);
                    
                    pathString.append(path.toString() + "\n");                    
                }
            }
            else
                throwMathLibException("path: wrong type of loader");
        }

        return new CharToken(new String(pathString));
    }
}

/*
@GROUP
system
@SYNTAX
path()
@DOC
Lists the current search path.
@NOTES
@EXAMPLES
<programlisting>
path()
</programlisting>
@SEE
addpath, rmpath, createfunctionslist
*/


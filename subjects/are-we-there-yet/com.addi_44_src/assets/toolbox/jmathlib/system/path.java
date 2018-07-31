/* 
 * This file is part or JMathLib 
 * 
 * Check it out at http://www.jmathlib.de
 *
 * Author:  
 * (c) 2005-2009   
 */
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


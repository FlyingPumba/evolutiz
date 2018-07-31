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
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**External function to remove an item from the search path*/
public class rmpath extends ExternalFunction
{
    /**removes an item from the search path
    @param operands[0] = item to remove*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        if (getNArgIn(operands)!=1)
            throwMathLibException("rmpath: number of arguments != 1");
        
        if ((operands[0] instanceof CharToken))
            throwMathLibException("rmpath: works only on char arrays");

        File path = new File(((CharToken)operands[0]).getElementString(0));
        
        for (int i=0;i<globals.getFunctionManager().getFunctionLoaderCount();i++) 
        {
            FunctionLoader loader = globals.getFunctionManager().getFunctionLoader(i);
            
            if (loader instanceof FileFunctionLoader) 
            {
                FileFunctionLoader ffl = (FileFunctionLoader)loader;
                                    
                if (ffl.getBaseDirectory().compareTo(path) == 0) 
                {
                    globals.getFunctionManager().removeFunctionLoader(loader);
                    break;
                }
            }
        }
        
        return DoubleNumberToken.one;
            
    }
}

/*
@GROUP
system
@SYNTAX
rmpath(path)
@DOC
Removes path from the current search path
@NOTES
Using rmpath will stop the system from finding any external functions
and M-files stored within the path.
@EXAMPLES
rmpath("./Functions/General")
@SEE
addpath, path
*/


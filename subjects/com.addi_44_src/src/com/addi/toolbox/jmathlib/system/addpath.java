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



/**External function to add an item to the search path*/
public class addpath extends ExternalFunction
{
    /**adds an item to the search path
    @param operands[0] = item to add*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        if (getNArgIn(operands)!=1)
            throwMathLibException("addpath: number of arguments != 1");

        boolean prepend = true;
        
		for(int index = 0; index < operands.length; index++)
		{
            
            // check if operand is of type char token
            if (!(operands[index] instanceof CharToken))
                throwMathLibException("addpath: parameter "+index+" is not a char array");
            
            String path = ((CharToken)operands[index]).getElementString(0);
            
            if(path.equalsIgnoreCase("end") || path.equals("1"))
                prepend = false;
            else if(path.equalsIgnoreCase("begin") || path.equals("0"))
                prepend = true;
        }
               
		for(int index = 0; index < operands.length; index++)
		{
            String path = ((CharToken)operands[index]).getElementString(0);
            if(!(path.equalsIgnoreCase("end") || path.equals("1") || path.equalsIgnoreCase("begin") || path.equals("0")))
            {
                FunctionLoader loader = new FileFunctionLoader(new File(path), true);
                if (!prepend)
                    globals.getFunctionManager().addFunctionLoader(loader);
                else globals.getFunctionManager().addFunctionLoaderAt(0, loader);
            }
        }
            
        return DoubleNumberToken.one;
    }
}

/*
@GROUP
system
@SYNTAX
addpath(path)
@DOC
Adds path to the current search path.
@NOTES
@EXAMPLES
addpath("../newpath")
@SEE
path, rmpath
*/


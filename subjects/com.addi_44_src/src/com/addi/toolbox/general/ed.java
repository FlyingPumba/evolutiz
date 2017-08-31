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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.FileOutputStream;

import android.content.Context;
import android.content.Context.*;

import com.addi.core.functions.*;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.interpreter.Interpreter;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**
 * An external function for openning up a .m file editor
 */
public class ed extends ExternalFunction
{

    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	String fileName = "";
    	
    	if (operands.length > 1)
    		throwMathLibException("ed: number of arguements > 1, only supports editing 1 file at a time");
    	
    	if (operands.length == 0)
    		throwMathLibException("ed: no file specified");
    	
        // Check if there's only parameters
    	if (getNArgIn(operands) == 1)
        {
    		// check if a file name is specified
    		if ((operands[0] instanceof CharToken)) 
    		{
    			fileName = ((CharToken)operands[0]).getElementString(0);
    			
    			if (fileName.endsWith(".m")) {
    				if (fileName.startsWith("/")) {
    					fileName = fileName;
    				} else {
    					fileName = globals.getWorkingDirectory() + "/" + fileName;
    				}
    			} else {
    				throwMathLibException("filename must end with .m");
    			}
    		}
    		else
    		{
            	throwMathLibException("ed: argument must be a string filename");
    		}
    	}
		globals.getInterpreter().displayText("STARTUPADDIEDITWITH="+fileName);
        
        return null;
    }
}

/*
@GROUP
general
@SYNTAX
ed(filename)
@DOC
Opens up window for editing file of name specified
@EXAMPLES
<programlisting>
ed("temp.txt")
</programlisting>
@SEE
ed
*/
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
import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;

/**An external function for loading and running m-files  (script-files)      *
 * This function is only for script-files not for function-files             */
public class runfile extends ExternalFunction
{
	/** Check that the operand is a string then open the file                *
	 *  referenced.                                                          *
         * @param operands[0] string which specifies the function to load    */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		String answerString="";
		String lineFile="";
		String line=" ";


		// One operand of type CharToken (e.g. runfile("test") )
        if (getNArgIn(operands) != 1)
			throwMathLibException("RunFile: number of arguments != 1");

		if (!(operands[0] instanceof CharToken)) 
        	throwMathLibException("RunFile: argument must be a string");
		
		String fileName = ((CharToken)operands[0]).getElementString(0);
			
		// If the filename doesn't have an extension add the default 
		//     extension of .m
		if(fileName.indexOf(".m") == -1)	fileName += ".m";
			
		File scriptFile;
		if (fileName.startsWith("/")) {
			scriptFile = new File(fileName);
		} else {
			scriptFile = new File(globals.getWorkingDirectory(), fileName);
		}
		
		if(!scriptFile.exists()) return null;

		ErrorLogger.debugLine("loading >"+fileName+"<");

		try 
		{			
			// load file 
			BufferedReader inReader = new BufferedReader( new FileReader(fileName));
			while ( line != null)
			{
				line= inReader.readLine();	    	
				lineFile += line;
			}
			inReader.close();					

			//execute the file and store the answer
			answerString = globals.getInterpreter().executeExpression(lineFile, com.addi.core.interpreter.Interpreter.getActivity(),com.addi.core.interpreter.Interpreter.getHandler());		

		}
		catch (Exception e)
		{
			ErrorLogger.debugLine("RunFile: load function exception");
		}		    

		
		return null;
	}
}

/*
@GROUP
IO
@SYNTAX
runfile("filename")
@DOC
Runs the script file specified by filename.
@NOTE
This is used to run script files, not function files.
@EXAMPLES
<programlisting>
runfile("script.m");
</programlisting>
@SEE
dir, cd, systemcommand
*/


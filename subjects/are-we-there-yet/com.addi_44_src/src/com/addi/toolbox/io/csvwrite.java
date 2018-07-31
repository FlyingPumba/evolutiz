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
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**An external function for loading a matrix from a csv file*/
public class csvwrite extends ExternalFunction
{
	/** Check that the operand is a string then open the file                
	   referenced.                                                          
       @param operands[0] = string which specifies the csv file to write    
       @param operands[1] = the matrix to save    
       @param operands[2] = the start row (optional)
       @param operands[3] = the start column (optional)*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		// at least one operand
        if (getNArgIn(operands) != 2)
			throwMathLibException("CSVWrite: number of arguments != 2");
		
		if(operands[0] instanceof CharToken)
		{
			if(operands[1] instanceof DoubleNumberToken)
			{
				double[][] values = ((DoubleNumberToken)operands[1]).getReValues();
				String fileName = ((CharToken)operands[0]).getElementString(0);
				
				File CSVFile;
				if (fileName.startsWith("/")) {
					CSVFile = new File(fileName);
				} else {
					CSVFile = new File(globals.getWorkingDirectory(), fileName);
				}
				ErrorLogger.debugLine("Writing CSV>"+fileName+"<");
				
				int startLine = 0;
				int startColumn = 0;
				
				if(operands.length > 2)
				{
					if(operands[2] instanceof DoubleNumberToken)
						startLine = ((DoubleNumberToken)operands[2]).getIntValue(0,0);
						
					if(operands.length > 3)
					{
						if(operands[3] instanceof DoubleNumberToken)
							startColumn = ((DoubleNumberToken)operands[3]).getIntValue(0,0);
					}
				}
				try 
				{		
					BufferedWriter outWriter = new BufferedWriter( new FileWriter(CSVFile));
	
					try 
					{		
						for(int row = 0; row < startLine; row++)
						{
							outWriter.newLine();
						}
					
						for(int row = 0; row < values.length; row++)
						{
							String line = "";
							for(int column = 0; column < startColumn; column++)
							{
								line = line +  " ";
							}
							for(int column = 0; column < values[row].length; column++)
							{
								line = line + values[row][column] + ",";
							}
							
							outWriter.write(line, 0, line.length() - 1);
							outWriter.newLine();
						}
					}
					catch(Exception e)
					{
						ErrorLogger.debugLine("CSVRead: load function exception - " + e.getMessage());
					}
					outWriter.close();
				}
				catch(Exception e)
				{
					ErrorLogger.debugLine("CSVRead: load function exception - " + e.getMessage());
				}
			}				
			else
				Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken", operands[1].getClass().getName()});
		}
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[0].getClass().getName()});
			
		return null;
	}
}

/*
@GROUP
IO
@SYNTAX
csvwrite(filename, matrix, startrow, endrow)
@DOC
Writes a matrix out to a file in comma seperated value format.
@EXAMPLES
<programlisting>
csvwrite("testfile.csv", [1,2;3,4], 0 ,0)
</programlisting>
@SEE
csvread, urlread, dir, delete
*/


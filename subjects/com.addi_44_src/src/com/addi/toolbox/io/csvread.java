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
import java.util.Stack;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**An external function for loading a matrix from a csv file*/
public class csvread extends ExternalFunction
{
	/** Check that the operand is a string then open the file                
	   referenced.                                                          
       @param operands[0] = string which specifies the csv file to load    
       @param operands[1] = the start row (optional)
       @param operands[2] = the start column (optional)
       @param operands[3] = range(optional, not implemented)
       @return the matrix as an OperandToken*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		DoubleNumberToken result = null;
        
        // at least one operand
        if (getNArgIn(operands) < 1)
			throwMathLibException("CSVRead: number of arguments must be > 0");

		if(operands[0] instanceof CharToken)
		{
			String fileName = ((CharToken)operands[0]).getElementString(0);
	
			File CSVFile;
			if (fileName.startsWith("/")) {
				CSVFile = new File(fileName);
			} else {
				CSVFile = new File(globals.getWorkingDirectory(), fileName);
			}	
	
			if(!CSVFile.exists()) return null;
	
			ErrorLogger.debugLine("loading CSV>"+fileName+"<");
			
			int startLine = 0;
			int startColumn = 0;
			
			if(operands.length > 1)
			{
				if(operands[1] instanceof DoubleNumberToken)
					startLine = ((DoubleNumberToken)operands[1]).getIntValue(0,0);
					
				if(operands.length > 2)
				{
					if(operands[2] instanceof DoubleNumberToken)
						startColumn = ((DoubleNumberToken)operands[2]).getIntValue(0,0);
				}
			}

			try 
			{		
				Stack rows = new Stack();	
				String line = " ";
				// load file 
				BufferedReader inReader = new BufferedReader( new FileReader(CSVFile));

				try 
				{		
					//move to the first line
					for(int lineNo = 0; lineNo < startLine; lineNo++)
					{
						inReader.readLine();	    	
					}
	
					int count = 0;
					while ( line != null)
					{
						line= inReader.readLine();	    	
						
						if(line != null)
						{
							line = line.substring(startColumn, line.length());
							double[] row = processLine(line);
							rows.push(row);
							count++;
						}
					}
					
					double[][] values = new double[count][];
					for(int yy = count - 1; yy >=0; yy--)
					{
						Object temp = rows.pop();
						if(temp != null)
							values[yy] = ((double[])temp);
					}
	
					result = new DoubleNumberToken(values);				
				}
				catch(Exception e)
				{
					ErrorLogger.debugLine("CSVRead: load function exception - " + e.getMessage());
				}
				inReader.close();					
			}
			catch (Exception e)
			{
				ErrorLogger.debugLine("CSVRead: load function exception - " + e.getMessage());
			}		    			
		}
		else
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[0].getClass().getName()});
			
		return result;
	}
	
	private double[] processLine(String line)
	{
		Stack columns = new Stack();
		
		int index = 0;
		int count = 0;
		
		do
		{
			index = line.indexOf(",");
			if(index > -1)
			{
				String temp = line.substring(0, index);
				if(!(temp.equals("")))
				{
					double val = Double.parseDouble(temp);
					columns.push(new DoubleNumberToken(val));
					count++;
				}
				line = line.substring(index + 1);
			}
		}while(index > -1);

ErrorLogger.debugLine("0");
		double val = Double.parseDouble(line);
		columns.push(new DoubleNumberToken(val));
ErrorLogger.debugLine("1");
		count++;
		
		double[] values = new double[count];
ErrorLogger.debugLine("2");
		for(int xx = count - 1; xx >= 0; xx--)
		{
ErrorLogger.debugLine("3");
			Object temp = columns.pop();
			if(temp != null)
			{
				val = ((DoubleNumberToken)temp).getValueRe();
				values[xx] = val;
ErrorLogger.debugLine("4");
			}
			else
				values[xx] = 0;			
		} 
		
		return values;
	}
}

/*
@GROUP
IO
@SYNTAX
matrix=csvread(filename, startrow, endrow)
@DOC
Reads in a matrix from a comma seperated value file.
@EXAMPLES
<programlisting>
cvsreac("testfile.csv", 0, 0)=[1,2;3,4]
</programlisting>
@SEE
csvwrite, urlread, dir, cd
*/


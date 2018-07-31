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
import com.addi.core.tokens.numbertokens.Int16NumberToken;
import com.addi.core.tokens.numbertokens.Int32NumberToken;

/**An external function for opening a file*/
public class fopen extends ExternalFunction
{
	/**                                                          
       @param operands[0] = string filename    
       @param operands[1] = string permissions
       @return integer number of the file handle*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		BufferedWriter outWriter = null;
		BufferedReader inReader = null;

		String fileName = "";	
		String permissions = "";
		Int32NumberToken result = new Int32NumberToken(-1,0);
	
        if (getNArgIn(operands) < 1)
			throwMathLibException("fopen: number of arguments must be > 0");
        
        if (getNArgIn(operands) > 2)
			throwMathLibException("fopen: number of arguments must be < 3");

		if(operands[0] instanceof CharToken)
		{
			fileName = ((CharToken)operands[0]).getElementString(0);
			if (fileName.startsWith("/")) {
				fileName = fileName;
			} else {
				fileName = globals.getWorkingDirectory().getAbsolutePath() + "/" + fileName;
			}
			permissions = "";
			
			if (getNArgIn(operands) == 2) {
				if(operands[1] instanceof CharToken) {
					permissions = ((CharToken)operands[1]).getElementString(0);
					if (permissions.compareTo("w") == 0) {
						try {
							outWriter = new BufferedWriter(new FileWriter(fileName));
						} catch (IOException e) {
							throwMathLibException("fopen: file does not exist or cannot write to file");
						}
					} else if (permissions.compareTo("r") == 0) {
						try {
							inReader = new BufferedReader(new FileReader(fileName));
						} catch (FileNotFoundException e) {
							throwMathLibException("fopen: file does not exist or cannot read from file");
						}
					} else {
						throwMathLibException("fopen: only supports modes of 'w' or 'r' currently");
					}
				} else {
					Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[1].getClass().getName()});		
				}
			} else {
				permissions = "r";
				try {
					inReader = new BufferedReader(new FileReader(fileName));
				} catch (FileNotFoundException e) {
					throwMathLibException("fopen: file does not exist or cannot read from file");
				}
			}
		} else {
			Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"CharToken", operands[0].getClass().getName()});		
		}
		result.setValue(0, GlobalValues.nextFileNum, 0);
		GlobalValues.fileNames.put(GlobalValues.nextFileNum, fileName);
		GlobalValues.filePermissions.put(GlobalValues.nextFileNum, permissions);
		GlobalValues.fileWriters.put(GlobalValues.nextFileNum, outWriter);
		GlobalValues.fileReaders.put(GlobalValues.nextFileNum, inReader);
		GlobalValues.nextFileNum++;
		
		return result;
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


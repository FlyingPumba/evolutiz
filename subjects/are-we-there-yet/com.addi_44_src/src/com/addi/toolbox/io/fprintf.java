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

import java.io.BufferedWriter;
import java.io.IOException;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;
import com.addi.core.tokens.numbertokens.Int32NumberToken;


/**An external function for changing strings into numbers */
public class fprintf extends ExternalFunction
{
    // format string
    String formatS;
    //  convert string to array of bytes
    String retString;
    // position
    int pos;
    // end of format string
    boolean EOL;
    // tokens
    Token[] tok;
    // nTok
    int nTok;
    
    /**returns a matrix of numbers 
    * @param operands[0] = string (e.g. ["hello"]) 
    * @return a matrix of numbers                                */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {

    	BufferedWriter outWriter = null;
    	int  formatOperand;
    	
        formatS = "";
        retString = "";
        pos = -1;
        EOL = false;
        nTok=-1;
    	
        // one operand 
        if (getNArgIn(operands) < 1) {
            throwMathLibException("fprintf: number of input arguments must be 1 or more");
        }
            
        if ((operands[0] instanceof Int32NumberToken)) {
        	int fileHandle = ((Int32NumberToken)operands[0]).getValueRe();
        	if (GlobalValues.fileWriters.containsKey(fileHandle)) {
        		outWriter = GlobalValues.fileWriters.get(fileHandle);
        	} else {
        		throwMathLibException("fprintf: no matching file handle");
        	}
        	if ( !(operands[1] instanceof CharToken)) {
        		throwMathLibException("fprintf: format must be a string");
        	}
        	formatOperand = 1;
        } else if(operands[0] instanceof CharToken) {
        	formatOperand = 0;
        } else {
        	throwMathLibException("fprintf: format must be a string");
        	formatOperand = 0;
        }
        
        // get format string
        formatS = ((CharToken)operands[formatOperand]).getValue();

        tok = new Token[operands.length-1-formatOperand];
        for (int i=0; i < tok.length; i++)
            tok[i] = operands[formatOperand+i+1];
        
        // possible formats
        // %[Flags][width].[toleranz]typ
        // +, - 
        // d
        // i
        // u
        // o
        // x, X
        // f
        // e, E
        // a, A
        // g, G
        // c
        // s
        // N
        // P
        // %
        
        // convert array of byte to array of double
        while (EOL == false)
        {
            char c = getNextChar();
            switch(c)
            {
                case '%':
                {
                    parseFormat();
                    break;
                }
                default:
                {
                    retString= retString + c;
                    ErrorLogger.debugLine("fprintf: "+retString);
                }
            }
        } // end while
        
        if (outWriter == null) {
        	return new CharToken( retString );
        } else {
        	try {
				outWriter.write(retString);
			} catch (IOException e) {
				throwMathLibException("fprintf: unable to write to file");
			}
        	return null;
        }

    } // end eval
    
    
    private void parseFormat()
    {
        while(!EOL)
        {
            char c = getNextChar();
            switch (c)
            {
                case '0': case '1': case '2': case '3':
                case '4': case '5': case '6': case '7':
                case '8': case '9': 
                {
                    ErrorLogger.debugLine("fprintf: Feature not implemented yet");
                    break;
                }
                case '.':
                {
                    ErrorLogger.debugLine("fprintf: Feature not implemented yet");
                    break;
                }
                case '+':
                {
                    ErrorLogger.debugLine("fprintf: Feature not implemented yet");
                    break;
                }
                case '-':
                {
                    ErrorLogger.debugLine("fprintf: Feature not implemented yet");
                    break;
                }
                case '#':
                {
                    ErrorLogger.debugLine("fprintf: Feature not implemented yet");
                    break;
                }
                case '%':
                {
                    retString= retString + c;
                    return;
                }
                case 'd': case'i': case 'u': case 'f':
                case 'e': case'E': case 'g': case 'G':
                {
                    nTok++;
                    retString = retString + tok[nTok].toString();
                    return;
                }
                case 's':
                {
                    nTok++;
                    if (tok[nTok] instanceof CharToken) {
                    	retString= retString + ((CharToken)tok[nTok]).getElementString(0);
                    } else {
                    	retString= retString + tok[nTok].toString();
                    }
                    return;
                }
                default:
                {
                    ErrorLogger.debugLine("fprintf: Feature not implemented yet");
                }
        
            }
        } // end while
    }
    
    private char getNextChar()
    {
        if (pos<(formatS.length()-1))
        {
            pos++;
            if (pos == (formatS.length()-1))
               EOL = true;
            return  formatS.charAt(pos);
        }
        return ' ';
    }
    
    private char inspectNextChar()
    {
        if (pos < (formatS.length()-2))
            return formatS.charAt(pos+1);
        else
            return ' ';
    }
}


/*
@GROUP
char
@SYNTAX
number = fprintf( formatString, arg0, arg1, arg2, ... )
@DOC
Convert write string to file
@EXAMPLES
CCX
@NOTES
.
@SEE
num2str
*/
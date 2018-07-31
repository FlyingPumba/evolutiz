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

package com.addi.toolbox.string;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**An external function for changing strings into numbers */
public class sprintf extends ExternalFunction
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
    	
        formatS = "";
        retString = "";
        pos = -1;
        EOL = false;
        nTok=-1;

        // one operand 
        if (getNArgIn(operands)<1)
            throwMathLibException("sprintf: number of input arguments <1");

        if ( !(operands[0] instanceof CharToken))
            throwMathLibException("sprintf: format must be a string");

        // get format string
        formatS = ((CharToken)operands[0]).getValue();
        
        if (getNArgIn(operands)==1)
        	return new CharToken( formatS );

        tok = new Token[operands.length-1];
        for (int i=0; i< (operands.length-1); i++)
            tok[i]= operands[i+1];
        
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
                    ErrorLogger.debugLine("sprintf: "+retString);
                }
            }
        } // end while

        return new CharToken( retString );

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
                    ErrorLogger.debugLine("sprintf: Feature not implemented yet");
                    break;
                }
                case '.':
                {
                    ErrorLogger.debugLine("sprintf: Feature not implemented yet");
                    break;
                }
                case '+':
                {
                    ErrorLogger.debugLine("sprintf: Feature not implemented yet");
                    break;
                }
                case '-':
                {
                    ErrorLogger.debugLine("sprintf: Feature not implemented yet");
                    break;
                }
                case '#':
                {
                    ErrorLogger.debugLine("sprintf: Feature not implemented yet");
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
                    retString= retString + tok[nTok].toString();
                    return;
                }
                default:
                {
                    ErrorLogger.debugLine("sprintf: Feature not implemented yet");
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
number = sprintf( formatString, arg0, arg1, arg2, ... )
@DOC
Convert strings into numbers
@EXAMPLES
str2num("hello 12")  returns [104, 101, 108, 108, 111, 32, 49, 50]
@NOTES
.
@SEE
num2str
*/
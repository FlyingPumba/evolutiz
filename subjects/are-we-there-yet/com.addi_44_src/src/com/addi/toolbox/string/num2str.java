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
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.*;


/**An external function for changing numbers into strings*/
public class num2str extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
        // one operand 
        if (getNArgIn(operands)!=1)
            throwMathLibException("num2str: number of input arguments != 1");

        if ( !(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("num2str: works only on numbers");
        
        double[][]  x = ((DoubleNumberToken)operands[0]).getReValues();
        
        if (x.length == 1 && x[0].length == 1) {
          String data = Double.toString(x[0][0]);
        
          //Cull the decimal point if the number is whole.
          if (data.endsWith(".0"))
            data = data.substring(0, data.length()-2);                   
             
          return new CharToken(data);            
        } else {
            String[][] strData = new String[x.length][x[0].length];
            int[] maxColLength = new int[x[0].length];
            boolean containsFractional = false;
            for (int row=0;row<x.length;row++) {
                for (int col=0;col<x[row].length;col++) {
                    String str = Double.toString(x[row][col]);                    
                    //Cull the decimal point if the number is whole.
                    if (str.endsWith(".0"))
                      str = str.substring(0, str.length()-2);
                    else if (str.indexOf('.') != -1)
                            containsFractional = true;
                    
                    strData[row][col] = str;
                    if (str.length() > maxColLength[col])
                        maxColLength[col] = str.length();
                }
            }
            
            if (x.length == 1 && !containsFractional) {
              StringBuffer b = new StringBuffer();
              for (int col=0;col<x[0].length;col++) {
                  b.append(strData[0][col]);
              }
              return new CharToken(b.toString());
            } else {
              String[] strRows = new String[x.length];  
              for (int row=0;row<x.length;row++) {
                StringBuffer b = new StringBuffer();
                for (int col=0;col<x[row].length;col++) {
                    b.append(strData[row][col]);
                    
                    //TODO use maxColLength to format each row correctly
                    if (col < (x[row].length-1))
                        b.append(' ');
                }
                strRows[row] = b.toString();
              }
              return new CharToken(strRows);
            }
	}
        }
}

/*
@GROUP
char
@SYNTAX
string = num2str(number)
@DOC
Converts a number to a string.
@NOTES
@EXAMPLES
num2str([104, 101]) = ["104" "101"]
@SEE
str2num
*/


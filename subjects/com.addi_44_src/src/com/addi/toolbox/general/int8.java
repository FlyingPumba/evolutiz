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

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.*;


public class int8 extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1 )
			throwMathLibException("int8: number of arguments !=1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("int: only works on numbers");

        DoubleNumberToken num = (DoubleNumberToken)operands[0];
        
        int[] size = num.getSize();
        
        int n = num.getNumberOfElements();
        
        Int8NumberToken int8 = new Int8NumberToken(size, null, null);

        double re  = 0;
        double im  = 0;
        byte   reI = 0;
        byte   imI = 0;
        for (int i=0; i<n; i++)
        {

            re = num.getValueRe(i);
            im = num.getValueIm(i);
                
            if (re>127)
                reI = 127;
            else if (re<-128)
                reI = -128;
            else
                reI = (byte)re;

            if (im>127)
                imI = 127;
            else if (im<-128)
                imI = -128;
            else
                imI = (byte)im;

            int8.setValue(i, reI, imI);
            
        }
        
        return int8;
        
	} // end eval
}

/*
@GROUP
general
@SYNTAX
int8(x)
@DOC
converts a double array into an array of int8 (range -128 up to +127)
@EXAMPLES
<programlisting>

</programlisting>
@SEE
double, int16, uint8, uint16, uint32
*/

/*
%!@testcase
%!  ml.executeExpression("a=int8(88);");
%!  ml.executeExpression("b=class(a);");
%!  assertEquals( "int8", ml.getString("b"));
%!
*/

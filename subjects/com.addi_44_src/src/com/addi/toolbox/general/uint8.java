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


public class uint8 extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1 )
			throwMathLibException("uint8: number of arguments !=1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("uint8: only works on numbers");

        DoubleNumberToken num = (DoubleNumberToken)operands[0];
        
        int[] size = num.getSize();
        
        int n = num.getNumberOfElements();
        
        UInt8NumberToken uint8 = new UInt8NumberToken(size, null, null);

        double re  = 0;
        double im  = 0;
        short   reI = 0;
        short   imI = 0;
        for (int i=0; i<n; i++)
        {

            re = num.getValueRe(i);
            im = num.getValueIm(i);
                
            if (re>255)
                reI = 255;
            else if (re<0)
                reI = 0;
            else
                reI = (short)re;

            if (im>255)
                imI = 255;
            else if (im<0)
                imI = 0;
            else
                imI = (short)im;

            uint8.setValue(i, reI, imI);
            
        }
        
        return uint8;
        
	} // end eval
}

/*
@GROUP
general
@SYNTAX
uint8(x)
@DOC
converts a double array into an array of uint8 (range 0 up to +255)
@EXAMPLES
<programlisting>

</programlisting>
@SEE
double, int16, int8, uint16, uint32
*/

/*
%!@testcase
%!  ml.executeExpression("a=uint16(88);");
%!  ml.executeExpression("b=class(a);");
%!  assertEquals( "uint16", ml.getString("b"));
%!
*/

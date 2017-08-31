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


public class uint16 extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1 )
			throwMathLibException("uint16: number of arguments !=1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("uint16: only works on numbers");

        DoubleNumberToken num = (DoubleNumberToken)operands[0];
        
        int[] size = num.getSize();
        
        int n = num.getNumberOfElements();
        
        UInt16NumberToken uint16 = new UInt16NumberToken(size, null, null);

        double re  = 0;
        double im  = 0;
        int    reI = 0;
        int    imI = 0;
        for (int i=0; i<n; i++)
        {

            re = num.getValueRe(i);
            im = num.getValueIm(i);
                
            if (re>65535)
                reI = 65535;
            else if (re<0)
                reI = 0;
            else
                reI = (int)re;

            if (im>65535)
                imI = 65535;
            else if (im<0)
                imI = 0;
            else
                imI = (int)im;

            uint16.setValue(i, reI, imI);
            
        }
        
        return uint16;
        
	} // end eval
}

/*
@GROUP
general
@SYNTAX
uint8(x)
@DOC
converts a double array into an array of uint16 
@EXAMPLES
<programlisting>

</programlisting>
@SEE
double, int16, int8, uint16
*/

/*
%!@testcase
%!  ml.executeExpression("a=uint16(88);");
%!  ml.executeExpression("b=class(a);");
%!  assertEquals( "uint16", ml.getString("b"));
%!
*/


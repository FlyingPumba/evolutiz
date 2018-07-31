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

package com.addi.toolbox.elfun;

import com.addi.core.functions.ExternalElementWiseFunction;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


public class tanh extends ExternalElementWiseFunction
{
    
    public tanh()
    {
        name = "tanh";
    }
    
    /**Calculates the hyperbolic tangent of a complex number
     * @param arg = the angle as an array of double
     * @return the result as an array of double
     */ 
    public double[] evaluateValue(double[] arg)
    {
        sinh sinhF = new sinh();
        cosh coshF = new cosh();
        
        double[] temp1 = sinhF.evaluateValue(arg);
        double[] temp2 = coshF.evaluateValue(arg);
        
        DoubleNumberToken  num = new DoubleNumberToken();
        
        return num.divide(temp1, temp2);
    }
    
}

/*
@GROUP
trigonometric
@SYNTAX
tanh(angle)
@DOC
Returns the hyperbolic tangent of angle.
@EXAMPLES
<programlisting>
tanh(0) = 0
tanh(1) = 0.76159
</programlisting>
@SEE
tan, atanh, cos, sin
*/


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
import com.addi.toolbox.jmathlib.matrix.log;


public class atanh extends ExternalElementWiseFunction
{
    
    public atanh()
    {
        name = "atanh";
    }
    
    /**Calculates the inverse hyperbolic tangent of a complex number
     * @param arg = the angle as an array of double
     * @return the result as an array of double
     */ 
    public double[] evaluateValue(double[] arg)
    {
        double result[] = new double[2];
        //  atanh(z)  =  1/2 * log( (1+z)/(1-z) )

        // _1:      one.Minus(z) ...
        double[] temp = new double[2];
        temp[REAL] = 1 - arg[REAL];
        temp[IMAG] = - arg[IMAG];

        // result:  one.Plus(z) ...
        result[REAL] = 1 + arg[REAL];

        // result:  result.Div(_1) ...
        DoubleNumberToken num = new DoubleNumberToken();
        result = num.divide(result, temp);

        // _1:      result.log() ...
        log logFunc = new log();
        result = logFunc.evaluateValue(result);

        // result:  _1.scale(0.5) ...
        result[REAL]  =  0.5 * result[REAL];
        result[IMAG]  =  0.5 * result[IMAG];
        return result;  
    }

}

/*
@GROUP
trigonometric
@SYNTAX
atanh(value)
@DOC
Returns the arc hyperbolic tangent of the first value.
@EXAMPLES
<programlisting>
atanh(1) = NaN
atanh(0) = 0
</programlisting>
@SEE
tan, tanh
*/


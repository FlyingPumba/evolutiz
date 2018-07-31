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
import com.addi.toolbox.jmathlib.matrix.log;
import com.addi.toolbox.jmathlib.matrix.sqrt;


public class acos extends ExternalElementWiseFunction
{
    
    public acos()
    {
        name = "acos";
    }
    
    /**Calculates the arccosine of a complex number
     * @param arg = the value as an array of double
     * @return the result as an array of double
     */ 
    public double[] evaluateValue(double[] arg)
    {
        double result[] = new double[2];
        double _re1, _im1;

        double re =  arg[REAL];
        double im =  arg[IMAG];
               
        // _1:      one - z^2 ...
        result[REAL]  =  1.0 - ( (re*re) - (im*im) );
        result[IMAG]  =  0.0 - ( (re*im) + (im*re) );

        // result:  _1.Sqrt() ...
        sqrt sqrtFunc = new sqrt();
        result = sqrtFunc.evaluateValue(result);

        // _1:      i * result ...
        _re1 =  - result[IMAG];
        _im1 =  + result[REAL];

        // result:  z +_1  ...
        result[REAL]  =  re + _re1;
        result[IMAG]  =  im + _im1;

        // _1:      result.log()
        log logFunc = new log();
        result = logFunc.evaluateValue(result);

        // result:  -i * _1 ...
        double temp  = result[IMAG];
        result[IMAG] = -result[REAL];
        result[REAL] = temp;
        
        return result;
    }

}

/*
@GROUP
trigonometric
@SYNTAX
angle = acos(value)
@DOC
Returns the arc cosine of value.
@EXAMPLES
<programlisting>
acos(1) = 0
acos(0) = 1.5707963267948966
</programlisting>
@SEE
cos, acosh, sin, asin, asinh
*/


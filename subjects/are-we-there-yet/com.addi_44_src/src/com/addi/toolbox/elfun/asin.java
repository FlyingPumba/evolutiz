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


public class asin extends ExternalElementWiseFunction
{
    
    public asin()
    {
        name = "asin";
    }
    
    /**Calculates the arcsine of a complex number
     *  @param arg = the value as an array of double
     *  @return the result as an array of double
     */ 
    public double[] evaluateValue(double[] arg)
    {
        double result[] = new double[2];
        //  asin(z)  =  -i * log(i*z + Sqrt(1 - z*z))
        double re =  arg[REAL];
        double im =  arg[IMAG];
        
        // _1:      one.Minus(z.Times(z)) ...
        result[REAL]       =  1.0 - ( (re*re) - (im*im) );
        result[IMAG]  =  0.0 - ( (re*im) + (im*re) );

        // result:  _1.Sqrt() ...
        sqrt s= new sqrt();
        result = s.evaluateValue(result);

        // _1:      z.Times(i) ...
        // result:  _1.Plus(result) ...
        result[REAL]       =   result[REAL] - im;
        result[IMAG]  = result[IMAG] +  re;

        // _1:      result.log() ...
        log logFunc = new log();
        result = logFunc.evaluateValue(result);

        double temp     = result[IMAG];
        result[IMAG]  =  -result[REAL];
        result[REAL]       =  temp;

        return result;
    }
}

/*
@GROUP
trigonometric
@SYNTAX
angle=asin(value)
@DOC
Returns the arc sine of the first operand.
@EXAMPLES
<programlisting>
asin(1) = 1.5707963267948966
asin(0) = 0
</programlisting>
@SEE
sin, asinh, cos, acos, acosh
*/


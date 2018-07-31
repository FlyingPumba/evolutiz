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


public class asinh extends ExternalElementWiseFunction
{
    
    public asinh()
    {
        name = "asinh";
    }
    
    /**Calculates the inverse hyperbolic sine of a complex number
    @param arg = the value as an array of double
    @return the result as an array of double*/ 
    public double[] evaluateValue(double[] arg)
    {

         double result[] = new double[2];
         //  asinh(z)  =  log(z + Sqrt(z*z + 1))
         double re = arg[REAL];
         double im = arg[IMAG];
         // _1:      z.Times(z).Plus(one) ...
         result[REAL] =  ( (re*re) - (im*im) ) + 1.0;
         result[IMAG] =  ( (re*im) + (im*re) );

         // result:  _1.Sqrt() ...
         sqrt sqrtFunc = new sqrt();
         result = sqrtFunc.evaluateValue(result);

         // result:  z.Plus(result) ...
         result[REAL] =  re + result[REAL];                                       // !
         result[IMAG] =  im + result[IMAG];                                       // !

         // _1:      result.log() ...
         log logFunc = new log();
         result = logFunc.evaluateValue(result);
                 
         return  result;
     }

}

/*
@GROUP
trigonometric
@SYNTAX
angle = asinh(value);
@DOC
Returns the arc hyperbolic sine
@EXAMPLES
<programlisting>
asinh(1) = 0.8813735870195429
asinh(0) = 0
</programlisting>
@SEE
sinh, asin, cosh, acos
*/


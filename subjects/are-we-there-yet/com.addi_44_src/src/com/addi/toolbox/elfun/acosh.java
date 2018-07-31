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


public class acosh extends ExternalElementWiseFunction
{
    
    public acosh()
    {
        name = "acosh";
    }
    
    /**Calculates the inverse hyperbolic cosine of a complex number
    @param arg = the angle as an array of double
    @return the result as an array of double*/ 
    public double[] evaluateValue(double[] arg)
    {
         double result[] = new double[2];
         //  acosh(z)  =  log(z + Sqrt(z*z - 1))
         double re = arg[REAL];
         double im = arg[IMAG];

         // _1:  z.Times(z).Minus(one) ...
         result[REAL]       =  ( (re*re) - (im*im) ) - 1.0;
         result[IMAG]  =  ( (re*im) + (im*re) ) - 0.0;

         // result:  _1.Sqrt() ...
         sqrt sqrtF = new sqrt();
         result = sqrtF.evaluateValue(result);

         // result:  z.Plus(result) ...
         result[REAL] =  re + result[REAL];         // !
         result[IMAG] =  im + result[IMAG];         // !

         // _1:  result.log() ...
         log logF = new log();
         result = logF.evaluateValue(result);

         // result:  _1 ...
         return  result;
     }

}

/*
@@GROUP
trigonometric
@SYNTAX
angle = ACOS(value)
@DOC
Returns the arc cosine of value.
@EXAMPLES
ACOS(1) = 0
ACOS(0) = 1.5707963267948966
@SEE
cos, acosh
*/


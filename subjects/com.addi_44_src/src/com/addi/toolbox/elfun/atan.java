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


public class atan extends ExternalElementWiseFunction
{
    
    public atan()
    {
        name = "atan";
    }
    
    /**Calculates the arctangent of a complex number
    @param arg = the value as an array of double
    @return the result as an array of double*/ 
    public double[] evaluateValue(double[] arg)
    {
         double result[] = new double[2];
         double[] temp = new double[2];
         //  atan(z)  =  -i/2 * log( (i-z)/(i+z) )

         double _re1, _im1;

         // result:  i.Minus(z) ...
         temp[REAL] = -arg[REAL];
         temp[IMAG] = 1 - arg[IMAG];

         // _1:      i.Plus(z) ...
         result[REAL] = arg[REAL];
         result[IMAG] = 1 + arg[IMAG];

         // result:  result.Div(_1) ...
         DoubleNumberToken num = new DoubleNumberToken();
         result = num.divide(temp, result);

         // _1:      result.log() ...
         log logFunc = new log();
         result = logFunc.evaluateValue(result);

         // result:  half_i.neg().Times(_2) ...
         double t = -0.5 * result[REAL];
         result[REAL] =   0.5 * result[IMAG];
         result[IMAG] =  t;
         return  result;        
     }

}

/*
@GROUP
trigonometric
@SYNTAX
atan(angle)
@DOC
Calculates the inverse tangent of angle.
@EXAMPLES
<programlisting>
atan(1) = 0.7853981633974483
atan(0) = 0
</programlisting>
@SEE
atan2, tan
*/


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

package com.addi.toolbox.jmathlib.matrix;

import com.addi.core.functions.ExternalElementWiseFunction;

public class ln extends ExternalElementWiseFunction
{
    
    public ln()
    {
        name = "ln";
    }
    
    /**Calculates the logarithm of a complex number
    @param arg = the value as an array of double
    @return the result as an array of double*/ 
    public double[] evaluateValue(double[] arg)
    {
        double[] result = new double[2];

        double temp = Math.pow(arg[REAL], 2) + Math.pow(arg[IMAG], 2);
        temp =  Math.sqrt(temp);
     
        result[REAL] = Math.log(temp);
        result[IMAG] = Math.atan2(arg[IMAG], arg[REAL]);
        
        return  result;                      
    }

}

/*
@GROUP
matrix
@SYNTAX
answer = ln(value)
@DOC
Returns the natural logarithm of value.
@EXAMPLES
<programlisting>
ln(2) = 0.6931471805599 
ln(10) = 2.30258509299
</programlisting>
@SEE
log
*/


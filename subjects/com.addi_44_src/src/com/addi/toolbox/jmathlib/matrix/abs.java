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

public class abs extends ExternalElementWiseFunction
{
    
    public abs()
    {
        name = "abs";
    }
    
    /**Standard functions - absolute value  
     * @param double array
     * @return the result as a double array
     */
    public double[] evaluateValue(double[] arg)
    {
        double[] result = new double[2];

        if (arg[IMAG]==0)
        {
            result[REAL] = Math.abs(arg[REAL]);
            result[IMAG] = 0;
        }
        else
        {
            result[REAL] = Math.sqrt(arg[REAL]*arg[REAL] + arg[IMAG]*arg[IMAG]);
            result[IMAG] = 0;
        }
        
        return  result;                      
    }

}

/*
@GROUP
matrix
@SYNTAX
abs(value)
@DOC
Returns the absolute positive value of value.
@NOTES
@EXAMPLES
<programlisting>
abs(-5)     = 5
abs(2)      = 2
abs(3 + 4I) = 5
</programlisting>
@SEE
sign, angle
*/


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

public class exp extends ExternalElementWiseFunction
{
    
    public exp()
    {
        name = "exp";
    }
    
    /**Calculates the exponent of a complex number
    @param arg = the value as an array of double
    @return the result as an array of double*/ 
    public double[] evaluateValue(double[] arg)
    {
        double[] result = new double[2];

        double scalar   = Math.exp(arg[REAL]);         // e^ix = cis x
        
        result[REAL]    = scalar * Math.cos(arg[IMAG]);
        result[IMAG]    = scalar * Math.sin(arg[IMAG]);
        
        return  result;                      
    }

}

/*
@GROUP
general
@SYNTAX
exp(value)
@DOC
.
@EXAMPLES
exp(-5.5) = -5
exp(2.3)   = 3
@SEE
log, ln
*/


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

public class round extends ExternalElementWiseFunction
{
    
    public round()
    {
        name = "round";
    }
    
    /**Standard functions - rounds the value to the nearest integer 
    @return the result as an OperandToken*/
    public double[] evaluateValue(double[] arg)
    {
        double[] result = new double[2];

        if (arg[REAL]>=0)
            result[REAL] = Math.floor(arg[REAL] + 0.5);
        else
            result[REAL] = Math.ceil(arg[REAL] - 0.5);
            
        if (arg[IMAG]>=0)
            result[IMAG] = Math.floor(arg[IMAG] + 0.5);
        else
            result[IMAG] = Math.ceil(arg[IMAG] - 0.5);

        return  result;                      
    }

}

/*
@GROUP
general
@SYNTAX
answer = round(value)
@DOC
Rounds a value to the nearest integer.
@EXAMPLES
round(2.2) = 2
round(5.5) = 6
@SEE
ceil, floor
*/


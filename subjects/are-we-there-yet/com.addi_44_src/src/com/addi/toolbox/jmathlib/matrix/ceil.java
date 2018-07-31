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

public class ceil extends ExternalElementWiseFunction
{
    
    public ceil()
    {
        name = "ceil";
    }
    
    /**Standard functions - rounds the value up   
    @return the result as a double array
    */
    public double[] evaluateValue(double[] arg)
    {
        double[] result = new double[2];

        result[REAL] = Math.ceil(arg[REAL]);
        result[IMAG] = Math.ceil(arg[IMAG]);
        
        return  result;                      
    }

}

/*
@GROUP
@GROUP
general
@SYNTAX
ceil(value)
@DOC
Rounds the value of the first operand up to the nearest integer
@EXAMPLES
ceil(-5.5) = -5
ceil(2.3)   = 3
@SEE
floor, round
*/


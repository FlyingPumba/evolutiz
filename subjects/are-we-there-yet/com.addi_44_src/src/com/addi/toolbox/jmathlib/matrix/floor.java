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

public class floor extends ExternalElementWiseFunction
{
    
    public floor()
    {
        name = "floor";
    }
    
    /**Standard functions - rounds the value down  
    @param  double array
    @return the result as an OperandToken
    */
    public double[] evaluateValue(double[] arg)
    {
        double[] result = new double[2];

        result[REAL] = Math.floor(arg[REAL]);
        result[IMAG] = Math.floor(arg[IMAG]);
        
        return  result;                      
    }

}

/*
@GROUP
matrix
@SYNTAX
floor(value)
@DOC
Rounds the value of the first operand down to the nearest integer.
@EXAMPLES
floor(-5.5) = -6
floor(2.3)  = 2
@SEE
ceil, round
*/


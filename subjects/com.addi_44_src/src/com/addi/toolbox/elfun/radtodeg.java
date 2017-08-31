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

public class radtodeg extends ExternalElementWiseFunction
{
    
    public radtodeg()
    {
        name = "radtodeg";
    }
    
    /**converts value from radians to degrees
    @param operands[0] = value to convert
    @return the converted value*/
    public double[] evaluateValue(double[] arg)
    {
        double[] result = new double[2];
        
        result[REAL] = arg[REAL]  * 180 / Math.PI;
        
        return result;
    }

}

/*
@GROUP
trigonometric
@SYNTAX
degrees = radtodeg(radians)
@DOC
converts the angle from radians to degrees.
@NOTES
@EXAMPLES
radtodeg(3.141592653589793)  = 180
radtodeg(1.5707963267948966) = 90
@SEE
gradtodeg, degtograd, radtograd, degtorad, radtodeg
*/


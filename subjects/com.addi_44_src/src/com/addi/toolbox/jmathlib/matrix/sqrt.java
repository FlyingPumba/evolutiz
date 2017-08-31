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

public class sqrt extends ExternalElementWiseFunction
{
    
    public sqrt()
    {
        name = "sqrt";
    }
    
    /**Calculates the sqrt of a complex number
    @param arg = the value as an array of double
    @return the result as an array of double*/ 
    public double[] evaluateValue(double[] arg)
    {
        
        // with thanks to Jim Shapiro <jnshapi@argo.ecte.uswc.uswest.com>
        // adapted from "Numerical Recipies in C" (ISBN 0-521-43108-5)
        // by William H. Press et al

        double[] result = new double[2];
        double   re     = arg[REAL];
        double   im     = arg[IMAG];

        double temp = Math.pow(re, 2) + Math.pow(im, 2);
        double mag  = Math.sqrt(temp);

        if (mag > 0.0) 
        {
            if (re > 0.0) 
            {
                temp =  Math.sqrt(0.5 * (mag + re));

                re =  temp;
                im =  0.5 * im / temp;
            } 
            else 
            {
                temp =  Math.sqrt(0.5 * (mag - re));

                if (im < 0.0) 
                {
                    temp =  -temp;
                }

                re =  0.5 * im / temp;
                im =  temp;
            }
        } 
        else 
        {
            re =  0.0;
            im =  0.0;
        }
        result[REAL] = re;
        result[IMAG] = im;
        
        return result;
    }


}

/*
@GROUP
general
@SYNTAX
answer = sqrt(value)
@DOC
Returns the sqrt of a value.
@EXAMPLES
sqrt(4) = 2
sqrt(9) = 3
@NOTES
@SEE
angle, abs
*/


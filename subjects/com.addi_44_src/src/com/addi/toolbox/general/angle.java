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

package com.addi.toolbox.general;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.*;


public class angle extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1 )
			throwMathLibException("angle: number of arguments !=1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("angle: only works on numbers");

		// get data from arguments
        double[][] a_r     = ((DoubleNumberToken)operands[0]).getValuesRe();
		double[][] a_i     = ((DoubleNumberToken)operands[0]).getValuesIm();
		int        dy      = ((DoubleNumberToken)operands[0]).getSizeY();
        int        dx      = ((DoubleNumberToken)operands[0]).getSizeX();
        double[][] ret     = new double[dy][dx];

        for (int y=0; y<dy ; y++)
        {
            for (int x=0; x<dx ; x++)
            {
                if ((a_r[y][x]<0) && (a_i[y][x]>=0))
                {
                    ret[y][x] = Math.atan( a_i[y][x] / a_r[y][x]) + Math.PI;
                }
                else if ((a_r[y][x]<0) && (a_i[y][x]<0))
                {
                    ret[y][x] = Math.atan( a_i[y][x] / a_r[y][x]) - Math.PI;
                }
                else
                {
                    ret[y][x] = Math.atan( a_i[y][x] / a_r[y][x]);
                }
            }
        }   

        return new DoubleNumberToken(ret);
        
	} // end eval
}

/*
@GROUP
general
@SYNTAX
angle(complex)
@DOC
Returns the angle of a complex number
@EXAMPLES
<programlisting>
angle(2i) = pi/2
real(1 + i) = pi/4
</programlisting>
@SEE
real, conj, imag, abs
*/


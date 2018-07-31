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

package com.addi.toolbox.specfun;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;
import com.addi.toolbox.specfun._private.*;


public class gammaln extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1)
			throwMathLibException("gammaln: number of arguments != 1");

		if (!(operands[0] instanceof DoubleNumberToken)) 
            throwMathLibException("gammaln: argument must be a number");

        double[][] x = ((DoubleNumberToken)operands[0]).getReValues();

        int dy     = ((DoubleNumberToken)operands[0]).getSizeY();
        int dx     = ((DoubleNumberToken)operands[0]).getSizeX();   

        double[][] gammaln = new double[dy][dx]; 
        
        for (int xi=0; xi<dx ; xi++)
        {
            for (int yi=0; yi<dy ; yi++)
            {
                gammaln[yi][xi] = Gamma.logGamma(x[yi][xi]);
            }
        }
        
		return new DoubleNumberToken(gammaln);		

	} // end eval
}

/*
@GROUP
specfun
@SYNTAX
gammaln(x)
@DOC
return the log of the gamma function
@EXAMPLES
<programlisting>
gammaln(3.5)
gammaln([2,3,4;5,6,7])
</programlisting>
@NOTES
@SEE
beta
*/


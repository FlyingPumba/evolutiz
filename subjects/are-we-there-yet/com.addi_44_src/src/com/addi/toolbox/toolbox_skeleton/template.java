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

package com.addi.toolbox.toolbox_skeleton;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;



/**An external function for computing a mesh of a matrix  */
public class template extends ExternalFunction
{
	/**returns two  matrices 
	* @param operands[0] = x values (e.g. [-2:0.2:2]) 
	* @param operands[1] = y values (e.g. [-2:0.2:2])
	* @return [X,Y] as matrices                                 */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// one operand (e.g. [x,y]=template([-2:0.2:2],[-2:0.2:2]) )
		if (getNArgIn(operands)!=2)
			throwMathLibException("template: number of input arguments != 2");

		// Check number of return arguments
		if (getNoOfLeftHandArguments()!=2)
		    throwMathLibException("template: number of output arguments != 2");

		if ( !(operands[0] instanceof DoubleNumberToken) ||
             !(operands[0] instanceof DoubleNumberToken)   )
			throwMathLibException("template: works only on numbers");

		// get data from arguments
		double[][] x =  ((DoubleNumberToken)operands[0]).getReValues();
		double[][] y =  ((DoubleNumberToken)operands[1]).getReValues();

		if ((x.length != 1) ||
            (y.length != 1)    )
            throwMathLibException("template: works only row vectors");

		int sizeX = x[0].length;
        int sizeY = y[0].length;
        
        double[][] X = new double[sizeY][sizeX];
        double[][] Y = new double[sizeY][sizeX];
        
        for (int i=0; i<sizeY; i++)
        {
        	for (int j=0; j<sizeX; j++)
            {
            	X[i][j] = x[0][j];
                Y[i][j] = y[0][i];
            }
        } 

  		OperandToken values[][] = new OperandToken[1][2];
		values[0][0] = new DoubleNumberToken(X);
		values[0][1] = new DoubleNumberToken(Y);
		return new MatrixToken( values );

	} // end eval
}


/*
@GROUP
General
@SYNTAX
answer = template (value)
@DOC
Returns the sign of value.
@EXAMPLES
<programlisting>
sign(-10)=-1
sign(10)=1
</programlisting>
@NOTES
This functions is used as a template for developing toolbox functions.
@SEE
template
*/

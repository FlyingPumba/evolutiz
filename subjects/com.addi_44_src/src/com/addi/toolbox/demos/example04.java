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

package com.addi.toolbox.demos;

import com.addi.core.functions.*;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An example of an external function - it returns 2 * the first parameter*/
public class example04 extends ExternalFunction
{
	/**Execute the function returning the first parameter
	operands - array of parameters*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		ErrorLogger.debugLine("example04 right-hand arguments= "+operands.length);
		ErrorLogger.debugLine("example04 left-hand  arguments= "+getNoOfLeftHandArguments());

		if (operands[0] instanceof DoubleNumberToken)
		{
			double[][] argValues 	= ((DoubleNumberToken)operands[0]).getReValues();
			int        argSizeX 	= ((DoubleNumberToken)operands[0]).getSizeX();	
			int        argSizeY 	= ((DoubleNumberToken)operands[0]).getSizeY(); 

			ErrorLogger.debugLine("*** demo function: example04 ***");

			/* Check dimensions of matrix */
			//ErrorLogger.debugLine("DoubleNumberToken: sub (n*m) - (n*m)");
			for (int yy=0; yy<argSizeY; yy++) 
			{
				for (int xx=0; xx<argSizeX; xx++)
				{
					argValues[yy][xx] = 2 * argValues[yy][xx] ;
				}
			}
   			return new DoubleNumberToken(argValues);   	
		}

		// return two left hand arguments: [a,b]=example04();
		OperandToken values[][] = new OperandToken[1][2];
		values[0][0] = new DoubleNumberToken(11.11);
		values[0][1] = new DoubleNumberToken(22.22);
		return new MatrixToken( values );
	}
}

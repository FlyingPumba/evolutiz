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


import java.util.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.interpreter.Variable;
import com.addi.core.tokens.MathLibObject;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for the sum of all values within the matrix or strucure*/
public class sum extends ExternalFunction
{
	/**Calculate the sum of the values within a matrix or structure
	@param operands[0] = the matrix or structure to sum*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;

        // check number of arguments
        if ((getNArgIn(operands) < 1) ||
            (getNArgIn(operands) > 2)    )
            throwMathLibException("sum: number of input arguments <1 or >2");

        // check token types
		if(!(operands[0] instanceof DoubleNumberToken) && 
           !(operands[0] instanceof MathLibObject))
			com.addi.core.interpreter.Errors.throwMathLibException(ERR_INVALID_PARAMETER, new Object[] {"DoubleNumberToken or Structure", operands[0].getClass().getName()});

        // checking for dimension argument
		// e.g. sum(...,1) or sum(...,2)  
        int dim = 0;  // vertical sums
        if (getNArgIn(operands) == 2)
        {
            if(!(operands[1] instanceof DoubleNumberToken))
                throwMathLibException("sum: dimension is of wrong type");
            
            // get dimension to sum
            dim = (int)((DoubleNumberToken)operands[1]).getValueRe();
        }
            
        if(operands[0] instanceof DoubleNumberToken)
        {
            double[][] xRe = ((DoubleNumberToken)operands[0]).getValuesRe();
            double[][] xIm = ((DoubleNumberToken)operands[0]).getValuesIm();
            int   xSizeX   = ((DoubleNumberToken)operands[0]).getSizeX();
            int   xSizeY   = ((DoubleNumberToken)operands[0]).getSizeY();

            if((xSizeY == 1) && (dim==0))
            {
                //vector
                double sumRe = 0;
                double sumIm = 0;
                for(int colno = 0; colno < xSizeX; colno++)
                {
                    sumRe += xRe[0][colno];
                    sumIm += xIm[0][colno];
                }
                result = new DoubleNumberToken(sumRe, sumIm);
            }	        
            else
            {
                //matrix
                if ((dim==0) || (dim==1))
                {
                    // sum up columns  (vertical)
                    double[][] sumRe = new double[1][xSizeX];
                    double[][] sumIm = new double[1][xSizeX];
                    
                    for(int rowno = 0; rowno < xSizeY; rowno++)
                    {
                        for(int colno = 0; colno < xSizeX; colno++)
                        {
                            sumRe[0][colno] += xRe[rowno][colno];
                            sumIm[0][colno] += xIm[rowno][colno];
                        }
                    }
                    result = new DoubleNumberToken(sumRe, sumIm);

                }                
                else
                {
                    // sum up rows (horizontal)
                    double[][] sumRe = new double[xSizeY][1];
                    double[][] sumIm = new double[xSizeY][1];
                    
                    for(int rowno = 0; rowno < xSizeY; rowno++)
                    {
                        for(int colno = 0; colno < xSizeX; colno++)
                        {
                            sumRe[rowno][0] += xRe[rowno][colno];
                            sumIm[rowno][0] += xIm[rowno][colno];
                        }
                    }
                    
                    result = new DoubleNumberToken(sumRe, sumIm);
                } // end dim
            }
        }
        else
        {
            Iterator iter = ((MathLibObject)operands[0]).getFields();
            Map.Entry first = ((Map.Entry)iter.next());
            Variable var = ((Variable)first.getValue());
            OperandToken value = ((OperandToken)var.getData());

            while(iter.hasNext())
            {
                Map.Entry next = ((Map.Entry)iter.next());
                var = ((Variable)next.getValue());
                value = value.add(((OperandToken)var.getData()));
            }                
            
            result = value;
        }
    
		
		return result;
	}
}

/*
@GROUP
matrix
@SYNTAX
answer = sum(matrix)
answer = sum(structure)
@DOC
Returns the sum of all the elements of a matrix or a structure.
@NOTES
@EXAMPLES
<programlisting>
sum([1,2;3,4]) = 10
sum([1,2,3;4,5,6]) = 21
x=struct(a, 1, b, 2, c, 3) = a = 1 : b = 2 : c = 3 :
sum x = 6
</programlisting>
@SEE
trace, diag
*/

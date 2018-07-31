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

package com.addi.core.functions;

import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**Base class for all external function classes which work element wise*/
abstract public class ExternalElementWiseFunction extends ExternalFunction
{

    /**
     *  standard function for evaluation of general external functions
     *  @param operands
     *  @param pointer to the global values (interpreter, function manager, graphics,...)
     *  @return 
     */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {

        // function works for one argument only
        if (getNArgIn(operands)!=1)
            throwMathLibException(name + " number of arguments < 1");

        // works on numbers only
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException(name + " only works on numbers");

        // get number token
        DoubleNumberToken numOp = (DoubleNumberToken)operands[0];
        
        // get dimension of number token (2dimensional, 3dim, ....)
        int[] dim = numOp.getSize();
        
        // ceate array of correct size with dimensions "dim"
        DoubleNumberToken num = new DoubleNumberToken(dim, null, null);
        
        // call element evaluation for all values inside the DoubleNumberToken
        for (int i=0; i< numOp.getNumberOfElements(); i++)
        {
            num.setValueComplex(i, evaluateValue( numOp.getValueComplex(i) ));
        }
        
        return num;
        

    } // end evaluate

    
    // all subclasses of this cluss MUST implement the method below
    //abstract public double[] evaluateValue(double[] complex);
    abstract public double[] evaluateValue(double[] complex);
    
}

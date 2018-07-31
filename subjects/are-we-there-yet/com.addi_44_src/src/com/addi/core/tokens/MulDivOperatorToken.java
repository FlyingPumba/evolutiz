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

package com.addi.core.tokens;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;



/**Class representing multiplicaton and division operations within an expression*/
public class MulDivOperatorToken extends BinaryOperatorToken
{

    /**Constructor taking the operator and priority
     * @param _operator = the actual operator
     */
    public MulDivOperatorToken (char _operator)
    {
    	/**call the super constructor, type defaults to ttoperator and operands to 2*/
        super(_operator, MULDIV_PRIORITY);
    }

    /**evaluate the operator
    @param operands = the operators operands
    @return the result as an OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        OperandToken result = null;

        OperandToken left = ((OperandToken)operands[0]);       
        if(left == null)
        	left = new DoubleNumberToken(0);
        	
        OperandToken right = ((OperandToken)operands[1]);
        if(right == null)
        	right = new DoubleNumberToken(0);

        OperandToken[] ops = {left, right}; //castOperands(left, right);			
			
	    //now evaluate op on left and right        
	    if(value == '*')
	    {
	        result = ops[0].multiply(ops[1]);
	    }
	    else if (value == '/')
	    {
	        result = ops[0].divide(ops[1]);
	    }
	    else if (value == 'm')
	    {
    		//scalar multiplication
    		result = ops[0].scalarMultiply(ops[1]);
	    }	
	    else if (value == 'd')
	    {
    		//scalar division
    		result = ops[0].scalarDivide(ops[1]);
	    }
        else if (value == 'L')
        {
            //left division
            result = ops[0].leftDivide(ops[1]);
        }
        else if (value == 'l')
        {
            //scalar left division
            result = ops[0].scalarLeftDivide(ops[1]);
        }
        else
            Errors.throwMathLibException("MulDiv: do not know operator");
        
        if(result == null)
        {
            //return origional expression
            result = new Expression(this, left, right);
        }
        
        return result;
    }

}

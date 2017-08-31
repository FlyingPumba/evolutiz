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

/**Class representing all power operators used in an expression*/
public class PowerOperatorToken extends BinaryOperatorToken
{

    /**Constructor taking the operator and priority
    @param _operator = the actual operator being created */
    public PowerOperatorToken (char _operator)
    {
    	/**call the super constructor, type defaults to ttoperator and operands to 2*/
        super(_operator, POWER_PRIORITY);
    }

    /**evaluates the operator
    @param operands = the operators operands
    @return the result as an OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        OperandToken result = null;

        OperandToken left = ((OperandToken)operands[0]); 
        
        if(left == null)
            Errors.throwMathLibException("PowerOperatorToken left null");
        	
        OperandToken right = ((OperandToken)operands[1]);
        if(right == null)
            Errors.throwMathLibException("PowerOperatorToken right null");

        //now evaluate op on left and right        
        if(value == 'm')
        {
            result = left.mPower(right);
        }
        else if (value=='p')
        {
            // e.g. 1.^[1,2,3]  or [1,2,3].^4
            result = left.power(right);
        }
        else
            Errors.throwMathLibException("PowerOperatorToken unknown power");


        return result;
    }
}

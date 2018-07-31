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

import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**Used to implement addition and subtraction operations within an expression*/
public class AddSubOperatorToken extends BinaryOperatorToken
{

    /**Constructor taking the operator and priority
     * @param _operator = the actual operator		   
     */
    public AddSubOperatorToken (char _operator)
    {
    	/**call the super constructor, type defaults to ttoperator and operands to 2*/
        super(_operator, ADDSUB_PRIORITY);
    }

    /**evaluates the operator
     * @param operands = the operators operands
     * @param globals
     * @return the result of the operation as an OperandToken
     */
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
        if(value == '+')
        {
        	result = ops[0].add(ops[1]);
        }
        else
        {
            result = ops[0].subtract(ops[1]);
        }
        
        if(result == null)
        {
            //return origional expression
            result = new Expression(this, left, right);
        }

        return result;
    }
        
}

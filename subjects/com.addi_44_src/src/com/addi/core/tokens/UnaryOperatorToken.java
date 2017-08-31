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



/**Class representing any unary operators in an expression*/
public class UnaryOperatorToken extends OperatorToken
{
    /**Constructor taking the operator and priority
     * @param _operator = the operator being constructed 
     */
    public UnaryOperatorToken(char _operator) 
    {
    	/**call the super constructor, type defaults to ttoperator and operands to 1*/
        super(UNARY_PRIORITY); 
        value = _operator;
    }

    /**evaluate the operator
    @param operands = the operator operands
    @return the result as an OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        OperandToken result = null;

		OperandToken operand = ((OperandToken)operands[0]);

		//now evaluate op on left
     	switch(value)
    	{
        	case '!':
        	{
           	 	result = operand.factorial();
        		break;
        	}
        	case '\'':
        	{
           	 	result = operand.transpose();
        		break;
        	}
            case 't':
            {
                result = operand.ctranspose();
                break;
            }
        	case '-':
        	{
                // -- operator
        	    // e.g. "a--" first return "a" then decrement by 1

                // check if operand is a variable (e.g. a--, bar--)
                if(operand instanceof VariableToken)
                {
                    // first: evaluate and return original value variable
                    result = operand.evaluate(null, globals);
                    
                    // second: decrease variable
                    OperandToken op = result.subtract(new DoubleNumberToken(1));
    
                    // save new variable value
                    String variable = ((VariableToken)operand).getName();
                    //getVariables().getVariable(variable).assign(op);
                    globals.getVariable(variable).assign(op);
                    
                    return result;
                }
                else if(operand instanceof NumberToken)
                {
                    // 4--
                    result = operand.subtract(new DoubleNumberToken(1));
                }
                else
                    Errors.throwMathLibException("UnaryOperatorToken --");

        		break;
        	}
        	case '+':
        	{
                // ++ operator
                // e.g. "a++" first return "a" then increment by 1


                // check if operand is a variable (e.g. a++, bar++)
                if(operand instanceof VariableToken)
                {    
                    // first: evaluate and return original value variable
            		result = operand.evaluate(null, globals);
            		
            		// second: increase variable
                    OperandToken op = result.add(new DoubleNumberToken(1));
    
                    // save new variable value
                    String variable = ((VariableToken)operand).getName();
            		//getVariables().getVariable(variable).assign(op);
                    globals.getVariable(variable).assign(op);
                    
                    return result;
                }
                else if(operand instanceof NumberToken)
                {
                    // 5++
                    result = operand.add(new DoubleNumberToken(1));
                }
                else
                    Errors.throwMathLibException("UnaryOperatorToken ++");

                break;
        	}
            default:
            {
                Errors.throwMathLibException("UnaryOperatorToken unknown value");
            }
    	}
        
     	return result;
    }

    /**
     * @return the operator as a string
     */
    public String toString()
    {
        if (value=='-')
            return "--";
        else if (value=='+')
            return "++";
        else
            return String.valueOf(value);
    }

}

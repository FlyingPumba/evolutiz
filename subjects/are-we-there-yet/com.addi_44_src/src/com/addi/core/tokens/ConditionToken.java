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


/**Used to implement if-then-else operations within an expression*/
public class ConditionToken extends OperandToken
{
    /**contains the expression condition*/
    private OperandToken condition;
    
    /**contains the code to execute if the condition is true*/
    private OperandToken code;
    
    /**Constructor setting ifRelation and ifCode
     * @param _ifRelation = the test relation
     * @param _ifCode     = the code to execute if the test is true
     */
    public ConditionToken(OperandToken _condition, OperandToken _code)
    {
	   condition   = _condition;
	   code        = _code;
    }

    /**
     * 
     * @return
     */
    public OperandToken getCondition()
    {
	   return condition;
    }

    /**
     * 
     * @return
     */
    public OperandToken getExpression()
    {
	   return code;
    }

    /**evaluates the operator
     * @param operands = the operators operands
     * @param
     * @return the result of the test as an OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
    	if(condition != null)
    	{
	    	ErrorLogger.debugLine("ConditionToken: testing " + condition.toString());
	    	OperandToken result = condition.evaluate(null, globals);
	    	
	    	if(result instanceof DoubleNumberToken)
	    	{
                DoubleNumberToken num = (DoubleNumberToken)result;
                boolean     tag = true;
                
                // all elements must by unequal zero
                for (int i=0; i<num.getNumberOfElements(); i++)
                {
                    if (num.getValueRe(i)==0.0)
                        tag = false;
                }
                
	    		if(tag)
	    		{
	    			// evaluate Code
                    code.evaluate(null, globals);
	    			return DoubleNumberToken.one;
                }
	    	}
            else if(result instanceof LogicalToken)
            {
                LogicalToken l   = (LogicalToken)result;
                boolean      tag = true;
                
                // all elements must be unequal "false"
                for (int i=0; i<l.getNumberOfElements(); i++)
                {
                    if (l.getValue(i)==false)
                        tag = false;
                }

                if (tag)
                {
                    // evaluate Code
                    code.evaluate(null, globals);
                    return DoubleNumberToken.one;
                }
            }
	 	}
        else
        {
            code.evaluate(null, globals);
	 	    return DoubleNumberToken.one;
        }
        	   	
    	return null;
    }
    
    /**Convert the operator to a string
     * @return the operator as a string
     */
    public String toString()
    {
		String retString = "";
        
        if (condition != null)
	        retString = "condition: " + condition.toString();
        else
            retString = "else: ";

        if (code != null)
            retString += " code: " + code.toString();
        else
            retString += " code: blank ";

        return retString;
    }

} // end ConditionToken


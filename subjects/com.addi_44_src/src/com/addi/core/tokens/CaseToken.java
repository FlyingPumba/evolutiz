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

import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.GlobalValues;


/**Used to implement if-then-else operations within an expression*/
public class CaseToken extends CommandToken
{

	/**condition */
	OperandToken value;

	/** { code } to execute if condition is true*/
	OperandToken code;

	/**Constructor setting ifRelation and ifCode
	@param _ifRelation = the test relation
	@param _ifCode     = the code to execute if the test is true*/
	public CaseToken(OperandToken _value, OperandToken _code)
	{
		value 	= _value;
		code	= _code;
	}

	public OperandToken getExpression()
	{
		return code;
	}


    /**evaluates the operator
    @param operands = the operators operands
    @return the result of the test as an OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
    	if(value != null)
    	{
	    	Expression exp = new Expression(new RelationOperatorToken('e'), 
	    									((OperandToken)operands[0]), 
											value);
	    	
	    	OperandToken result = exp.evaluate(null, globals);
	    	
	    	if(result instanceof LogicalToken)
	    	{
	    		if(((LogicalToken)result).getValue(0))
	    		{
	    			ErrorLogger.debugLine("case is TRUE ");
	    			code.evaluate(null, globals);
	    			return new LogicalToken(true);
	    		}
	    	}
	 	}
	 	else
	 	{
	 		ErrorLogger.debugLine("case is DEFAULT ");
	    	code.evaluate(null, globals);
	    	return new LogicalToken(true);
	 	}
	 		   	
    	return null;
    }
    

    /**Convert the operator to a string
    @return the operator as a string*/
    public String toString()
    {
		if (value != null)
	        return "case: " + value.toString();
		else
			return "default: ";						  
    }

}

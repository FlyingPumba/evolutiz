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

/**Used to implement object orientated access of methods and arguments*/
public class DotOperatorToken extends OperatorToken
{
	/**Default Constructor - creates an operator with the value set to ' '*/
    public DotOperatorToken()
    {
        super(); 
    }

    /**evaluates the operator*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
    	ErrorLogger.debugLine("DotOperatorToken: evaluate");
        
        //  syntax is <left><dot><right>  (e.g. a.b)
        Token left  = operands[0];
        Token right = operands[1];
        
		left = left.evaluate(null, globals);
        
        // not needed. is done by variable token
        // check if left is a variable (e.g. a.abc, where "a" is a structure)
        //if(operands[0] instanceof VariableToken)
        //{
        //    String objName      = ((VariableToken)operands[0]).getName();
        //    String fieldName    = operands[1].toString();
        //    
        //    MathLibObject obj   = (MathLibObject)(getVariables().getVariable(objName).getData()); 
        //    OperandToken  op    = obj.getFieldData(fieldName);
		//
        //    ErrorLogger.debugLine("DotOperatorToken getting object " + objName);
        //    return op.evaluate(null);
        //}
        
        // (e.g. a.sin() or a.getColor() or 2.sin or 3.sin() )
        String name = "";
        
        if (right instanceof FunctionToken)
        { 
            name = ((FunctionToken)right).getName();
        }        
	    
        
        if (!name.equals(""))
        {
	        try
	   	 	{
	    	    //check if a function with this name exists
	    	    if (globals.getFunctionManager().findFunctionByName(name) != null)
				{
	                ErrorLogger.debugLine("parser value.function");
		        	FunctionToken func = new FunctionToken(name, (OperandToken)left);
	     
	    	        return func.evaluate(null, globals);
				}
	        }
	    	catch(Exception e){}
		}
        
        //if(function != null)
        //{
        //}
        //else
        //{
        //    String firstParam = operandStack.pop().toString();
        //    ErrorLogger.debugLine("parser value.field");
        //    OperandToken tree = new VariableToken(token.toString(), firstParam);
        //    return tree;
        //}

        return null;
    }
    

    /**Convert the operator to a string*/
    public String toString()
    {
		return ".";
    }
    
}

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

import com.addi.core.functions.*;
import com.addi.core.interpreter.*;


/**Class used to represent any functions used in the expression*/
public class FunctionToken extends OperandToken
{
    /**The name of the function*/    
    private String name;
    
    /**operands of this function*/
    OperandToken[] operands;
    
    /**Number of left-hand arguments (e.g. [a,b,c]=some_functions() ) */
    private int noOfLeftHandArguments = 0;

    /**Indicator if function is a class file or a m-script file */
    private boolean scriptTag = false;
    
    /**Indicator if function has been evaluated */
    private boolean isEvaluatedB = false;
    
    // Indicator wether or not the operands should be evaluated:
    // e.g. global aa bb   (then global needs aa=variable as input and 
    //   not the value of aa
    public boolean evaluationLockB = false;
    
    /**constructor for function tokens
     * @param _name = the name of the function
     */
    public FunctionToken(String _name)
    {
        name = _name;
        priority = 10;
    }

    /**constructor for function tokens
    @param _name  = the name of the function
    @param _param = the first argument*/
    public FunctionToken(String _name, OperandToken _firstParameter)
    {
        name        = _name;
        priority    = 10;
        operands    = new OperandToken[1];
        operands[0] = _firstParameter;
    }
    
    /**constructor for function tokens
    @param _name   = the name of the function
    @param _param  = the parameters of the function*/
    public FunctionToken(String _name, OperandToken[] _parameters)
    {
        name        = _name;
        priority    = 10;
        operands    = _parameters;
    }
    

    /**Sets the number of left-hand arguments of the function    
    (e.g.) [a,b,c]=some_function will return set a "3"        
    @param _number = the number of left hand arguments*/
    public void setNoOfLeftHandArguments(int _number)
    {
        noOfLeftHandArguments = _number;
    }

    /**Return one of the operands
     * @param index    = index of operand 
     * @return operand = requested operand
     */
    public OperandToken getOperand(int index)
    {
        if (operands == null)
            return null;
            
        if ((index >= operands.length) || (index <=0))
            return null;
            
        return operands[index];
    }

    /**Return all operands
    @return operands = all operands of this function */
    public OperandToken[] getOperands()
    {
        return operands;
    }

    /**Set operands of this function
    @param _operands = operands of this function */
    public void setOperands(OperandToken[] _operands)
    {
        operands = _operands;
    }

    /**Set first operand of this function
    @param _operand = first operand of this function */
    public void setOperand(OperandToken _operand)
    {
        operands    = new OperandToken[1];
        operands[0] = _operand;
    }

    /**Executes the function referenced by this token
     * @param operands = the functions operands
     * @param
     * @return the result of the function as an OperandToken
     */
    public OperandToken evaluate(Token[] _operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        Function     function = null;
        OperandToken result   = null;
        ErrorLogger.debugLine("FunctionToken: eval "+name);
            
        //setGlobals(globals);
        
        // special handling for "return" function
        //  throw control exception
        if (name.equals("return"))
        {
            ErrorLogger.debugLine("FunctionToken: return: control exception");
            throw new ControlException();
        }
        
        // check if the function is overloaded by a variable name
        //if( getVariables().getVariable(name) != null)
        if( globals.getVariable(name) != null)
        {
            // Variable overloads function 
            ErrorLogger.debugLine("FunctionToken: eval: variable overloads function");
        
            // create variable and set parameters of this function as limits of variable
            VariableToken  varToken = new VariableToken(name, operands);
         
            // evaluate variable with limits and return result
            return varToken.evaluate(null, globals); 
        }
    
        // Function is not overloaded by a variable
        // Now try to find the function
        try {
			function = globals.getFunctionManager().findFunction(this);
		} catch (Exception e) {
			Errors.throwMathLibException(e.getMessage());
		}
      
        // Check wether or not the function was found
        if(function != null)
        {
            // Function has been found
        
            if (function instanceof UserFunction)
                if (((UserFunction)function).isScript())
                    scriptTag = true;
        
            // Set the number of left-hand arguments for return value processing
            function.setNoOfLeftHandArguments(noOfLeftHandArguments);
            
            ErrorLogger.debugLine("FunctionToken eval = " + name);

            // evaluate operands first
            // do not evaluate if evaluationLock is set (e.g. global aa bb cc)
            if ((operands != null) && (!evaluationLockB))
            {
                for (int i=0; i<operands.length; i++)
                {
                    operands[i]=operands[i].evaluate(null, globals);
                }
            }
            
            // evaluate function with its operands
            // If the function if of type UserFunction it has to be cloned so that
            //    the original m-function or m-script stays untouched
            if (function instanceof UserFunction)
                result = ((Function)function.clone()).evaluate(operands, globals);
            else
                result = function.evaluate(operands, globals);
            
            // remember that this function has been evaluated
            // (important for functions which are overloading variables (e.g. who ))
            isEvaluatedB = true;

            // check for ???
            //if ((operands != null) && (operands.length > 0))
            //{
            //   if(result == null) 
            //   {
            //      result = this;
            //   }
            //}
            
            ErrorLogger.debugLine("FunctionToken result = "+result); 
            return result;
        } // end function!=null
            
        Errors.throwMathLibException("FunctionToken: undefinded variable or function "+name);
        return result;
    } // end evaluate

    /** Getter method,
    @return true is the function is not a function but an m-script file*/
    public boolean isScript()
    {
        return scriptTag;
    }
    
    /** Getter method,
    @return true is the function has been evaluated*/
    public boolean isEvaluated()
    {
        return isEvaluatedB;
    }

    /**Getter method, 
    @return the functions name*/
    public String getName()
    {
        return name;
    }

    /**Tests if an object is equal to this function
    @param obj = object to test
    if obj is an instance of Function or Function token then it
    compares the name of obj to the functions name otherwise it
    calls the superclasses version
    @return true if they are equal*/
    public boolean equals(Object obj)
    {
        boolean equal = false;
        if(obj instanceof Function)
        {
            equal = ((Function)obj).getName().toUpperCase().equals(name.toUpperCase());
        }
        else if(obj instanceof FunctionToken)
        {
            equal = ((FunctionToken)obj).getName().toUpperCase().equals(name.toUpperCase());
        }
        else
            equal = super.equals(obj);
        return equal;
    }

    /**@return the function as a string*/
    public String toString()
    {
        String result = name + "(";

        if (operands == null)
        	return null;
        
        for (int i=0; i<operands.length; i++)
        {
            if (operands[i] != null)
            {
                result += operands[i].toString();
                if (i < (operands.length-1))
                {
                    if (operands[i+1] != null) result += ",";
                }
            }
        }

        result += ")";

        return result;
    }

}

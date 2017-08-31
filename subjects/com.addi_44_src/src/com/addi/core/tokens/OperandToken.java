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


import java.util.Enumeration;
import java.util.ArrayList;

import com.addi.core.interpreter.*;

/**The base class of all operands used in an expression*/
abstract public class OperandToken extends Token
{
	
    /**Default Constructor*/
    public OperandToken()
    {
    	super();
    }

    /**Constructor 
    @param _priority, priority of token
    @param _typeName - a string representing the type, used for casting*/
    public OperandToken(int _priority)
    {
        super(_priority);
    }

    /**Constructor 
       @param _priority, priority of token
       @param _typeName - a string representing the type, used for casting*/
    //public OperandToken(int _priority, String _typeName)
    //{
    //	super(_priority, ttOperand, _typeName);
    //}

    /**multiply this token by another
       @param arg = the amount to multiply it by
       @return the result as an OperandToken*/
    public OperandToken multiply(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken multiply");
        return null;
    }

    /**scalar multiply this token by another
       @param arg = the amount to multiply it by
       @return the result as an OperandToken*/
    public OperandToken scalarMultiply(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken scalarMultiply");
    	return null;
    }


    /**add this token to another
       @param arg = the amount to add to it 
       @return the result as an OperandToken*/
    public OperandToken add(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken add");
    	return null;
    }

    /**subtract this token from another
       @param arg = the amount to subtract from it 
       @return the result as an OperandToken*/
    public OperandToken subtract(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken subtract");
    	return null;
    }
    
    /**divide this token by another
       @param arg = the amount to divide it by
       @return the result as an OperandToken*/
    public OperandToken divide(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken divide");
    	return null;
    }

    /**divide this token by another
       @param arg = the amount to divide it by
       @return the result as an OperandToken*/
    public OperandToken scalarDivide(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken scalarDevide");
    	return null;
    }

    public OperandToken leftDivide(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken leftDivide");
        return null;
    }
    
    public OperandToken scalarLeftDivide(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken scalarLeftDivide");
        return null;
    }

    /**raise this token to the power of another
       @param arg = the amount to raise it by
       @return the result as an OperandToken*/
    public OperandToken power(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken power");
    	return null;
    }

    /**raise this token to the matrix power of another
    @param arg = the amount to raise it by
    @return the result as an OperandToken*/
     public OperandToken mPower(OperandToken arg)
     {
         Errors.throwMathLibException("OperandToken matrix power");
        return null;
     }

    /**raise this token to the power of another
    @param arg = the amount to multiply it by
    @return the result as an OperandToken*/
    public OperandToken mpower(OperandToken arg)
    {
        Errors.throwMathLibException("OperandToken mpower");
        return null;
    }


    
    //unary operations
    /**calculate the factorial
       @return the result as an OperandToken*/
    public OperandToken factorial()
    {
        Errors.throwMathLibException("OperandToken factorial");
        return null;
    }

    /**calculate the transpose
       @return the result as an OperandToken*/
    public OperandToken transpose()
    {
        Errors.throwMathLibException("OperandToken transpose");
    	return null;
    }

    /**calculate the conjugate transpose
    @return the result as an OperandToken*/
    public OperandToken ctranspose()
    {
        Errors.throwMathLibException("OperandToken ctranspose");
        return null;
    }

    /**calculate the transpose
       @return the result as an OperandToken*/
    public OperandToken negate()
    {
        Errors.throwMathLibException("OperandToken negate");
    	return this;
    }

    /**Symbolic function - simplifies the token*/
    public OperandToken simplify()
    {
        Errors.throwMathLibException("OperandToken simplify");
        return this;
    }

    /**Symbolic function - expands the token*/
    public OperandToken expand()
    {
        Errors.throwMathLibException("OperandToken expand");
        return this;
    }

    /**Symbolic function - calculates the the derivative of the token
       @param deriveBy = the symbol to derive by*/
    public OperandToken derivative(String deriveBy)
    {
        Errors.throwMathLibException("OperandToken derivative");
        return this;
    }
    
    /**Symbolic function - calculates the the integral of the token
       @param deriveBy = the symbol to integrate by*/
    public OperandToken integral(String integrateBy)
    {
        Errors.throwMathLibException("OperandToken integral");
        return this;
    }

    /**Symbolic expression - replaces a symbol with an expression
       @param old = the symbol to replace
       @param substBy = the expression to replace it with*/
    public OperandToken subst(OperandToken old, OperandToken substBy)
    {
        Errors.throwMathLibException("OperandToken subst");
        return this;
    }

    /**Checks if this operand is a numeric value
    @return true if this is a number, false if it's 
    an algebraic expression*/
    public boolean isNumeric()
    {
    	return false;
    }

    /**@return true if the token is null*/
    public boolean isNull()
    {
    	return false;
    }

    /**checks if this is a leaf node of the expression tree
    @return true*/
    public boolean isLeaf()
    {
        return true;
    }
    
    /**function to access all children of a node within the expression tree
    @return all the nodes children as a enumeration*/
    public Enumeration Children()
    {
        return null;
    }    

    /**@return the token as an ArrayList*/
    public ArrayList asArray()
    {
    	ArrayList list =  new ArrayList();
    	list.add(this);
    	return list;
    }

}

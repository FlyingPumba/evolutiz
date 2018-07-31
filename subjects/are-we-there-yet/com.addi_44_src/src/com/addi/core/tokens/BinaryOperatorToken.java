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


/**The base class for all binary operators*/
public class BinaryOperatorToken extends OperatorToken
{

    /**Default COnstructor - creates an operator with the value set to ' '*/
    public BinaryOperatorToken()
    {
        super(0); 
        value = ' ';
    }

    /**Constructor taking the operator and priority
    @param _operator = the type of operator
    @param _priority = the priority of the operator*/
    public BinaryOperatorToken(char _operator, int _priority)
    {
    	/**call the super constructor, type defaults to ttoperator and operands to 2*/
        super(_priority); 

        value = _operator;
    }

    /**evaluate the operator
    @param operands = the operators operands
    @return the result as and OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        return null;
    }

    /**Convert the operator to a string*/
    public String toString()
    {
        return String.valueOf(value);
    }

    /**Checks if an object is equal to this operator
    if object is a binary operator then it checks whether they have the
    same value otherwise it calls the super classes version
    @param object = the object being tested against
    @return true if they are equal*/
    public boolean equals(Object object)
    {
    	boolean equal = false;
    	if(object instanceof BinaryOperatorToken)
    		equal = (value == ((BinaryOperatorToken)object).value);
    	else
    		equal = super.equals(object);

    	return equal;
    }
}

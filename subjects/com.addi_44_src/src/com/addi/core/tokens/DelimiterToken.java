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

import com.addi.core.constants.TokenConstants;
import com.addi.core.interpreter.GlobalValues;


/*Class used to represent delimiter tokens such as ( ) and ,*/
public class DelimiterToken extends OperandToken implements TokenConstants
{
	/**Character representing the delimiter*/
    public char value;
    
    private String wordValue;

	/**param _value = the value of the delimiter as a char*/
    public DelimiterToken(char _value)
    {
    	super(0); //, "Delimiter");
    	value = _value;
    	wordValue = "";
    }

	/**param _value = the value of the delimiter as a string*/
    public DelimiterToken(String _value)
    {
    	super(0); //, "Delimiter");
    	value = '-';
    	wordValue = _value;
    }

	/**Evaluate the delimiter, just returns the object itself
	@param operands = the delimiters operands
	@return the delimter token as an OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {   
    	if (breakHit || continueHit)
    		return null;
    	
    	return this;
    }

	/**@return the value of the delimiter as a string*/    
    public String toString()
    {
    	return String.valueOf(value) + wordValue;
    }

    /**Checks if this operand is a numeric value
    @return true if this is a number, false if it's 
    an algebraic expression*/
    public boolean isNumeric()
    {
    	return true;
    }
    
    //accessor functions
    /**@return the value of wordValue*/
    public String getWordValue()
    {
    	return wordValue;
    }   
}

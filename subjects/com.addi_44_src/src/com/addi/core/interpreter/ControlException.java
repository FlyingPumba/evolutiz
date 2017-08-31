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

package com.addi.core.interpreter;

import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;

/**MathLib specific exception */
public class ControlException extends ArithmeticException
{
    public static final int Return = 0;
    public static final int Yield = 0;

    private int type;

    private OperandToken result;

    /**Create a new exception object*/
    public ControlException()
    {
    	type = 0;
    	result = null;
    }
    
    /**Set the message text
     @param text = the text to display*/
    public ControlException(int _type, Token _result)
    {
    	super("");
    	type = _type;
    	result = ((OperandToken)_result);
    }

    public int getType()
    {
    	return type;
    }

    public OperandToken getResults()
    {
    	return result;
    }
}

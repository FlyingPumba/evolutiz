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


/**
 * 
 * @author stefan
 *
 */
public class FunctionHandleToken extends DataToken
{            

    
    private String name = "";
    
    public FunctionHandleToken()
    {
        super(5, "function_handle");
    }

    public FunctionHandleToken(String _handleS)
    {
        // empty number token
            super(5, "function_handle");
            name = _handleS;
    }

    public String getName()
    {
        return name;
    }
    
    
    /**Evaluate the token. This causes it to return itself*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        return this;    
    }


    public String toString()
    {
        return "@"+name;
    }


} // end FunctionHandleToken

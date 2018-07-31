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

/**Class for implementing commands */
public class CommandToken extends OperandToken
{

	/**default constructor*/
    public CommandToken()
    {
    }

	/**@return the command as a string*/
    public String toString()
    {
    	return null; 
    }

	/**Evaluates the command
	@param operands = the commands operands
	@return the result of the command as an OperandToken*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        return null;
    }
}

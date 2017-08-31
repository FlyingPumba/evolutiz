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

package com.addi.core.functions;

import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**The base class for all the internal function types*/
public class InternalFunction extends Function
{
	/**Default constructor - creates an internal function with a null name*/
	public InternalFunction()
	{
		name = "";
	}

	/**Creates an internal function with it's name set to _name
	 * @param _name = the name of the function
	 */
	public InternalFunction(String _name)
	{
		name = _name;
	}

	/**
	* Executes the internal function
	* @param operands - the array of parameters
	* @param pointer to global values
	* @return the result as an OperandToken
	*/	
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		return null;
	}
}
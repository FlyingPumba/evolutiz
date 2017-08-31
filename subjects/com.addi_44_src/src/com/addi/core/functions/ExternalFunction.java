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

/**Base class for all external function classes*/
abstract public class ExternalFunction extends Function
{
    
    /**Index for real values within array*/
    protected static final int REAL = 0;
    
    /**Index for Imaginary values within array*/
    protected static final int IMAG = 1;

	/**Number of paramaters take by the function*/
	private int paramCount;	
	
	/**Default constructor - creates an external function with a null name*/
	public ExternalFunction()
	{
		name = "";
	}

	/**Creates an external function called _name
	@param _name = the name of the function*/
	public ExternalFunction(String _name)
	{
		name = _name;
	}

	/**@return the number of paramaters taken by the function*/
	public int getParamCount()
	{
		return paramCount;
	}

}

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

package com.addi.core.constants;

public interface ErrorCodes
{
    public static final int OK = 0;
    public static final int OK_FUNCTION_PROCESS = 1;

    //syntax errors
    public static final int ERR_OPNOTSUPPORTED           = 1000;

    public static final int ERR_BRACKET_ORDER = 2000;
    public static final int ERR_BRACKET_OPEN  = 2001;

    //variable errors
    public static final int ERR_VARIABLE_NOTDEFINED = 3000;
    public static final int ERR_LVALUE_REQUIRED     = 3100;
    
	//General function errors
	public static final int ERR_INVALID_PARAMETER = 10000;
	public static final int ERR_INSUFFICIENT_PARAMETERS=10001;	    
	public static final int ERR_TOO_MANY_PARAMETERS=10002;	    
	
	public static final int ERR_FUNCTION_NOT_FOUND       = 10100;
   public static final int ERR_FUNCTION_NOT_IMPLEMENTED = 10101;

    //matrix errors
    public static final int ERR_NOT_SQUARE_MATRIX = 12000;
    public static final int ERR_MATRIX_SINGULAR   = 12001;

	//misc errors
	public static final int ERR_USER_ERROR = 20000;
} 
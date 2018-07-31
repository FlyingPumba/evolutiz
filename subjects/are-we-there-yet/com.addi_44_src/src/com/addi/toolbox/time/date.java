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

package com.addi.toolbox.time;

import java.util.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;


/*
original author: Stefan Mueller (stefan@held-mueller.de) 2002
*/


/**An external function for computing a mesh of a matrix  */
public class date extends ExternalFunction
{
	/**returns a string 
	* @return the current date as a string */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		Calendar date = Calendar.getInstance();
        
		return new CharToken( date.toString() );

	} // end eval
}


/*
@GROUP
time
@SYNTAX
data = date()
@DOC
Returns the current date.
@EXAMPLES
<programlisting>
d=date()
returns 
d= 7-7-2002
</programlisting>
@NOTES
@SEE
tic, toc, pause, time
*/

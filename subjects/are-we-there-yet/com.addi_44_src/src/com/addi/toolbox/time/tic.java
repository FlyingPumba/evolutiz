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
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;




/**An external function for starting the internal stop watch  */
public class tic extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		Date d = new Date();
        
        double start = (double)d.getTime();
        
   	 	Variable ticVar = globals.getGlobalVariables().createVariable("_tic");
		ticVar.assign(new DoubleNumberToken(start));
        
		return null; //DoubleNumberToken.one;

	} // end eval
}


/*
@GROUP
time
@SYNTAX
tic()
@DOC
Starts the stop watch.
@EXAMPLES
<programlisting>
tic()
toc()
ans = 1.34
</programlisting>
@NOTES
@SEE
toc, pause, date, time
*/

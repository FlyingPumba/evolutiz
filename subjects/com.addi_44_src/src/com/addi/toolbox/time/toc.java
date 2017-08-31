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
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for computing the time difference between a 
   call to tic() and toc()  (internal stop watch)  */
public class toc extends ExternalFunction
{
	/**returns a time difference 
	* @return the time difference in seconds as a double number */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		Date d = new Date();
        
        double stop = (double)d.getTime();
        
        if (!globals.getGlobalVariables().isVariable("_tic"))
        	throwMathLibException("toc: you must call tic before toc");
            
   	 	OperandToken ticTok = globals.getGlobalVariables().getVariable("_tic").getData();
		
        if (ticTok instanceof DoubleNumberToken)
        {
        	double start = ((DoubleNumberToken)ticTok).getValueRe();
            return new DoubleNumberToken( (stop - start)/1000 );
        }
        else
            throwMathLibException("toc: _tic variable has wrong type"); 	
            
        return null; 

	} // end eval
}


/*
@GROUP
time
@SYNTAX
toc()
@DOC
Returns the time difference in seconds between the call to tic()
and toc().
@EXAMPLES
<programlisting>
tic()
toc()
returns
ans = 1.34
</programlisting>
@NOTES
@SEE
tic, pause, date, time
*/

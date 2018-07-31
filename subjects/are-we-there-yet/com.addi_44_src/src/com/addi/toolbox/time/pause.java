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

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/*
original author: Stefan Mueller (stefan@held-mueller.de) 2003
*/

/**wait for a specified period of time*/
public class pause extends ExternalFunction
{

	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1)
			throwMathLibException("pause: number of arguments != 1");
            

		if (operands[0] instanceof DoubleNumberToken) 
        {
			double pause = (((DoubleNumberToken)operands[0]).getReValues()[0][0]);
        
            try {        
                Thread.sleep((int)(pause*1000));
            }
            catch (InterruptedException e)
            {
            }

		}
        
		return null;
	}
}

/*
@GROUP
time
@SYNTAX
pause(value)
@DOC
Wait for a specified period of time. The command pause(n)
waits for n seconds
@EXAMPLES
<programlisting>
pause(3) waits for 3 seconds
pause(3.5) waits for 3.5 seconds
</programlisting>
@NOTES
Keep in mind that the parsing of the pause(x) command will
also take some time. Therefor the  waiting time will be longer
than expected.
@SEE
tic, toc, date, time

*/


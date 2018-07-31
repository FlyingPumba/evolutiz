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

package com.addi.toolbox.general;


import java.util.Date;
import java.util.Calendar;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**External function to return the date and time*/
public class clock extends ExternalFunction
{
	/**@return the current date and time as a 6 by 1 vector containing
	[year month day hours minutes seconds]*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		Date now = new Date();
		
		Calendar calendarInst = Calendar.getInstance();
		calendarInst.setTime(now);
		
		double[][] datetime = new double[1][6];
		datetime[0][0]  = calendarInst.get(Calendar.YEAR);
		datetime[0][1]  = calendarInst.get(Calendar.MONTH) + 1;
		datetime[0][2]  = calendarInst.get(Calendar.DATE);
		
		datetime[0][3]  = calendarInst.get(Calendar.HOUR) + 12*calendarInst.get(Calendar.AM_PM);
		datetime[0][4]  = calendarInst.get(Calendar.MINUTE);
		datetime[0][5]  = calendarInst.get(Calendar.SECOND);
		
		DoubleNumberToken result	= new DoubleNumberToken(datetime);
		return result;
	}
}

/*
@GROUP
general
@SYNTAX
clock()
@DOC
Returns the current date and time as a six element vector.
@EXAMPLES
clock() = [2,002, 6, 6, 5, 23, 34]
@NOTES
The variable should be given as a string containing the variable name.
@SEE
date, tic, toc, time
*/

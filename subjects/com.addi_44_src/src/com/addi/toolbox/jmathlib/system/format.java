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

package com.addi.toolbox.jmathlib.system;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;

public class format extends ExternalFunction
{
	/**Returns an enviroment variable
	@param operand[0] = the name of the variable
	@param operand[1] = a default value (optional)
	@return the enviroment value*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
		
        if (getNArgIn(operands)>1)
            throwMathLibException("format: number of arguments > 1");
        

        if ( (getNArgIn(operands)==1) &&
             (!(operands[0] instanceof CharToken)) )
            throwMathLibException("format: argument must be a string");

		String type = "";
		
		if (getNArgIn(operands)==1)
		    type = ((CharToken)operands[0]).getElementString(0);
		
        //setNumberFormat(DecimalFormat.getInstance(Locale.ENGLISH));
		
		if (type.equals("short"))
		    globals.setNumberFormat(new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.ENGLISH)));
		else if (type.equals("long"))
		    globals.setNumberFormat(new DecimalFormat("0.000000000000000", new DecimalFormatSymbols(Locale.ENGLISH)));
		else if (type.equals("short e"))
		    globals.setNumberFormat(new DecimalFormat("0.0000E000", new DecimalFormatSymbols(Locale.ENGLISH)));
        else if (type.equals("long e"))
            globals.setNumberFormat(new DecimalFormat("0.000000000000000E000", new DecimalFormatSymbols(Locale.ENGLISH)));
        else if (type.equals("short g"))
            globals.setNumberFormat(new DecimalFormat("0.0000E000", new DecimalFormatSymbols(Locale.ENGLISH)));
        else if (type.equals("long g"))
            globals.setNumberFormat(new DecimalFormat("0.000000000000000E000", new DecimalFormatSymbols(Locale.ENGLISH)));
        else if (type.equals("short eng"))
            globals.setNumberFormat(new DecimalFormat("0.0000E000", new DecimalFormatSymbols(Locale.ENGLISH)));
        else if (type.equals("long eng"))
            globals.setNumberFormat(new DecimalFormat("0.000000000000000E000", new DecimalFormatSymbols(Locale.ENGLISH)));
		else
		    globals.setNumberFormat(new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.ENGLISH)));
		    
		return result;
	}
}

/*
@GROUP
system
@SYNTAX
format('type')
@DOC
changes the numerical format for numbers.
@NOTES
It only affects the display of numbers, the internal format
of numbers is not affected at all.
@EXAMPLES
format()
format('long')
format('short')
format('long e')
format('short e')
format('long g')
format('short g')
format('long eng')
format('short eng')
@SEE
disp
*/


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

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


public class isa extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 2)
			throwMathLibException("isa: number of arguments != 2");

        if (!(operands[0] instanceof DataToken))  
            throwMathLibException("isa: first operand must be a data token");
        
        if (!(operands[1] instanceof CharToken))  
            throwMathLibException("isa: second operand must be a char token");
        
        String opDataType = ((DataToken)operands[0]).getDataType();
        
        String reqDataType = ((CharToken)operands[1]).getValue();

        
		if (opDataType.compareTo(reqDataType)==0)  
            return DoubleNumberToken.one;
		else
            return DoubleNumberToken.zero;
	}
}

/*
@GROUP
general
@SYNTAX
isa(value,'class')
@DOC
.
@EXAMPLES
<programlisting>
a=55
isa(a,'double')  -> 1
isa(a,'char')    -> 0
</programlisting>
@NOTES
.
@SEE
ismatrix, isnumeric, isscalar, issquare, islogical
*/


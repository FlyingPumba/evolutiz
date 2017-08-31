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

import com.addi.core.functions.*;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**External function for creating structures*/
public class struct extends ExternalFunction
{
    /**create a structure
    @param operands[n]   = name of field
    @param operands[n+1] = field value*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        MathLibObject obj;
        int length = operands.length;
        int start = 0;
        
        if(operands[0] instanceof MathLibObject)
        {
            ErrorLogger.debugLine("1st param structure");
            obj = new MathLibObject(((MathLibObject)operands[0]));
            start = 1;
        }
        else
        {
            obj = new MathLibObject();
        }

        for(int fieldno = start; fieldno < length; fieldno +=2)
        {
            String fieldName = ((CharToken)operands[fieldno]).getElementString(0);
            OperandToken value = null;
            if(length > fieldno + 1)
               value = ((OperandToken)operands[fieldno + 1]);
            else
                value = DoubleNumberToken.zero;
            
            obj.setField(fieldName, value);
        }
        return obj;
    }
}

/*
@GROUP
general
@SYNTAX
structure = STRUCT(variable1, value1, variable2, value2,...., variableN, valueN);
structure = STRUCT(structure, variable1, value1, variable2, value2,...., variableN, valueN);
@DOC
Creates a structured variable.
If the first paramater is a structure then the structure inherits it's values.
@EXAMPLES
<programlisting>
x=STRUCT("a", 1, "b", 2) = a = 1 : b = 2 :
y=STRUCT(x,"c",3) = a = 1 : b = 2 : c = 3 :
</programlisting>
@NOTES
@SEE
*/


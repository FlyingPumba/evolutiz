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

import java.util.Date;
import java.util.Random;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;



public class jmathlibcreateuniqueid extends ExternalFunction
{
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {   

        //throwMathLibException("jmathlibcreateuniqueid");

        
        String uniqueIDS = globals.getProperty("jmathlib.id.unique");
        
        if (uniqueIDS==null)
        {
            
            Date d = new Date();
            double start = (double)d.getTime();
            String startS = new Double(start).toString();
            
            String randS = new Double(Math.random()).toString();
            
            //System.out.println("RANDOM "+startS +" "+randS);
            
            globals.setProperty("jmathlib.id.unique", startS+randS);
        }
        
        return  null;
    }
}

/*
@GROUP
system
@SYNTAX
jmathlibcreateuniqueid
@DOC
create a unique id for jmathlib
@NOTES
@EXAMPLES
@SEE
update, checkforupdates
*/
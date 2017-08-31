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

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;


/**An external function for returning version information*/
public class ver extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		//String s        = "";
 		    
		globals.getInterpreter().displayText("PRINTADDIVERSION");

        //s = globals.getProperty("jmathlib.version");
        //globals.getInterpreter().displayText("version: "+s);

        //s = globals.getProperty("jmathlib.release.date");
        //globals.getInterpreter().displayText("release date: "+s);

        //s = globals.getProperty("jmathlib.release.name");
        //globals.getInterpreter().displayText("release name: "+s);

        //s = globals.getProperty("jmathlib.release.description");
        //globals.getInterpreter().displayText("release description: "+s);

        //s = globals.getProperty("jmathlib.copyright");
        //globals.getInterpreter().displayText(s);

		return null;		
	}
}

/*
@GROUP
system
@SYNTAX
ver
@DOC
detailed version information about toolboxes
@EXAMPLE
<programlisting>
ver
</programlisting>
@NOTES
.
@SEE
version
*/

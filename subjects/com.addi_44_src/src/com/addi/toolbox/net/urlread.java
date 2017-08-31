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

package com.addi.toolbox.net;


import java.net.*;
import java.io.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;

/**An external function for reading files over the network*/
public class urlread extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		String s        = "";
        String lineFile = "";;
 		    
        if (getNArgIn(operands) != 1)
			throwMathLibException("urlread: number of arguments < 1");
            
		if (!(operands[0] instanceof CharToken))
			throwMathLibException("urlread: argument must be String");
        
        String urlString = ((CharToken)operands[0]).getElementString(0);
        
        // open URL
        URL url = null;
        try
        {
            url = new URL( urlString );
        }
        catch (Exception e)
        {
            throwMathLibException("urlread: malformed url");
        }          
        
        // read file over the network
        try 
	    {			
		    BufferedReader inReader = 
                new BufferedReader(new InputStreamReader( url.openStream() ));
 		    
            while ((lineFile = inReader.readLine()) != null)
		    {		    	
		        s += lineFile + "\n";
		    }

		    inReader.close();
	     }
	     catch (Exception e)
	     {
		    throwMathLibException("urlread: error input stream");
	     }		    

	   

		return new CharToken(s);		
	}
}

/*
@GROUP
net
@SYNTAX
urlread( URL)
@DOC
Read a text file from the network. This function supports many
transfer protocols e.g. http, gopher, ftp, file. 
@EXAMPLE
<programlisting>
urlread("http://www.heise.de/newsticker/");
urlread("file:///c:/home/text.txt");
</programlisting>
@NOTES
.
@SEE
csvread, csvwrite
*/

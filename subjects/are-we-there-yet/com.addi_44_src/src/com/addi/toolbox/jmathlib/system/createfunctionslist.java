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


import java.util.Calendar;
import java.util.Vector;
import java.io.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.functions.FileFunctionLoader;
import com.addi.core.functions.FunctionLoader;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**An external function for creating a filelist used by the class
   loader for java applets      */
public class createfunctionslist extends ExternalFunction
{

	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        Vector pathVector = new Vector();
        
        // get all search paths from all file loaders
        for (int i=0;i<globals.getFunctionManager().getFunctionLoaderCount();i++) 
        {

            FunctionLoader loader = globals.getFunctionManager().getFunctionLoader(i);
            
            if (loader instanceof FileFunctionLoader) 
            {
                FileFunctionLoader ffl = (FileFunctionLoader)loader;
                
                for (int pathIdx=0;pathIdx<ffl.getPathCount();pathIdx++) 
                {
                    File path = ffl.getPath(pathIdx).getAbsoluteFile();
                        
                    pathVector.add(path.toString());                    
                }                                        
            }
        }        
        
        
        int    size       = pathVector.size();
        String path       = "";
        String line       = "";

        ErrorLogger.debugLine("working directory: "+globals.getWorkingDirectory().getAbsolutePath());

        try 
        {
            
            // create file to store all class-, m-files and images
            File funcFile = new File(globals.getWorkingDirectory().getAbsoluteFile() +
			                         File.separator + "bin" +
                                     File.separator + "webFunctionsList.dat");
			BufferedWriter outWriter = new BufferedWriter( new FileWriter(funcFile));
            ErrorLogger.debugLine("funcFile ="+funcFile.toString());


            /* The first line of the file is a comment */
            line = "# created with createfunctionslist()";
            outWriter.write(line, 0, line.length());
			outWriter.newLine();
            line = "# This is a generated file. DO NOT EDIT!";
            outWriter.write(line, 0, line.length());
			outWriter.newLine();
			
			// add date and time to the webFunctionsList
			Calendar cal = Calendar.getInstance();
            String date  = Integer.toString(cal.get(Calendar.YEAR))        + "/"
                         + Integer.toString(cal.get(Calendar.MONTH)+1)     + "/"
                         + Integer.toString(cal.get(Calendar.DAY_OF_MONTH))+ " "
                         + Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + ":"
                         + Integer.toString(cal.get(Calendar.MINUTE))      + ":"
                         + Integer.toString(cal.get(Calendar.SECOND));
            line = "# created on "+ date;
            outWriter.write(line, 0, line.length());
            outWriter.newLine();
	        
	        // search through all serach directories
	        for(int n = 0; n < size; n++)
	        {
	            path = (String)pathVector.elementAt(n);
	            ErrorLogger.debugLine("path func manager = "+path);
	            
	           	File dir       = new File(path);
				String[] files = dir.list();

                // do not use empty directories
                if (files != null)
                {
                    for(int fileNo = 0; (fileNo < files.length); fileNo++)
                    {
                        line = path + "/" + files[fileNo];
                    
                        // use unix-style file separator as default
                        line = line.replace('\\', '/'); 

                        // do not put CVS directories and files into functions list
                        if (!line.endsWith("/CVS")            &&
                            !line.endsWith("/CVS/Entries")    &&
                            !line.endsWith("/CVS/Root")       &&
                            !line.endsWith("/CVS/Repository") &&
                            !line.contains(".svn")              )
                        {
                            // remove preceding absolute path
                            File f = new File(globals.getWorkingDirectory(),"bin/");
                            
                            //ErrorLogger.debugLine(f.getCanonicalPath().toString());
                            line = line.substring(f.getCanonicalPath().toString().length()+1);

                            ErrorLogger.debugLine("path = "+ line);

                            // also remove unwanted directories
                            if (!line.startsWith("jmathlibtests") && 
                                !line.startsWith("jmathlib/tools")   )
                            {
                                // write relative path and filename to dat-file
                                outWriter.write(line, 0, line.length());
                                outWriter.newLine();
                            }
                        }

                    }
                }
                else
                    ErrorLogger.debugLine("directory is empty");
	        }
			outWriter.close();

        }
        catch (Exception e)
        {
            ErrorLogger.debugLine("createFunctionsList");
            e.printStackTrace();
        }
        return DoubleNumberToken.one;		

	} // end evaluate
}

/*
@GROUP
system
@SYNTAX
createfunctionslist()
@DOC
Creates a file called webFunctionList.dat containing the name 
of all external functions.
This functions is used when running JMathLib as an applet.
@NOTES
@EXAMPLES
createfunctionslist()
@SEE
path, rmpath, update, checkforupdates
*/


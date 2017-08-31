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

package com.addi.core.interpreter;

import java.io.*;

/**write error messages + debug information to a log file*/
public class ErrorLogger 
{
    /** indent string */
    private static String indentS = "";

    /**flag for logging mode of JMathLib*/
    private static boolean debugB = false;
    
    /** handle to log file */
    private static RandomAccessFile output = null;
    
    /**
     * @return the setting of the debug flag
     */
    public static boolean getDebug()
    {
        return debugB;
    }
    
    /**
     * sets the debug flag
     * @param _debug = should debug information be displayed
     */
    public static void setDebug(boolean _debug)
    {
        debugB = _debug;
    }
    
    /**
     * display a debug line to the standard output and the file MathLib.log
     *  @param text = the text to display
     */
    public static void debugLine(String text)
    {

        if(debugB)
        {
            //for (int i=1; i<indentSize; i++) text= " "+text;	
            //text = new String( new byte[indentSize]) + text;
            //text = indentS + text;
            
            try
            {
                // open log file only if it is not yet open
                if (output == null)
                {
                    System.out.println("ERROR LOGGER: OPENING FILE");
                    output = new RandomAccessFile("JMathLib.log", "rw");
                    output.seek(output.length());
                }
                
                //write log message
                output.writeBytes(indentS + text + "\n");
            }
            catch(IOException error) 
            {
                System.out.println("ERROR LOGGER: IOException");
            }
            catch(SecurityException error)
            {
                System.out.println("ERROR LOGGER: SecurityException");
            }
    
            // write log message to display
            System.out.println(indentS + text);
        }
    }
    
    /**
     * Will release the file handle to the logfile
     * @throws Throwable
     */
    public void finalize() throws Throwable 
    {
        try 
        {
            System.out.println("ERROR LOGGER FINALIZE");
            output.close();    
        } 
        finally 
        {
            super.finalize();
        }
    }

	/**
	 * display an integer to the standard output
	 * @param value = the number to display
	 */
	public static void debugLine(int value)
	{
		debugLine(new Integer(value).toString());
	}

	/**
	 * display a real value to the standard output
	 * @param value = the number to display
	 */
	public static void debugLine(double value)
	{
		debugLine(new Double(value).toString());
	}

	/**
	 * Increases the level of indent
	 */
	public static void increaseIndent()
	{
		indentS += " ";
	}

	/**
	 * Decreases the level of indent
	 */
	public static void decreaseIndent()
	{
		if (indentS.length()==0) 
		    return;
		
		indentS = indentS.substring(0, indentS.length()-1);
	}

    /**
     * Prints the current execution stack trace, the list of 
     * functions that have been called up to the current one.
     * @param message = The message to display before the stack trace
     */
    public static void displayStackTrace(String message)
    {
        new Exception(message).printStackTrace();           
    }
}







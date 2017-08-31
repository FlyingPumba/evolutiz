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

import java.util.*;
import java.text.MessageFormat;


/**Class used to read and display error strings from a resource bundle*/
public class Errors
{
    /**returns the localised error string for a specific code
    @param errorCode = the code for the error string
    @return the string containing the text*/
    public static String getErrorText(int errorCode)
    {
        String text = "";
        
        ResourceBundle bundle = ResourceBundle.getBundle("com.addi.resourcebundles.ErrorBundle");
    
        text = bundle.getString(Integer.toString(errorCode));

        return text;
    }

    /**throws an exception with a localised error string
       @param errorCode = the code for the error string*/
    public static void throwMathLibException(int errorCode)
    {
    	String text = getErrorText(errorCode);
        throw (new JMathLibException(text));
    	
    }

    /**Throws an exception with an error string. This methode should only
       be used by external functions. Internal classes should use localised
       error messages
       @param errorCode = the error message*/
    public static void throwMathLibException(String errorMessage)
    {
        throw (new JMathLibException("ERROR: "+errorMessage));
    }

    /**
     * Throws an exception with an error string. This method should only
     * be used by Parser.java
     * @param errorMessage = the parser error
     */
    public static void throwParserException(String errorMessage)
    {
        throw (new JMathLibException("PARSER: "+errorMessage));
    }

    
    /**
     * Throws an exception with an error string. This method should only
     * be used by Usage.java
     * @param errorMessage = the parser error
     */
    public static void throwUsageException(String errorMessage)
    {
        throw (new JMathLibException("USAGE: "+errorMessage));
    }

    /**returns the localised error string for a specific code, 
    containing extra data fields
    @param errorCode = the code for the error string
    @param params    = the extra data
    @return the string containing the text*/
    public static String getErrorText(int errorCode, Object[] params)
    {
        String text = "";
        ResourceBundle bundle = ResourceBundle.getBundle("com.addi.resourcebundles.ErrorBundle");
        text = MessageFormat.format(bundle.getString(Integer.toString(errorCode)), params);

        return text;
    }

    /**throws an exception with a localised error string,
    containing extra data fields
    @param errorCode = the code for the error string
    @param params    = the extra data*/
    public static void throwMathLibException(int errorCode, Object[] params)
    {
    	String text = getErrorText(errorCode, params);
        throw (new JMathLibException(text));
    	
    }
}

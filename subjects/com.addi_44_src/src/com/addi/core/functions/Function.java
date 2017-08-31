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

package com.addi.core.functions;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.FunctionToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**This is the base class for all functions*/
abstract public class Function extends RootObject
{
	/**The functions name*/
	protected String name;

    /** path and filename where this function is located on the disc*/
    protected String pathAndFileName;
    
    /** data and time of last modification of this file on disc*/
    protected long lastModified;

    /**Number of left-hand arguments (e.g. [a,b,c]=some_functions() ) */
	private int nargout = 0;

	/**Default constructor - Creates a function with a null name*/
	public Function()
	{
		name = "";
	}

	/**Creates a function called _name
	 * @param _name = the name of the function
	 */
	public Function(String _name)
	{
		name = _name;
	}

    /**
     * @return the name of the function
     */
    public String getName()
    {
        return name;
    }

    /**
     * sets the path and filename that belongs to this functions 
     * @param _pathAndFileName
     */
    public void setPathAndFileName(String _pathAndFileName)
    {
        pathAndFileName = _pathAndFileName;
    }  

    /**
     * returns the path and filename that belongs to this functions 
     * @return path and filename
     */ 
    public String getPathAndFileName()
    {
        return pathAndFileName;
    }

    /**
     * Set the date of last modification of this function
     * @param _lastModified
     */
    public void setLastModified(long _lastModified)
    {
        lastModified = _lastModified;
    }  

    /**
     * Returns the date of last modification of this function
     * @return date of last modification
     */
    public long getLastModified()
    {
        return lastModified;
    }

    /**Sets the name of this function
     * @param name = name of this function 
     */
	public void setName(String _name)
	{
	    name = _name;
	}  
 
	/**Returns the number of left-hand arguments of the function 
	 * (e.g.) [a,b,c]=some_function will return "3"              
	 * @return the number of left hand arguments
	 */
	public int getNoOfLeftHandArguments()
	{
		return nargout;
	}

	/**Returns the number of left-hand arguments of the function 
	 * (e.g.) [a,b,c]=some_function will return "3"              
	 * @return the number of left hand arguments
	 */
	protected int getNArgOut()
	{
		return nargout;
	}

	/**Returns the number of right-hand arguments of the function
     * (e.g.: a=some_function(a,b,c,d) will return "4"
     * @return number of right hand arguments. Returns -1 if there
     *         are no right hand arguments. Returns also -1 if there
     *         if one argument==null  
     */
	protected int getNArgIn(Token[] operands)
	{
		// Check if there are arguments at all
		if (operands == null)
			return -1;

		return  operands.length;
	}

	/**Sets the number of left-hand arguments of the function    
	 * (e.g.) [a,b,c]=some_function will return set a "3"        
     * @param _number = the number of left hand arguments
     */
	public void setNoOfLeftHandArguments(int _number)
	{
		nargout = _number;
	}


	/**Tests if an object is equal to this function
     * if obj is an instance of Function or Function token then it
     * compares the name of obj to the functions name otherwise it
     * calls the superclasses version
     * @param obj = object to test
     * @return true if the objects are equal
     */
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if(obj instanceof Function)
		{
			equal = ((Function)obj).getName() == name;
		}
		else if(obj instanceof FunctionToken)
		{
		    equal = ((FunctionToken)obj).getName() == name;
		}
		else
		{
		    equal = super.equals(obj);
		}
		return equal;
	}

	/**Executes the function
	 * @param operands - an array of the functions paramaters
	 * @param globals TODO
	 * @return the result as an OperandToken
	 */
	abstract public OperandToken evaluate(Token[] operands, GlobalValues globals);

    /**Throws arithmetic exception for external functions
     * @param error text to display
     * */
    public void throwMathLibException(String errorMessage)
    {
        Errors.throwMathLibException(errorMessage);
    }
    
    /** write debug line to std-out and file
     * @param s
     */
    public void debugLine(String s)
    {
        ErrorLogger.debugLine(s);
    }
    
}

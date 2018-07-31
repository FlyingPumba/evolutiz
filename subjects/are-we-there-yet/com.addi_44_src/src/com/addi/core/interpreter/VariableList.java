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
import java.io.*;

import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**Class used to store all the declared variables within a context*/
public class VariableList implements Cloneable, java.io.Serializable 
{
    /**The actual list of variables*/
    private HashMap variables;

    /**Sets up the ArrayList of variables*/
    public VariableList()
    {
        variables = new HashMap();
    }

    /**Create a duplicate of a VariableList
	@param parent = the previous variable context*/
    public VariableList(VariableList _variables)
    {
        variables = new HashMap();
        
        Iterator list = _variables.getIterator();
        
        while(list.hasNext())
        {
	        Map.Entry next = ((Map.Entry)list.next());
	        String name    = ((String)next.getKey());
	        Variable var   = ((Variable)next.getValue());
	        variables.put(name, var.clone());
        }
    }

    /**@return an iterator for the key/value pairs of the list*/
    public Iterator getIterator()
    {
        return variables.entrySet().iterator();
    }

    /**@return the number of variables*/
    public int getSize()
    {
        return variables.size();
    }

    /**Remove a variable from the list
       @param variable-string = the variable to remove*/
    public void remove(String name)
    {
        variables.remove(name);
    }

    /**remove all variables from the list*/
    public void clear()
    {
        variables = new HashMap();
    }

     /**@param name  = the name of the variable
	@param value = the value to set it to*/
    public void setVariable(String name, OperandToken value)
    {
        ((Variable)variables.get(name)).assign(value);
    }


     /**@param name = the name of the variable
	@return the variable represented with the name name*/
    public Variable getVariable(String name)
    {
        return ((Variable)variables.get(name));
    }

    /**Check wether or not a variable with the given name exists.
    @param name = the name of a variable */
    public boolean isVariable(String name)
    {
    	//if i or j, just return true, if it doesn't exist, we will make it look like it does later
    	if ((name.compareTo("i") == 0) || (name.compareTo("j") == 0))
    		return true;
    	
        return variables.containsKey(name);
    }


    /**Lists all the currently declared variables to the console and a log file*/
    public void listVariables()
    {
        ErrorLogger.debugLine("listing variables");
        Iterator iter = getIterator();
        Variable var;
        while(iter.hasNext())
        {
            Map.Entry next = ((Map.Entry)iter.next());
            var = ((Variable)next.getValue());
            ErrorLogger.debugLine(var.getName()); //toString());
        }
        ErrorLogger.debugLine("------------------------------");
    }
	
    /**creates a variable, if it doesn't already exist.
       it returns the created variable
       @param name = the name of the variable to create
       @return the variable with that name*/
    public Variable createVariable(String name)
    {
        Variable newVariable = null;
        if(!variables.containsKey(name))
        {
            newVariable = new Variable(name);
            variables.put(name, newVariable);
        }
        else
        {
            newVariable = ((Variable)variables.get(name));
        }
        
        
        return newVariable;
    }


    /**saves the list of variables
     @param fileName = the name of the file to save to*/
    public void saveVariables(String fileName)
    {
        try
        {    	
            //create streams
            FileOutputStream output = new FileOutputStream(fileName);
            
            //create object stream
            ObjectOutputStream objectOutput = new ObjectOutputStream(output);
            
            objectOutput.writeObject(variables);
            
            //close output objects	    	
            objectOutput.close();
            output.close();
        }
        catch(java.io.IOException except)
        {
            Errors.throwMathLibException("VariableList: IO exception");
            ErrorLogger.debugLine(except.getMessage());
            //except.printStackTrace();
        }
    }
	
    /**loads the list of variables
     @param fileName = the name of the file to load from*/
    public void loadVariables(String fileName)
    {        
        try
        {            
            FileInputStream input = new FileInputStream(fileName);
            
            ObjectInputStream objectInput   = new ObjectInputStream(input);
            
            try
            {
                variables = ((HashMap)objectInput.readObject());
            }
            catch(java.lang.ClassNotFoundException except)
            {
                Errors.throwMathLibException("VariableList: Class not found exception");
                
                //close input objects
                objectInput.close();
                input.close();	    	
            }
            catch(java.lang.ClassCastException except)
            {
                Errors.throwMathLibException("VariableList: Class cast exception");
                
                //close input objects
                objectInput.close();
                input.close();	    	
            }
            
            //close input objects
            objectInput.close();
            input.close();	    	
        }
        catch(java.io.IOException except)
        {
            Errors.throwMathLibException("VariableList: IO exception");
            ErrorLogger.debugLine(except.getMessage());
        }
    }
    
    /*saves the list of variables when app paused*/
    public void saveVariablesOnPause(FileOutputStream output)
    {
        try
        {    	
            //create object stream
            ObjectOutputStream objectOutput = new ObjectOutputStream(output);
            
            objectOutput.writeObject(variables);
            
            //close output objects	    	
            objectOutput.close();
            output.close();
        }
        catch(java.io.IOException except)
        {
        }
    }
	
    /**loads the list of variables
     @param fileName = the name of the file to load from*/
    public void loadVariablesOnCreate(FileInputStream input)
    {        
        try
        {             
            ObjectInputStream objectInput   = new ObjectInputStream(input);
            
            try
            {
                variables = ((HashMap)objectInput.readObject());
            }
            catch(java.lang.ClassNotFoundException except)
            {
            }
            catch(java.lang.ClassCastException except)
            {	    	
            }
            
            //close input objects
            objectInput.close();
            input.close();	    	
        }
        catch(java.io.IOException except)
        {
        }
    }
    
    /**Create a duplicate of this Variable List*/
    public Object clone()
    {
        return new VariableList(this);
    }   
}

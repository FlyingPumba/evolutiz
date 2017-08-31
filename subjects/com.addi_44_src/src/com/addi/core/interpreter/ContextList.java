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

import com.addi.core.tokens.OperandToken;


/**Stores a list of contexts. A context is a set of variables pluss some code.
   When a function is called a new context is added to the list. When the function is removed, the context is destroyed. */
public class ContextList
{
    // reference to the currently executing context
    private Context localContext;
    
    // reference to the global context
    private Context globalContext;
    
    /**Create an empty context to act as the global context
     * This constructor must only be called ONCE, because it also creates the
     * GLOBAL context
     */
    public ContextList()
    {
        globalContext  = new Context(); // global context 
        localContext   = new Context(); // local context
    }

    /**
     * 
     * @param context
     */
    public void pushContext(Context context)
    {
        context.setParent(localContext);
        localContext = context; 
    }

    /** Used to create a new context and put it on the stack when executing a function
     * @param _variables = the variable list of the function, null if it's a script file
     */
    public Context createContext(VariableList _variables)
    {
        //check if this is a script file
        if(_variables == null)
        {
            //use the calling functions variable list
            _variables = localContext.getVariables();
        }
                   
        //create a new context and make it the current context
        //localContext = new Context(_variables, null, localContext);
        localContext = new Context(_variables, localContext);

        return localContext;
    }
    
    /**
     * return to the calling context of the function
     */
    public Context popContext()
    {       
        Context context = localContext;
        localContext    = localContext.getParent();
        return context; 
    }

    /**
     * @return the variable list of the current context
     */
    public VariableList getLocalVariables()
    {
        return localContext.getVariables();
    }

    /**
     * @return the variable list of the global context
     */
    public VariableList getGlobalVariables()
    {
        return globalContext.getVariables();
    }

    /**
     * this method returns a variable from the workspace. It also checks
     * if the variable is local or global and returns the value from
     * the correct local or global workspace.
     * @param name
     * @return
     */
    public Variable getVariable(String name)
    {
        // check if variable is marked as global in current context
        Variable var = localContext.getVariables().getVariable(name);
        if ((var!=null) && (var.isGlobal()))
        {
            // variable is marked global
            // return data from global context
            return globalContext.getVariables().getVariable(name);
        }
        else
        {
            // variable is local only
            // return data from current context
            return var;
        }
    }
    
    /**
     * create a variable in the local or global workspace
     * @param
     * @return
     */
    public Variable createVariable(String name)
    {
        // check if variable is already created in local or global workspace
        Variable var = getVariable(name);
        
        // return if variable is already created
        if (getVariable(name)!=null)
            return var;
        
        // create a local variable
        return getLocalVariables().createVariable(name);
        
    }

    /**
     * Set a variable in the local or global workspace. In case
     * the variable is not yet created, create it, too.
     * @param
     * @param
     */
    public void setVariable(String name, OperandToken value)
    {
        // try to get variable from local or global context
        Variable var = getVariable(name);
        
        // if variable is not yet available, create it
        if (var==null)
            createVariable(name);
     
        // assign value to newly created variable
        getVariable(name).assign(value);
    }

    /**
     * @return true if the current context is the global one
     */
    public boolean isGlobalContext()
    {
        return (localContext.getParent() == null);
    }

    /**
     * 
     * @return
     */
    public Context getLocalContext()
    {
        return localContext;
    }
}

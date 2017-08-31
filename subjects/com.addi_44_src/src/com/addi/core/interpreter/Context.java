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


/**A context object contains the variables and code for the executing function*/
public class Context implements java.io.Serializable 
{
     /**Reference to the contexts variables*/
     private VariableList variables;
        
     /**Reference to the contexts calling context*/
     private Context parent;
    

     /**Create a Context with an empty variable list, used to construct the global context*/        
     public Context()
     {
         variables    = new VariableList();
         parent       = null;
     }

     /**Create a Context with the supplied values
      * @param _variables = the variable list of the new context
      * @param _parent    = the calling context
      */
     public Context(VariableList _variables, Context _parent)
     {
         variables    = _variables;
         parent       = _parent;
     }
        
     /**
      * 
      * @return
      */
     public Context getParent()
     {
         return parent;
     }

     /**
      * 
      * @param _parent
      */
     public void setParent(Context _parent)
     {
         parent = _parent;
     }

     /**
      * 
      * @return
      */
     public VariableList getVariables()
     {
         return variables;
     }

     /**
      * 
      */
     public Object clone()
     {
         VariableList _variables = null;
         if(variables != null)
             _variables = ((VariableList)variables.clone());

         //Context context = new Context(_variables, _code, null);
         Context context = new Context(_variables, null);
         //context.setFunctionName(functionName);
         return context;
     }

}

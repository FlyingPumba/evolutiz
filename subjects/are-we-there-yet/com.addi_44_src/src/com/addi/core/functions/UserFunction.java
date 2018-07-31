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


import java.util.ArrayList;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**Class for storing user defines*/
public class UserFunction extends Function
{
    
    /** The code of the current m-function */
    private OperandToken code;
    
    /**The names of the parameter values*/
    private ArrayList parameterVariables;

    /**The names of the return values*/
    private ArrayList returnVariables;

    /**true if this is a M-Script (not a M-function) */
    private boolean mScriptB = false;

    /**Creates a user function */	
    public UserFunction() 
    {
        parameterVariables = new ArrayList();
        returnVariables    = new ArrayList();
    }
    
    /**Executes a user function
     * @param operands - the array of parameters
     * @param globals
     * @return the result of the function as an OperandToken
     * */	
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        Context      functionContext;     // The context of the function
        OperandToken result     = null;
        boolean      vararginB  = false;
        boolean      varargoutB = false;
        
        if (mScriptB)
        {
            // evaluate m-script
            if (code!=null)
                result = code.evaluate(null, globals);
        }
        else
        {
            // evaluate m-function

            // m-functions have a local context (local variables)
            VariableList localVariables = new VariableList();
            functionContext = globals.getContextList().createContext(localVariables);

            //if nescessary create a context and store on list
	        //if(activeContext == null)
	        //    activeContext = ((Context)functionContext.clone());
	
	        //getContextList().pushContext(activeContext);
	        
	        //set the variable NARGIN to the number of arguments
	        int opLength = 0;
	        if (operands!=null)
	            opLength = operands.length;
	        
            // check for "varargin" as last input parameter
            // e.g. function x=barfoo(a,b,c,varargin)
            if (parameterVariables.size()>0)
            {
                if ( ((String)parameterVariables.get(parameterVariables.size()-1)).equals("varargin") )
                {
                    vararginB = true;
                    ErrorLogger.debugLine("UserF: varargin found");   
                }
            }                
            
            // check for "varargout" as last input parameter
            // e.g. function [x,y,varargout]=barfoo(a,b,c)
            if (returnVariables.size()>0)
            {
                if ( ((String)returnVariables.get(returnVariables.size()-1)).equals("varargout") )
                {
                    varargoutB = true;
                    ErrorLogger.debugLine("UserF: varargout found");   
                }
            }
            
	        // check if number of parameters inside the function is equal to the number
	        //    of calling expression  
	        // e.g. plot(x,y,z)  
	        //      function [...]=plot(x,y,z)
	        if ( (parameterVariables.size() < opLength) && !vararginB )
	            Errors.throwMathLibException("UserFunction: "+name+" number of parameters to large"); 
	        
	        // set the variable NARGIN to the number of parameters of the calling function
	        globals.createVariable("nargin").assign(new DoubleNumberToken(opLength));

	        // set the variable NARGOUT to the number of return values
	        globals.createVariable("nargout").assign(new DoubleNumberToken(returnVariables.size()));
	
	
            //set the input parameters for the function
	        // e.g. function x=barfoo(x,y,z)
            // e.g. function x=barfoo(x,y,z,varargin)
            if (!vararginB)
            {
                // e.g. function =bar(a,b,c,d,e)
    	        for(int paramNo = 0; paramNo < opLength; paramNo++)
    	        {
    	            String parameterName = (String)parameterVariables.get(paramNo);
    		        //System.out.println("UserFunction: "+parameterName);
    	            globals.createVariable(parameterName).assign((OperandToken)operands[paramNo]);
    	        }
            }
            else
            {
                //e.g. function =bar(a,b,c,varargin)
                int parN         = parameterVariables.size();
                int remainingOps = opLength - (parN - 1);

                // copy parameters, but not "varargin"  (copy parameters 0 ... n-1)
                for(int paramNo = 0; paramNo < Math.min((parN - 1), opLength); paramNo++)
                {
                    String parameterName = (String)parameterVariables.get(paramNo);
                    ErrorLogger.debugLine("UserF: params: "+parameterName);
                    globals.createVariable(parameterName).assign((OperandToken)operands[paramNo]);
                }
                
                ErrorLogger.debugLine("UserF: remainingOps: "+ remainingOps);
                

                // copy remaining operands into cell array, but only if operands are left
                // e.g. function x=barfoo(a,b,c,d,varargin)   
                //    with barfoo(1,2,3) will have varargin==null;
                if (remainingOps >0)
                {
                    OperandToken[][] values       = new OperandToken[remainingOps][1];
                    for (int i=0; i<remainingOps; i++)
                    {
                        values[i][0] = (OperandToken)operands[parameterVariables.size()-1+i];
                    }
                    CellArrayToken cell = new CellArrayToken(values);
                    globals.createVariable("varargin").assign(cell);
                }
                else
                {
                    // varargin is empty
                    CellArrayToken cell = new CellArrayToken();
                    globals.createVariable("varargin").assign(cell);
                }
            }
            
	        
            // execute m-function
	        try
	        {
	            // must clone function code, so that the original code remains untouched
	            OperandToken codeLocal = (OperandToken)code.clone();
	            result = codeLocal.evaluate(null, globals);
	            
	            
	            // result should be DoubleNumberToken e.g. 1+2             ->  >3<  
	            // or a MatrixToken             e.g. [x,y]=foo(2,4)  ->  [x,y]
	            if(returnVariables.size() == 1)
	            {
	                String name  = (String)returnVariables.get(0);
	                //System.out.println("UserFunction: returnVariable "+name);
	                //Variable var = (Variable)getVariables().getVariable(name);
                    Variable var = globals.getVariable(name);
	                result       = var.getData();
	            }
	            else if (returnVariables.size() > 1)
	            {
	                
	                // for more than one return argument, return a matrix of operands
	                // e.g.  function [t,y]=foo(...)
	                OperandToken[][] values = new OperandToken[1][returnVariables.size()];           

	                for(int i = 0; i < returnVariables.size(); i++)
	                {
	                      String name  = (String)returnVariables.get(i);
	                      //Variable retVar = ((Variable)getVariables().getVariable(name));
                          Variable retVar = globals.getVariable(name);
	                      // check if return variable has been used before
	                      if (retVar != null)
	                      	  values[0][i] = retVar.getData();
	                      else
	                      	  values[0][i] = null;
	                }
	                    
	                result = new MatrixToken(values);
	            }        
	                
            
	            //reset to the previous variable frame
	            globals.getContextList().popContext();
	        }
	        catch(ControlException e)
	        {
                    // assign return values
	            globals.getContextList().popContext();
	                //activeContext = null;
	        }
            catch (Exception e)
            {
                globals.getContextList().popContext();
                throwMathLibException(e.getMessage());
            }

        }
        
        
         
        return result;
    }
    
    /**Tests if an object is equal to this function
       if obj is an instance of Function or Function token then it
       compares the name of obj to the functions name otherwise it
       calls the superclasses version
       @param obj = object to test
       @return true if the objects are equal*/
    public boolean equals(Object obj)
    {
        boolean equal = false;

        if(obj instanceof Function)
        {
            equal = ((Function)obj).getName().toUpperCase().equals(name.toUpperCase());
        }
        else if(obj instanceof FunctionToken)
        {
            equal = ((FunctionToken)obj).getName().toUpperCase().equals(name.toUpperCase());
        }
        else
            equal = super.equals(obj);
        return equal;
    }
	
    /**
     * 
     * @param _parameterVariables
     */
    public void setParameterVariables(ArrayList _parameterVariables)
    {
        //getLocalVariables().createVariable(name);
        parameterVariables = _parameterVariables;
    }

    /**
     * 
     * @param _returnVariables
     */
    public void setReturnVariables(ArrayList _returnVariables)
    {
        //getLocalVariables().createVariable(name);
        returnVariables = _returnVariables;
    }

    /**Sets the parsed code as a token tree
     *  @param code = parsed code of this function 
     */
    public void setCode(OperandToken _code)
    {
        code = _code;
    }

    /** set true if this is a m-script file and not a function 
     * @param
     */
    public void setScript(boolean _mScriptB)
    {
        mScriptB = _mScriptB;
    }
    
    /**  returns true if this is a m-script file 
     * @return
     */
    public boolean isScript()
    {
        return mScriptB;
    }
      
}

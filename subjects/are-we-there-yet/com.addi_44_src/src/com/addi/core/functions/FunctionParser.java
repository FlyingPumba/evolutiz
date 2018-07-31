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

import com.addi.core.constants.TokenConstants;
import com.addi.core.constants.*;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;


/**A class for parsing user functions. M-files contain either functions or they
   are script files. Script files are just a couple of commands which are typed
   into a text file*/
public class FunctionParser extends RootObject implements TokenConstants, ErrorCodes
{
    
    /**default constructor - creates the FunctionParser object with an empty expression string*/
    public FunctionParser()
    {
    }

    /**Parse a user function
    @param expression the function as a string
    @return the UserFunction object created*/
    public UserFunction parseFunction(String expr) 
    {
        boolean foundMFunction = false;

        ErrorLogger.debugLine("FunctionParser: parseFunction");
        
        // create a new instance of a user function
        //this creates a new context and pushes it on the stack
        UserFunction function = new UserFunction(); 
        
        // create an instance of a parser 
        Parser p = new Parser();
        p.setExpression(expr);
        
        // check if the m-file starts with the keyword "function" or not
        // e.g.
        // function y=some_name(x)
        // y = sin(x)+3;
        //
        Token t = p.peekNextToken();
        if (t instanceof VariableToken)
        {
            if (((VariableToken)t).getName().toLowerCase().equals("function"))
            {
                // the m-file is a function file
                ErrorLogger.debugLine("FunctionParser: found m-function");
                t = p.getNextToken();
                foundMFunction = true;
            }
        }
        
        // parse m-function- of m-script-file
        if (foundMFunction)
        {
            // parse m-function file
            // Possible syntax of function headers are:
            // (e.g.  a     = foo(b)   )
            // (e.g.  [a,b] = foo(c)   )
            // (e.g.  [a,b] = foo(c,d) )
            // (e.g.          foo(a)   )
            // (e.g.  a     = foo()    )
        
            // check for left hand side arguments 
            // (e.g. [a,b,c]=foo(x) )
            // (e.g.: a     =foo(x) )
            //ArrayList names = new ArrayList();
            ArrayList returnVariables    = new ArrayList();
            ArrayList parameterVariables = new ArrayList();
            
            t = p.peekNextToken();
            if (t instanceof VariableToken)
            {  
                //single return argument
                t = p.getNextToken();
                
                String retVariable = ((VariableToken)t).getName();
                
                //function.getLocalVariables().createVariable(retVariable);           
                //returnCount = 1;
                ErrorLogger.debugLine("FunctionParser: function: 1 return value: "+retVariable); 
                returnVariables.add(retVariable);
                //names.add(retVariable);
            }
            else if (t instanceof DelimiterToken)
            {
                if ( ((DelimiterToken)t).value != '[' )
                    Errors.throwMathLibException("FunctionParser: missing [");
                t = p.getNextToken();
          
                // parse return variables
                while(true)
                {
                    t = p.getNextToken();
               
                    if (t instanceof VariableToken)
                    {
                        // variable token is a return value of the function
                        String        parameter = ((VariableToken)t).getName();

                        // check if return name is unique
                        if (returnVariables.contains(parameter))
                            Errors.throwMathLibException("FunctionParser: return parameter "+parameter+" not unique");

                        // add parameter to list of parameters
                        returnVariables.add(parameter);
                    }    
                    else if (t instanceof DelimiterToken)
                    {
                        if ( ((DelimiterToken)t).value == ']' )
                        {
                            // closing ']' bracket
                            break;
                        }   
                        else if ( ((DelimiterToken)t).value == ',' )
                        {   
                            // delimiter between arguments
                            //check for alternating variable token and delimiter
                        }
                        else
                        Errors.throwMathLibException("FunctionParser: wrong delimiter");
                    }
                    else
                        Errors.throwMathLibException("FunctionParser: wrong return");
                
                } // end while   

            }
            else
            {
                // no return value
                ErrorLogger.debugLine("FunctionParser: no return value");
            }
         
            // check for assignment operator (e.g. y=sin(x))
            t = p.peekNextToken();
            if (t instanceof AssignmentOperatorToken)
            {
                //found a "=" token
                t = p.getNextToken();
                ErrorLogger.debugLine("FunctionParser: found = token");
            }
            
            
            // check for the name of this function
            t = p.peekNextToken();
            if (t instanceof FunctionToken) 
            {
                //found the name of this function 
                t = p.getNextToken();
                String functionName = ((FunctionToken)t).getName();
                ErrorLogger.debugLine("FunctionParser: function name: "+functionName);

                //set the name of the parsed function
                function.setName(functionName);
            }
            else if (t instanceof VariableToken) 
            {
                // e.g. y = hallo(x)
                //found the name of this function 
                t = p.getNextToken();
                String functionName = ((VariableToken)t).getName();
                ErrorLogger.debugLine("FunctionParser: function name: "+functionName);

                //set the name of the parsed function
                function.setName(functionName);
            }
            else
            {
                // did not find a function name. Since there was the keyword "function" there
                //   must be a name
                Errors.throwMathLibException("FunctionParser: no function name, but"+t.toString());
            }
         
         
            // get "("  (e.g. a=foo"("b)  )
            t = p.getNextToken();
            if (!(t instanceof DelimiterToken))
            {
                // throw exception
                Errors.throwMathLibException("FunctionParser: not ( , but "+t.toString());
            }    
                
            // check for right hand side arguments (e.g. a=foo("x,y,z")  )
            ErrorLogger.debugLine("FunctionParser: reading right hand side arguments");
            while (true)
            {
                t = p.getNextToken();
                
                if (t instanceof VariableToken)
                {
                    // variable token is a parameter of the function
                    String        parameter    = ((VariableToken)t).getName();
                    ErrorLogger.debugLine("FunctionParser: parameter: "+parameter);
              
                    // check if parameter name is unique
                    if (parameterVariables.contains(parameter))
                        Errors.throwMathLibException("FunctionParser: calling parameter "+parameter+" not unique");
              
                    // add parameter to list of parameters
                    parameterVariables.add(parameter);
                }  
                else if (t instanceof DelimiterToken)
                {  
                    if ( ((DelimiterToken)t).value == ')' )
                    {
                        // closing ')' bracket
                        break;
                    }
                    else if ( ((DelimiterToken)t).value == ',' )
                    { 
                        // delimiter between arguments
                        //check for alternating variable token and delimiter
                    }
                    else
                        Errors.throwMathLibException("FunctionParser: wrong delimiter");
                }
                else
                    Errors.throwMathLibException("FunctionParser: wrong argument");
         
         
            } // end parameters
            
            // set return values
            function.setReturnVariables( returnVariables );
            
            // set parameters
            function.setParameterVariables( parameterVariables );
            
            // parse the body of the function and store the parsed code             
            OperandToken code  = p.parseRemainingExpression();  
            function.setCode(code);
        }
        else
        {
            // the current function is a m-script file
            
            // parse m-script file
            ErrorLogger.debugLine("FuntionParser: m-script file");
         
            // set function name to "_scriptFile" so that it is not stored as a function
            function.setScript(true);    
         
            // parse the body of the m-script                
            OperandToken code  = p.parseRemainingExpression();     
            function.setCode(code);
        }
        
        return function;
    
    } // end parseFunction
    
}

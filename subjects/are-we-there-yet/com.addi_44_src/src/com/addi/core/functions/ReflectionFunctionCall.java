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

//import MathLib.Tokens.FunctionToken;
//import MathLib.Tokens.VariableToken;
//import MathLib.Tokens.StringToken;
//import MathLib.Tokens.Expression;

//import java.applet.*;
//import java.util.Vector;
//import java.io.*;
//import java.net.*;
import java.lang.reflect.*;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;

/**Class for call a function from an external class using reflection*/
public class ReflectionFunctionCall extends ExternalFunction
{
    private Class externalClass;
    
    /**creates a reflection function call containing the class being called*/
    public ReflectionFunctionCall(Class _externalClass, String functionName)
    {
        super(functionName);
        
        externalClass = _externalClass;
    }
    
    public OperandToken evaluate(Token []operands, GlobalValues globals)
    {
        boolean found = false;
        ErrorLogger.debugLine("evaluating reflection function");
        Method[] methodList = externalClass.getMethods();
        
        ErrorLogger.debugLine(name);
        for(int methodNo = 0; methodNo < methodList.length && !found; methodNo++)
        {
            String funcName = methodList[methodNo].toString();
            funcName = funcName.substring(0, funcName.indexOf("(") );
            funcName = funcName.substring( funcName.lastIndexOf(".") + 1);
            ErrorLogger.debugLine("method " + methodNo + " = " + funcName);
            
            if(funcName.equalsIgnoreCase(name))
            {
                ErrorLogger.debugLine("found method*********************************************");                
                evaluateMethod(methodList[methodNo]);
                found = true;
            }
        }
        
        return null;
    }
    
    private void evaluateMethod(Method calledMethod)
    {
        Class[] paramaterList = calledMethod.getParameterTypes();

        for(int paramaterNo = 0; paramaterNo < paramaterList.length; paramaterNo++)
        {
            ErrorLogger.debugLine("method " + paramaterNo + " = " + paramaterList[paramaterNo].toString());            
        }                
    }
    
    public String toString()
    {
        return "reflection object";
    }
}

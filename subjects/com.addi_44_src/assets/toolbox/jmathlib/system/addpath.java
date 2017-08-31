package com.addi.toolbox.jmathlib.system;

import java.io.File;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.functions.FileFunctionLoader;
import com.addi.core.functions.FunctionLoader;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;



/**External function to add an item to the search path*/
public class addpath extends ExternalFunction
{
    /**adds an item to the search path
    @param operands[0] = item to add*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        if (getNArgIn(operands)!=1)
            throwMathLibException("addpath: number of arguments != 1");

        boolean prepend = true;
        
		for(int index = 0; index < operands.length; index++)
		{
            
            // check if operand is of type char token
            if (!(operands[index] instanceof CharToken))
                throwMathLibException("addpath: parameter "+index+" is not a char array");
            
            String path = ((CharToken)operands[index]).getElementString(0);
            
            if(path.equalsIgnoreCase("end") || path.equals("1"))
                prepend = false;
            else if(path.equalsIgnoreCase("begin") || path.equals("0"))
                prepend = true;
        }
               
		for(int index = 0; index < operands.length; index++)
		{
            String path = ((CharToken)operands[index]).getElementString(0);
            if(!(path.equalsIgnoreCase("end") || path.equals("1") || path.equalsIgnoreCase("begin") || path.equals("0")))
            {
                FunctionLoader loader = new FileFunctionLoader(new File(path), true);
                if (!prepend)
                    globals.getFunctionManager().addFunctionLoader(loader);
                else globals.getFunctionManager().addFunctionLoaderAt(0, loader);
            }
        }
            
        return DoubleNumberToken.one;
    }
}

/*
@GROUP
system
@SYNTAX
addpath(path)
@DOC
Adds path to the current search path.
@NOTES
@EXAMPLES
addpath("../newpath")
@SEE
path, rmpath
*/


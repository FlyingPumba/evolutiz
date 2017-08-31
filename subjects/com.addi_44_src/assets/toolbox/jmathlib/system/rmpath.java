/* 
 * This file is part or JMathLib 
 * 
 * Check it out at http://www.jmathlib.de
 *
 * Author:  
 * (c) 2005-2009   
 */
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

/**External function to remove an item from the search path*/
public class rmpath extends ExternalFunction
{
    /**removes an item from the search path
    @param operands[0] = item to remove*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        if (getNArgIn(operands)!=1)
            throwMathLibException("rmpath: number of arguments != 1");
        
        if ((operands[0] instanceof CharToken))
            throwMathLibException("rmpath: works only on char arrays");

        File path = new File(((CharToken)operands[0]).getElementString(0));
        
        for (int i=0;i<globals.getFunctionManager().getFunctionLoaderCount();i++) 
        {
            FunctionLoader loader = globals.getFunctionManager().getFunctionLoader(i);
            
            if (loader instanceof FileFunctionLoader) 
            {
                FileFunctionLoader ffl = (FileFunctionLoader)loader;
                                    
                if (ffl.getBaseDirectory().compareTo(path) == 0) 
                {
                    globals.getFunctionManager().removeFunctionLoader(loader);
                    break;
                }
            }
        }
        
        return DoubleNumberToken.one;
            
    }
}

/*
@GROUP
system
@SYNTAX
rmpath(path)
@DOC
Removes path from the current search path
@NOTES
Using rmpath will stop the system from finding any external functions
and M-files stored within the path.
@EXAMPLES
rmpath("./Functions/General")
@SEE
addpath, path
*/


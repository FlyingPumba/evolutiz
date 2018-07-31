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

package com.addi.toolbox.general;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.interpreter.Variable;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.VariableToken;


/**An external function which checks if the argument is numeric*/
public class global extends ExternalFunction
{
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

        if (getNArgIn(operands) != 1)
			throwMathLibException("global: number of arguments != 1");
            
        if (!(operands[0] instanceof VariableToken))
            throwMathLibException("global: argument must be variable");
            
        VariableToken var  = (VariableToken)operands[0];
        String        name = var.getName();
        
        debugLine("global "+operands[0].toString());
        
        debugLine("global: local  variable:"+globals.getLocalVariables().isVariable(var.getName()));
        debugLine("global: global variable:"+globals.getGlobalVariables().isVariable(var.getName()));

        // this is the procedure for global variables:
        // - normally all variables a local to each workspace
        // - the "global" workspace is a totally separate workspace
        // - each workspace which defines a variable as global will
        //   use the "global" workspace instead of its own local workspace
        // - in case a variable is defined as "global" in one workspace and
        //   "local" in another workspace, the local workspace will work
        //   with the "local" variable whereas the "global-one" will work
        //   with the "global" variable
        //   -> ALL indidivual workspaces which like to use the "global"
        //      version of a variable need to call "global variable-name"
        
        
        // check if variable is already created in global context
        if (globals.getGlobalVariables().isVariable(name))
        {
            // variable is already created in global context

            // check if local context already contains variable
            if (globals.getLocalVariables().isVariable(name))
            {
                // variable is already created in local context
                
                // remove variable from current workspace (may delete current value)
                // create empty variable and set pointer to "global" property
                globals.getLocalVariables().remove(name);
                globals.getLocalVariables().createVariable(name);
                globals.getLocalVariables().getVariable(name).setGlobal(true);

                globals.getInterpreter().displayText("WARNING global: variable "+name+
                  " already existed in the local workspace. \n"+
                  " It has been overwritten with the value from"+
                  " the global workspace.\n" +
                  " please type: global variable\n"+
                  " before using a variable as global.");
            }
            else
            {
                // variable is not yet created in local context
                
                // create empty variable and set "global" property
                globals.getLocalVariables().createVariable(name);
                globals.getLocalVariables().getVariable(name).setGlobal(true);
                
            }
         }
        else
        {
            // variable not yet created in global context
            
            // create variable in global context (data only in global context)
            globals.getGlobalVariables().createVariable(name);
            globals.getGlobalVariables().getVariable(name).setGlobal(true);                

            // check if current context already contains variable
            if (globals.getLocalVariables().isVariable(name))
            {
                // current context already contains variable
                Variable varCurrent = globals.getLocalVariables().getVariable(name);
                globals.getGlobalVariables().getVariable(name).assign(varCurrent.getData());
                
                // remove variable, create new one and set variable to global
                globals.getLocalVariables().remove(name);
            }

        }

        // create new variable in current context and set variable to global
        globals.getLocalVariables().createVariable(name);
        globals.getLocalVariables().getVariable(name).setGlobal(true);

        debugLine("global:global var:"+name+" global="+globals.getGlobalVariables().getVariable(name).isGlobal());
        debugLine("global:local  var:"+name+" global="+globals.getLocalVariables().getVariable(name).isGlobal());
       
        
        return null;
	}
}

/*
@GROUP
general
@SYNTAX
global aaa
@DOC
Returns 1 if the first operand is a cell array, else it returns 0.
@EXAMPLES
<programlisting>
a=33;
global a

</programlisting>
@NOTES
@SEE
isglobal
*/


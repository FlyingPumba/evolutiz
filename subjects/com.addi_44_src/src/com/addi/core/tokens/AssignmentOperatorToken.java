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

package com.addi.core.tokens;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;
import com.addi.toolbox.jmathlib.matrix.*;



/**Used to implement assignment operations within an expression*/
public class AssignmentOperatorToken extends BinaryOperatorToken
{	
	public boolean returnResult = false;

    /**
     * 
     */
    public AssignmentOperatorToken()
    {
        /**call the super constructor, type defaults to ttoperator and operands to 2*/
        super('=', ASSIGN_PRIORITY);
    }

    /** evaluates the operator
     * @param operands = the operators operands
     * @return the result as and OperandToken
     */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        OperandToken result = null;

        if (operands.length != 2)
            Errors.throwMathLibException("Assignment: operands length != 2");

        ErrorLogger.debugLine("AssignmentOperatorToken: eval");

        if(operands[0] instanceof VariableToken)
        {
            // left operand is a variable
            ErrorLogger.debugLine("AssignmentOpTok: eval: op is VariableToken");
            VariableToken leftVarTok = (VariableToken)operands[0];
            
            // if leftVarTok is of type MathLibObject the next line will return
            //  a reference to the field values (e.g. a.b)
	        Variable      left       = leftVarTok.getVariable(globals);

			// check if variable already exists
			if (left == null)
			{
				ErrorLogger.debugLine("AssignmentOpTok: variable is null");
				String name      = leftVarTok.getName();      // get name of struct
				String fieldName = leftVarTok.getFieldName(); // get name of new field name
		
				if (leftVarTok.isCell())
                {
                    // e.g. a{8}=...
                    ErrorLogger.debugLine("AssignmentOpTok: new cell");
                    left = globals.createVariable(name);
                    left.assign(new CellArrayToken());
                }
                else if (!leftVarTok.isStruct())
				{
					// auto create a new variable
	               	left = globals.createVariable(name);
				}
				else if (leftVarTok.isStruct() && 
						 globals.getVariable(name) ==  null)
				{
					// struct is not created yet
					ErrorLogger.debugLine("AssignmentOpTok: new struct: "+name+"."+fieldName);
					MathLibObject obj = new MathLibObject();
					obj.setField(fieldName, DoubleNumberToken.zero);
					Variable var = globals.createVariable(name );
					var.assign(obj);
					left = ((MathLibObject)globals.getVariable(name).getData()).getFieldVariable(fieldName);
				}
				else
				{
					// struct is already created, but field is missing
					// variable is a struct -> auto create new field variable
					ErrorLogger.debugLine("AssignmentOpTok: struct new field: "+name+"."+fieldName);
					((MathLibObject)globals.getVariable(name).getData()).setField(fieldName,DoubleNumberToken.zero);
					left       = leftVarTok.getVariable(globals);
				}
			}

            
	        OperandToken right = ((OperandToken)operands[1]);

			// Check if there are limits, because this will result in
            //   a call to subassign
            // e.g. a(1,2)=6  
			if (leftVarTok.isLimited())
			{
				ErrorLogger.debugLine("AssignmentOpTok: var has limits");
				
				// create arguments for function call to subassign
                OperandToken[] limits = leftVarTok.getLimits();
				int limitsLength      = limits.length;
				OperandToken[] ops = new OperandToken[2+limitsLength];
                
                // clone limits functions/values to preserve for future evalutation
                ops[0] = left.getData();
				ops[1] = right;
                ops[2] = (OperandToken)limits[0].clone();
                ops[2] = ops[2].evaluate(null, globals);
                
				if (limitsLength==2)
				{
                    ops[3] = ((OperandToken)limits[1].clone()); 
                    ops[3] = ops[3].evaluate(null, globals);
                }

                subassign subA = new subassign();
                
                if (leftVarTok.isCell())
                {
                    ErrorLogger.debugLine("assign: left is cell");
                    subA.setLeftCell();
                }
                
				// create instance of external function SubAssign and compute assignment
                right = subA.evaluate(ops, globals);
			}

			result = left.assign(right);
            
            //Is this needed??????????????????????????????????
            /* display the result this expression in the user console*/
            if (isDisplayResult())
            {
                ErrorLogger.debugLine("AssignmentOperatorToken: displayResult");
                if ((right!=null))
                    globals.getInterpreter().displayText(left.getName() +" = "+ right.toString(globals));
                else
                    globals.getInterpreter().displayText(left.getName() +" = []");
            }
            
            if (returnResult) {
            	return result;
            } else {
            	return null;
            }
            
        }
        else if (operands[0] instanceof Expression)
        {
            ErrorLogger.debugLine("AssignmentOpTok: eval: Expression *******");
            operands[0] = operands[0].evaluate(null, globals);
            //return evaluate(operands);
            return null;
        }
        else if (operands[0] instanceof MatrixToken)
        {
            ErrorLogger.debugLine("AssignOperatorToken: eval: MatrixToken - " + operands[1].toString());
            // e.g.: [x,t]=some_function(x)
            if (operands[1] instanceof MatrixToken)
            {
		    ErrorLogger.debugLine("AssignOperatorToken: eval: MatrixToken = MatrixToken");
		    MatrixToken left  = (MatrixToken)operands[0];
		    MatrixToken right = (MatrixToken)operands[1];
		    if (   (left.getSizeX()  != right.getSizeX())
			   || (left.getSizeY()  != 1)
			   || (right.getSizeY() != 1) )
		    {
		        ErrorLogger.debugLine("AssignOperatorToken: unequal sizes");
		        return null;
		    }

		    OperandToken[][] leftOps  = left.getValue();
		    OperandToken[][] rightOps = right.getValue();

		    // assign all right side elements to left side variables
		    for (int i=0; i<left.getSizeX(); i++)
		    {
                if (!(leftOps[0][i]  instanceof VariableToken)) 
                    return null; 
                //ErrorLogger.debugLine("AssignOpToken: inst Var..");
                
                if (!(rightOps[0][i] instanceof DoubleNumberToken  )) 
                    return null;
                //ErrorLogger.debugLine("AssignOpToken: inst Num..");
                
                // get one variable element of left side 
                VariableToken varToken = (VariableToken)leftOps[0][i];
                Variable leftVar = globals.getVariable(varToken.getName());
                
                // get number-matrix of an element on the right side
                DoubleNumberToken   number   = (DoubleNumberToken)rightOps[0][i]; 
                
                if (leftVar == null)
                {
                    ErrorLogger.debugLine("AssignmentOpTok: variable is null");
                    
                    // create variable
                    leftVar = globals.createVariable(varToken.getName() );
                }
                
                // assign right side matrix to left side variable
                result = leftVar.assign(number);
		    }
		    //return DoubleNumberToken.one;
		    return null;
		}
	    else if(operands[1] instanceof DoubleNumberToken)
	    {
	        ErrorLogger.debugLine("AssignOperatorToken: eval: MatrixToken = DoubleNumberToken");
	        MatrixToken left  = (MatrixToken)operands[0];
	        DoubleNumberToken right = (DoubleNumberToken)operands[1];
	        if (   (left.getSizeX()  != right.getSizeX())
		       || (left.getSizeY()  != 1)
		       || (right.getSizeY() != 1) )
	        {
	            ErrorLogger.debugLine("AssignOperatorToken: unequal sizes");
	            return null;
	        }

	        OperandToken[][] leftOps  = left.getValue();
	        double[][] rightOps = right.getReValues();

	        // assign all right side elements to left side variables
	        for (int i=0; i<left.getSizeX(); i++)
	        {
	            if (!(leftOps[0][i]  instanceof VariableToken)) 
	                return null; 
	            //ErrorLogger.debugLine("AssignOpToken: inst Var..");
		    
	            // get one variable element of left side 
	            VariableToken varToken = ((VariableToken)leftOps[0][i]);
	            Variable leftVar = globals.getVariable(varToken.getName());

		            // check if variable already exists
		            if (leftVar == null)
		            {
		                ErrorLogger.debugLine("AssignmentOpTok: variable is null");
			
			            // create variable
			            leftVar = globals.createVariable(varToken.getName() );
		            }
					
		            // get number-matrix of an element on the right side
		            DoubleNumberToken   number   = new DoubleNumberToken(rightOps[0][i]); 

		            // assign right side matrix to left side variable
		            result = leftVar.assign(number);
		        }
		        return null; 
	        }
        }
        else
        {
            Errors.throwMathLibException(ERR_LVALUE_REQUIRED);
        }

        return null; 
    }

}

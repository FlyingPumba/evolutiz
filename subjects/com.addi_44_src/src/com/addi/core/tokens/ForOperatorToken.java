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



/**Used to implement for loops within an expression*/
public class ForOperatorToken extends CommandToken
{

	/**initialisation */
	OperandToken forInitialisation;

	/**condition*/
	OperandToken forRelation;
	
	/**increment*/
	OperandToken forIncrement;

	/** { code } to execute For the condition is true*/
	OperandToken forCode;

	/**Constructor setting the ForRelation and ForCode
	@param _ForInitialisation 	= the test start values
	@param _ForRelation     	= the test relationship
	@param _ForIncrement 		= the test increment
	@param _ForCode     = the code to execute For the test is true*/
	public ForOperatorToken(OperandToken _forInitialisation,
    						OperandToken _forRelation, 
                            OperandToken _forIncrement, 
                            OperandToken _forCode)
	{
		forInitialisation 	= _forInitialisation;
		forRelation 		= _forRelation;
		forIncrement 		= _forIncrement;
		forCode				= _forCode;
	}

	/**
	 * 
	 * @return
	 */
	public OperandToken getForInitialisation()
	{
		return forInitialisation;
	}

	/**
	 * 
	 * @return
	 */
	public OperandToken getForRelation()
	{
		return forRelation;	
	}

	/**
	 * 
	 * @return
	 */
	public OperandToken getForIncrement()
	{
		return forIncrement;
	}

	/**
	 * 
	 * @return
	 */
	public OperandToken getForCode()
	{
		return forCode;
	}

    /**evaluates the operator
     * @param operands = the tokens parameters (not used)
     * @param
     * @return the result as an OperandToken
     */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
		ErrorLogger.debugLine("Parser: For: evaluate");
		
		loopDepth++;

		if(forRelation == null)
		{
			vectorFor(globals);
		}
		else
		{	
			OperandToken intialisationLine = ((OperandToken)forInitialisation.clone());
			intialisationLine.evaluate(null, globals);
			
			while(true)
			{
				continueHit = false;
				if (breakHit) {
					breakHit = false;
					return null;
				}
	
				// Check condition of For(...)
				OperandToken relationLine = ((OperandToken)forRelation.clone());
				OperandToken result       = relationLine.evaluate(null, globals);
                
				if (result instanceof DoubleNumberToken)
				{
					double[][] opValues	= ((DoubleNumberToken)result).getReValues(); 
					int        opSizeX 	= ((DoubleNumberToken)result).getSizeX();	
					int        opSizeY 	= ((DoubleNumberToken)result).getSizeY(); 
					boolean    cond		= false;	
	
					// Check if relation hold for at least one element
					for (int yy=0; yy<opSizeY; yy++) 
					{
						for (int xx=0; xx<opSizeX; xx++)
						{
							if (opValues[yy][xx] != 0.0)
							{
								//System.out.println("opValues "+opValues[yy][xx]);
								// At least one element is TRUE (!=0)
								cond = true;
								break;
							}
						}
						if (cond) break;
					}
	
					// if condition is false, then break For loop
					if (!cond) break;
	
					/* evaluate code */
					OperandToken code;
					if (cond)
					{
						OperandToken codeLine = ((OperandToken)forCode.clone());
						ErrorLogger.debugLine("Parser: for number is true");
						code = codeLine.evaluate(null, globals);
	
						//evaluate increment code
						OperandToken incrementLine = ((OperandToken)forIncrement.clone());
						incrementLine.evaluate(null, globals);
					}
				}
                else if (result instanceof LogicalToken)
                {
                    boolean cond = ((LogicalToken)result).getValue(0);

                    // if condition is false, then break while loop
                    if (!cond) break;

                    /* evaluate code */
                    OperandToken code;
                    if (cond)
                    {
                        OperandToken codeLine = ((OperandToken)forCode.clone());
                        ErrorLogger.debugLine("Parser: for boolean is true");
                        code = codeLine.evaluate(null, globals);
    
                        //evaluate increment code
                        OperandToken incrementLine = ((OperandToken)forIncrement.clone());
                        incrementLine.evaluate(null, globals);
                    }

                }
                else
                    Errors.throwMathLibException("For: unknown token");

			} // end while
		}
		
		loopDepth--;
		breakHit = false;
		continueHit = false;

		return null;
    }
    

    /**
     * @return the operator as a string
     */
    public String toString()
    {
        return "For";
    }

    
	/**evaluate for loop defined with a vector*/
	private void vectorFor(GlobalValues globals)
	{
		ErrorLogger.debugLine("vector for " + forInitialisation.toString());
		
		if(forInitialisation instanceof Expression)
		{
			Expression forExpression = ((Expression)forInitialisation);
			if(forExpression.getData() instanceof AssignmentOperatorToken)
			{
				ErrorLogger.debugLine("vector for assignmentop");
				
				if(forExpression.getChild(0) instanceof VariableToken)
				{
					ErrorLogger.debugLine("vector for evaluating 1");
					VariableToken variableToken = ((VariableToken)forExpression.getChild(0));
					Variable variable = globals.createVariable(variableToken.getName());

					DoubleNumberToken   vector = null;
					Token child = forExpression.getChild(1);
					if (child instanceof VariableToken) 
					{
						child = ((VariableToken)child).evaluate(null,globals);
					}
					
					if(child instanceof DoubleNumberToken)
					{
						ErrorLogger.debugLine("vector for evaluating 2");
						vector   = ((DoubleNumberToken)child);
					}
					else if(child instanceof Expression)
					{
						ErrorLogger.debugLine("vector for evaluating 3");
						OperandToken childOp = (OperandToken)child.clone();
                        childOp = childOp.evaluate(null, globals);
                        ErrorLogger.debugLine("for op "+ childOp);
                        
                        //child = ((Expression)child).getChild(1);
						if(childOp instanceof DoubleNumberToken)
						{
						    ErrorLogger.debugLine("vector for evaluating 4");
							vector   = ((DoubleNumberToken)childOp);						
							ErrorLogger.debugLine(vector.toString());
						}
					}
						
					int sizeX = vector.getSizeX();
					int sizeY = vector.getSizeY();
					
					double[][] values = vector.getReValues();
					
					for(int xx = 0; xx < sizeX; xx++)
					{
						//for(int xx = 0; xx < sizeX; xx++)
						//{
							continueHit = false;
							if (breakHit) {
								breakHit = false;
								return;
							}
							
							double[][] tempVals = new double[sizeY][1];
							
							for(int yy = 0; yy < sizeY; yy++)
							{
								tempVals[yy][0] = values[yy][xx];
							}
							
							DoubleNumberToken value = new DoubleNumberToken(tempVals);
							
							variable.assign(value);
							
							Expression exp = ((Expression)forCode.clone());
							exp.evaluate(null, globals);
						//}
					}
				}
			}
		}
	}	
}

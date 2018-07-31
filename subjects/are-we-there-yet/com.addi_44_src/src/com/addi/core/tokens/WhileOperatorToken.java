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



/**Used to implement if-then-else operations within an expression*/
public class WhileOperatorToken extends CommandToken
{
	/**condition */
	OperandToken whileRelation;

	/** { code } to execute while the condition is true*/
	OperandToken whileCode;

	/**Constructor setting the whileRelation and whileCode
	 * @param _whileRelation = the test relationship
	 * @param _whileCode     = the code to execute while the test is true
	 */
	public WhileOperatorToken(OperandToken _whileRelation, OperandToken _whileCode)
	{
		whileRelation 	= _whileRelation;
		whileCode		= _whileCode;
	}

    /**evaluates the operator
     * @param operands = the tokens parameters (not used)
     * @param globals
     * @return the result as an OperandToken
     * */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
		ErrorLogger.debugLine("Parser: While: evaluate");
		
		loopDepth++;
	
		while(true)
		{
			continueHit = false;
			if (breakHit) {
				breakHit = false;
				return null;
			}

			// Check condition of while(...)
			OperandToken relationLine = ((OperandToken)whileRelation.clone());
ErrorLogger.debugLine("line = " + relationLine.toString());			
			OperandToken result = relationLine.evaluate(null, globals);
            
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

				// if condition is false, then break while loop
				if (!cond) break;

				/* evaluate code */
				OperandToken code;
				if (cond)
				{
					OperandToken codeLine = ((OperandToken)whileCode.clone());
					ErrorLogger.debugLine("Parser: while number is true");
					code = codeLine.evaluate(null, globals);
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
                    OperandToken codeLine = ((OperandToken)whileCode.clone());
                    ErrorLogger.debugLine("Parser: while boolean is true");
                    code = codeLine.evaluate(null, globals);
                }

            }
            else
                Errors.throwMathLibException("While: unknown token");

        } // end while
		
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
        return "while";
    }
    
}

/* 
 * This file is part or JMathLib 
 * 
 * Check it out at http://www.jmathlib.de
 *
 * Author:  
 * (c) 2005-2009   
 */
package com.addi.toolbox.jmathlib.system;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**Display the debug information of an expression*/
public class foreach extends ExternalFunction
{
	/**Executes an expression, displaying the parse tree.
	@param operand[0] = a matrix containing the values
	@param operand[1] = a string containing the variable name
	@param operand[2] = a string containing the expression
	@return 0 if there were any errors, otherwise it returns 1*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		int result = 0;
		//check that the first operand is a matrix
		if(operands[0] instanceof DataToken)
		{
			//get the size of the matrix
			DataToken matrix = ((DataToken)operands[0]);
			int matSizeX = matrix.getSizeX();
			int matSizeY = matrix.getSizeY();

			if((operands[1] instanceof CharToken) && (operands[2] instanceof CharToken))
			{
				//create a variable with the correct name
				Variable var = globals.createVariable(((CharToken)operands[1]).getElementString(0));
				String expression = ((CharToken)operands[2]).getElementString(0);

				//parse the expression
				Parser p = new Parser();
		
	            OperandToken expressionTree = p.parseExpression(expression);
				
				//execute expression for each element of the matrix
				for(int yy = 0; yy < matSizeY; yy++)
				{			
					for(int xx = 0; xx < matSizeX; xx++)
					{
						var.assign(matrix.getElement(yy, xx));
						
						OperandToken exp = ((OperandToken)expressionTree.clone());			
						
						exp.evaluate(null, globals);
					}
				}
				result = 1;
			}
		}	
		return new DoubleNumberToken(result);
	}
}

/*
@GROUP
system
@SYNTAX
FOREACH(Matrix, Variable, Expression)
@DOC
executes an expression on each element of a matrix.
@NOTES
@EXAMPLES
FOREACH([1,2;3,4],"x","DISP(x)")
1
2
3
4
@SEE
*/


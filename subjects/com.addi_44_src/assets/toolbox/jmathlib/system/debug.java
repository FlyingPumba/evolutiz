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
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**Display the debug information of an expression*/
public class debug extends ExternalFunction
{
	/**Executes an expression, displaying the parse tree.
	@param operand[0] = the string containing the expression
	@return the result of the expression*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		String answer = "";
		if(operands[0] instanceof CharToken)
		{
			Parser p = new Parser();
		
			String expression = ((CharToken)operands[0]).getElementString(0);
			// separate expression into tokens and return tree of expressions
            OperandToken expressionTree = p.parseExpression(expression);

			// open a tree to show the expression-tree for a parsed command
            //CCX tools.treeanalyser.TreeAnalyser treeAnalyser = new tools.treeanalyser.TreeAnalyser(expressionTree);

	        OperandToken answerToken = expressionTree.evaluate(null, globals);
			//while(answerToken != null)
			//{        
	        	if(answerToken != null)
	        	{
		            //storeAnswer(answerToken);
	
					//if(answerToken.display)
		            //	answer += answerToken.toString() + "\n";

		            answerToken = expressionTree.evaluate(null, globals);
		          
		        }
	        //}
		}

		return new CharToken(answer);				
	}
}

/*
@GROUP
system
@SYNTAX
DEBUG(expression)
@DOC
This evaluates expression, displaying the op tree which is created.
@NOTES
@EXAMPLES
DEBUG("3*5")=15
@SEE
*/

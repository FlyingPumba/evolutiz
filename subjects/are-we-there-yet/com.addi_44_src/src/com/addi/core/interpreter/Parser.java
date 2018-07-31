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

package com.addi.core.interpreter;


import java.util.Vector;
import java.util.Stack;

import com.addi.core.constants.TokenConstants;
import com.addi.core.constants.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.*;

/**The parser uses the Lexer to produce a list of tokens which it builds into an abstract syntax tree. */
public class Parser extends RootObject implements TokenConstants, ErrorCodes
{
    /** the token which is currently processed */
    private Token   currentToken      = null;
    
    /** the token which will be parsed next */
    private Token   peekNextToken     = null;
    
    /** indicator wether the next token to be parsed was already requested from the 
        lexical analyser */
    private    boolean peekNextTokenBool = false;
    
    /**The lexical analyser which separates an expression into tokens*/
    private LexicalAnalyser lex = new LexicalAnalyser();

    //private boolean endOfTokens = false;

    /** */
    public String evaluationLockWords;

    private boolean _calledFromCommandLine = false;
    
    /**default constructor - creates the Parser object with an empty string*/
    public Parser(boolean calledFromCommandLine)
    {
        evaluationLockWords += " global isglobal ";
        _calledFromCommandLine = calledFromCommandLine;
    }
    public Parser()
    {
    }

    /**Parse an expression and return an operand tree of the expression
    @param expression = a vector of tokens
    @return the parsed expressions as a tree of operands*/
    public OperandToken parseExpression(String expression)
    {
        // show indention for every logging message
    	//ErrorLogger.setDisplayIndent(true);

        lex.analyseExpression(expression);
        //endOfTokens = false;

        // parse ALL available Tokens
        OperandToken expr = parseCommandList();             
        
        //ErrorLogger.setDisplayIndent(false);
        return expr; 
    }

    /** set expression of a m-function file or m-script file.
    This method is used for parsing m-scripts/functions. 
    @param expression = code of a function as a string*/
    public void setExpression(String expression)
    {
    	// display indention while parsing
        //ErrorLogger.setDisplayIndent(true);

        lex.analyseExpression(expression);
        //endOfTokens = false;

        //ErrorLogger.setDisplayIndent(false);
    }

    /** parse remaining tokens of a m-function file or m-script file 
    @return the parsed expresssion*/
    public OperandToken parseRemainingExpression()
    {
    	// display indention while parsing
        //ErrorLogger.setDisplayIndent(true);

        // parse ALL available Tokens
        OperandToken expr = parseCommandList();             
        
        //ErrorLogger.setDisplayIndent(false);
        return expr; 
    }
            
    /** return the currently scanned line of code to identify the 
    possible errors. (e.g. a=sin(3]+4  is an eroror and  
    returns a=sin(3] to display to the user
    @return the currently scanned line of code */ 
    public String getScannedLineOfCode()
    {
        return lex.getScannedLineOfCode();
    }
            
/*****************************************************************************/
/* Methods to return/inspect/work on single tokens returned from the scanner */

    /**get next token from the lexical analyser
    @return the next token*/
    public Token getNextToken()
    {
        return getNextToken(SINGLE);
    }

    /**get next token from the lexical analyser
    @param indicator if lexical analyser should consider whitespaces or not
    @return the next token*/
    private Token getNextToken(int type)
    {
        //ErrorLogger.debugLine("Parser: getNextToken");

        // copy peek next token to next token OR get new next token
        if (!peekNextTokenBool)
        {
            currentToken     =  lex.getNextToken(type);
        }
        else
        {
            currentToken      = peekNextToken;
            peekNextTokenBool = false;
        }

        return currentToken;
    }
    
    /**get next Token from lexical analyser, without moving forward
    @return the next token*/
    public Token peekNextToken()
    {
        return peekNextToken(SINGLE);
    }

    /**get next Token from lexical analyser, without moving forward
    @param indicator if lexical analyser should consider whitespaces or not
    @return the next token*/
    private Token peekNextToken(int type)
    {
        //ErrorLogger.debugLine("Parser: peekNextToken");
        
        if (!peekNextTokenBool)
        {
            peekNextToken     = lex.getNextToken(type);
            peekNextTokenBool = true;

            //if (peekNextToken == null)
            //    endOfTokens = true;
        }

           return peekNextToken;
    }

/*****************************************************************************/

    /**parse the expression contained in exp, this method is also called recursivly 
     to find the parameters of functions and matrices                             
    @param deliTyp - shows whether parser is parsing expressions or a matrix 
    @return the parsed operand/expression */
    private OperandToken parseSingle(Stack operandStack, int deliTyp)
    {
        //         ==SINGLE, this is a parameter of a function (e.g.: sin(3) or 3 or a )
        //         ==MATRIX, also consider whitespaces during parsing of matrices
        if (deliTyp==MATRIX) ErrorLogger.debugLine("Parser parseSingle matrix");
        else                 ErrorLogger.debugLine("Parser parseSingle"); 

        ErrorLogger.increaseIndent();

 
        OperandToken retToken  = null;                   // holds token to be returned
        Token        peekToken = peekNextToken(deliTyp); // holds next (actual) token

        if (peekToken == null)
        {
            // No more tokens available
            // Do nothing
        }
        else if(peekToken instanceof DelimiterToken)
        {
            DelimiterToken token = ((DelimiterToken)peekToken);

            if (token.value == '(' ) 
            {
                ErrorLogger.debugLine("Parser: found (");
                getNextToken(deliTyp); // remove (

                //parse expression, e.g.: (a+1)+2                    
                retToken = parseArithExpression(SINGLE);         
                
                // get current token to see if closing ')'
                if (!isExpectedDelimiter(peekNextToken(deliTyp), ')' )) 
                    Errors.throwParserException(" missing )");
                else
                    getNextToken(deliTyp);

                // (e.g. (2+3) is hidden inside in expression to avoid problems with
                // expressions of higher priority. (e.g. (2+3)*4 )
                retToken = new Expression(null, retToken);
                
            }
            else if (token.value == '[')
            {
                ErrorLogger.debugLine("Parser: matrix begin [");
                getNextToken(deliTyp); // remove [
                
                retToken = parseMatrix();
                
                //ErrorLogger.debugLine("Parser: Matrix: ] end");        
            }
            else if (token.value == '{')
            {
                ErrorLogger.debugLine("Parser: cell array begin {");
                getNextToken(deliTyp); // remove {
                
                retToken = parseCellArray();
                
                //ErrorLogger.debugLine("Parser: CellArray: ] end");        
            }
            else
            {
                // Delimiter: ] , ; \r \n ) } - 
            	//      end, endif, elseif, else
                // The parser found an unspecified delimiter, this can mark 
                //   the end of a parameter
                //   or the end of a function
                //   or the end of a line/command 
                // The delimiter will be consumed by a the parser function
                //   which is expecting a delimiter
                ErrorLogger.debugLine("Parser closing "+token.toString());
            }
        } 
        else if(peekToken instanceof VariableToken)
        {
            // variables (e.g. aaa or b)
            retToken = (OperandToken)getNextToken(deliTyp);
            
            ErrorLogger.debugLine("Parser parseSingle ppp "+retToken.toString()); 

       
            // check if the variable is followed by a dot operator
            if (peekNextToken(deliTyp) instanceof DotOperatorToken)
            {
                // e.g. a.something or a.sin() or a.getObject().getColor()
                operandStack.push(retToken);
                getNextToken(deliTyp);  // remove "." from scanner
                retToken = parseDotOperator(operandStack, deliTyp);
            }
            else if (peekNextToken(deliTyp) instanceof DelimiterToken)
            {
                // this might be something like e.g. ( sin (  )
                DelimiterToken token = ((DelimiterToken)peekNextToken(deliTyp));
                if (token.value == '(')
                {
                    ErrorLogger.debugLine("Parser: found function while parsing variable");
                    String name = ((VariableToken)peekToken).getName();
                    retToken = parseFunctionAndParameters( new FunctionToken(name), null );    
                }
                else if (token.value == '{')
                {
                    ErrorLogger.debugLine("Parser: found cell array structure");
                    String name = ((VariableToken)peekToken).getName();
                    retToken = parseFunctionAndParameters( new FunctionToken(name), null );    
                }
            }
            else if ( (peekNextToken(deliTyp) instanceof VariableToken) ||
                      (peekNextToken(deliTyp) instanceof CharToken)        )
            {
                // parse something like  disp hello    instead of disp("hello")
                // parse something like  disp "hello"  instead of disp("hello")
                // convert arguments into char arrays
                
                String name = ((VariableToken)retToken).getName();
                FunctionToken func = new FunctionToken(name);
                
                Token next = null ;
                while(true)
                {
                    next  = peekNextToken();
                    if (next==null)
                        break;
                        
                    //ErrorLogger.debugLine("Parser: var var "+next.toString());
                    
                    if (next instanceof DelimiterToken)
                    {
                        break;
                    }
                    else if ( (next instanceof VariableToken) ||
                              (next instanceof CharToken)        ) 
                    {
                    	String s;
                    	if (next instanceof CharToken) {
                    		s = ((CharToken)next).getElementString(0);
                    	} else {
                    		s = next.toString();
                    	}
                        
                        ErrorLogger.debugLine("Parser: var var variable "+next.toString());
                        getNextToken();
                        func.setOperands(new OperandToken[] {(OperandToken)new CharToken(s)});
                    }
                    else
                        Errors.throwMathLibException("Parser: var var");
                }
                
                return func;

            }
            else
                ErrorLogger.debugLine("Parser: VariableToken: " + retToken.toString());
        }
        else if(peekToken instanceof FunctionToken)
        {
            // * / + - ' sin() cos() and more
            // something like sin() cos() somefunction() if() switch()
            String name = ((FunctionToken)getNextToken(deliTyp)).getName();

            /* if-then-else, while, for, switch */
            if( name.equals("if"))
            {
                ErrorLogger.debugLine("Parser: if");
        		retToken = parseIf();
                ErrorLogger.debugLine("Parser: if end");
            }
            else if( name.equals("while"))
            {
                ErrorLogger.debugLine("Parser: while");
                retToken = parseWhile();                    
                ErrorLogger.debugLine("Parser: while end");
            }
            else if( name.equals("for"))
            {
                ErrorLogger.debugLine("Parser: for");
                retToken = parseFor();                    
                ErrorLogger.debugLine("Parser: for end");
            }
            else if( name.equals("switch"))
            {
                ErrorLogger.debugLine("Parser: switch");
                retToken = parseSwitch();                    
                ErrorLogger.debugLine("Parser: switch end");
            }
            else
            {
                /* Some function */
                ErrorLogger.debugLine("Parser: function "+name);
                retToken = parseFunctionAndParameters( (FunctionToken)peekToken, null );                    
            }
        }
        else if(peekToken instanceof OperandToken)
        {
            // numbers, variables (e.g 3 or a or b)
            retToken = (OperandToken)getNextToken(deliTyp);
            ErrorLogger.debugLine("Parser: operand: " + retToken.toString());
        }
        else if(peekToken instanceof OperatorToken)
        { 
            Token nextToken = getNextToken(deliTyp);
            Token pToken    = peekNextToken(deliTyp);

            // throw exception for multiple operands
            // e.g. a=/7  a=*8  +*5  -*8  -/9
            if (( (nextToken  instanceof AssignmentOperatorToken) ||
                  (nextToken  instanceof AddSubOperatorToken)     ||
                  (nextToken  instanceof MulDivOperatorToken)        ) &&
                (pToken     instanceof MulDivOperatorToken)               )
                Errors.throwParserException("multiple operators * /");

            // e.g. a=^7  +^3  -^6  *^5  ^^5  !^7
            if (( (nextToken  instanceof AssignmentOperatorToken) ||
                  (nextToken  instanceof AddSubOperatorToken)     ||
                  (nextToken  instanceof PowerOperatorToken)        ) &&
                (pToken     instanceof PowerOperatorToken)                )
                Errors.throwParserException("multiple operators ^");

            ErrorLogger.debugLine("PARSER  op+op: "+nextToken);
            ErrorLogger.debugLine("PARSER  op+op: "+pToken);
            
        	if(nextToken instanceof AssignmentOperatorToken)
            {
                ErrorLogger.debugLine("Parser: <x> = <y>");
                retToken = parseAssignmentOperator(nextToken, operandStack);                    
            }
            else if(nextToken instanceof ColonOperatorToken)
            {
                ErrorLogger.debugLine("Parser: <x> : <y>");
                retToken = parseColonOperator(nextToken, operandStack);                    
            }
            else if(nextToken instanceof DotOperatorToken)
            {                    
                ErrorLogger.debugLine("Parser: dot-operator");
                retToken  = parseDotOperator(operandStack, deliTyp);
            }
            else if((nextToken instanceof AddSubOperatorToken) &&
                    (pToken instanceof AssignmentOperatorToken))
            {
                // += or -=
                ErrorLogger.debugLine("Parser: += or -=");
                getNextToken(deliTyp);
                retToken  = parseAssignmentOperator(nextToken, operandStack);
                
            }
            else if((nextToken instanceof MulDivOperatorToken) &&
                    (pToken instanceof AssignmentOperatorToken))
            {
                // *= or /=
                ErrorLogger.debugLine("Parser: *= or /=");
                getNextToken(deliTyp);
                retToken  = parseAssignmentOperator(nextToken, operandStack);
                
            }
            else if(    (nextToken instanceof AddSubOperatorToken)
                     || (nextToken instanceof MulDivOperatorToken)
                     || (nextToken instanceof PowerOperatorToken)
                     || (nextToken instanceof BinaryOperatorToken)
                     || (nextToken instanceof RelationOperatorToken))
            {    
                /* Binary tokens: +, -, *, /, ^, <=, >=, ~=, == */
                ErrorLogger.debugLine("Parser: <x> " + nextToken.toString() + " <y>");
                retToken = parseBinaryOperator(nextToken, operandStack);                    
            }
            else if(nextToken instanceof UnaryOperatorToken)
            {
                ErrorLogger.debugLine("Parser: unary operator "+nextToken.toString());
                retToken = parseUnaryOperator(nextToken, operandStack);                    
            }
        }
        else 
        { 
            // every valid token must have been analyzed above
            Errors.throwParserException(" unknown token: "+peekToken.toString());
        }

        ErrorLogger.decreaseIndent();
        if (retToken!=null) ErrorLogger.debugLine("Parser return: " + retToken.toString());
        else                ErrorLogger.debugLine("Parser return: null");
        
        return retToken;
    } // end parseSingle
 

    /**parse the expression contained in exp, this method is also called recursivly 
       to find the parameters of functions and matrices  (e.g. something like 2+3+(2*4) )
     @param  deliTyp indicates if an argument is part of a matrix (whitespaces are treated
             as delimiters in this case)
     @return expression (e.g. 3 or 2+3 or sin(cos(3)) ) */
    private OperandToken parseArithExpression(int deliTyp)
    {
        
        // expressions have their own stack
        Stack operandStack = new Stack(); 
        
        ErrorLogger.debugLine("Parser parseArithmeticExpr begin");
        ErrorLogger.increaseIndent();

        while(true)
        {
            // parsing is not useful if the next token will be a closing ")"
            // (e.g. who() or a.getColor() )
            if ( isExpectedDelimiter(peekNextToken(deliTyp), ')') )
                break;
            
            // parse next token
            //CCX this may be null
            OperandToken subExpr = parseSingle(operandStack, deliTyp);         

            // Put parsed token on the operand stack
            // If nothing useful was parsed then break
            if (subExpr != null)
                operandStack.push(subExpr);

            Token peekToken = peekNextToken(deliTyp);
            
            // arithmetic expressions are terminated by the following delimiters
            if(peekToken instanceof DelimiterToken)
            {
                DelimiterToken token = ((DelimiterToken)peekToken);

                // check wether or not the next token is a closing delimiter
                if ( (token.value == ',')  || (token.value == ';')  ||  
                     (token.value == '\r') || (token.value == '\n') || 
                     (token.value == '}')  || (token.value == '-')  || 
                     (token.value == ' ')  || (token.value == ')')  || 
                     (token.value == ']')                               ) 
                 {
                     ErrorLogger.debugLine("Parser delimiter break "+token.value);
                     break;   
                 }
                    
            }
            
            
            // if no more tokens are available return
            if(peekToken==null)
                break;
            
        } // end while
    
    
        // Check if the currently parsed expression is terminated by a delimiter
        //  which has the display property.     
        //  (e.g: display "a=4,"  don't display "a=4;")
        //if ((getCurrentToken() instanceof DelimiterToken) &&
        //    isDisplayResultToken(getCurrentToken())       &&
        //    !operandStack.isEmpty())
        //{
        //      OperandToken op = (OperandToken)operandStack.pop();
        //    ErrorLogger.debugLine("Parser: display true "+op.toString());
        //    op.setDisplayResult(true);
        //    operandStack.push(op);
        //}

        
        // build return token
        OperandToken retT = null;
        if (!operandStack.isEmpty())
            retT = (OperandToken)operandStack.pop();
            
        // throw error if stacksize > 1
    
        ErrorLogger.decreaseIndent();
        ErrorLogger.debugLine("Parser parseArithmeticExpr end ");
            
        return retT;
    }

    /** parse a list of commands (e.g. a=2+3;if(a=2){b=3};c=sin(pi) )
    @return OperantToken tree of operand tokens (e.g. a=2+3) */
    private OperandToken parseCommandList()
    {
        // these could be concatenated expressions (e.g.: "a=1;b=sin(4);c=3")
        ErrorLogger.debugLine("Parser parseCommandList begin");
        ErrorLogger.increaseIndent();

        Stack operandStack   = new Stack();

        OperandToken command;
        while (true) 
        {
        
            // parse next token/command
        	ErrorLogger.debugLine("Parser parseCommandList next");
            //command = parseSingle(operandStack, SINGLE);
        	
            // handling of {...} for commands is done here only, there has
        	//   to be a difference in order to parse cell arrays
            if (isExpectedDelimiter(peekNextToken(), '{' ))
            {
    	        getNextToken();
            	// get commands inside {...}
            	// get commands (e.g. if(3) ... endif  or  if(3) ... elseif
    	        command = parseCommandList();    
    	        
    	        if (!isExpectedDelimiter( peekNextToken(), '}' ))
                    Errors.throwParserException(" parseCommandList ERROR missing }");
                else
                    getNextToken();
            }
            else
            	command = parseSingle(operandStack, SINGLE);
        	

            
            Token peekToken = peekNextToken();
            
            // Check if the currently parsed expression will be terminated by a delimiter
            //  which has the display property.     
            //  (e.g: display "a=4,"  don't display "a=4;")
            if (isDisplayResultToken(peekToken) && (command != null))
            {
                ErrorLogger.debugLine("Parser: display true "+command.toString());
                command.setDisplayResult(true);
            }
            
            // put parsed token on the operand stack
            if (command != null)
                operandStack.push(command);
            
            // Check if the token, why the parser is returned is correct
            // Command lists are terminated by "}" "endif" "end" "else"
            if (    isExpectedDelimiter(peekToken, "elseif")      ||
            		isExpectedDelimiter(peekToken, "else")        ||
    				isExpectedDelimiter(peekToken, "endif")       ||
    				isExpectedDelimiter(peekToken, "end")         ||
    				isExpectedDelimiter(peekToken, "endfunction") ||
    				isExpectedDelimiter(peekToken, "case")        ||
    				isExpectedDelimiter(peekToken, "default")     ||
    				isExpectedDelimiter(peekToken, "otherwise")   ||
    				isExpectedDelimiter(peekToken, "endswitch")   ||
    				isExpectedDelimiter(peekToken, "endwhile")    ||
    				isExpectedDelimiter(peekToken, "endfor")      ||
					isExpectedDelimiter(peekToken, '}')              )
            {
                // this path is only called from parseSingle( .. parseCommandList() )
            	ErrorLogger.debugLine("Parser parseCommandList break");
            	break;
            }
            else if (isExpectedDelimiter(peekToken, ',')  ||
            	     isExpectedDelimiter(peekToken, ';')  ||
				     isExpectedDelimiter(peekToken, '\r') ||
				     isExpectedDelimiter(peekToken, '\n')    )
            {
                // remove delimiters from parseSingle
            	getNextToken();
            }
            else  if (peekToken == null) 
            {
            	// command lists may be terminated by end of file 
                break;
            }
            else
            {
            	// is none of the above delimiters occured, there is an
            	//    error e.g. ceil(5;8]
            	//Errors.throwParserException("parseCommandList wrong delimiter");
            	//break;
            }
            	
        } // end while
        
        // put all code parts from the stack into one expression object    
        OperandToken tree      = null;
        int          stackSize = operandStack.size();
        if (stackSize > 0)
        {
            //The expressions have been put onto the stack in reversed order
            OperandToken[] opTokenArray = new OperandToken[stackSize];
            for(int i=stackSize-1; i>=0; i--)
            {
                OperandToken ob = ((OperandToken)operandStack.pop());
                   opTokenArray[i] = ob;
            }
            tree = new Expression(null, opTokenArray, stackSize);
        }

        ErrorLogger.decreaseIndent();
        ErrorLogger.debugLine("Parser parseCommandList end");
        return tree;
    }

/***************************************************************************************/
/*****             individual parsing methods                                     ******/
/***************************************************************************************/

    /** parse <variable><=><operand> (e.g. a=3 or a=2+3+4 or [a,b]=step(x) )           */                  
    /* @param                             				                                 */
    /* @param                                                                          */
    /* @return expression of assignment (e.g. a=3)                                     */
    private OperandToken parseAssignmentOperator(Token currentToken, Stack operandStack)
    {
        // operator    (this should be a "=" or ("+" for +=) or ("-" for -=))
        // or "*" for *=   or "/" for /=
        OperatorToken operator = (OperatorToken)currentToken; 
        
        //parse right parameter                    
        OperandToken rightSide = parseArithExpression(SINGLE);     

        //get left parameter from operandStack (e.g. a or [a,b,c]  =...)                
        OperandToken leftSide = (OperandToken)operandStack.pop(); 

        /* create new expression */
        Expression tree = null;
        if (currentToken instanceof AssignmentOperatorToken)
        {
            // e.g. a=8
            tree = new Expression(operator, leftSide, rightSide);
        }
        else if (currentToken instanceof AddSubOperatorToken)
        {
            // e.g. a+=8  ->  a=a+8
            // e.g. a-=7  ->  a=a-7
            ErrorLogger.debugLine("Parser: += or -=");
            tree = new Expression(operator, leftSide, rightSide);
            tree = new Expression(new AssignmentOperatorToken(), leftSide, tree);
        }
        else if (currentToken instanceof MulDivOperatorToken)
        {
            // e.g. a*=8 -> a=a*8
            // e.g. a/=9 -> a=a/9
            ErrorLogger.debugLine("Parser: *= or /=");
            tree = new Expression(operator, leftSide, rightSide);
            tree = new Expression(new AssignmentOperatorToken(), leftSide, tree);
        }

        return tree;
    } // end parseAssignmentOperator


    /***********************************************************************************/
    /** parse <operand>:<operand>           (e.g. 2:7 -> [2,3,4,5,6,7])                */
    /** parse <operand>:<operand>:<operand> (e.g. 2:3:8 -> [2,5,8]                     */
    /* @param                                                                            */
    /* @param                                                                           */
    /* @return                                                                         */
    private OperandToken parseColonOperator(Token nextToken, Stack operandStack)
    {
        OperandToken leftSide = null;

        //get left parameter from operandStack                    
        if(!operandStack.isEmpty())     
            leftSide =(OperandToken)operandStack.pop(); 

        //parse right parameter                    
        //be careful about e.g. a(:), a(1,:)
        OperandToken rightSide = null;
        if (!isExpectedDelimiter(peekNextToken(), ')') &&
            !isExpectedDelimiter(peekNextToken(), ',') &&
            !isExpectedDelimiter(peekNextToken(), ';')    ) 
            rightSide = parseArithExpression(SINGLE);     
        else
            rightSide = null;
        
        // also possible is something like a(1:end) or a(2:4:end)
        if (isExpectedDelimiter(peekNextToken(), "end"))
            rightSide = (OperandToken)getNextToken();

        // Check is left Side is empty
        if ((leftSide==null)                     || 
            (leftSide instanceof DelimiterToken)    )
        {
            // something like:  a(:)
            ErrorLogger.debugLine("Parser: colon: (:)");
            operandStack.push(leftSide); // push left side back on stack
            return new Expression((OperatorToken)nextToken);
        }
        else
        {
            // e.g. 1:8
            if (rightSide instanceof Expression)
            {
                 Expression rightExpr = (Expression)rightSide;
                if (rightExpr.getData() instanceof ColonOperatorToken)
                {
                    // e.g. 2:<4:5>
                    ErrorLogger.debugLine("Parser: colon: "+leftSide.toString()+":"
                                                           +rightExpr.getChild(0).toString()+":"
                                                           +rightExpr.getChild(1).toString()      );
                    OperandToken[] opToken = { leftSide,
                                               rightExpr.getChild(0),
                                               rightExpr.getChild(1) };
                    return new Expression((OperatorToken)nextToken, opToken, 3);

                }
                else
                {
                    // e.g. 2:<6+7> (just one colon, but expression)
                    ErrorLogger.debugLine("Parser: colon: "+leftSide.toString()+":"+rightSide.toString());
                    return new Expression((OperatorToken)nextToken,
                                          leftSide,
                                          rightSide);
                }
            }
            else
            {
                // e.g.: 2:6
                ErrorLogger.debugLine("Parser: colon: "+leftSide.toString()+":"+rightSide.toString());
                return new Expression((OperatorToken)nextToken, leftSide, rightSide);
             }
            
        }
        
    } // end parseColonOperator


    /***********************************************************************************/
    /** parse <operand><operator><operand> (e.g. 3+4, 3*4, 3/4, 1+2*3+4*5)             */
    /* @param                                                                            */
    /* @param                                                                           */
    /* @return                                                                         */
    private OperandToken parseBinaryOperator(Token nextToken, Stack operandStack)
    {
        // operator for this operation 
        OperatorToken operator = (OperatorToken)nextToken;
        
        //get left parameter from operandStack 
        //(exception: if parsing "-a" left operand will be null)                    
        OperandToken leftSide = null;
        if (!operandStack.isEmpty())
            leftSide = (OperandToken)operandStack.pop(); 

        //parse right parameter                    
        OperandToken rightSide = parseSingle(operandStack, SINGLE);         

        // create new expression 
        Expression tree = new Expression(operator, leftSide, rightSide);
           
        // Check if priority is correct. e.g.: 2*3+4 => (2*3)+4
        // (left side and right side might be changed 
        //   if the operators (+-*^!') have different priorities)
        if (!(leftSide instanceof Expression))
            return tree;
        
        Expression    leftExpr   = (Expression)leftSide;
        OperatorToken leftOperator = (OperatorToken)leftExpr.getData();
        
        if (leftOperator == null)
            return tree;
            
        if (!((leftOperator instanceof BinaryOperatorToken) || (leftOperator instanceof UnaryOperatorToken)))
            return tree;
            
        /* only priority swapping for binary operators + - * / ^ < > ... */
        // 1+2*3+4*5 should be parsed into ((1+(2*3))+(4*5))
        // 1/2-1+9   should be parsed into (((1 / 2) - 1) + 9)
        ErrorLogger.debugLine("priority ("+ leftOperator.getPriority()   +
                              ","         + operator.getPriority() +
                              ") <"       + leftSide.toString()  +
                              "> "        + operator.toString() +
                              " <"        + rightSide.toString() + ">");

        if (leftOperator.getPriority() < operator.getPriority())
        {
            // something like 2+3*4 : <2+3> * <4>    
            // change to              <2>   + <3*4>
            // left:       +
            //         <2>    <3>     
            ErrorLogger.debugLine("swapping priorities");
            OperandToken newLeft = leftExpr.getRight();
                        
            //already parsed right side is first operand of new right
            //operandStack.push(rightSide);
            
            //parse right
            
            //OperandToken newRight = (OperandToken)operandStack.pop();
            OperandToken newRight = new Expression((OperatorToken)operator,
                                                 leftExpr.getRight(),
                                                 rightSide); 
            
            // Check next token if it is a binary operator and call a parse() method 
            // until priority of next operator is lower/equals as the calling operator
            // (e.g. 1+2 * 3 <+> .... priority of <+> is lower than "*")
        	if (peekNextToken() instanceof OperatorToken)
            {
                
                OperatorToken nextOperator = (OperatorToken)peekNextToken();
                
                if (operator.getPriority() <= nextOperator.getPriority())
                {
                
                    // the current arithmetic expression is not terminated yet
                    // parse next operands
                    ErrorLogger.debugLine("Parser: BinaryOperator: recursive parsing");
                    operandStack.push(newRight);
                    
                    newRight = parseSingle(operandStack, SINGLE);
                }
            }
                    
            // create tree  again 
            tree  = new Expression((OperatorToken)leftOperator,
                                    leftExpr.getLeft(),
                                    newRight);
        }
        
        return tree;               
    } // end parseBinaryOperator


    /***********************************************************************************/
    /** parse <operand><operator> (e.g. 3! or !(a<3))                                  */
    /* @param                                                                          */
    /* @param                                                                          */
    /* @return                                                                         */
    private OperandToken parseUnaryOperator(Token nextToken, Stack operandStack)
    {

    	// if parsing !(a<3) or !3 operand stack is empty
    	if (operandStack.isEmpty())
    	{
    		ErrorLogger.debugLine("Parser: Unary !3 or ~3");
    		
    		UnaryOperatorToken tok = (UnaryOperatorToken)nextToken;
    		if (! ((tok.getValue()=='!') || (tok.getValue()=='~')))
    			Errors.throwParserException(" Unary operator by empty stack");
    		
    		//CCX don't actually want to invert whole rest of statement
    		//only until we have an operand and the next operator is not of higher precendence 
			OperandToken operand = null;
			Token peekToken = null;
			do {
				operand = parseSingle(operandStack, SINGLE);
				peekToken = peekNextToken();
				if (peekToken instanceof PowerOperatorToken) {
					operandStack.push(operand);
				}
			} while (peekToken instanceof PowerOperatorToken);
			FunctionToken func = new FunctionToken("not");
			func.setOperand(operand);
    		return func;	
    	} else {
    	
	    	// operator for this operation 
	        OperatorToken operator = (OperatorToken)nextToken;
	        
	        //get left parameter from operandStack 
	        OperandToken leftSide = (OperandToken)operandStack.pop();          
	
	        // create new expression 
	        Expression tree = new Expression(operator, leftSide);
	           
	        // Check if priority is correct. e.g.: 2*3+4 => (2*3)+4
	        // (left side and right side might be changed 
	        //   if the operators (+-*^!') have different priorities)
	        if (!(leftSide instanceof Expression))
	            return tree;
	        
	        Expression    leftExpr   = (Expression)leftSide;
	        OperatorToken leftOperator = (OperatorToken)leftExpr.getData();
	        
	        if (leftOperator == null)
	            return tree;
	            
	        if (!((leftOperator instanceof BinaryOperatorToken) || (leftOperator instanceof UnaryOperatorToken)))
	            return tree;
	            
	        /* only priority swapping for binary operators + - * / ^ < > ... */
	        // 1+2*3+4*5 should be parsed into ((1+(2*3))+(4*5))
	        // 1/2-1+9   should be parsed into (((1 / 2) - 1) + 9)
//	        ErrorLogger.debugLine("priority ("+ leftOperator.getPriority()   +
//	                              ","         + operator.getPriority() +
//	                              ") <"       + leftSide.toString()  +
//	                              "> "        + operator.toString() +
//	                              " <"        + rightSide.toString() + ">");
	
	        if (leftOperator.getPriority() < operator.getPriority())
	        {
	            // something like 2+3*4 : <2+3> * <4>    
	            // change to              <2>   + <3*4>
	            // left:       +
	            //         <2>    <3>     
	            ErrorLogger.debugLine("swapping priorities");
	            OperandToken newLeft = leftExpr.getRight();
	                        
	            //already parsed right side is first operand of new right
	            //operandStack.push(rightSide);
	            
	            //parse right
	            
	            //OperandToken newRight = (OperandToken)operandStack.pop();
	            OperandToken newRight = new Expression((OperatorToken)operator,
	                                                 leftExpr.getRight()); 
	                    
	            // create tree  again 
	            tree  = new Expression((OperatorToken)leftOperator,
	                                    leftExpr.getLeft(),
	                                    newRight);
	        }
	        
	        return tree; 
    	}
    } // end parseUnaryOperator


    /***********************************************************************************/
    /** parse expressions of the form value.function()                                 */
    /* @param  operand stack                                                            */
    /* @param  type of delimiters (including whitespaces or not)                         */
    /* @return parsed expression or variable token                                     */
    private OperandToken parseDotOperator(Stack operandStack, int deliTyp)
    {
        ErrorLogger.debugLine("Parser: DotOperator");
        
        // result of parsing dot operator
        OperandToken result;
        
        // the left operand has been parsed already
        OperandToken leftSide  = (OperandToken)operandStack.pop();
        
        // right side
        OperandToken rightSide = null; //parseSingle(operandStack, SINGLE);

        // check type of right side operand (e.g. a.bbbb or a.someFunction() )
        if (peekNextToken(deliTyp) instanceof VariableToken)
        {
            // (e.g. a.B or bar.foo)
            rightSide = (VariableToken)getNextToken(deliTyp);
        }
        else if (peekNextToken(deliTyp) instanceof FunctionToken)
        {
            // (e.g. a.sin() or a.getColor() )
            rightSide =  parseFunctionAndParameters((FunctionToken)getNextToken(deliTyp), null);
        }        
        else
            Errors.throwParserException("DotOperator: unknown right side");
            
        // create extended variable token or expression of dot-operator
        if ((leftSide  instanceof VariableToken) &&
            (rightSide instanceof VariableToken)   )
        {
            // (e.g.  a.length or system.A or a.bbb)
            String left  = ((VariableToken)leftSide).getName();
            String right = ((VariableToken)rightSide).getName();
            ErrorLogger.debugLine("Parser: " + left + "." + right);
            result = new VariableToken(left, right);
        }
        else
        {
            // (e.g. a.sin() or a.getColor() 
            ErrorLogger.debugLine("Parser:  foo.some_func()");
            result = new Expression(new DotOperatorToken(), leftSide, rightSide);
        }                 
        
        // check if the dot-expression is followed by another dot operator
        // (e.g. aaa.getGraphics().getSize().getValue() )
        if (peekNextToken(deliTyp) instanceof DotOperatorToken)
        {
            // push already parsed expression on stack
            operandStack.push(result);                  
            
            // remove "." from scanner
            getNextToken(deliTyp);                 
                                  
            // parse next dot-expression
            result = parseDotOperator(operandStack, deliTyp);  
        }     
             
        return result;
    }


    /***********************************************************************************/
    /** parse <function>(<operand>,<operand>,....) (e.g. min(3,4) )                    */
    /* @param FunctionToken                                                            */
    /* @param first parameter                                                            */
    /* @return parsed function (e.g. sin(3) or min(2,3) )                              */
    private OperandToken parseFunctionAndParameters(OperandToken nextToken, OperandToken firstParam)
    {
        // create new expression
        FunctionToken func = (FunctionToken)nextToken;

        // check if it is a special function e.g. "global a b c"
        if ( func.getName().equals("global") || func.getName().equals("isglobal"))
        {
            ErrorLogger.debugLine("Parser: found global");
         
            // do not evaluate operands
            func.evaluationLockB=true;
            
            Token next = null ;
            while(true)
            {
                next      = peekNextToken();
                if (next==null)
                    break;
                    
                ErrorLogger.debugLine("Parser: global "+next.toString());
                
                if (next instanceof DelimiterToken)
                {
                    break;
                }
                else if (next instanceof VariableToken)
                {
                    ErrorLogger.debugLine("Parser: global variable "+next.toString());
                    getNextToken();
                    func.setOperands(new OperandToken[] {(OperandToken)next});
                }
                else
                    Errors.throwMathLibException("Parser: global");
            }
            
            // !!!! needs stack for multiple "global a b c e"
            //func.setOperands(new OperandToken[] {(OperandToken)next}); //parameters);
            return func;
        }
        
        
        // if next token is a "(" then ignore it
        Token next      = peekNextToken();
        
        // indicator if a cell array acess is parsed
        // e.g. a{4,6}='66'
        boolean cellB = false;
        
        if (isExpectedDelimiter( next, '(' ))
        {
            getNextToken();
        }
        else if (isExpectedDelimiter( next, '{' ))
        {
            getNextToken();
            cellB = true;
        }
        else if(next instanceof DotOperatorToken)
        {
            return func;
        }

        Stack parameterStack  = new Stack();
        
        // if a first parameter is supplied, put it on the stack
        if(firstParam != null)
        {
           firstParam.setDisplayResult(false);
           parameterStack.push(firstParam);
           func.setOperand(firstParam);
        }

        int i=0;
        while (true)
        {
            i++;
            ErrorLogger.debugLine("Parser: function parse Parameter "+i);

            //parse next parameter                    
               OperandToken operand = parseArithExpression(SINGLE);        

            // check if a parameter is returned (e.g. who() returns no parameter)
            if (operand == null)
            {
                // who() returns no parameter, but still need to remove ")" from scanner
                if (isExpectedDelimiter(peekNextToken(), ')') && !cellB)
                    getNextToken();
                if (isExpectedDelimiter(peekNextToken(), '}') &&  cellB)
                    getNextToken();
            
                break;
            }
            
            //values of parameters are not displayed at the prompt
            operand.setDisplayResult(false);
            
            //add parameter to expression tree
            parameterStack.push(operand);

            // get current token to see if more parameters expected or if closing ')'
            Token current = peekNextToken();

            if (current != null)
                ErrorLogger.debugLine("Parser: function parse Parameter current "+current.toString());            

            if (isExpectedDelimiter(peekNextToken(), ',' )) 
            {
                getNextToken();
            }
            else if (isExpectedDelimiter(peekNextToken(), ')' ) && !cellB) 
            {
                getNextToken();
                break;
            }
            else if (isExpectedDelimiter(peekNextToken(), '}' ) && cellB) 
            {
                getNextToken();
                break;
            }
            //else if (isExpectedDelimiter(peekNextToken(), ';' )) 
            //{
            //       getNextToken();
            //    break;
            //}
            else
                Errors.throwParserException(" error parsing parameter");
            

        }
        
        // copy parameters from stack into operands array
        int            parSize    = parameterStack.size();
        OperandToken[] parameters = new OperandToken[parSize];
        
        // reverse order, because stack is last in first out
        for( int p=parSize-1; p>=0; p--)
        {
            parameters[p] = ((OperandToken)parameterStack.pop());
        }
        
        // e.g. foo{888}=...
        if (cellB)
        {
            return new VariableToken(func.getName(),parameters, "cell");
        }
        
        
        func.setOperands(parameters); // = ( (OperatorToken)nextToken , parameters, parSize);
        
        return func;
    } // end parseFunctionParameters

    /***********************************************************************************/
    /** parse <if>(<relation>){<expression>} else {<expression>}                       */
    /*  parse <if>(<relation>) <expression> else <expression> endif
    /* @param                                                                 		   */
    /* @param                                                                           */
    /* @return                                                                         */
    private OperandToken parseIf()
    {
        // parse releation (e.g. if (3<5) ...)
    	// next Token must be a "(" 
        //if (!isExpectedDelimiter(getNextToken(), '('))
        //    Errors.throwParserException(" if missing (");

        // get argument inside (...)
        OperandToken ifRelation = parseArithExpression(SINGLE);         

        // check if argument is terminated by ")"
        //if (!isExpectedDelimiter(peekNextToken(), ')' )) 
        //    Errors.throwParserException("If: missing )");
        //else
        //    getNextToken();
            
        ErrorLogger.debugLine("Parser: if after relation: "+ifRelation.toString());


        // parse commands (e.g. if(3<5) {some_commands} or
        //                      if(3<5)  some_commands  endif
        ErrorLogger.debugLine("Parser: if-command");
        OperandToken ifCode = parseCommandList();       
        //ErrorLogger.debugLine("Parser: if command: "+ifCode.toString());
        
        // If(..) {...}
        IfThenOperatorToken ifToken = new IfThenOperatorToken(ifRelation, ifCode);
        
        // start parsing elseif, ... , else
        while(true)
        {
            
            Token nextToken = peekNextToken();
            //ErrorLogger.debugLine("Parser: if "+nextToken);
            
            if(isExpectedDelimiter(peekNextToken(), "elseif"))
            {
                // this is at least some if-elseif-else
            	getNextToken(); // remove "elseif"-token 
        
                // elseif(..) {...} 
                ErrorLogger.debugLine("Parser: if: found elseif");

                //if (!isExpectedDelimiter(getNextToken(), '(' ))
                //    Errors.throwParserException(" elseif missing (");

                // get argument inside (...)
                OperandToken elseIfRelation = parseArithExpression(SINGLE);         
   		
                // check if argument is terminated by ")"
                //if (!isExpectedDelimiter(peekNextToken(), ')' ) ) 
                //    Errors.throwParserException("If: missing )");
                //else
                //    getNextToken();
                
                ErrorLogger.debugLine("Parser: elseIf after relation: "+elseIfRelation.toString());
                
                // parse commands (e.g. elseif(3<5) {some_commands} or
                //                      elseif(3<5)  some_commands  endif
                OperandToken elseIfCode = parseCommandList();       
              
                //ErrorLogger.debugLine("Parser: elseIf code: "+elseIfCode.toString());
                
                ifToken.addCondition(elseIfRelation, elseIfCode);
            }
            else if (isExpectedDelimiter(peekNextToken(), "else") )
            {
                getNextToken(); // remove "else"-token 
        
                // If(..) {...} else {...}
                ErrorLogger.debugLine("Parser: if: found else");

                // get commands inside {...}
                OperandToken elseCode = parseCommandList();       
                
                //ErrorLogger.debugLine("Parser: else code: "+elseCode.toString());

                ifToken.addCondition(null, elseCode);
            }
	        else if (isExpectedDelimiter(peekNextToken(), "endif") ||
	        		 isExpectedDelimiter(peekNextToken(), "end")      )
	        {
	        	getNextToken();  // remove endif token
	        	break;
	        }
	        else
	        	if (_calledFromCommandLine)
		           Errors.throwParserException("CCX: continue");
	        	else
	        	   Errors.throwParserException("If: missing end");
        } // end while
        
        ErrorLogger.debugLine("Parser: if: end of parsing");
        return ifToken;
    } // end parseIf        


    /***********************************************************************************/
    /** parse  switch(<variable>)                                                      */
    /*         {                                                                       */
    /*            case(<number>): { <expression> }                                     */
    /*                  ...                                                            */
    /*            case(<number>): { <expression> }                                     */
    /*            default:        { <expression> }                                     */
    /*         }                                                                       */
    /* @param                                                                            */
    /* @param                                                                           */
    /* @return                                                                         */
    private OperandToken parseSwitch()
    {
        Token nextToken = null;
        
        // next Token must be a "(" 
        //if (!isExpectedDelimiter(getNextToken(), '(' ))
        //    ErrorLogger.debugLine("Parser: switch missing (");

        // get argument inside (...)
        OperandToken switchRelation = parseArithExpression(SINGLE);         

        //if (!isExpectedDelimiter(peekNextToken(), ')' )) 
        //    Errors.throwParserException("Switch: missing )");
        //else
        //    getNextToken();
        
        
        if (!isExpectedDelimiter(peekNextToken(), '\n')    ) 
            Errors.throwParserException("switch: missing \\n");
        else
            getNextToken();

        ErrorLogger.debugLine("Parser: switch after relation");

        
        //parse switch statement
        Vector cases = new Vector(10);

        // if the next Token is a "{" then remove it
        //if(isExpectedDelimiter(peekNextToken(), '{' ))
        //    nextToken = getNextToken();
            
        // parse case,default,otherwise commands
        while(peekNextToken() != null)
        {

            if(isExpectedDelimiter(peekNextToken(), "case"))
            {
            	getNextToken();
            	ErrorLogger.debugLine("Parser: switch: case     *****");
                cases.addElement(parseCase());
                ErrorLogger.debugLine("Parser: switch: case end *****");
            }
            else if(isExpectedDelimiter(peekNextToken(), "default")  ||
            		isExpectedDelimiter(peekNextToken(), "otherwise")   )
            {
            	getNextToken();
            	ErrorLogger.debugLine("Parser: switch: default");
                cases.addElement(parseDefault());
            }
            else if(isExpectedDelimiter(peekNextToken(), "end")      ||
            		isExpectedDelimiter(peekNextToken(), "endswitch")   )
            {
            	getNextToken();
                ErrorLogger.debugLine("Parser: switch: end");
                break;
            }
            else 
            {
            	if (_calledFromCommandLine)
 		           Errors.throwParserException("CCX: continue");
 	        	else
 	        	   Errors.throwParserException("Switch: missing end");
            }
            
            
        }  // end while
        
        ErrorLogger.debugLine("Parser: switch returning");
        return new SwitchToken(switchRelation, cases);
    }
    
    /**
     * parsing of "case" commands
     * @return
     */
    private OperandToken parseCase()
    {
        //if (!isExpectedDelimiter(getNextToken(), '(' ))
        //    ErrorLogger.debugLine("Parser: case missing (");

        // get argument inside (...)
        ErrorLogger.debugLine("Parser: switch: case relation     *****");
        OperandToken caseRelation = parseArithExpression(SINGLE);         

        //if (!isExpectedDelimiter(peekNextToken(), ')' )) 
        //    Errors.throwParserException("Switch case: missing )");
        //else
        //    getNextToken();

        ErrorLogger.debugLine("Parser: switch: case relation end *****");
        
        // get commands 
        ErrorLogger.debugLine("Parser: switch: case code     *****");
        OperandToken caseCode  = parseCommandList();       
        
	    ErrorLogger.debugLine("Parser: switch: case code end *****");

        return new CaseToken(caseRelation, caseCode);
    }

    /**
     *  parse "default" or "otherwise" command of switch
     * @return
     */
    private OperandToken parseDefault()
    {
        ErrorLogger.debugLine("Parser: switch default");

        // get default commands 
        OperandToken defaultCode  = parseCommandList();       
        
        return new CaseToken(null, defaultCode);
    }

    /***********************************************************************************/
    /** parse while(something) { do something }                                        */
    /* @param                                                                            */
    /* @param                                                                           */
    /* @return                                                                         */
    private OperandToken parseWhile()
    {

        // check for "("   (e.g. while(.....  )
        //if(!isExpectedDelimiter(peekNextToken(), '(') )
        //    Errors.throwParserException("while: ERROR missing (");
        //else
        //    getNextToken();

        // get argument inside (...)
        OperandToken whileRelation = parseArithExpression(SINGLE);         

        //if(!isExpectedDelimiter(peekNextToken(), ')') )
        //    Errors.throwParserException("While: ERROR missing )");
        //else
        //    getNextToken();

        ErrorLogger.debugLine("Parser: while after relation");

        // get commands inside {...}
        OperandToken whileCode = parseCommandList();          

        //if (isExpectedDelimiter(peekNextToken(), ',' ) ||
        //    isExpectedDelimiter(peekNextToken(), ';' )   )
        //    getNextToken();

        if(isExpectedDelimiter(peekNextToken(), "end")      ||
           isExpectedDelimiter(peekNextToken(), "endwhile")   )
        {
        	getNextToken();
            ErrorLogger.debugLine("Parser: while: end");
        }
        else 
        	if (_calledFromCommandLine)
		        Errors.throwParserException("CCX: continue");
	        else
	        	Errors.throwParserException("While: missing end");

        // While(..) {...}
        return (OperandToken)( new WhileOperatorToken(whileRelation, whileCode));
    }


    /***********************************************************************************/
    /** this method parses for loops                                                   */
    /*  (e.g. for(z=0;z<10;z=z+1) { disp(z); } endfor                                       */
    /* @param                                                                            */
    /* @param            				                                               */
    /* @return                                                                         */
    private OperandToken parseFor()
    {
        OperandToken forInitialisation   = null;         
        OperandToken forRelation         = null;                 
        OperandToken forIncrement        = null;         
        
        //skip the opening bracket
        //Token dummy = peekNextToken();
        
        if(isExpectedDelimiter(peekNextToken(), '(') )
        {
            
            //skip the opening bracket
            getNextToken();
            ErrorLogger.debugLine("Parser: for loop");        
    
            // get arguments inside (...;...;...)
            forInitialisation     = parseArithExpression(SINGLE);         
            ErrorLogger.debugLine("Parser: for: initialisation = " + forInitialisation.toString());          
            
            if (!isExpectedDelimiter(peekNextToken(), ';' )) 
                Errors.throwParserException("For: missing ;");
            else
                getNextToken();
                
            forRelation             = parseArithExpression(SINGLE);         
            ErrorLogger.debugLine("Parser: for: relation = " + forRelation.toString());
            
            if (!isExpectedDelimiter(peekNextToken(), ';' )) 
                Errors.throwParserException("For: missing ;");
            else
                getNextToken();
            
            forIncrement             = parseArithExpression(SINGLE);         
            ErrorLogger.debugLine("Parser: for: increment = " + forIncrement.toString());
        
            if (!isExpectedDelimiter(peekNextToken(), ')' )) 
                Errors.throwParserException("For: missing )");
            else
                  getNextToken();
        
        }
        else
        {
            // e.g. for i=1:4   
            // e.g. for i=x
            // e.g. for i=[1,3,5]
            ErrorLogger.debugLine("Parser: FOR vector for loop");        
    
            // get arguments inside for < >, ...  (e.g. for x=[1,2,3], ...)
            forInitialisation     = parseArithExpression(SINGLE); //MATRIX); //SINGLE);
            
            // get rid of the closing , ; \n
            // e.g. for i=2:5,
            // e.g. for i=2:5;
            // e.g. for i=2:5\n
            if ((peekNextToken() != null) &&
            	!isExpectedDelimiter(peekNextToken(), ';' ) &&
                !isExpectedDelimiter(peekNextToken(), ',' ) &&
                !isExpectedDelimiter(peekNextToken(), '\n')    ) 
                Errors.throwParserException("For: missing ; , \\n");
            else
                getNextToken();
            
            ErrorLogger.debugLine("ParserFOR" + peekNextToken());
            
            
            //if (!isExpectedDelimiter(peekNextToken(), ',' ) ||
            //    !isExpectedDelimiter(peekNextToken(), '\n' )   ) 
            //    Errors.throwParserException("For: missing , or \\n");
            //else
            //      getNextToken();         
            
            ErrorLogger.debugLine("Parser: for: initialisation = " + forInitialisation.toString());          
        }

        // get commands inside {...}
           OperandToken forCode = parseCommandList();
        
        //if (peekNextToken() instanceof DelimiterToken)
        //    getNextToken();
        
        if(isExpectedDelimiter(peekNextToken(), "end")    ||
           isExpectedDelimiter(peekNextToken(), "endfor")   )
        {
            getNextToken();
            ErrorLogger.debugLine("Parser: for: end");
        }
        else 
        	if (_calledFromCommandLine)
		       Errors.throwParserException("CCX: continue");
	        else
	           Errors.throwParserException("For: missing end");
                    
        ErrorLogger.debugLine("Parser: for: code = "+forCode.toString());

        // For(...;...;...) {...}
        return new ForOperatorToken(forInitialisation, forRelation, forIncrement, forCode);
    }


    /***********************************************************************************/
    /** this method parses matrices (e.g. a=[1,2,3;4,5,6] or a=[1 2 3])                */
    /* @return   parsed matrix                                                         */
    private OperandToken parseMatrix()
    {
        boolean endLine          = false;         //
        boolean endMatrix        = false;         //
        int x                    = 0;             // x-size of matrix
        int y                    = 1;             // y-size of matrix
        Stack elements           = new Stack();   // stack for elements
        Stack rowLength          = new Stack();   //
        boolean numberB          = true;          //
        boolean singleB          = true;          //
        boolean numberIndicatorB = false;         //
        boolean singleIndicatorB = false;         //
        boolean imagIndicatorB   = false;         //

        // remove spaces between "[" and first element (e.g. [  2,3] -> [2,3]
        while (isExpectedDelimiter(peekNextToken(MATRIX), ' '))
        		getNextToken();

        // parse code of matrices (e.g. [1,2,3;4,5,6] or [1 sin(2) 3; 4 5+1 a]
        while(!endMatrix) 
        {
            // get next parameter (whitespaces are treated as delimiter, too)
            OperandToken nextParameter = parseArithExpression(MATRIX);    

            numberIndicatorB = false;            
            singleIndicatorB = false;
            
            // check type of each parameter: some operands need special attention
            if (nextParameter instanceof Expression)
            {
                Expression expr = (Expression)nextParameter;
                
                if (expr.getData() == null)
                {
                    if (expr.getNumberOfChildren() == 1)
                    {
                        // there is only one parameter
                        singleIndicatorB = true;
                    }
                }
            }
            else if (nextParameter instanceof DoubleNumberToken)
            {
                // this parameter is a number token
                DoubleNumberToken numberT = (DoubleNumberToken)nextParameter;

                // check if matrix has imaginary values
                if (numberT.getValuesIm() != null)
                     imagIndicatorB = true;
                            
                // Check if this number token is a scalar. Otherwise deal with
                // it like a general matrix
                if((numberT.getSizeX()==1) && (numberT.getSizeY()==1))                        
                       numberIndicatorB = true;
            }
            
            if (numberIndicatorB == false)
            {
                // at least one element of the matrix is NOT a number
                numberB = false;
            }
            if (singleIndicatorB == false)
            {
                // at least one element is not a single operand
                singleB = false;
            }

            ErrorLogger.debugLine("Parser: para" + nextParameter);
            // push parameter onto element stack
            if (nextParameter != null)
                elements.push(nextParameter);

            DelimiterToken t = (DelimiterToken)peekNextToken(MATRIX);
    
            if (t == null) {
            	if (_calledFromCommandLine)
 		           Errors.throwParserException("CCX: continue");
 	        	else
 	        	   Errors.throwParserException("Matrix: missing ]");
            }
            
            if((t.value == ',') ||
               (t.value == ' ')    )
            {
                //this marks the end of a matrix element
                ErrorLogger.debugLine("Parser: Matrix ,");
                getNextToken(MATRIX);
                x++;
            }
            else if((t.value == ';')  ||
                    (t.value == '\n') ||
                    (t.value == '\r')   )
            {
                //this marks the end of a row
                getNextToken(MATRIX);
                x++;
                ErrorLogger.debugLine("Parser: Matrix ; length "+x);
                rowLength.push(new Integer(x)); // save row length 
                x=0;
                y++; // increment row counter
            }
            else if(t.value == ']')
            {
                //this marks the end of the matrix
                getNextToken(MATRIX);
                x++;
                rowLength.push(new Integer(x)); // save row length
                ErrorLogger.debugLine("Parser: Matrix y="+y+" x="+x);
                endMatrix = true;
            }
            else
            {
                Errors.throwParserException("Matrix: missing end");
            }

        } // end while

        // create matrix of correct size
        if (numberB)
        {
            // the parsed matrix is a pure number array
            ErrorLogger.debugLine("Parser: matrix pure numbers");

            // length of rows must be equal
            for (int yy=y-1; yy>=0; yy--)
            {
                if ( ((Integer)rowLength.pop()).intValue() != x)
                    Errors.throwParserException(" Matrix: all rows must have the same length");
            }
            
            double values[][]     = new double[y][x];
            double valuesImag[][] = new double[1][1];

            if (imagIndicatorB)
                valuesImag = new double[y][x];
                
            // fill array with double values from element stack
            for (int yy=y-1; yy>=0; yy--)
            {
                for (int xx=x-1; xx>=0; xx--)
                {
                    DoubleNumberToken n  = (DoubleNumberToken)elements.pop(); 
                    values[yy][xx] = n.getValueRe();
                    if (imagIndicatorB)
                    {
                        valuesImag[yy][xx] = n.getValueIm();
                    }
                }
            }

            if (!imagIndicatorB)
                return  (OperandToken)(new DoubleNumberToken(values));
            else
                return  (OperandToken)(new DoubleNumberToken(values, valuesImag));

        }
        else if (singleB)
        {
            // pure operands (e.g. variables..., [a,b,c])
            ErrorLogger.debugLine("Parser: matrix pure operands");
            OperandToken values[][] = new OperandToken[y][x];

            // fill array with operands from element stack
            for (int yy=y-1; yy>=0; yy--)
            {
                // row length may vary, e.g. a=[b;c,d] 
                x = ((Integer)rowLength.pop()).intValue();
                values[yy] = new OperandToken[x];
                
                for (int xx=x-1; xx>=0; xx--)
                {
                    Expression expr = (Expression)elements.pop();
                    values[yy][xx]  = (OperandToken)expr.getChild(0); 
                }
            }

            return  new MatrixToken(values);
        }
        else if (x==1 && y==1 && elements.empty() )
        {
            // this is supposed to be an empty array
            ErrorLogger.debugLine("Parser: matrix: empty array");
            
            return  new DoubleNumberToken();   // return empty DoubleNumberToken
        }
        else
        {
            // mixed numbers and expressions
            OperandToken values[][] = new OperandToken[y][1];
            
            // fill array with tokens from element stack
            for (int yy=y-1; yy>=0; yy--)
            {
                // row length may vary, e.g. a=[1,2,3;b] with b=[1,2,3]
                x = ((Integer)rowLength.pop()).intValue();
                values[yy] = new OperandToken[x];
                ErrorLogger.debugLine("Parser: matrix: row length: "+x);
                
                for (int xx=x-1; xx>=0; xx--)
                {
                    values[yy][xx] = (OperandToken)(elements.pop());
                }
            }

            return (OperandToken)(new MatrixToken(values));
        }

    } // end parseMatrix
   
    /***********************************************************************************/
    /** this method parses cell arrays (e.g. a={1, "barfoo", rand(3)} )                */
    /* @return   parsed cell array                                                     */
    private OperandToken parseCellArray()
    {
        boolean endMatrix        = false;         //
        int x                    = 0;             // x-size of matrix
        int y                    = 1;             // y-size of matrix
        Stack elements           = new Stack();   // stack for elements
        int rowLength            = 0;   //

        // remove spaces between "{" and first element (e.g. {  2,3} -> {2,3}
        while (isExpectedDelimiter(peekNextToken(MATRIX), ' '))
        		getNextToken();
        
        // parse code of cell array (e.g. {1, rand(3); "hello", 4+5}
        while(!endMatrix) 
        {
            // get next parameter (whitespaces are treated as delimiter, too)
            OperandToken nextParameter = parseArithExpression(MATRIX);    

            ErrorLogger.debugLine("Parser: cell para" + nextParameter);
            // push parameter onto element stack
            if (nextParameter != null)
                elements.push(nextParameter);

            DelimiterToken t = (DelimiterToken)peekNextToken(MATRIX);
    
            if((t.value == ',') ||
               (t.value == ' ')    )
            {
                //this marks the end of a matrix element
                ErrorLogger.debugLine("Parser: CellArray ,");
                getNextToken(MATRIX);
                x++;
            }
            else if((t.value == ';')  ||
                    (t.value == '\n') ||
                    (t.value == '\r')   )
            {
                //this marks the end of a row
                getNextToken(MATRIX);
                x++;
                ErrorLogger.debugLine("Parser: CellArray ; length "+x);
                
                if ((rowLength>0) && (rowLength!=x))
                	Errors.throwParserException(" Cell Array: unequal rows");

                rowLength = x; // save row length 
                x         = 0; // reset column count
                y++;           // increment row counter
            }
            else if(t.value == '}')
            {
                //this marks the end of the matrix
                getNextToken(MATRIX);
                x++;
                if ((rowLength>0) && (rowLength!=x))
                	Errors.throwParserException(" Cell Array: unequal rows }");
                
                ErrorLogger.debugLine("Parser: CellArray y="+y+" x="+x);
                endMatrix = true;
            }
            else
                Errors.throwParserException(" CellArray error");

        } // end while

        // check if this is supposed to be an empty array
        if (x==1 && y==1 && elements.empty() )
        {
            ErrorLogger.debugLine("Parser: cell: empty array");
            
            return  new CellArrayToken();   // return empty CellToken
        }

        // created final array of operands
        OperandToken values[][] = new OperandToken[y][x];
        
        // fill array with tokens from element stack
        for (int yy=y-1; yy>=0; yy--)
        {
            for (int xx=x-1; xx>=0; xx--)
            {
                values[yy][xx] = (OperandToken)(elements.pop());
            }
        }

        return (OperandToken)(new CellArrayToken(values));

    } // end parseCellArray

/***************************************************************************************/
/********             additional/helper methods                              ***********/
/***************************************************************************************/
   
    /***********************************************************************************/
    /** return false if an expression is terminated by ","                               */
    /** return true if  an expression is terminated by something else ", EOF"          */ 
    private boolean isDisplayResultToken(Token token)
    {
        // if expression is terminated be nothing it should be displayed
        if (token == null)
            return true;
            
        if(token instanceof DelimiterToken)
        {
            if ( ((DelimiterToken)token).value == ';')
               {
                 // expression is terminated by ";" don't display result
                //ErrorLogger.debugLine("Parser: setDisplayResult(false)");
                return false;
            }
            
            // expression is terminated by delimiter unequal ";"
            return true;
        }
        
        // expression is not terminated by a delimiter
        return false;
    }

    /***********************************************************************************/
    /** return false if the token is no delimiter OR not the specified char            */
    /** return true  if the token is a delimiter and is the specified char, e.g. ,;(){}*/ 
    private boolean isExpectedDelimiter(Token token, char c)
    {
        // check type of token
        if(!(token instanceof DelimiterToken))
            return false;
        
         // check if value of delimiter token is as expected
        if( ((DelimiterToken)token).value != c)
            return false;
            
        // token IS a delimiter token AND the value is as expected
        return true;
    }
    
    private boolean isExpectedDelimiter(Token token, String c)
    {
        // check type of token
        if(!(token instanceof DelimiterToken))
            return false;
        
         // check if value of delimiter token is as expected
        if( ! ( ((DelimiterToken)token).getWordValue().equals(c) ) )
            return false;
            
        // token IS a delimiter token AND the value is as expected
        return true;
    }
} // end class

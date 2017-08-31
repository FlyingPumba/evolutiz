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


import java.util.Enumeration;

import com.addi.core.interpreter.*;

/**Class implementing a tree where each node has a variable no of children*/
public class Expression extends OperandToken
{
    /**array containing the expressions operands*/
    private OperandToken[] children;

     /**The number of operands*/
    private int noChildren = 0;

    /**the operator being held within the node*/
    private OperatorToken data;
    
    /**Stores the index of the child being executed*/
    private int childNo;

    /**Default constructor - creates an expression with a null operator and no operands*/
    public Expression()
    {
        super(50);// , "EXPRESSION");
        data         = null;
        children     = null; 
    }

    /**Creates an expression with no operands
    @param _data = the expressions operator*/    
    public Expression(OperatorToken _data)
    {
        super(50); 
        data         = _data;
        children     = null; 
    }

    /**Creates an expression with one operand
    @param _data = the expressions operator
    @param operand = the expressions operand*/    
    public Expression(OperatorToken _data, OperandToken operand)
    {
        super(50); 
        data         = _data;
        children     = new OperandToken[1]; 
        children[0] = operand;
        noChildren     = 1;
    }
    
    /**Creates an expression with two operands
    @param _data = the expressions operator
    @param left  = the left hand operand
    @param right = the right hand operand*/    
    public Expression(OperatorToken _data, OperandToken left, OperandToken right)
    {
        super(50); 
        data         = _data;
        children    = new OperandToken[2]; 
        children[0] = left;
        children[1] = right;
        noChildren  = 2;
    }

    /**Creates an expression with three operands
    @param _data   = the expressions operator
    @param first   = the left hand operand
    @param second  = the middle operand
    @param third   = the right hand operand*/    
    public Expression(OperatorToken _data, OperandToken op1, 
                                           OperandToken op2,
                                           OperandToken op3)
    {
        super(50); 
        data        = _data;
        children    = new OperandToken[3]; 
        children[0] = op1;
        children[1] = op2;
        children[2] = op3;
        noChildren  = 3;
    }

    /**Creates an expression with an array of operands
    @param data         = the expressions operator
    @param operands     = and array of operands
    @param _noChildren     = the number of operands*/    
    public Expression(OperatorToken _data, OperandToken[] _children, int _noChildren)
    {
        super(50); 
        data         = _data;
        children     = _children;
        noChildren   = _noChildren;
    }

    /**retrieves the data object
    @return the operator assigned to the expression*/
    public OperatorToken getData()
    {
        return data;
    }
    
    /**@return the number of children*/
    public int getNumberOfChildren()
    {
        return noChildren;
    }

    /**Sets the operator of the expression
    @param _data = the expressions operator*/
    public void setData(OperatorToken _data)
    {
        data = _data;
    }

    /**Get the a child with a specific index
    @param childNo = the index of the operand
    @return the specified operand*/
    public OperandToken getChild(int childNo)
    {
        return children[childNo];
    }

    /**set the child with a specific index
    @param childNo = the index of the child
    @param child = the value to set it to
    */
    public void setChild(int childNo, OperandToken child)
    {
        children[childNo] = child;
    }
    
    /**Get the first child of this node
    @return the first operand*/
    public OperandToken getLeft()
    {
        return children[0];
    }

    /**Get the last child of this node
    @return the last operand*/
    public OperandToken getRight()
    {
        return children[noChildren - 1];
    }

    /**insert a child node on this Expression
    if _data is a Expression then it just gets added to the current node
    Otherwise a new node is created and this is added to the current node
    @param _data = the operand to add*/
    public void insert(Token _data)
    {
        OperandToken subExpression;

        //if the item is a Expression node then just add it to the Expression
        //without creating a new node
        if(_data instanceof Expression)
            subExpression = ((Expression)_data);
        else if(_data instanceof OperandToken)
            subExpression = ((OperandToken)_data);
        else
            subExpression = new Expression(((OperatorToken)_data));

        // check if no of children exceeds dimension of array
        if (children != null)
        {
            if (noChildren >= children.length )
            {
                int childrenOldLength = children.length;

                ErrorLogger.debugLine("Expression: expand children array "+childrenOldLength);

                OperandToken[] childrenTemp = new OperandToken[childrenOldLength];

                // save old children array to temporary array 
                for (int i=0; i<childrenOldLength; i++)
                {
                    childrenTemp[i] = children[i];
                }

                // create new children array: size +1
                children = new OperandToken[ childrenOldLength + 1 ];
        
                // restore temporary array to new children array
                for (int i=0; i<childrenOldLength; i++)
                {
                    children[i] = childrenTemp[i];
                }
            }
        }
        else
        {
            // create first children. Expression didn't have children before
            children = new OperandToken[1];
        }
        
        children[noChildren] = subExpression;

        noChildren++;
    }

    /**evaluate the data item held within this node
    @param ops = the expressions operands
    @return the result of the expression as an OperandToken*/
    public OperandToken evaluate(Token[] ops, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        OperandToken result = null;
        ErrorLogger.debugLine("Expression: evaluate " + toString());
        
        // for assignments (e.g.: a=3+3) only evaluate the right side
        if(data instanceof AssignmentOperatorToken)        
        {    
            // data is an assignment (e.g. a=3 or [x,t]=func(....) or a(:,3) = [...] )
            OperandToken left  = children[0];
            OperandToken right = children[1];
            
            if (right instanceof Expression) {
            	if (((Expression)right).data instanceof AssignmentOperatorToken) {
            		((Expression)right).data.setDisplayResult(this.isDisplayResult());
            		((AssignmentOperatorToken)((Expression)right).data).returnResult = true;
            	}
            }
            
            //ErrorLogger.debugLine("Expression: evaluate assignment");
            //ErrorLogger.debugLine("Expression: evaluate assignment "+left.toString());
            //ErrorLogger.debugLine("Expression: evaluate assignment "+right.toString());
            
                           
            /* Check how many arguments are on the LEFT-hand side and pass this */
            /*  number to a possible function token on the RIGHT-hand side.     */ 
            if ((left  instanceof MatrixToken)  && 
                (right instanceof FunctionToken)   ) 
            {
                int x = 1;
                
                x = ((MatrixToken)left).getSizeX();
                ErrorLogger.debugLine("Expression: [ "+x+" ]=func()");
                FunctionToken func = ((FunctionToken)right);
                func.setNoOfLeftHandArguments(x);
                children[1] = func;
            }
        
            // evaluate right-hand argument (e.g. a= 2+3)
            children[1] = right.evaluate(null, globals);
            
            //check LEFT side for submatrices (e.g. a(1,:) = [1,2,3] )
            if (left instanceof FunctionToken)
            {
                // A function can never be valid on the left side of an expression
                //   therefore this MUST be variable
                //   (e.g. a(3,2)=5 )

                FunctionToken   function = (FunctionToken)left;
                
                ErrorLogger.debugLine("Expression: eval: function/variable on left side");    

                // create variable token from function data
                children[0] = new VariableToken(function.getName(), function.getOperands());
            }
        }
        else if(data instanceof UnaryOperatorToken && (((UnaryOperatorToken)data).getValue() == '+' || 
                                                       ((UnaryOperatorToken)data).getValue() == '-'   ) )
        {
            //!!! is this line really necessary !!!!???
            //do nothing
        }
        else if(data instanceof DotOperatorToken)        
        {
            // (e.g. a.getLambda()  or  a.color  or  a.argument1)
            // don't evaluate children.
        }
        else
        {
            // the data of this expression is null or of no interest 
            // evaluate all children
            boolean dispB = false;
            for(int i = 0; i < noChildren; i++)
            {
                if(children[i] != null)
                {
                    ErrorLogger.debugLine("Expression: child: "+children[i].toString());
                    //ErrorLogger.debugLine("Expression: globals: "+globals);

                    // check if result should be displayed
                    dispB = children[i].isDisplayResult(); 
                    
                    // evaluate children
                    children[i] = children[i].evaluate(null, globals);                    
                     
                    // check if result should be displayed before
                    if (dispB && children[i]!=null)
                        children[i].setDisplayResult(true);
                    
                }
            } // end for
        }
        
        // ******************************************************************************
        // evaluate operator with its children                    
        if(data != null)
        {
            // display "a=11", do not display "a=11;"
            if (isDisplayResult() && (data instanceof AssignmentOperatorToken))
                data.setDisplayResult(true); 
            
            // evaluate expression
            result = data.evaluate(children, globals); 
            
            /* set the display state of the result, if the original expression
               also has the display state set  */
            if (isDisplayResult() && (result != null))
                result.setDisplayResult(true);
        }
        else
        {
            // operator data is null
            // the result of this expression might be hidden in the first child
            // (e.g. (2+3)*4  here (2+3) will be hidden inside an expression)
            result = children[0];
             
            /*store operand of expressions without data in "ans" variable*/
            for(int i = 0; i < noChildren; i++)
            {
                if (children[i]!=null)
                {
                    ErrorLogger.debugLine("Expression: store ans "+children[i].toString());
                    Variable answervar = globals.createVariable("ans");
                    answervar.assign(children[i]);
                }
                
                /* display the result this expression in the user console*/
                if ((children[i] != null)          &&
                     children[i].isDisplayResult()    )
                {
                    //ErrorLogger.debugLine("Expression: !!!!!!!!! showResult");
                    globals.getInterpreter().displayText(" ans = "+ children[i].toString(globals));
                }
            }                                
        }                

        return result;
    }  // end evaluate

    /**Converts the expression to a string
    @return string representation of expression */
    public String toString()
    {
        String result = "";
        if (data != null)
        {
            result = data.toString(children);
        }
        else
        {
            if (children == null)
                return "";
            
            if (children[0] != null)  
                result = children[0].toString();
        }

        return result;
    }

    /**Builds an expression tree
    @param op      = the expressions operator
    @param left  = the left hand operand
    @param right = the right hand operand
    @return the expression created*/
    private Expression buildTree(OperatorToken op, OperandToken left, OperandToken right)
    {
        Expression tree = new Expression(op);

        tree.insert(left);
        tree.insert(right);
        
        return tree;
    }    

    /**Checks if this operand is a numeric value
    @return true if this is a number, false if it's 
    an algebraic expression*/
    public boolean isNumeric()
    {
        boolean numeric = true;
        
        for(int childNo = 0; childNo < noChildren; childNo++)
        {
        if(!children[childNo].isNumeric())
        numeric = false;
        }
        
        return numeric;
    }
    
    /**@return the index number of the current child expression*/
    public int getChildNo()
    {
        return childNo;
    }
    
    /**@return the expression being executed*/
    public OperandToken getCurrentChild()
    {
        return children[childNo];
    }
 
    /**checks if this is a leaf node of the expression tree
    @return false if this expression has any children*/
    public boolean isLeaf()
    {
        return (noChildren == 0);
    }
    
    /**function to access all children of a node within the expression tree
    @return all the nodes children as a enumeration*/
    public Enumeration getChildren()
    {
        return new ExpressionEnumeration();
    }
    
    class ExpressionEnumeration implements Enumeration
    {
        private int index;
        
        public ExpressionEnumeration()
        {
            index = 0;
        }
        
        public boolean hasMoreElements()
        {
            return (index < noChildren);
        }
        
        public Object nextElement()
        {
            OperandToken element = children[index];
            index++;
            return element;
        }
    }
}

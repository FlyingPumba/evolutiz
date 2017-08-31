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

package com.addi.toolbox.jmathlib.matrix;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function for assigning a sub matrix to another matrix   */
/* (e.g.: subassign(a,b,1,1:2) will assign matrix b to matrix a.       */
/* Matrix indices start with <b>1</b> and <b>not</b> with <b>0</b>.    */
public class subassign extends ExternalFunction
{
    
    boolean leftCellB = false;
    
    public void setLeftCell()
    {
        leftCellB = true;
    }

        
	/**return a sub matrix and assign a submatrix
	@param operands[0] = original matrix 
	@param operands[1] = replacement matrix 
	@param operands[1] = vertical limits (1 or 1:3 or : ) 
	@param operands[2] = horizontal limits (optional) (1 or 1:3 or : )
	(e.g.: a= subassign(a,b,1,1) assigns the value b to the element (1,1) of matrix a, 
     a = subassign(a,b,:,2)      assigns b as the second column of a,
     a = subassign(a,b,2:3,0:2)  assigns b as a 2-by-3 submatrix of a,
	 a = subassign(a,b,2) returns the first element of the second row of a*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
        DataToken retToken = null;

		// at least three operands (e.g. subassign(a,b,2) )
        // maximum is 4 operands (e.g. a=subassign(a,b,2,4) )
		if ((getNArgIn(operands)<3) ||
            (getNArgIn(operands)>4)    )
			throwMathLibException("SubAssign: number of arguments < 3 or >4");

        if (operands[0]!=null) debugLine("SubAssign1: "+operands[0].toString());
        if (operands[1]!=null) debugLine("SubAssign2: "+operands[1].toString());
        if (operands[2]!=null) debugLine("SubAssign3: "+operands[2].toString());

		// first two operands must be DataTokens (e.g. subassign(a,b,..) )
		if ((operands[0] != null)                &&
	        (!(operands[0] instanceof DataToken))   ) 
		    throwMathLibException("SubAssign: first argument must be a data token");

        if  (!(operands[1] instanceof DataToken))     
			throwMathLibException("SubAssign: second argument must be a data token");
		
        // e.g. a={3,4,5}  then a(2)=99 is wrong, must be a(2)={99}
        // because "a" is a cell array.
        if ( !leftCellB && 
            (operands[0] instanceof CellArrayToken) &&
            !(operands[1] instanceof CellArrayToken)    )
            throwMathLibException("SubAssign: conversion to cell from something not possible");
        
		// values of the original matrix
        int dy = 0;
		int	dx = 0; 
		if (operands[0]!=null)
        {
            // original matrix does not have any values yet
            // e.g. subassign(null,9,1)
		    dy          = ((DataToken)operands[0]).getSizeY(); 
            dx          = ((DataToken)operands[0]).getSizeX();
        }
		else
        {
		    // e.g. subassign(null,888,3)
            // create return token as same type as '888'
		    operands[0] = ((DataToken)operands[1]).getElementSized(1,1);
        }
        
        // size of replacement matrix  
		int dy_r = ((DataToken)operands[1]).getSizeY(); 
		int	dx_r = ((DataToken)operands[1]).getSizeX(); 

        // first limit (y-limit)
        int        y_dy      = 0;    // y-size of first limit 
        int        y_dx      = 0;    // x-size of first limit 
        double[][] y_indexes = null; // elements
        int        dy_max    = 0;

        // second limit (x-limit)
        int        x_dy      = 0;    // y-size of second limit 
        int        x_dx      = 0;    // x-size of second limit 
        double[][] x_indexes = null; // elements
        int        dx_max    = 0;

        // evaluate vertical selection (e.g. subassign(a,<....>,3) )
        if(operands[2] instanceof DoubleNumberToken)
        {
            // e.g. subassign(a,<number>) or subassign(a,<number>,4)
            // subassign(a,3:5) 
        }
        else if(operands[2] instanceof Expression)
        {
            // e.g.  subassign(a,:) or subassign(a,2:end)
            Expression    expr = (Expression)operands[2];
            OperatorToken op   = (OperatorToken)expr.getData();

            debugLine("subassign expr="+operands[2].toString());
            
            // check if expression contains colon, e.g. (:) , (3:end)
            if ((op == null)                         ||
                (!(op instanceof ColonOperatorToken))  )
                throwMathLibException("SubAssign: colon error");

            OperandToken colonOp = null;
            
            // possible colon operations. e.g. (:),(2:end),(2:3:end)
            if (expr.getNumberOfChildren() == 2)
            {
                // Get operands (e.g. <1>:<5>)
                OperandToken left  = expr.getChild(0);
                OperandToken right = expr.getChild(1);
                    
                if ( (!(right instanceof DelimiterToken))                   ||
                     (!((DelimiterToken)right).getWordValue().equals("end"))  )
                        throwMathLibException("SubAssign: wrong delimiter");
                
                // "end" delimiter indicates total number of values or
                //   just the number of rows
                // if two   arguments: e.g. subassign(a,3:end)     -> 3:dy*dx 
                // if three arguments: e.g. subassign(a,3:end,4:8) -> 3:dy
                if (getNArgIn(operands)==3)
                    right = new DoubleNumberToken(dy*dx);
                else
                    right = new DoubleNumberToken(dy);

                // create new ColonOperator and return new indexes
                colonOp = new Expression(new ColonOperatorToken(), left, right);
            }
            else if (expr.getNumberOfChildren() == 3)
            {
                // e.g. (2:3:end)
                // Get operands (e.g. <1>:<5>)
                OperandToken left   = expr.getChild(0);
                OperandToken middle = expr.getChild(1);
                OperandToken right  = expr.getChild(2);
                    
                if ( (!(right instanceof DelimiterToken))                   ||
                     (!((DelimiterToken)right).getWordValue().equals("end"))  )
                        throwMathLibException("SubAssign: wrong delimiter");
                
                // "end" delimiter indicates total number of values or
                //   just the number of rows
                // if two   arguments: e.g. subassign(a,3:2:end)     -> 3:2:dy*dx 
                // if three arguments: e.g. subassign(a,3:2:end,4:8) -> 3:2:dy
                if (getNArgIn(operands)==3)
                    right = new DoubleNumberToken(dy*dx);
                else
                    right = new DoubleNumberToken(dy);

                // create new ColonOperator and return new indexes
                colonOp = new Expression(new ColonOperatorToken(), left, middle, right);
            }
            else if (expr.getNumberOfChildren() == 0)
            {
                // ":" indicates all indexes of matrix/rows
                // if two   arguments: e.g. subassign(a,:)   -> ALL elements
                // if three arguments: e.g. subassign(a,3,:) -> all rows
                int len = 0;
                if (getNArgIn(operands)==3)
                    len = (dy*dx);
                else
                    len = dy;

                colonOp = new Expression(new ColonOperatorToken(),
                                         new DoubleNumberToken(1),
                                         new DoubleNumberToken(len)    );

            }
            else
                throwMathLibException("SubAssign: colon wrong number of childs");

            // evaluate new colon expression
            colonOp = colonOp.evaluate(null, globals);
            
            if ( !(colonOp instanceof DoubleNumberToken))
                throwMathLibException("SubAssign: colon error wrong type");
              
            // e.g. a(:) must return a column vector
            if (getNArgIn(operands)==2)
                colonOp = colonOp.transpose();

            // copy new array of indices to second operand of SubAssign
            operands[2]= colonOp;

        }        
        else if(operands[2] instanceof LogicalToken)
        {
            // e.g. subassign(a,[true,true,false,true])
            LogicalToken l = (LogicalToken)operands[2];
            
            // find number of elements unequal zero
            int n = 0;
            for (int i=0; i<l.getNumberOfElements(); i++)
            {
                if (l.getValue(i))
                    n++;
            }
            
            // create index array from boolean values
            // eg. a=[true,true,false,true] ->[1,2,4] (use indices with "true")
            double[][] values = new double [1][n];
            int ni =0;
            for (int i=0; i<l.getNumberOfElements(); i++)
            {
                if (l.getValue(i))
                {
                  values[0][ni] = (double)i + 1;
                  ni++;
                }
            }            
            operands[2] = new DoubleNumberToken(values,null);
        }
        else
            throwMathLibException("SubAssign: eval: unknown operand");

        // get limits size and indices
        y_dy      = ((DoubleNumberToken)operands[2]).getSizeY();
        y_dx      = ((DoubleNumberToken)operands[2]).getSizeX();
        y_indexes = ((DoubleNumberToken)operands[2]).getReValues();
        ErrorLogger.debugLine("SubAssign: y_dy="+y_dy+" y_dx="+y_dx);

        // find highest index
        for (int xi=0; xi<y_dx; xi++)
        {
            for (int yi=0; yi<y_dy ; yi++)
            {
                if (y_indexes[yi][xi]> dy_max)
                    dy_max = (int)y_indexes[yi][xi];
                if (y_indexes[yi][xi]<=0)
                    throwMathLibException("SubAssign: index y <=0");
            }
        }
        debugLine("SubAssign: dy_max "+dy_max);
        
        // evaluate horizontal selection (e.g. subassign(a,b,3,<...>) )
        if(operands.length==4)
        {
            debugLine("SubAssign: "+operands[3].toString());
            
            if(operands[3] instanceof DoubleNumberToken)
            {
                // e.g. subassign(a,1,<some array>)
                // e.g. subassign(a,1,2) 
            }
            else if(operands[3] instanceof Expression)
            {
                // e.g.  subassign(a,:) or subassign(a,2:end)
                Expression    expr = (Expression)operands[3];
                OperatorToken op   = (OperatorToken)expr.getData();

                // check if expression contains colon, e.g. (:) , (3:end)
                if ((op == null)                         ||
                    (!(op instanceof ColonOperatorToken))  )
                    throwMathLibException("SubAssign: colon error");

                OperandToken colonOp = null;

                if (expr.getNumberOfChildren() == 2)
                {
                    // subassign(a,3,4:end)
                    OperandToken left  = expr.getChild(0);
                    OperandToken right = expr.getChild(1);
                        
                    if ( (!(right instanceof DelimiterToken))                   ||
                         (!((DelimiterToken)right).getWordValue().equals("end"))  )
                            throwMathLibException("SubAssign: wrong delimiter");
                    
                    // if three arguments: e.g. subassign(a,3,4:end) -> 4:dx
                    right = new DoubleNumberToken(dx);

                    // create new ColonOperator and return new indexes
                    colonOp = new Expression(new ColonOperatorToken(), left, right);
                }
                else if (expr.getNumberOfChildren() == 3)
                {
                    // e.g. (2:3:end)
                    OperandToken left   = expr.getChild(0);
                    OperandToken middle = expr.getChild(1);
                    OperandToken right  = expr.getChild(2);
                        
                    if ( (!(right instanceof DelimiterToken))                   ||
                         (!((DelimiterToken)right).getWordValue().equals("end"))  )
                            throwMathLibException("SubAssign: wrong delimiter");
                    
                    // if three arguments: e.g. subassign(a,3,4:2:end) -> 4:2:dx
                    right = new DoubleNumberToken(dx);

                    // create new ColonOperator and return new indexes
                    colonOp = new Expression(new ColonOperatorToken(), left, middle, right);
                }
                else if (expr.getNumberOfChildren() == 0)
                {
                    // if three arguments: e.g. subassign(a,3,:) -> all columns
                    colonOp = new Expression(new ColonOperatorToken(),
                                             new DoubleNumberToken(1),
                                             new DoubleNumberToken(dx)    );
                }
                else
                    throwMathLibException("SubAssign: colon wrong number of childs");
        
                // evaluate new colon expression
                colonOp = colonOp.evaluate(null, globals);
                
                if ( !(colonOp instanceof DoubleNumberToken))
                    throwMathLibException("SubAssign: colon error wrong type");
                  
                // copy new array of indices to second operand of SubAssign
                operands[3]= colonOp;
            }
            else
                throwMathLibException("SubAssign: eval: unknown operand");
            
            x_dy      = ((DoubleNumberToken)operands[3]).getSizeY();
            x_dx      = ((DoubleNumberToken)operands[3]).getSizeX();
            x_indexes = ((DoubleNumberToken)operands[3]).getReValues();
            ErrorLogger.debugLine("SubAssign: "+x_dy+" "+x_dx);

            // find highest index
            for (int xi=0; xi<x_dx; xi++)
            {
                for (int yi=0; yi<x_dy ; yi++)
                {
                    if (x_indexes[yi][xi]> dx_max)
                        dx_max = (int)x_indexes[yi][xi];
                    if (x_indexes[yi][xi]<=0)
                        throwMathLibException("SubAssign: index x <=0");
                }
            }
            debugLine("SubAssign: dx_max "+dx_max);
        } // end op.length

        
        // look at something like 
        //   subassign(a,b,4)   or 
        //   subassign(a,b,2:3) or
        //   subassign(a,b, : )
        // Something like subassign(a,b,3)
        //  or            subassign(a,c,2:4)
        // a=[1,2,3]       then a(2)=44 should return a=[1,44,3]
        // a=[1,2,3;4,5,6] then a(1:4) should return [1,4,2,5]
        if(getNArgIn(operands)==3)
        {
            if ((dx_r == 0) && (dy_r == 0)) {
            	if (operands[0] instanceof DoubleNumberToken) {
            		DoubleNumberToken tempToken = new DoubleNumberToken();
            		if ((dy==1) || ((dy==0) && (dx==0))) {
            			// subassign([3,4,5],[],4) or subassign([],[],7:9)
            			tempToken.setSize(1,dx-y_dx);
            		} else if (dx==1) { 
            			// subassign[2;3;4],[],5:6)
            			if ((y_dy == 1) && (y_dx > 1)) {
            				tempToken.setSize(dy-y_dx,1);
            			} else {
            				tempToken.setSize(dy-y_dy,1);
            			}
            		}
            		// copy data to return array
            		int n=0;
            		int m=0;
            		for (int xi=0; xi<dx; xi++)
            		{
            			for (int yi=0; yi<dy ; yi++)
            			{
            				boolean matchFound = false;
            				for (int xi2=0; (xi2<y_dx) && (matchFound == false); xi2++)
            				{
            					for (int yi2=0; (yi2<y_dy) && (matchFound == false); yi2++)
            					{
            						int index = (int)y_indexes[yi2][xi2]-1;

            						if (index == n) {
            							matchFound = true;
            						}
            					}
            				}
            				if (matchFound == true) {
            					n++;
            				} else {
            					tempToken.setElement(m,
                                    ((DoubleNumberToken)operands[0]).getElement(n));
            					n++;
            					m++;
            				}
            			} // end yi
            		} // end xi
            		return tempToken;
            	} else {
            		return null;
            	}
            }
            else if ((dy==1) || ((dy==0) && (dx==0)) )
            {
                // subassign([3,4,5],88,4) or subassign([],[66,66,66],7:9)
                ((DataToken)operands[0]).setSize(1,Math.max(dx,dy_max));
            }
            else if (dx==1)
            {
                // subassign[2;3;4],[4,5],5:6)
                ((DataToken)operands[0]).setSize(Math.max(dy,dy_max),1);
            }
            //else
            //    throwMathLibException("SubAssign: assignment not possible");
                
            // new size of data token
            dy          = ((DataToken)operands[0]).getSizeY(); 
            dx          = ((DataToken)operands[0]).getSizeX();

            // different approach if working on cell arrays
            if (operands[0] instanceof CellArrayToken)
            {
                ErrorLogger.debugLine("SubAssign: cell1");
                
                OperandToken data = (OperandToken)operands[1];
                
                // e.g. a{2}={44} -> a={...,44,...}
                if (!leftCellB && (data instanceof CellArrayToken))
                    data = ((CellArrayToken)data).getElement(0);

                ((DataToken)operands[0]).setElement((int)y_indexes[0][0]-1, data);

                return (DataToken)operands[0]; 
            }
           

            // no. of elements to copy must be the size of the second matrix
            // (e.g. b=[1,2,3] -> size 1*3   so subassign(a,b,2:4) -> 2:4=>3
            // (exception: a([2,6,3])=6 is also possible
            if (((y_dy*y_dx) != (dy_r*dx_r)) && (dy_r>1 || dx_r>1))
                throwMathLibException("SubAssign: no. of elements to copy must be the same as second array"); 

            // copy data to return array
            int n=0;
            for (int xi=0; xi<y_dx; xi++)
            {
                for (int yi=0; yi<y_dy ; yi++)
                {
                    int index = (int)y_indexes[yi][xi]-1;
                    if ((index<0) || (index>dy*dx-1))
                            throwMathLibException("SubAssign: index exceeds array dimensions");

                    // different approach if working on cell arrays
                    // if a={'asdf',[4,5]} then a{1,2} will return a number token [4,5] 
                   /* if ((operands[0] instanceof CellArrayToken) && leftCellB)
                    {
                        ErrorLogger.debugLine("SubAssign: cell1");
                        return ((DataToken)operands[0]).getElement(y,x); 
                    }
                    */
                    
                    // copy original values to return array
                    if ((dy_r==1) && (dx_r==1))
                        ((DataToken)operands[0]).setElement(index,
                                        ((DataToken)operands[1]).getElement(0));
                    else
                        ((DataToken)operands[0]).setElement(index,
                                        ((DataToken)operands[1]).getElement(n));
                    
                    n++;
                    
                } // end yi
            } // end xi
            return (DataToken)operands[0];
            
        } // end 3 operands
        else
        {
            // 4 operands
            // dimensions are within the original matrix
            //ErrorLogger.debugLine("SubAssign: y_min="+y_min+" y_max="+y_max);   
            //ErrorLogger.debugLine("SubAssign: x_min="+x_min+" x_max="+x_max);   
    
            // look at something like
            // subassign(a,b,2,3)
            // subassign(a,b,2:3,3)
            // subassign(a,b,2:3,5:6)
            // subassign(a,b,:,:)
            // subassign(a,b,:,1)
            // subassign(a,b,:,1:2)
            // subassign(a,b,1,:)
            // subassign(a,b,1,2:3)
            
            // a(1:2,1:2)=[1,2,3,4] is not allowed
            // a(1:2,1:2)=[1,2;3,4] is     allowed
            // (exception: a([2,6,3],[4,5])=6 is also possible

            if ( ((y_dy*y_dx)>1) && ((x_dy*x_dx)>1)            &&
                 ( ((y_dy*y_dx)!=dy_r) || ((x_dy*x_dx)!=dx_r) )  && 
                 (dy_r>1 || dx_r>1)  ) 
                throwMathLibException("SubAssign: A(matrix,matrix)=B, size of B must match (matrix,matrix)"); 

            // check if new array will be larger than original one
            //  -> if yes create a larger array first
            // e.g. a=[1,2;3,4] -> a(1,4)=55 -> a=[1,2,0,55;3,4,0,0]
            if ( (dy_max>dy) || (dx_max>dx) )
            {
                // new size of data token
                ((DataToken)operands[0]).setSize(Math.max(dy_max,dy),
                                                 Math.max(dx_max,dx) );
                dy          = ((DataToken)operands[0]).getSizeY(); 
                dx          = ((DataToken)operands[0]).getSizeX();
            }
            debugLine("SubAssign: dy="+dy+" dx="+dx);
    
            // different approach if working on cell arrays
            if (operands[0] instanceof CellArrayToken)
            {
                ErrorLogger.debugLine("SubAssign: cell2");
                
                OperandToken data = (OperandToken)operands[1];
                
                // e.g. a{2}={44} -> a={...,44,...}
                if (!leftCellB && (data instanceof CellArrayToken))
                    data = ((CellArrayToken)data).getElement(0);
    
                ((DataToken)operands[0]).setElement((int)y_indexes[0][0]-1,
                                                    (int)x_indexes[0][0]-1,
                                                    data);
    
                return (DataToken)operands[0]; 
            }
    
            // Check if range of indices is equal to the size of the replacement matrix
            // (exception a([1,2],6)=99 is also possible)
            if (((y_dy*y_dx*x_dy*x_dx) != (dy_r*dx_r)) && (dy_r>1 || dx_r>1))
                throwMathLibException("SubAssign: no. of elements to copy must be the same as second array"); 

            
            int n=0;
            // work through x_indixces
            for (int xxi=0; xxi<x_dx; xxi++)
            {
                for (int xyi=0; xyi<x_dy ; xyi++)
                {
                    // check if row-number is valid
                    int indexX = (int)x_indexes[xyi][xxi]-1;
                    if ((indexX<0) || (indexX>dx-1))
                            throwMathLibException("SubAssign: index X exceeds array dimensions");
    
                    // work through y_indexes
                    for (int yxi=0; yxi<y_dx; yxi++)
                    {
                        for (int yyi=0; yyi<y_dy; yyi++)
                        {
                            //ErrorLogger.debugLine("SubAssign: y="+y+" x="+x);
                            
                            // check if column-number is valid
                            int indexY = (int)y_indexes[yyi][yxi]-1;
                            if ((indexY<0) || (indexY>dy-1))
                                    throwMathLibException("SubAssign: index Y exceeds array dimensions");
    
                            // different approach if working on cell arrays
                            // if a={'asdf',[4,5]} then a{1,2} will return a number token [4,5] 
                            /*if ((operands[0] instanceof CellArrayToken) && leftCellB)
                            {
                                ErrorLogger.debugLine("SubAssign: cell1");
                                return ((DataToken)operands[0]).setElement((int)y_indexes[yyi][yxi]-1,
                                                                           (int)x_indexes[xyi][xxi]-1,
                                                                           ); 
                            }
                            */
                            //debugLine("sub "+n+"  "+((DataToken)operands[1]).getElement(n).toString());
                            debugLine("sub "+(y_indexes[yyi][yxi]-1)+" "+(x_indexes[xyi][xxi]-1));

                            // copy original values to return array
                            if ((dy_r==1) && (dx_r==1))
                                ((DataToken)operands[0]).setElement((int)y_indexes[yyi][yxi]-1, 
                                                                    (int)x_indexes[xyi][xxi]-1,
                                                                    ((DataToken)operands[1]).getElement(0) );
                            else
                                ((DataToken)operands[0]).setElement((int)y_indexes[yyi][yxi]-1, 
                                                                    (int)x_indexes[xyi][xxi]-1,
                                                                    ((DataToken)operands[1]).getElement(n) );
                                
                            n++;
                        } // end yyi
                    } // end yxi
                } // end xyi
            } // end xxi
    
    		return (DataToken)operands[0]; 		
        } // end operands.length
        
	} // end eval
}

/*
@GROUP
matrix
@SYNTAX
subassign(matrix, replacement matrix, vertical limit, [horizontal limit])
@DOC
Assigns a section of replacement matrix to matrix.
@NOTES
@EXAMPLES
<programlisting>
subassign(a,b,1,1:2) 
a=[2,3,4,5]; a(1,3)=77
</programlisting>
@SEE
submatrix

*/


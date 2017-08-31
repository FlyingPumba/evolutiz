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


/**An external function for creating a sub matrix of a DoubleNumberToken           */
/* (e.g.: submatrix(a,1,1:2) will return the the elements 1 and 2 from row 1 */
public class submatrix extends ExternalFunction
{

    boolean leftCellB = false;
    
    public void setLeftCell()
    {
        leftCellB = true;
    }

	/**return a sub matrix 
	@param operands[0] = matrix 
	@param operands[1] = vertical limits (1 or 1:3 or : ) 
	@param operands[2] = horizontal limits (optional) (1 or 1:3 or : )
	(e.g.: submatrix(a,0,0) returns the top left element of a,      	<br>
     submatrix(a,:,2) returns the second column of a,					<br>
     submatrix(a,2:3,0:2) returns a 2-by-3 submatrix of a,				<br>
	 submatrix(a,2) returns the first element of the second row of a	<br>
     e.g.: a=[1,2,3,4,5; 												<br>
              6,7,8,9,10;												<br>
              1,2,3,4,5]                                                <br>
     then  submatrix(a,1,2)   returns 2                                 <br>
     then  submatrix(a,3,:)   returns [3,8,3]'	       					<br>
     then  submatrix(a,2:5,2) returns [7,8,9,10] 						<br>
     then  submatrix(a,:)     return [1,2,3,4,5,6,7,8,9...]' column     <br>
         (also see subassign() )
     */
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		// at least two operands (e.g. submatrix(a,2) )
		if ((getNArgIn(operands)<2) || (getNArgIn(operands)>3))
			throwMathLibException("SubMatrix: number of arguments <2 or >3");

		// first operand must be a DoubleNumberToken
		if (!(operands[0] instanceof DataToken))
			throwMathLibException("SubMatrix: first argument must be a data token");
		
		// values of the data array 
		int        dy        = ((DataToken)operands[0]).getSizeY(); 
		int		   dx        = ((DataToken)operands[0]).getSizeX(); 

        // first limit (y-limit)
        int        y_dy      = 0;    // y-size of first limit 
        int        y_dx      = 0;    // x-size of first limit 
        double[][] y_indexes = null; // elements

        // second limit (x-limit)
        int        x_dy      = 0;    // y-size of second limit 
        int        x_dx      = 0;    // x-size of second limit 
        double[][] x_indexes = null; // elements


		ErrorLogger.debugLine("SubMatrix: "+operands[1].toString());

		// evaluate VERTICAL selection (e.g. submatrix(a,<something>,3) )     
		if(operands[1] instanceof DoubleNumberToken)
		{
			// e.g. submatrix(a,<number>) or submatrix(a,<number>,4)
            // submatrix(a,3:5) 
		}
        else if(operands[1] instanceof LogicalToken)
        {
            // e.g. submatrix(a,[true,true,false,true])
            LogicalToken l = (LogicalToken)operands[1];
            
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
            operands[1] = new DoubleNumberToken(values,null);
            
        }
        else if(operands[1] instanceof Expression)
		{
			// e.g.  submatrix(a,:) or submatrix(a,2:end)
			Expression    expr = (Expression)operands[1];
			OperatorToken op   = (OperatorToken)expr.getData();

            // check if expression contains colon, e.g. (:) , (3:end)
            if ((op == null)                         ||
                (!(op instanceof ColonOperatorToken))  )
                throwMathLibException("SubMatrix: colon error");

            OperandToken colonOp = null;
            
			// possible colon operations. e.g. (:),(2:end),(2:3:end)
            if (expr.getNumberOfChildren() == 2)
			{
				// Get operands (e.g. <1>:<5>)
				OperandToken left  = expr.getChild(0);
				OperandToken right = expr.getChild(1);
                    
                if ( (!(right instanceof DelimiterToken))                   ||
                     (!((DelimiterToken)right).getWordValue().equals("end"))  )
                        throwMathLibException("SubMatrix: wrong delimiter");
                
                // "end" delimiter indicates total number of values or
                //   just the number of rows
                // if two   arguments: e.g. submatrix(a,3:end)     -> 3:dy*dx 
                // if three arguments: e.g. submatrix(a,3:end,4:8) -> 3:dy
                if (getNArgIn(operands)==2)
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
                        throwMathLibException("SubMatrix: wrong delimiter");
                
                // "end" delimiter indicates total number of values or
                //   just the number of rows
                // if two   arguments: e.g. submatrix(a,3:2:end)     -> 3:2:dy*dx 
                // if three arguments: e.g. submatrix(a,3:2:end,4:8) -> 3:2:dy
                if (getNArgIn(operands)==2)
                    right = new DoubleNumberToken(dy*dx);
                else
                    right = new DoubleNumberToken(dy);

                // create new ColonOperator and return new indexes
                colonOp = new Expression(new ColonOperatorToken(), left, middle, right);
            }
			else if (expr.getNumberOfChildren() == 0)
			{
                // ":" indicates all indexes of matrix/rows
                // if two   arguments: e.g. submatrix(a,:)   -> ALL elements
                // if three arguments: e.g. submatrix(a,3,:) -> all rows
                int len = 0;
                if (getNArgIn(operands)==2)
                    len = (dy*dx);
                else
                    len = dy;

                colonOp = new Expression(new ColonOperatorToken(),
                                         new DoubleNumberToken(1),
                                         new DoubleNumberToken(len)    );

            }
            else
                throwMathLibException("SubMatrix: colon wrong number of childs");

            // evaluate new colon expression
            colonOp = colonOp.evaluate(null, globals);
            
            if ( !(colonOp instanceof DoubleNumberToken))
                throwMathLibException("SubMatrix: colon error wrong type");
              
            // e.g. a(:) must return a column vector
            if (getNArgIn(operands)==2)
                colonOp = colonOp.transpose();

            // copy new array of indices to second operand of SubMatrix
            operands[1]= colonOp;

        }
        else
           	throwMathLibException("SubMatrix: eval: unknown operand");

        // get limits size and indices
        y_dy      = ((DoubleNumberToken)operands[1]).getSizeY();
        y_dx      = ((DoubleNumberToken)operands[1]).getSizeX();
        y_indexes = ((DoubleNumberToken)operands[1]).getReValues();
        ErrorLogger.debugLine("SubMatrix: "+y_dy+" "+y_dx);

        
		/***********************************************************************/
		// create return array for e.g. submatrix(a,5) or submatrix(a,3:5)   
        // or submatrix(a,3:end) or submatrix(a,:) or submatrix(a,[1,6,2,4])
        if(getNArgIn(operands)==2)
		{
        	//if array is a column and selection is a row, the result is a column
        	int dy_r = y_dy;
        	int dx_r = y_dx;
        	if ((dy_r == 1) && (dx_r > 1) && (dx == 1) && (dy > 1)) {
        		int temp = dx_r;
        		dx_r = dy_r;
        		dy_r = temp;
        	}
            // create return array with size of limits operator
            DataToken retToken = ((DataToken)operands[0]).getElementSized(dy_r,dx_r);
            
            int returnCount = 0;
            // copy data to return array
            for (int xi=0; xi<y_dx; xi++)
            {
                for (int yi=0; yi<y_dy ; yi++)
                {
                    int index = (int)y_indexes[yi][xi]-1;
                    if ((index<0) || (index>dy*dx-1))
                            throwMathLibException("SubMatrix: index exceeds array dimensions");

                    // find position in array (y,x) by index
                    int x = (int)(index/dy); // column of original data
                    int y = index - x*dy;    // row of original data
                    
                    int x_r = (int)(returnCount/dy_r); // column of original data
                    int y_r = returnCount - x_r*dy_r;    // row of original data
                    returnCount++;
                    
                    // different approach if working on cell arrays
                    // if a={'asdf',[4,5]} then a{1,2} will return a number token [4,5] 
                    if ((operands[0] instanceof CellArrayToken) && leftCellB)
                    {
                        ErrorLogger.debugLine("SubMatrix: cell1");
                        return ((DataToken)operands[0]).getElement(y,x); 
                    }

                    // copy original values to return array
                    retToken.setElement(y_r, 
                                        x_r,
                                        ((DataToken)operands[0]).getElement(y,x));

                } // end yi
            } // end xi
            return retToken;
            
		} // end two operands
		
        
   		/***********************************************************************/
		// evaluate HORIZONTAL selection (e.g. submatrix(a,3,<...>) )          
        ErrorLogger.debugLine("SubMatrix: "+operands[2].toString());

		if(operands[2] instanceof DoubleNumberToken)
		{
            // e.g. submatrix(a,1,<some array>)
			// e.g. submatrix(a,1,2) 
		}
        else if(operands[2] instanceof LogicalToken)
        {
            // e.g. submatrix(a,b,[true,true,false,true])
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
		else if(operands[2] instanceof Expression)
		{
            // e.g.  submatrix(a,:) or submatrix(a,2:end)
            Expression    expr = (Expression)operands[2];
            OperatorToken op   = (OperatorToken)expr.getData();

            // check if expression contains colon, e.g. (:) , (3:end)
            if ((op == null)                         ||
                (!(op instanceof ColonOperatorToken))  )
                throwMathLibException("SubMatrix: colon error");

            OperandToken colonOp = null;

            if (expr.getNumberOfChildren() == 2)
            {
                // submatrix(a,3,4:end)
                OperandToken left  = expr.getChild(0);
                OperandToken right = expr.getChild(1);
                    
                if ( (!(right instanceof DelimiterToken))                   ||
                     (!((DelimiterToken)right).getWordValue().equals("end"))  )
                        throwMathLibException("SubMatrix: wrong delimiter");
                
                // if three arguments: e.g. submatrix(a,3,4:end) -> 4:dx
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
                        throwMathLibException("SubMatrix: wrong delimiter");
                
                // if three arguments: e.g. submatrix(a,3,4:2:end) -> 4:2:dx
                right = new DoubleNumberToken(dx);

                // create new ColonOperator and return new indexes
                colonOp = new Expression(new ColonOperatorToken(), left, middle, right);
            }
            else if (expr.getNumberOfChildren() == 0)
            {
                // if three arguments: e.g. submatrix(a,3,:) -> all columns
                colonOp = new Expression(new ColonOperatorToken(),
                                         new DoubleNumberToken(1),
                                         new DoubleNumberToken(dx)    );
            }
            else
                throwMathLibException("SubMatrix: colon wrong number of childs");
    
            // evaluate new colon expression
            colonOp = colonOp.evaluate(null, globals);
            
            if ( !(colonOp instanceof DoubleNumberToken))
                throwMathLibException("SubMatrix: colon error wrong type");
              
            // copy new array of indices to second operand of SubMatrix
            operands[2]= colonOp;
        }
        else
            throwMathLibException("SubMatrix: eval: unknown operand");
		
        x_dy      = ((DoubleNumberToken)operands[2]).getSizeY();
        x_dx      = ((DoubleNumberToken)operands[2]).getSizeX();
        x_indexes = ((DoubleNumberToken)operands[2]).getReValues();

        
        //*********************************************************************
        int sizeY = y_dy*y_dx;  // number of rows    of return array
        int sizeX = x_dy*x_dx;  // number of columns of return array
        int y     = 0;
        int x     = 0;
        ErrorLogger.debugLine("SubMatrix: sizeY="+sizeY+" sizeX="+sizeX);   

        // create return array with size of limits operator
        DataToken retToken = ((DataToken)operands[0]).getElementSized(sizeY,sizeX);
        
        // work through y_indixces
        for (int yxi=0; yxi<y_dx; yxi++)
        {
            for (int yyi=0; yyi<y_dy ; yyi++)
            {
                // check if row-number is valid
                int indexY = (int)y_indexes[yyi][yxi]-1;
                if ((indexY<0) || (indexY>dy-1))
                        throwMathLibException("SubMatrix: index exceeds array dimensions");

                // work through x_indixes
                x=0;
                for (int xxi=0; xxi<x_dx; xxi++)
                {
                    for (int xyi=0; xyi<x_dy; xyi++)
                    {
                        ErrorLogger.debugLine("SubMatrix: y="+y+" x="+x);
                        

                        // different approach if working on cell arrays
                        // if a={'asdf',[4,5]} then a{1,2} will return a number token [4,5] 
                        if ((operands[0] instanceof CellArrayToken) && leftCellB)
                        {
                            ErrorLogger.debugLine("SubMatrix: cell1");
                            return ((DataToken)operands[0]).getElement((int)y_indexes[yyi][yxi]-1,
                                                                       (int)x_indexes[xyi][xxi]-1); 
                        }

                        
                        // copy original values to return array
                        retToken.setElement(y, 
                                            x,
                                            ((DataToken)operands[0]).getElement((int)y_indexes[yyi][yxi]-1,
                                                                                (int)x_indexes[xyi][xxi]-1));
                     x++;   
                        
                    } // end xyi
                } // end xxi
  
                y++;
            } // end yyi
        } // end yxi
        return retToken;
        
	} // end evaluate
}

/*
@GROUP
matrix
@SYNTAX
answer = submatrix(matrix, sizey, sizex)
answer = submatrix(matrix, index)
@DOC
Returns a portion of a matrix.
@NOTES
@EXAMPLES
a=[1,2,3,4,5;6,7,8,9,10;1,2,3,4,5]                                                
submatrix(a,1,2)   = 2                                 
submatrix(a,3,:)   = [3,8,3]'	       					
submatrix(a,2:5,2) = [7,8,9,10]
submatrix(a,1:end,:)
submatrix(a,[2,6,3,2]) 						
@SEE
subassign

*/


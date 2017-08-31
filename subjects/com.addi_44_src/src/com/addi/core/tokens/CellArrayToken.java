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

/**Class used to represent cell arrays 
 * e.g. a={ 3, [1,2,3;4,5,6], "hello" ; "barfoo" , rand(5), 1+2 } */
public class CellArrayToken extends DataToken
{
    /** values of the cell array */
    OperandToken values[][];  
    
    /** Constructor creating empty cell array token
     */
    public CellArrayToken()
    {
        super(5, "cell"); 
        sizeY  = 0;
        sizeX  = 0;
        sizeA  = new int[]{0,0};
        noElem = 0;
        values = null;
    }

    /**Constructor for cell arrays
     * @param _value = the values of the elements of the cell array
     */
    public CellArrayToken(OperandToken[][] _values)
    {
        super(5, "cell"); 
		sizeY  = _values.length;
		sizeX  = _values[0].length;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = _values;
    }

    /**Constructor for cell array with 1x1 size
     * @param _value = the values of the elements of the matrix
     */
    public CellArrayToken(OperandToken _value)
    {
        super(5, "cell");
        sizeY        = 1;
        sizeX        = 1;
        sizeA        = new int[]{sizeY, sizeX};
        noElem       = sizeY * sizeX;
		values       = new OperandToken[1][1];
		values[0][0] = _value;
    }

    /** return a new CellArrayToken of size y*x
     * @param y
     * @param x
     * @return DataToken
     */
    public DataToken getElementSized(int y, int x)
    {
        return new CellArrayToken(new OperandToken[y][x]); 
    }
    
    /**
     * @param dy
     * @praram dx
     */
    public void setSize(int dy, int dx)
    {
        OperandToken[][] newValues = new OperandToken[dy][dx];        

        ErrorLogger.debugLine("cell "+dy+" "+dx);
        ErrorLogger.debugLine("cell "+sizeY+" "+sizeX);
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                ErrorLogger.debugLine("cell "+yy+" "+xx);
                newValues[yy][xx] = values[yy][xx];
            }
        }
        values = newValues;
        sizeY  = dy;
        sizeX  = dx;      
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;

    } // end setSize
    
    /**
     * @param n
     * @return
     */
    public OperandToken getElement(int n)
    {
        int x = (int) (n/sizeY); // column to start
        int y = n - x*sizeY;     // row to start

        return values[y][x];
    }

    /**
     * @param y
     * @param x
     * @return
     */
    public OperandToken getElement(int y, int x)
    {
        //return new CellArrayToken(values[y][x]);
        return values[y][x];
    }

    /**
     * @param y
     * @param x
     * @param op
     */
    public void setElement(int y, int x, OperandToken op)
    {
        values[y][x]= op; //((CellArrayToken)op).getValue()[0][0];
    }
    
    /**
     * @param n
     * @param num
     */
    public void setElement(int n, OperandToken num)
    {
        int    x    = (int)(n/sizeY); // column to start
        int    y    = n - x*sizeY;      // row to start
        
        values[y][x]= num; //((CellArrayToken)op).getValue()[0][0];
    }

 
    /**evaluate function - just returns the object itself
     * @param operands = the cell arrray operands (not used)
     * @return the cell array itself
     */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
		ErrorLogger.debugLine("CellArray evaluate size: "+sizeY+" "+sizeX);
		
        // if cell array is empty return itself
        if ((sizeX==0) && (sizeY==0))
            return this; 
        
        int tmpSizeX    = 0;    // no. or columns of new overall matrix
        
        // Evaluate every single element of cell array
        //  maybe something like a={1+2,"hallo",6}
        for (int yy=0; yy<values.length; yy++)
		{
		    for (int xx=0; xx<values[yy].length; xx++)
			{
				if (values[yy][xx] != null) // only evaluate non-null elements  
					values[yy][xx] = values[yy][xx].evaluate(null, globals);

			} // end xx
            
            // Store length of first row (as size for all rows)
			if (yy==0)
            	tmpSizeX  = values[0].length;  // size of overall array
			
            // Check if all rows have the same number of columns
            if (values[yy].length != tmpSizeX)
            	Errors.throwMathLibException("CellArray; number of columns of all rows must be equal");
            	      
		} // end yy

        
       return new CellArrayToken(values);
   } // end evaluate

    /**Convert the matrix to a string
     * @return string representation of cell array
     */
    public String toString()
    {
        
        // check if empty cell array
        if((sizeY == 0) && (sizeX == 0))
            return "{}";
        
		String s = "{";
     
		int y = values.length;
		int x = values[0].length;

		//System.out.println("matrix toString "+y+" "+x);

		for (int yy=0; yy<y; yy++)
		{
			x = values[yy].length;
            for (int xx=0; xx<x; xx++)
			{
				if (values[yy][xx] != null)	s = s + values[yy][xx].toString();
				else						s = s +" []";

                if (xx < (x-1)) { s = s + " , "; } 
			}
    		if (yy < (y-1))  { s = s + "},\n{"; }
		}
		s = s + " }";
        return s;
    }

    
    /**Return the value of the number
     * @return the value as an 2D array of double
     */
    public OperandToken[][] getValue()
    {
		ErrorLogger.debugLine("CellArray getValue");
        return values;
    }

} // end CellArrayToken

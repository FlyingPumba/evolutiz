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

import com.addi.core.interpreter.Errors;

/**Base class of all JMathLib-"Classes"*/
abstract public class ClassdefToken extends OperandToken
{
    /** The horizontal size of the number matrix */
    protected int sizeX; 

    /** The vertical size of the number matrix */
    protected int sizeY;

    // dimension of N-D-Array (e.g. a(2,3,4))
    // e.g. size={2,3} is a 2x3 array
    // e.g. size={2,3,4} is a 2x3x4 array
    protected int[] sizeA;
    
    // number of elements
    protected int   noElem;

    // name of data type
    protected String dataType = "datatoken";
    
    /**Default Constructor*/
    public ClassdefToken()
    {
        super(0); 
    }

    /**Constructor 
       @param _priority - priority of token
       @param _typeName -  the name of the type, used for casting between types*/
    public ClassdefToken(int _priority, String _dataType)
    {
        super(_priority); 
        dataType = _dataType;
    }

    /**return the number or columns in the number*/
    public int getSizeX()
    {
        return sizeX;
    }

    /**return the number of columns in the matrix*/
    public int getSizeY()
    {
        return sizeY;
    }

    /**
     * 
     * @return
     */
    public int[] getSize()
    {
        return sizeA;
    }
    
    /**
     * 
     * @return
     */
    public int getDimensions()
    {
        return sizeA.length;
    }

    /**
     * 
     * @return
     */
    public int getNumberOfElements()
    {
        return noElem;
    }

    
    public static boolean checkEqualDimensions(int[] size1, int[] size2)
    {
        
        if ((size1==null) || (size2==null))
            return false;
        
        if (size1.length != size2.length)
            return false;
        
        for (int i=0; i<size1.length; i++)
        {
            if (size1[i]!=size2[i])
                return false;
        }
        
        return true;
    }
    
    /**
     * returns the type of the token's data
     * @return
     */
    public String getDataType()
    {
        return dataType;
    }
    
    //abstract
    public OperandToken getElement(int y, int x)
    {
        Errors.throwMathLibException("DataToken getElement y x");
        return null;
    }

    //abstract
    public OperandToken getElement(int n)
    {
        Errors.throwMathLibException("DataToken getElement n");
        return null;
    }
    
    //abstract 
    public void setElement(int y, int x, OperandToken op)
    {
        Errors.throwMathLibException("DataToken setElement");
    }

    //abstract 
    public void setElement(int n, OperandToken op)
    {
        Errors.throwMathLibException("DataToken setElement n");
    }

    //abstract 
    public DataToken getElementSized(int y, int x)
    {
        Errors.throwMathLibException("DataToken getElementSized");
        return null;
    }

    //abstract
    public void setSize(int y, int x)
    {
        Errors.throwMathLibException("DataToken setSize");
    }

}

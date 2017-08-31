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

import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;



public class LogicalToken extends DataToken
{            
    /** data */
    private boolean values[]; 

    
    /**
     * empty logical token
     *
     */
    public LogicalToken()
    {
        dataType = "logical";
        sizeY    = 0;
        sizeX    = 0;
        sizeA    = new int[]{0,0};
        noElem   = 0;
        values   = null;
    }

    /**Constructor taking the numbers value as a string
     * @param _real = the numbers real value as a string
     * @param _imaginary = the numbers imaginary value as a string
     */
    public LogicalToken(String _real)
    {
        this(Boolean.getBoolean(_real));
    }

    /**Constructor taking the numbers value as a pair of double
     *  values representing real and imaginary part
     * @param _real = the numbers real value as a double
     */
    public LogicalToken(double _real)
    {
        super(5, "logical"); 
        sizeX   = 1;
        sizeY   = 1;
        sizeA   = new int[]{1, 1};
        noElem  = 1;
        values  = new boolean[1];

        if (_real==0)
            values[0]= true;
        else
            values[0]= true;
    }

    /**
     * 
     * @param _value
     */
    public LogicalToken(boolean _value)
    {
        super(5, "logical"); 
        sizeX    = 1;
        sizeY    = 1;
        sizeA    = new int[]{1, 1};
        noElem   = 1;
        values   = new boolean[1];
        
        values[0] = _value;
    }

    /**Constructor taking the numbers value as two double[][]
    @param _real = the numbers value as a 2D array of double
    @param _imaginary = the numbers value as a 2D array of double*/
    public LogicalToken(boolean[][] _values)
    {
        super(5, "logical"); 
        sizeY    = _values.length;
        sizeX    = _values[0].length;
        sizeA    = new int[]{sizeY, sizeX};
        noElem   = sizeY * sizeX;
        values   = new boolean[noElem];        

        for(int xx = 0; xx < sizeX; xx++)
        {
            for(int yy = 0; yy < sizeY; yy++)
            {
                values[xx*sizeY+yy] = _values[yy][xx];
            }
        }
    }

    /**
     * @param _dy
     * @param _dx
     * @param _reValues
     * @param _imValues
     */
    public LogicalToken(int _dy, int _dx, boolean[] _values)
    {
        super(5, "logical"); 
        sizeY  = _dy;
        sizeX  = _dx;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new boolean[noElem];        

        if ((_values != null)             &&
            (noElem    != _values.length)    )
            Errors.throwMathLibException("LogicalToken: dimension mismatch");


        for(int ni = 0; ni< noElem; ni++)
        {
            if (_values != null)
                values[ni] = _values[ni];
        }
        
    }

    /**
     * Constructor for multidimensional array
     * @param _sizeA
     * @param _reValues
     */
    public LogicalToken(int[] _sizeA, boolean[] _values)
    {
        super(5, "logical"); 
        sizeA  = _sizeA;

        if (sizeA.length<2)
            Errors.throwMathLibException("LogicalToken: dimension too low <2");
        
        sizeY  = sizeA[0];
        sizeX  = sizeA[1];

        // compute number of elements over all dimensions
        noElem = 1;
        for (int i=0; i<sizeA.length; i++)
        {
            noElem *= sizeA[i];
        }
        
        values = new boolean[noElem];    

        if ((_values != null)             &&
            (noElem    != _values.length)    )
            Errors.throwMathLibException("LogicalToken: dimension mismatch");


        for(int ni = 0; ni< noElem; ni++)
        {
            if (_values != null)
                values[ni] = _values[ni];
        }

    }

    /**
     * Convert y,x points to element number n
     * @param  y
     * @param  x
     * @return n
     */
    protected int yx2n(int y, int x)
    {
        int n = x*sizeY + y;
        return n;
    }

    /*protected int n2y(int n)
    {
        int x = (int) (n/sizeY); // column to start
        int y = n - x*sizeY;     // row to start
        return y;
    }*/

    /*protected int n2x(int n)
    {
        int x = (int) (n/sizeY); // column to start
        return x;
    }*/

    /**
     *  Convert from index to n (e.g. index={2,3,5} -> n)
     *  @param
     *  @return
     */
    public int index2n(int[] index)
    {
        String s="";
        for (int i=0; i<index.length; i++)
            s += index[i]+" ";
            
        ErrorLogger.debugLine("LogicalToken: index2n: index: "+s);
    
        int dn = noElem;
        int n  = 0;
        
        for (int i=index.length-1; i>0; i--)
        {
            dn = dn / sizeA[i];
            n += dn * index[i];
        }
        
        n+= index[0];
        
        return n;
    }
    
    /** 
     * 
     */
    public DataToken getElementSized(int y, int x)
    {
        return new LogicalToken(y, x, new boolean[y*x]); 
    }

    /** increase/decrease the size of the current DoubleNumberToken to size y*x
     *  @param dy number of rows
     *  @param dx number of columns
     */
    public void setSize(int dy, int dx)
    {
        boolean[] newValues = new boolean[dy*dx];        

        ErrorLogger.debugLine("boolean "+dy+" "+dx);
        ErrorLogger.debugLine("boolean "+sizeY+" "+sizeX);
        
        // new array must be bigger than original value, otherwise values will be
        //   lost after copying into the new array
        if ((dy<sizeY) ||  (dx<sizeX))
            Errors.throwMathLibException("LogicalToken: setSize: loosing values");
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                int n = yx2n(yy,xx);
                ErrorLogger.debugLine("boolean "+yy+" "+xx);
                newValues[xx*dy + yy] = values[n];
            }
        }
        values = newValues;
        sizeY  = dy;
        sizeX  = dx;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
    } // end setSize



    /**
     * 
     * @param y
     * @param x
     * @return
     */
    public OperandToken getElement(int y, int x)
    {   
        int n = yx2n(y,x);
        return getElement(n);
    }

    /**
     * 
     * @param n
     * @return
     */
    public OperandToken getElement(int n)
    {
        return new LogicalToken(values[n]);
    }

    /**
     * 
     * @param y
     * @param x
     * @param num
     */
    public void setElement(int y, int x, OperandToken num)
    {
        int    n    = yx2n(y,x);
        setElement(n, num);
    }

    /**
     * 
     * @param n
     * @param num
     */
    public void setElement(int n, OperandToken num)
    {
        values[n] = ((LogicalToken)num).getValue(n);
    }

    /**
     * 
     * @param n
     * @return
     */
    public boolean getValue(int n)
    {
        return values[n];
    }

    /**
     * 
     * @return
     */
    public boolean[][] getValues()
    {       
        boolean[][] temp = new boolean[sizeY][sizeX];

        if ((sizeY==0) && (sizeX==0))
            return null;
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                int n = yx2n(yy,xx);
                temp[yy][xx] = values[n];
            }
        }
        return temp;
    }

    /** Set value at position y, x
     * @param y = y position in matrix
     * @param x = x position in matrix
     * @param real = real value
     * @param imag = imaginary value
     */    
    public void setValue(int y, int x, boolean _value)
    {
        int n = yx2n(y,x);
        setValue(n, _value);
    }

    /**
     * 
     * @param n
     * @param _real
     * @param _imag
     */
    public void setValue(int n, boolean _value)
    {
        values[n] = _value;
    }

    /**
     * 
     * @param index  multidimensional index
     * @param _real
     * @param _imag
     */
    public void setValue(int[] index, boolean _value)
    {

        int n = index2n(index);
        
        setValue(n, _value);
        
    }
    
    /**
     * conversion into a number token
     * @return
     */
    public DoubleNumberToken getDoubleNumberToken()
    {
        double[] ret = new double[values.length];
        
        for (int i=0; i<values.length; i++)
        {
            if (values[i])
                ret[i]=1.0;
            else
                ret[i]=0.0;
        }
        
        return new DoubleNumberToken(sizeA, ret, null);
    }

    
    /**return the number as a string*/
    public String toString()
    {
        String result = null;
        if((sizeY == 0) && (sizeX == 0))
        {
            // e.g. a=null;
            result = "[]";
        }
        else if((sizeY == 1) && (sizeX == 1) && sizeA.length==2)
        {
            // e.g. a=555;
            result = ""+values[0];
        }
        else if (sizeA.length ==2)
        {
            result = toString2d(new int[]{sizeY,sizeX});
        }
        else
        {
            // e.g. a=[1,2,3;4,5,6] or multidimensional
            
            int[] dim = new int[sizeA.length];
            dim[0] = sizeY;
            dim[1] = sizeX;
            
            String s = toString(dim, sizeA.length-1);
            
            result = new String(s);
        }           
        return result;
    }
    
    /**
     * @param dim
     * @param i
     * @return
     */
    private String toString(int[] dim, int i)
    {
        String ret="";
        
        if (i>=2)
        {
            // e.g. at least 3rd dimension
            // e.g. a(5,3,4,x,3,1)
            for (int n=0; n<sizeA[i]; n++)
            {
                dim[i]=n;
                
                
                // e.g. a(5,3,Y,x,3,1)
                ret += toString(dim, i-1);
                
            }
            
        }
        else
        {
            // e.g. 
            ret += "(:,:";
            for (int k=2; k<dim.length; k++)
            {
                ret += "," + (dim[k]+1);   //NOTE: conversion from internal to external index
            }
            ret += ") = \n";

            ret += toString2d(dim);
            
            ret += "\n";
        }
        return ret;
    }

    /**
     * 
     * @param nn
     * @return
     */
    private String toString2d(int[] nn)
    {
        StringBuffer buffer = new StringBuffer(20);
        
        for(int yy = 0; yy < sizeA[0]; yy++)
        {
            buffer.append(" [");
            for(int xx = 0; xx < sizeA[1]; xx++)
            {
                nn[0] = yy;
                nn[1] = xx;
                int n = index2n(nn);
                ErrorLogger.debugLine(" NToken: "+index2n(nn));
                
                buffer.append(values[n]);
                
                if(xx < sizeX - 1)
                    buffer.append(" ,  ");
            }           
            buffer.append("]\n");
        }
        return buffer.toString();
    }
        

    /**Evaluate the token. This causes it to return itself*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        return this;    
    }

    /**Check if two tokens are equal
       @param arg = the object to check against*/
    public boolean equals(Object arg)
    {
        if(arg instanceof LogicalToken)
        {
            LogicalToken nArg = (LogicalToken)arg;
            if(sizeX == nArg.getSizeX() && sizeY == nArg.getSizeY())
            {
                boolean equal = true;
                for (int yy=0; yy<sizeY && equal; yy++)
                {
                        for (int xx=0; xx<sizeX && equal; xx++)
                        {
                            int n = yx2n(yy,xx);
                            if(values[n] != nArg.getValue(n))
                                equal = false;
                        }
                }                        
                return equal;
            }
            return false;
        }
        return false;
    }

    ///////////////////////standard operators///////////////////
    ////////////////////////////////////////////////////////////
    
    /**add arg to this object for a number token
    @param = the value to add to it
    @return the result as an OperandToken*/
    public OperandToken add(OperandToken arg)
    {
    	return this.getDoubleNumberToken().add(arg);
    } // end add
    
    /**subtract arg from this object for a number token
     * @param = the value to subtract
     * @return the result as an OperandToken
     */
    public OperandToken subtract(OperandToken arg)
    {
    	return this.getDoubleNumberToken().subtract(arg);    
    }

    /**Raise this object to the power of arg
     * @param = the value to raise it to the power of
     * @return the result as an OperandToken
     */
    public OperandToken power(OperandToken arg)
    {
    	return this.getDoubleNumberToken().power(arg);        
    } // end power

    /** The value to raise it to the matrix power of
     * @param arg
     * @return
     */
    public OperandToken mPower(OperandToken arg)
    {
    	return this.getDoubleNumberToken().mPower(arg);    
    } // end mPower
    
    /**multiply arg by this object for a number token
     * @param arg = the value to multiply it by
     * @return the result as an OperandToken
     */
    public OperandToken multiply(OperandToken arg) 
    {
    	return this.getDoubleNumberToken().multiply(arg);    
    } // end multiply

    /**divide this object by arg for a number token
     * @param arg = the value to divide it by
     * @return the result as an OperandToken
     */
    public OperandToken divide(OperandToken arg)
    {       
    	return this.getDoubleNumberToken().divide(arg);
    } // end divide
    
    //////////////////////////////////////////SCALAR OPERATORS//////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**scalar multiply arg by this object for a number token
    @arg = the value to multiply it by
    @return the result as an OperandToken*/
    public OperandToken scalarMultiply(OperandToken arg)
    {
    	return this.getDoubleNumberToken().scalarMultiply(arg);
    } // end scalarMultiply

    /**scalar divide arg by this object for a number token
    @arg = the value to divide it by
    @return the result as an OperandToken*/
    public OperandToken scalarDivide(OperandToken arg)
    {
    	return this.getDoubleNumberToken().scalarDivide(arg);    
    } // end scalarDivide

    /**left divide 
    @arg = 
    @return the result as an OperandToken*/
    public OperandToken leftDivide(OperandToken arg)
    {
    	return this.getDoubleNumberToken().leftDivide(arg);    
    } // end leftDivide

    /**scalar left divide 
    @arg = 
    @return the result as an OperandToken*/
    public OperandToken scalarLeftDivide(OperandToken arg)
    {
    	return this.getDoubleNumberToken().scalarLeftDivide(arg);    
    } // end scalarLeftDivide
           
    /**calculate the transpose of an array
    @return the result as an OperandToken*/
    public OperandToken transpose()
    {
        // transposed array
        boolean[][] b = new boolean[sizeX][sizeY];
    
        // swap rows and columns
        for (int y=0; y<sizeY; y++)
        {
            for (int x=0; x<sizeX; x++)
            {
                int n = yx2n(y,x);
                // copy (y,x) -> (x,y)
                b[x][y] = values[n];
            }
        }
        return new LogicalToken(b);     
    }

    
    /**
     * @return true if this number token is a scalar (1*1 matrix)
     */
    public boolean isScalar()
    {

        for (int i=0; i<sizeA.length; i++)
        {
            // in case one entry in the size-array is unequal 1 it 
            //  is not a scalar any more
            if (sizeA[i]!=1)
                return false;
        }

        return true;
    }

} // end LogicalToken

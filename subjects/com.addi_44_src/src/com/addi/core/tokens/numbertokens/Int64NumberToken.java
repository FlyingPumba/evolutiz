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

package com.addi.core.tokens.numbertokens;

import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.Errors;
import com.addi.core.tokens.DataToken;
import com.addi.core.tokens.NumberToken;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;



public class Int64NumberToken extends NumberToken
{            
    /**Complex values of the token
     * the data is organizes as on single vector. 
     * e.g. a=[1,2;3,4] will be stored like below
     * values = 1
     *          3
     *          2
     *          4         */
    private long values[][]; 


    /** Constructor creating empty number token 
     */
    public Int64NumberToken()
    {
        // empty number token
        super(5, "int64");
        sizeY  = 0;
        sizeX  = 0;
        sizeA  = new int[]{0,0};
        noElem = 0;
        values = null;
    }

    /** Constructor creating a scalar taking the numbers value as a double
     * @param _value = the numbers value as a double
     */
    public Int64NumberToken(long _value)
    {
        this(_value, (long)0);
    }

    /** Constructor taking the numbers value as a double[][]
     * @param _value = the numbers value as a 2D array of double
     */
    public Int64NumberToken(long[][] _values)
    {
        this(_values, null);
    }

    /**Constructor taking the numbers value as a string
     * @param _real = the numbers real value as a string
     * @param _imaginary = the numbers imaginary value as a string
     */
    public Int64NumberToken(String _real, String _imaginary)
    {
        super(5, "int64"); 
        sizeX  = 1;
        sizeY  = 1;
        sizeA  = new int[]{1, 1};
        noElem = 1;
        values = new long[1][2];

        // create real part
        if (_real!=null)
            values[0][REAL] = new Long(_real).longValue();
        else
            values[0][REAL] = 0;
        
        // create imaginary part
        if (_imaginary!=null)
            values[0][IMAG] = new Long(_imaginary).longValue();
        else
            values[0][IMAG] = 0;
    }

    /**Constructor taking the numbers value as a pair of double
     *  values representing real and imaginary part
     * @param _real = the numbers real value as a double
     * @param _imaginary = the numbers imaginary value as a double
     */
    public Int64NumberToken(long _real, long _imaginary)
    {
        super(5, "int64"); 
        sizeX  = 1;
        sizeY  = 1;
        sizeA  = new int[]{1, 1};
        noElem = 1;
        values = new long[1][2];

        values[0][REAL] = _real;
        values[0][IMAG] = _imaginary;
    }

    /**Constructor taking the numbers value as two double[][]
    @param _real = the numbers value as a 2D array of double
    @param _imaginary = the numbers value as a 2D array of double*/
    public Int64NumberToken(long[][] _real, long[][] _imaginary)
    {
        super(5, "int64"); 

        if (_real!=null)
        {
            sizeY  = _real.length;
            sizeX  = _real[0].length;
        }
        else if(_imaginary!=null)
        {
            sizeY  = _imaginary.length;
            sizeX  = _imaginary[0].length;
        }   
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new long[noElem][2];        

        for(int xx = 0; xx < sizeX; xx++)
        {
            for(int yy = 0; yy < sizeY; yy++)
            {
                if (_real != null)
                    values[xx*sizeY+yy][REAL] = _real[yy][xx];
                else
                    values[xx*sizeY+yy][REAL] = 0;
                
                if (_imaginary != null)
                    values[xx*sizeY+yy][IMAG] = _imaginary[yy][xx];
                else
                    values[xx*sizeY+yy][IMAG] = 0;
            }
        }
    }

    /**Constructor taking the numbers value as a double[][][]
       @param _values = the numbers value as a 3D array of double*/
    public Int64NumberToken(long[][][] _values)
    {
        super(5, "int64"); 
        sizeY  = _values.length;
        sizeX  = _values[0].length;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new long[noElem][2];        

        for(int xx = 0; xx < sizeX; xx++)
        {
            for(int yy = 0; yy < sizeY; yy++)
            {
                values[xx*sizeY+yy][REAL] = _values[yy][xx][REAL];
                values[xx*sizeY+yy][IMAG] = _values[yy][xx][IMAG];
            }
        }
    }

    /**
     * 
     * @param _dy
     * @param _dx
     * @param _reValues
     * @param _imValues
     */
    public Int64NumberToken(int _dy, int _dx, long[] _reValues, long[] _imValues)
    {
        super(5, "int64"); 
        sizeY  = _dy;
        sizeX  = _dx;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new long[noElem][2];        

        if ((_reValues != null)             &&
            (noElem    != _reValues.length)    )
            Errors.throwMathLibException("Int64NumberToken: real dimension mismatch");

        if ((_imValues != null)             &&
            (noElem    != _imValues.length)    )
            Errors.throwMathLibException("Int64NumberToken: imag dimension mismatch");

        for(int ni = 0; ni< noElem; ni++)
        {
            if (_reValues != null)
                values[ni][REAL] = _reValues[ni];
            else
                values[ni][REAL] = 0;
                
            if (_imValues != null)
                values[ni][IMAG] = _imValues[ni];
            else
                values[ni][IMAG] = 0;
        }
        
        
    }

    /**
     * Constructor for multidimensional array
     * @param _sizeA
     * @param _reValues
     * @param _imValues
     */
    public Int64NumberToken(int[] _sizeA, long[] _reValues, long[] _imValues)
    {
        super(5, "int64"); 
        sizeA  = _sizeA;

        if (sizeA.length<2)
            Errors.throwMathLibException("Int64NumberToken: dimension too low <2");
        
        sizeY  = sizeA[0];
        sizeX  = sizeA[1];

        // compute number of elements over all dimensions
        noElem = 1;
        for (int i=0; i<sizeA.length; i++)
        {
            noElem *= sizeA[i];
        }
        
        values = new long[noElem][2];    

        if ((_reValues != null)             &&
            (noElem    != _reValues.length)    )
            Errors.throwMathLibException("Int64NumberToken: real dimension mismatch");

        if ((_imValues != null)             &&
            (noElem    != _imValues.length)    )
            Errors.throwMathLibException("Int64NumberToken: imag dimension mismatch");

        for(int ni = 0; ni< noElem; ni++)
        {
            if (_reValues != null)
                values[ni][REAL] = _reValues[ni];
            else
                values[ni][REAL] = 0;
                
            if (_imValues != null)
                values[ni][IMAG] = _imValues[ni];
            else
                values[ni][IMAG] = 0;
        }

    }

    /** return a new Number Token of size y*x
     * 
     */
    public DataToken getElementSized(int y, int x)
    {
        return new Int64NumberToken(y, x, new long[y*x],null); 
    }

    /** increase/decrease the size of the current DoubleNumberToken to size y*x
     *  @param dy number of rows
     *  @param dx number of columns
     */
    public void setSize(int dy, int dx)
    {
        long[][] newValues = new long[dy*dx][2];        

        ErrorLogger.debugLine("number "+dy+" "+dx);
        ErrorLogger.debugLine("number "+sizeY+" "+sizeX);
        
        // new array must be bigger than original value, otherwise values will be
        //   lost after copying into the new array
        if ((dy<sizeY) ||  (dx<sizeX))
            Errors.throwMathLibException("Int64NumberToken: setSize: loosing values");
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                int n = yx2n(yy,xx);
                //ErrorLogger.debugLine("int8number "+yy+" "+xx);
                newValues[xx*dy + yy][REAL] = values[n][REAL];
                newValues[xx*dy + yy][IMAG] = values[n][IMAG];
            }
        }
        values = newValues;
        sizeY  = dy;
        sizeX  = dx;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
    } // end setSize

    
    /**@return the real value of the first number*/
    public long getValueRe()
    {
        return getValueRe(0);
    }

    /**
     * 
     * @param y
     * @param x
     * @return the real value of the number at position y, x
     */
    public long getValueRe(int y, int x)
    {
        return getValueRe( yx2n(y,x) );
    }

    /**
     * 
     * @param index
     * @return
     */
    public long getValueRe(int[] index)
    {
        return getValueRe( index2n(index) );
    }

    /**
     * 
     * @param n
     * @return
     */
    public long getValueRe(int n)
    {
        return values[n][REAL];
    }


   
    /**@return the imaginary value of the first number*/
    public long getValueIm()
    {
        return getValueIm(0);
    }

    /**@return the imaginary value of the number at position y, x*/
    public long getValueIm(int y, int x)
    {
        return getValueIm( yx2n(y,x) );
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public long getValueIm(int[] index)
    {
        return getValueIm( index2n(index) );
    }

    /**
     * 
     * @param n
     * @return
     */
    public long getValueIm(int n)
    {
        return values[n][IMAG];
    }

    
    /**@return the real values of the number*/
    public long[][] getValuesRe()
    {       
        long[][] temp = new long[sizeY][sizeX];

        if ((sizeY==0) && (sizeX==0))
            return null;
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                int n = yx2n(yy,xx);
                temp[yy][xx] = values[n][REAL];
            }
        }
        return temp;
    }


    /**@return the imaginary values of the number*/
    public long[][] getValuesIm()
    {
        long[][] temp = new long[sizeY][sizeX];

        if ((sizeY==0) && (sizeX==0))
            return null;

        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                int n = yx2n(yy,xx);
                temp[yy][xx] = values[n][IMAG];
            }
        }
        return temp;
    }
    

    /**
     * 
     * @param n
     * @return
     */
    public OperandToken getElement(int n)
    {
        return new Int64NumberToken(values[n][REAL], values[n][IMAG]);
    }


    /**
     * 
     * @param n
     * @param num
     */
    public void setElement(int n, OperandToken num)
    {
        values[n][REAL] = ((Int64NumberToken)num).getValueRe();
        values[n][IMAG] = ((Int64NumberToken)num).getValueIm();
    }

    
    /** Set value at position y, x
     * @param y = y position in matrix
     * @param x = x position in matrix
     * @param real = real value
     * @param imag = imaginary value
     */    
    public void setValue(int y, int x, long _real, long _imag)
    {
        int n = yx2n(y,x);
        setValue(n, _real, _imag);
    }

    /**
     * 
     * @param n
     * @param _real
     * @param _imag
     */
    public void setValue(int n, long _real, long _imag)
    {
        values[n][REAL] = _real;
        values[n][IMAG] = _imag;
    }

    /**
     * 
     * @param index  multidimensional index
     * @param _real
     * @param _imag
     */
    public void setValue(int[] index, long _real, long _imag)
    {

        int n = index2n(index);
        
        setValue(n, _real, _imag);
        
    }

    ///////////////////////standard operators///////////////////
    ////////////////////////////////////////////////////////////
    
    
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
            result = toString(values[0]);
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
                
                buffer.append(toString(values[n]));
                
                if(xx < sizeX - 1)
                    buffer.append(" ,  ");
            }           
            buffer.append("]\n");
        }
        return buffer.toString();
    }

    /** create string representation of (complex) double values 
    @param _values[]={REAL,IMAG} real and imaginary part of number*/
    public String toString(long _values[])
    {
        if (_values==null)
            return "XXXXXX";
        
        long re = _values[REAL];
        long im = _values[IMAG];
        
        StringBuffer result =  new StringBuffer();
        
        if(im != 0)
            result.append("(");

        result.append(re);

        // imaginary part of number
        if(im != 0.0)
        {
            if ((re!=0) && !(im<0))
                result.append("+");
                
            result.append(im);
        
            result.append("i)");
        }
        return  result.toString();
    }

    /**Evaluate the token. This causes it to return itself*/
    public OperandToken evaluate(Token[] operands)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        return this;    
    }

} // end Int64NumberToken

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



public class UInt8NumberToken extends NumberToken
{            
    /**Complex values of the token
     * the data is organizes as on single vector. 
     * e.g. a=[1,2;3,4] will be stored like below
     * values = 1
     *          3
     *          2
     *          4         */
    private short values[][]; 


    /** Constructor creating empty number token 
     */
    public UInt8NumberToken()
    {
        // empty number token
        super(5, "uint8");
        sizeY  = 0;
        sizeX  = 0;
        sizeA  = new int[]{0,0};
        noElem = 0;
        values = null;
    }

    /** Constructor creating a scalar taking the numbers value as a double
     * @param _value = the numbers value as a double
     */
    public UInt8NumberToken(short _value)
    {
        this(_value, (short)0);
    }

    /** Constructor taking the numbers value as a double[][]
     * @param _value = the numbers value as a 2D array of double
     */
    public UInt8NumberToken(short[][] _values)
    {
        this(_values, null);
    }

    /**Constructor taking the numbers value as a string
     * @param _real = the numbers real value as a string
     * @param _imaginary = the numbers imaginary value as a string
     */
    public UInt8NumberToken(String _real, String _imaginary)
    {
        super(5, "uint8"); 
        sizeX  = 1;
        sizeY  = 1;
        sizeA  = new int[]{1, 1};
        noElem = 1;
        values = new short[1][2];

        // create real part
        if (_real!=null)
            values[0][REAL] = new Short(_real).shortValue();
        else
            values[0][REAL] = 0;
        
        // create imaginary part
        if (_imaginary!=null)
            values[0][IMAG] = new Short(_imaginary).shortValue();
        else
            values[0][IMAG] = 0;
    }

    /**Constructor taking the numbers value as a pair of double
     *  values representing real and imaginary part
     * @param _real = the numbers real value as a double
     * @param _imaginary = the numbers imaginary value as a double
     */
    public UInt8NumberToken(short _real, short _imaginary)
    {
        super(5, "uint8"); 
        sizeX  = 1;
        sizeY  = 1;
        sizeA  = new int[]{1, 1};
        noElem = 1;
        values = new short[1][2];

        values[0][REAL] = _real;
        values[0][IMAG] = _imaginary;
    }

    /**Constructor taking the numbers value as two double[][]
    @param _real = the numbers value as a 2D array of double
    @param _imaginary = the numbers value as a 2D array of double*/
    public UInt8NumberToken(short[][] _real, short[][] _imaginary)
    {
        super(5, "uint8"); 

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
        values = new short[noElem][2];        

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
    public UInt8NumberToken(short[][][] _values)
    {
        super(5, "uint8"); 
        sizeY  = _values.length;
        sizeX  = _values[0].length;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new short[noElem][2];        

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
    public UInt8NumberToken(int _dy, int _dx, short[] _reValues, short[] _imValues)
    {
        super(5, "uint8"); 
        sizeY  = _dy;
        sizeX  = _dx;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new short[noElem][2];        

        if ((_reValues != null)             &&
            (noElem    != _reValues.length)    )
            Errors.throwMathLibException("UInt8NumberToken: real dimension mismatch");

        if ((_imValues != null)             &&
            (noElem    != _imValues.length)    )
            Errors.throwMathLibException("UInt8NumberToken: imag dimension mismatch");

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
    public UInt8NumberToken(int[] _sizeA, short[] _reValues, short[] _imValues)
    {
        super(5, "uint8"); 
        sizeA  = _sizeA;

        if (sizeA.length<2)
            Errors.throwMathLibException("UInt8NumberToken: dimension too low <2");
        
        sizeY  = sizeA[0];
        sizeX  = sizeA[1];

        // compute number of elements over all dimensions
        noElem = 1;
        for (int i=0; i<sizeA.length; i++)
        {
            noElem *= sizeA[i];
        }
        
        values = new short[noElem][2];    

        if ((_reValues != null)             &&
            (noElem    != _reValues.length)    )
            Errors.throwMathLibException("UInt8NumberToken: real dimension mismatch");

        if ((_imValues != null)             &&
            (noElem    != _imValues.length)    )
            Errors.throwMathLibException("UInt8NumberToken: imag dimension mismatch");

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
        return new UInt8NumberToken(y, x, new short[y*x],null); 
    }

    /** increase/decrease the size of the current DoubleNumberToken to size y*x
     *  @param dy number of rows
     *  @param dx number of columns
     */
    public void setSize(int dy, int dx)
    {
        short[][] newValues = new short[dy*dx][2];        

        ErrorLogger.debugLine("number "+dy+" "+dx);
        ErrorLogger.debugLine("number "+sizeY+" "+sizeX);
        
        // new array must be bigger than original value, otherwise values will be
        //   lost after copying into the new array
        if ((dy<sizeY) ||  (dx<sizeX))
            Errors.throwMathLibException("UInt8NumberToken: setSize: loosing values");
        
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
    public short getValueRe()
    {
        return getValueRe(0);
    }

    /**
     * 
     * @param y
     * @param x
     * @return the real value of the number at position y, x
     */
    public short getValueRe(int y, int x)
    {
        return getValueRe( yx2n(y,x) );
    }

    /**
     * 
     * @param index
     * @return
     */
    public short getValueRe(int[] index)
    {
        return getValueRe( index2n(index) );
    }

    /**
     * 
     * @param n
     * @return
     */
    public short getValueRe(int n)
    {
        return values[n][REAL];
    }


   
    /**@return the imaginary value of the first number*/
    public short getValueIm()
    {
        return getValueIm(0);
    }

    /**@return the imaginary value of the number at position y, x*/
    public short getValueIm(int y, int x)
    {
        return getValueIm( yx2n(y,x) );
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public short getValueIm(int[] index)
    {
        return getValueIm( index2n(index) );
    }

    /**
     * 
     * @param n
     * @return
     */
    public short getValueIm(int n)
    {
        return values[n][IMAG];
    }

    
    /**@return the real values of the number*/
    public short[][] getValuesRe()
    {       
        short[][] temp = new short[sizeY][sizeX];

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
    public short[][] getValuesIm()
    {
        short[][] temp = new short[sizeY][sizeX];

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
        return new UInt8NumberToken(values[n][REAL], values[n][IMAG]);
    }


    /**
     * 
     * @param n
     * @param num
     */
    public void setElement(int n, OperandToken num)
    {
        values[n][REAL] = ((UInt8NumberToken)num).getValueRe();
        values[n][IMAG] = ((UInt8NumberToken)num).getValueIm();
    }

    
    /** Set value at position y, x
     * @param y = y position in matrix
     * @param x = x position in matrix
     * @param real = real value
     * @param imag = imaginary value
     */    
    public void setValue(int y, int x, short _real, short _imag)
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
    public void setValue(int n, short _real, short _imag)
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
    public void setValue(int[] index, short _real, short _imag)
    {

        int n = index2n(index);
        
        setValue(n, _real, _imag);
        
    }

    ///////////////////////standard operators///////////////////
    ////////////////////////////////////////////////////////////
    
    /**add arg to this object for a number token
    @param = the value to add to it
    @return the result as an OperandToken*/
    public OperandToken add(OperandToken arg)
    {

        if (arg instanceof UInt8NumberToken)
        {    
            UInt8NumberToken nArg = ((UInt8NumberToken)arg);
    
            // Check dimensions of matrices 
            if(checkEqualDimensions(sizeA, nArg.getSize()))
            {
                // Add (n*m) + (n*m) or
                //  same dimensions (n,m,r)==(n,m,r)
                ErrorLogger.debugLine("UInt8NumberToken: add (n*m) + (n*m)");
                UInt8NumberToken result = new UInt8NumberToken(sizeA, null, null);
    
                for(int n = 0; n < noElem; n++)
                {
                    short real = add(getValueRe(n), nArg.getValueRe(n));
                    short imag = add(getValueIm(n), nArg.getValueIm(n));
                    result.setValue(n, real, imag);
                }
                
                //ErrorLogger.debugLine("end UInt8NumberToken: add (n*m) + (n*m)");
                return result;      
            } 
            else if(isScalar())
            {
                // 1 + [3,4,5]       
                ErrorLogger.debugLine("UInt8NumberToken: add (1*1) + (n*m)");
                UInt8NumberToken result = new UInt8NumberToken(nArg.getSize(), null, null);
                
                for(int n = 0; n < nArg.getNumberOfElements(); n++)
                {
                    short realval      = add(getValueRe(), nArg.getValueRe(n));
                    short imaginaryval = add(getValueIm(), nArg.getValueIm(n));
                    result.setValue(n, realval, imaginaryval);
                }
                
                //ErrorLogger.debugLine("end UInt8NumberToken: add (n*m) + (n*m)");
                return result;      
            } 
            else if(nArg.isScalar())
            {
                // [3,4,5] +1
                ErrorLogger.debugLine("UInt8NumberToken: add (n,m) + (1,1)");
                UInt8NumberToken result = new UInt8NumberToken(sizeA, null, null);
                
                for(int n = 0; n < noElem; n++)
                {
                    short realval      = add(getValueRe(n), nArg.getValueRe());
                    short imaginaryval = add(getValueIm(n), nArg.getValueIm());
                    result.setValue(n, realval, imaginaryval);
                }
                
                //ErrorLogger.debugLine("end UInt8NumberToken: add (n*m) + (n*m)");
                return result;      
            } 
            else
            {
                /* Matrices have unequal size: (n*m) != (o*p) */       
                Errors.throwMathLibException("UInt8NumberToken: add matrices of unequal size");
                return null;
            }
        }
        else if (arg instanceof DoubleNumberToken)
        {
            ErrorLogger.debugLine("UInt8NumberToken: add (n,m) + (1,1)double");
            
            DoubleNumberToken nArg = ((DoubleNumberToken)arg);

            if ((nArg.getSize().length!=2) || 
                (nArg.getSizeX()      !=1) || 
                (nArg.getSizeY()      !=1)    )
                Errors.throwMathLibException("UInt8NumberToken: uint8+double only works if double is scalar");
                
            UInt8NumberToken result = new UInt8NumberToken(sizeA, null, null);
            
            for(int n = 0; n < noElem; n++)
            {
                short realval      = add(getValueRe(n), nArg.getValueRe());
                short imaginaryval = add(getValueIm(n), nArg.getValueIm());
                result.setValue(n, realval, imaginaryval);
            }
            return result;
        }


        
        Errors.throwMathLibException("UInt8NumberToken: add: wrong type");
        return null;
        
    } // and add

    private short add(double a, double b)
    {
        double c= a + b;
        
        if (c>255)
            return 255;
        else if (c<0)
            return 0;
        else
            return (short)c;
    }

    /**subtract arg from this object for a number token
    @param = the value to subtract from
    @return the result as an OperandToken*/
    public OperandToken subtract(OperandToken arg)
    {

        if (arg instanceof UInt8NumberToken)
        {    
            UInt8NumberToken nArg = ((UInt8NumberToken)arg);
    
            // Check dimensions of matrices 
            if(checkEqualDimensions(sizeA, nArg.getSize()))
            {
                // Add (n*m) - (n*m) or
                //  same dimensions (n,m,r)==(n,m,r)
                ErrorLogger.debugLine("UInt8NumberToken: subtract (n*m) - (n*m)");
                UInt8NumberToken result = new UInt8NumberToken(sizeA, null, null);
    
                for(int n = 0; n < noElem; n++)
                {
                    short real = subtract(getValueRe(n), nArg.getValueRe(n));
                    short imag = subtract(getValueIm(n), nArg.getValueIm(n));
                    result.setValue(n, real, imag);
                }
                
                //ErrorLogger.debugLine("end UInt8NumberToken: add (n*m) - (n*m)");
                return result;      
            } 
            else if(isScalar())
            {
                // 1 + [3,4,5]       
                ErrorLogger.debugLine("UInt8NumberToken: subtract (1*1) - (n*m)");
                UInt8NumberToken result = new UInt8NumberToken(nArg.getSize(), null, null);
                
                for(int n = 0; n < nArg.getNumberOfElements(); n++)
                {
                    short realval      = subtract(getValueRe(), nArg.getValueRe(n));
                    short imaginaryval = subtract(getValueIm(), nArg.getValueIm(n));
                    result.setValue(n, realval, imaginaryval);
                }
                
                //ErrorLogger.debugLine("end UInt8NumberToken: add (n*m) + (n*m)");
                return result;      
            } 
            else if(nArg.isScalar())
            {
                // [3,4,5] +1
                ErrorLogger.debugLine("UInt8NumberToken: subtract (n,m) - (1,1)");
                UInt8NumberToken result = new UInt8NumberToken(sizeA, null, null);
                
                for(int n = 0; n < noElem; n++)
                {
                    short realval      = subtract(getValueRe(n), nArg.getValueRe());
                    short imaginaryval = subtract(getValueIm(n), nArg.getValueIm());
                    result.setValue(n, realval, imaginaryval);
                }
                
                //ErrorLogger.debugLine("end UInt8NumberToken: add (n*m) - (n*m)");
                return result;      
            } 
            else
            {
                /* Matrices have unequal size: (n*m) != (o*p) */       
                Errors.throwMathLibException("UInt8NumberToken: subtract matrices of unequal size");
                return null;
            }
        }
        else if (arg instanceof DoubleNumberToken)
        {
            ErrorLogger.debugLine("UInt8NumberToken: subtract (n,m) - (1,1)double");
            
            DoubleNumberToken nArg = ((DoubleNumberToken)arg);

            if ((nArg.getSize().length!=2) || 
                (nArg.getSizeX()      !=1) || 
                (nArg.getSizeY()      !=1)    )
                Errors.throwMathLibException("UInt8NumberToken: uint8-double only works if double is scalar");
                
            UInt8NumberToken result = new UInt8NumberToken(sizeA, null, null);
            
            for(int n = 0; n < noElem; n++)
            {
                short realval      = subtract(getValueRe(n), nArg.getValueRe());
                short imaginaryval = subtract(getValueIm(n), nArg.getValueIm());
                result.setValue(n, realval, imaginaryval);
            }
            return result;
        }


        
        Errors.throwMathLibException("UInt8NumberToken: subtract: wrong type");
        return null;
        
    } // and add

    private short subtract(double a, double b)
    {
        double c= a - b;
        
        if (c>255)
            return 255;
        else if (c<0)
            return 0;
        else
            return (short)c;
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
    public String toString(short _values[])
    {
        if (_values==null)
            return "XXXXXX";
        
        short re = _values[REAL];
        short im = _values[IMAG];
        
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

} // end Int8NumberToken

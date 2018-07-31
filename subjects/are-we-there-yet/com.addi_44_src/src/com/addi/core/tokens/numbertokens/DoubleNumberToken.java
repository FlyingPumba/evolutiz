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
import com.addi.core.tokens.CharToken;
import com.addi.core.tokens.DataToken;
import com.addi.core.tokens.NumberToken;
import com.addi.core.tokens.LogicalToken;
import com.addi.core.tokens.OperandToken;


public class DoubleNumberToken extends NumberToken
{            
    /**Complex values of the token
     * the data is organizes as on single vector. 
     * e.g. a=[1,2;3,4] will be stored like below
     * values = 1
     *          3
     *          2
     *          4         */
    private double values[][]; 

    /**Constant value set to 1*/
    public static final DoubleNumberToken one = new DoubleNumberToken(1);

    /**Constant value set to 0*/
    public static final DoubleNumberToken zero = new DoubleNumberToken(0);

    /**Constant value set to j*/
    public static final DoubleNumberToken j    = new DoubleNumberToken(0,1);


    /** Constructor creating empty number token 
     */
    public DoubleNumberToken()
    {
        // empty number token
        super(5, "double");
        sizeY  = 0;
        sizeX  = 0;
        sizeA  = new int[]{0,0};
        noElem = 0;
        values = null;
    }

    /** Constructor creating a scalar taking the numbers value as a double
     * @param _value = the numbers value as a double
     */
    public DoubleNumberToken(double _value)
    {
        this(_value, 0);
    }

    /** Constructor taking the numbers value as a double[][]
     * @param _value = the numbers value as a 2D array of double
     */
    public DoubleNumberToken(double[][] _values)
    {
        this(_values, null);
    }

    /**Constructor taking the numbers value as a string
     * @param _real = the numbers real value as a string
     * @param _imaginary = the numbers imaginary value as a string
     */
    public DoubleNumberToken(String _real, String _imaginary)
    {
        super(5, "double");
        sizeX  = 1;
        sizeY  = 1;
        sizeA  = new int[]{1, 1};
        noElem = 1;
        values = new double[1][2];

        // create real part
        if (_real!=null)
            values[0][REAL] = new Double(_real).doubleValue();
        else
            values[0][REAL] = 0.0;
        
        // create imaginary part
        if (_imaginary!=null)
            values[0][IMAG] = new Double(_imaginary).doubleValue();
        else
            values[0][IMAG] = 0.0;
    }

    /**Constructor taking the numbers value as a pair of double
     *  values representing real and imaginary part
     * @param _real = the numbers real value as a double
     * @param _imaginary = the numbers imaginary value as a double
     */
    public DoubleNumberToken(double _real, double _imaginary)
    {
        super(5, "double");
        sizeX  = 1;
        sizeY  = 1;
        sizeA  = new int[]{1, 1};
        noElem = 1;
        values = new double[1][2];

        values[0][REAL] = _real;
        values[0][IMAG] = _imaginary;
    }

    /**Constructor taking the numbers value as two double[][]
     * @param _real = the numbers value as a 2D array of double
     * @param _imaginary = the numbers value as a 2D array of double
     */
    public DoubleNumberToken(double[][] _real, double[][] _imaginary)
    {
        super(5, "double");
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
        values = new double[noElem][2];        

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
     * @param _values = the numbers value as a 3D array of double
     */
    public DoubleNumberToken(double[][][] _values)
    {
        super(5, "double");
        sizeY  = _values.length;
        sizeX  = _values[0].length;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new double[noElem][2];        

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
    public DoubleNumberToken(int _dy, int _dx, double[] _reValues, double[] _imValues)
    {
        super(5, "double"); 
        sizeY  = _dy;
        sizeX  = _dx;
        sizeA  = new int[]{sizeY, sizeX};
        noElem = sizeY * sizeX;
        values = new double[noElem][2];        

        if ((_reValues != null)             &&
            (noElem    != _reValues.length)    )
            Errors.throwMathLibException("DoubleNumberToken: real dimension mismatch");

        if ((_imValues != null)             &&
            (noElem    != _imValues.length)    )
            Errors.throwMathLibException("DoubleNumberToken: imag dimension mismatch");

        for(int ni = 0; ni< noElem; ni++)
        {
            if (_reValues != null)
                values[ni][REAL] = _reValues[ni];
            else
                values[ni][REAL] = 0.0;
                
            if (_imValues != null)
                values[ni][IMAG] = _imValues[ni];
            else
                values[ni][IMAG] = 0.0;
        }
        
        
    }

    /**
     * Constructor for multidimensional array
     * @param _sizeA
     * @param _reValues
     * @param _imValues
     */
    public DoubleNumberToken(int[] _sizeA, double[] _reValues, double[] _imValues)
    {
        super(5, "double"); 
        sizeA  = _sizeA;

        if (sizeA.length<2)
            Errors.throwMathLibException("DoubleNumberToken: dimension too low <2");
        
        sizeY  = sizeA[0];
        sizeX  = sizeA[1];

        // compute number of elements over all dimensions
        noElem = 1;
        for (int i=0; i<sizeA.length; i++)
        {
            noElem *= sizeA[i];
        }
        
        values = new double[noElem][2];    

        if ((_reValues != null)             &&
            (noElem    != _reValues.length)    )
            Errors.throwMathLibException("DoubleNumberToken: real dimension mismatch");

        if ((_imValues != null)             &&
            (noElem    != _imValues.length)    )
            Errors.throwMathLibException("DoubleNumberToken: imag dimension mismatch");

        for(int ni = 0; ni< noElem; ni++)
        {
            if (_reValues != null)
                values[ni][REAL] = _reValues[ni];
            else
                values[ni][REAL] = 0.0;
                
            if (_imValues != null)
                values[ni][IMAG] = _imValues[ni];
            else
                values[ni][IMAG] = 0.0;
        }

    }


    /** return a new Number Token of size y*x
     * @param x
     * @param y
     * @return
     */
    public DataToken getElementSized(int y, int x)
    {
        return new DoubleNumberToken(y, x, new double[y*x],null); 
    }

    /** increase/decrease the size of the current DoubleNumberToken to size y*x
     *  @param dy number of rows
     *  @param dx number of columns
     */
    public void setSize(int dy, int dx)
    {
        double[][] newValues = new double[dy*dx][2];        

        ErrorLogger.debugLine("number "+dy+" "+dx);
        ErrorLogger.debugLine("number "+sizeY+" "+sizeX);
        
        // new array must be bigger than original value, otherwise values will be
        //   lost after copying into the new array
        if ((dy<sizeY) ||  (dx<sizeX))
            Errors.throwMathLibException("DoubleNumberToken: setSize: loosing values");
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                int n = yx2n(yy,xx);
                ErrorLogger.debugLine("number "+yy+" "+xx);
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

    /**
     * @return the real value of the first number
     */
    public double getValueRe()
    {
        return getValueRe(0);
    }

    /**
     * @param y
     * @param x
     * @return the real value of the number at position y, x
     */
    public double getValueRe(int y, int x)
    {
        return getValueRe( yx2n(y,x) );
    }

    /**
     * @param index
     * @return
     */
    public double getValueRe(int[] index)
    {
        return getValueRe( index2n(index) );
    }

    /**
     * @param n
     * @return
     */
    public double getValueRe(int n)
    {
        return values[n][REAL];
    }

    /**
     * @param y
     * @param x
     * @return the real value of the number at position y, x as an integer
     */
    public int getIntValue(int y, int x)
    {
        double temp = getValueRe( yx2n(y,x) );
        return (new Double(temp)).intValue();
    }
    
    /**
     * @return the imaginary value of the first number
     */
    public double getValueIm()
    {
        return getValueIm(0);
    }

    /**
     * @param y
     * @param x
     * @return the imaginary value of the number at position y, x
     */
    public double getValueIm(int y, int x)
    {
        return getValueIm( yx2n(y,x) );
    }
    
    /**
     * @param index
     * @return
     */
    public double getValueIm(int[] index)
    {
        return getValueIm( index2n(index) );
    }

    /**
     * @param n
     * @return
     */
    public double getValueIm(int n)
    {
        return values[n][IMAG];
    }

    
    /**
     * @param y
     * @param x
     * @return the absolute value of the number at position y, x
     * */
    public double getValueAbs(int y, int x)
    {
        int n = yx2n(y,x);
        double temp = Math.pow(values[n][REAL], 2) + Math.pow(values[n][IMAG], 2);
        return Math.sqrt(temp);
    }

    /**
     * @param y
     * @param x
     * @return the angle of the number at position y, x in radians
     */
    public double getValueArg(int y, int x)
    {
        int n = yx2n(y,x);
        return Math.atan2(values[n][IMAG], values[n][REAL]);
    }

    /**
     * @return the real values of the number
     */
    public double[][] getValuesRe()
    {       
        double[][] temp = new double[sizeY][sizeX];

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

    /**
     * @return the real values of the number
     */
    public double[][] getReValues()
    {
        return getValuesRe();
    }

    /**
     * @return the imaginary values of the number
     */
    public double[][] getValuesIm()
    {
        double[][] temp = new double[sizeY][sizeX];

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
     * @param n
     * @return
     */
    public OperandToken getElement(int n)
    {
        return new DoubleNumberToken(values[n][REAL], values[n][IMAG]);
    }


    /**
     * @param n
     * @param num
     */
    public void setElement(int n, OperandToken num)
    {
        values[n][REAL] = ((DoubleNumberToken)num).getValueRe();
        values[n][IMAG] = ((DoubleNumberToken)num).getValueIm();
    }
    
    /**
     * @param y = y position in matrix
     * @param x = x position in matrix
     * @return an array of double representing the element at y,x 
     */
    public double[] getValueComplex(int y, int x)
    {
        int n = yx2n(y,x);
        return getValueComplex(n);
    }
   
    /**
     * @param n
     * @return
     */
    public double[] getValueComplex(int n)
    {
        return values[n];
    }
    
    /**
     * @param n
     * @param _value
     */
    public void setValueComplex(int n, double[] _value)
    {
        values[n] = _value;
    }

    /**Set value at position y, x
     * @param y = y position in matrix
     * @param x = x position in matrix
     * @param _value = the value to set it to as an array of doubles  
     */    
    public void setValueComplex(int y, int x, double[] _value)
    {
        int n = yx2n(y,x);
        values[n][REAL] = _value[REAL];
        values[n][IMAG] = _value[IMAG];
    }
    
    /** Set value at position y, x
     * @param y = y position in matrix
     * @param x = x position in matrix
     * @param real = real value
     * @param imag = imaginary value
     */    
    public void setValue(int y, int x, double _real, double _imag)
    {
        int n = yx2n(y,x);
        setValue(n, _real, _imag);
    }

    /**
     * @param n
     * @param _real
     * @param _imag
     */
    public void setValue(int n, double _real, double _imag)
    {
        values[n][REAL] = _real;
        values[n][IMAG] = _imag;
    }

    /**
     * @param index  multidimensional index
     * @param _real
     * @param _imag
     */
    public void setValue(int[] index, double _real, double _imag)
    {

        int n = index2n(index);
        
        setValue(n, _real, _imag);
        
    }
    
    /**return the number as a string
     * @return
     */
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
     * @param nn
     * @return
     */
    private String toString2d(int[] nn)
    {
        StringBuffer buffer = new StringBuffer(20);
        
        buffer.append("\n");
        
        for(int yy = 0; yy < sizeA[0]; yy++)
        {
            buffer.append("\n   ");
            for(int xx = 0; xx < sizeA[1]; xx++)
            {
                nn[0] = yy;
                nn[1] = xx;
                int n = index2n(nn);
                //ErrorLogger.debugLine(" NToken: "+index2n(nn));
                
                buffer.append(toString(values[n]));
                
                if(xx < sizeX - 1)
                    buffer.append("   ");
            }           
        }
        
        buffer.append("\n");
        
        return buffer.toString();
    }
        
    /** create string representation of (complex) double values 
     * @param _values[]={REAL,IMAG} real and imaginary part of number
     * @return
     */
    public String toString(double _values[])
    {
        
        if (_values==null)
            return "XXXXXX";
        
        double re = _values[REAL];
        double im = _values[IMAG];
        
        StringBuffer result =  new StringBuffer();

        // real part of number
        // +/- infinity, not a number, number
        if (re == Double.POSITIVE_INFINITY)
            result.append("Inf");
        else if (re == Double.NEGATIVE_INFINITY)    
            result.append("-Inf");
        else if (Double.isNaN(re))    
            result.append("NaN");
        else
            result.append(com.addi.core.interpreter.GlobalValues.numFormat.format(re));

        // imaginary part of number
        if((im != 0.0) || Double.isNaN(im))
        {
            if (im>0)
                result.append(" + ");
            else {
            	result.append(" - ");
            	im = im * -1;
            }
                
            // +/- infinity, not a number, number
            if (im == Double.POSITIVE_INFINITY)
                result.append("Inf");
            else if (im == Double.NEGATIVE_INFINITY)    
                result.append("-Inf");
            else if (Double.isNaN(im))    
                result.append("NaN");
            else
                //result.append(getNumberFormat().format(im));
                result.append(com.addi.core.interpreter.GlobalValues.numFormat.format(im));
            
            result.append("i");
        }
        return  result.toString();
    }

    /**Evaluate the token. This causes it to return itself*/
    //public OperandToken evaluate(Token[] operands)
    //{
    //    return this;    
    //}

    /**Check if two tokens are equal
     * @param arg = the object to check against
     * @return
     */
    public boolean equals(Object arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(arg instanceof DoubleNumberToken)
        {
            DoubleNumberToken nArg = (DoubleNumberToken)arg;
            if(sizeX == nArg.getSizeX() && sizeY == nArg.getSizeY())
            {
                boolean equal = true;
                for (int yy=0; yy<sizeY && equal; yy++)
                {
                        for (int xx=0; xx<sizeX && equal; xx++)
                        {
                            int n = yx2n(yy,xx);
                            if((values[n][REAL] - nArg.getValueRe(n) != 0) ||
                               (values[n][IMAG] - nArg.getValueIm(n) != 0)    )
                                equal = false;
                        }
                }                        
                return equal;
            }
            return false;
        }
        return false;
    }

    /**calculate the arg of the complex number at y, x
     * @param y
     * @param x
     * @return
     */
    public double arg(int y, int x) 
    {
        int n = yx2n(y,x);
        return  Math.atan2(values[n][REAL], values[n][IMAG]);
    }

    ///////////////////////standard operators///////////////////
    ////////////////////////////////////////////////////////////
    
    /**add arg to this object for a number token
     * @param = the value to add to it
     * @return the result as an OperandToken
     */
    public OperandToken add(OperandToken arg)
    {

    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(arg instanceof DoubleNumberToken)
        {

            DoubleNumberToken nArg = ((DoubleNumberToken)arg);
    
            // Check dimensions of matrices 
            if(checkEqualDimensions(sizeA, nArg.sizeA))
            {
                // Add (n*m) + (n*m) or
                //  same dimensions (n,m,r)==(n,m,r)
                ErrorLogger.debugLine("DoubleNumberToken: add (n*m) + (n*m)");
                DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);
    
                for(int n = 0; n < noElem; n++)
                {
                    double real = getValueRe(n) + nArg.getValueRe(n);
                    double imag = getValueIm(n) + nArg.getValueIm(n);
                    result.setValue(n, real, imag);
                }
                
                //ErrorLogger.debugLine("end DoubleNumberToken: add (n*m) + (n*m)");
                return result;      
            } 
            else if(isScalar())
            {
                // 1 + [3,4,5]       
                ErrorLogger.debugLine("DoubleNumberToken: add (1*1) + (n*m)");
                DoubleNumberToken result = new DoubleNumberToken(nArg.sizeA, null, null);
                
                for(int n = 0; n < nArg.getNumberOfElements(); n++)
                {
                    double realval      = getValueRe() + nArg.getValueRe(n);
                    double imaginaryval = getValueIm() + nArg.getValueIm(n);
                    result.setValue(n, realval, imaginaryval);
                }
                
                //ErrorLogger.debugLine("end DoubleNumberToken: add (n*m) + (n*m)");
                return result;      
            } 
            else if(nArg.isScalar())
            {
                // [3,4,5] +1
                ErrorLogger.debugLine("DoubleNumberToken: add (n,m) + (1,1)");
                DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);
                
                for(int n = 0; n < noElem; n++)
                {
                    double realval      = getValueRe(n) + nArg.getValueRe();
                    double imaginaryval = getValueIm(n) + nArg.getValueIm();
                    result.setValue(n, realval, imaginaryval);
                }
                
                //ErrorLogger.debugLine("end DoubleNumberToken: add (n*m) + (n*m)");
                return result;      
            } 
            else
            {
                /* Matrices have unequal size: (n*m) != (o*p) */       
                Errors.throwMathLibException("DoubleNumberToken: add matrices of unequal size");
                return null;
            }
        }
        else if(arg instanceof Int8NumberToken)
        {
            // transform: double + int8   into int8 + double 
            Int8NumberToken nArg = ((Int8NumberToken)arg);
            
            return nArg.add(this);
        }

        Errors.throwMathLibException("DoubleNumberToken: add: no number");
        return null;

    } // and add

    /**subtract arg from this object for a number token
     * @param = the value to subtract
     * @return the result as an OperandToken
     */
    public OperandToken subtract(OperandToken arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: substract: no number");

        DoubleNumberToken nArg    = ((DoubleNumberToken)arg);

        //Check dimensions of matrices 
        if( checkEqualDimensions(this.sizeA, nArg.sizeA) )
        {
            //  Sub (n*m) - (n*m) or      
            //  multidimensional (n*m*r) - (n*m*r)
            ErrorLogger.debugLine("DoubleNumberToken: sub (n*m) - (n*m)");
            DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);

            for(int n = 0; n < noElem; n++)
            {
                double realval      = getValueRe(n) - nArg.getValueRe(n);
                double imaginaryval = getValueIm(n) - nArg.getValueIm(n);
                result.setValue(n, realval, imaginaryval);
            }

            return result;      
        } 
        else if( isScalar() )
        {
            //  1 - [2,3,4]
            ErrorLogger.debugLine("DoubleNumberToken: sub (1*1) - (n*m)");
            DoubleNumberToken result = new DoubleNumberToken(nArg.sizeA, null, null);
            
            for(int n = 0; n < nArg.getNumberOfElements(); n++)
            {
                double realval      = getValueRe() - nArg.getValueRe(n);
                double imaginaryval = getValueIm() - nArg.getValueIm(n);
                result.setValue(n, realval, imaginaryval);
            }
            
            return result;      
        } 
        else if( nArg.isScalar() )
        {
            //  [3,4,5] - 5     
            ErrorLogger.debugLine("DoubleNumberToken: sub (n*m) - (1*1)");
            DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);
            
            for(int n = 0; n < noElem; n++)
            {
                double realval      = values[n][REAL] - nArg.getValueRe();
                double imaginaryval = values[n][IMAG] - nArg.getValueIm();
                result.setValue(n, realval, imaginaryval);
            }
            
            return result;      
        } 
        else
        {
            // Matrices have unequal size: (n*m) != (o*p) 
            Errors.throwMathLibException("DoubleNumberToken: sub matrices of unequal size");
            return null;
        }
    }

    /**Raise this object to the power of arg
     * @param = the value to raise it to the power of
     * @return the result as an OperandToken
     */
    public OperandToken power(OperandToken arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        // works only on numbers
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: powerOf: no number");
       
        // get operand data and size
        DoubleNumberToken nArg = (DoubleNumberToken)arg;
        double[][]   argValuesRe = ((DoubleNumberToken)arg).getValuesRe();
        double[][]   argValuesIm = ((DoubleNumberToken)arg).getValuesIm();
        int          argSizeX    = ((DoubleNumberToken)arg).getSizeX();   
        int          argSizeY    = ((DoubleNumberToken)arg).getSizeY(); 
        
        if ( this.isScalar() )
        {
            // e.g. 4.^[1,2,3;4,5,6]
            
            // return values
            double [][][]results = new double[argSizeY][argSizeX][2];

            for (int y=0; y<argSizeY; y++)
            {
                for (int x=0; x<argSizeX; x++)
                {
                    if ((values[0][REAL]==0)   && (values[0][IMAG]==0)  &&
                        (argValuesRe[y][x]==0) && (argValuesIm[y][x]==0)  )
                    {
                        // 0^[1,0,3]  -> [0,1,0]
                        results[y][x][REAL] = 1.0;
                        results[y][x][IMAG] = 0.0;
                    }
                    else if ((values[0][REAL]==0)   && (values[0][IMAG]==0)  &&
                             (argValuesRe[y][x]!=0) && (argValuesIm[y][x]==0)  )
                    {
                        // 0^[1,0,3]  -> [0,1,0]
                        results[y][x][REAL] = 0.0;
                        results[y][x][IMAG] = 0.0;
                    }
                    else
                    {
                        double re =  Math.log(getValueAbs(0, 0));
                        double im =  getValueArg(0, 0);
                
                        double re2 =  (re*argValuesRe[y][x]) - (im*argValuesIm[y][x]);
                        double im2 =  (re*argValuesIm[y][x]) + (im*argValuesRe[y][x]);
                
                        double scalar =  Math.exp(re2);
                
                        results[y][x][REAL] = scalar * Math.cos(im2);
                        results[y][x][IMAG] = scalar * Math.sin(im2);
                    }
                }
            }
            
            return new DoubleNumberToken(results);
        }
        else if ( nArg.isScalar() )
        {
            // e.g. [1,2,3;4,5,6].^2
            
            // return values
            double [][][]results = new double[sizeY][sizeX][2];

            for (int y=0; y<sizeY; y++)
            {
                for (int x=0; x<sizeX; x++)
                {
                    if ((getValueRe(y,x)==0)    && (getValueIm(y,x)==0)   &&
                        ((argValuesRe[0][0]==0) && (argValuesIm[0][0]==0))   )
                    {
                        // [1,0,3].^0  -> [1,0,1]
                        results[y][x][REAL] = 1.0;
                        results[y][x][IMAG] = 0.0;
                    }
                    else if ((getValueRe(y,x)==0)    && (getValueIm(y,x)==0)   &&
                            ((argValuesRe[0][0]!=0) && (argValuesIm[0][0]==0))   )
                    {
                        // [2,0,3].^2  -> [4,1,9]
                        results[y][x][REAL] = 0.0;
                        results[y][x][IMAG] = 0.0;
                    }
                    else
                    {
                        double re =  Math.log(getValueAbs(y, x));
                        double im =  getValueArg(y, x);
                
                        double re2 =  (re*argValuesRe[0][0]) - (im*argValuesIm[0][0]);
                        double im2 =  (re*argValuesIm[0][0]) + (im*argValuesRe[0][0]);
                
                        double scalar =  Math.exp(re2);
                
                        results[y][x][REAL] = scalar * Math.cos(im2);
                        results[y][x][IMAG] = scalar * Math.sin(im2);
                    }
                }
            }
            
            return new DoubleNumberToken(results);
        }
        else if ( checkEqualDimensions(this.sizeA, nArg.sizeA) )
        {
            // e.g. [1,2,3;4,5,6].^[3,4,5;6,7,8]
            
            // return values
            double [][][]results = new double[sizeY][sizeX][2];

            for (int y=0; y<sizeY; y++)
            {
                for (int x=0; x<sizeX; x++)
                {
                    double re =  Math.log(getValueAbs(y, x));
                    double im =  getValueArg(y, x);
            
                    double re2 =  (re*argValuesRe[y][x]) - (im*argValuesIm[y][x]);
                    double im2 =  (re*argValuesIm[y][x]) + (im*argValuesRe[y][x]);
            
                    double scalar =  Math.exp(re2);
            
                    results[y][x][REAL] = scalar * Math.cos(im2);
                    results[y][x][IMAG] = scalar * Math.sin(im2);
                }
            }
            
            return new DoubleNumberToken(results);
        }

        Errors.throwMathLibException("DoubleNumberToken: .^ dimensions do not fit");
        
        return null;
    } // end powerOf

    
    /** The value to raise it to the matrix power of
     * @param arg
     * @return
     */
    public OperandToken mPower(OperandToken arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        // works only on numbers
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: mPower: no number");
       
        // get operand data and size
        DoubleNumberToken nArg = (DoubleNumberToken)arg;
        double[][] argValuesRe = ((DoubleNumberToken)arg).getValuesRe();
        double[][] argValuesIm = ((DoubleNumberToken)arg).getValuesIm();
        int        argSizeX    = ((DoubleNumberToken)arg).getSizeX();    
        int        argSizeY    = ((DoubleNumberToken)arg).getSizeY(); 
        
        if (this.isScalar() && (argSizeX==1) && (argSizeY==1))
        {
            // e.g. 4.^5
            
            // return values
            double [][][]results = new double[1][1][2];

            double re =  Math.log(getValueAbs(0, 0));
            double im =  getValueArg(0, 0);
    
            double re2 =  (re*argValuesRe[0][0]) - (im*argValuesIm[0][0]);
            double im2 =  (re*argValuesIm[0][0]) + (im*argValuesRe[0][0]);
    
            double scalar =  Math.exp(re2);
    
            results[0][0][REAL] = scalar * Math.cos(im2);
            results[0][0][IMAG] = scalar * Math.sin(im2);
            
            return new DoubleNumberToken(results);

        }
        else if ((sizeX==sizeY) && (argSizeX==1) && (argSizeY==1))
        {
            // [2,3;4,5]^9
            Errors.throwMathLibException("DoubleNumberToken: mPower: [n*n]^scalar not implemented yet");
            return null;
        }
        else if ((sizeX==1) && (sizeY==1) && (argSizeX==argSizeY))
        {
            // 9^[2,3;4,5]
            Errors.throwMathLibException("DoubleNumberToken: mPower: scalar^[n*n] not implemented yet");
            return null;
        }

        Errors.throwMathLibException("DoubleNumberToken: mPower: [n*m]^[o*p] not supported");
        return null;
    } // end matrixPower
    
    
    /**multiply arg by this object for a number token
     * @param arg = the value to multiply it by
     * @return the result as an OperandToken
     */
    public OperandToken multiply(OperandToken arg) 
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: multiply: no number");

        DoubleNumberToken nArg   = (DoubleNumberToken)arg; 
        DoubleNumberToken argValues   = ((DoubleNumberToken)arg); 
        int         argSizeX    = ((DoubleNumberToken)arg).getSizeX();    
        int         argSizeY    = ((DoubleNumberToken)arg).getSizeY(); 

        /* Check if arg is a scalar */
        if( nArg.isScalar() )
        {
            //  Multiply (n*m) = (n*m) * scalar        
            ErrorLogger.debugLine("DoubleNumberToken: multiply ("+sizeY+"*"+sizeX+") * scalar");
            double [][][]results = new double[sizeY][sizeX][2];
            
            double argRe = argValues.getValueRe();
            double argIm = argValues.getValueIm();
            for (int yy=0; yy<sizeY; yy++) 
            {
                for (int xx=0; xx<sizeX; xx++)
                {
                    int n = yx2n(yy,xx);
                    double temp =   values[n][REAL]      * argRe
                        - values[n][IMAG] * argIm;
                    results[yy][xx][IMAG] =   values[n][IMAG] * argRe 
                        + values[n][REAL]      * argIm;
                    results[yy][xx][REAL]      = temp;
                }
            }
            return new DoubleNumberToken(results);
        } 
        else if( this.isScalar() )
        {
            /* the DoubleNumberToken of this class is a scalar */
            /*  Multiply (n*m) = scalar * (n*m) */       
            ErrorLogger.debugLine("DoubleNumberToken: multiply scalar * (n*m) ");

            //argValues = argValues.times(values.get(0,0));
            //values[0][0] = multiply(values[0][0], argValues.getValueComplex(0, 0));
            return arg.multiply(this);      
        }
        else if (sizeX == argSizeY && (sizeA.length==2))
        {
            /*  Multiply (n*o) = (n*m) * (m*o) */  
            ErrorLogger.debugLine("DoubleNumberToken: multiply (n*m) * (m*o)");
            //Zmat resultValues = values.times(argValues);
            double[][][] results = new double[sizeY][argSizeX][2];

            for(int i=0; i<sizeY; i++)
            {
                for (int k=0; k<argSizeX; k++)
                {
                    results[i][k][REAL] = 0;
                    results[i][k][IMAG] = 0;
                    for (int j=0; j<sizeX; j++)
                    {
                        int n = yx2n(i,j);
                        double temp[] = multiply(values[n], argValues.getValueComplex(j, k));
                        results[i][k][REAL] += temp[REAL];
                        results[i][k][IMAG] += temp[IMAG];
                    }
                }
            }

            return new DoubleNumberToken(results);
        }
        else
        {
            /* dimensions do not match */
            Errors.throwMathLibException("DoubleNumberToken: multiply: dimensions don't match");
            return null;
        }
    } // end multiply

    /**Multiplies two complex numbers
     * @param arg1 = the first complex number as an array of double
     * @param arg2 = the second complex number as an array of double
     * @return the result as an array of double
     */
    public double[] multiply(double[] arg1, double[]arg2) 
    {
        double[] temp = new double[2];
        temp[REAL]    = (arg1[REAL] * arg2[REAL]) - (arg1[IMAG] * arg2[IMAG]);
        temp[IMAG]    = (arg1[REAL] * arg2[IMAG]) + (arg1[IMAG] * arg2[REAL]);
        return  temp;
    }

    /**divide this object by arg for a number token
     * @param arg = the value to divide it by
     * @return the result as an OperandToken
     */
    public OperandToken divide(OperandToken arg)
    {       
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: divide: no number");

        DoubleNumberToken nArg = (DoubleNumberToken)arg;
        int        argSizeX     = nArg.getSizeX();  
        int        argSizeY     = nArg.getSizeY(); 

        // Check if arg is a scalar 
        if( nArg.isScalar() )
        {
            //  Divide (n*m) = (n*m) / scalar       

            DoubleNumberToken result = new DoubleNumberToken(); //this);
            result.setSize(sizeY, sizeX);
            
            double[] argValue = nArg.getValueComplex(0, 0);
            ErrorLogger.debugLine("DoubleNumberToken: divide (n*m) / scalar");
            for (int yy=0; yy<sizeY; yy++) 
            {
                for (int xx=0; xx<sizeX; xx++)
                {
                    int n = yx2n(yy,xx);
                    result.setValueComplex(yy, xx, divide(values[n], argValue));
                }
            }
            return result;      
        } 
        else if( this.isScalar() )
        {
            // the DoubleNumberToken of this class is a scalar 
            //  Divide (n*m) = scalar / (n*m) 
            ErrorLogger.debugLine("DoubleNumberToken: divide scalar / (n*m) ");
            DoubleNumberToken result = new DoubleNumberToken(1);
            double[] value = values[0];
            for (int yy=0; yy<argSizeY; yy++)
            {
                for (int xx=0; xx<argSizeX; xx++)
                {
                    result.setValueComplex(yy, xx, divide(value, nArg.getValueComplex(yy, xx)));
                }
            }
            return result;      
         }
        else
        {
            Errors.throwMathLibException("DoubleNumberToken: divide: dimensions don't match");
            return null;
        }

    } // end divide

    /** divide two complex numbers and return a complex number again,
        pay special attention to infinity and not a number 
       @param arg1 = the first complex number as an array of double
       @param arg2 = the second complex number as an array of double
       @return the result as an array of double*/
    public double[] divide(double[] arg1, double[] arg2)
    {
        /* {REAL,IMAG} = divide( {REAL,IMAG}, {REAL,IMAG}) */
        double x = arg2[REAL];
        double y = arg2[IMAG];
        
        double zRe, zIm;
        double scalar;
        double[] temp = new double[2];

        if ((x==0.0) && (y==0.0))
        {
            // something like 1/0  50/0 or 0/0
            // real part
            if (Double.isNaN(arg1[REAL]))
                zRe = Double.NaN;
            else if (arg1[REAL]>0)
                zRe = Double.POSITIVE_INFINITY; 
            else if (arg1[REAL]<0)
                zRe = Double.NEGATIVE_INFINITY;
            else
                zRe = Double.NaN; 
             
            // something like (1+i)/0  50i/0 
            // imaginary part
            if (Double.isNaN(arg1[IMAG]))
                zIm = Double.NaN;
            else if (arg1[IMAG]>0)
                zIm = Double.POSITIVE_INFINITY; 
            else if (arg1[IMAG]<0)
                zIm = Double.NEGATIVE_INFINITY;
            else
                zIm = 0.0; 
        }
        else if(Math.abs(x) >= Math.abs(y)) 
        {
            scalar =  1.0 / ( x + y*(y/x) );

            zRe =  scalar * (arg1[REAL] + arg1[IMAG]*(y/x));
            zIm =  scalar * (arg1[IMAG] - arg1[REAL]*(y/x));

        } 
        else 
        {
            scalar =  1.0 / ( x*(x/y) + y );

            zRe =  scalar * (arg1[REAL]*(x/y) + arg1[IMAG]);
            zIm =  scalar * (arg1[IMAG]*(x/y) - arg1[REAL]);
        }

        temp[REAL] = zRe;
        temp[IMAG] = zIm;
        
        return temp;
    }
    
    //////////////////////////////////////////SCALAR OPERATORS//////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**scalar multiply arg by this object for a number token
    @arg = the value to multiply it by
    @return the result as an OperandToken*/
    public OperandToken scalarMultiply(OperandToken arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: scalar multiply: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);

        if ( checkEqualDimensions(this.sizeA, nArg.sizeA) )
        {
            //  scalar multiplication (n*m) = (n*m) .* (n*m) 
            ErrorLogger.debugLine("DoubleNumberToken: multiply (n*m) .* (n*m)");
            DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);

            for (int n=0; n<noElem; n++)
            {
                double[] argVal = nArg.getValueComplex(n);
                result.setValueComplex(n, multiply(values[n], argVal) );                       
            }
            return result;      
        }
        else if ( nArg.isScalar() )
        {
            //  scalar multiplication (n*m) .* (1*1) 
            ErrorLogger.debugLine("DoubleNumberToken: multiply (n*m) .* (1*1)");
            DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);

            for (int n=0; n<noElem; n++)
            {
                double[] argVal = nArg.getValueComplex(0);
                result.setValueComplex(n, multiply(values[n], argVal) );                       
            }
            return result;      
        }
        else if ( this.isScalar() )
        {
            //  scalar multiplication (1*1) .* (n*m) 
            ErrorLogger.debugLine("DoubleNumberToken: multiply (1*1) .* (n*m)");
            DoubleNumberToken result = new DoubleNumberToken(nArg.sizeA, null, null);
                    
            for (int n=0; n<nArg.noElem; n++)
            {
                double[] argVal = nArg.getValueComplex(n);
                result.setValueComplex(n, multiply(values[0], argVal) );                       
            }
            return result;      
        }
        else
        {
            //dimensions do not match 
            Errors.throwMathLibException("DoubleNumberToken: scalar multiply: dimensions don't match");
            return null;
        }

    } // end scalarMultiply

    /**scalar divide arg by this object for a number token
    @arg = the value to divide it by
    @return the result as an OperandToken*/
    public OperandToken scalarDivide(OperandToken arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
            
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: scalar divide: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
        double[][] argValues    = nArg.getValuesRe(); 
        double[][] argValuesImg = nArg.getValuesIm(); 
        int        argSizeX     = nArg.getSizeX();  
        int        argSizeY     = nArg.getSizeY(); 

        ErrorLogger.debugLine("DoubleNumberToken: scalarDivide. " +sizeY+" "+sizeX+" "+argSizeY+" "+argSizeX);
        
        if ((sizeX == argSizeX) && (sizeY == argSizeY))
        {
            //  divide multiplication (n*m) = (n*m) .* (n*m) 
            ErrorLogger.debugLine("DoubleNumberToken: scalar divide (n*m) .* (n*m)");
            double[][][] results = new double[sizeY][sizeX][2];
                    
            for (int y=0; y<sizeY; y++)
            {
                for (int x=0; x<sizeX; x++)
                {
                    int n = yx2n(y,x);
                    double[] argVal = nArg.getValueComplex(y, x);
                    results[y][x] = divide(values[n], argVal);
                }
            }
            return new DoubleNumberToken(results);    
        }
        else if ( nArg.isScalar() )
        {
            //  divide multiplication (n*m) ./ (1,1) 
            ErrorLogger.debugLine("DoubleNumberToken: scalar divide (n*m) ./ (1*1)");
            double[][][] results = new double[sizeY][sizeX][2];
                    
            for (int y=0; y<sizeY; y++)
            {
                for (int x=0; x<sizeX; x++)
                {
                    int n = yx2n(y,x);
                    double[] argVal = nArg.getValueComplex(0, 0);
                    results[y][x] = divide(values[n], argVal);
                }
            }
            return new DoubleNumberToken(results);    
        }
        else if ( this.isScalar() )
        {
            //  divide multiplication (n*m) ./ (1,1) 
            ErrorLogger.debugLine("DoubleNumberToken: scalar divide (1*1) ./ (n*m)");
            double[][][] results = new double[argSizeY][argSizeX][2];
                    
            for (int y=0; y<argSizeY; y++)
            {
                for (int x=0; x<argSizeX; x++)
                {
                    double[] val = getValueComplex(0, 0);
                    results[y][x] = divide(val, nArg.getValueComplex(y,x) );
                }
            }
            return new DoubleNumberToken(results);    
        }
        else
        {
            //dimensions do not match 
            Errors.throwMathLibException("DoubleNumberToken: scalar multiply: dimensions don't match");
            return null;
        }
        
    } // end scalarDivide

    /**left divide 
    @arg = 
    @return the result as an OperandToken*/
    public OperandToken leftDivide(OperandToken arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: left divide: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
        DoubleNumberToken num  = new DoubleNumberToken(nArg.getReValues());
        
        //return num.divide(new DoubleNumberToken(values));
        return num.divide(new DoubleNumberToken( this.getValuesRe(),this.getValuesIm() ));
        
    } // end leftDivide

    /**scalar left divide 
    @arg = 
    @return the result as an OperandToken*/
    public OperandToken scalarLeftDivide(OperandToken arg)
    {
    	if (arg instanceof LogicalToken)
    		arg = ((LogicalToken)arg).getDoubleNumberToken();
    	
    	if (arg instanceof CharToken)
    		arg = ((CharToken)arg).getDoubleNumberToken();
    	
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: scalar left divide: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
        DoubleNumberToken num  = new DoubleNumberToken(nArg.getReValues());
        
        //return num.scalarDivide(new DoubleNumberToken(values));
        return num.scalarDivide(new DoubleNumberToken(this.getValuesRe(),this.getValuesIm()));
        
    } // end scalarLeftDivide

    /**calculate the transpose of a matrix
    @return the result as an OperandToken*/
    public OperandToken transpose()
    {
        // transposed array
        double[][] real = new double[sizeX][sizeY];
        double[][] imag = new double[sizeX][sizeY];
    
        // swap rows and columns
        for (int y=0; y<sizeY; y++)
        {
            for (int x=0; x<sizeX; x++)
            {
                int n = yx2n(y,x);
                // copy (y,x) -> (x,y), also change sign of imaginary part
                real[x][y] = values[n][REAL];
                imag[x][y] = values[n][IMAG] * (-1);
            }
        }
        return new DoubleNumberToken(real, imag);     
    }

    /**calculate the conjugate transpose of a matrix
    @return the result as an OperandToken*/
    public OperandToken ctranspose()
    {
        // transposed array
        double[][] real = new double[sizeX][sizeY];
        double[][] imag = new double[sizeX][sizeY];
    
        // swap rows and columns
        for (int y=0; y<sizeY; y++)
        {
            for (int x=0; x<sizeX; x++)
            {
                int n = yx2n(y,x);
                // copy (y,x) -> (x,y), for conjugate transpose do not
                //   change sign of imaginary part
                real[x][y] = values[n][REAL];
                imag[x][y] = values[n][IMAG] ;
            }
        }
        return new DoubleNumberToken(real, imag);     
    }


    ///////////////////////////////////////Standard Math Functions///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**standard function - returns the negative of the number*/
    public OperandToken negate()
    {
        DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);
        
        for(int n = 0; n < noElem; n++)
        {
            result.setValue(n, -getValueRe(n), -getValueIm(n) );                
        }
        return result;      
    }


    /**Standard functions - calculates the factorial of the number
       @return the result as an OperandToken*/
    public OperandToken factorial()
    {
        DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);
        
        for(int n = 0; n < noElem; n++)
        {
            double amount = Math.rint(getValueRe(n));
            result.setValue(n, 
                            factorial(amount) ,
                            0                  );                
        }
        return result;
    }

    /**Calulates the factorial of a real value
       @param amount = the number to calc the factorial of
       @return the result as a double*/
    public double factorial(double amount)
    {
        double answer = 1;
        for(int count = 1; count <= amount; count++)
            answer *= count;
            
        return answer;
    }
    
    //////////////////////////////////////Complex Functions/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    /**Complex function - calculates the complex conjugate of the number*/
    public OperandToken conjugate()
    {
        DoubleNumberToken result = new DoubleNumberToken(sizeA, null, null);
        
        for(int n = 0; n < noElem; n++)
        {
            result.setValue(n, 
                            getValueRe(n)   ,
                            -getValueIm(n)  );                
        }
        return result;      
    }

    //////////////////////////////////////Test Functions///////////////////////////////////////  
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    /**Checks if this operand is zero
    @return true if this number == 0 or that all values are
    0 for a matrix*/
    public boolean isNull()
    {

        for (int n=0; n<noElem; n++)
        {
            if( (getValueRe(n)!=0) || (getValueIm(n)!=0) )
                return false;
        }
        return true;
    }   

    /**@return true if this number token is a real matrix, without an imaginary part*/
    public boolean isReal()
    {
        // if at least one element has an imaginary part the matrix is not real 
        for (int n=0; n<noElem; n++)
        {
            if (getValueIm(n) != 0)
                return false;
        }
        return true;
    }

    /**@return true if this number token is an imaginary matrix, without a real part*/
    public boolean isImaginary()
    {
        // if at least one element has a real part the matrix is not imaginary 
        for (int n=0; n<noElem; n++)
        {
            if (getValueIm(n) != 0)
                return false;
        }
        return true;
    }
    
    /**@return true if this number token is a complex matrix with both real and imaginary parts*/
    public boolean isComplex()
    {
        // if at least one element has a real value and also on element has
        //  an imaginary part the matrix is complex 
        boolean isRe = false;
        boolean isIm = false;
        
        for (int n=0; n<noElem; n++)
        {
            if (getValueRe(n) != 0)
                isRe = true;
            if (getValueIm(n)!= 0)
                isIm = true;
        }
        
        // check if both real and imaginary tags are set
        if (isRe && isIm)
            return true;
        else
            return false;
    }

} // end DoubleNumberToken

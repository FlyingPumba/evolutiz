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

import java.text.NumberFormat;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**Class representing numbers used in expression
holds a 2D array of complex numers in a 3d array
values[y][x][REAL/IMAGINARY]
All operations on a DoubleNumberToken create a new DoubleNumberToken*/
public class SparseNumberToken extends DataToken
{            
    /**Complex values of the token*/
    private double values[][][];

    /**Constant value set to 1*/
    public static final DoubleNumberToken one = new DoubleNumberToken(1);

    /**Constant value set to 0*/
    public static final DoubleNumberToken zero = new DoubleNumberToken(0);

    /**Constant value set to 2*/
    public static final DoubleNumberToken two = new DoubleNumberToken(2);

    /**Constant value set to j*/
    public static final DoubleNumberToken j = new DoubleNumberToken(0,1);

    /**stores the number format for displaying the number*/
    private static NumberFormat numFormat = NumberFormat.getInstance();

    /**Index for real values within array*/
    private static final int REAL = 0;
    
    /**Index for Imaginary values within array*/
    private static final int IMAGINARY = 1;

    /** Constructor creating empty number token
     */
    public SparseNumberToken()
    {
        super(5, "sparse"); 
        sizeY  = 0;
        sizeX  = 0;
        values = null;
    }

    /** Constructor creating a scalar taking the numbers value as a string
     * @param _value = the numbers value as a string
     */
    public SparseNumberToken(String _value)
    {
        this(_value, "");
    }

    /** Constructor creating a scalar taking the numbers value as a double
     * @param _value = the numbers value as a double
     */
    public SparseNumberToken(double _value)
    {
        this(_value, 0);
    }

    /** Constructor taking the numbers value as a double[][]
     * @param _value = the numbers value as a 2D array of double
     */
    public SparseNumberToken(double[][] _values)
    {
        this(_values, null);
    }

    /**Constructor taking the numbers value as a string
     * @param _real = the numbers real value as a string
     * @param _imaginary = the numbers imaginary value as a string
     */
    public SparseNumberToken(String _real, String _imaginary)
    {
        super(5, "sparse"); 
	    sizeX = 1;
	    sizeY = 1;
        values = new double[1][1][2];

        if (!_real.equals(""))
            values[0][0][REAL] = new Double(_real).doubleValue();

        if (!_imaginary.equals(""))
            values[0][0][IMAGINARY] = new Double(_imaginary).doubleValue();
    }

    /**Constructor taking the numbers value as a pair of double
     *  values representing real and imaginary part
     * @param _real = the numbers real value as a double
     * @param _imaginary = the numbers imaginary value as a double
     */
    public SparseNumberToken(double _real, double _imaginary)
    {
        super(5, "sparse"); 
		sizeX = 1;
		sizeY = 1;

        values = new double[1][1][2];
        values[0][0][REAL]      = _real;
        values[0][0][IMAGINARY] = _imaginary;
    }

    /**Constructor taking the numbers value as a pair of double
    values representing real and imaginary part
    @param _values = the values as a array containing the real and
    imaginary values*/
    public SparseNumberToken(double[] _values)
    {
        super(5, "sparse"); 
		sizeX = 1;
		sizeY = 1;

        values = new double[1][1][2];
        values[0][0][REAL]      = _values[REAL];
        values[0][0][IMAGINARY] = _values[IMAGINARY];
    }

    /**Constructor taking the numbers value as two double[][]
    @param _real = the numbers value as a 2D array of double
    @param _imaginary = the numbers value as a 2D array of double*/
    public SparseNumberToken(double[][] _real, double[][] _imaginary)
    {
        super(5, "sparse"); 
		sizeY  = _real.length;
		sizeX  = _real[0].length;

        values = new double[sizeY][sizeX][2];        

        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                values[yy][xx][REAL] = _real[yy][xx];

                // imaginary number may be null
                if (_imaginary != null)
                    values[yy][xx][IMAGINARY] = _imaginary[yy][xx];
                else
                    values[yy][xx][IMAGINARY] = 0;
            }
        }
    }

    /**Constructor taking the numbers value as a double[][][]
       @param _values = the numbers value as a 3D array of double*/
    public SparseNumberToken(double[][][] _values)
    {
        super(5, "sparse"); 
		sizeY  = _values.length;
		sizeX  = _values[0].length;

        values = new double[sizeY][sizeX][2];        

        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                values[yy][xx][REAL]      = _values[yy][xx][REAL];
                values[yy][xx][IMAGINARY] = _values[yy][xx][IMAGINARY];
            }
        }
    }


    /** return a new Number Token of size y*x
     * 
     */
    public DataToken getElementSized(int y, int x)
    {
        return new DoubleNumberToken(new double[y][x][2]); 
    }

    /** increase/decrease the size of the current DoubleNumberToken to size y*x
     *  @param dy number of rows
     *  @param dx number of columns
     */
    public void setSize(int dy, int dx)
    {
        double[][][] newValues = new double[dy][dx][2];        

        ErrorLogger.debugLine("number "+dy+" "+dx);
        ErrorLogger.debugLine("number "+sizeY+" "+sizeX);
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                ErrorLogger.debugLine("number "+yy+" "+xx);
                newValues[yy][xx][REAL]      = values[yy][xx][REAL];
                newValues[yy][xx][IMAGINARY] = values[yy][xx][IMAGINARY];
            }
        }
        values = newValues;
        sizeY  = dy;
        sizeX  = dx;
    } // end setSize

    /**@return the real value of the first number*/
    public double getValue()
    {
        return values[0][0][REAL];
    }

    /**@return the real value of the first number*/
    //public double getValueRe()
    //{
    //    return values[0][0][REAL];
    //}

    /**@return the real value of the number at position y, x*/
    public double getValueRe(int y, int x)
    {
        return values[y][x][REAL];
    }

    /**@return the real value of the first number as an integer*/
    //public int getIntValue()
    //{
	//    double temp = values[0][0][REAL];
	//    return (new Double(temp)).intValue();
    //}

    /**@return the real value of the number at position y, x as an integer*/
    public int getIntValue(int y, int x)
    {
	    double temp = values[x][y][REAL];
	    return (new Double(temp)).intValue();
    }
    
    /**@return the imaginary value of the first number*/
    public double getValueIm()
    {
        return values[0][0][IMAGINARY];
    }

    /**@return the imaginary value of the number at position y, x*/
    public double getValueIm(int y, int x)
    {
        return values[y][x][IMAGINARY];
    }

    /**@return the absolute value of the number at position y, x*/
    public double getValueAbs(int y, int x)
    {
        double temp = Math.pow(values[y][x][REAL], 2) + Math.pow(values[y][x][IMAGINARY], 2);
        return Math.sqrt(temp);
    }

    /**@return the angle of the number at position y, x in radians*/
    public double getValueArg(int y, int x)
    {
        return Math.atan2(values[y][x][IMAGINARY], values[y][x][REAL]);
    }

    /**@return the value of the number (old notation)*/
    public double[][] getValuesSSSSS()
    {
        return getReValues();
    }

    /**@return the real values of the number*/
    public double[][] getReValues()
    {    	
        double[][] temp = new double[sizeY][sizeX];

        if (sizeY==0 && sizeX==0)
            return null;
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                temp[yy][xx] = values[yy][xx][REAL];
            }
        }
        return temp;
    }

    /**@return the imaginary values of the number*/
    public double[][] getImValues()
    {
        double[][] temp = new double[sizeY][sizeX];

        if (sizeY==0 && sizeX==0)
            return null;

        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                temp[yy][xx] = values[yy][xx][IMAGINARY];
            }
        }
        return temp;
    }
    
    /**@return the imaginary value of the number*/
    public double[][] getValuesImg()
    {
        return getImValues();
    }

    /*public OperandToken getElement(int y, int x)
    {
        return new DoubleNumberToken(values[y][x]);
    }*/

    /*public OperandToken getElement(int n)
    {
        int x = (int) (n/sizeY); // column to start
        int y = n - x*sizeY;     // row to start

        return new DoubleNumberToken(values[y][x]);
    }*/

    public void setElement(int y, int x, OperandToken num)
    {
        double real = ((DoubleNumberToken)num).getValueRe();
        double imag = ((DoubleNumberToken)num).getValueIm();
        
        ErrorLogger.debugLine("DoubleNumberToken("+y+","+x+")"+ real+" "+imag);
        values[y][x][REAL]      = real;
        values[y][x][IMAGINARY] = imag;
    }

    public void setElement(int n, OperandToken num)
    {
        int    x    = (int)(n/sizeY);  // column to start
        int    y    = n - x*sizeY;     // row to start
        double real = ((DoubleNumberToken)num).getValueRe();
        double imag = ((DoubleNumberToken)num).getValueIm();
        
        ErrorLogger.debugLine("DoubleNumberToken("+y+","+x+")"+ real+" "+imag);
        values[y][x][REAL]      = real;
        values[y][x][IMAGINARY] = imag;
    }

    
    /**@return an array of double representing the element at y,x
    @param y = y position in matrix
    @param x = x position in matrix*/
    public double[] getValueComplex(int y, int x)
    {
        return values[y][x];
    }
    
    /**Set value at position y, x
    @param y = y position in matrix
    @param x = x position in matrix
    @param _value = the value to set it to as an array of doubles  
    @param imag = imaginary value
    */    
    public void setValueComplex(int y, int x, double[] _value)
    {
        values[y][x][REAL]      = _value[REAL];
        values[y][x][IMAGINARY] = _value[IMAGINARY];
    }
    
    /**Set value at position y, x
    @param y = y position in matrix
    @param x = x position in matrix
    @param real = real value
    @param imag = imaginary value
    */    
    public void setValue(int y, int x, double _real, double _imag)
    {
        values[y][x][REAL]      = _real;
        values[y][x][IMAGINARY] = _imag;
    }

    /**return the number as a string*/
    public String toString()
    {
	    String result = null;
        if((sizeY == 0) && (sizeX == 0))
        {
            result = "[]";
        }
	    else if((sizeY == 1) && (sizeX == 1))
	    {
	        result = toString(values[0][0]);
	    }
	    else
	    {
    	    StringBuffer buffer = new StringBuffer(20);
	        for(int yy = 0; yy < sizeY; yy++)
	        {
        		buffer.append("[");
		        for(int xx = 0; xx < sizeX; xx++)
		        {
		            buffer.append(toString(values[yy][xx]));
					
		            if(xx < sizeX - 1)
			            buffer.append(" , ");
		        }			
		        buffer.append("]\n");
	        }
	        result = new String(buffer);
	    }			
	    return result;
    }

    /** create string representation of (complex) double values 
    @param _values[]={REAL,IMAGINARY} real and imaginary part of number*/
    public String toString(double _values[])
    {
        double re = _values[REAL];
        double im = _values[IMAGINARY];
        
        StringBuffer result =  new StringBuffer();
        
        if((im != 0.0) || Double.isNaN(im))
            result.append("(");

        // real part of number
        // +/- infinity, not a number, number
        if (re == Double.POSITIVE_INFINITY)
        	result.append("Inf");
        else if (re == Double.NEGATIVE_INFINITY)    
        	result.append("-Inf");
        else if (Double.isNaN(re))    
        	result.append("NaN");
		else
        	result.append(numFormat.format(re));

        // imaginary part of number
        if((im != 0.0) || Double.isNaN(im))
        {
	        if ((re!=0.0) && !(im<0))
            	result.append("+");
                
            // +/- infinity, not a number, number
	        if (im == Double.POSITIVE_INFINITY)
	        	result.append("Inf");
	        else if (im == Double.NEGATIVE_INFINITY)    
	        	result.append("-Inf");
	        else if (Double.isNaN(im))    
	        	result.append("NaN");
			else
	        	result.append(numFormat.format(im));
        
            result.append("i)");
        }
        return  result.toString();
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
                            if((values[yy][xx][REAL] - nArg.getValueRe(yy, xx) != 0) ||
                                (values[yy][xx][IMAGINARY] - nArg.getValueIm(yy, xx) != 0))
                                equal = false;
                        }
                }                        
                return equal;
            }
            return false;
        }
        return false;
    }

    /**calculate the arg of the complex number at y, x*/
    public double arg(int y, int x) 
    {
        return  Math.atan2(values[y][x][REAL], values[y][x][IMAGINARY]);
    }

    ///////////////////////standard operators///////////////////
    ////////////////////////////////////////////////////////////
    
    /**add arg to this object for a number token
    @param = the value to add to it
    @return the result as an OperandToken*/
    public OperandToken add(OperandToken arg)
    {
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: add: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
        int        argSizeX 	= nArg.getSizeX();	
        int        argSizeY 	= nArg.getSizeY(); 

        // Check dimensions of matrices 
        if((sizeX == argSizeX) && (sizeY == argSizeY))
        {
            //  Add (n*m) + (n*m)        
            ErrorLogger.debugLine("DoubleNumberToken: add (n*m) + (n*m)");
	        DoubleNumberToken result = new DoubleNumberToken(values); 

            for(int yy = 0; yy < sizeY; yy++)
            {
                for(int xx = 0; xx < sizeX; xx++)
                {
                    double realval      = values[yy][xx][REAL]      + nArg.getValueRe(yy, xx);
                    double imaginaryval = values[yy][xx][IMAGINARY] + nArg.getValueIm(yy, xx);
                    result.setValue(yy, xx, realval, imaginaryval);
                }
            }
            //ErrorLogger.debugLine("end DoubleNumberToken: add (n*m) + (n*m)");
            return result;   	
        } 
        else if((sizeX==1) && (sizeY==1))
        {
            // 1 + [3,4,5]       
            ErrorLogger.debugLine("DoubleNumberToken: add (1*1) + (n*m)");
            DoubleNumberToken result = nArg;
            double re = getValueRe(0, 0);
            double im = getValueIm(0, 0);
            
            for(int yy = 0; yy < argSizeY; yy++)
            {
                for(int xx = 0; xx < argSizeX; xx++)
                {
                    double realval      = nArg.getValueRe(yy,xx)  + re;
                    double imaginaryval = nArg.getValueIm(yy,xx) + im;
                    result.setValue(yy, xx, realval, imaginaryval);
                }
            }
            //ErrorLogger.debugLine("end DoubleNumberToken: add (n*m) + (n*m)");
            return result;      
        } 
        else if((argSizeX==1) && (argSizeY==1))
        {
            // [3,4,5] +1
            ErrorLogger.debugLine("DoubleNumberToken: add (n,m) + (1,1)");
            DoubleNumberToken result = new DoubleNumberToken(values);
            double re = nArg.getValueRe(0, 0);
            double im = nArg.getValueIm(0, 0);
            
            for(int yy = 0; yy < sizeY; yy++)
            {
                for(int xx = 0; xx < sizeX; xx++)
                {
                    double realval      = values[yy][xx][REAL]      + re;
                    double imaginaryval = values[yy][xx][IMAGINARY] + im;
                    result.setValue(yy, xx, realval, imaginaryval);
                }
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
    } // and add

    /**subtract arg from this object for a number token
    @param = the value to subtract
    @return the result as an OperandToken*/
    public OperandToken subtract(OperandToken arg)
    {
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: substract: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
        int        argSizeX 	= nArg.getSizeX();	
        int        argSizeY 	= nArg.getSizeY(); 

        //Check dimensions of matrices 
        if((sizeX == argSizeX) && (sizeY == argSizeY))
        {
            //  Sub (n*m) - (n*m)     
            ErrorLogger.debugLine("DoubleNumberToken: sub (n*m) - (n*m)");
	        DoubleNumberToken result = new DoubleNumberToken(values); 

            for(int yy = 0; yy < sizeY; yy++)
            {
                for(int xx = 0; xx < sizeX; xx++)
                {
                    double realval      = values[yy][xx][REAL]      - nArg.getValueRe(yy, xx);
                    double imaginaryval = values[yy][xx][IMAGINARY] - nArg.getValueIm(yy, xx);
                    result.setValue(yy, xx, realval, imaginaryval);
                }
            }
            return result;   	
        } 
        else if((sizeX == 1) && (sizeY == 1))
        {
            //  1 - [2,3,4]
            ErrorLogger.debugLine("DoubleNumberToken: sub (1*1) - (n*m)");
            DoubleNumberToken result = nArg;
            double re = getValueRe(0, 0);
            double im = getValueIm(0, 0);

            for(int yy = 0; yy < argSizeY; yy++)
            {
                for(int xx = 0; xx < argSizeX; xx++)
                {
                    double realval      = re - nArg.getValueRe(yy,xx);
                    double imaginaryval = im - nArg.getValueIm(yy,xx);
                    result.setValue(yy, xx, realval, imaginaryval);
                }
            }
            return result;      
        } 
        else if((argSizeX == 1) && (argSizeY == 1))
        {
            //  [3,4,5] - 5     
            ErrorLogger.debugLine("DoubleNumberToken: sub (n*m) - (1*1)");
            DoubleNumberToken result = new DoubleNumberToken(values); 
            double re = nArg.getValueRe(0, 0);
            double im = nArg.getValueIm(0, 0);

            for(int yy = 0; yy < sizeY; yy++)
            {
                for(int xx = 0; xx < sizeX; xx++)
                {
                    double realval      = values[yy][xx][REAL]      - re;
                    double imaginaryval = values[yy][xx][IMAGINARY] - im;
                    result.setValue(yy, xx, realval, imaginaryval);
                }
            }
            return result;      
        } 
        else
        {
            // Matrices have unequal size: (n*m) != (o*p) 
            ErrorLogger.debugLine("DoubleNumberToken: sub matrices of unequal size");
            return null;
        }
    }

    /**Raise this object to the power of arg
    @param = the value to raise it to the power of
    @return the result as an OperandToken*/
    public OperandToken power(OperandToken arg)
    {
        // works only on numbers
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: powerOf: no number");
       
        //double[][] argValuesImg	= ((DoubleNumberToken)arg).getValuesImg();
        int        argSizeX 	= ((DoubleNumberToken)arg).getSizeX();	
        int        argSizeY 	= ((DoubleNumberToken)arg).getSizeY(); 
		
        if ((sizeX==1) && (sizeY==1))
        {
            // e.g. [1,2,3;4,5,6].^4
            
            double [][][]results = new double[sizeY][sizeX][2];

        	// bug in Z()-class: 0^something equals 0
            if (values[0][0][REAL] == 0 && values[0][0][IMAGINARY] == 0)
                return zero;

            double[] argValues = ((DoubleNumberToken)arg).getValueComplex(0, 0);
            //Complex result = Z.pow(values.get(0,0), argValues.get(0, 0));

            double re =  Math.log(getValueAbs(0, 0));
            double im =  getValueArg(0, 0);
    
            double re2 =  (re*argValues[REAL]) - (im*argValues[IMAGINARY]);
            double im2 =  (re*argValues[IMAGINARY]) + (im*argValues[REAL]);
    
            double scalar =  Math.exp(re2);
    
            results[0][0][REAL]      = scalar * Math.cos(im2);
            results[0][0][IMAGINARY] = scalar * Math.sin(im2);
            
            return new DoubleNumberToken(results);
        }
        else if ((argSizeX==1) && (argSizeY==1))
        {
            // e.g. [1,2,3;4,5,6].^2
            
        }
        
        
        Errors.throwMathLibException("DoubleNumberToken: power invalid array");
        return null;
    } // end powerOf

    /**multiply arg by this object for a number token
    @param arg = the value to multiply it by
    @return the result as an OperandToken*/
    public OperandToken multiply(OperandToken arg) 
    {
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: multiply: no number");

        DoubleNumberToken argValues 	= ((DoubleNumberToken)arg); 
        int         argSizeX 	= ((DoubleNumberToken)arg).getSizeX();	
        int         argSizeY 	= ((DoubleNumberToken)arg).getSizeY(); 

        /* Check if arg is a scalar */
        if((argSizeX == 1) && (argSizeY == 1))
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
    		        double temp =   values[yy][xx][REAL]      * argRe
    			        - values[yy][xx][IMAGINARY] * argIm;
    		        results[yy][xx][IMAGINARY] =   values[yy][xx][IMAGINARY] * argRe 
    			        + values[yy][xx][REAL]      * argIm;
    		        results[yy][xx][REAL]      = temp;
                }
    		}
            return new DoubleNumberToken(results);
        } 
        else if((sizeX == 1) && (sizeY == 1))
        {
            /* the DoubleNumberToken of this class is a scalar */
            /*  Multiply (n*m) = scalar * (n*m) */       
            ErrorLogger.debugLine("DoubleNumberToken: multiply scalar * (n*m) ");

            //argValues = argValues.times(values.get(0,0));
            //values[0][0] = multiply(values[0][0], argValues.getValueComplex(0, 0));
            return arg.multiply(this);   	
        }
        else if (sizeX == argSizeY)
        {
            /*  Multiply (n*o) = (n*m) * (m*o) */  
            ErrorLogger.debugLine("DoubleNumberToken: multiply (n*m) * (m*o)");
            //Zmat resultValues = values.times(argValues);
            double[][][] results = new double[sizeY][argSizeX][2];

            for(int i=0; i<sizeY; i++)
            {
                for (int k=0; k<argSizeX; k++)
                {
                    results[i][k][REAL]      = 0;
                    results[i][k][IMAGINARY] = 0;
                    for (int j=0; j<sizeX; j++)
                    {
                        double temp[] = multiply(values[i][j], argValues.getValueComplex(j, k));
                        results[i][k][REAL]      += temp[REAL];
                        results[i][k][IMAGINARY] += temp[IMAGINARY];
                    }
                }
            }

            return new DoubleNumberToken(results);
        }
        else
        {
            /* dimensions do not match */
            ErrorLogger.debugLine("DoubleNumberToken: multiply: dimensions don't match");
            return null;
        }
    } // end multiply

    /**Multiplies two complex numbers
       @param arg1 = the first complex number as an array of double
       @param arg2 = the second complex number as an array of double
       @return the result as an array of double*/
    public double[] multiply(double[] arg1, double[]arg2) 
    {
        double[] temp     = new double[2];
        temp[REAL]        = (arg1[REAL] * arg2[REAL]) - (arg1[IMAGINARY] * arg2[IMAGINARY]);
        temp[IMAGINARY]   = (arg1[REAL] * arg2[IMAGINARY]) + (arg1[IMAGINARY] * arg2[REAL]);
        return  temp;
    }

    /**divide this object by arg for a number token
    @param arg = the value to divide it by
    @return the result as an OperandToken*/
    public OperandToken divide(OperandToken arg)
    {		
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: divide: no number");

        DoubleNumberToken nArg = (DoubleNumberToken)arg;
        int        argSizeX 	= nArg.getSizeX();	
        int        argSizeY 	= nArg.getSizeY(); 

        // Check if arg is a scalar 
        if((argSizeX == 1) && (argSizeY == 1))
        {
            DoubleNumberToken result = new DoubleNumberToken(values); //this);

            //  Divide (n*m) = (n*m) / scalar       
            double[] argValue = nArg.getValueComplex(0, 0);
            ErrorLogger.debugLine("DoubleNumberToken: divide (n*m) / scalar");
            for (int yy=0; yy<sizeY; yy++) 
            {
                for (int xx=0; xx<sizeX; xx++)
                {
                    result.setValueComplex(yy, xx, divide(values[yy][xx], argValue));
                }
            }
            return result;   	
        } 
        else if((sizeX == 1) && (sizeY == 1))
        {
            // the DoubleNumberToken of this class is a scalar 
            //  Divide (n*m) = scalar / (n*m) 
            ErrorLogger.debugLine("DoubleNumberToken: divide scalar / (n*m) ");
            DoubleNumberToken result = new DoubleNumberToken(1);
            double[] value = values[0][0];
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
            ErrorLogger.debugLine("DoubleNumberToken: divide: dimensions don't match");
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
        /* {REAL,IMAGINARY} = divide( {REAL,IMAGINARY}, {REAL,IMAGINARY}) */
        double x = arg2[REAL];
        double y = arg2[IMAGINARY];
        
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
            if (Double.isNaN(arg1[IMAGINARY]))
                zIm = Double.NaN;
            else if (arg1[IMAGINARY]>0)
            	zIm = Double.POSITIVE_INFINITY; 
            else if (arg1[IMAGINARY]<0)
            	zIm = Double.NEGATIVE_INFINITY;
            else
            	zIm = 0.0; 
        }
        else if(Math.abs(x) >= Math.abs(y)) 
        {
            scalar =  1.0 / ( x + y*(y/x) );

            zRe =  scalar * (arg1[REAL] + arg1[IMAGINARY]*(y/x));
            zIm =  scalar * (arg1[IMAGINARY] - arg1[REAL]*(y/x));

        } 
        else 
        {
            scalar =  1.0 / ( x*(x/y) + y );

            zRe =  scalar * (arg1[REAL]*(x/y) + arg1[IMAGINARY]);
            zIm =  scalar * (arg1[IMAGINARY]*(x/y) - arg1[REAL]);
        }

        temp[REAL]      = zRe;
        temp[IMAGINARY] = zIm;
        
        return temp;
    }
    
    //////////////////////////////////////////SCALAR OPERATORS//////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**scalar multiply arg by this object for a number token
    @arg = the value to multiply it by
    @return the result as an OperandToken*/
    public OperandToken scalarMultiply(OperandToken arg)
    {
    	if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: scalar multiply: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
	    double[][] argValues 	= nArg.getValuesIm(); 
	    double[][] argValuesImg = nArg.getValuesIm(); 
	    int        argSizeX 	= nArg.getSizeX();	
	    int        argSizeY 	= nArg.getSizeY(); 

	    if ((sizeX == argSizeX) && (sizeY == argSizeY))
	    {
    		//  scalar multiplication (n*m) = (n*m) .* (n*m) 
    		ErrorLogger.debugLine("DoubleNumberToken: multiply (n*m) .* (n*m)");
    		double[][][] results = new double[sizeY][sizeX][2];
                    
    		for (int y=0; y<sizeY; y++)
    		{
    		    for (int x=0; x<sizeX; x++)
    		    {
    		        double[] argVal = nArg.getValueComplex(y, x);
                    results[y][x] = multiply(values[y][x], argVal);                       
    		    }
    		}
    		return new DoubleNumberToken(results);   	
	    }
        else if ((argSizeX==1) && (argSizeY==1))
        {
            //  scalar multiplication (n*m) .* (1*1) 
            ErrorLogger.debugLine("DoubleNumberToken: multiply (n*m) .* (1*1)");
            double[][][] results = new double[sizeY][sizeX][2];
                    
            for (int y=0; y<sizeY; y++)
            {
                for (int x=0; x<sizeX; x++)
                {
                    double[] argVal = nArg.getValueComplex(0, 0);
                    results[y][x]   = multiply(values[y][x], argVal);                       
                }
            }
            return new DoubleNumberToken(results);    
        }
        else if ((sizeX == 1) && (sizeY == 1))
        {
            //  scalar multiplication (1*1) .* (n*m) 
            ErrorLogger.debugLine("DoubleNumberToken: multiply (1*1) .* (n*m)");
            double[][][] results = new double[argSizeY][argSizeX][2];
                    
            for (int y=0; y<argSizeY; y++)
            {
                for (int x=0; x<argSizeX; x++)
                {
                    double[] val  = getValueComplex(0, 0);
                    results[y][x] = multiply(val, nArg.getValueComplex(y,x));                       
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

    } // end scalarMultiply

    /**scalar divide arg by this object for a number token
    @arg = the value to divide it by
    @return the result as an OperandToken*/
    public OperandToken scalarDivide(OperandToken arg)
    {
			
    	if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: scalar divide: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
	    double[][] argValues 	= nArg.getValuesRe(); 
	    double[][] argValuesImg = nArg.getValuesIm(); 
	    int        argSizeX 	= nArg.getSizeX();	
	    int        argSizeY 	= nArg.getSizeY(); 

	    if ((sizeX == argSizeX) && (sizeY == argSizeY))
	    {
    		//  divide multiplication (n*m) = (n*m) .* (n*m) 
    		ErrorLogger.debugLine("DoubleNumberToken: scalar divide (n*m) .* (n*m)");
    		double[][][] results = new double[sizeY][sizeX][2];
                    
    		for (int y=0; y<sizeY; y++)
    		{
    		    for (int x=0; x<sizeX; x++)
    		    {
    		        double[] argVal = nArg.getValueComplex(y, x);
                    results[y][x] = divide(values[y][x], argVal);
    		    }
    		}
    		return new DoubleNumberToken(results);   	
	    }
        else if ((argSizeX==1) && (argSizeY==1))
        {
            //  divide multiplication (n*m) ./ (1,1) 
            ErrorLogger.debugLine("DoubleNumberToken: scalar divide (n*m) ./ (1*1)");
            double[][][] results = new double[sizeY][sizeX][2];
                    
            for (int y=0; y<sizeY; y++)
            {
                for (int x=0; x<sizeX; x++)
                {
                    double[] argVal = nArg.getValueComplex(0, 0);
                    results[y][x] = divide(values[y][x], argVal);
                }
            }
            return new DoubleNumberToken(results);    
        }
        else if ((sizeX==1) && (sizeY==1))
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

        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: left divide: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
        DoubleNumberToken num = new DoubleNumberToken(nArg.getReValues());
        
        return num.divide(new DoubleNumberToken(values));
        
    } // end leftDivide

    /**scalar left divide 
    @arg = 
    @return the result as an OperandToken*/
    public OperandToken scalarLeftDivide(OperandToken arg)
    {
        if(!(arg instanceof DoubleNumberToken))
            Errors.throwMathLibException("DoubleNumberToken: scalar left divide: no number");

        DoubleNumberToken nArg = ((DoubleNumberToken)arg);
        DoubleNumberToken num = new DoubleNumberToken(nArg.getReValues());
        
        return num.scalarDivide(new DoubleNumberToken(values));
        
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
        		// copy (y,x) -> (x,y), also change sign of imaginary part
        		real[x][y] = values[y][x][REAL];
        		imag[x][y] = values[y][x][IMAGINARY] * (-1);
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
                // copy (y,x) -> (x,y), for conjugate transpose do not
                //   change sign of imaginary part
                real[x][y] = values[y][x][REAL];
                imag[x][y] = values[y][x][IMAGINARY] ;
            }
        }
        return new DoubleNumberToken(real, imag);     
    }

     ///////////////////////////////////////Trigonometric functions/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**trigonometric functions - calculate the sine of this token
    @return the result as an OperandToken*/
    public OperandToken sin()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = sin(values[y][x]);                
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the sine of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] sin(double[] arg)
    {
        double result[] = new double[2];
        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;

        // iz:      i.Times(z) ...
        iz_re =  -arg[IMAGINARY];
        iz_im =   arg[REAL];

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _1:      _1.Minus(_2) ...
        _re1 = _re1 - _re2;                                                // !!!
        _im1 = _im1 - _im2;                                                // !!!

        // result:  _1.Div(2*i) ...
        result[REAL] = 0.5*_im1;
        result[IMAGINARY] = -0.5*_re1;
        
        return result;
    }

    /**trigonometric functions - calculate the cosine of this token
    @return the result as an OperandToken*/
    public OperandToken cos()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = cos(values[y][x]);                
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the cosine of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] cos(double[] arg)
    {
        double result[] = new double[2];
        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;

        // iz:      i.Times(z) ...
        iz_re =  -arg[IMAGINARY];
        iz_im =   arg[REAL];

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _1:      _1.Plus(_2) ...
        _re1 = _re1 + _re2;                                                // !!!
        _im1 = _im1 + _im2;                                                // !!!

        // result:  _1.scale(0.5) ...
        result[REAL] = 0.5*_re1;
        result[IMAGINARY] = -0.5*_im1;
        
        return result;
    }

    /**trigonometric functions - calculate the tangent of this token
    @return the result as an OperandToken*/
    public OperandToken tan()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = tan(values[y][x]);                
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the tangent of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] tan(double[] arg)
    {
        double[] temp1 = new double[2];
        temp1[REAL] = arg[REAL];
        temp1[IMAGINARY] = arg[IMAGINARY];
        temp1 = sin(temp1);
        double[] temp2 = cos(arg);
        return divide(temp1, temp2);
    }

    /**trigonometric functions - calculate the arc sine of this token
    @return the result as an OperandToken*/
    public OperandToken asin()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = asin(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the arcsine of a complex number
       @param arg = the value as an array of double
       @return the result as an array of double*/ 
    public double[] asin(double[] arg)
    {
        double result[] = new double[2];
        //  asin(z)  =  -i * log(i*z + Sqrt(1 - z*z))
        double re =  arg[REAL];
        double im =  arg[IMAGINARY];
        
        // _1:      one.Minus(z.Times(z)) ...
        result[REAL]       =  1.0 - ( (re*re) - (im*im) );
        result[IMAGINARY]  =  0.0 - ( (re*im) + (im*re) );

        // result:  _1.Sqrt() ...
        result = sqrt(result);

        // _1:      z.Times(i) ...
        // result:  _1.Plus(result) ...
        result[REAL]       =   result[REAL] - im;
        result[IMAGINARY]  = result[IMAGINARY] +  re;

        // _1:      result.log() ...
        result = log(result);

        double temp     = result[IMAGINARY];
        result[IMAGINARY]  =  -result[REAL];
        result[REAL]       =  temp;

        return result;
    }
    
    /**trigonometric functions - calculate the arc cosine of this token
    @return the result as an OperandToken*/
    public OperandToken acos()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = acos(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the arccosine of a complex number
       @param arg = the value as an array of double
       @return the result as an array of double*/ 
    public double[] acos(double[] arg)
    {
        double result[] = new double[2];
        double _re1, _im1;

        double re =  arg[REAL];
        double im =  arg[IMAGINARY];
               
        // _1:      one - z^2 ...
        result[REAL]       =  1.0 - ( (re*re) - (im*im) );
        result[IMAGINARY]  =  0.0 - ( (re*im) + (im*re) );

        // result:  _1.Sqrt() ...
        result = sqrt(result);

        // _1:      i * result ...
        _re1 =  - result[IMAGINARY];
        _im1 =  + result[REAL];

        // result:  z +_1  ...
        result[REAL]       =  re + _re1;
        result[IMAGINARY]  =  im + _im1;

        // _1:      result.log()
        result = log(result);

        // result:  -i * _1 ...
        double temp = result[IMAGINARY];
        result[IMAGINARY] = -result[REAL];
        result[REAL] = temp;
        
        return result;
    }
    
    /**trigonometric functions - calculate the arc tangent of this token
    @return the result as an OperandToken*/
    public OperandToken atan()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = atan(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the arctangent of a complex number
       @param arg = the value as an array of double
       @return the result as an array of double*/ 
    public double[] atan(double[] arg)
    {
        double result[] = new double[2];
        double[] temp = new double[2];
        //  atan(z)  =  -i/2 * log( (i-z)/(i+z) )

        double _re1, _im1;

        // result:  i.Minus(z) ...
        temp[REAL] = -arg[REAL];
        temp[IMAGINARY] = 1 - arg[IMAGINARY];

        // _1:      i.Plus(z) ...
        result[REAL] = arg[REAL];
        result[IMAGINARY] = 1 + arg[IMAGINARY];

        // result:  result.Div(_1) ...
        result = divide(temp, result);

        // _1:      result.log() ...
        result = log(result);

        // result:  half_i.neg().Times(_2) ...
        double t = -0.5 * result[REAL];
        result[REAL] =   0.5 * result[IMAGINARY];
        result[IMAGINARY] =  t;
        return  result;        
    }
	
    /**Trigonometric function - calculates the hyperbolic sine
       @return the result as an OperandToken*/   
    public OperandToken sinh()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = sinh(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the hyperbolic sine of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] sinh(double[] arg)
    {
        double result[] = new double[2];
        double scalar;
        double _re1, _im1;
        double _re2, _im2;

        // _1:      z.exp() ...
        scalar =  Math.exp(arg[REAL]);
        _re1 =  scalar * Math.cos(arg[IMAGINARY]);
        _im1 =  scalar * Math.sin(arg[IMAGINARY]);

        // _2:      z.neg().exp() ...
        scalar =  Math.exp(-arg[REAL]);
        _re2 =  scalar * Math.cos(-arg[IMAGINARY]);
        _im2 =  scalar * Math.sin(-arg[IMAGINARY]);

        // _1:      _1.Minus(_2) ...
        _re1 = _re1 - _re2;                        // !!!
        _im1 = _im1 - _im2;                        // !!!

        // result:  _1.scale(0.5) ...
        result[REAL] = 0.5 * _re1;
        result[IMAGINARY] = 0.5 * _im1;

        return result;
    }    

    /**Trigonometric function - calculates the hyperbolic cosine
       @return the result as an OperandToken*/   
    public OperandToken cosh()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = cosh(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
	}

    /**Calculates the hyperbolic cosine of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] cosh(double[] arg)
    {
        double result[] = new double[2];
        double scalar;
        double _re1, _im1;
        double _re2, _im2;

        // _1:      z.exp() ...
        scalar =  Math.exp(arg[REAL]);
        _re1 =  scalar * Math.cos(arg[IMAGINARY]);
        _im1 =  scalar * Math.sin(arg[IMAGINARY]);

        // _2:      z.neg().exp() ...
        scalar =  Math.exp(-arg[REAL]);
        _re2 =  scalar * Math.cos(-arg[IMAGINARY]);
        _im2 =  scalar * Math.sin(-arg[IMAGINARY]);

        // _1:  _1.Plus(_2) ...
        _re1 = _re1 + _re2;                    // !!!
        _im1 = _im1 + _im2;                    // !!!

        // result:  _1.scale(0.5) ...
        result[REAL] = 0.5 * _re1;
        result[IMAGINARY] = 0.5 * _im1;
        
        return result;
    }
    
    /**Trigonometric function - calculates the hyperbolic tan
       @return the result as an OperandToken*/   
    public OperandToken tanh()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = tanh(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
	}
		
    /**Calculates the hyperbolic tangent of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] tanh(double[] arg)
    {
        double[] temp1 = new double[2];
        temp1[REAL] = arg[REAL];
        temp1[IMAGINARY] = arg[IMAGINARY];
        
        temp1 = sinh(temp1);
        double[] temp2 = cosh(arg);
        
        return divide(temp1, temp2);
    }

    /**Trigonometric function - calculates the inverse hyperbolic sine
       @return the result as an OperandToken*/   
    public OperandToken asinh()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = asinh(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }
	
    /**Calculates the inverse hyperbolic sine of a complex number
       @param arg = the value as an array of double
       @return the result as an array of double*/ 
    public double[] asinh(double[] arg)
    {
        double result[] = new double[2];
        //  asinh(z)  =  log(z + Sqrt(z*z + 1))
        double re = arg[REAL];
        double im = arg[IMAGINARY];
        // _1:      z.Times(z).Plus(one) ...
        result[REAL] =  ( (re*re) - (im*im) ) + 1.0;
        result[IMAGINARY] =  ( (re*im) + (im*re) );

        // result:  _1.Sqrt() ...
        result = sqrt(result);

        // result:  z.Plus(result) ...
        result[REAL] =  re + result[REAL];                                       // !
        result[IMAGINARY] =  im + result[IMAGINARY];                                       // !

        // _1:      result.log() ...
        result = log(result);
                
        return  result;
    }
    
    /**Trigonometric function - calculates the inverse hyperbolic cosine
       @return the result as an OperandToken*/   
    public OperandToken acosh()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = acosh(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the inverse hyperbolic cosine of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] acosh(double[] arg)
    {
        double result[] = new double[2];
        //  acosh(z)  =  log(z + Sqrt(z*z - 1))
        double re = arg[REAL];
        double im = arg[IMAGINARY];

        // _1:  z.Times(z).Minus(one) ...
        result[REAL]       =  ( (re*re) - (im*im) ) - 1.0;
        result[IMAGINARY]  =  ( (re*im) + (im*re) ) - 0.0;

        // result:  _1.Sqrt() ...
        result = sqrt(result);

        // result:  z.Plus(result) ...
        result[REAL] =  re + result[REAL];                                       // !
        result[IMAGINARY] =  im + result[IMAGINARY];                                       // !

        // _1:  result.log() ...
        result = log(result);

        // result:  _1 ...
        return  result;
    }

    /**Trigonometric function - calculates the inverse hyperbolic tangent
       @return the result as an OperandToken*/   
    public OperandToken atanh()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = atanh(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the inverse hyperbolic tangent  of a complex number
       @param arg = the angle as an array of double
       @return the result as an array of double*/ 
    public double[] atanh(double[] arg)
    {
        double result[] = new double[2];
        //  atanh(z)  =  1/2 * log( (1+z)/(1-z) )

        // _1:      one.Minus(z) ...
        double[] temp = new double[2];
        temp[REAL]      = 1 - arg[REAL];
        temp[IMAGINARY] = - arg[IMAGINARY];

        // result:  one.Plus(z) ...
        result[REAL] = 1 + arg[REAL];

        // result:  result.Div(_1) ...
        result = divide(result, temp);

        // _1:      result.log() ...
        result = log(result);

        // result:  _1.scale(0.5) ...
        result[REAL]       =  0.5 * result[REAL];
        result[IMAGINARY]  =  0.5 * result[IMAGINARY];
        return result;  
    }

    ///////////////////////////////////////Exponential Functions////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**Standard functions - calculates the exponent
       @return the result as an OperandToken*/
    public OperandToken exp()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = exp(values[y][x]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the exponent of a complex number
       @param arg = the value as an array of double
       @return the result as an array of double*/ 
    public double[] exp(double[] arg)
    {
        double[] result = new double[2];
        double scalar =  Math.exp(arg[REAL]);                                         // e^ix = cis x
        result[REAL]       = scalar * Math.cos(arg[IMAGINARY]);
        result[IMAGINARY]  = scalar * Math.sin(arg[IMAGINARY]);
        return  result;
    }
    
    /**Standard functions - calculates the natural logarythm
       @return the result as an OperandToken*/
    public OperandToken ln()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = log(values[y][x]);                
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Standard functions - calculates the logarythm
       @return the result as an OperandToken*/
    public OperandToken log()
    {        
        double[][][] results = new double[sizeY][sizeX][2];
        
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = log(values[y][x]);               
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Calculates the logarythm of a complex number
       @param arg = the value as an array of double
       @return the result as an array of double*/ 
    public double[] log(double[] arg) 
    {
        double[] result = new double[2];
        double re = arg[REAL];
        double im = arg[IMAGINARY];

        double temp = Math.pow(re, 2) + Math.pow(im, 2);
        temp =  Math.sqrt(temp);
        
        result[REAL]      = Math.log(temp);
        result[IMAGINARY] = Math.atan2(im, re);
        return  result;                      // principal value
    }

    /**Standard functions - calculates the square root    
       @return the result as an OperandToken*/
    public OperandToken sqrt()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x] = sqrt(values[y][x]);                
            }
        }
        return new DoubleNumberToken(results);   	
    }
    
    /**Calculates the sqrt of a complex number
       @param arg = the value as an array of double
       @return the result as an array of double*/ 
    public double[] sqrt(double[] arg) 
    {
        // with thanks to Jim Shapiro <jnshapi@argo.ecte.uswc.uswest.com>
        // adapted from "Numerical Recipies in C" (ISBN 0-521-43108-5)
        // by William H. Press et al

        double[] result = new double[2];
        double re = arg[REAL];
        double im = arg[IMAGINARY];

        double temp = Math.pow(re, 2) + Math.pow(im, 2);
        double mag =  Math.sqrt(temp);

        if (mag > 0.0) 
        {
            if (re > 0.0) 
            {
                temp =  Math.sqrt(0.5 * (mag + re));

                re =  temp;
                im =  0.5 * im / temp;
            } 
            else 
            {
                temp =  Math.sqrt(0.5 * (mag - re));

                if (im < 0.0) 
                {
                    temp =  -temp;
                }//endif

                re =  0.5 * im / temp;
                im =  temp;
            }//endif
        } 
        else 
        {
            re =  0.0;
            im =  0.0;
        }
        result[REAL] = re;
        result[IMAGINARY] = im;
        
        return result;
    }

    ///////////////////////////////////////Standard Math Functions///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**Standard functions - absolute value  
    @return the result as an OperandToken*/
    public OperandToken abs()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        if (isReal())
        {
            // real numbers only
	        for (int yy=0; yy<sizeY; yy++)
	        {
	            for (int xx=0; xx<sizeX; xx++)
	            {
	                results[yy][xx][REAL]  = java.lang.Math.abs(values[yy][xx][REAL]);
	            }
	        }
        }
        else
        {
        	// complex absolute value
    	    double real = 0;
            double imag = 0;
            for (int yy=0; yy<sizeY; yy++)
            {
                for (int xx=0; xx<sizeX; xx++)
                {
                    real = 	values[yy][xx][REAL];
                    imag = 	values[yy][xx][IMAGINARY];
                    results[yy][xx][REAL]      = Math.sqrt( real*real + imag*imag );
                    results[yy][xx][IMAGINARY] = 0;
                }
            }
        }

        return new DoubleNumberToken(results);   	
    }

    /**Standard functions - rounds the value down  
       @return the result as an OperandToken*/
    public OperandToken floor()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for (int yy=0; yy<sizeY; yy++)
        {
            for (int xx=0; xx<sizeX; xx++)
            {
                results[yy][xx][REAL]      = java.lang.Math.floor(values[yy][xx][REAL]);
                results[yy][xx][IMAGINARY] = java.lang.Math.floor(values[yy][xx][IMAGINARY]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Standard functions - rounds the value up   
       @return the result as an OperandToken*/
    public OperandToken ceil()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for (int yy=0; yy<sizeY; yy++)
        {
            for (int xx=0; xx<sizeX; xx++)
            {
                results[yy][xx][REAL]      = Math.ceil(values[yy][xx][REAL]);
                results[yy][xx][IMAGINARY] = Math.ceil(values[yy][xx][IMAGINARY]);
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Standard functions - rounds the value to the nearest integer 
       @return the result as an OperandToken*/
    public OperandToken round()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for (int yy=0; yy<sizeY; yy++)
        {
            for (int xx=0; xx<sizeX; xx++)
            {
                results[yy][xx][REAL]      = java.lang.Math.rint(values[yy][xx][REAL]);
                results[yy][xx][IMAGINARY] = java.lang.Math.rint(values[yy][xx][IMAGINARY]);
            }
        }
        return new DoubleNumberToken(results);   	
    }
    
    /**standard function - returns the negative of the number*/
    public OperandToken negate()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for (int yy=0; yy<sizeY; yy++)
        {
            for (int xx=0; xx<sizeX; xx++)
            {
                results[yy][xx][REAL]      = -values[yy][xx][REAL];
                results[yy][xx][IMAGINARY] = -values[yy][xx][IMAGINARY];
            }
        }
        return new DoubleNumberToken(results);   	
    }

    /**Standard functions - calculates the minimum of two values
       @return the result as an OperandToken*/
    public OperandToken min(RootObject arg)
    {
		// !!! see matlab documentation for correct implementation		

        double arg2 = ((DoubleNumberToken)arg).getValueRe(0, 0);       
        double result = java.lang.Math.min(values[0][0][REAL], arg2);
        return new DoubleNumberToken(result);   	
    }

    /**Standard functions - calculates the maximum of two values
       @return the result as an OperandToken*/
    public OperandToken max(RootObject arg)
    {
		// !!! see matlab documentation for correct implementation		

        double arg2 = ((DoubleNumberToken)arg).getValueRe(0, 0);       
        double result = java.lang.Math.max(values[0][0][REAL], arg2);
        return new DoubleNumberToken(result);   	
    }

    /**Standard functions - calculates the factorial of the number
       @return the result as an OperandToken*/
    public OperandToken factorial()
    {
        double[][][] results = new double[sizeY][sizeX][2];
        for (int yy=0; yy<sizeY; yy++)
        {
            for (int xx=0; xx<sizeX; xx++)
            {
                double amount = java.lang.Math.rint(values[yy][xx][REAL]);
                results[yy][xx][REAL] = factorial(amount);               
            }
        }
        return new DoubleNumberToken(results);   	
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
        double[][][] results = new double[sizeY][sizeX][2];
        for(int y = 0; y < sizeY; y++)
        {
            for(int x = 0; x < sizeX; x++)
            {
                results[y][x][REAL]      = values[y][x][REAL];
                results[y][x][IMAGINARY] = -values[y][x][IMAGINARY];
            }
        }
        return new DoubleNumberToken(results);   	
    }

    //////////////////////////////////////Test Functions///////////////////////////////////////  
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    /**Checks if this operand is zero
    @return true if this number == 0 or that all values are
    0 for a matrix*/
    public boolean isNull()
    {
    	boolean result = true;
    	for (int yy=0; yy<sizeY && result; yy++)
    	{
    	    for (int xx=0; xx<sizeX && result; xx++)
    	    {
    	        if(values[yy][xx][REAL] != 0 || values[yy][xx][IMAGINARY] != 0)
    	            result = false;
    	    }
    	}
    	return result;
    }   

    /**Checks if this operand is a numeric value
       @return true if this is a number, false if it's 
       an algebraic expression*/
    public boolean isNumeric()
    {
    	return true;
    }
    
    /**@return true if this number token is a scalar (1*1 matrix)*/
    public boolean isScalar()
    {
    	if ((sizeX==1) && (sizeY==1))
        	return true;
        else
        	return false;
    }
       
    /**@return true if this number token is a real matrix, without an imaginary part*/
    public boolean isReal()
    {
    	// if at least one element has an imaginary part the matrix is not real 
    	for (int y=0; y<sizeY; y++)
        {
    	    for (int x=0; x<sizeX; x++)
            {
            	if (values[y][x][IMAGINARY] != 0)
                	return false;
            }
        }
        return true;
    }

    /**@return true if this number token is an integral matrix, without an imaginary part*/
    public boolean isInteger()
    {
    	// if at least one element has an imaginary part the matrix is not real, or is not integral
    	for (int y=0; y<sizeY; y++)
        {
    	    for (int x=0; x<sizeX; x++)
            {
            	if ((values[y][x][IMAGINARY] != 0 ) || 
                    (values[y][x][REAL] != java.lang.Math.floor(values[y][x][REAL])))
            	    return false;
            }
        }
        return true;
    }

    /**@return true if this number token is an imaginary matrix, without a real part*/
    public boolean isImaginary()
    {
    	// if at least one element has a real part the matrix is not imaginary 
        for (int y=0; y<sizeY; y++)
        {
            for (int x=0; x<sizeX; x++)
            {
                if (values[y][x][REAL] != 0)
                    return false;
            }
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
        
        for (int y=0; y<sizeY; y++)
        {
            for (int x=0; x<sizeX; x++)
            {
            	if (values[y][x][REAL] != 0)
                	isRe = true;
            	if (values[y][x][IMAGINARY] != 0)
                	isIm = true;
            }
        }
        
        // check if both real and imaginary tags are set
        if (isRe & isIm)
            return true;
        else
            return false;
    }

    public OperandToken degreesToRadians()
    {
        //convert from degrees to radians	    
        double[][] values = getReValues();
			
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                values[yy][xx] = values[yy][xx] * Math.PI / 180;
            }				
        }

        return new DoubleNumberToken(values);
    }

    public OperandToken radiansToDegrees()
    {
        //convert from degrees to radians	    
        double[][] values = getReValues();
			
        for(int yy = 0; yy < sizeY; yy++)
        {
            for(int xx = 0; xx < sizeX; xx++)
            {
                values[yy][xx] = values[yy][xx] * 180 / Math.PI;
            }				
        }

        return new DoubleNumberToken(values);
    }
}

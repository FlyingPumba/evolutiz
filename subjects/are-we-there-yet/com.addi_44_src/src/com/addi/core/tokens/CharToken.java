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
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**Class used to represent any strings used in an expression*/
public class CharToken extends DataToken
{
    /**The value of the string*/
    private String[] values;
    
    /**Creates an empty char array
     */
     public CharToken()
     {
         super(99, "char"); 
         sizeY     = 0;
         sizeX     = 0;
         sizeA     = new int[]{0, 0};
         noElem    = 0;
         values    = null;
     }

    /**Creates a string with a value of _value
     * @param _value = the value of the string
     */
    public CharToken(String _value)
    {
        super(99, "char"); 
        values    = new String[1];
        values[0] = _value;
        sizeY     = 1;
        sizeX     = values[0].length();
        sizeA     = new int[]{sizeY,sizeX};
        noElem    = sizeY * sizeX;
    }
    
    /**Creates a string with a value of _value
     * @param _value = the value of the string
     */
    public CharToken(String[] _values)
    {
        super(99, "char"); 
        sizeY     = _values.length;
        sizeX     = _values[0].length();
        sizeA     = new int[]{sizeY,sizeX};
        noElem    = sizeY * sizeX;
        
        values    = new String[sizeY];
        for (int i=0;i<sizeY;i++) {
          if (_values[i].length() > sizeX) {
        	  sizeX     = _values[i].length();
        	  sizeA     = new int[]{sizeY,sizeX};
        	  noElem    = sizeY * sizeX;
          }
          values[i] = _values[i];
        }
    }
    

    /**Creates a string with a value of _value
     * @param _value = the value of the string
     */
     public CharToken(char[][] _values)
     {
         super(99, "char"); 
         values = new String[_values.length];
         sizeY  = values.length;
         sizeX  = _values[0].length;
         sizeA  = new int[]{sizeY,sizeX};
         noElem = sizeY * sizeX;
         for (int i=0;i<sizeY;i++) {
             if (_values[i].length > sizeX) {
           	  sizeX     = _values[i].length;
           	  sizeA     = new int[]{sizeY,sizeX};
           	  noElem    = sizeY * sizeX;
             }
             values[i] = new String(_values[i]);
         }
     }

     /**Creates a string with a value of _value
      * @param _value = the value of the string
      */
      public CharToken(char _value)
      {
          super(99, "char"); 
          values       = new String[1];
          values[0]    = Character.toString(_value);
          sizeY        = 1;
          sizeX        = 1;
          sizeA        = new int[]{sizeY,sizeX};
          noElem       = sizeY * sizeX;
      }

    /**Evaluates the token, just returns the token itself
     * @param operands = the tokens operands (not used)
     * @param
     * @return the token itself as an OperandToken
     */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        return this;
    }

    /**
     * @return the string value
     */    
    /**return the number as a string
     * @return
     */
    public String toString()
    {
    	if (sizeY == 1) {
    		return getElementString(0);
    	}
        String result = null;
        if((sizeY == 0) && (sizeX == 0))
        {
            result = "[]";
        }
        else if((sizeY == 1) && (sizeX == 1) && sizeA.length==2)
        {
            result = values[0];
        }
        else if (sizeA.length ==2)
        {
            result = toString2d(new int[]{sizeY,sizeX});
        }
        else
        {
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

            for (int n=0; n<sizeA[i]; n++)
            {
                dim[i]=n;
                
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
        
        for(int yy = 0; yy < sizeY; yy++)
        {
            buffer.append("\n   ");
            buffer.append(values[yy]);         
        }
        
        buffer.append("\n");
        
        return buffer.toString();
    }

    /**
     * @return the value of the string
     */
    public String getValue()
    {
        return new String(values[0]);
    }

    /**
     * @return the value of the string
     */
    public char getCharValue()
    {
        return values[0].charAt(0);
    }

    /**
     * cast all char-values into double-array
     * @return
     */
    public double[][] getValuesRe()
    {
        // in case char-array is empty return null
        if ((sizeY==0) && (sizeX==0))
            return null;        

        // create empty return array
        double[][] d = new double[sizeY][sizeX];

        // convert array of byte to array of double
        for (int y=0; y<sizeY; y++)
        {
            for (int x=0; x<sizeX; x++)
            {   
            	if (x >= values[y].length()) {
            		d[y][x]= (double)' ';
            	} else {
                   d[y][x]= (double)values[y].charAt(x);
            	}
            }
        } 

        return d;
    }
    
    /**
     * @param
     * @param
     */
    public OperandToken getElement(int y, int x)
    {
    	if (x >= values[y].length()) {
    	   return new CharToken(' ');
    	} else {
           return new CharToken(values[y].charAt(x));
    	}
    }
    
    /**
     * @param
     * @param
     */
    public String getElementString(int y)
    {
    	return values[y];
    }

    /**
     * @param
     * @param
     * @param
     */
    public void setElement(int y, int x, OperandToken op)
    {
        char c = ((CharToken)op).getCharValue();
        
        ErrorLogger.debugLine("CharToken("+y+","+x+")"+ c);
        String tempString = values[y];
        values[y] = "";
        if (x > 0) {
           values[y] += values[y].substring(0, x-1);
        }
        values[y] += c;
        if (x < (values[y].length()-1)) {
        	values[y] += values[y].substring(x+1, values[y].length()-1);
        }
    }
    
    /**
     * @param
     * @param
     * @return
     */
    public DataToken getElementSized(int y, int x)
    {
        return new CharToken(new char[y][x]); 
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
    
        // swap rows and columns - string arrays only have rows
    	// if there are multiple rows, will become just 1 element
        String returnString = values[0];
        for (int y=1; y<sizeY; y++)
        {
        	returnString += values[y];
        }
        return new CharToken(returnString);     
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

    
    /**
     * conversion into a number token
     * @return
     */
    public DoubleNumberToken getDoubleNumberToken()
    {
    	double[][] d = getValuesRe();
        
        return new DoubleNumberToken(d);
    }
    
  }

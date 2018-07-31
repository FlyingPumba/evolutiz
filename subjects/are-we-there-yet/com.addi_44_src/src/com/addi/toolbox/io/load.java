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

package com.addi.toolbox.io;


import java.io.*;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.ErrorLogger;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**An external function for loading a matrix from a csv file*/
public class load extends ExternalFunction
{
 
    
 //!!!! make a functions for dataSTream.read(xxx) and swap endian   
 //   if ( dataStream.read(b,0,4) != 4)
 //       throwMathLibException("load: could not read next tag");
 //   swap(b, 4, swapEndian);
    
    // MAT-file data types
    static final byte miINT8       =  1;
    static final byte miUINT8      =  2;
    static final byte miINT16      =  3;
    static final byte miUINT16     =  4;
    static final byte miINT32      =  5;
    static final byte miUINT32     =  6;
    static final byte miSINGLE     =  7;
    // reserved
    static final byte miDOUBLE     =  9;
    // reserved
    // reserved
    static final byte miINI64      = 12;
    static final byte miUINT64     = 13;
    static final byte miMATRIX     = 14;
    static final byte miCOMPRESSED = 15;
    static final byte miUTF8       = 16;
    static final byte miUTF16      = 17;
    static final byte miUTF32      = 18;
    
    // length of each data type from above
    static final byte[] miLength   = { 1, 1, 2, 2, 4,
                                       4 };  // one entry of each data type
     
    // array types
    static final byte mxCELL       =  1;
    static final byte mxSTRUCT     =  2;
    static final byte mxOBJECT     =  3;
    static final byte mxCHAR       =  4;
    static final byte mxSPARSE     =  5;
    static final byte mxDOUBLE     =  6;
    static final byte mxSINGLE     =  7;
    static final byte mxINT8       =  8;
    static final byte mxUINT8      =  9;
    static final byte mxINT16      = 10;
    static final byte mxUINT16     = 11;
    static final byte mxINT32      = 12;
    static final byte mxUINT32     = 13;
    
    
    // array to read stream of data from MAT-files
    byte[] b = new byte[1024];
    
    
	/** Check that the operand is a string then open the file                
	   referenced.                                                          
       @param operands[0] = string which specifies the csv file to load    
       @param operands[1] = the start row (optional)
       @param operands[2] = the start column (optional)
       @param operands[3] = range(optional, not implemented)
       @return the matrix as an OperandToken*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{
		OperandToken result = null;
        
        String fileName = "testvalues.mat";
        
        // at least one operand
        //if (getNArgIn(operands) > 0)
        //{
        //    fileName = ((StringToken)operands[0]).toString();
        //}
            
        System.out.println("load dir "+globals.getWorkingDirectory() );
        File fileHandle;
		if (fileName.startsWith("/")) {
			fileHandle = new File(fileName);
		} else {
			fileHandle = new File(globals.getWorkingDirectory(), fileName);
		}
	
		if(!fileHandle.exists()) 
            throwMathLibException("load file does not exist");
	
		ErrorLogger.debugLine("loading CSV>"+fileName+"<");
			
			

			try 
			{		
				// load file 
                //FileReader fileReader = new FileReader(fileHandle);
				FileInputStream fileStream = new FileInputStream(fileHandle);
                //BufferedReader inReader = new BufferedReader( );

                DataInputStream dataStream = new DataInputStream( fileStream );

                try 
				{		
                    
                    //// load MAT-file header (128 bytes)
                    // load descriptive header (116 bytes)
                    if ( dataStream.read(b,0,116) != 116)
                        throwMathLibException("load: length descriptive header != 116");

                    String descriptiveHeader = new String(b);
                    System.out.println("load :"+ descriptiveHeader );    
                    
                    // load subsys data offset
                    if ( dataStream.read(b,0,8) != 8)
                        throwMathLibException("load: length subsys data offset != 8");

                    // version
                    if ( dataStream.read(b,0,2) != 2)
                        throwMathLibException("load: length version != 2");
                    int version = ((int)b[1])<< 8 + ((int)b[0]); 
                    if (version != 0x0100)
                        throwMathLibException("load: version != 0x0100");
                        
                    // endian indicator
                    if ( dataStream.read(b,0,2) != 2)
                         throwMathLibException("load: length endian indicator != 2");
                    
                    // if endian indicator is "MI" now byte swapping
                    // if endian indicator is "IM" must swap bytes
                    boolean swapEndian = false;
                    if ((char)b[0] == 'M' && (char)b[1]== 'I')
                        System.out.println("load: endian ok");
                    else if ((char)b[0] == 'I' && (char)b[1]== 'M')
                    {
                        swapEndian = true;
                        System.out.println("load: endian: must swap bytes");
                    }
                    else
                        throwMathLibException("load: endian: error");

                    
                    ////// read data ///////
                    
                    // read next Tag
                    if ( dataStream.read(b,0,4) != 4)
                        throwMathLibException("load: could not read next tag");

                // check about small an big endian
                    swap(b, 4, swapEndian);
                    
                    // check if normal or small data element format
                    byte dataType = 0;
                    if (b[0]==0 && b[1]==0)
                    {
                        // normal data element format
                        System.out.println("load: normal data element format");
                        dataType = b[3];
                    }
                    else
                    {
                        // small data element format
                        System.out.println("load: small data element format");
                        dataType = b[0];
                    }
                    System.out.println("load: data type "+ dataType);
                    System.out.println("load: data type "+ b[2]);
                    System.out.println("load: data type "+ b[3]);
                    
                    // number of bytes
                    byte[] number = new byte[16];
                    if ( dataStream.read(number,0,4) != 4)
                        throwMathLibException("load: could not read data tag");
                    swap(number, 4, swapEndian);
                    
                    switch (dataType)
                    {
                        case miMATRIX:
                        {
                            int numberOfBytes = number[3];  // ?!? maybe all 4 bytes need treating
                            System.out.println("load: miMATRIX:  bytes "+numberOfBytes);
                            
                            // check dimensions
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read next tag");
                            swap(b, 4, swapEndian);
                            
                            // check data type of dimensions
                            if (b[0]!=0 || b[1]!=0 || b[2]!=0 || b[3]!=0x06)
                                throwMathLibException("load: miMATRIX: data type of dimension is not UINT32");

                            // read dimensions length
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read next tag");
                            swap(b, 4, swapEndian);

                            // check length of dimensions
                            if (b[0]!=0 || b[1]!=0 || b[2]!=0 || b[3]!=8)
                                throwMathLibException("load: miMATRIX: dimensions should be 8");

                            ////// read array flags /////
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read array flags");
                            swap(b, 4, swapEndian);

                            // check if complex bit is set
                            boolean complexFlag = false;
                            if ((b[3]& 0x08) != 0)  complexFlag = true;
                            System.out.println("load: miMATRIX: complex flag "+complexFlag);

                            // check if global bit is set
                            boolean globalFlag = false;
                            if ((b[3]& 0x04) != 0)  globalFlag = true;
                            System.out.println("load: miMATRIX: global flag "+globalFlag);

                            // check if logical bit is set
                            boolean logicalFlag = false;
                            if ((b[3]& 0x02) != 0)  logicalFlag = true;
                            System.out.println("load: miMATRIX: logical flag "+logicalFlag);

                            // read class of data of matrix
                            int arrayClassType = b[3];
                            System.out.println("load: miMATRIX: array class type "+ arrayClassType);
                            
                            // read unused array flags
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read array flags unused");
                            swap(b, 4, swapEndian);

                            /////// dimensions array ////////
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read type of dimensions array");
                            swap(b, 4, swapEndian);
                            if (b[0]!=0 || b[1]!=0 || b[2]!=0 || b[3]!=0x05)
                                throwMathLibException("load: miMATRIX: data type of dimensions array is not INT32");

                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read value of array dimensions");
                            swap(b, 4, swapEndian);
                            if (b[0]!=0 || b[1]!=0 || b[2]!=0 || b[3]!=8)
                                throwMathLibException("load: miMATRIX: data length of dimensions array is not 8");

                            
                            // read length first axis
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read length of first axis");
                            swap(b, 4, swapEndian);
                            int xLength = b[3];  // !?!maybe check all four bytes
                            System.out.println("load: miMATRIX x="+xLength);
                            
                            // read length second axis
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read length of second axis");
                            swap(b, 4, swapEndian);
                            int yLength = b[3];  // !?!maybe check all four bytes
                            System.out.println("load: miMATRIX y="+yLength);

                            
                            //////// read array name ////////
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read name of array");
                            swap(b, 4, swapEndian);
                            if (b[0]!=0 || b[1]!=0 || b[2]!=0 || b[3]!=1)
                                throwMathLibException("load: miMATRIX: data type of array name is not INT8");
                            
                            // length array name
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read length of name of array");
                            swap(b, 4, swapEndian);
                            int nameLength = b[3]; //?!?! maybe take all 4 bytes

                            byte[] nameB = new byte[nameLength];
                            if ( dataStream.read(nameB,0,nameLength) != nameLength)
                                throwMathLibException("load: could not read name of array");
                            String arrayName = new String(nameB);
                            System.out.println("load: miMATRIX array name: "+arrayName);

                            // read bytes up to the next 8 Byte border
                            int stuffLength = 8* ((nameLength/8) + 1) - nameLength ;
                            System.out.println("load: miMATRIX stufflength: "+stuffLength);
                            if ( dataStream.read(b,0,stuffLength) != stuffLength )
                                throwMathLibException("load: could not read stuff bytes");
                            
                            //////// read real data ////////
                            double[][] data = new double[yLength][xLength];
                            
                            // read type of data
                            if ( dataStream.read(b,0,4) != 4)
                                throwMathLibException("load: could not read type of data");
                            swap(b, 4, swapEndian);
                            int realDataType = b[3]; //?!?! maybe take all 4 bytes
                            int realDataLength = miLength[realDataType];
                            
                            for (int xi=0; xi<xLength; xi++)
                            {
                                for (int yi=0; yi<yLength; yi++)
                                {
                                    if ( dataStream.read(b,0,realDataLength) != realDataLength)
                                        throwMathLibException("load: could not read real data");
                                    swap(b, stuffLength, swapEndian);
                                    
                                    // fill array
            // ????                        data[yi][xi] = b[]; // ????
                                }
                            }
                            
                            
                            
                            //////// read imaginary data  ////////
                            
                            
                            
                            
                            break;
                        }
                        //case miINT8:
                        //case miINT16;
                        //....
                        // more to come
                        default:
                        {    
                            throwMathLibException("load: data type not yet supported");
                        }
                    }
					result = new DoubleNumberToken(5555);				
				}
				catch(Exception e)
				{
                    throwMathLibException("load" + e.getMessage());
				}
				fileStream.close();					
			}
			catch (Exception e)
			{
                throwMathLibException("load" + e.getMessage());
			}		    			
			
		return result;
	}
    
    /**
     * swap bytes if type of endian coding is necessary
     * @param c
     * @param len
     */
    private void swap(byte[] c, int len, boolean _swapEndian)
    {
        byte d = 0;
        
        if (_swapEndian)
        {
            for (int i=0; i<len/2; i++)
            {
                d          = c[len-1-i];
                c[len-1-i] = c[i];
                c[i]       = d;
            }
        }
        
    }
    
}
	

/*
@GROUP
IO
@SYNTAX
load(file)
@DOC
Reads in a matrix from a comma seperated value file.
@EXAMPLES
<programlisting>
load
</programlisting>
@SEE
csvread, csvwrite
*/


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

package com.addi.toolbox.general;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;


/**An external function which checks if the argument is a struct*/
public class fft extends ExternalFunction
{
	static {
		System.loadLibrary("addiLib");
	}
	
	public static native double[][] fftNative (double inputReal[], double inputComplex[], int size);
	public static native int fftCleanup ();
	
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {

        if (getNArgIn(operands) != 1)
            throwMathLibException("fft: number of arguments != 1");
        
        if (!(operands[0] instanceof DoubleNumberToken))
            throwMathLibException("fft: not a number");
        
        int sizeX;
        int sizeY;
        double[][] valReal;
        double[][] valImag;
        boolean transposed = false;
        DoubleNumberToken op;
        
        sizeY = ((DoubleNumberToken)operands[0]).getSizeY();
        
        if (sizeY == 1) { //this is just a row, transpose and run down columns
        	op = (DoubleNumberToken) ((DoubleNumberToken)operands[0]).transpose();
        	transposed = true;  //so I can put it back later
        } else {
        	op = ((DoubleNumberToken)operands[0]);
        }

        valReal = op.getValuesRe();
        valImag = op.getValuesIm();
        sizeX = op.getSizeX();
        sizeY = op.getSizeY();
        
        double valOutReal[][] = new double[sizeY][sizeX];
        double valOutImag[][] = new double[sizeY][sizeX];
        
        for (int i=0; i<sizeX; i++) { //per column
        	double valInReal[] = new double[sizeY];
        	double valInImag[] = new double[sizeY];
        	for (int j=0; j<sizeY; j++) {  //per row
        		valInReal[j] = valReal[j][i];
            	valInImag[j] = valImag[j][i]; 
        	}
        	double valOut[][] = fftNative(valInReal, valInImag, sizeY);
        	for (int jj=0; jj<sizeY; jj++) { //per row
        		valOutReal[jj][i] = valOut[jj][0];
        		valOutImag[jj][i] = valOut[jj][1];
        	}
        	fftCleanup();
        }
        
        if (transposed) {
        	return new DoubleNumberToken(valOutReal,valOutImag).transpose();
        } else {
        	return new DoubleNumberToken(valOutReal,valOutImag);
        }
    
    }

}

/*
@GROUP
general
@SYNTAX
fft(values)
@DOC
@EXAMPLES
fft(sin([1:255]))
@NOTES
@SEE
*/
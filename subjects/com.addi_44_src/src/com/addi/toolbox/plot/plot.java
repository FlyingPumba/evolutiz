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

package com.addi.toolbox.plot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.FileOutputStream;

import android.content.Context;
import android.content.Context.*;

import com.addi.core.functions.*;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.interpreter.Interpreter;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

/**
 * An external function for sending data to AddiPlot
 */
public class plot extends ExternalFunction
{

    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	
    	double[][] valuesX = null;
    	double[][] valuesY = null;
    	
    	if (operands.length > 2)
    		throwMathLibException("plot: number of arguements > 2, only support simple plots currently");
    	
    	if (operands.length == 0)
    		throwMathLibException("plot: number of arguements == 0");
    	
        // Check if there's only one or zero parameters
        if (operands.length == 1)
        {
        	if(operands[0] instanceof DoubleNumberToken)
			{
        		valuesY = ((DoubleNumberToken)operands[0]).getReValues();
        		valuesX = valuesY;  //just get the size of it
        		for(int row = 0; row < valuesX.length; row++)
        		{
        			for(int column = 0; column < valuesX[row].length; column++) 
        			{
        				valuesX[row][column] = column;  //the X is just the sample count
        			}
        		}
			}
        	else
        	{
        		throwMathLibException("plot: only supports numeric arguments");
        	}
        }
        else
        {
        	if((operands[0] instanceof DoubleNumberToken)  && (operands[1] instanceof DoubleNumberToken))
			{
        		valuesY = ((DoubleNumberToken)operands[1]).getReValues();
        		valuesX = ((DoubleNumberToken)operands[0]).getReValues();
			}
        	else
        	{
        		throwMathLibException("plot: only supports numeric arguments");
        	}
        }
        
        String plotData = "";
		for(int row = 0; row < valuesX.length; row++) {
    		for(int column = 0; column < valuesX[row].length; column++) {
    			if (valuesX.length == 1) {
    				if (column != 0) {
     				   plotData = plotData + ";";
     				}
    			} else {
    				if (column != 0) {
    				   plotData = plotData + ",";
    				}
    			}
    			plotData = plotData + Double.toString(valuesX[row][column])+ "," + Double.toString(valuesY[row][column]);
    		}
    		plotData = plotData + ";";
		}
		globals.getInterpreter().displayText("STARTUPADDIPLOTWITH="+plotData);
        
        return DoubleNumberToken.one;
    }
}

/*
@GROUP
statistics
@SYNTAX
plot(x,y)
@DOC
Plots a simple 2D line(s)
@EXAMPLES
<programlisting>
plot(x,y)
</programlisting>
@SEE
plot
*/
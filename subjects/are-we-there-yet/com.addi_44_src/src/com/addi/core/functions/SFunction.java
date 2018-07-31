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

package com.addi.core.functions;

import com.addi.core.interpreter.Errors;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;


/**
 * This is the main class for S-Functions
 * The main difference compared to MATLAB's S-Functions is that there is no external
 *    SimStructure needed. All data is kept within this class
 * */
public class SFunction extends ExternalFunction{

    public String SFunctionName  = "";
    public int    SFunctionLevel = 2;

    //public void SFunction()
    //{
    //
    //}
    
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
        return null;
    }
    
    public void mdlCheckParameters()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlProcessParameters()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlInitializeSizes()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    } 

    // number of parameters needs to be fixed
    public void mdlSetInputPortFrameData()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    // number of parameters needs to be fixed
    public void mdlSetInputPortWidth()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    // number of parameters needs to be fixed
    public void mdlSetOutputPortWidth()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    } 

    // number of parameters needs to be fixed
    public void mdlSetInputPortDimensionInfo()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    // number of parameters needs to be fixed
    public void mdlSetOutputPortDimensionInfo()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void mdlSetDefaultPortDimensionInfo()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    // number of parameters needs to be fixed
    public void mdlSetInputPortSampleTime()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    } 

    // number of parameters needs to be fixed
    public void mdlSetOutputPortSampleTime()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    } 

    public void mdlInitializeSampleTimes()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    } 

    // number of parameters needs to be fixed
    public void mdlSetInputPortDataType()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    // number of parameters needs to be fixed
    public void mdlSetOutputPortDataType()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlSetDefaultPortDataTypes()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    // number of parameters needs to be fixed
    public void mdlSetInputPortComplexSignal()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    // number of parameters needs to be fixed
    public void mdlSetOutputPortComplexSignal()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void mdlSetDefaultPortComplexSignals()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void mdlSetWorkWidths()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlInitializeConditions()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlStart()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlGetTimeOfNextVarHit()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlZeroCrossings()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    // number of parameters needs to be fixed
    public void mdlOutputs()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    // number of parameters needs to be fixed
    public void mdlUpdate()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlDerivatives()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void mdlTerminate()
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    
    ////////////////////////////////////////////////////////////////////
    // methods to access the data of the simulation structure

    
    public void ssSetNumContStates(int number)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetNumDiscStates(int number)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void ssSetNumInputPorts(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void ssSetInputPortWidth(int x, int y)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetInputPortRequiredContiguous(int x, boolean b)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetInputPortDirectFeedThrough(int x, int y)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void ssSetNumOutputPorts(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetOutputPortWidth(int x, int y)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void ssSetNumSampleTimes(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void ssSetNumRWork(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetNumIWork(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetNumPWork(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetNumModes(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }
    
    public void ssSetNumNonsampledZCs(int x)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

    public void ssSetOptions(int options)
    {
        Errors.throwMathLibException("SFunction: not yet implemented");
    }

} // end class SFunction

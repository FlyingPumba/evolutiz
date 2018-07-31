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

import com.addi.core.constants.TokenConstants;
import com.addi.core.constants.*;
import com.addi.core.interpreter.*;


/**This is the base class for all the types of token supported by MathLib*/
abstract public class Token extends RootObject implements TokenConstants
{
    /**The priority of the token*/
    protected int priority;
    
    /**Indicator if a result of an operation is displayed at the prompt or not
       (e.g. a=2+3, then a=5 is shown at the prompt. a=2+3; then nothing is
       displayed at the prompt */
    private boolean displayResultSwitch = false;
    
    public static int loopDepth = 0;
    public static boolean breakHit = false;
    public static boolean continueHit = false;

    /**Default Constructor - create a token with the type not set
     */
    public Token()
    {
        priority = 0;
    }

    /**Constructor 
     * @param _priority = priority of token
     */
    public Token(int _priority)
    {
        priority = _priority;
    }

    /**evaluate the token
     * @param operands = an array of RootObject containing the tokens operands
     * @param 
     * @return the result of the token evaluation as a RootObject
     * */
    public abstract OperandToken evaluate(Token[] operands, GlobalValues globals);
    
    /**
     * @return a string representation of the token
     */
    abstract public String toString();

    /**
     * 
     * @param globals
     * @return
     */
    public String toString(GlobalValues globals)
    {
        return toString();
    }
    
    /**Converts the token to its MathML representation. At the moment this is unimplemented and just
       converts the token into a string*/
    public String toMathMlString(OperandToken[] operands)
    {
        return toString(operands);
    }
    
    /** set the display flag for a given token */
    public void setDisplayResult(boolean _displayResultSwitch)
    {
    	displayResultSwitch = _displayResultSwitch;
    }
    
    /** return if the display flag for a given token is set 
     * @return
     */
    public boolean isDisplayResult()
    {
    	return displayResultSwitch;
    }
}

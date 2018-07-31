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



/**Used to implement relation operations within an expression*/
public class RelationOperatorToken extends BinaryOperatorToken
{

    /**Constructor taking the operator and priority
     * @param _operator = the operator being created
     */
    public RelationOperatorToken (char _operator)
    {
        super(_operator, RELATION_PRIORITY);
    }
    
    /**
     * 
     * @return priority of current relation (e.g. < > | & && ...)
     */
    public int getPriority()
    {
        switch (value)
        {
            case '<':
            case '>':
            case 'l':
            case 'g':
                return RELATION_PRIORITY;
            case 'e':
            case 'n':
                return RELATION_PRIORITY;
            case '&':
                return AND_PRIORITY;
            case '|':
                return OR_PRIORITY;
            case 'a':
                return ANDAND_PRIORITY;
            case 'o':
                return OROR_PRIORITY;
        }
        
        Errors.throwMathLibException("RelationToken unknown priority");
        return 0;
        
    }

    /**evaluates the operator
     * @param operands = the operators operands
     * @return the result as an OperandToken
     */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;

		ErrorLogger.debugLine("RelationToken: evaluate");

        // it is much easier to compare number, so convert to number
        if (operands[0] instanceof LogicalToken)
            operands[0]=((LogicalToken)operands[0]).getDoubleNumberToken();

        // it is much easier to compare number, so convert to number
        if (operands[1] instanceof LogicalToken)
            operands[1]=((LogicalToken)operands[1]).getDoubleNumberToken();
        
                
		if (    (operands[0] instanceof DoubleNumberToken)
			 && (operands[1] instanceof DoubleNumberToken) )
		{
            // two operands and both a numbers
            // e.g. 2>3, 3>=6, 4!=3, [2,3,4]>3
            DoubleNumberToken num0  = (DoubleNumberToken)operands[0];
            DoubleNumberToken num1  = (DoubleNumberToken)operands[1];
            int[]       size0 = num0.getSize();
            int[]       size1 = num1.getSize();
            int         n0    = num0.getNumberOfElements();
            int         n1    = num1.getNumberOfElements();
            
            
			if ( num0.isScalar() && num1.isScalar() )
			{
				// <scalar> <operator> <scalar>
			    // e.g.  2!=3, 5>6
				boolean returnValue = evalRelation(value, 
                                                   num0.getValueRe(0),
                                                   num1.getValueRe(0));
				return new LogicalToken( returnValue );		
			}
			else if ( num1.isScalar() )
			{
				// <array> <operator> <scalar>
			    // e.g. [2,3,4,5] > 3
				boolean[] returnValues = new boolean[n0];

				// Check relation for each element
				for (int i=0; i<n0; i++) 
				{
				    returnValues[i] = evalRelation(value, 
				                                   num0.getValueRe(i), 
                                                   num1.getValueRe(0));
				}
				return new LogicalToken(size0, returnValues);
                
			}
			else if ( num0.isScalar() )
			{
				// <scalar> <operator> <array>
			    // e.g. 3 > [2,3,4]
                boolean[] returnValues = new boolean[n1];

                // Check relation for each element
                for (int i=0; i<n1; i++) 
                {
                    returnValues[i] = evalRelation(value, 
                                                   num0.getValueRe(0), 
                                                   num1.getValueRe(i));
                }
                return new LogicalToken(size1, returnValues);
                
			}
            else if ( DataToken.checkEqualDimensions(size0, size1) )
            {
                // <array> <operator> <array>
                // e.g. [3,3,3] > [2,3,4]
                boolean[] returnValues = new boolean[n0];

                // Check relation for each element
                for (int i=0; i<n0; i++) 
                {
                    returnValues[i] = evalRelation(value, 
                                                   num0.getValueRe(i), 
                                                   num1.getValueRe(i));
                }
                return new LogicalToken(size0, returnValues);

            }
			else
			{
				// dimensions do not fit
                // e.g. [2,3]>[2,3,4]
				Errors.throwMathLibException("RelationToken: dimensions don't fit");
				return null;
			}
		}
        else if (    (operands[0] instanceof CharToken)
                  && (operands[1] instanceof CharToken) )
        {
            // two operands and both are strings
            // e.g. 2>3, 3>=6, 4!=3
            CharToken char0 = (CharToken)operands[0];
            CharToken char1 = (CharToken)operands[1];
            int[]     size0 = char0.getSize();
            int[]     size1 = char1.getSize();
            int       n0    = char0.getNumberOfElements();
            int       n1    = char1.getNumberOfElements();

     // !!! changed from x,y to n-d-array        
            
            String  op0Values       = ((CharToken)operands[0]).getValue(); 
            int     op0SizeX        =  op0Values.length();    
            String  op1Values       = ((CharToken)operands[1]).getValue(); 
            int     op1SizeX        =  op1Values.length();    

            if (op1SizeX == 1) 
            {
                // <array> <operator> <scalar>
                // e.g. 'asdf' != 'd'
                boolean[][] returnValues = new boolean[1][op0SizeX];

                // Check relation for each element
                for (int xx=0; xx<op0SizeX; xx++)
                {
                    returnValues[0][xx] = evalRelation(value, 
                                                       op0Values.charAt(xx), 
                                                       op1Values.charAt(0));
                }
                return new LogicalToken( returnValues );
            }
            else if (op0SizeX == 1) 
            {
                // <array> <operator> <scalar>
                // e.g. 'asdf' != 'd'
                boolean[][] returnValues = new boolean[1][op1SizeX];

                // Check relation for each element
                for (int xx=0; xx<op1SizeX; xx++)
                {
                    returnValues[0][xx] = evalRelation(value, 
                                                       op0Values.charAt(0), 
                                                       op1Values.charAt(xx));
                }
                return new LogicalToken( returnValues );
            }
            else if ( op0SizeX == op1SizeX )
            {
                // <array> <operator> <scalar>
                // e.g. 'asdf' != 'asdd'
                boolean[][] returnValues = new boolean[1][op0SizeX];

                // Check relation for each element
                for (int xx=0; xx<op0SizeX; xx++)
                {
                    returnValues[0][xx] = evalRelation(value, 
                                                       op0Values.charAt(xx), 
                                                       op1Values.charAt(xx));
                }
                return new LogicalToken( returnValues );
            }
            else
            {
                // dimensions do not fit
                // e.g. [2,3]>[2,3,4]
                Errors.throwMathLibException("RelationToken: dimensions don't fit");
            }

        }
        
		Errors.throwMathLibException("RelationToken: Token not supported");
		return null;
    }
    
	/** evaluate relations (<, >, <=, >=, == ~=) 
	 * @param value = the tpye of operator
	 * @param arg0 = the left hand operand
	 * @param arg1 = the right hand operand
	 * @return the result as a double
     */
	private boolean evalRelation(char value, double arg0, double arg1)
	{
		switch (value)
		{
		case '<':
			if (arg0 < arg1)  return true;
			break;
		case '>':
			if (arg0 > arg1)  return true;
			break;
		case 'l':
			if (arg0 <= arg1) return true;
			break;
		case 'g':
			if (arg0 >= arg1) return true;
			break;
		case 'e':
			if (arg0 == arg1) return true;
			break;
		case 'n':
			if (arg0 != arg1) return true;
			break;
        case '|':
            if ((arg0!=0.0) || (arg1!=0.0)) return true;
            break;
        case 'o':
            if ((arg0!=0.0) || (arg1!=0.0)) return true;
            break;
        case '&':
            if ((arg0!=0.0) && (arg1!=0.0)) return true;
            break;
        case 'a':
            if ((arg0!=0.0) && (arg1!=0.0)) return true;
            break;
                    
		}

		// relation not true --> return false
        return false;
	}

    /**
     * 
     * @param value
     * @param arg0
     * @param arg1
     * @return
     */
    private boolean evalRelation(char value, char arg0, char arg1)
    {

        switch (value)
        {
        case '<':
        case '>':
        case 'l':
        case 'g':
        case '|':
        case 'o':
        case '&':
        case 'a':
            Errors.throwMathLibException("relation not implemented");
            break;
        case 'e':
            if (arg0 == arg1) return true;
            break;
        case 'n':
            if (arg0 != arg1) return true;
            break;
                    
        }

        // relation not true --> return false
        return false;
    }

    /**
     * 
     * @return
     */
    public String toString()
    {
		String valueString = String.valueOf(value);
		switch (value)
		{
		case 'l':
			valueString = "<=";
			break;
		case 'g':
			valueString = ">=";
			break;
		case 'e':
			valueString = "==";
			break;
		case 'n':
			valueString = "~=";
			break;
		case 'a':
			valueString = "&&";
			break;
		case 'o':
			valueString = "||";
			break;
		}
        return valueString;
    }
    
} // end RelationOperatorToken

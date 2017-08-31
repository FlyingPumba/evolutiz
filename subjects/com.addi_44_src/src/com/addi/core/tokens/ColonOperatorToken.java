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




/**Used to implement subscriptions of a array*/
public class ColonOperatorToken extends BinaryOperatorToken
{
	/**Default Constructor - creates an operator with the value set to ' '*/
    public ColonOperatorToken()
    {
        super();
        value = ' ';
    }

    /**evaluates the operator*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;

		double x1;      // minimum
		double x2;      // maximum
		double dx = 1;  // increment

        if (operands==null) return new Expression(new ColonOperatorToken());
        
		if ((operands.length < 2) || (operands.length >3)) 
            Errors.throwMathLibException("ColonOperator: <2 or >3 arguments");

		if ((operands[0]==null) || (operands[1]==null) )   
		    Errors.throwMathLibException("ColonOperator: argument 1 or 2 is null");

		if (  operands.length==2 && 
              (  (operands[0] instanceof DoubleNumberToken) &&
			     (operands[1] instanceof DelimiterToken) ) )  
            return new Expression(new ColonOperatorToken(), 
                                  (OperandToken)operands[0],
                                  (OperandToken)operands[1]);
            
        if (operands.length==3 && 
            (operands[0] instanceof DoubleNumberToken) &&
            (operands[1] instanceof DoubleNumberToken) &&
            (operands[2] instanceof DelimiterToken) )
        {
            OperandToken[] rettok = new OperandToken[3];
            rettok[0]= (OperandToken)operands[0];
            rettok[1]= (OperandToken)operands[1];
            rettok[2]= (OperandToken)operands[2];
            return new Expression(new ColonOperatorToken(), rettok, 3);
        }
 
		// get data from arguments
		if (operands.length==0)
        {
            return new Expression(new ColonOperatorToken());
        }
        else if (operands.length == 2)
		{
            if ( (!(operands[0] instanceof DoubleNumberToken)) ||
                 (!(operands[1] instanceof DoubleNumberToken))   )
                    Errors.throwMathLibException("ColonOperator: argument not number or end (x:x)");

			// e.g. 4:5
			x1 =      ((DoubleNumberToken)operands[0]).getReValues()[0][0];
			x2 =      ((DoubleNumberToken)operands[1]).getReValues()[0][0];		
		}
		else
		{
			if (operands[2] == null)                   return null;
            if ( (!(operands[0] instanceof DoubleNumberToken)) ||
                 (!(operands[1] instanceof DoubleNumberToken)) ||
                 (!(operands[2] instanceof DoubleNumberToken))   )
                       Errors.throwMathLibException("ColonOperator: argument not number or end (x:x:x)");

			// e.g. 4:2:20
			x1 =      ((DoubleNumberToken)operands[0]).getReValues()[0][0];
			dx =      ((DoubleNumberToken)operands[1]).getReValues()[0][0];
			x2 =      ((DoubleNumberToken)operands[2]).getReValues()[0][0];		
		}

		ErrorLogger.debugLine("ColonOperator: x1,dx,x2 "+ x1 +" "+ dx +" " +x2); 
		
        int n = (int) ( (x2-x1)/dx ) + 1;
		double[][] values = new double[1][n];
		for (int i=0; i<n ; i++)
		{
			values[0][i] = x1 +  ((double)i) * dx;
		}
		
		return new DoubleNumberToken(values);	

    }
    

    /**Convert the operator to a string*/
    public String toString()
    {
        return ":";
    }

    /**return a string containing the operator and it's operands*/
    /*public String toString(OperandToken[] operands)
    {
        if (operands==null) return ":";
        
        if (operands.length==2)
		{
			if ((operands[0]!=null) && (operands[1]!=null))
	            return operands[0].toString() + ":" + operands[1].toString();
		}
		else if (operands.length==3)
		{
			if ((operands[0]!=null) && 
				(operands[1]!=null) &&
				(operands[2]!=null)   )
	            return operands[0].toString() + ":" + 
                       operands[1].toString() + ":" +
					   operands[2].toString();
		}
        return ":";
    }*/
    
}

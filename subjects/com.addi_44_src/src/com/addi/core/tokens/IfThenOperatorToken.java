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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.addi.core.interpreter.*;

/**Used to implement if-then-else operations within an expression*/
public class IfThenOperatorToken extends CommandToken implements Collection
{
    /**a list of the conditions*/
    ArrayList conditions;

    /**Constructor setting ifRelation and ifCode
       @param _ifRelation = the test relation
       @param _ifCode     = the code to execute if the test is true*/
    public IfThenOperatorToken(OperandToken _ifRelation, OperandToken _ifCode)
    {
      //  this();
        ConditionToken condition = new ConditionToken(_ifRelation, _ifCode);
        conditions = new ArrayList();
        conditions.add(condition);
    }

    /**Constructor setting ifRelation and ifCode and elseCode
       @param _ifRelation = the test relation
       @param _ifCode     = the code to execute if the test is true
       @param _elseCode   = the code to execute if the test is false*/
    public IfThenOperatorToken(OperandToken _ifRelation, OperandToken _ifCode, OperandToken _elseCode)
    {
        //this();
        
        // add IF condition (e.g. if(a=1) b=2)
        ConditionToken condition = new ConditionToken(_ifRelation, _ifCode);
        conditions.add(condition);

        // add ELSE (e.g. else b=3)
        condition = new ConditionToken(null, _elseCode);
        conditions.add(condition);
    }
    
    /**Add another relation to the if expression
    @param _ifRelation = the test
    @param _ifCode     = the code to execute*/
    public void addCondition(OperandToken _ifRelation, OperandToken _ifCode)
    {
        ConditionToken condition = new ConditionToken(_ifRelation, _ifCode);
        conditions.add(condition);
    }

    public Expression getIfRelation()
    {
        return null;  //ifRelation;
    }

    public Expression getIfCode()
    {
        return null;  //ifCode;
    }

    public Expression getElseCode()
    {
        return null;  //elseCode;
    }

    /**evaluates the operator
     * @param operands = the operators operands
     * @param
     * @return the result of the test as an OperandToken
     */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
    	ErrorLogger.debugLine("Parser: IfThen: evaluate "+conditions.size());

    	OperandToken result = null;
    	
    	int pos = 0;
    	int count = conditions.size();
    	while((result == null) && (pos < count)) 
    	{
    		ConditionToken conditionToken = ((ConditionToken)conditions.get(pos));
    		ErrorLogger.debugLine("Parser: IfThen: cond: " + conditionToken.toString());
    		
    		result = conditionToken.evaluate(null, globals);
    		
    		pos++;
    	}
    	
    	return null;  // if-then-elseif-else doesn't return any data
    }
    

    /**Convert the operator to a string
    @return the operator as a string*/
    public String toString()
    {
        return "if then";
    }


    /*Collection methods*/
  
    /**return an iterator for traversing the condition objects*/
    public Iterator iterator()
    {
        return conditions.iterator();
    }

    public Object[] toArray(Object[] wtf)
    {
        return conditions.toArray(wtf);
    }

    public Object[] toArray()
    {
        return conditions.toArray();
    }

    public boolean retainAll(Collection c)
    {
        return conditions.retainAll(c);
    }

    public boolean removeAll(Collection c)
    {
        return conditions.removeAll(c);
    }

    public boolean containsAll(Collection c)
    {
        return conditions.containsAll(c);
    }

    public boolean addAll(Collection c)
    {
        return conditions.addAll(c);
    }

    public boolean remove(Object o)
    {
        return conditions.remove(o);
    }

    public boolean contains(Object o)
    {
        return conditions.contains(o);
    }

    public boolean add(Object o)
    {
        return false;
    }

    public boolean isEmpty()
    {
        return conditions.isEmpty();
    }

    public void clear()
    {
        conditions.clear();
    }

    public int size()
    {
        return conditions.size();
    }
}

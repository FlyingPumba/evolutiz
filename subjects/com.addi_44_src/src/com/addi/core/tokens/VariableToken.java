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

import com.addi.core.constants.*;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;
import com.addi.toolbox.jmathlib.matrix.*;



// !!!!! for NumberTokens I changed value to values[0][0]
// !!!!! THERE is MORE to do (stefan)


/**Class representing any variables used in an expression*/
public class VariableToken extends DataToken implements ErrorCodes
{
    /**The variable name*/
    private String name;

    /** name for struct*/
    private String fieldName;

    /**The limits of this variable (e.g. a(2,3:5) )*/
    private OperandToken[] limitTokens;

    /**Boolean indicator if limits are active */
    private boolean limitSwitch = false;
    
    /** true if e.g. a{8}... */
    private boolean cellB = false;
    
    /**constructor containing the variables name
     * @param _name = the name of the variable
     */
    public VariableToken(String _name)
    {
        super(5, "variable"); 
        name       = _name;
        fieldName  = null;
    }

    /** constructor containing the variables name
     * @param _name = the name of the variable
     * @param _var  = the variable being referenced
     */
    public VariableToken(String _name, String _fieldName) 
    {
        super(5, "variable"); 
        name       = _name;
        fieldName  = _fieldName;
    }
  

    /**constructor containing the variables name and limiting arguments
     * @param _name      = the name of the variable
     * @param _operands  = the limits of this variable (e.g. a(1,2:4) )
     */
    public VariableToken(String _name, OperandToken[] _limits) 
    {
        super(5, "variable"); 
        name       = _name;
        fieldName  = null;
        this.setLimits(_limits);
    }

    /**constructor containing the variables name and limiting arguments
     * @param _name      = the name of the variable
     * @param _operands  = the limits of this variable (e.g. a(1,2:4) )
     * @param _type      = 'cell' if cell variable
     */
    public VariableToken(String _name, OperandToken[] _limits, String _type) 
    {
        super(5, "variable"); 
        name       = _name;
        fieldName  = null;
        this.setLimits(_limits);
        cellB      = true;
    }


	/**@return true if the variable token has limits*/
	public boolean isLimited()
	{
		return limitSwitch;
	}

    /**set limits for variable (e.g. a(1,3:4) )
    @param limits = the limiting operands (DoubleNumberToken, ColonOperator) */
	public void setLimits(OperandToken[] _limits)
	{
		ErrorLogger.debugLine("VariableToken: setLimits ");
		
		// variable has limits
        limitSwitch = true;
        limitTokens = new OperandToken[_limits.length];
        
        
        for(int childNo = 0; childNo < _limits.length; childNo++)
        {
            limitTokens[childNo] = (OperandToken)(_limits[childNo].clone());
        }
        
    /*    // check and set limits of variable token
	    for(int childNo = 0; childNo < _limits.length; childNo++)
	    {
			if(_limits[childNo] != null)
			{
			    // do not evaluate something like "4:6" or ":" when it is a parameter
			    //   of a function/variable
			    if (_limits[childNo] instanceof Expression)
			    {
                   	if (!( ((Expression)_limits[childNo]).getData() instanceof ColonOperatorToken))
			    	    limitTokens[childNo] = _limits[childNo].evaluate(null);
                    else
                        limitTokens[childNo] = _limits[childNo];
			    }
                else 
		        {
                   limitTokens[childNo] = _limits[childNo].evaluate(null);
                }
                
        	}
	    }
     */
	} // end setlimits

	/** return limits of this variable */
    public OperandToken[] getLimits()
	{
		ErrorLogger.debugLine("VariableToken: getLimits");
		return limitTokens;
	}
   
    /**@return the value of the data held within the variable
    if the variable has not been inisitalised then it returns the variable*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
		ErrorLogger.debugLine("VariableToken: eval: " + name);
        //ErrorLogger.debugLine("VariableToken: evalG: " + globals);
        
        // Check if variable is defined (in object storage)
        if(fieldName != null)
        {
            //if(fieldName.equals("global")) //is this access to the global ascope
            //{
            //    ErrorLogger.debugLine("getting global variable");
            //    return getGlobalVariables().getVariable(name).getData();
            //}
            //else
            //{
                ErrorLogger.debugLine("VariableToken: " + name + "getting field " + fieldName);
                return ((MathLibObject)globals.getVariable(name).getData()).getFieldData(fieldName);
            //}            
        }
        else if( globals.getVariable(name) == null )
        {
            // variable is not yet defined (e.g. user typed sin(a) and "a" is unknown)
            //  or it is a function
            
            // check for predefined variables
            if (name.equals("pi"))
            	return new DoubleNumberToken(3.14159265358979);

            if (name.equals("eps"))
                return new DoubleNumberToken(2.2204e-016);

            if (name.equals("e"))
                return new DoubleNumberToken(2.718281828459046);

            if (name.equals("i"))
                return new DoubleNumberToken("0","1");
            
            if (name.equals("j"))
                return new DoubleNumberToken("0","1");
            
    	    ErrorLogger.debugLine("VariableToken: var " + name + " not found: check functions");
    
    	    // If it is not a variable maybe it's a function or script-file
    	    FunctionToken function = new FunctionToken(name, limitTokens);
    	    function.setOperands(new OperandToken[] {}); 
    	    OperandToken  retValue = function.evaluate(null, globals); 
            
            // if the "function" is really a script it won't return anything
            if (function.isScript())
               return null;
               
            // if the functions returns something, also return that value
            if (retValue != null) 
               return retValue;

            // check if a function has been evluated 
            //(in case this functions returns null by default)
            if (function.isEvaluated())
                return null;
            
    	    // all variables must be defined
    	    Errors.throwMathLibException("VariableToken: undefined variable or function "+name);
    
    	    return null;  
		 
        }

    	// variable is defined already/now
    	// get data of variable
        OperandToken variableData = globals.getVariable(name).getData();

        // check if data is available
        if(variableData != null)
        {
            ErrorLogger.debugLine("VariableToken data = " + variableData.toString());
        	
            // clone data so that original values are not changed
            variableData = ((OperandToken)variableData.clone());

        	OperandToken result = variableData;

			// check if variable has limits
			if (limitSwitch && (result instanceof DataToken))     
			{
				// create operand-array for SubMatrix() method
				OperandToken[] opTok = new OperandToken[limitTokens.length + 1];
				opTok[0] = variableData;
				for (int i=0; i<limitTokens.length; i++)
				{
					//ErrorLogger.debugLine(i);
					// clone limits functions/values to preserve for future evalutation
                    opTok[i+1] = ((OperandToken)limitTokens[i].clone());
                    opTok[i+1] = opTok[i+1].evaluate(null, globals);

					if (opTok[i+1] != null)
						ErrorLogger.debugLine("VariableToken: eval: toString("+i+") "+opTok[i+1].toString());
				}
                
                submatrix subM = new submatrix();
                
                if (isCell())
                {
                    ErrorLogger.debugLine("variable token: left is cell");
                    subM.setLeftCell();
                }
                
                // create instance of external function SubMatix and compute submatrix
                result = subM.evaluate(opTok, globals);

			}

			/* display the result of this variable in the user console*/
            if (isDisplayResult())
                globals.getInterpreter().displayText(name +" = "+ result.toString(globals));
            
            return result;
        }
        else
		{
            ErrorLogger.debugLine("Variable data = NULL");
            
            // display the result of this variable in the user console
            if (isDisplayResult())
                globals.getInterpreter().displayText(name +" = []");

            return null;
		}
    } // end eval

	/**Implement the equals operator to find a VariableToken with the
       correct name
    @param _data = the object to match against
    @return true if the are equal*/
    public boolean equals(OperandToken _data)
    {
        boolean equal;
        if(_data instanceof VariableToken)
            equal = this.name.equals( ((VariableToken)_data).getName() );
        else
            equal = super.equals(_data);

		ErrorLogger.debugLine("VariableToken equals "+_data.toString()+" "+equal);

        return equal;
    }

	/**@return either the variable data as a string or the variable name*/
    public String toString()
    {
       	String result = name;

        if(fieldName != null)
            result = result + "." + fieldName;

        return result;
    }

	/**return the name of the variable*/
    public String getName()
    {
    	return name;
    }

	/**return the name of the variable*/
    public String getFieldName()
    {
    	return fieldName;
    }
    
	/**return the data of the variable*/
/*    public OperandToken getData()
    {
        if(fieldName == null)
    	   return getVariable(name).getData();
        else
           return ((MathLibObject)getVariable(name).getData()).getFieldData(fieldName);
    }
*/
    public boolean equals(Object obj)
    {
        if(obj instanceof VariableToken)
        {
            VariableToken var = ((VariableToken)obj);
            if(var.getName().equals(name))
                return true;            
        }
        return false;
    }
    
    /**Checks if this operand is a numeric value
    @return true if this is a number, false if it's 
    an algebraic expression*/
/*    public boolean isNumeric()
    {
    	boolean numeric = false;
    	OperandToken data = null;
        data = getVariable(name).getData();
        
    	if(data != null)   //exchanged "this" with "name"
    		numeric = data.isNumeric();
    		
    	return numeric;
    }
 */   
    
    /**
     * check if variable is a struct
     * @return
     */
    public boolean isStruct()
    {
    	if (fieldName!=null)
    		return true;
    	else
    		return false;
    	
    }

    /**
     * check if variable is a cell array
     * @return
     */
    public boolean isCell()
    {
        return cellB;
    }

    /**get the variable that this token references*/
    public Variable getVariable(GlobalValues globals)
    {
        if(fieldName == null)
            return globals.getVariable(name);
        
        if(globals.getVariable(name)!=null)
             return ((MathLibObject)globals.getVariable(name).getData()).getFieldVariable(fieldName);

        return null;
    }
}

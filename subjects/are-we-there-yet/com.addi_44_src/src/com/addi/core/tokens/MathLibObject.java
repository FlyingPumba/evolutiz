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

import java.util.*;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.numbertokens.*;

/** */
public class MathLibObject extends DataToken
{
    /**An array of all the data stored in the structure*/
    private HashMap fields;
    
    /**Create a new structure*/
    public MathLibObject()
    {
        super(10, "struct"); 
        fields = new HashMap();
    }
    
    /**Create a structure and copy it's data from another structure
    @param oldVal = the structure to copy the values from*/
    public MathLibObject(MathLibObject oldVal)
    {
        super(10, "struct"); 
    	fields = ((HashMap)oldVal.getFieldsHash().clone());
    }

    /**Set the value of one of the structures fields
    @param fieldName = the name of the field to set
    @param value = the value to set the field to*/
    public void setField(String fieldName, OperandToken value)
    {
		 Variable var = ((Variable)fields.get(fieldName));
	
		 if(var == null)
		 {
		      var = new Variable(fieldName, value);
		      fields.put(fieldName, var);
		 }
		 var.assign(value);
    }

    /**Get the value of a particular field
    @param fieldName = the name of the field
    @return the fields data*/    
    public OperandToken getFieldData(String fieldName)
    {
		ErrorLogger.debugLine("getfield");
		Variable var = ((Variable)fields.get(fieldName));
		if(var != null)
		    return var.getData();   
	
	        return null;
    } 
    
    /**Get the value of a particular field
    @param fieldName = the name of the field
    @return a variable pointing to the fields data*/    
    public Variable getFieldVariable(String fieldName)
    {
    	Variable var = ((Variable)fields.get(fieldName));
        if(var != null)
        {
            ErrorLogger.debugLine("getting field data");
            return var;   
        }
        return null;
    }

    /**@return a list of all fields*/
    public Iterator getFields()
    {
        return fields.entrySet().iterator();
    }
    
    /**@return a list of all fields*/
    public HashMap getFieldsHash()
    {
        return fields;
    }

    /**Turns the structure into a string*/
    public String toString()
    {
        String result = "[";
		Set entries = fields.entrySet();
		Iterator iter = entries.iterator();
		while(iter.hasNext())
		{
		    Map.Entry entry = (Map.Entry)iter.next();
		    result += entry.getKey() + " = " + entry.getValue(); 
	
		    if(iter.hasNext())
			 result += ", ";
		}    
        
		result += "]";
        return result;
    }
    
    /**Evaluates the structure
    @return the structure itself*/
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {
    	if (breakHit || continueHit)
    		return null;
    	
        return this;
    }

    /**
     * @param
     * @return
     */
    public OperandToken add(OperandToken arg)
    {
		 MathLibObject result = new MathLibObject(this);
	        
		 if(arg instanceof MathLibObject)
		 {
		      Iterator argFields = ((MathLibObject)arg).getFields();
            
            while(argFields.hasNext())
            {
                Variable var = ((Variable)((Map.Entry)argFields.next()).getValue());
                String fieldName = var.getName();
                
                OperandToken data = getFieldData(fieldName);
                
                if(data != null)
                {
                    result.setField(fieldName, data.add(var.getData()));
                }
                else
                {
                    result.setField(fieldName, var.getData());
                }
            }                
        }
        return result;
    }
    
    /**
     * @param
     * @return
     */
    public OperandToken subtract(OperandToken arg)
    {
    	MathLibObject result = new MathLibObject(this);
        
    	if(arg instanceof MathLibObject)
    	{
    		Iterator argFields = ((MathLibObject)arg).getFields();
            
		      while(argFields.hasNext())
		      {
			   Variable var = ((Variable)((Map.Entry)argFields.next()).getValue());
			   String fieldName = var.getName();
			   
			   OperandToken data = getFieldData(fieldName);
	                
			   if(data != null)
			   {
				result.setField(fieldName, data.subtract(var.getData()));
			   }
			   else
			   {
				NumberToken temp = DoubleNumberToken.zero;
				result.setField(fieldName, temp.subtract(var.getData()));
			   }
		      }                
    	}
    	return result;
    }
    
    /**
     * @param arg
     * @return 
     */
    public OperandToken multiply(OperandToken arg)
    {
    	MathLibObject result = new MathLibObject(this);
        
    	 if(arg instanceof MathLibObject)
    	 {
    	      Iterator argFields = ((MathLibObject)arg).getFields();
                
    	      while(argFields.hasNext())
    	      {
    		   Variable var = ((Variable)((Map.Entry)argFields.next()).getValue());
    		   String fieldName = var.getName();
    		   
    		   OperandToken data = getFieldData(fieldName);
                    
    		   if(data != null)
    		   {
    			result.setField(fieldName, data.multiply(var.getData()));
    		   }
    		   else
    		   {
    			result.setField(fieldName, DoubleNumberToken.zero);
    		   }
    	      }                
    	 }
    	 return result;
    }

    /**
     * @param
     * @return
     */
    public OperandToken divide(OperandToken arg)
    {
    	 MathLibObject result = new MathLibObject(this);
            
    	 if(arg instanceof MathLibObject)
    	 {
    	      Iterator argFields = ((MathLibObject)arg).getFields();
                
    	      while(argFields.hasNext())
    	      {
    		   Variable var = ((Variable)((Map.Entry)argFields.next()).getValue());
    		   String fieldName = var.getName();
    		   
    		   OperandToken data = getFieldData(fieldName);
                    
    		   if(data != null)
    		   {
    			result.setField(fieldName, data.divide(var.getData()));
    		   }
    		   else
    		   {
    			result.setField(fieldName, DoubleNumberToken.zero);
    		   }
    	      }                
    	 }
    	 return result;
    }

    /**
     * @param
     */
    public OperandToken power(OperandToken arg)
    {
    	 MathLibObject result = new MathLibObject(this);
            
    	 if(arg instanceof MathLibObject)
    	 {
    	      Iterator argFields = ((MathLibObject)arg).getFields();
                
    	      while(argFields.hasNext())
    	      {
                   Variable var = ((Variable)((Map.Entry)argFields.next()).getValue());
                   String fieldName = var.getName();
                   
                   OperandToken data = getFieldData(fieldName);
                        
                   if(data != null)
                   {
                       result.setField(fieldName, data.power(var.getData()));
                   }
                   else
                   {
                       result.setField(fieldName, DoubleNumberToken.zero);
                   }
    	      }                
    	 }
    	 return result;
    }
   
}

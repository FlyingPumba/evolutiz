package com.addi.toolbox.jmathlib.system;

import java.util.Date;
import java.util.Random;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.OperandToken;
import com.addi.core.tokens.Token;



public class jmathlibcreateuniqueid extends ExternalFunction
{
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {   

        //throwMathLibException("jmathlibcreateuniqueid");

        
        String uniqueIDS = globals.getProperty("jmathlib.id.unique");
        
        if (uniqueIDS==null)
        {
            
            Date d = new Date();
            double start = (double)d.getTime();
            String startS = new Double(start).toString();
            
            String randS = new Double(Math.random()).toString();
            
            //System.out.println("RANDOM "+startS +" "+randS);
            
            globals.setProperty("jmathlib.id.unique", startS+randS);
        }
        
        return  null;
    }
}

/*
@GROUP
system
@SYNTAX
jmathlibcreateuniqueid
@DOC
create a unique id for jmathlib
@NOTES
@EXAMPLES
@SEE
update, checkforupdates
*/
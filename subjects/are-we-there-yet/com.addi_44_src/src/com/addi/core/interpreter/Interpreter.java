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

 package com.addi.core.interpreter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.content.Context;

/**This is the main interface for the program. Any interface to the MathLib program would access
it through the functions exposed by this class.*/
public class Interpreter 
{
    /**Is the class being called from an application or an applet*/
    boolean runningStandalone;

    /**panel used for displaying text*/
    //CCX private JMathLibOutput outputPanel = null;
    private ArrayAdapter<String> _outputArrayAdapter;
    
    /**global pointers and values */
    public GlobalValues globals = null;
    
    /**for testing purposes additional throwing of errors can be enables */
    public boolean throwErrorsB = false;
    
    private static Handler _mHandler;
    
    private static AssetManager _assetManager;
    
    private static Activity _act;
    
    private static File cacheDir;

    /**
     * Constructs the interpreter and sets the constants
     * @param _runningStandalone = true if this is being used from an application
     */	
    public Interpreter(boolean _runningStandalone)
    {
    	// indicator if this is a stand alone application or
        // if JMathLib is running as an applet or servlet or ... without
        // direct access to the file system
        runningStandalone = _runningStandalone;
        
    	// initialize global pointers, this pointer will be passed to
    	//  all expressions for access to function manager, variable lists, contexts,...
    	globals = new GlobalValues(this, runningStandalone);

	    // read preferences from a file on the disc or on the web
	    globals.loadPropertiesFromFile();
        
    }
    
    /**
     * sets the panel to write any text to
     * @param _outputPanel = the panel to write to, must implement the
     *         MathLibOutput interface
     */
    public void setOutputAdapter(ArrayAdapter<String> outputArrayAdapter)
    {
	    _outputArrayAdapter = outputArrayAdapter;
    }

    /**
     * returns the panel to write any text to
     * @return outputPanel = the panel to write to
     */
    public ArrayAdapter<String> getOutputAdapter()
    {
	    return _outputArrayAdapter;
    }


    /**
     * write status message to GUI
     * @param _status
     */
    public void setStatusText(String _status)
    {
        //CCX outputPanel.setStatusText(_status);
    }
    
    
    /**
     * displays a string to the outputPanel
     *  @param text = the text to display
     */
    public static void displayText(String text)
    {
    	Message msg = new Message();
    	Bundle bndl = new Bundle();
    	bndl.putString("text", text);
    	msg.setData(bndl);
    	_mHandler.sendMessage(msg);
    }
	
    /**
     * saves the variable list
     */
    public void save() 
    {
	    if(runningStandalone)
	    {
	        executeExpression("finish",_act,_mHandler);
	    
	        // store current properties to file
            globals.storePropertiesToFile();
        }
    }
    
    public static Activity getActivity() {
    	return _act;
    }
    
    public static Handler getHandler() {
    	return _mHandler;
    }
	
    /**execute a single line.
     * @param expression = the line to execute
     * @return the result as a String
     */
    public String executeExpression(String expression, Activity act, Handler handler)
    {  
    	_mHandler = handler;
    	_act = act;
    	_assetManager = _act.getResources().getAssets();
    	
    	Token.breakHit = false;
    	Token.continueHit = false;
    	Token.loopDepth = 0;
    	
    	expression = expression.replace("\\n", "\n");
    	
        String answer = "";
        Parser p = new Parser(true);
        
        //CCX temp fix for lex and parser issue
        
        //issues with cd, same issue may exist for other commands
        //current lex doesn't understand .. not in a string
        //current parser doesn't understand cd path, only understands cd "path"
        if (expression.trim().startsWith("cd ")) {
        	String tempExp = expression.trim().substring(3).trim();
        	if (tempExp.startsWith("\"") || tempExp.startsWith("'")) {
        		//do nothing
        	} else {
        		tempExp = "\"" + tempExp + "\"";
        		expression = "cd " + tempExp;
        	}
        	//expression = expression.replace('\\', '/');
        }
        if (expression.trim().startsWith("help ")) {
        	String tempExp = expression.trim().substring(5).trim();
        	if (tempExp == "") {
        		tempExp = "\" \"";
        	}
        	expression = "help(\"" + tempExp + "\");";
        }
        if (expression.trim().startsWith("ed ")) {
        	String tempExp = expression.trim().substring(3).trim();
        	expression = "ed(\"" + tempExp + "\");";
        }
        if (expression.trim().startsWith("edit ")) {
        	String tempExp = expression.trim().substring(5).trim();
        	expression = "edit(\"" + tempExp + "\");";
        }

        // if required rehash m-files
        if(runningStandalone)
            globals.getFunctionManager().checkAndRehashTimeStamps();

        try
        {
	        // separate expression into tokens and return tree of expressions
            OperandToken expressionTree = p.parseExpression(expression);

	        // open a tree to show the expression-tree for a parsed command
	        //MathLib.Tools.TreeAnalyser.TreeAnalyser treeAnalyser = new MathLib.Tools.TreeAnalyser.TreeAnalyser( expressionTree );

	        // evaluate tree of expressions
            OperandToken answerToken = null;
            if (expressionTree!=null)
            {
                answerToken = expressionTree.evaluate(null, globals);
            }
			
            //getVariables().listVariables();
        }
        catch(JMathLibException e)
        {
            answer = e.getMessage();
            if (answer.equals("PARSER: CCX: continue") == false) {
               //display currently parse line of code to display
               displayText("??? "+p.getScannedLineOfCode());
            
               //log error information
               ErrorLogger.debugLine("??? "+p.getScannedLineOfCode());
               ErrorLogger.debugLine(answer);
        	
               // save last error to special variable
               Variable var = globals.createVariable("lasterror");
	           var.assign(new CharToken(answer));
            
	           // rethrow errors if enabled
               if (throwErrorsB) throw(e);
            }
        }
        catch(java.lang.Throwable error)
        {
            answer = error.getClass().toString() + " : " + error.getMessage();
			
            // print stack trace: will show the line and file where the error occured
            error.printStackTrace();
            
            if(runningStandalone)
            {
            	ErrorLogger.debugLine( answer );
            }

            // save last error to special variable
            Variable var = globals.createVariable("lasterror");
            var.assign(new CharToken(answer));	
        }

        ErrorLogger.debugLine("Interpreter: done");
        
        _assetManager = null;
        _act = null;
        
        return answer;
    }

    /**get the real part of a scalar variable 
     * @param name = name of the scalar variable
     * @return numerical value of the variable 
     */
    public double getScalarValueRe(String name)
    {
	    // get variable from variable list		 
        OperandToken variableData = globals.getVariable(name).getData(); 
				    	
        // check if variable is already set
        if (variableData == null) return 0.0;
        
        // check if data is a DoubleNumberToken
        if (!(variableData instanceof DoubleNumberToken)) return 0.0;
        
        // cast to number token
        DoubleNumberToken number = (DoubleNumberToken)(variableData.clone());
                
        if (number.isScalar())
        	return number.getValueRe();
        else
        	return 0.0;
    }

    /**get the imaginary part of a scalar variable 
     * @param name = name of the scalar variable
     * @return numerical value of the variable 
     */
    public double getScalarValueIm(String name)
    {
		// get variable from variable list		 
        OperandToken variableData = globals.getVariable(name).getData(); 
        		    	
        // check if variable is already set
        if (variableData == null) return 0.0;
        
        // check if data is a DoubleNumberToken
        if (!(variableData instanceof DoubleNumberToken)) return 0.0;
        
        // cast to number token
        DoubleNumberToken number = (DoubleNumberToken)(variableData.clone());
                
        if (number.isScalar())
        	return number.getValueIm();
        else
        	return 0.0;
    }

    /**
     * 
     * @param name
     * @return
     */
    public boolean getScalarValueBoolean(String name)
    {
        // get variable from variable list       
        OperandToken variableData = globals.getVariable(name).getData(); 
                        
        // check if variable is already set
        if (variableData == null) return false;
        
        // check if data is a LogicalToken
        if (!(variableData instanceof LogicalToken)) return false;
        
        // cast to number token
        LogicalToken l = (LogicalToken)(variableData.clone());
                
        if (l.isScalar())
            return l.getValue(0);
        else
            return false;
    }

    /**get the real values of a an array 
     * @param name = name of the array
     * @return numerical value of the array 
     */
    public double[][] getArrayValueRe(String name)
    {
        // get variable from variable list		 
        OperandToken variableData = globals.getVariable(name).getData(); 
				    	
        // check if variable is already set
        if (variableData == null) return null;
        
        // check if data is a DoubleNumberToken
        if (!(variableData instanceof DoubleNumberToken)) return null;
        
        // cast to number token
        DoubleNumberToken number = (DoubleNumberToken)(variableData.clone());
                
       	return number.getReValues();
    }
    
    /**get the imaginary values of a an array 
     * @param name = name of the array
     * @return numerical value of the array 
     */
    public double[][] getArrayValueIm(String name)
	{
        // get variable from variable list		 
        OperandToken variableData = globals.getVariable(name).getData(); 
				    	
        // check if variable is already set
        if (variableData == null) return null;
        
        // check if data is a DoubleNumberToken
        if (!(variableData instanceof DoubleNumberToken)) return null;
        
        // cast to number token
        DoubleNumberToken number = (DoubleNumberToken)(variableData.clone());
                
       	return number.getValuesIm();
    }

    /**
     * 
     * @param name
     * @return
     */
    public boolean[][] getArrayValueBoolean(String name)
    {
        // get variable from variable list       
        OperandToken variableData = globals.getVariable(name).getData(); 
                        
        // check if variable is already set
        if (variableData == null) return null;
        
        // check if data is a DoubleNumberToken
        if (!(variableData instanceof LogicalToken)) return null;
        
        // cast to logical token
        LogicalToken l = (LogicalToken)(variableData.clone());
                
        return l.getValues();
    }

    /** store a scalar variable in jmathlib's workspace
     * @param name    = name of the scalar
     * @param valueRe = real value of the scalar
     * @param valueIM = imaginary value of the scalar
     */
    public void setScalar(String name, double valueRe, double valueIm)
    {
        // Create variable. In case variable is already created it will
        // return the current variable
        Variable answervar =  globals.createVariable(name);

        // assign value to variable
        answervar.assign(new DoubleNumberToken(valueRe, valueIm));
    }
    
    /** Store an array variable in jmathlib's workspace
     * @param name    = name of the array
     * @param valueRe = real values of the array
     * @param valueIM = imaginary values of the array
     */
    public void setArray(String name, double[][] valueRe, double[][] valueIm)
    {
        // Create variable. In case variable is already created it will
        // return the current variable
        Variable answervar = globals.createVariable(name);

        // assign value to variable
        answervar.assign(new DoubleNumberToken(valueRe, valueIm));
    }

    /** Return the result of the last calculation
     * @return a string containing the last result
     */
    public String getResult()
    {
        // get variable from variable list		 
        OperandToken variableData = globals.getVariable("ans").getData(); 
				    	
        // check if variable is already set
        if (variableData == null) return "";

        return variableData.toString();               
    }

    /** Return the result of the last calculation
     * @return a string containing the last result
     */
    public String getString(String name)
    {
        // get variable from variable list      
        OperandToken variableData = globals.getVariable(name).getData(); 
                        
        // check if variable is already set
        if (variableData == null) return "";
        
        // check if data is a CharToken
        if (!(variableData instanceof CharToken)) return "";
        
        // return string
        return ((CharToken)(variableData.clone())).getValue();
                
    }
    
	public static String readAsset(String asset) {

        // Programmatically load text from an asset and place it into the
        // text view.  Note that the text we are loading is ASCII, so we
        // need to convert it to UTF-16.
        try {
            InputStream is = _assetManager.open(asset);

            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            String text = new String(buffer);
            
            return text;

        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
	}
	
	public static String readPackageAsset(String pack, String asset) {

		Context ctx = null;
		try {
			ctx = _act.createPackageContext("com." + pack, 4);
		} catch (NameNotFoundException e1) {
			displayText("You need to install the Addi package named " + pack);
			displayText("PROMPTTOINSTALL=" + pack);
			return null;
		}  //CONTEXT_RESTRICTED
		AssetManager assetManager = ctx.getAssets();
		
        // Programmatically load text from an asset and place it into the
        // text view.  Note that the text we are loading is ASCII, so we
        // need to convert it to UTF-16.
        try {
            InputStream is = assetManager.open(asset);

            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            String text = new String(buffer);
            
            return text;

        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
	}

	public static void setCacheDir(File cacheDirIn) {
		cacheDir = cacheDirIn;
	}

	public static File getCacheDir() {
		return cacheDir;
	}
	
}

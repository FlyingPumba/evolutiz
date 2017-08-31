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


import java.io.*;
import java.net.*;

import android.app.Activity;

import com.addi.core.interpreter.*;
import com.addi.*;


/**Class for storing and managing the m- and p-functions*/
public class MFileLoader extends RootObject
{

    boolean pFileCachingEnabledB = false;
    
    /**Default constructor*/
    public MFileLoader() 
    {

    }

    /**loads an .m-file via the web
    @param directory = the directory containing the file
    @param mFileName = the name of the m file
    @return the result of the file as a FunktionToken*/
    public UserFunction loadMFileViaWeb(URL codeBase, String directoryAndFile, String mFileName)
    {
        String       code     = "";
        UserFunction function = null;
     
        ErrorLogger.debugLine("MFileLoader: loading >"+mFileName+".m<");
        
        // open file and read m-file line by line
        try 
        {          
            URL            url      = new URL(codeBase, directoryAndFile);
            InputStream    in       = url.openStream();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = inReader.readLine()) != null)
            {               
                code += line + "\n";
            }
            inReader.close();
        }
        catch (Exception e)
        {
            Errors.throwMathLibException("MFileLoader: m-file exception via web");
        }          
 
        ErrorLogger.debugLine("MFileLoader: code: begin \n"+code+"\ncode end");
        
        // send code to function parser and return function
        FunctionParser funcParser = new FunctionParser();
        function = funcParser.parseFunction(code);

        // set name of user function
        // remember: the name of the called function could be different compared
        // to the function's name inside the m-file
        function.setName(mFileName);
 
        ErrorLogger.debugLine("MFileLoader: finished webloading >" + mFileName + ".m<");

        return function;
     }
    
    public UserFunction loadMFile(File file) {
      return loadMFile(file.getParent(), file.getName());
    }
    
    public UserFunction loadPFile(File file) {
      return loadPFile(file.getParent(), file.getName());
    }    
    
    /**reads in an .m-file
    @param directory = the directory containing the file
    @param mFileName = the name of the m file
    @return the result of the file as a FunktionToken*/
 public UserFunction loadBuiltInMFile(String mFileName, String mFilePath)
 {
     String       code         = "";
     UserFunction function     = null;
     String       fullFileName = mFilePath;
     
     ErrorLogger.debugLine("MFileLoader: loading >"+mFilePath);
 
     // open file and read m-file line by line
     try 
     {                 
    	code = com.addi.core.interpreter.Interpreter.readAsset(mFilePath);
     }
     catch (Exception e)
     {
    	 Errors.throwMathLibException(ERR_FUNCTION_NOT_FOUND, new Object[] {mFileName});
     }          
 
     ErrorLogger.debugLine("MFileLoader: code: begin \n"+code+"\ncode end");
         
     // send code to function parser and return function
     FunctionParser funcParser = new FunctionParser();
     function = funcParser.parseFunction(code);

     // set name of user function
     // remember: the name of the called function could be different compared
     // to the function's name inside the m-file
     function.setName(mFileName);
     
     // set date of file as it has been found on the disc
     function.setLastModified(0);
     
     // set filename and full path to the function
     function.setPathAndFileName(fullFileName);
 
     ErrorLogger.debugLine("MFileLoader: finished loading >" + mFileName + ".m<");

     return function;
 }
 
 /**reads in an .m-file
 @param directory = the directory containing the file
 @param mFileName = the name of the m file
 @return the result of the file as a FunktionToken*/
public UserFunction loadPackageMFile(String mFileName, String mFilePath)
{
  String       code         = "";
  UserFunction function     = null;
  String [] splitStr =  mFilePath.split("\\.");
  String fullFileName = splitStr[0] + ".m";
  String pack = splitStr[1];
  
  ErrorLogger.debugLine("MFileLoader: loading >"+mFilePath);

  // open file and read m-file line by line
  try 
  {                 
 	code = com.addi.core.interpreter.Interpreter.readPackageAsset(pack, fullFileName);
  }
  catch (Exception e)
  {
 	 Errors.throwMathLibException(ERR_FUNCTION_NOT_FOUND, new Object[] {mFileName});
  }
  if (code == null) {
	  Errors.throwMathLibException(ERR_FUNCTION_NOT_FOUND, new Object[] {mFileName});
  }

  ErrorLogger.debugLine("MFileLoader: code: begin \n"+code+"\ncode end");
      
  // send code to function parser and return function
  FunctionParser funcParser = new FunctionParser();
  function = funcParser.parseFunction(code);

  // set name of user function
  // remember: the name of the called function could be different compared
  // to the function's name inside the m-file
  function.setName(mFileName);
  
  // set date of file as it has been found on the disc
  function.setLastModified(0);
  
  // set filename and full path to the function
  function.setPathAndFileName(fullFileName);

  ErrorLogger.debugLine("MFileLoader: finished loading >" + mFileName + ".m<");

  return function;
}


    /**reads in an .m-file
       @param directory = the directory containing the file
       @param mFileName = the name of the m file
       @return the result of the file as a FunktionToken*/
    public UserFunction loadMFile(String directory, String mFileName)
    {
        String       code         = "";
        UserFunction function     = null;
        String       fullFileName = directory + File.separator + mFileName;
        File         file         = null;
        
        ErrorLogger.debugLine("MFileLoader: loading >"+mFileName+".m<");
    
        // open file and read m-file line by line
        try 
        {          
            file = new File (fullFileName);
            BufferedReader inReader = new BufferedReader( new FileReader(file));
            String line;
            while ((line = inReader.readLine()) != null)
            {               
               code += line + "\n";
            }
            inReader.close();
        }
        catch (Exception e)
        {
        	Errors.throwMathLibException(ERR_FUNCTION_NOT_FOUND, new Object[] {mFileName});
        }          
    
        ErrorLogger.debugLine("MFileLoader: code: begin \n"+code+"\ncode end");
            
        // send code to function parser and return function
        FunctionParser funcParser = new FunctionParser();
        function = funcParser.parseFunction(code);

        // set name of user function
        // remember: the name of the called function could be different compared
        // to the function's name inside the m-file
        function.setName(mFileName);
        
        // set date of file as it has been found on the disc
        function.setLastModified(file.lastModified());
        
        // set filename and full path to the function
        function.setPathAndFileName(fullFileName);
        
        // check if p-file caching is enabled
        if (pFileCachingEnabledB)
        {
            //serialise the function as a p-file
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(
                     new FileOutputStream(directory + File.separator + mFileName + ".p"));
                out.writeObject(function);
            }
            catch(Exception e)
            {
                Errors.throwMathLibException("Error serialising function: " + e);
                e.printStackTrace();
            }
        }
    
        ErrorLogger.debugLine("MFileLoader: finished loading >" + mFileName + ".m<");

        return function;
    }
    
    /**reads in an .p-file
       @param directory = the directory containing the file
       @param pFileName = the name of the p file
       @return the result of the file as a FunktionToken*/
    public UserFunction loadPFile(String directory, String pFileName)
    {
        UserFunction function = null;

        ErrorLogger.debugLine("MFileLoader: loading >" + pFileName + ".p<");

        try
        {
            ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(directory + File.separator + pFileName + ".p"));
            function = ((UserFunction)in.readObject());
            ErrorLogger.debugLine("MFileLoader: code: " + function);
        }
        catch(Exception e)
        {
            ErrorLogger.debugLine("Error reading serialised function: " + e);
            e.printStackTrace();
        }
    
        ErrorLogger.debugLine("MFileLoader: finished loading >" + pFileName + ".p<");

        return function;
    }
    
    /** set caching of p-file to on of off
     * 
     * @param pFileCaching  true= caching of p-files on; false: caching of p-files off
     */
    public void setPFileCaching(boolean pFileCaching)
    {
        pFileCachingEnabledB = pFileCaching;
    }

    /** return whether of not caching of p-files is enabled of not 
     * 
     * @return status of caching p-files
     */
    public boolean getPFileCaching()
    {
        return pFileCachingEnabledB;
    }

}

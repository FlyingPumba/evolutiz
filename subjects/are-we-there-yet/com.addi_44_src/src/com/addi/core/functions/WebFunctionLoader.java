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
import java.util.*;

import com.addi.core.interpreter.*;


/**Class for storing and managing the m- and p-functions  */
public class WebFunctionLoader extends FunctionLoader
{

    boolean pFileCachingEnabledB = false;

    private String directory; 
    
    // paths to external function for applet usage
    private Vector functionListV = new Vector(30);  
    
    /**Default constructor*/
    public WebFunctionLoader()
    {
        
        try 
        {
            //if (codeBase!=null) 
            //    System.out.println("WebFunctionLoader: new url"+ codeBase.toString());
            //URL url = new URL(codeBase, "webFunctionsList.dat");
            //InputStream in = url.openStream();
            
            // load list relative to the path of THIS class.
            InputStream    in = WebFunctionLoader.class.getResourceAsStream("../../../webFunctionsList.dat");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            // read each line of the functions list
            String line = null;
            while ((line = br.readLine()) != null) 
            {
                System.out.println("read =" + line);
                if (!line.startsWith("#")) 
                    functionListV.addElement(line);
            }
        } 
        catch (Exception ex) {
            //ErrorLogger.debugLine("FunctionManager: applet error");
            ex.printStackTrace();
        }

    }
    
    /**
     * 
     * @return
     */
    //public URL getCodeBase() 
    //{
    //    return codeBase;
    //}
    
    /**
     * 
     * @return
     */
    public String getDirectory() 
    {
        return directory;
    }

    /**
     * loads an .m-file via the web
     * @param directory = the directory containing the file
     * @param mFileName = the name of the m file
     * @return the result of the file as a FunktionToken
     */
    public Function findFunction(String functionName)
    {
        //JMH TBD find functions on the classpath
        
        String       code     = "";
        UserFunction function = (UserFunction)getCachedFunction(functionName);
        if (function != null)
            return function;
     
        ErrorLogger.debugLine("WebFunctionLoader: loading >"+functionName+".m<");
        
        boolean foundClassFileB = false;
        boolean foundMFileB     = false;
        String  functionPath    = null;
        
        // Search filelist for wanted function 
        for (int i=0; i<functionListV.size(); i++)
        {
            // The searchstring is set to "/foo.class" to avoid matches
            //  of substring of some other function like "barfoo.class"
            functionPath = ((String)functionListV.elementAt(i));
            
            // !! Remark: the command toLowerCase() is necessary, because some filenames have upper case letters (e.g. Disp.class)
            if ( functionPath.endsWith("/"+functionName+".class") )
            {
                foundClassFileB = true; // indicate that class file was found
                functionPath = functionPath.substring(0,functionPath.length()-6);
                functionPath = functionPath.replace('/',  '.');
                functionPath = functionPath.replace('\\', '.');
                ErrorLogger.debugLine("WebFunctionLoader found "+functionPath);
                break;
            }

            if ( functionPath.endsWith("/"+functionName+".m") )
            {
                // functionsPath contains the path AND the m-filename
                foundMFileB = true;  // indicate that m-file was found
                ErrorLogger.debugLine("WebFunctionLoader found "+functionPath);
                break;
            }
        } // end for
        
        Function func;
        
         // check if class was found and load it
         try
         {
            if (foundClassFileB)
            {
                Class extFunctionClass = Class.forName(functionPath);
                //Class extFunctionClass = Class.forName("MathLib.Functions.Matrix.ones");
                Object funcObj = extFunctionClass.newInstance();
                
                func = ((Function)funcObj);     
                return func;        
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    
    
        ErrorLogger.debugLine("WebFunctionLoader: webloader");
        // check if m-file was found and load it
        if (foundMFileB)
        {   
            // load .m-file. could be script- or function-file
            //UserFunction userFunc = mLoader.loadMFileViaWeb(applet.getCodeBase(), 
            //                                                functionPath, 
            //                                                funcName);

            // open file and read m-file line by line
            try 
            {          
                ErrorLogger.debugLine("WebFunctionLoader: "+directory+"/"+functionName+".m");
                //ErrorLogger.debugLine("WebFunctionLoader: "+codeBase+" "+directory+"/"+functionName+".m");
                //URL            url      = new URL(codeBase, directory+"/"+functionName+".m");
                //URL            url      = new URL(codeBase, functionPath);
                //InputStream    in       = url.openStream();
                
                // load class or resource relative to the path of THIS class.
                InputStream    in       = WebFunctionLoader.class.getResourceAsStream("../../../"+functionPath);
                BufferedReader inReader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = inReader.readLine()) != null)
                {               
                    ErrorLogger.debugLine("WebFunctionLoader: "+line);
                    code += line + "\n";
                }
                inReader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Errors.throwMathLibException("WebFunctionLoader: m-file exception via web");
                
            }          
     
            ErrorLogger.debugLine("WebFunctionLoader: code: begin \n"+code+"\ncode end");
            
            // send code to function parser and return function
            FunctionParser funcParser = new FunctionParser();
            function = funcParser.parseFunction(code);
    
            // set name of user function
            // remember: the name of the called function could be different compared
            // to the function's name inside the m-file
            function.setName(functionName);
            
            //addUserFunction(userFunc);
            cacheFunction(function);
     
            ErrorLogger.debugLine("WebFunctionLoader: finished webloading >" + functionName + ".m<");
    
            return function;
        }
        
        return null;
     }
    
    
    /** 
     * set caching of p-file to on of off
     * @param pFileCaching  true= caching of p-files on; false: caching of p-files off
     */
    public void setPFileCaching(boolean pFileCaching)
    {
        pFileCachingEnabledB = pFileCaching;
    }

    /** 
     * return whether of not caching of p-files is enabled of not 
     * @return status of caching p-files
     */
    public boolean getPFileCaching()
    {
        return pFileCachingEnabledB;
    }
    
    public void checkAndRehashTimeStamps() {
      //TODO: Add timestamp checks
    }
}
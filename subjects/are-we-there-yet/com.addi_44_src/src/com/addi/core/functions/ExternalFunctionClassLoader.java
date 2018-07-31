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

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import com.addi.core.interpreter.*;

/**Class to load any External functions used*/
public class ExternalFunctionClassLoader extends ClassLoader
{
    private FunctionPathBroker pathBroker;

    /**Create the class loader
       @param _classDir = the name of the base class directory*/
    public ExternalFunctionClassLoader(FunctionPathBroker _pathBroker)
    {
        pathBroker = _pathBroker;        
    }

    /** Returns a class name that refers to the functionName.
     * The class name (rather than simply the function name) must be used when calling any
     * Classloader methods.
     */
    public String getClassnameForFunction(String functionName) {
      File classFile = pathBroker.findFunction(functionName);
      if (classFile != null) {
        String baseDir = pathBroker.getBaseDirectory().toString();            
        String className = classFile.toString();
        className = className.substring(baseDir.length() + 1, className.length());
        className = className.replace('/',  '.');
        className = className.replace('\\', '.');
        if (className.toLowerCase().endsWith(".class"))
          className = className.substring(0, className.length()-6);        
        return className;
      }
      return null;
    }
    
    /* checks the specified directory for the class file, if this fails it
    calls the system file to load the class
    @param className = the class for to look for*/  
    public Class findClass(String className) throws ClassNotFoundException
    {
        ErrorLogger.debugLine("ext func loader loadClass "+ className);
        Class newClass = null;

        // try loading the class
        try
        {
            File baseDir = pathBroker.getBaseDirectory();
            
            String classFilename = className.replace('.', '/');
            classFilename = classFilename + ".class";
            
            File classFile = new File(baseDir, classFilename);            
            
            byte[] classData = getClassData(classFile);
            
            if (classData != null) {
              newClass = this.defineClass(className, classData, 0, classData.length);
            }
        }
        catch(Exception readError)
        {
          throw new ClassNotFoundException(className);
        }
        
        return newClass;
    }

    /**read the data for the class from the specified directory and any subdirectories
    @param className = the class to load*/
    protected byte[] getClassData(File classFile) throws IOException
    {
        byte[] classData = null;
        
        if (classFile == null)
        {
            ErrorLogger.debugLine("ext func loader: NOTFOUND");
            return null;
        }
        else if (!classFile.getName().endsWith(".class")){            
            ErrorLogger.debugLine("ext func loader: Non-class file attempted load");
            return null;
        }else
        {
            classData = new byte[((int)classFile.length())];
            
            //now open the input stream
            FileInputStream inFile = new FileInputStream(classFile);
            try {            
              inFile.read(classData);
            } finally {
              inFile.close();
            }
        }
        
        return classData;
    }
}

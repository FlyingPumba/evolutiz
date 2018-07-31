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

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import com.addi.core.interpreter.*;

/**Class to load a Functions (whether they are compiled into class form, mFile or pFile) from a particular directory structure.
 */
public class FileFunctionLoader extends FunctionLoader
{    
    
    // 
    private FunctionPathBroker pathBroker;
    
    //
    private ExternalFunctionClassLoader classLoader;

    //
    private MFileLoader mLoader;
    
    // 
    private Vector paths = new Vector();

    protected FileFunctionLoader(File _functionDir, boolean _traverseChildren, boolean _isSystemLoader)
    {
        super(_isSystemLoader);
        
        pathBroker  = new FunctionPathBroker(_functionDir, _traverseChildren);

        classLoader = new ExternalFunctionClassLoader(pathBroker);
        
        mLoader     = new MFileLoader();
    }
    /**Create the class loader
       @param _functionDir = the name of the base class directory*/
    public FileFunctionLoader(File _functionDir, boolean _traverseChildren)
    {      
      this(_functionDir, _traverseChildren, false);
    }
    
    public File getBaseDirectory() 
    {
        return pathBroker.getBaseDirectory();
    }
    
    public void setBaseDirectory(File dir) {
        if (!dir.equals(pathBroker.getBaseDirectory())) {
          pathBroker.setBaseDirectory(dir);
          clearCache();
        }       
    }
    
    /** Attempts to find a java class, that has the functionName*/
    public Class findOnlyFunctionClass(String functionName) 
    {
        try {
            String className = classLoader.getClassnameForFunction(functionName);
            return classLoader.loadClass(className);
        } catch (Exception ex) {
            ErrorLogger.debugLine(ex.toString());
        }
        return null;
    }

    /**find unknown class/m-file in directory structure
       @param fileName = the file to look for*/
    public Function findFunction(String functionName)
    {
        ErrorLogger.debugLine("searching for " + functionName);
        // find class- or m-file
        Function func = (Function)getCachedFunction(functionName);
        if (func != null)
            return func;
        
        
        File functionFile = pathBroker.findFunction(functionName);
        
        if (functionFile != null) 
        {
            return parseFunctionFile(functionFile, functionName);
        }
        return null;        
    }
    
    protected Function parseFunctionFile(File functionFile, String functionName) {
        Function func = null;
        String fileName = functionFile.getName();
        if (fileName.endsWith(".m"))
        {
            func = mLoader.loadMFile(functionFile.getParent(), functionName);
            cacheFunction(func);
            return func;
        }
        else if (fileName.endsWith(".p")) 
        {
            String pFileName = functionFile.getName();
          
            //Generate a mFile from the pFile
            File mFile = new File(functionFile.getParent(), pFileName.substring(0, pFileName.length()-3));
            //if an mfile exists and is newer than the p file then use that instead
            if(mFile.exists() &&
              (mFile.lastModified() > functionFile.lastModified()) )
            {
                return mLoader.loadMFile(mFile);
            } else 
                return mLoader.loadPFile(functionFile);
        } 
        else if (fileName.endsWith(".class"))
        {
            try {
                String className        = classLoader.getClassnameForFunction(functionName);
                Class  extFunctionClass = classLoader.loadClass(className);
                Object funcObj          = extFunctionClass.newInstance();
                return ((Function)funcObj);                
            } 
            catch (Exception ex) 
            {
                ErrorLogger.debugLine(ex.toString());
            }
        }
        else
            throw new RuntimeException("Coding error");
         
        return null;
    }

    /**
     * 
     */
    public void setPFileCaching(boolean caching) 
    {
        mLoader.setPFileCaching(caching);
    }

    /**
     * 
     */
    public boolean getPFileCaching() 
    {
        return mLoader.getPFileCaching();
    }
    
    /**
     * 
     * @return
     */
    public int getPathCount() 
    {
        return pathBroker.getPathCount();
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public File getPath(int index) 
    {
        return pathBroker.getPath(index);          
    }
    
    public void checkAndRehashTimeStamps() {
        Iterator fns = getCachedFunctionIterator();
        
        while (fns.hasNext()) {
          Function f = (Function)fns.next();
          String file = f.getPathAndFileName();
          if (file != null) {
              File physFile = new File(file);
              if (!physFile.exists())
                  clearCachedFunction(f.getName());
              else if (physFile.lastModified() > f.getLastModified()) {                 
                  f = parseFunctionFile(physFile, f.getName());
                  cacheFunction(f);
              }
          }
        }
    }
}

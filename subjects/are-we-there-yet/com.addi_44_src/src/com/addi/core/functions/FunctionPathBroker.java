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

import java.util.ArrayList;
import java.io.File;

/**
 * Using a base directory, uses a consistent search to find a function by its name
 */
public class FunctionPathBroker 
{
    private File      baseDir = null;
    private ArrayList paths   = new ArrayList();
    private boolean traverseChildren;
    
    public FunctionPathBroker(File _baseDir, boolean _traverseChildren) 
    {
        baseDir = _baseDir;
        traverseChildren = _traverseChildren;
        
        populateSearchPaths();      
    }
    
    private void populateSearchPaths() {
        paths.clear();
        if (traverseChildren) 
        {            
            if (baseDir.exists() && baseDir.isDirectory()) 
            {
              addSearchPath(baseDir);
            }
        }      
    }
    
    public File getBaseDirectory() 
    {
        return baseDir;
    }
    
    public void setBaseDirectory(File dir) {
       this.baseDir = dir;
       
       populateSearchPaths();
    }
    
    public File findFunction(String functionName) {
        File result = findClassOrMFile(baseDir, functionName);
        if(result == null)
        {            
            int size = paths.size();
            for(int index = 0; index < size && (result == null); index++)
            {
                result = findClassOrMFile((File)paths.get(index), functionName);               
            }
        }      
        return result;
    }
    
    /**Searchs a directory for the specified class
        @param path         - the directory to search
        @param functionName - the function to search for
        
        @return a File object representing the full path to the file that matches the fileName
      */
    private File findClassOrMFile(File path, String functionName)
    {
        //System.out.println("file search: "+fileName);
        File[] files = path.listFiles();
        
        File result = null;
        
        // only check non-empty directories
        if (files != null)
        {
            for(int fileNo = 0; fileNo < files.length; fileNo++)
            {
                String fileName = files[fileNo].getName();
               
                int index = fileName.lastIndexOf(".");
                //System.out.println("file: "+temp);
                if(index > -1)
                {                    
                    String tempFunction = fileName.substring(0, index);
                    if(tempFunction.equals(functionName) &&
                       (fileName.equals(functionName+".m")   ||
                        fileName.equals(functionName+".p")   ||
                        fileName.equals(functionName+".class") ) )
                    {
                        result = files[fileNo];
                        break;
                    }
                }
            }
        }
        return result;
    }    
    
    /**
     * build up the list of directories to search for functions
     */
    private void addSearchPath(File path)
    {
        String[] files = path.list();

        if(files != null)
        {
            for(int fileNo = 0; fileNo < files.length; fileNo++)
            {               
                String newPath = path + File.separator + files[fileNo];
                File temp = new File(newPath);
                if(temp.isDirectory() && newPath.indexOf("_private") == -1)
                {                    
                    paths.add(temp);
                    addSearchPath(temp);
                }
            }
        }
    }   
    
    /**
     * 
     * @return
     */
    public int getPathCount() 
    {
        return paths.size();
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public File getPath(int index) 
    {
        return (File)paths.get(index);
    }
}

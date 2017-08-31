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

import java.util.*;


/** Base class used to find and load a function.
 */
public abstract class FunctionLoader
{    
    private boolean isSystemLoader;
    private HashMap functionCache = new HashMap();
    
    protected FunctionLoader(boolean _isSystemLoader) {
        isSystemLoader = _isSystemLoader;
    }
    
    public FunctionLoader() {this(false);}
    
    
    protected void cacheFunction(Function f) {
      functionCache.put(f.name, f);
    }
    
    protected Function getCachedFunction(String name) {
      return (Function)functionCache.get(name);
    }
    
    protected Iterator getCachedFunctionIterator() {
        return functionCache.values().iterator();
    }
    
    protected void clearCachedFunction(String name) {
        functionCache.remove(name);
    }
    
    public void clearCache() {
      functionCache.clear();
    }
    
    public boolean isSystemLoader() {
      return isSystemLoader;
    }
    
    /**find unknown class/m-file in directory structure
       @param fileName = the file to look for*/
    public abstract Function findFunction(String functionName);
    
    public abstract void setPFileCaching(boolean caching);
    
    public abstract boolean getPFileCaching();
    
    public abstract void checkAndRehashTimeStamps();
    

}

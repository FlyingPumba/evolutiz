package com.addi.toolbox.jmathlib.matrix._private.Jampack;

/**Transposes a matrix*/
public class Transpose
{
    public static Zmat o(Zmat A)
    {
    	int nc = A.nr;
    	int nr = A.nc;
    	
    	Zmat result = new Zmat(nr, nc);
    	
    	for(int i = 0; i < nr; i++)
    	{
    		for(int j = 0; j < nc; j++)
    		{
    			result.put(i, j, A.get(j, i));
    			
    		}
    	}
    	
    	return result;
    }
}

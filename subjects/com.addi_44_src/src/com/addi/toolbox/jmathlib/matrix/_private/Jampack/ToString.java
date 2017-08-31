package com.addi.toolbox.jmathlib.matrix._private.Jampack;

/**
   converts numbers, matrices, and arrays to a string.  All floating-point
   numbers are printed in e format with field width w and d figures
   to the left of the right of the decimal point.  For the defaults
   see <a href="Parameters.html"> Parameters </a>.

*/
public class ToString
{
	static public String o(int val)
	{
		return Integer.toString(val);
	}

	static public String o(double val)
	{
		return Double.toString(val);
	}	

	static public String o(Z val)
	{
		return val.toString();
	}
	
	static public String o(Zmat val)
	{
		String result = null;
		if(val.nr == 1 && val.nc == 1)
		{
			result = val.get0(0,0).toString();
		}
		else
		{
			StringBuffer buffer = new StringBuffer(20);
			int sizeY = val.nrow;
			int sizeX = val.ncol;
			for(int yy = 0; yy < sizeY; yy++)
			{
				buffer.append("[");
				for(int xx = 0; xx < sizeX; xx++)
				{
					buffer.append(val.get0(yy, xx).toString());
					
    				if(xx < sizeX - 1)
					   buffer.append(",");
				}			
				buffer.append("]\n");
			}
			result = new String(buffer);
		}			
		return result;
	}	
}

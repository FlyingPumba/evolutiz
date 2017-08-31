package com.addi.toolbox.jmathlib.matrix._private.Jampack;

/**Set of functions for performing standard maths functions
on a matrix*/
public class Maths
{
	/**Standard functions - calculates the absolute value
    @return the result as an OperandToken*/
    public static Zmat abs(Zmat a)
    {
		Zmat X = new Zmat(a.nr, a.nc);
		Z[][] C = X.getZ();
		for (int yy=0; yy<a.nr; yy++)
		{
			for (int xx=0; xx<a.nc; xx++)
			{
				C[yy][xx] = new Z(a.get(yy, xx).abs());
			}
		}
        return X;   	
    }

	/**Standard functions - calculates the exponent
    @return the result as an OperandToken*/
    public static Zmat exp(Zmat a)
    {
		Zmat X = new Zmat(a.nr, a.nc);
		Z[][] C = X.getZ();
		for (int yy=0; yy<a.nr; yy++)
		{
			for (int xx=0; xx<a.nc; xx++)
			{
				C[yy][xx] = a.get(yy, xx).exp();
			}
		}
        return X;   	
    }

	/**Standard functions - calculates the natural logarythm
    @return the result as an OperandToken*/
    public static Zmat ln(Zmat a)
    {
		Zmat X = new Zmat(a.nr, a.nc);
		Z[][] C = X.getZ();
		for (int yy=0; yy<a.nr; yy++)
		{
			for (int xx=0; xx<a.nc; xx++)
			{
				C[yy][xx] = a.get(yy, xx).log();
			}
		}
        return X;   	
    }

	/**Standard functions - calculates the logarythm
	@param arg = the base to calculate the log to
    @return the result as an OperandToken*/
    public static Zmat log(Zmat a, Zmat b)
    {        
		Zmat X = new Zmat(a.nr, a.nc);
		Z[][] C = X.getZ();
		for (int yy=0; yy<a.nr; yy++)
		{
			for (int xx=0; xx<a.nc; xx++)
			{
				C[yy][xx] = a.get(yy, xx).log().Div(b.get(yy, xx).log());
			}
		}
        return X;   	    	
    }

	/**Standard functions - rounds the value down
	@param arg = the base to calculate the log to
    @return the result as an OperandToken*/
    public static Zmat floor(Zmat a)
    {        
		double[][] real = a.getRe();
		double[][] imag = a.getIm();
		for (int yy=0; yy<a.nr; yy++)
		{
			for (int xx=0; xx<a.nc; xx++)
			{
				real[yy][xx] = java.lang.Math.floor(real[yy][xx]);
				imag[yy][xx] = java.lang.Math.floor(imag[yy][xx]);
			}
		}
		Zmat X = null;
		try{X = new Zmat(real, imag);}	catch(JampackException e){}
        return X;   	    	
    }

	/**Standard functions - rounds the value up
	@param arg = the base to calculate the log to
    @return the result as an OperandToken*/
    public static Zmat ceil(Zmat a)
    {        
		double[][] real = a.getRe();
		double[][] imag = a.getIm();
		for (int yy=0; yy<a.nr; yy++)
		{
			for (int xx=0; xx<a.nc; xx++)
			{
				real[yy][xx] = java.lang.Math.ceil(real[yy][xx]);
				imag[yy][xx] = java.lang.Math.ceil(imag[yy][xx]);
			}
		}
		Zmat X = null;
		try{X = new Zmat(real, imag);}	catch(JampackException e){}
        return X;   	    	
    }

	/**Standard functions - rounds the value to the nearest int
	@param arg = the base to calculate the log to
    @return the result as an OperandToken*/
    public static Zmat round(Zmat a)
    {        
		double[][] real = a.getRe();
		double[][] imag = a.getIm();
		for (int yy=0; yy<a.nr; yy++)
		{
			for (int xx=0; xx<a.nc; xx++)
			{
				real[yy][xx] = java.lang.Math.rint(real[yy][xx]);
				imag[yy][xx] = java.lang.Math.rint(imag[yy][xx]);
			}
		}
		Zmat X = null;
		try{X = new Zmat(real, imag);}	catch(JampackException e){}
        return X;   	    	
    }
}	
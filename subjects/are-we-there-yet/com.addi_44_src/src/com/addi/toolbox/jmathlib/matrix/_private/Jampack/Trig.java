package com.addi.toolbox.jmathlib.matrix._private.Jampack;

/**Set of functions for doing trigonometric calculations*/
public class Trig
{
	/**calculates the sine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat sin(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].sin();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the cosine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat cos(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].cos();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the tangent of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat tan(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].tan();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the arc sine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat asin(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].asin();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the arc cosine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat acos(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].acos();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the arc tangent of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat atan(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].atan();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }
    
	/**calculates the hyperbolic sine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat sinh(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].sinh();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the hyperbolic cosine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat cosh(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].cosh();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the hyperbolic tangent of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat tanh(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].tanh();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the hyperbolic arc sine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat asinh(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].asinh();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the hyperbolic arc cosine of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat acosh(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].acosh();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }

	/**calculates the hyperbolic arc tangent of all the elements in the matrix
	@return the results as a new matrix*/
    public static Zmat atanh(Zmat A)
    {
		Z[][] C = new Z[A.nr][A.nc];
		Z[][] values = A.getZ();

		for (int yy=0; yy < A.nr; yy++)
		{
			for (int xx=0; xx < A.nc; xx++)
			{
				C[yy][xx] = values[yy][xx].atanh();
			}
		}
		Zmat X = new Zmat(C);
		return X;
    }    
}
package com.addi.toolbox.jmathlib.matrix._private.Jampack;

public class Times{

/**
   Computes the product of a Z and a Zmat.
   @param     z  The complex scalar
   @param     A  The Zmat
   @return    zA
*/

    public static Zmat o(Z z, Zmat A)
    {

      Zmat B = new Zmat(A.nrow, A.ncol);
      for (int i=0; i<A.nrow; i++)
         for (int j=0; j<A.ncol; j++){
            B.re[i][j] = z.re*A.re[i][j] - z.im*A.im[i][j];
            B.im[i][j] = z.im*A.re[i][j] + z.re*A.im[i][j];
      }
      return B;
   }
/**
   Computes the product of two Zmats.
   @param     A  The first Zmat
   @param     B  The second Zmat
   @return    AB
   @exception JampackException for unconformity
*/

   public static Zmat o(Zmat A, Zmat B)
   throws JampackException{
      if (A.ncol != B.nrow)
         throw new JampackException("Unconformity in product");
      Zmat C = new Zmat(A.nrow, B.ncol);
         for (int i=0; i<A.nrow; i++)
            for (int k=0; k<A.ncol; k++)
               for (int j=0; j<B.ncol; j++){
                  C.re[i][j] = C.re[i][j] + A.re[i][k]*B.re[k][j]
                                          - A.im[i][k]*B.im[k][j];
                  C.im[i][j] = C.im[i][j] + A.im[i][k]*B.re[k][j]
                                          + A.re[i][k]*B.im[k][j];
      }
      return C;
   }
}

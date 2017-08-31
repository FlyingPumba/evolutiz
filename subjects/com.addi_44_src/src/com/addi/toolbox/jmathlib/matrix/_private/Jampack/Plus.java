package com.addi.toolbox.jmathlib.matrix._private.Jampack;

/**
   Plus Computes the sum of two matrices.

   @version Pre-alpha
   @author G. W. Stewart
*/

public class Plus{

/**
   Computes the sum of two Zmats
   @param     A  The first Zmat
   @param     B  The second Zmat
   @return    A + B
   @exception JampackException
              Thrown for nonconformity.
*/
   public static Zmat o(Zmat A, Zmat B)
   throws JampackException{
      if (A.nrow!=B.nrow || A.ncol != B.ncol){
         throw new JampackException("Matrices not conformable for addition");
      }
      Zmat C = new Zmat(A.nr, A.nc);

      for (int i=0; i<A.nrow; i++)
         for (int j=0; j<A.ncol; j++){
            C.re[i][j] = A.re[i][j] + B.re[i][j];
            C.im[i][j] = A.im[i][j] + B.im[i][j];
         }
      return C;
   }
}

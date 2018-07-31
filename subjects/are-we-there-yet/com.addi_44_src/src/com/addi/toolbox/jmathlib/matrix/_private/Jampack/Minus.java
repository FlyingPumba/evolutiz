package com.addi.toolbox.jmathlib.matrix._private.Jampack;

/**
   Minus negates a matrix or computes the difference of two
   matrices.

   @version Pre-alpha
   @author G. W. Stewart
*/

public class Minus{

/**
   Computes the difference of two Zmats.
   @param     A   The diminuend
   @param     B   The subtrahend
   @return    A-B
   @exception JampackException
              Thrown if there is a nonconformity.
*/
   public static Zmat o(Zmat A, Zmat B)
   throws JampackException{
      if (A.nrow!=B.nrow || A.ncol != B.ncol)
         throw new JampackException
            ("Matrices not conformable for subtraction");

      Zmat C = new Zmat(A.nrow, A.ncol);

      for (int i=0; i<A.nrow; i++)
         for (int j=0; j<A.ncol; j++){
            C.re[i][j] = A.re[i][j] - B.re[i][j];
            C.im[i][j] = A.im[i][j] - B.im[i][j];
         }
      return C;
   }

/**
   Negates a Zmat
   @param A  The matrix to be negated
   @return   -A
*/
   public static Zmat o(Zmat A)
   {

      Zmat B = new Zmat(A.nrow, A.ncol);

      for (int i=0; i<A.nrow; i++)
         for (int j=0; j<A.ncol; j++){
            B.re[i][j] = -A.re[i][j];
            B.im[i][j] = -A.im[i][j];
         }
      return B;
   }
}

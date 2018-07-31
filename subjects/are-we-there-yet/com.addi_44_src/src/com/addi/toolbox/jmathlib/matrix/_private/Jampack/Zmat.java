package com.addi.toolbox.jmathlib.matrix._private.Jampack;

/** 
   Zmat implements general complex matrix stored in a rectangular
   array class Z.

   @version Pre-alpha
   @author G. W. Stewart
*/

public class Zmat implements java.io.Serializable, java.lang.Cloneable, com.addi.core.constants.ErrorCodes, com.addi.core.constants.TokenConstants
{

/** The number of rows */
    protected int nrow;

/** The number of columns */
    protected int ncol;

/** The base index */
    protected int  basex;

/** The real part of the matrix */
    protected double re[][];

/** The imaginary part of the matrix */
    protected double im[][];

/** True if the matrix has been altered */
    protected boolean dirty;

/** The base index */
    public int bx;

/** The upper row index */
    public int rx;

/** The number of rows */
    public int nr;

/** Points to an LU decompoistion of the matrix
    provided one exists */
    protected Zludpp LU;

/** Points to a Householder QR decompoistion of the matrix
    provided one exists */
    protected Zhqrd HQR;

/** Points to a Cholesky decompoistion of the matrix
    provided one exists */
    protected Zchol CHOL;
    
/** The upper column index */
    public int cx;

/** The number of columns */
    public int nc;
/**
   Creates a Zmat and initializes its real and imaginary
   parts to a pair of arrays.

   @param     re Contains the real part.
   @param     im Contains the imaginary part.
   @exception JampackException if the dimensions of re and im
              do not match
*/

   public Zmat(double re[][], double im[][])
   throws JampackException{
      //Parameters.BaseIndexNotChangeable = true;
      basex = 0;
      nrow = re.length;
      ncol = re[0].length;
      if (nrow != im.length || ncol != im[0].length)
         throw new JampackException
            ("Inconsistent array dimensions");
      getProperties();
      this.re = new double[nr][nc];
      this.im = new double[nr][nc];
      for (int i=0; i<nr; i++)
         for (int j=0; j<nc; j++){
            this.re[i][j] = re[i][j];
            this.im[i][j] = im[i][j];
         }
   }


/**
   Creates a Zmat and initializes it to an array of class Z.
*/

   public Zmat(Z A[][]){
      //Parameters.BaseIndexNotChangeable = true;
      basex = 0;
      nrow = A.length;
      ncol = A[0].length;
      getProperties();
      re = new double[nr][nc];
      im = new double[nr][nc];
      for (int i=0; i<nr; i++)
         for (int j=0; j<nc; j++){
            re[i][j] = A[i][j].re;
            im[i][j] = A[i][j].im;
         }
   }
/**
   Creates a Zmat and initializes its real part to
   to an array of class double.  The imaginary part is
   set to zero.
*/

   public Zmat(double A[][]){
      //Parameters.BaseIndexNotChangeable = true;
      basex = 0;
      nrow = A.length;
      ncol = A[0].length;
      getProperties();
      re = new double[nr][nc];
      im = new double[nr][nc];
      for (int i=0; i<nr; i++)
         for (int j=0; j<nc; j++){
            re[i][j] = A[i][j];
            im[i][j] = 0;
         }
   }

/**
   Creates a Zmat and intitializes it to a Zmat.
*/

   public Zmat(Zmat A){
      //Parameters.BaseIndexNotChangeable = true;
      basex = 0;
      nrow = A.nrow;
      ncol = A.ncol;
      getProperties();
      re = new double[nr][nc];
      im = new double[nr][nc];
      for (int i=0; i<nr; i++)
         for (int j=0; j<nc; j++){
            re[i][j] = A.re[i][j];
            im[i][j] = A.im[i][j];
         }
   }

/**
   Creates a Zmat and initialize it to a Z1.
*/

   public Zmat(Z1 A){
      //Parameters.BaseIndexNotChangeable = true;
      basex = 0;
      nrow = A.n;
      ncol = 1;
      getProperties();
      re = new double[nr][nc];
      im = new double[nr][nc];
      for (int i=0; i<nr; i++){
         re[i][0] = A.re[i];
         im[i][0] = A.im[i];
      }
   }

/**
   Creates a Zmat and initializes it to zero.
*/

   public Zmat(int nrow, int ncol){
      //Parameters.BaseIndexNotChangeable = true;
      basex = 0;
      this.nrow = nrow;
      this.ncol = ncol;
      getProperties();
      re = new double[nr][nc];
      im = new double[nr][nc];
      for (int i=0; i<nr; i++)
         for (int j=0; j<nc; j++){
            re[i][j] = 0;
            im[i][j] = 0;
         }
   }


/**
   Sets the public parameters.
*/

   public void getProperties(){
      bx = basex;
      rx = bx + nrow - 1;
      cx = bx + ncol - 1;
      nr = nrow;
      nc = ncol;
   }

/**
   Returns a copy of the real part of a Zmat.
*/

   public double[][] getRe(){

      double[][] A = new double[nrow][ncol];
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++)
            A[i][j] = re[i][j];
      return A;
   }
      
/**
   Returns a copy of the imaginary part of a Zmat.
*/

   public double[][] getIm(){

      double[][] A = new double[nrow][ncol];
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++)
            A[i][j] = im[i][j];
      return A;
   }
      
/**
   Returns a copy of the real and imaginary parts as a complex array.
*/

   public Z[][] getZ(){

      Z[][] A = new Z[nrow][ncol];
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++)
            A[i][j] = new Z(re[i][j], im[i][j]);
      return A;
   }
      
/**
   Returns the (ii,jj)-element of a Zmat.
   @param ii    The row index of the element
   @param jj    The column index of the element
*/

   public Z get(int ii, int jj){

      return new Z(re[ii][jj],im[ii][jj]);
   }

/**
   Returns the zero-based (i,j)-element of a Zmat.
   @param i  The row index of the element
   @param j  The column index of the element
*/

   public Z get0(int i, int j){

      return new Z(re[i][j],im[i][j]);
   }

/**
   Writes the (ii,jj) element of a Zmat.
   @param ii  The row index of the element
   @param jj  The column index of the element
   @param a   The new value of the element
*/
   public void put(int ii, int jj, Z a){

      dirty = true;
      re[ii][jj] = a.re;
      im[ii][jj] = a.im;
   }

/**
   Writes the zero-based (i,j)-element of a Zmat.
   @param i   The row index of the element
   @param j   The column index of the element
   @param a   The new value of the element
*/
   public void put0(int i, int j, Z a){

      dirty = true;
      re[i][j] = a.re;
      im[i][j] = a.im;
   }

/**
   Returns the submatrix  (ii1:ii2, jj1:jj2).
   @param ii1    The lower column index
   @param ii2    The upper column index
   @param jj1    The lower row index
   @param jj2    The upper row index
*/

   public Zmat get(int ii1, int ii2, int jj1, int jj2){
      int nrow = ii2-ii1+1;
      int ncol = jj2-jj1+1;
      Zmat A = new Zmat(nrow, ncol);
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            A.re[i][j] = re[i+ii1-basex][j+jj1-basex];
            A.im[i][j] = im[i+ii1-basex][j+jj1-basex];
         }
      return A;
   }

/**
   Overwrites the submatrix (ii1:ii2, jj1:jj2) with a Zmat.
   @param ii1    The lower column index
   @param ii2    The upper column index
   @param jj1    The lower row index
   @param jj2    The upper row index
   @param A      The new value of the submatrix
*/

   public void put(int ii1, int ii2, int jj1, int jj2, Zmat A){
      dirty = true;
      int nrow = ii2-ii1+1;
      int ncol = jj2-jj1+1;
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            re[i+ii1-basex][j+jj1-basex]= A.re[i][j];
            im[i+ii1-basex][j+jj1-basex]= A.im[i][j];
      }
   }

/**
   Returns the submatrix  (ii[], jj1:jj2).
   @param i[]    Contains the row indices of the submatrix
   @param jj1    The lower column index
   @param jj2    The upper column index
*/

   public Zmat get(int ii[], int jj1, int jj2){
      int nrow = ii.length;
      int ncol = jj2-jj1+1;
      Zmat A = new Zmat(nrow, ncol);
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            A.re[i][j] = re[ii[i]-basex][j+jj1-basex];
            A.im[i][j] = im[ii[i]-basex][j+jj1-basex];
         }
      return A;
   }

/**
   Overwrites the submatrix (ii[], jj1:jj2) with a Zmat.
   @param i[]    Contains the row indices of the submatrix
   @param jj1    The lower column index
   @param jj2    The upper column index
   @param A      The new value of the submatrix.
*/

   public void put(int ii[], int jj1, int jj2, Zmat A){
      dirty = true;
      int nrow = ii.length;
      int ncol = jj2-jj1+1;
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            re[ii[i]-basex][j+jj1-basex] = A.re[i][j];
            im[ii[i]-basex][j+jj1-basex] = A.im[i][j];
         }
   }

/**
   Returns the submatrix  (ii1:ii2, jj[]).
   @param ii1    The lower row index
   @param ii2    The upper row index
   @param jj[]   Contains the column indices of the submatrix
*/

   public Zmat get(int ii1, int ii2, int jj[]){
      int nrow = ii2-ii1+1;
      int ncol = jj.length;
      Zmat A = new Zmat(nrow, ncol);
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            A.re[i][j] = re[i+ii1-basex][jj[j]-basex];
            A.im[i][j] = im[i+ii1-basex][jj[j]-basex];
         }
      return A;
   }

/**
   Overwrites the submatrix (ii1:ii2, jj[]) with a Zmat.
   @param ii1    The lower row index
   @param ii2    The upper row index
   @param jj[]   Contains the column indices of the submatrix
   @param A      The new value of the submatrix
*/

   public void put(int ii1, int ii2, int jj[], Zmat A){
      dirty = true;
      int nrow = ii2-ii1+1;
      int ncol = jj.length;
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            re[i+ii1-basex][jj[j]-basex] = A.re[i][j];
            im[i+ii1-basex][jj[j]-basex] = A.im[i][j];
         }
   }

/**
   Returns the submatrix  (ii[], jj[]).
   @param ii[]  Contains the row indices of the submatrix
   @param jj[]  Contains the column indices of the submatrix
*/

   public Zmat get(int ii[] , int jj[]){
      int nrow = ii.length;
      int ncol = jj.length;
      Zmat A = new Zmat(nrow, ncol);
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            A.re[i][j] = re[ii[i]-basex][jj[j]-basex];
            A.im[i][j] = im[ii[i]-basex][jj[j]-basex];
         }
      return A;
   }

/**
   Overwrites the submatrix (ii[], jj[])  with a Zmat.
   Returns the submatrix  (ii[], jj[])
   @param ii[]  Contains the row indices of the submatrix
   @param jj[]  Contains the column indices of the submatrix
   @param A     The value of the new submatrix
*/

   public void put(int ii[] , int jj[], Zmat A){
      dirty = true;
      int nrow = ii.length;
      int ncol = jj.length;
      for (int i=0; i<nrow; i++)
         for (int j=0; j<ncol; j++){
            re[ii[i]-basex][jj[j]-basex] = A.re[i][j];
            im[ii[i]-basex][jj[j]-basex] = A.im[i][j];
         }
   }

/**
    Nullifies the history pointers if the matrix is dirty
    and sets the dirty flag to false.
*/

   protected void clean(){
      if (dirty){
         dirty = false;
      }
   }

}


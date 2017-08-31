/* File: Complex.java
 *                             -- A  Java  class  for performing complex
 *                                number arithmetic to double precision.
 *
 * Copyright (c) 1997 - 2001, Alexander Anderson.
 *
 * This  program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published  by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be  useful,  but
 * WITHOUT   ANY   WARRANTY;   without  even  the  implied  warranty  of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR  PURPOSE.   See  the  GNU
 * General Public License for more details.
 *
 * You  should  have  received  a copy of the GNU General Public License
 * along  with  this  program;  if  not,  write  to  the  Free  Software
 * Foundation,  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 */

package com.addi.toolbox.jmathlib.matrix._private.Jampack;

import  java.io.Serializable;
import java.text.NumberFormat;

/**
* <p>
* @version
*     <b>1.0.1</b> <br>
*     <tt>
*     Last change:  ALM  23 Mar 2001    8:56 pm
*     </tt>
* <p>
* A Java class for performing complex number arithmetic to <tt>double</tt>
* precision.
*
* <p>
* <center>
*     <applet
*         name="SeeComplex"
*         archive="SeeComplex.jar"
*         code="SeeComplex.class"
*         codebase="imagery"
*         width="85%"
*         height="85%"
*         align="Middle"
*         alt="SeeZ Applet"
*     >
*         Make yours a Java enabled browser and OS!
*     </applet>
* <p>
* This applet has been adapted<br>from a <a
* href="http://www.pa.uky.edu/~phy211/VecArith/index.html">Vector
* Visualization applet</a> by <a
* href="mailto:Vladimir Sorokin <vsoro00@pop.uky.edu>">Vladimir Sorokin</a>.
* </center>
* <hr>
*
* <p>
* @author               <a HREF="mailto:Alexander Anderson <sandy@almide.demon.co.uk>">Sandy Anderson</a>
* @author               Priyantha Jayanetti
* <p>
* <font color="000080">
* <pre>
*  <b>Copyright (c) 1997 - 2001, Alexander Anderson.</b>
*
*  This  program is free software; you can redistribute it and/or modify
*  it under the terms of the <a href="http://www.gnu.org/">GNU</a> General Public License as published  by
*  the Free Software Foundation; either version 2 of the License, or (at
*  your option) any later version.
*
*  This program is distributed in the hope that it will be  useful,  but
*  WITHOUT   ANY   WARRANTY;   without  even  the  implied  warranty  of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR  PURPOSE.   See  the  GNU
*  General Public License for more details.
*
*  You  should  have  received  a copy of the GNU General Public <a href="GNU_GeneralPublicLicence.html">License</a>
*  along  with  this  program;  if  not,  write  to  the  Free  Software
*  Foundation,  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
*  USA.
* </pre>
* </font>
* <p>
* The latest version of this <tt>Complex</tt> class is available from
* the <a href="http://www.netlib.org/">Netlib Repository</a>.
* <p>
* Here's an example of the style the class permits:<br>
*
* <pre>
*         <b>import</b>  ORG.netlib.math.complex.Complex;<br>
*         <b>public</b> <b>class</b> Test {<br>
*             <b>public boolean</b> isInMandelbrot (Z c, <b>int</b> maxIter) {
*                 Z z= <b>new</b> Z(0, 0);<br>
*                 <b>for</b> (<b>int</b> i= 0; i < maxIter; i++) {
*                     z= z.Times(z).Plus(c);
*                     <b>if</b> (z.abs() > 2) <b>return false</b>;
*                 }<br>
*                 <b>return true</b>;
*             }<br>
*         }
* </pre>
* </dd>
* <p>
* <dd>This class was developed by
*     <a HREF="http://www.almide.demon.co.uk">Sandy Anderson</a> at the
*     School of Electronic Engineering,
*     <a HREF="http://www.mdx.ac.uk/">Middlesex University</a>, UK, and
*     Priyantha Jayanetti at The Power Systems Program, the
*     <a HREF="http://www.eece.maine.edu/">University of Maine</a>, USA.
* </dd>
* <p>
* <dd>And many, many thanks to <a href="mailto:R.D.Hirsch@red-deer.demon.co.uk">Mr. Daniel
*     Hirsch</a>, for his constant advice on the mathematics, his exasperating
*     ability to uncover bugs blindfold, and for his persistent badgering over
*     the exact wording of this documentation.
* </dd>
* <p>
* <dd>For instance, he starts to growl like a badger if you say "infinite set".</dd><br>
* <dd>"Grrr...What's <i>that</i> mean?  <i>Countably</i> infinite?"</dd><br>
* <dd>You think for a while.</dd><br>
* <dd>"Grrr..."</dd><br>
* <dd>"Yes."</dd><br>
* <dd>"Ah! Then you mean <i>infinitely many</i>."</dd><br>
* <p>
**/

public class Z implements Cloneable, Serializable {

    public    static final String          VERSION             =  "1.0.1";
    public    static final String          DATE                =  "Fri 23-Mar-2001 8:56 pm";
    public    static final String          AUTHOR              =  "sandy@almide.demon.co.uk";
    public    static final String          REMARK              =  "Class available from "
                                                                  + "http://www.netlib.org/";
/** Z 1. */
    public static final Z ONE = new Z(1,0);

/** Z 0. */
    public static final Z ZERO = new Z(0,0);

/** Imaginary unit. */
    public static final Z I = new Z(0,1);

    /**stores the number format for displaying the number*/
    private static NumberFormat numFormat = NumberFormat.getInstance();
    /**
    * Switches on debugging information.
    * <p>
    **/
    // protected static       boolean         debug               =  false;

    /**
    * Whilst debugging:  the nesting level when tracing method calls.
    * <p>
    **/
    // private   static       int             trace_nesting       =  0;

    /**
    * Twice <a
    * href="http://cad.ucla.edu/repository/useful/PI.txt"><tt><b>PI</b></tt></a>
    * radians is the same thing as 360 degrees.
    * <p>
    **/
    protected static final double          TWO_PI              =  2.0 * Math.PI;

    /**
    * A constant representing <i><b>i</b></i>, the famous square root of
    * <i>-1</i>.
    * <p>
    * The other square root of <i>-1</i> is - <i><b>i</b></i>.
    * <p>
    **/
    public    static final Z         i                   =  new Z(0.0, 1.0);

    /**The number 1 stored as a complex number
    **/
    public    static final Z One                       =  new Z(1.0, 0);
    
    /**The number 0 stored as a complex number
    **/
    public    static final Z Zero                       =  new Z(0.0, 0);
    // private   static       long            objectCount;                        // !!!


    public                double          re;
    public                double          im;



    //---------------------------------//
    //           CONSTRUCTORS          //
    //---------------------------------//


    /**
     Constructs a <tt>Complex</tt> representing the number zero.
    
     <p>
    **/
    public Z () 
    {
        this(0.0, 0.0);
    }

    /**
    * Constructs a <tt>Complex</tt> representing a real number.
    *
    * <p>
    * @param  re               The real number
    * <p>
    * @see                     Complex#real(double)
    **/
    public Z (double re) 
    {
        this(re, 0.0);
    }

    /**
    * Constructs a separate new <tt>Complex</tt> from an existing
    * <tt>Complex</tt>.
    *
    * <p>
    * @param  z                A <tt>Complex</tt> number
    * <p>
    **/
    public Z (Z z) 
    {
        this(z.re, z.im);
    }

    /**
    * Constructs a <tt>Complex</tt> from real and imaginary parts.
    *
    * <p>
    * <i><b>Note:</b><ul> <font color="000080">All methods in class
    * <tt>Complex</tt> which deliver a <tt>Complex</tt> are written such that
    * no intermediate <tt>Complex</tt> objects get generated.  This means that
    * you can easily anticipate the likely effects on garbage collection caused
    * by your own coding.</font>
    * </ul></i>
    * <p>
    * @param  re               Real part
    * @param  im               Imaginary part
    * <p>
    * @see                     Complex#cart(double, double)
    * @see                     Complex#polar(double, double)
    **/
    public Z (double re, double im) 
    {
        this.re =  re;
        this.im =  im;
        //numFormat.setMaximumFractionDigits(3);
    }

    //---------------------------------//
    //             STATIC              //
    //---------------------------------//



    /**
    * Returns a <tt>Complex</tt> representing a real number.
    *
    * <p>
    * @param  real             The real number
    * <p>
    * @return                  <tt>Complex</tt> representation of the real
    * <p>
    * @see                     Complex#re()
    * @see                     Complex#cart(double, double)
    **/
    public static Z real(double real) 
    {
        return  new Z(real, 0.0);
    }



    /**
    * Returns a <tt>Complex</tt> from real and imaginary parts.
    *
    * <p>
    * @param  re               Real part
    * @param  im               Imaginary part
    * <p>
    * @return                  <tt>Complex</tt> from Cartesian coordinates
    * <p>
    * @see                     Complex#re()
    * @see                     Complex#im()
    * @see                     Complex#polar(double, double)
    * @see                     Complex#toString()
    **/
    public static Z cart (double re, double im) 
    {
        return  new Z(re, im);
    }

    /**
    * Returns a <tt>Complex</tt> from a size and direction.
    *
    * <p>
    * @param  r                Size
    * @param  theta            Direction (in <i>radians</i>)
    * <p>
    * @return                  <tt>Complex</tt> from Polar coordinates
    * <p>
    * @see                     Complex#abs()
    * @see                     Complex#arg()
    * @see                     Complex#cart(double, double)
    **/
    public static Z polar (double r, double theta) 
    {
        if (r < 0.0) 
        {
            theta +=  Math.PI;
            r      =  -r;
        }

        theta =  theta % TWO_PI;

        return  cart(r * Math.cos(theta), r * Math.sin(theta));
    }

    /**
    * Returns the <tt>Complex</tt> base raised to the power of the exponent.
    *
    * <p>
    * @param  base             The base "to raise"
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  base "raised to the power of" exponent
    * <p>
    * @see                     Complex#pow(double, Complex)
    **/
    public static Z pow (Z base, double exponent) 
    {
        // return  base.log().scale(exponent).exp();

        double re =  exponent * Math.log(base.abs());
        double im =  exponent * base.arg();

        double scalar =  Math.exp(re);

        return  cart( scalar * Math.cos(im), scalar * Math.sin(im) );
    }



    /**
    * Returns the base raised to the power of the <tt>Complex</tt> exponent.
    *
    * <p>
    * @param  base             The base "to raise"
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  base "raised to the power of" exponent
    * <p>
    * @see                     Complex#pow(Complex, Complex)
    * @see                     Complex#exp()
    **/
    public static Z pow (double base, Z exponent) 
    {
        // return  real(base).log().Times(exponent).exp();

        double re =  Math.log(Math.abs(base));
        double im =  Math.atan2(0.0, base);

        double re2 =  (re*exponent.re) - (im*exponent.im);
        double im2 =  (re*exponent.im) + (im*exponent.re);

        double scalar =  Math.exp(re2);

        return  cart( scalar * Math.cos(im2), scalar * Math.sin(im2) );
    }

    /**
    * Returns the <tt>Complex</tt> base raised to the power of the <tt>Complex</tt> exponent.
    *
    * <p>
    * @param  base             The base "to raise"
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  base "raised to the power of" exponent
    * <p>
    * @see                     Complex#pow(Complex, double)
    * @see                     Complex#pow(Complex)
    **/
    public static Z pow (Z base, Z exponent) 
    {
        // return  base.log().Times(exponent).exp();

        double re =  Math.log(base.abs());
        double im =  base.arg();

        double re2 =  (re*exponent.re) - (im*exponent.im);
        double im2 =  (re*exponent.im) + (im*exponent.re);

        double scalar =  Math.exp(re2);

        return  cart( scalar * Math.cos(im2), scalar * Math.sin(im2) );
    }

    //---------------------------------//
    //             PUBLIC              //
    //---------------------------------//
    /**
    * Returns <tt>true</tt> if either the real or imaginary component of this
    * <tt>Complex</tt> is an infinite value.
    *
    * <p>
    * @return                  <tt>true</tt> if either component of the <tt>Complex</tt> object is infinite; <tt>false</tt>, otherwise.
    * <p>
    **/
    public boolean isInfinite() 
    {
        return  ( Double.isInfinite(re) || Double.isInfinite(im) );
    }

    /**
    * Returns <tt>true</tt> if either the real or imaginary component of this
    * <tt>Complex</tt> is a Not-a-Number (<tt>NaN</tt>) value.
    *
    * <p>
    * @return                  <tt>true</tt> if either component of the <tt>Complex</tt> object is <tt>NaN</tt>; <tt>false</tt>, otherwise.
    * <p>
    **/
    public boolean isNaN() 
    {
        return  ( Double.isNaN(re) || Double.isNaN(im) );
    }

    /**
    * Decides if two <tt>Complex</tt> numbers are "sufficiently" alike to be
    * considered equal.
    *
    * <p>
    * <tt>tolerance</tt> is the maximum magnitude of the difference between
    * them before they are considered <i>not</i> equal.
    * <p>
    * Checking for equality between two real numbers on computer hardware is a
    * tricky business.  Try
    * <p>
    * <pre>    System.out.println((1.0/3.0 * 3.0));</pre>
    * <p>
    * and you'll see the nature of the problem!  It's just as tricky with
    * <tt>Complex</tt> numbers.
    * <p>
    * Realize that because of these complications, it's possible to find that
    * the magnitude of one <tt>Complex</tt> number <tt>a</tt> is less than
    * another, <tt>b</tt>, and yet <tt>a.equals(b, myTolerance)</tt> returns
    * <tt>true</tt>.  Be aware!
    * <p>
    * @param  z                The <tt>Complex</tt> to compare with
    * @param  tolerance        The tolerance for equality
    * <p>
    * @return                  <tt>true</tt>, or <tt>false</tt>
    * <p>
    **/
    public boolean equals (Z z, double tolerance) 
    {
        // still true when _equal_ to tolerance? ...
        return  abs(re - z.re, im - z.im) <= Math.abs(tolerance);
        // ...and tolerance is always non-negative
    }//end equals(Complex,double)

    /**test if the object is equal to this one within a tolerance of
    1E-10
    @param arg = the object to test against*/
    public boolean equals (Object arg) 
    {
        if(arg instanceof Z)
        {
            // still true when _equal_ to tolerance? ...
            return  equals( ((Z)arg), 0.0000000001);
            // ...and tolerance is always non-negative
        }
        
        return false;
    }//end equals(Complex,double)

    /**
    * Overrides the {@link java.lang.Cloneable <tt>Cloneable</tt>} interface.
    *
    * <p>
    * Standard override; no change in semantics.
    * <p>
    * The following Java code example illustrates how to clone, or <i>copy</i>, a
    * <tt>Complex</tt> number:
    * <p>
    * <pre>
    *     Z z1 =  <b>new</b> Z(0, 1);
    *     Z z2 =  (Complex) z1.clone();
    * </pre>
    * <p>
    * @return                  An <tt>Object</tt> that is a copy of this <tt>Complex</tt> object.
    * <p>
    * @see                     java.lang.Cloneable
    * @see                     java.lang.Object#clone()
    **/
    public Object clone () 
    {
        try 
        {
            return  (Object)(super.clone());
        } 
        catch (java.lang.CloneNotSupportedException e) 
        {
            return null;                                                       // This cannot happen: there would have to be a serious internal error in the Java runtime if this codepath happens!
        }
    }

    /**
    * Extracts the real part of a <tt>Complex</tt> as a <tt>double</tt>.
    *
    * <p>
    * <pre>
    *     re(x + <i><b>i</b></i>*y)  =  x
    * </pre>
    * <p>
    * @return                  The real part
    * <p>
    * @see                     Complex#im()
    * @see                     Complex#cart(double, double)
    * @see                     Complex#real(double)
    **/

    public double re () 
    {
        return  re;
    }

    /**
    * Extracts the imaginary part of a <tt>Complex</tt> as a <tt>double</tt>.
    *
    * <p>
    * <pre>
    *     im(x + <i><b>i</b></i>*y)  =  y
    * </pre>
    * <p>
    * @return                  The imaginary part
    * <p>
    * @see                     Complex#re()
    * @see                     Complex#cart(double, double)
    **/

    public double im () 
    {
        return  im;
    }//end im()

    /**
    * Returns the square of the "length" of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     norm(x + <i><b>i</b></i>*y)  =  x*x + y*y
    * </pre>
    * <p>
    * Always non-negative.
    * <p>
    * @return                  The norm
    * <p>
    * @see                     Complex#abs()
    **/
    public double norm () 
    {
        return  (re*re) + (im*im);
    }

    /**
    * Returns the magnitude of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     abs(z)  =  Sqrt(norm(z))
    * </pre>
    * <p>
    * In other words, it's Pythagorean distance from the origin
    * (<i>0 + 0<b>i</b></i>, or zero).
    * <p>
    * The magnitude is also referred to as the "modulus" or "length".
    * <p>
    * Always non-negative.
    * <p>
    * @return                  The magnitude (or "length")
    * <p>
    * @see                     Complex#arg()
    * @see                     Complex#polar(double, double)
    * @see                     Complex#norm()
    **/
    public double abs() 
    {
        return  abs(re, im);
    }

    static public double abs(Z z) 
    {
        return  z.abs();
    }

    static private double abs(double x, double y) 
    {
        //  abs(z)  =  Sqrt(norm(z))

        // Adapted from
        // "Numerical Recipes in Fortran 77: The Art of Scientific Computing"
        // (ISBN 0-521-43064-X)

        double absX =  Math.abs(x);
        double absY =  Math.abs(y);

        if (absX == 0.0 && absY == 0.0) {                                      // !!! Numerical Recipes, mmm?
            return  0.0;
        } else if (absX >= absY) {
            double d =  y / x;
            return  absX*Math.sqrt(1.0 + d*d);
        } else {
            double d =  x / y;
            return  absY*Math.sqrt(1.0 + d*d);
        }//endif
    }//end abs()

    /**
    * Returns the <i>principal</i> angle of a <tt>Complex</tt> number, in
    * radians, measured counter-clockwise from the real axis.  (Think of the
    * reals as the x-axis, and the imaginaries as the y-axis.)
    *
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>arg(z)</i>, the others are of
    * the form:
    * <p>
    * <pre>
    *     <b>A</b> + 2*k*<b>PI</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * <tt>arg()</tt> always returns a <tt>double</tt> between
    * -<tt><b>PI</b></tt> and +<tt><b>PI</b></tt>.
    * <p>
    * <i><b>Note:</b><ul> 2*<tt><b>PI</b></tt> radians is the same as 360 degrees.
    * </ul></i>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> There are no restrictions: the
    * class defines arg(0) to be 0
    * </ul></i>
    * <p>
    * @return                  Principal angle (in radians)
    * <p>
    * @see                     Complex#abs()
    * @see                     Complex#polar(double, double)
    **/
    public double arg () 
    {
        return  Math.atan2(im, re);
    }

    /**
    * Returns the "negative" of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     neg(a + <i><b>i</b></i>*b)  =  -a - <i><b>i</b></i>*b
    * </pre>
    * <p>
    * The magnitude of the negative is the same, but the angle is flipped
    * through <tt><b>PI</b></tt> (or 180 degrees).
    * <p>
    * @return                  Negative of the <tt>Complex</tt>
    * <p>
    * @see                     Complex#scale(double)
    **/

    public Z neg() 
    {
        return  this.scale(-1.0);
    }

    /**
    * Returns the <tt>Complex</tt> "conjugate".
    *
    * <p>
    * <pre>
    *     conj(x + <i><b>i</b></i>*y)  =  x - <i><b>i</b></i>*y
    * </pre>
    * <p>
    * The conjugate appears "flipped" across the real axis.
    * <p>
    * @return                  The <tt>Complex</tt> conjugate
    *<p>
    **/

    public Z conj() 
    {
        return  cart(re, -im);
    }//end conj()


    public Z Conj(Z a) 
    {
        im = -im;
        return this;
    }//end conj()

    static private void inv (Z z) 
    {
        double zRe, zIm;
        double scalar;

        if (Math.abs(z.re) >= Math.abs(z.im)) 
        {
            scalar =  1.0 / ( z.re + z.im*(z.im/z.re) );

            zRe =    scalar;
            zIm =    scalar * (- z.im/z.re);
        } 
        else 
        {
            scalar =  1.0 / ( z.re*(z.re/z.im) + z.im );

            zRe =    scalar * (  z.re/z.im);
            zIm =  - scalar;
        }

        z.re = zRe;
        z.im = zIm;
    }



    /**
    * Returns the <tt>Complex</tt> scaled by a real number.
    *
    * <p>
    * <pre>
    *     scale((x + <i><b>i</b></i>*y), s)  =  (x*s + <i><b>i</b></i>*y*s)
    * </pre>
    * <p>
    * Scaling by the real number <i>2.0</i>, doubles the magnitude, but leaves
    * the <tt>arg()</tt> unchanged.  Scaling by <i>-1.0</i> keeps the magnitude
    * the same, but flips the <tt>arg()</tt> by <tt><b>PI</b></tt> (180 degrees).
    * <p>
    * @param  scalar           A real number scale factor
    * <p>
    * @return                  <tt>Complex</tt> scaled by a real number
    * <p>
    * @see                     Complex#Times(Complex)
    * @see                     Complex#Div(Complex)
    * @see                     Complex#neg()
    **/
    public Z scale (double scalar) 
    {
        return  cart(scalar*re, scalar*im);
    }



    /**
    * To perform z1 + z2, you write <tt>z1.Plus(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) + (c + <i><b>i</b></i>*d)  =  ((a+c) + <i><b>i</b></i>*(b+d))
    * </pre>
    * <p>
    **/
    public Z Plus(Z z) 
    {
    	Z temp = cart(re + z.re, im + z.im);
        return  temp;
    }


    public Z Plus(Z z1, Z z2) 
    {
    	Z temp = cart(z1.re + z2.re, z1.im + z2.im);
        return  temp;
    }

    /**
    * To perform z1 - z2, you write <tt>z1.Minus(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) - (c + <i><b>i</b></i>*d)  =  ((a-c) + <i><b>i</b></i>*(b-d))
    * </pre>
    * <p>
    **/
    public Z Minus(Z z) 
    {
        return  cart(re - z.re, im - z.im);
    }

    public Z Minus(Z z1, Z z2) 
    {
        return  cart(z1.re - z2.re, z1.im - z2.im);
    }
    
    /**
    * To perform z1 * z2, you write <tt>z1.Times(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) * (c + <i><b>i</b></i>*d)  =  ( (a*c) - (b*d) + <i><b>i</b></i>*((a*d) + (b*c)) )
    * </pre>
    * <p>
    * @see                     Complex#scale(double)
    **/
    public Z Times(Z z) 
    {
        return  cart( (re*z.re) - (im*z.im), (re*z.im) + (im*z.re) );
        // return  cart( (re*z.re) - (im*z.im), (re + im)*(z.re + z.im) - re*z.re - im*z.im);
    }

	public Z Times(Z a, Z b)
	{
		Z result = a.Times(b);
		re = result.re;
		im = result.im;
		
		return this;
	}

    /**
    * To perform z1 / z2, you write <tt>z1.Div(z2)</tt> .
    *
    * <p>
    * <pre>
    *     (a + <i><b>i</b></i>*b) / (c + <i><b>i</b></i>*d)  =  ( (a*c) + (b*d) + <i><b>i</b></i>*((b*c) - (a*d)) ) / norm(c + <i><b>i</b></i>*d)
    * </pre>
    * <p>
    * <i><b>Take care not to divide by zero!</b></i>
    * <p>
    * <i><b>Note:</b><ul> <tt>Complex</tt> arithmetic in Java never causes
    * exceptions.  You have to deliberately check for overflow, division by
    * zero, and so on, <u>for yourself</u>.
    * </ul></i>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> z1/z2 is undefined if z2 = 0
    * </ul></i>
    * <p>
    * @see                     Complex#scale(double)
    **/
    public Z Div(Z z) 
    {
        Z result =  new Z(this);
        Div(result, z.re, z.im);
        return  result;
    }


	public Z Div(Z a, Z b)
	{
		Z result = a.Div(b);
		re = result.re;
		im = result.im;
		
		return this;
	}

    static public Z Div(Z z, double x) 
    {
    	return Div(z, x, 0.0);
	}

    static public Z Div(Z z, double x, double y) 
    {
        // Adapted from
        // "Numerical Recipes in Fortran 77: The Art of Scientific Computing"
        // (ISBN 0-521-43064-X)

        double zRe, zIm;
        double scalar;

        if (Math.abs(x) >= Math.abs(y)) 
        {
            scalar =  1.0 / ( x + y*(y/x) );

            zRe =  scalar * (z.re + z.im*(y/x));
            zIm =  scalar * (z.im - z.re*(y/x));

        } 
        else 
        {
            scalar =  1.0 / ( x*(x/y) + y );

            zRe =  scalar * (z.re*(x/y) + z.im);
            zIm =  scalar * (z.im*(x/y) - z.re);
        }

        z.re = zRe;
        z.im = zIm;
        
        return z;
    }



    /**
    * Returns a <tt>Complex</tt> representing one of the two square roots.
    *
    * <p>
    * <pre>
    *     Sqrt(z)  =  Sqrt(abs(z)) * ( cos(arg(z)/2) + <i><b>i</b></i> * sin(arg(z)/2) )
    * </pre>
    * <p>
    * For any <i>complex</i> number <i>z</i>, <i>Sqrt(z)</i> will return the
    * <i>complex</i> root whose <i>arg</i> is <i>arg(z)/2</i>.
    * <p>
    * <i><b>Note:</b><ul> There are always two square roots for each
    * <tt>Complex</tt> number, except for 0 + 0<b>i</b>, or zero.  The other
    * root is the <tt>neg()</tt> of the first one.  Just as the two roots of
    * 4 are 2 and -2, the two roots of -1 are <b>i</b> and - <b>i</b>.
    * </ul></i>
    * <p>
    * @return                  The square root whose <i>arg</i> is <i>arg(z)/2</i>.
    * <p>
    * @see                     Complex#pow(Complex, double)
    **/
    public Z Sqrt () 
    {
        Z result =  new Z(this);
        Sqrt(result);
        return  result;
    }//end Sqrt()


    static public void Sqrt (Z z) 
    {
        // with thanks to Jim Shapiro <jnshapi@argo.ecte.uswc.uswest.com>
        // adapted from "Numerical Recipies in C" (ISBN 0-521-43108-5)
        // by William H. Press et al

        double mag =  z.abs();

        if (mag > 0.0) 
        {
            if (z.re > 0.0) 
            {
                double temp =  Math.sqrt(0.5 * (mag + z.re));

                z.re =  temp;
                z.im =  0.5 * z.im / temp;
            } 
            else 
            {
                double temp =  Math.sqrt(0.5 * (mag - z.re));

                if (z.im < 0.0) 
                {
                    temp =  -temp;
                }//endif

                z.re =  0.5 * z.im / temp;
                z.im =  temp;
            }//endif
        } 
        else 
        {
            z.re =  0.0;
            z.im =  0.0;
        }
    }



    /**
    * Returns this <tt>Complex</tt> raised to the power of a <tt>Complex</tt> exponent.
    *
    * <p>
    * @param  exponent         The exponent "by which to raise"
    * <p>
    * @return                  this <tt>Complex</tt> "raised to the power of" the exponent
    * <p>
    * @see                     Complex#pow(Complex, Complex)
    **/
    public Z pow (Z exponent) 
    {
        return  Z.pow(this, exponent);
    }



    /**
    * Returns the number <i><b>e</b></i> "raised to" a <tt>Complex</tt> power.
    *
    * <p>
    * <pre>
    *     exp(x + <i><b>i</b></i>*y)  =  exp(x) * ( cos(y) + <i><b>i</b></i> * sin(y) )
    * </pre>
    * <p>
    * <i><b>Note:</b><ul> The value of <i><b>e</b></i>, a transcendental number, is
    * roughly 2.71828182846...
    * <p>
    *
    * Also, the following is quietly amazing:
    * <pre>
    *     <i><b>e</b></i><sup><font size=+0><b>PI</b>*<i><b>i</b></i></font></sup>    =    - 1
    * </pre>
    * </ul>
    * </i>
    * <p>
    * @return                  <i><b>e</b></i> "raised to the power of" this <tt>Complex</tt>
    * <p>
    * @see                     Complex#log()
    * @see                     Complex#pow(double, Complex)
    **/
    public Z exp () 
    {
        double scalar =  Math.exp(re);                                         // e^ix = cis x
        return  cart( scalar * Math.cos(im), scalar * Math.sin(im) );
    }


    /**
    * Returns the <i>principal</i> natural logarithm of a <tt>Complex</tt>
    * number.
    *
    * <p>
    * <pre>
    *     log(z)  =  log(abs(z)) + <i><b>i</b></i> * arg(z)
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>L</b> is the principal solution of <i>log(z)</i>, the others are of
    * the form:
    * <p>
    * <pre>
    *     <b>L</b> + (2*k*<b>PI</b>)*<i><b>i</b></i>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> natural logarithm
    * <p>
    * @see                     Complex#exp()
    **/
    public Z log() 
    {
        return  cart( Math.log(this.abs()), this.arg() );                      // principal value
    }

    /**
    * Returns the sine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     sin(z)  =  ( exp(<i><b>i</b></i>*z) - exp(-<i><b>i</b></i>*z) ) / (2*<i><b>i</b></i>)
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> sine
    * <p>
    * @see                     Complex#asin()
    * @see                     Complex#sinh()
    * @see                     Complex#cosec()
    * @see                     Complex#cos()
    * @see                     Complex#tan()
    **/
    public Z sin () 
    {
        Z result;
            //  sin(z)  =  ( exp(i*z) - exp(-i*z) ) / (2*i)

        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;

        // iz:      i.Times(z) ...
        iz_re =  -im;
        iz_im =   re;

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _1:      _1.Minus(_2) ...
        _re1 = _re1 - _re2;                                                // !!!
        _im1 = _im1 - _im2;                                                // !!!

        // result:  _1.Div(2*i) ...
        result =  cart( 0.5*_im1, -0.5*_re1 );
        // ... result =  cart(_re1, _im1);
        //     Div(result, 0.0, 2.0);
        return  result;
    }

    /**
    * Returns the cosine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cos(z)  =  ( exp(<i><b>i</b></i>*z) + exp(-<i><b>i</b></i>*z) ) / 2
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> cosine
    * <p>
    * @see                     Complex#acos()
    * @see                     Complex#cosh()
    * @see                     Complex#sec()
    * @see                     Complex#sin()
    * @see                     Complex#tan()
    **/
    public Z cos() 
    {
        Z result;
        //  cos(z)  =  ( exp(i*z) + exp(-i*z) ) / 2

        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;

        // iz:      i.Times(z) ...
        iz_re =  -im;
        iz_im =   re;

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _1:      _1.Plus(_2) ...
        _re1 = _re1 + _re2;                                                // !!!
        _im1 = _im1 + _im2;                                                // !!!

        // result:  _1.scale(0.5) ...
        result =  cart( 0.5 * _re1, 0.5 * _im1 );
        return  result;
    }

    /**
    * Returns the tangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     tan(z)  =  sin(z) / cos(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> tan(z) is undefined whenever z = (k + 1/2) * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> tangent
    * <p>
    * @see                     Complex#atan()
    * @see                     Complex#tanh()
    * @see                     Complex#cot()
    * @see                     Complex#sin()
    * @see                     Complex#cos()
    **/
    public Z tan() 
    {
        Z result;
        //  tan(z)  =  sin(z) / cos(z)

        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;
        double _re3, _im3;

        double cs_re, cs_im;

        // sin() ...

        // iz:      i.Times(z) ...
        iz_re =  -im;
        iz_im =   re;

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _3:      _1.Minus(_2) ...
        _re3 = _re1 - _re2;
        _im3 = _im1 - _im2;

        // result:  _3.Div(2*i) ...
        result =  cart( 0.5*_im3, -0.5*_re3 );
        // result =  cart(_re3, _im3);
        // Div(result, 0.0, 2.0);

        // cos() ...

        // _3:      _1.Plus(_2) ...
        _re3 = _re1 + _re2;
        _im3 = _im1 + _im2;

        // cs:      _3.scale(0.5) ...
        cs_re =  0.5 * _re3;
        cs_im =  0.5 * _im3;

        // result:  result.Div(cs) ...
        Div(result, cs_re, cs_im);
    return  result;
    }

    /**
    * Returns the cosecant of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cosec(z)  =  1 / sin(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> cosec(z) is undefined whenever z = k * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> cosecant
    * <p>
    * @see                     Complex#sin()
    * @see                     Complex#sec()
    * @see                     Complex#cot()
    **/
    public Z cosec () 
    {
        Z result;
        //  cosec(z)  =  1 / sin(z)

        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;

        // iz:      i.Times(z) ...
        iz_re =  -im;
        iz_im =   re;

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _1:      _1.Minus(_2) ...
        _re1 = _re1 - _re2;                                                // !!!
        _im1 = _im1 - _im2;                                                // !!!

        // _result: _1.Div(2*i) ...
        result =  cart( 0.5*_im1, -0.5*_re1 );
        // result =  cart(_re1, _im1);
        // Div(result, 0.0, 2.0);

        // result:  one.Div(_result) ...
        inv(result);
        return  result;
    }



    /**
    * Returns the secant of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     sec(z)  =  1 / cos(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> sec(z) is undefined whenever z = (k + 1/2) * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> secant
    * <p>
    * @see                     Complex#cos()
    * @see                     Complex#cosec()
    * @see                     Complex#cot()
    **/
    public Z sec () 
    {
        Z result;
        //  sec(z)  =  1 / cos(z)

        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;

        // iz:      i.Times(z) ...
        iz_re =  -im;
        iz_im =   re;

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _1:      _1.Plus(_2) ...
        _re1 = _re1 + _re2;
        _im1 = _im1 + _im2;

        // result: _1.scale(0.5) ...
        result =  cart(0.5*_re1, 0.5*_im1);

        // result:  one.Div(result) ...
        inv(result);
        return  result;
    }

    /**
    * Returns the cotangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cot(z)  =  1 / tan(z)
    * </pre>
    * <p>
    * <i><b>Domain Restrictions:</b><ul> cot(z) is undefined whenever z = k * <tt><b>PI</b></tt><br>
    * where k is any integer
    * </ul></i>
    * <p>
    * @return                  The <tt>Complex</tt> cotangent
    * <p>
    * @see                     Complex#tan()
    * @see                     Complex#cosec()
    * @see                     Complex#sec()
    **/

    public Z cot() 
    {
        Z result;
        //  cot(z)  =  1 / tan(z)  =  cos(z) / sin(z)

        double scalar;
        double iz_re, iz_im;
        double _re1, _im1;
        double _re2, _im2;
        double _re3, _im3;

        double sn_re, sn_im;

        // cos() ...

        // iz:      i.Times(z) ...
        iz_re =  -im;
        iz_im =   re;

        // _1:      iz.exp() ...
        scalar =  Math.exp(iz_re);
        _re1 =  scalar * Math.cos(iz_im);
        _im1 =  scalar * Math.sin(iz_im);

        // _2:      iz.neg().exp() ...
        scalar =  Math.exp(-iz_re);
        _re2 =  scalar * Math.cos(-iz_im);
        _im2 =  scalar * Math.sin(-iz_im);

        // _3:      _1.Plus(_2) ...
        _re3 = _re1 + _re2;
        _im3 = _im1 + _im2;

        // result:  _3.scale(0.5) ...
        result =  cart( 0.5*_re3, 0.5*_im3 );

        // sin() ...

        // _3:      _1.Minus(_2) ...
        _re3 = _re1 - _re2;
        _im3 = _im1 - _im2;

        // sn:      _3.Div(2*i) ...
        sn_re =    0.5 * _im3;                                             // !!!
        sn_im =  - 0.5 * _re3;                                             // !!!

        // result:  result.Div(sn) ...
        Div(result, sn_re, sn_im);
        return  result;
    }

    /**
    * Returns the hyperbolic sine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     sinh(z)  =  ( exp(z) - exp(-z) ) / 2
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> hyperbolic sine
    * <p>
    * @see                     Complex#sin()
    * @see                     Complex#asinh()
    **/

    public Z sinh () 
    {
        Z result;
        //  sinh(z)  =  ( exp(z) - exp(-z) ) / 2

        double scalar;
        double _re1, _im1;
        double _re2, _im2;

        // _1:      z.exp() ...
        scalar =  Math.exp(re);
        _re1 =  scalar * Math.cos(im);
        _im1 =  scalar * Math.sin(im);

        // _2:      z.neg().exp() ...
        scalar =  Math.exp(-re);
        _re2 =  scalar * Math.cos(-im);
        _im2 =  scalar * Math.sin(-im);

        // _1:      _1.Minus(_2) ...
        _re1 = _re1 - _re2;                                                // !!!
        _im1 = _im1 - _im2;                                                // !!!

        // result:  _1.scale(0.5) ...
        result =  cart( 0.5 * _re1, 0.5 * _im1 );
        return  result;
    }//end sinh()

    /**
    * Returns the hyperbolic cosine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     cosh(z)  =  ( exp(z) + exp(-z) ) / 2
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> hyperbolic cosine
    * <p>
    * @see                     Complex#cos()
    * @see                     Complex#acosh()
    **/

    public Z cosh () 
    {
        Z result;
        //  cosh(z)  =  ( exp(z) + exp(-z) ) / 2

        double scalar;
        double _re1, _im1;
        double _re2, _im2;

        // _1:      z.exp() ...
        scalar =  Math.exp(re);
        _re1 =  scalar * Math.cos(im);
        _im1 =  scalar * Math.sin(im);

        // _2:      z.neg().exp() ...
        scalar =  Math.exp(-re);
        _re2 =  scalar * Math.cos(-im);
        _im2 =  scalar * Math.sin(-im);

        // _1:  _1.Plus(_2) ...
        _re1 = _re1 + _re2;                                                // !!!
        _im1 = _im1 + _im2;                                                // !!!

        // result:  _1.scale(0.5) ...
        result =  cart( 0.5 * _re1, 0.5 * _im1 );
        return  result;
    }//end cosh()

    /**
    * Returns the hyperbolic tangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     tanh(z)  =  sinh(z) / cosh(z)
    * </pre>
    * <p>
    * @return                  The <tt>Complex</tt> hyperbolic tangent
    * <p>
    * @see                     Complex#tan()
    * @see                     Complex#atanh()
    **/
    public Z tanh () 
    {
        Z result;
        //  tanh(z)  =  sinh(z) / cosh(z)

        double scalar;
        double _re1, _im1;
        double _re2, _im2;
        double _re3, _im3;

        double ch_re, ch_im;

        // sinh() ...

        // _1:      z.exp() ...
        scalar =  Math.exp(re);
        _re1 =  scalar * Math.cos(im);
        _im1 =  scalar * Math.sin(im);

        // _2:      z.neg().exp() ...
        scalar =  Math.exp(-re);
        _re2 =  scalar * Math.cos(-im);
        _im2 =  scalar * Math.sin(-im);

        // _3:      _1.Minus(_2) ...
        _re3 =  _re1 - _re2;
        _im3 =  _im1 - _im2;

        // result:  _3.scale(0.5) ...
        result =  cart(0.5*_re3, 0.5*_im3);

        // cosh() ...

        // _3:      _1.Plus(_2) ...
        _re3 =  _re1 + _re2;
        _im3 =  _im1 + _im2;

        // ch:      _3.scale(0.5) ...
        ch_re =  0.5 * _re3;
        ch_im =  0.5 * _im3;

        // result:  result.Div(ch) ...
        Div(result, ch_re, ch_im);
        return  result;
    }//end tanh()

    /**
    * Returns the <i>principal</i> arc sine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     asin(z)  =  -<i><b>i</b></i> * log(<i><b>i</b></i>*z + Sqrt(1 - z*z))
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>asin(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     k*<b>PI</b> + (-1)<sup><font size=-1>k</font></sup>  * <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> arc sine
    * <p>
    * @see                     Complex#sin()
    * @see                     Complex#sinh()
    **/

    public Z asin () 
    {
        Z result;
        //  asin(z)  =  -i * log(i*z + Sqrt(1 - z*z))

        double _re1, _im1;

        // _1:      one.Minus(z.Times(z)) ...
        _re1 =  1.0 - ( (re*re) - (im*im) );
        _im1 =  0.0 - ( (re*im) + (im*re) );

        // result:  _1.Sqrt() ...
        result =  cart(_re1, _im1);
        Sqrt(result);

        // _1:      z.Times(i) ...
        _re1 =  - im;
        _im1 =  + re;

        // result:  _1.Plus(result) ...
        result.re =  _re1 + result.re;
        result.im =  _im1 + result.im;

        // _1:      result.log() ...
        _re1 =  Math.log(result.abs());
        _im1 =  result.arg();

        // result:  i.neg().Times(_1) ...
        result.re =    _im1;
        result.im =  - _re1;
        return  result;
    }

    /**
    * Returns the <i>principal</i> arc cosine of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     acos(z)  =  -<i><b>i</b></i> * log( z + <i><b>i</b></i> * Sqrt(1 - z*z) )
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>acos(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     2*k*<b>PI</b> +/- <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> arc cosine
    * <p>
    * @see                     Complex#cos()
    * @see                     Complex#cosh()
    **/
    public Z acos () 
    {
        Z result;
        //  acos(z)  =  -i * log( z + i * Sqrt(1 - z*z) )

        double _re1, _im1;

        // _1:      one.Minus(z.Times(z)) ...
        _re1 =  1.0 - ( (re*re) - (im*im) );
        _im1 =  0.0 - ( (re*im) + (im*re) );

        // result:  _1.Sqrt() ...
        result =  cart(_re1, _im1);
        Sqrt(result);

        // _1:      i.Times(result) ...
        _re1 =  - result.im;
        _im1 =  + result.re;

        // result:  z.Plus(_1) ...
        result.re =  re + _re1;
        result.im =  im + _im1;

        // _1:      result.log()
        _re1 =  Math.log(result.abs());
        _im1 =  result.arg();

        // result:  i.neg().Times(_1) ...
        result.re =    _im1;
        result.im =  - _re1;
        return  result;
    }

    /**
    * Returns the <i>principal</i> arc tangent of a <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     atan(z)  =  -<i><b>i</b></i>/2 * log( (<i><b>i</b></i>-z)/(<i><b>i</b></i>+z) )
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>atan(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     <b>A</b> + k*<b>PI</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * <i><b>Domain Restrictions:</b><ul> atan(z) is undefined for z = + <b>i</b> or z = - <b>i</b>
    * </ul></i>
    * <p>
    * @return                  Principal <tt>Complex</tt> arc tangent
    * <p>
    * @see                     Complex#tan()
    * @see                     Complex#tanh()
    **/
    public Z atan () 
    {
        Z result;
        //  atan(z)  =  -i/2 * log( (i-z)/(i+z) )

        double _re1, _im1;

        // result:  i.Minus(z) ...
        result =  cart(- re, 1.0 - im);

        // _1:      i.Plus(z) ...
        _re1 =  + re;
        _im1 =  1.0 + im;

        // result:  result.Div(_1) ...
        Div(result, _re1, _im1);

        // _1:      result.log() ...
        _re1 =  Math.log(result.abs());
        _im1 =  result.arg();

        // result:  half_i.neg().Times(_2) ...
        result.re =   0.5*_im1;
        result.im =  -0.5*_re1;
        return  result;
    }

    /**
    * Returns the <i>principal</i> inverse hyperbolic sine of a
    * <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     asinh(z)  =  log(z + Sqrt(z*z + 1))
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>asinh(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     k*<b>PI</b>*<b><i>i</i></b> + (-1)<sup><font size=-1>k</font></sup>  * <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> inverse hyperbolic sine
    * <p>
    * @see                     Complex#sinh()
    **/
    public Z asinh () 
    {
        Z result;
        //  asinh(z)  =  log(z + Sqrt(z*z + 1))

        double _re1, _im1;

        // _1:      z.Times(z).Plus(one) ...
        _re1 =  ( (re*re) - (im*im) ) + 1.0;
        _im1 =  ( (re*im) + (im*re) ) + 0.0;

        // result:  _1.Sqrt() ...
        result =  cart(_re1, _im1);
        Sqrt(result);

        // result:  z.Plus(result) ...
        result.re =  re + result.re;                                       // !
        result.im =  im + result.im;                                       // !

        // _1:      result.log() ...
        _re1 =  Math.log(result.abs());
        _im1 =  result.arg();

        // result:  _1 ...
        result.re =  _re1;
        result.im =  _im1;

        /*
        * Many thanks to the mathematicians of aus.mathematics and sci.math,
        * and to Zdislav V. Kovarik of the  Department  of  Mathematics  and
        * Statistics,     McMaster     University     and    John    McGowan
        * <jmcgowan@inch.com> in particular, for their advice on the current
        * naming conventions for "area/argumentus sinus hyperbolicus".
        */

        return  result;
    }//end asinh()

    /**
    * Returns the <i>principal</i> inverse hyperbolic cosine of a
    * <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     acosh(z)  =  log(z + Sqrt(z*z - 1))
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>acosh(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     2*k*<b>PI</b>*<b><i>i</i></b> +/- <b>A</b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * @return                  Principal <tt>Complex</tt> inverse hyperbolic cosine
    * <p>
    * @see                     Complex#cosh()
    **/
    public Z acosh() 
    {
        Z result;
        //  acosh(z)  =  log(z + Sqrt(z*z - 1))

        double _re1, _im1;

        // _1:  z.Times(z).Minus(one) ...
        _re1 =  ( (re*re) - (im*im) ) - 1.0;
        _im1 =  ( (re*im) + (im*re) ) - 0.0;

        // result:  _1.Sqrt() ...
        result =  cart(_re1, _im1);
        Sqrt(result);

        // result:  z.Plus(result) ...
        result.re =  re + result.re;                                       // !
        result.im =  im + result.im;                                       // !

        // _1:  result.log() ...
        _re1 =  Math.log(result.abs());
        _im1 =  result.arg();

        // result:  _1 ...
        result.re =  _re1;
        result.im =  _im1;
        return  result;
    }//end acosh()


    /**
    * Returns the <i>principal</i> inverse hyperbolic tangent of a
    * <tt>Complex</tt> number.
    *
    * <p>
    * <pre>
    *     atanh(z)  =  1/2 * log( (1+z)/(1-z) )
    * </pre>
    * <p>
    * There are infinitely many solutions, besides the principal solution.
    * If <b>A</b> is the principal solution of <i>atanh(z)</i>, the others are
    * of the form:
    * <p>
    * <pre>
    *     <b>A</b> + k*<b>PI</b>*<b><i>i</i></b>
    * </pre>
    * <p>
    * where k is any integer.
    * <p>
    * <i><b>Domain Restrictions:</b><ul> atanh(z) is undefined for z = + 1 or z = - 1
    * </ul></i>
    * <p>
    * @return                  Principal <tt>Complex</tt> inverse hyperbolic tangent
    * <p>
    * @see                     Complex#tanh()
    **/
    public Z atanh () 
    {
        Z result;
        //  atanh(z)  =  1/2 * log( (1+z)/(1-z) )

        double _re1, _im1;

        // result:  one.Plus(z) ...
        result =  cart(1.0 + re, + im);

        // _1:      one.Minus(z) ...
        _re1 =  1.0 - re;
        _im1 =  - im;

        // result:  result.Div(_1) ...
        Div(result, _re1, _im1);

        // _1:      result.log() ...
        _re1 =  Math.log(result.abs());
        _im1 =  result.arg();

        // result:  _1.scale(0.5) ...
        result.re =  0.5 * _re1;
        result.im =  0.5 * _im1;
        return  result;
    }

    /**
    * Converts a <tt>Complex</tt> into a {@link java.lang.String <tt>String</tt>} of the form
    * <tt>(</tt><i>a</i><tt> + </tt><i>b</i><tt>i)</tt>.
    *
    * <p>
    * This enables a <tt>Complex</tt> to be easily printed.  For example, if
    * <tt>z</tt> was <i>2 - 5<b>i</b></i>, then
    * <pre>
    *     System.out.println("z = " + z);
    * </pre>
    * would print something like
    * <pre>
    *     z = (2.0 - 5.0i)
    * </pre>
    * <!--
    * <i><b>Note:</b><ul>Concatenating {@link java.lang.String <tt>String</tt>}s, using a system
    * overloaded meaning of the "<tt>+</tt>" operator, in fact causes the
    * <tt>toString()</tt> method to be invoked on the object <tt>z</tt> at
    * runtime.</ul></i>
    * -->
    * <p>
    * @return                  {@link java.lang.String <tt>String</tt>} containing the cartesian coordinate representation
    * <p>
    * @see                     Complex#cart(double, double)
    **/
    public String toString () 
    {
        StringBuffer result =  new StringBuffer();
        if(im != 0)
        	result.append("(");
        result.append(numFormat.format(re));

        if (im < 0.0) 
        {                                                        // ...remembering NaN & Infinity
            result.append(" - ").append(numFormat.format(-im));
	        result.append("i)");
        } 
        else if (1.0 / im == Double.NEGATIVE_INFINITY) 
        {
        } 
        else if(im > 0)
        {
            result.append(" + ").append(numFormat.format(+im));
	        result.append("i)");
        }

        return  result.toString();
    }//end toString()

/**
 * Interchanges the real and imaginary parts of two Z's.

   @param     a a Z
   @return    this = a, with a set to the original
              value of this.
*/

   public Z Exch(Z a){
      double t;
      t = re; re = a.re; a.re = t;
      t = im; im = a.im; a.im = t;
      return this;
   }
   
   public Z Eq(Z a){
   		re = a.re;
   		im = a.im;
   		return this;
   }
}




/*           Jim Shapiro <jnshapi@argo.ecte.uswc.uswest.com>


                           Priyantha Jayanetti
                       ---------------------------
                       email: pidge@eece.maine.edu

               Dept.  of Electrical & Computer Engineering
                       University of Maine,  Orono


                            Mr.  Daniel Hirsch
                     <R.D.Hirsch@red-deer.demon.co.uk>


/*             C A U T I O N   E X P L O S I V E   B O L T S
--                     REMOVE BEFORE ENGAGING REPLY
//
// Kelly and Sandy Anderson <kelsan@explosive-alma-services-bolts.co.uk>
// (alternatively            kelsan_odoodle at ya who period, see oh em)
// Alexander (Sandy)  1B5A DF3D A3D9 B932 39EB  3F1B 981F 4110 27E1 64A4
// Kelly              673F 6751 6DBA 196F E8A8  6D87 4AEC F35E E9AD 099B
// Homepages             http://www.explosive-alma-services-bolts.co.uk/
*/

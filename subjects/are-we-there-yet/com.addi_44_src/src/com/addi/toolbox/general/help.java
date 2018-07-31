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

package com.addi.toolbox.general;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.functions.Function;
import com.addi.core.interpreter.*;
import com.addi.core.tokens.*;

/**An external function for changing to another directory         */
public class help extends ExternalFunction
{
	Map<String, String> functions = new HashMap();
	
	/**Default constructor - Creates a function with a null name*/
	public help()
	{
		name = "help";
		functions.put(" ","For help with individual commands and functions type\n   help NAME\n(replace NAME with the name of the command or function you would like to learn more about).\nFor a more detailed introduction to Addi, please go to http://addi.googlecode.com\nYou can also check out the FAQ found here http://code.google.com/p/addi/wiki/FAQ");
		functions.put("acos","acos:\n   Inverse cosine; result in radians\nSyntax:\n   Y = acos(X)\nSee Also:\n   acosd, acosh, cos\nhttp://www.mathworks.com/help/techdoc/ref/acos.html");
		functions.put("acosh","acosh:\n   Inverse hyperbolic cosine\nSyntax:\n   Y = acosh(X)\nSee Also:\n   acos, cosh\nhttp://www.mathworks.com/help/techdoc/ref/acosh.html");
		functions.put("acot","acot:\n   Inverse cotangent; result in radians\nSyntax:\n   Y = acot(X)\nSee Also:\n   cot, acotd, acoth\nhttp://www.mathworks.com/help/techdoc/ref/acot.html");
		functions.put("acoth","acoth:\n   Inverse hyperbolic cotangent\nSyntax:\n   Y = acoth(X)\nSee Also:\n   acot, coth\nhttp://www.mathworks.com/help/techdoc/ref/acoth.html");
		functions.put("acsc","acsc:\n   Inverse cosecant; result in radians\nSyntax:\n   Y = acsc(X)\nSee Also:\n   csc, acscd, acsch\nhttp://www.mathworks.com/help/techdoc/ref/acsc.html");
		functions.put("acsch","acsch:\n   Inverse hyperbolic cosecant\nSyntax:\n   Y = acsch(X)\nSee Also:\n   acsc, csch\nhttp://www.mathworks.com/help/techdoc/ref/acsch.html");
		functions.put("asec","asec:\n   Inverse secant; result in radians\nSyntax:\n   Y = asec(X)\nSee Also:\n   asecd, asech, sec\nhttp://www.mathworks.com/help/techdoc/ref/asec.html");
		functions.put("asech","asech:\n   Inverse hyperbolic secant\nSyntax:\n   Y = asech(X)\nSee Also:\n   asec, sech\nhttp://www.mathworks.com/help/techdoc/ref/asech.html");
		functions.put("asin","asin:\n   Inverse sine; result in radians\nSyntax:\n   Y = asin(X)\nSee Also:\n   asind, sin, sind\nhttp://www.mathworks.com/help/techdoc/ref/asin.html");
		functions.put("asinh","asinh:\n   Inverse hyperbolic sine\nSyntax:\n   Y = asinh(X)\nSee Also:\n   asin, asind, sin, sinh, sind\nhttp://www.mathworks.com/help/techdoc/ref/asinh.html");
		functions.put("atan","atan:\n   Inverse tangent; result in radians\nSyntax:\n   Y = atan(X)\nSee Also:\n   atan2, tan, atand, atanh\nhttp://www.mathworks.com/help/techdoc/ref/atan.html");
		functions.put("atanh","atanh:\n   Inverse hyperbolic tangent\nSyntax:\n   Y = atanh(X)\nSee Also:\n   atan2, atan, tanh\nhttp://www.mathworks.com/help/techdoc/ref/atanh.html");
		functions.put("cos","cos:\n   Cosine of argument in radians\nSyntax:\n   Y = cos(X)\nSee Also:\n   acos, acosd, cosd, cosh\nhttp://www.mathworks.com/help/techdoc/ref/cos.html");
		functions.put("cosh","cosh:\n   Hyperbolic cosine\nSyntax:\n   Y = cosh(X)\nSee Also:\n   acos, acosh, cos\nhttp://www.mathworks.com/help/techdoc/ref/cosh.html");
		functions.put("cot","cot:\n   Cotangent of argument in radians\nSyntax:\n   Y = cot(X)\nSee Also:\n   cotd, coth, acot, acotd, acoth\nhttp://www.mathworks.com/help/techdoc/ref/cot.html");
		functions.put("coth","coth:\n   Hyperbolic cotangent\nSyntax:\n   Y = coth(X)\nSee Also:\n   acot, acoth, cot\nhttp://www.mathworks.com/help/techdoc/ref/coth.html");
		functions.put("csc","csc:\n   Cosecant of argument in radians\nSyntax:\n   Y = csc(x)\nSee Also:\n   cscd, csch, acsc, acscd, acsch\nhttp://www.mathworks.com/help/techdoc/ref/csc.html");
		functions.put("csch","csch:\n   Hyperbolic cosecant\nSyntax:\n   Y = csch(x)\nSee Also:\n   acsc, acsch, csc\nhttp://www.mathworks.com/help/techdoc/ref/csch.html");
		functions.put("degtograd","No help contents yet entered for function");
		functions.put("degtorad","degtorad:\n   Convert angles from degrees to radians\nSyntax:\n   angleInRadians = degtorad(angleInDegrees)\nSee Also:\n   fromDegrees, fromRadians, radtodeg, toDegrees, toRadians\nhttp://www.mathworks.com/help/toolbox/map/ref/degtorad.html");
		functions.put("gradtodeg","No help contents yet entered for function");
		functions.put("gradtorad","No help contents yet entered for function");
		functions.put("radtodeg","radtodeg:\n   Convert angles from radians to degrees\nSyntax:\n   angleInDegrees = radtodeg(angleInRadians)\nSee Also:\n   degtorad, fromDegrees, fromRadians, toDegrees, toRadians\nhttp://www.mathworks.com/help/toolbox/map/ref/radtodeg.html");
		functions.put("radtograd","No help contents yet entered for function");
		functions.put("sec","sec:\n   Secant of argument in radians\nSyntax:\n   Y = sec(X)\nSee Also:\n   secd, sech, asec, asecd, asech\nhttp://www.mathworks.com/help/techdoc/ref/sec.html");
		functions.put("sech","sech:\n   Hyperbolic secant\nSyntax:\n   Y = sech(X)\nSee Also:\n   asec, asech, sec\nhttp://www.mathworks.com/help/techdoc/ref/sech.html");
		functions.put("sin","sin:\n   Sine of argument in radians\nSyntax:\n   Y = sin(X)\nSee Also:\n   sind\nhttp://www.mathworks.com/help/techdoc/ref/sin.html");
		functions.put("sinh","sinh:\n   Hyperbolic sine of argument in radians\nSyntax:\n   Y = sinh(X)\nSee Also:\n   sin, sind, asin, asinh, asind\nhttp://www.mathworks.com/help/techdoc/ref/sinh.html");
		functions.put("tan","tan:\n   Tangent of argument in radians\nSyntax:\n   Y = tan(X)\nSee Also:\n   tand, tanh, atan, atan2, atand, atanh\nhttp://www.mathworks.com/help/techdoc/ref/tan.html");
		functions.put("tanh","tanh:\n   Hyperbolic tangent\nSyntax:\n   Y = tanh(X)\nSee Also:\n   atan, atan2, tan\nhttp://www.mathworks.com/help/techdoc/ref/tanh.html");
		functions.put("acosd","acosd:\n   Inverse cosine; result in degrees\nSyntax:\n   Y = acosd(X)\nSee Also:\n   cosd, acos\nhttp://www.mathworks.com/help/techdoc/ref/acosd.html");
		functions.put("acotd","acotd:\n   Inverse cotangent; result in degrees\nSyntax:\n   Y = acosd(X)\nSee Also:\n   cotd, acot\nhttp://www.mathworks.com/help/techdoc/ref/acotd.html");
		functions.put("acscd","acscd:\n   Inverse cosecant; result in degrees\nSyntax:\n   Y = acscd(X)\nSee Also:\n   cscd, acsc\nhttp://www.mathworks.com/help/techdoc/ref/acscd.html");
		functions.put("asecd","asecd:\n   Inverse secant; result in degrees\nSyntax:\n   Y = asecd(X)\nSee Also:\n   secd, asec\nhttp://www.mathworks.com/help/techdoc/ref/asecd.html");
		functions.put("asind","asind:\n   Inverse sine; result in degrees\nSyntax:\n   Y = asind(X)\nSee Also:\n   asin, sin, sind\nhttp://www.mathworks.com/help/techdoc/ref/asind.html");
		functions.put("atand","atand:\n   Inverse tangent; result in degrees\nSyntax:\n   Y = atand(X)\nSee Also:\n   tand, atan\nhttp://www.mathworks.com/help/techdoc/ref/atand.html");
		functions.put("cosd","cosd:\n   Cosine of argument in degrees\nSyntax:\n   Y = cosd(X)\nSee Also:\n   acosd, cos\nhttp://www.mathworks.com/help/techdoc/ref/cosd.html");
		functions.put("cotd","cotd:\n   Cotangent of argument in degrees\nSyntax:\n   Y = cotd(X)\nSee Also:\n   cot, coth, acot, acotd, acoth\nhttp://www.mathworks.com/help/techdoc/ref/cotd.html");
		functions.put("cscd","cscd:\n   Cosecant of argument in degrees\nSyntax:\n   Y = cscd(X)\nSee Also:\n   csc, csch, acsc, acscd, acsch\nhttp://www.mathworks.com/help/techdoc/ref/cscd.html");
		functions.put("secd","secd:\n   Secant of argument in degrees\nSyntax:\n   Y = secd(X)\nSee Also:\n   sec, sech, asec, asecd, asech\nhttp://www.mathworks.com/help/techdoc/ref/secd.html");
		functions.put("sind","sind:\n   Sine of argument in degrees\nSyntax:\n   Y = sind(X)\nSee Also:\n   sin\nhttp://www.mathworks.com/help/techdoc/ref/sind.html");
		functions.put("tand","tand:\n   Tangent of argument in degrees\nSyntax:\n   Y = tand(X)\nSee Also:\n   tan, tanh, atan, atan2, atand, atanh\nhttp://www.mathworks.com/help/techdoc/ref/tand.html");
		functions.put("time","No help contents yet entered for function");
		functions.put("tic","tic:\n   Measure performance using stopwatch timer\nSyntax:\n   tic; any_statements; toc;\nSee Also:\n   clock, cputime, etime, profile\nhttp://www.mathworks.com/help/techdoc/ref/tic.html");
		functions.put("toc","toc:\n   Measure performance using stopwatch timer\nSyntax:\n   tic; any_statements; toc;\nSee Also:\n   clock, cputime, etime, profile\nhttp://www.mathworks.com/help/techdoc/ref/toc.html");
		functions.put("pause","pause:\n   Halt execution temporarily\nSyntax:\n   pause\nSee Also:\n   keyboard, input, drawnow\nhttp://www.mathworks.com/help/techdoc/ref/pause.html");
		functions.put("date","date:\n   Current date string\nSyntax:\n   str = date\nSee Also:\n   clock, datestr, datenum, now\nhttp://www.mathworks.com/help/techdoc/ref/date.html");
		functions.put("is_leap_year","No help contents yet entered for function");
		functions.put("blanks","blanks:\n   Create string of blank characters\nSyntax:\n   blanks(n)\nSee Also:\n   clc, format, home\nhttp://www.mathworks.com/help/techdoc/ref/blanks.html");
		functions.put("char","char:\n   Convert to character array (string)\nSyntax:\n   S = char(X)\nSee Also:\n   ischar, isletter, isspace, isstrprop, cellstr, iscellstr, get, set, strings, text\nhttp://www.mathworks.com/help/techdoc/ref/char.html");
		functions.put("deblank","deblank:\n   Strip trailing blanks from end of string\nSyntax:\n   str = deblank(str)\nSee Also:\n   strjust, strtrim\nhttp://www.mathworks.com/help/techdoc/ref/deblank.html");
		functions.put("findstr","findstr:\n   Find string within another, longer string\nSyntax:\n   k = findstr(str1, str2)\nSee Also:\n   strfind, strtok, strcmp, strncmp, strcmpi, strncmpi, regexp, regexpi, regexprep\nhttp://www.mathworks.com/help/techdoc/ref/findstr.html");
		functions.put("isspace","isspace:\n   Array elements that are space characters\nSyntax:\n   tf = isspace('str')\nSee Also:\n   isletter, isstrprop, ischar, strings, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/isspace.html");
		functions.put("lower","lower:\n   Convert string to lowercase\nSyntax:\n   t = lower('str')\nSee Also:\n   upper\nhttp://www.mathworks.com/help/techdoc/ref/lower.html");
		functions.put("num2str","num2str:\n   Convert number to string\nSyntax:\n   str = num2str(A)\nSee Also:\n   mat2str, int2str, str2num, sprintf, fprintf\nhttp://www.mathworks.com/help/techdoc/ref/num2str.html");
		functions.put("sprintf","sprintf:\n   Format data into string\nSyntax:\n   str = sprintf(format, A, ...)\nSee Also:\n   char, fprintf, fscanf, int2str, num2str, sscanf\nhttp://www.mathworks.com/help/techdoc/ref/sprintf.html");
		functions.put("str2num","str2num:\n   Convert string to number\nSyntax:\n   x = str2num('str')\nSee Also:\n   num2str, str2double, hex2num, sscanf, sparse, char\nhttp://www.mathworks.com/help/techdoc/ref/str2num.html");
		functions.put("strcat","strcat:\n   Concatenate strings horizontally\nSyntax:\n   combinedStr = strcat(s1, s2, ..., sN)\nSee Also:\n   cat, cellstr, horzcat, special character, vertcat\nhttp://www.mathworks.com/help/techdoc/ref/strcat.html");
		functions.put("strcmp","strcmp:\n   Compare strings\nSyntax:\n   TF = strcmp('str1', 'str2')\nSee Also:\n   strncmp, strncmpi, strfind, regexp, regexpi, regexprep, regexptranslate\nhttp://www.mathworks.com/help/techdoc/ref/strcmp.html");
		functions.put("strcmpi","strcmpi:\n   Compare strings\nSyntax:\n   TF = strcmp('str1', 'str2')\nSee Also:\n   strncmp, strncmpi, strfind, regexp, regexpi, regexprep, regexptranslate\nhttp://www.mathworks.com/help/techdoc/ref/strcmpi.html");
		functions.put("strfind","strfind:\n   Find one string within another\nSyntax:\n   k = strfind(str, pattern)\nSee Also:\n   strtok, strcmp, strncmp, strcmpi, strncmpi, regexp, regexpi, regexprep\nhttp://www.mathworks.com/help/techdoc/ref/strfind.html");
		functions.put("strlength","No help contents yet entered for function");
		functions.put("strncmp","strncmp:\n   Compare first n characters of strings\nSyntax:\n   TF = strncmp('str1', 'str2', n)\nSee Also:\n   strcmp, strcmpi, strfind, regexp, regexpi, regexprep, regexptranslate\nhttp://www.mathworks.com/help/techdoc/ref/strncmp.html");
		functions.put("strncmpi","strncmpi:\n   Compare first n characters of strings\nSyntax:\n   TF = strncmp('str1', 'str2', n)\nSee Also:\n   strcmp, strcmpi, strfind, regexp, regexpi, regexprep, regexptranslate\nhttp://www.mathworks.com/help/techdoc/ref/strncmpi.html");
		functions.put("strvcat","strvcat:\n   Concatenate strings vertically\nSyntax:\n   S = strvcat(t1, t2, t3, ...)\nSee Also:\n   strcat, cat, vertcat, horzcat, int2str, mat2str, num2str, strings, special character, []\nhttp://www.mathworks.com/help/techdoc/ref/strvcat.html");
		functions.put("substring","No help contents yet entered for function");
		functions.put("upper","upper:\n   Convert string to uppercase\nSyntax:\n   t = upper('str')\nSee Also:\n   lower\nhttp://www.mathworks.com/help/techdoc/ref/upper.html");
		functions.put("average","No help contents yet entered for function");
		functions.put("variation","No help contents yet entered for function");
		functions.put("center","No help contents yet entered for function");
		functions.put("cloglog","No help contents yet entered for function");
		functions.put("cov","cov:\n   Covariance matrix\nSyntax:\n   cov(x)\nSee Also:\n   corrcoef, mean, median, std, var\nhttp://www.mathworks.com/help/techdoc/ref/cov.html");
		functions.put("mahalanobis","No help contents yet entered for function");
		functions.put("mean","mean:\n   Average or mean value of array\nSyntax:\n   M = mean(A)\nSee Also:\n   corrcoef, cov, max, median, min, mode, std, var\nhttp://www.mathworks.com/help/techdoc/ref/mean.html");
		functions.put("meansq","No help contents yet entered for function");
		functions.put("std","std:\n   Standard deviation\nSyntax:\n   s = std(X)\nSee Also:\n   corrcoef, cov, mean, median, var\nhttp://www.mathworks.com/help/techdoc/ref/std.html");
		functions.put("var","var:\n   Variance\nSyntax:\n   V = var(X)\nSee Also:\n   corrcoef, cov, mean, median, std\nhttp://www.mathworks.com/help/techdoc/ref/var.html");
		functions.put("logistic_regression_derivatives","No help contents yet entered for function");
		functions.put("hankel","hankel:\n   Hankel matrix\nSyntax:\n   H = hankel(c)\nSee Also:\n   hadamard, toeplitz, kron\nhttp://www.mathworks.com/help/techdoc/ref/hankel.html");
		functions.put("hilb","hilb:\n   Hilbert matrix\nSyntax:\n   H = hilb(n)\nSee Also:\n   invhilb\nhttp://www.mathworks.com/help/techdoc/ref/hilb.html");
		functions.put("lauchli","No help contents yet entered for function");
		functions.put("pascal","pascal:\n   Pascal matrix\nSyntax:\n   A = pascal(n)\nSee Also:\n   chol\nhttp://www.mathworks.com/help/techdoc/ref/pascal.html");
		functions.put("rosser","No help contents yet entered for function");
		functions.put("sylvester_matrix","No help contents yet entered for function");
		functions.put("toeplitz","toeplitz:\n   Toeplitz matrix\nSyntax:\n   T = toeplitz(c,r)\nSee Also:\n   hankel, kron\nhttp://www.mathworks.com/help/techdoc/ref/toeplitz.html");
		functions.put("wilkinson","wilkinson:\n   Wilkinson's eigenvalue test matrix\nSyntax:\n   W = wilkinson(n)\nSee Also:\n   eig, gallery, pascal\nhttp://www.mathworks.com/help/techdoc/ref/wilkinson.html");
		functions.put("bessel","No help contents yet entered for function");
		functions.put("beta","beta:\n   Beta function\nSyntax:\n   B = beta(Z,W)\nSee Also:\n   betainc, betaln, gammaln\nhttp://www.mathworks.com/help/techdoc/ref/beta.html");
		functions.put("betaln","betaln:\n   Logarithm of beta function\nSyntax:\n   L = betaln(Z,W)\nSee Also:\n   beta, betainc, gammaln\nhttp://www.mathworks.com/help/techdoc/ref/betaln.html");
		functions.put("gammaln","gammaln:\n   Logarithm of gamma function\nSyntax:\n   Y = gammaln(A)\nSee Also:\n   gammainc, gammaincinv, gamma, psi\nhttp://www.mathworks.com/help/techdoc/ref/gammaln.html");
		functions.put("log2","log2:\n   Base 2 logarithm and dissect floating-point numbers into exponent and mantissa\nSyntax:\n   Y = log2(X)\nSee Also:\n   log, pow2\nhttp://www.mathworks.com/help/techdoc/ref/log2.html");
		functions.put("perms","perms:\n   All possible permutations\nSyntax:\n   P = perms(v)\nSee Also:\n   nchoosek, permute, randperm\nhttp://www.mathworks.com/help/techdoc/ref/perms.html");
		functions.put("bartlett","No help contents yet entered for function");
		functions.put("blackman","No help contents yet entered for function");
		functions.put("durbinlevinson","No help contents yet entered for function");
		functions.put("hanning","No help contents yet entered for function");
		functions.put("hurst","No help contents yet entered for function");
		functions.put("rectangle_lw","No help contents yet entered for function");
		functions.put("rectangle_sw","No help contents yet entered for function");
		functions.put("sinc","No help contents yet entered for function");
		functions.put("sinewave","No help contents yet entered for function");
		functions.put("triangle_lw","No help contents yet entered for function");
		functions.put("triangle_sw","No help contents yet entered for function");
		functions.put("complement","No help contents yet entered for function");
		functions.put("create_set","No help contents yet entered for function");
		functions.put("union","union:\n   Find set union of two vectors\nSyntax:\n   c = union(A, B)\nSee Also:\n   intersect, setdiff, setxor, unique, ismember, issorted\nhttp://www.mathworks.com/help/techdoc/ref/union.html");
		functions.put("qconj","No help contents yet entered for function");
		functions.put("qderiv","No help contents yet entered for function");
		functions.put("qderivmat","No help contents yet entered for function");
		functions.put("qinv","No help contents yet entered for function");
		functions.put("qmult","No help contents yet entered for function");
		functions.put("qtrans","No help contents yet entered for function");
		functions.put("qtransv","No help contents yet entered for function");
		functions.put("qtransvmat","No help contents yet entered for function");
		functions.put("quaternion","No help contents yet entered for function");
		functions.put("binomial","No help contents yet entered for function");
		functions.put("compan","compan:\n   Companion matrix\nSyntax:\n   A = compan(u)\nSee Also:\n   eig, poly, polyval, roots\nhttp://www.mathworks.com/help/techdoc/ref/compan.html");
		functions.put("mkpp","mkpp:\n   Make piecewise polynomial\nSyntax:\n   pp = mkpp(breaks,coefs)\nSee Also:\n   ppval, spline, unmkpp\nhttp://www.mathworks.com/help/techdoc/ref/mkpp.html");
		functions.put("poly","poly:\n   Polynomial with specified roots\nSyntax:\n   p = poly(A)\nSee Also:\n   conv, polyval, residue, roots\nhttp://www.mathworks.com/help/techdoc/ref/poly.html");
		functions.put("polyinteg","No help contents yet entered for function");
		functions.put("polyreduce","No help contents yet entered for function");
		functions.put("polyval","polyval:\n   Polynomial evaluation\nSyntax:\n   y = polyval(p,x)\nSee Also:\n   polyfit, polyvalm, polyder, polyint\nhttp://www.mathworks.com/help/techdoc/ref/polyval.html");
		functions.put("polyvalm","polyvalm:\n   Matrix polynomial evaluation\nSyntax:\n   Y = polyvalm(p,X)\nSee Also:\n   polyfit, polyval\nhttp://www.mathworks.com/help/techdoc/ref/polyvalm.html");
		functions.put("roots","roots:\n   Polynomial roots\nSyntax:\n   r = roots(c)\nSee Also:\n   fzero, poly, residue\nhttp://www.mathworks.com/help/techdoc/ref/roots.html");
		functions.put("unmkpp","unmkpp:\n   Piecewise polynomial details\nSyntax:\n   [breaks,coefs,l,k,d] = unmkpp(pp)\nSee Also:\n   mkpp, ppval, spline\nhttp://www.mathworks.com/help/techdoc/ref/unmkpp.html");
		functions.put("physical_constant","No help contents yet entered for function");
		functions.put("urlread","urlread:\n   Download content at URL into Addi string\nSyntax:\n   str = urlread(URL)\nSee Also:\n   ftp, urlwrite, web\nhttp://www.mathworks.com/help/techdoc/ref/urlread.html");
		functions.put("ans","ans:\n   Most recent answer\nSyntax:\n   ans\nSee Also:\n   display\nhttp://www.mathworks.com/help/techdoc/ref/ans.html");
		functions.put("comma","No help contents yet entered for function");
		functions.put("flops","No help contents yet entered for function");
		functions.put("mexext","mexext:\n   Binary MEX-file name extension\nSyntax:\n   ext = mexext\nSee Also:\n   mex\nhttp://www.mathworks.com/help/techdoc/ref/mexext.html");
		functions.put("semicolon","No help contents yet entered for function");
		functions.put("single","single:\n   Convert to single precision\nSyntax:\n   B = single(A)\nSee Also:\n   double\nhttp://www.mathworks.com/help/techdoc/ref/single.html");
		functions.put("texas_lotto","No help contents yet entered for function");
		functions.put("commutation_matrix","No help contents yet entered for function");
		functions.put("dmult","No help contents yet entered for function");
		functions.put("dot","dot:\n   Vector dot product\nSyntax:\n   C = dot(A,B)\nSee Also:\n   cross\nhttp://www.mathworks.com/help/techdoc/ref/dot.html");
		functions.put("duplication_matrix","No help contents yet entered for function");
		functions.put("housh","No help contents yet entered for function");
		functions.put("logm","logm:\n   Matrix logarithm\nSyntax:\n   L = logm(A)\nSee Also:\n   expm, funm, sqrtm\nhttp://www.mathworks.com/help/techdoc/ref/logm.html");
		functions.put("norm","norm:\n   Vector and matrix norms\nSyntax:\n   n = norm(A)\nSee Also:\n   cond, condest, hypot, normest, rcond\nhttp://www.mathworks.com/help/techdoc/ref/norm.html");
		functions.put("null","null:\n   Null space \nSyntax:\n   Z = null(A)\nSee Also:\n   orth, rank, rref, svd\nhttp://www.mathworks.com/help/techdoc/ref/null.html");
		functions.put("orth","orth:\n   Range space of matrix\nSyntax:\n   B = orth(A)\nSee Also:\n   null, svd, rank\nhttp://www.mathworks.com/help/techdoc/ref/orth.html");
		functions.put("rank","rank:\n   Rank of matrix\nSyntax:\n   k = rank(A)\nSee Also:\n   sprank\nhttp://www.mathworks.com/help/techdoc/ref/rank.html");
		functions.put("trace","trace:\n   Sum of diagonal elements\nSyntax:\n   b = trace(A)\nSee Also:\n   det, eig\nhttp://www.mathworks.com/help/techdoc/ref/trace.html");
		functions.put("vec","No help contents yet entered for function");
		functions.put("vech","No help contents yet entered for function");
		functions.put("abcddim","No help contents yet entered for function");
		functions.put("is_abcd","No help contents yet entered for function");
		functions.put("is_sample","No help contents yet entered for function");
		functions.put("ss2sys","No help contents yet entered for function");
		functions.put("sysgettsam","No help contents yet entered for function");
		functions.put("sysgettype","No help contents yet entered for function");
		functions.put("axis2dlim","No help contents yet entered for function");
		functions.put("swap","No help contents yet entered for function");
		functions.put("zgfmul","No help contents yet entered for function");
		functions.put("aes","No help contents yet entered for function");
		functions.put("vdp1","No help contents yet entered for function");
		functions.put("vdp2","No help contents yet entered for function");
		functions.put("finite","No help contents yet entered for function");
		functions.put("isstr","No help contents yet entered for function");
		functions.put("engine","No help contents yet entered for function");
		functions.put("fv","No help contents yet entered for function");
		functions.put("fvl","No help contents yet entered for function");
		functions.put("nper","No help contents yet entered for function");
		functions.put("npv","No help contents yet entered for function");
		functions.put("pmt","No help contents yet entered for function");
		functions.put("pv","No help contents yet entered for function");
		functions.put("pvl","No help contents yet entered for function");
		functions.put("vol","No help contents yet entered for function");
		functions.put("ftp","No help contents yet entered for function");
		functions.put("euler","No help contents yet entered for function");
		functions.put("eulerm","No help contents yet entered for function");
		functions.put("feval","feval:\n   Evaluate function\nSyntax:\n   [y1, y2, ...] = feval(fhandle, x1, ..., xn)\nSee Also:\n   assignin, function_handle, functions, builtin, eval, evalin\nhttp://www.mathworks.com/help/techdoc/ref/feval.html");
		functions.put("angle","angle:\n   Phase angle\nSyntax:\n   P = angle(Z)\nSee Also:\n   abs, atan2, unwrap\nhttp://www.mathworks.com/help/techdoc/ref/angle.html");
		functions.put("beep","No help contents yet entered for function");
		functions.put("bench","bench:\n   Addi benchmark\nSyntax:\n   bench\nSee Also:\n   profile, profsave, mlint, mlintrpt, memory, pack, tic, cputime, rehash\nhttp://www.mathworks.com/help/techdoc/ref/bench.html");
		functions.put("bitand","bitand:\n   Bitwise AND\nSyntax:\n   C = bitand(A, B)\nSee Also:\n   bitcmp, bitget, bitmax, bitor, bitset, bitshift, bitxor\nhttp://www.mathworks.com/help/techdoc/ref/bitand.html");
		functions.put("bitor","bitor:\n   Bitwise OR\nSyntax:\n   C = bitor(A, B)\nSee Also:\n   bitand, bitcmp, bitget, bitmax, bitset, bitshift, bitxor\nhttp://www.mathworks.com/help/techdoc/ref/bitor.html");
		functions.put("bitshift","bitshift:\n   Shift bits specified number of places\nSyntax:\n   C = bitshift(A, k)\nSee Also:\n   bitand, bitcmp, bitget, bitmax, bitor, bitset, bitxor, fix\nhttp://www.mathworks.com/help/techdoc/ref/bitshift.html");
		functions.put("bitxor","bitxor:\n   Bitwise XOR\nSyntax:\n   C = bitxor(A, B)\nSee Also:\n   bitand, bitcmp, bitget, bitmax, bitor, bitset, bitshift\nhttp://www.mathworks.com/help/techdoc/ref/bitxor.html");
		functions.put("cell","No help contents yet entered for function");
		functions.put("class","No help contents yet entered for function");
		functions.put("clear","clear:\n   Remove items from workspace, freeing up system memory\nSyntax:\n   clear\nSee Also:\n   clc, clearvars, close, delete, import, inmem, load, memory, mlock, munlock, pack, persistent, save, who, whos, workspace\nhttp://www.mathworks.com/help/techdoc/ref/clear.html");
		functions.put("clock","clock:\n   Current time as date vector\nSyntax:\n   c = clock\nSee Also:\n   cputime, datenum, datevec, now, etime, tic, toc\nhttp://www.mathworks.com/help/techdoc/ref/clock.html");
		functions.put("combinations","No help contents yet entered for function");
		functions.put("complex","complex:\n   Construct complex data from real and imaginary components\nSyntax:\n   c = complex(a,b)\nSee Also:\n   abs, angle, conj, i, imag, isreal, j, real\nhttp://www.mathworks.com/help/techdoc/ref/complex.html");
		functions.put("conj","conj:\n   Complex conjugate\nSyntax:\n   ZC = conj(Z)\nSee Also:\n   i, j, imag, real\nhttp://www.mathworks.com/help/techdoc/ref/conj.html");
		functions.put("diff","diff:\n   Differences and approximate derivatives\nSyntax:\n   Y = diff(X)\nSee Also:\n   gradient, prod, sum\nhttp://www.mathworks.com/help/techdoc/ref/diff.html");
		functions.put("factor","factor:\n   Prime factors\nSyntax:\n   f = factor(n)\nSee Also:\n   isprime, primes\nhttp://www.mathworks.com/help/techdoc/ref/factor.html");
		functions.put("fft","fft:\n   Discrete Fourier transform\nSyntax:\n   Y = fft(X)\nSee Also:\n   fft2, fftn, fftw, fftshift, ifft\nhttp://www.mathworks.com/help/techdoc/ref/fft.html");
		functions.put("fibonacci","No help contents yet entered for function");
		functions.put("finish","No help contents yet entered for function");
		functions.put("fix","fix:\n   Round toward zero\nSyntax:\n   B = fix(A)\nSee Also:\n   ceil, floor, round\nhttp://www.mathworks.com/help/techdoc/ref/fix.html");
		functions.put("func2str","func2str:\n   Construct function name string from function handle\nSyntax:\n   func2str(fhandle)\nSee Also:\n   function_handle, str2func, functions\nhttp://www.mathworks.com/help/techdoc/ref/func2str.html");
		functions.put("getpfilecaching","No help contents yet entered for function");
		functions.put("global","global:\n   Declare global variables\nSyntax:\n   global X Y Z\nSee Also:\n   clear, isglobal, who\nhttp://www.mathworks.com/help/techdoc/ref/global.html");
		functions.put("harmonic","No help contents yet entered for function");
		functions.put("imag","imag:\n   Imaginary part of complex number\nSyntax:\n   Y = imag(Z)\nSee Also:\n   conj, i, j, real\nhttp://www.mathworks.com/help/techdoc/ref/imag.html");
		functions.put("int16","No help contents yet entered for function");
		functions.put("int32","No help contents yet entered for function");
		functions.put("int64","No help contents yet entered for function");
		functions.put("int8","No help contents yet entered for function");
		functions.put("isa","isa:\n   Determine whether input is object of given class\nSyntax:\n   K = isa(obj, 'class_name')\nSee Also:\n   class, is*, exist\nhttp://www.mathworks.com/help/techdoc/ref/isa.html");
		functions.put("iscell","iscell:\n   Determine whether input is cell array\nSyntax:\n   tf = iscell(A)\nSee Also:\n   cell, iscellstr, isstruct, isnumeric, islogical, isobject, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/iscell.html");
		functions.put("ischar","ischar:\n   Determine whether item is character array\nSyntax:\n   tf = ischar(A)\nSee Also:\n   char, strings, isletter, isspace, isstrprop, iscellstr, isnumeric, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/ischar.html");
		functions.put("isdefinite","No help contents yet entered for function");
		functions.put("isdouble","No help contents yet entered for function");
		functions.put("isfunctionhandle","No help contents yet entered for function");
		functions.put("isglobal","isglobal:\n   Determine whether input is global variable\nSyntax:\n   tf = isglobal(A)\nSee Also:\n   global, isvarname, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/isglobal.html");
		functions.put("isint16","No help contents yet entered for function");
		functions.put("isint32","No help contents yet entered for function");
		functions.put("isint64","No help contents yet entered for function");
		functions.put("isint8","No help contents yet entered for function");
		functions.put("islogical","islogical:\n   Determine whether input is logical array\nSyntax:\n   tf = islogical(A)\nSee Also:\n   logical, isnumeric, ischar, isreal, elementwise, short-circuit, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/islogical.html");
		functions.put("ismatrix","ismatrix:\n   Determine whether input is matrix\nSyntax:\n   ismatrix(V)\nSee Also:\n   iscolumn, isrow, isscalar, isvector\nhttp://www.mathworks.com/help/techdoc/ref/ismatrix.html");
		functions.put("isnumeric","isnumeric:\n   Determine whether input is numeric array\nSyntax:\n   tf = isnumeric(A)\nSee Also:\n   isstrprop, isnan, isreal, isprime, isfinite, isinf, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/isnumeric.html");
		functions.put("isprime","isprime:\n   Array elements that are prime numbers\nSyntax:\n   TF = isprime(A)\nSee Also:\n   is*\nhttp://www.mathworks.com/help/techdoc/ref/isprime.html");
		functions.put("isscalar","isscalar:\n   Determine whether input is scalar\nSyntax:\n   isscalar(A)\nSee Also:\n   isvector, ismatrix, isrow, iscolumn, islogical, ischar, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/isscalar.html");
		functions.put("issingle","No help contents yet entered for function");
		functions.put("issqare","No help contents yet entered for function");
		functions.put("isstruct","isstruct:\n   Determine whether input is structure array\nSyntax:\n   tf = isstruct(A)\nSee Also:\n   struct, isfield, iscell, ischar, isobject, isnumeric, islogical, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/isstruct.html");
		functions.put("isstudent","isstudent:\n   Determine if version is Student Version\nSyntax:\n   tf = isstudent\nSee Also:\n   ver, version, license, ispc, isunix, is*\nhttp://www.mathworks.com/help/techdoc/ref/isstudent.html");
		functions.put("issymmetric","No help contents yet entered for function");
		functions.put("isuint16","No help contents yet entered for function");
		functions.put("isuint32","No help contents yet entered for function");
		functions.put("isuint64","No help contents yet entered for function");
		functions.put("isuint8","No help contents yet entered for function");
		functions.put("isvector","isvector:\n   Determine whether input is vector\nSyntax:\n   isvector(A)\nSee Also:\n   iscolumn, ismatrix, isrow, isscalar, isempty, isnumeric, islogical, ischar, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/isvector.html");
		functions.put("length","length:\n   Length of vector or largest array dimension\nSyntax:\n   numberOfElements = length(array)\nSee Also:\n   ndims, numel, size\nhttp://www.mathworks.com/help/techdoc/ref/length.html");
		functions.put("linspace","linspace:\n   Generate linearly spaced vectors\nSyntax:\n   y = linspace(a,b)\nSee Also:\n   logspace\nhttp://www.mathworks.com/help/techdoc/ref/linspace.html");
		functions.put("logical","logical:\n   Convert numeric values to logical\nSyntax:\n   K = logical(A)\nSee Also:\n   true, false, islogical\nhttp://www.mathworks.com/help/techdoc/ref/logical.html");
		functions.put("logspace","logspace:\n   Generate logarithmically spaced vectors\nSyntax:\n   y = logspace(a,b)\nSee Also:\n   linspace\nhttp://www.mathworks.com/help/techdoc/ref/logspace.html");
		functions.put("lookup","No help contents yet entered for function");
		functions.put("mod","mod:\n   Modulus after division\nSyntax:\n   M = mod(X,Y)\nSee Also:\n   rem\nhttp://www.mathworks.com/help/techdoc/ref/mod.html");
		functions.put("ndims","ndims:\n   Number of array dimensions\nSyntax:\n   n = ndims(A)\nSee Also:\n   size\nhttp://www.mathworks.com/help/techdoc/ref/ndims.html");
		functions.put("nextpow2","nextpow2:\n   Next higher power of 2\nSyntax:\n   p = nextpow2(A)\nSee Also:\n   fft, log2, pow2\nhttp://www.mathworks.com/help/techdoc/ref/nextpow2.html");
		functions.put("nthroot","nthroot:\n   Real nth root of real numbers\nSyntax:\n   y = nthroot(X, n)\nSee Also:\n   power, sqrt\nhttp://www.mathworks.com/help/techdoc/ref/nthroot.html");
		functions.put("numel","numel:\n   Number of elements in array or subscripted array expression\nSyntax:\n   n = numel(A)\nSee Also:\n   nargin, nargout, prod, size, subsasgn, subsref\nhttp://www.mathworks.com/help/techdoc/ref/numel.html");
		functions.put("performfunction","No help contents yet entered for function");
		functions.put("permutations","No help contents yet entered for function");
		functions.put("primes","primes:\n   Generate list of prime numbers\nSyntax:\n   p = primes(n)\nSee Also:\n   factor\nhttp://www.mathworks.com/help/techdoc/ref/primes.html");
		functions.put("rand","rand:\n   Uniformly distributed pseudorandom numbers\nSyntax:\n   rand(n)\nSee Also:\n   randi, randn, @RandStream, rand (RandStream), getDefaultStream (RandStream), sprand, sprandn, randperm\nhttp://www.mathworks.com/help/techdoc/ref/rand.html");
		functions.put("randperm","randperm:\n   Random permutation \nSyntax:\n   p = randperm(n)\nSee Also:\n   permute\nhttp://www.mathworks.com/help/techdoc/ref/randperm.html");
		functions.put("real","real:\n   Real part of complex number\nSyntax:\n   X = real(Z)\nSee Also:\n   abs, angle, conj, i, j, imag\nhttp://www.mathworks.com/help/techdoc/ref/real.html");
		functions.put("rem","rem:\n   Remainder after division\nSyntax:\n   R = rem(X,Y)\nSee Also:\n   mod\nhttp://www.mathworks.com/help/techdoc/ref/rem.html");
		functions.put("setpfilecaching","No help contents yet entered for function");
		functions.put("sign","sign:\n   Signum function\nSyntax:\n   Y = sign(X)\nSee Also:\n   abs, conj, imag, real\nhttp://www.mathworks.com/help/techdoc/ref/sign.html");
		functions.put("size","size:\n   Array dimensions\nSyntax:\n   d = size(X)\nSee Also:\n   exist, length, numel, whos\nhttp://www.mathworks.com/help/techdoc/ref/size.html");
		functions.put("startup","startup:\n   Startup file for user-defined options\nSyntax:\n   startup \nSee Also:\n   finish, Addirc, Addiroot, path, quit, userpath\nhttp://www.mathworks.com/help/techdoc/ref/startup.html");
		functions.put("str2func","str2func:\n   Construct function handle from function name string\nSyntax:\n   str2func('str')\nSee Also:\n   function_handle, func2str, functions\nhttp://www.mathworks.com/help/techdoc/ref/str2func.html");
		functions.put("struct","struct:\n   Create structure array\nSyntax:\n   s = struct('field1', values1, 'field2', values2, ...)\nSee Also:\n   cell2struct, deal, fieldnames, getfield, isfield, isstruct, namelengthmax, orderfields, rmfield, setfield, struct2cell, substruct\nhttp://www.mathworks.com/help/techdoc/ref/struct.html");
		functions.put("tril","tril:\n   Lower triangular part of matrix\nSyntax:\n   L = tril(X)\nSee Also:\n   diag, triu\nhttp://www.mathworks.com/help/techdoc/ref/tril.html");
		functions.put("triu","triu:\n   Upper triangular part of matrix\nSyntax:\n   U = triu(X)\nSee Also:\n   diag, tril\nhttp://www.mathworks.com/help/techdoc/ref/triu.html");
		functions.put("who","who:\n   List variables in workspace\nSyntax:\n   who\nSee Also:\n   assignin, clear, computer, dir, evalin, exist, inmem, load, save, what, workspace\nhttp://www.mathworks.com/help/techdoc/ref/who.html");
		functions.put("whos","whos:\n   List variables in workspace\nSyntax:\n   who\nSee Also:\n   assignin, clear, computer, dir, evalin, exist, inmem, load, save, what, workspace\nhttp://www.mathworks.com/help/techdoc/ref/whos.html");
		functions.put("autumn","No help contents yet entered for function");
		functions.put("bone","No help contents yet entered for function");
		functions.put("brighten","brighten:\n   Brighten or darken colormap\nSyntax:\n   brighten(beta)\nSee Also:\n   colormap, rgbplot\nhttp://www.mathworks.com/help/techdoc/ref/brighten.html");
		functions.put("colormap","colormap:\n   Set and get current colormap\nSyntax:\n   colormap(map)\nSee Also:\n   brighten, caxis, colorbar, colormapeditor, contrast, hsv2rgb, pcolor, rgbplot, rgb2hsv\nhttp://www.mathworks.com/help/techdoc/ref/colormap.html");
		functions.put("cool","No help contents yet entered for function");
		functions.put("copper","No help contents yet entered for function");
		functions.put("gray","No help contents yet entered for function");
		functions.put("gray2ind","No help contents yet entered for function");
		functions.put("hot","No help contents yet entered for function");
		functions.put("jet","No help contents yet entered for function");
		functions.put("ntsc2rgb","No help contents yet entered for function");
		functions.put("pink","No help contents yet entered for function");
		functions.put("rainbow","No help contents yet entered for function");
		functions.put("spring","No help contents yet entered for function");
		functions.put("summer","No help contents yet entered for function");
		functions.put("white","No help contents yet entered for function");
		functions.put("winter","No help contents yet entered for function");
		functions.put("crule","No help contents yet entered for function");
		functions.put("ncrule","No help contents yet entered for function");
		functions.put("cd","cd:\n   Change current folder\nSyntax:\n   cd(newFolder)\nSee Also:\n   dir, fileparts, path, pwd, what\nhttp://www.mathworks.com/help/techdoc/ref/cd.html");
		functions.put("createnewfile","No help contents yet entered for function");
		functions.put("csvread","csvread:\n   Read comma-separated value file\nSyntax:\n   M = csvread(filename)\nSee Also:\n   csvwrite, dlmread, textscan, file formats, importdata, uiimport\nhttp://www.mathworks.com/help/techdoc/ref/csvread.html");
		functions.put("csvwrite","csvwrite:\n   Write comma-separated value file\nSyntax:\n   csvwrite(filename,M)\nSee Also:\n   csvread, dlmwrite, xlswrite, file formats, importdata, uiimport\nhttp://www.mathworks.com/help/techdoc/ref/csvwrite.html");
		functions.put("delete","No help contents yet entered for function");
		functions.put("dir","dir:\n   Folder listing\nSyntax:\n   dir\nSee Also:\n   cd, fileattrib, isdir, ls, mkdir, rmdir, what\nhttp://www.mathworks.com/help/techdoc/ref/dir.html");
		functions.put("exist","exist:\n   Check existence of variable, function, folder, or class\nSyntax:\n   exist name\nSee Also:\n   assignin, computer, dir, evalin, help, inmem, isfield, isempty, lookfor, mfilename, what, which, who\nhttp://www.mathworks.com/help/techdoc/ref/exist.html");
		functions.put("isdirectory","No help contents yet entered for function");
		functions.put("isfile","No help contents yet entered for function");
		functions.put("ishidden","No help contents yet entered for function");
		functions.put("lastmodified","No help contents yet entered for function");
		functions.put("load","load:\n   Load data from MAT-file into workspace\nSyntax:\n   S = load(filename)\nSee Also:\n   clear, fileformats, importdata, regexp, save, uiimport, whos\nhttp://www.mathworks.com/help/techdoc/ref/load.html");
		functions.put("loadvariables","No help contents yet entered for function");
		functions.put("mkdir","mkdir:\n   Make new folder\nSyntax:\n   mkdir('folderName')\nSee Also:\n   copyfile, cd, dir, ls, movefile, rmdir\nhttp://www.mathworks.com/help/techdoc/ref/mkdir.html");
		functions.put("pwd","pwd:\n   Identify current folder\nSyntax:\n   pwd\nSee Also:\n   cd, dir\nhttp://www.mathworks.com/help/techdoc/ref/pwd.html");
		functions.put("rmdir","rmdir:\n   Remove folder\nSyntax:\n   rmdir('folderName')\nSee Also:\n   catch, cd, copyfile, delete, dir, fileattrib, filebrowser, MException, mkdir, movefile, try\nhttp://www.mathworks.com/help/techdoc/ref/rmdir.html");
		functions.put("runfile","No help contents yet entered for function");
		functions.put("savevariables","No help contents yet entered for function");
		functions.put("systemcommand","No help contents yet entered for function");
		functions.put("abs","abs:\n   Absolute value and complex magnitude\nSyntax:\n   abs(X)\nSee Also:\n   angle, sign, unwrap\nhttp://www.mathworks.com/help/techdoc/ref/abs.html");
		functions.put("adjoint","No help contents yet entered for function");
		functions.put("all","all:\n   Determine whether all array elements are nonzero or true\nSyntax:\n   B = all(A)\nSee Also:\n   any, :\nhttp://www.mathworks.com/help/techdoc/ref/all.html");
		functions.put("and","and:\n   Find logical AND of array or scalar inputs\nSyntax:\n   A &; B &; ...\nSee Also:\n   bitand, or, xor, not, any, all\nhttp://www.mathworks.com/help/techdoc/ref/and.html");
		functions.put("any","any:\n   Determine whether any array elements are nonzero\nSyntax:\n   B = any(A)\nSee Also:\n   all, colon\nhttp://www.mathworks.com/help/techdoc/ref/any.html");
		functions.put("ceil","ceil:\n   Round toward positive infinity\nSyntax:\n   B = ceil(A)\nSee Also:\n   fix, floor, round\nhttp://www.mathworks.com/help/techdoc/ref/ceil.html");
		functions.put("chol","chol:\n   Cholesky factorization\nSyntax:\n   R = chol(A)\nSee Also:\n   cholinc, cholupdate\nhttp://www.mathworks.com/help/techdoc/ref/chol.html");
		functions.put("col","No help contents yet entered for function");
		functions.put("columns","No help contents yet entered for function");
		functions.put("ctranspose","No help contents yet entered for function");
		functions.put("cumprod","cumprod:\n   Cumulative product\nSyntax:\n   B = cumprod(A)\nSee Also:\n   cumsum, prod, sum\nhttp://www.mathworks.com/help/techdoc/ref/cumprod.html");
		functions.put("cumsum","cumsum:\n   Cumulative sum\nSyntax:\n   B = cumsum(A)\nSee Also:\n   cumprod, prod, sum\nhttp://www.mathworks.com/help/techdoc/ref/cumsum.html");
		functions.put("det","det:\n   Matrix determinant\nSyntax:\n   d = det(X)\nSee Also:\n   cond, condest, inv, lu, rref\nhttp://www.mathworks.com/help/techdoc/ref/det.html");
		functions.put("determinant","No help contents yet entered for function");
		functions.put("diag","diag:\n   Diagonal matrices and diagonals of matrix\nSyntax:\n   X = diag(v,k)\nSee Also:\n   spdiags, tril, triu, blkdiag\nhttp://www.mathworks.com/help/techdoc/ref/diag.html");
		functions.put("eig","eig:\n   Eigenvalues and eigenvectors\nSyntax:\n   d = eig(A)\nSee Also:\n   balance, condeig, eigs, hess, qz, schur\nhttp://www.mathworks.com/help/techdoc/ref/eig.html");
		functions.put("elementat","No help contents yet entered for function");
		functions.put("eq","eq:\n   Test for equality\nSyntax:\n   A == B\nSee Also:\n   ne, le, ge, lt, gt\nhttp://www.mathworks.com/help/techdoc/ref/eq.html");
		functions.put("exp","exp:\n   Exponential\nSyntax:\n   Y = exp(X)\nSee Also:\n   expm, log\nhttp://www.mathworks.com/help/techdoc/ref/exp.html");
		functions.put("eye","eye:\n   Identity matrix\nSyntax:\n   Y = eye(n)\nSee Also:\n   magic, ones, speye, zeros\nhttp://www.mathworks.com/help/techdoc/ref/eye.html");
		functions.put("find","find:\n   Find indices and values of nonzero elements\nSyntax:\n   ind = find(X)\nSee Also:\n   nonzeros, sparse, colon, ind2sub\nhttp://www.mathworks.com/help/techdoc/ref/find.html");
		functions.put("fliplr","fliplr:\n   Flip matrix left to right\nSyntax:\n   B = fliplr(A)\nSee Also:\n   flipdim, flipud, rot90\nhttp://www.mathworks.com/help/techdoc/ref/fliplr.html");
		functions.put("flipud","flipud:\n   Flip matrix up to down\nSyntax:\n   B = flipud(A)\nSee Also:\n   flipdim, fliplr, rot90\nhttp://www.mathworks.com/help/techdoc/ref/flipud.html");
		functions.put("floor","floor:\n   Round toward negative infinity\nSyntax:\n   B = floor(A)\nSee Also:\n   ceil, fix, round\nhttp://www.mathworks.com/help/techdoc/ref/floor.html");
		functions.put("ge","ge:\n   Test for greater than or equal to\nSyntax:\n   A &gt;= B\nSee Also:\n   gt, eq, le, lt, ne\nhttp://www.mathworks.com/help/techdoc/ref/ge.html");
		functions.put("gt","gt:\n   Test for greater than\nSyntax:\n   A &gt; B\nSee Also:\n   lt, ge, le, ne, eq\nhttp://www.mathworks.com/help/techdoc/ref/gt.html");
		functions.put("inf","inf:\n   Infinity\nSyntax:\n   Inf\nSee Also:\n   isinf, NaN\nhttp://www.mathworks.com/help/techdoc/ref/inf.html");
		functions.put("inv","inv:\n   Matrix inverse\nSyntax:\n   Y = inv(X)\nSee Also:\n   det, lu, rref\nhttp://www.mathworks.com/help/techdoc/ref/inv.html");
		functions.put("inversematrix","No help contents yet entered for function");
		functions.put("isempty","isempty:\n   Determine whether array is empty\nSyntax:\n   TF = isempty(A)\nSee Also:\n   is*\nhttp://www.mathworks.com/help/techdoc/ref/isempty.html");
		functions.put("isfinite","isfinite:\n   Array elements that are finite\nSyntax:\n   TF = isfinite(A)\nSee Also:\n   isinf, isnan, is*\nhttp://www.mathworks.com/help/techdoc/ref/isfinite.html");
		functions.put("isimaginary","No help contents yet entered for function");
		functions.put("isinf","isinf:\n   Array elements that are infinite\nSyntax:\n   TF = isinf(A)\nSee Also:\n   isfinite, isnan, is*\nhttp://www.mathworks.com/help/techdoc/ref/isinf.html");
		functions.put("isnan","isnan:\n   Array elements that are NaN\nSyntax:\n   TF = isnan(A)\nSee Also:\n   isfinite, isinf, is*\nhttp://www.mathworks.com/help/techdoc/ref/isnan.html");
		functions.put("isreal","isreal:\n   Check if input is real array\nSyntax:\n   TF = isreal(A)\nSee Also:\n   complex, isnumeric, isnan, isprime, isfinite, isinf, isa, is*\nhttp://www.mathworks.com/help/techdoc/ref/isreal.html");
		functions.put("ldivide","ldivide:\n   Left or right array division \nSyntax:\n   ldivide(A,B)\nSee Also:\n   mldivide, mrdivide\nhttp://www.mathworks.com/help/techdoc/ref/ldivide.html");
		functions.put("le","le:\n   Test for less than or equal to\nSyntax:\n   A &lt;= B\nSee Also:\n   lt, eq, ge, gt, ne\nhttp://www.mathworks.com/help/techdoc/ref/le.html");
		functions.put("ln","No help contents yet entered for function");
		functions.put("log","log:\n   Natural logarithm\nSyntax:\n   Y = log(X)\nSee Also:\n   exp, log10, log2, logm, reallog\nhttp://www.mathworks.com/help/techdoc/ref/log.html");
		functions.put("lowertriangle","No help contents yet entered for function");
		functions.put("lt","lt:\n   Test for less than\nSyntax:\n   A &lt; B\nSee Also:\n   gt, le, ge, ne, eq\nhttp://www.mathworks.com/help/techdoc/ref/lt.html");
		functions.put("lu","lu:\n   LU matrix factorization\nSyntax:\n   Y = lu(A)\nSee Also:\n   cond, det, inv, luinc, qr, rref\nhttp://www.mathworks.com/help/techdoc/ref/lu.html");
		functions.put("magic","magic:\n   Magic square\nSyntax:\n   M = magic(n)\nSee Also:\n   ones, rand\nhttp://www.mathworks.com/help/techdoc/ref/magic.html");
		functions.put("max","max:\n   Largest elements in array\nSyntax:\n   C = max(A)\nSee Also:\n   isnan, mean, median, min, sort\nhttp://www.mathworks.com/help/techdoc/ref/max.html");
		functions.put("min","min:\n   Smallest elements in array\nSyntax:\n   C = min(A)\nSee Also:\n   max, mean, median, sort\nhttp://www.mathworks.com/help/techdoc/ref/min.html");
		functions.put("minus","No help contents yet entered for function");
		functions.put("mldivide","No help contents yet entered for function");
		functions.put("mpower","No help contents yet entered for function");
		functions.put("mrdivide","No help contents yet entered for function");
		functions.put("mtimes","mtimes:\n   Matrix multiplication\nSyntax:\n   C = A*B\nSee Also:\n\nhttp://www.mathworks.com/help/techdoc/ref/mtimes.html");
		functions.put("nan","nan:\n   Not-a-Number\nSyntax:\n   NaN\nSee Also:\n   Inf, isnan\nhttp://www.mathworks.com/help/techdoc/ref/nan.html");
		functions.put("ne","ne:\n   Test for inequality\nSyntax:\n   A ~= B\nSee Also:\n   eq, le, ge, lt, gt\nhttp://www.mathworks.com/help/techdoc/ref/ne.html");
		functions.put("nnz","nnz:\n   Number of nonzero matrix elements\nSyntax:\n   n = nnz(X)\nSee Also:\n   find, isa, nonzeros, nzmax, size, whos\nhttp://www.mathworks.com/help/techdoc/ref/nnz.html");
		functions.put("not","not:\n   Find logical NOT of array or scalar input\nSyntax:\n   ~A\nSee Also:\n   bitcmp, and, or, xor, any, all\nhttp://www.mathworks.com/help/techdoc/ref/not.html");
		functions.put("numel","numel:\n   Number of elements in array or subscripted array expression\nSyntax:\n   n = numel(A)\nSee Also:\n   nargin, nargout, prod, size, subsasgn, subsref\nhttp://www.mathworks.com/help/techdoc/ref/numel.html");
		functions.put("ones","ones:\n   Create array of all ones\nSyntax:\n   Y = ones(n)\nSee Also:\n   eye, zeros, complex\nhttp://www.mathworks.com/help/techdoc/ref/ones.html");
		functions.put("or","or:\n   Find logical OR of array or scalar inputs\nSyntax:\n   A | B | ...\nSee Also:\n   bitor, and, xor, not, any, all\nhttp://www.mathworks.com/help/techdoc/ref/or.html");
		functions.put("plus","No help contents yet entered for function");
		functions.put("pow2","pow2:\n   Base 2 power and scale floating-point numbers\nSyntax:\n   X = pow2(Y)\nSee Also:\n   log2, exp, hex2num, realmax, realmin\nhttp://www.mathworks.com/help/techdoc/ref/pow2.html");
		functions.put("power","power:\n   Array power\nSyntax:\n   Z = X.^Y\nSee Also:\n   nthroot, realpow\nhttp://www.mathworks.com/help/techdoc/ref/power.html");
		functions.put("prod","prod:\n   Product of array elements\nSyntax:\n   B = prod(A)\nSee Also:\n   cumprod, diff, sum\nhttp://www.mathworks.com/help/techdoc/ref/prod.html");
		functions.put("qr","qr:\n   Orthogonal-triangular decomposition\nSyntax:\n   [Q,R] = qr(A)\nSee Also:\n   ldl, lu\nhttp://www.mathworks.com/help/techdoc/ref/qr.html");
		functions.put("rdivide","rdivide:\n   Left or right array division \nSyntax:\n   ldivide(A,B)\nSee Also:\n   mldivide, mrdivide\nhttp://www.mathworks.com/help/techdoc/ref/rdivide.html");
		functions.put("repmat","repmat:\n   Replicate and tile array\nSyntax:\n   B = repmat(A,m,n)\nSee Also:\n   reshape, bsxfun, NaN, Inf, ones, zeros\nhttp://www.mathworks.com/help/techdoc/ref/repmat.html");
		functions.put("reshape","reshape:\n   Reshape array\nSyntax:\n   B = reshape(A,m,n)\nSee Also:\n   shiftdim, squeeze, circshift, permute, repmat\nhttp://www.mathworks.com/help/techdoc/ref/reshape.html");
		functions.put("round","round:\n   Round to nearest integer\nSyntax:\n   Y = round(X)\nSee Also:\n   ceil, fix, floor\nhttp://www.mathworks.com/help/techdoc/ref/round.html");
		functions.put("row","No help contents yet entered for function");
		functions.put("rows","No help contents yet entered for function");
		functions.put("simultaneouseq","No help contents yet entered for function");
		functions.put("sort","sort:\n   Sort array elements in ascending or descending order\nSyntax:\n   B = sort(A)\nSee Also:\n   issorted, max, mean, median, min, sortrows, unique\nhttp://www.mathworks.com/help/techdoc/ref/sort.html");
		functions.put("sqrt","sqrt:\n   Square root\nSyntax:\n   B = sqrt(X)\nSee Also:\n   nthroot, sqrtm, realsqrt\nhttp://www.mathworks.com/help/techdoc/ref/sqrt.html");
		functions.put("subassign","No help contents yet entered for function");
		functions.put("submatix","No help contents yet entered for function");
		functions.put("sum","sum:\n   Sum of array elements\nSyntax:\n   B = sum(A)\nSee Also:\n   cumsum, diff, isfloat, prod\nhttp://www.mathworks.com/help/techdoc/ref/sum.html");
		functions.put("sumsq","No help contents yet entered for function");
		functions.put("svd","No help contents yet entered for function");
		functions.put("times","No help contents yet entered for function");
		functions.put("transpose","No help contents yet entered for function");
		functions.put("uminus","No help contents yet entered for function");
		functions.put("uplus","No help contents yet entered for function");
		functions.put("uppertriangle","No help contents yet entered for function");
		functions.put("xor","xor:\n   Logical exclusive-OR\nSyntax:\n   C = xor(A, B)\nSee Also:\n   all, any, find\nhttp://www.mathworks.com/help/techdoc/ref/xor.html");
		functions.put("zeros","zeros:\n   Create array of all zeros\nSyntax:\n   B = zeros(n)\nSee Also:\n   eye, ones, rand, randn, complex\nhttp://www.mathworks.com/help/techdoc/ref/zeros.html");
		functions.put("addpath","addpath:\n   Add folders to search path\nSyntax:\n   addpath('folderName1','folderName2','folderName3' ...)\nSee Also:\n   genpath, path, pathsep, rehash, restoredefaultpath, rmpath, savepath\nhttp://www.mathworks.com/help/techdoc/ref/addpath.html");
		functions.put("checkforupdates","No help contents yet entered for function");
		functions.put("createfunctionslist","No help contents yet entered for function");
		functions.put("dbquit","dbquit:\n   Quit debug mode\nSyntax:\n   dbquit\nSee Also:\n   dbclear, dbcont, dbdown, dbstack, dbstatus, dbstep, dbstop, dbtype, dbup\nhttp://www.mathworks.com/help/techdoc/ref/dbquit.html");
		functions.put("debug","No help contents yet entered for function");
		functions.put("disp","disp:\n   Display text or array\nSyntax:\n   disp(X)\nSee Also:\n   format, int2str, num2str, rats, sprintf, fprintf, :\nhttp://www.mathworks.com/help/techdoc/ref/disp.html");
		functions.put("error","error:\n   Display message and abort function\nSyntax:\n   error('msgIdent', 'msgString', v1, v2, ..., vN)\nSee Also:\n   assert, try, catch, dbstop, errordlg, warning, warndlg, MException, throw(MException), rethrow(MException), throwAsCaller(MException), addCause(MException), getReport(MException), last(MException)\nhttp://www.mathworks.com/help/techdoc/ref/error.html");
		functions.put("exit","exit:\n   Terminate Addi program (same as quit)\nSyntax:\n   exit\nSee Also:\n   quit, finish\nhttp://www.mathworks.com/help/techdoc/ref/exit.html");
		functions.put("foreach","No help contents yet entered for function");
		functions.put("format","format:\n   Set display format for output\nSyntax:\n   format\nSee Also:\n   disp, display, isnumeric, isfloat, isinteger, floor, sprintf, fprintf, num2str, rat, spy\nhttp://www.mathworks.com/help/techdoc/ref/format.html");
		functions.put("getdebug","No help contents yet entered for function");
		functions.put("getenv","getenv:\n   Environment variable\nSyntax:\n   getenv 'name'\nSee Also:\n   setenv, computer, pwd, ver, path\nhttp://www.mathworks.com/help/techdoc/ref/getenv.html");
		functions.put("getjmathlibproperty","No help contents yet entered for function");
		functions.put("getproperty","No help contents yet entered for function");
		functions.put("java","No help contents yet entered for function");
		functions.put("getjmathlibcreateuniqueid","No help contents yet entered for function");
		functions.put("nargchk","nargchk:\n   Validate number of input arguments\nSyntax:\n   msgstring = nargchk(minargs, maxargs, numargs)\nSee Also:\n   nargoutchk, nargin, nargout, varargin, varargout, error\nhttp://www.mathworks.com/help/techdoc/ref/nargchk.html");
		functions.put("nargoutchk","nargoutchk:\n   Validate number of output arguments\nSyntax:\n   msgstring = nargoutchk(minargs, maxargs, numargs)\nSee Also:\n   nargchk, nargout, nargin, varargout, varargin, error\nhttp://www.mathworks.com/help/techdoc/ref/nargoutchk.html");
		functions.put("newline","No help contents yet entered for function");
		functions.put("path","path:\n   View or change search path\nSyntax:\n   path\nSee Also:\n   addpath, cd, dir, genpath, Addiroot, pathsep, pathtool, rehash, restoredefaultpath, rmpath, savepath, startup, userpath, what\nhttp://www.mathworks.com/help/techdoc/ref/path.html");
		functions.put("print_usage","No help contents yet entered for function");
		functions.put("printstacktrace","No help contents yet entered for function");
		functions.put("quit","quit:\n   Terminate Addi program\nSyntax:\n   quit\nSee Also:\n   exit, save, finish, startup\nhttp://www.mathworks.com/help/techdoc/ref/quit.html");
		functions.put("rehash","rehash:\n   Refresh function and file system path caches\nSyntax:\n   rehash\nSee Also:\n   addpath, clear, Addiroot, path, rmpath\nhttp://www.mathworks.com/help/techdoc/ref/rehash.html");
		functions.put("rmpath","rmpath:\n   Remove folders from search path\nSyntax:\n   rmpath('folderName') \nSee Also:\n   addpath, cd, dir, genpath, Addiroot, path, pathsep, pathtool, rehash, restoredefaultpath, savepath, userpath, what\nhttp://www.mathworks.com/help/techdoc/ref/rmpath.html");
		functions.put("setdebug","No help contents yet entered for function");
		functions.put("setjmathlibproperty","No help contents yet entered for function");
		functions.put("usage","No help contents yet entered for function");
		functions.put("ver","ver:\n   Version information for MathWorks products\nSyntax:\n   ver\nSee Also:\n   computer, help, hostid, license, verlessthan, version, whatsnew\nhttp://www.mathworks.com/help/techdoc/ref/ver.html");
		functions.put("version","version:\n   Version number for Addi and libraries\nSyntax:\n   version\nSee Also:\n   computer, ver, verlessthan, whatsnew\nhttp://www.mathworks.com/help/techdoc/ref/version.html");
		functions.put("warning","warning:\n   Warning message\nSyntax:\n   warning('message')\nSee Also:\n   lastwarn, warndlg, error, lasterror, errordlg, dbstop, disp, sprintf\nhttp://www.mathworks.com/help/techdoc/ref/warning.html");
		functions.put("plot","plot:\n   2-D line plot\nSyntax:\n   plot(Y)\nSee Also:\n   axes, axis, bar, gca, grid, hold, legend, line, lineseries properties, LineSpec, LineWidth, loglog, MarkerEdgeColor, MarkerFaceColor, MarkerSize, plot3, plotyy, semilogx, semilogy, subplot, title, xlabel, xlim, ylabel, ylim\nhttp://www.mathworks.com/help/techdoc/ref/plot.html");
	}

    /* @param operands[0] string which specifies the function to lookup*/
	public OperandToken evaluate(Token[] operands, GlobalValues globals)
	{

		String function=" ";

		// at least one operand
        if (getNArgIn(operands) > 1)
			throwMathLibException("help: number of arguments > 1");

        if (getNArgIn(operands) == 1)
        {
    		// check if a directory is specified
    		if ((operands[0] instanceof CharToken)) 
    		{
    			function = ((CharToken)operands[0]).getElementString(0);
    		}
    		else
    		{
            	throwMathLibException("help: argument must be a string");
    		}
    	}
        
        String helpString = functions.get(function);
        if (helpString == null) {
        	throwMathLibException("help: no entry found for " + function);
        } else {
        	globals.getInterpreter().displayText(helpString);		    
        }
        
		return null;		

	} // end eval
}

/*
@GROUP
IO
@SYNTAX
cd(directory)
@DOC
Sets the working directory to directory. Also switches between directories.
@EXAMPLES
<programlisting>
cd("C:\barfoo");
</programlisting>
@SEE
dir, cd, isdirectory
*/


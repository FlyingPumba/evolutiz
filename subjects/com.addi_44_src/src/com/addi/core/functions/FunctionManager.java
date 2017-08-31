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

package com.addi.core.functions;

import java.util.*;
import java.io.*;
import java.net.*;
import dalvik.system.PathClassLoader;

import com.addi.core.interpreter.*;
import com.addi.core.tokens.Expression;
import com.addi.core.tokens.FunctionToken;
import com.addi.core.tokens.Token;
import com.addi.core.tokens.VariableToken;

import com.addi.toolbox.*;

/**Class for storing and managing the functions being used*/
public class FunctionManager {
    class SystemFileFunctionLoader extends FileFunctionLoader {
        SystemFileFunctionLoader(File _functionDir, boolean _traverseChildren) {
            super(_functionDir, _traverseChildren, true);
        }
    }
    
    //location of function
    Map<String, String> functions = new HashMap();
    
    Map<String, Function> loadedFunctions = new HashMap(); 
    
    // indicates if FunctionManager is running in an application or an applet
    private boolean runningStandalone;

    // different function loaders
    private Vector functionLoaders = new Vector();
    // android class loader
    //CCX PathClassLoader functionLoader; 
    
    // flag for caching of p files
    boolean pFileCachingEnabledB = false;
    
    //working directory 
    File workingDirectory;
    
    //used to load m files
    private MFileLoader mLoader;

    /**Creates the function manager and defines any internal functions
    if this is an application then it creates a class loader to load external functions
    @param runningStandalone = true if the program is running as an application*/
    public FunctionManager(boolean _runningStandalone) {  //, Applet _applet) {

    	workingDirectory = new File("/");
    	
        runningStandalone = _runningStandalone;
 
        mLoader = new MFileLoader();
        
        functions.put("lin2mu","toolbox/audio/lin2mu.m");
        functions.put("loadaudio","toolbox/audio/loadaudio.m");
        functions.put("mu2lin","toolbox/audio/mu2lin.m");
        functions.put("playaudio","toolbox/audio/playaudio.m");
        functions.put("record","toolbox/audio/record.m");
        functions.put("saveaudio","toolbox/audio/saveaudio.m");
        functions.put("setaudio","toolbox/audio/setaudio.m");
        functions.put("wavread","toolbox/audio/wavread.m");
        functions.put("wavwrite","toolbox/audio/wavwrite.m");
        functions.put("acos","com.addi.toolbox.elfun");
        functions.put("acosh","com.addi.toolbox.elfun");
        functions.put("asin","com.addi.toolbox.elfun");
        functions.put("asinh","com.addi.toolbox.elfun");
        functions.put("atan","com.addi.toolbox.elfun");
        functions.put("atanh","com.addi.toolbox.elfun");
        functions.put("cos","com.addi.toolbox.elfun");
        functions.put("cosh","com.addi.toolbox.elfun");
        functions.put("degtograd","com.addi.toolbox.elfun");
        functions.put("degtorad","com.addi.toolbox.elfun");
        functions.put("gradtodeg","com.addi.toolbox.elfun");
        functions.put("gradtorad","com.addi.toolbox.elfun");
        functions.put("radtodeg","com.addi.toolbox.elfun");
        functions.put("radtograd","com.addi.toolbox.elfun");
        functions.put("sin","com.addi.toolbox.elfun");
        functions.put("sinh","com.addi.toolbox.elfun");
        functions.put("tan","com.addi.toolbox.elfun");
        functions.put("tanh","com.addi.toolbox.elfun");
        functions.put("acosd","toolbox/elfun/acosd.m");
        functions.put("acot","toolbox/elfun/acot.m");
        functions.put("acotd","toolbox/elfun/acotd.m");
        functions.put("acoth","toolbox/elfun/acoth.m");
        functions.put("acsc","toolbox/elfun/acsc.m");
        functions.put("acscd","toolbox/elfun/acscd.m");
        functions.put("acsch","toolbox/elfun/acsch.m");
        functions.put("asec","toolbox/elfun/asec.m");
        functions.put("asecd","toolbox/elfun/asecd.m");
        functions.put("asech","toolbox/elfun/asech.m");
        functions.put("asind","toolbox/elfun/asind.m");
        functions.put("atand","toolbox/elfun/atand.m");
        functions.put("cosd","toolbox/elfun/cosd.m");
        functions.put("cot","toolbox/elfun/cot.m");
        functions.put("cotd","toolbox/elfun/cotd.m");
        functions.put("coth","toolbox/elfun/coth.m");
        functions.put("csc","toolbox/elfun/csc.m");
        functions.put("cscd","toolbox/elfun/cscd.m");
        functions.put("csch","toolbox/elfun/csch.m");
        functions.put("lcm","toolbox/elfun/lcm.m");
        functions.put("sec","toolbox/elfun/sec.m");
        functions.put("secd","toolbox/elfun/secd.m");
        functions.put("sech","toolbox/elfun/sech.m");
        functions.put("sind","toolbox/elfun/sind.m");
        functions.put("tand","toolbox/elfun/tand.m");
        functions.put("convhull","toolbox/geometry/convhull.m");
        functions.put("delaunay","toolbox/geometry/delaunay.m");
        functions.put("delaunay3","toolbox/geometry/delaunay3.m");
        functions.put("delaunayn","toolbox/geometry/delaunayn.m");
        functions.put("dsearch","toolbox/geometry/dsearch.m");
        functions.put("dsearchn","toolbox/geometry/dsearchn.m");
        functions.put("griddata","toolbox/geometry/griddata.m");
        functions.put("griddata3","toolbox/geometry/griddata3.m");
        functions.put("griddatan","toolbox/geometry/griddatan.m");
        functions.put("inpolygon","toolbox/geometry/inpolygon.m");
        functions.put("rectint","toolbox/geometry/rectint.m");
        functions.put("trimesh","toolbox/geometry/trimesh.m");
        functions.put("triplot","toolbox/geometry/triplot.m");
        functions.put("trisurf","toolbox/geometry/trisurf.m");
        functions.put("tsearchn","toolbox/geometry/tsearchn.m");
        functions.put("voronoi","toolbox/geometry/voronoi.m");
        functions.put("voronoin","toolbox/geometry/voronoin.m");
        functions.put("__additional_help_message__","toolbox/help/__additional_help_message__.m");
        functions.put("__makeinfo__","toolbox/help/__makeinfo__.m");
        functions.put("__strip_html_tags__","toolbox/help/__strip_html_tags__.m");
        functions.put("doc","toolbox/help/doc.m");
        functions.put("gen_doc_cache","toolbox/help/gen_doc_cache.m");
        functions.put("get_first_help_sentence","toolbox/help/get_first_help_sentence.m");
        //functions.put("help","toolbox/help/help.m");
        functions.put("lookfor","toolbox/help/lookfor.m");
        //functions.put("print_usage","toolbox/help/print_usage.m");
        functions.put("type","toolbox/help/type.m");
        functions.put("which","toolbox/help/which.m");
        functions.put("time","com.addi.toolbox.time");
        functions.put("tic","com.addi.toolbox.time");
        functions.put("toc","com.addi.toolbox.time");
        functions.put("pause","com.addi.toolbox.time");
        functions.put("date","com.addi.toolbox.time");
        functions.put("is_leap_year","toolbox/time/is_leap_year.m");
        functions.put("_char","com.addi.toolbox.string");
        functions.put("_double","com.addi.toolbox.string");
        functions.put("blanks","com.addi.toolbox.string");
        functions.put("base2dec","toolbox/string/base2dec.m");
        functions.put("bin2dec","toolbox/string/bin2dec.m");
        functions.put("blanks","toolbox/string/blanks.m");
        functions.put("char","toolbox/string/char.m");
        functions.put("cstrcat","toolbox/string/cstrcat.m");
        functions.put("deblank","toolbox/string/deblank.m");
        functions.put("dec2base","toolbox/string/dec2base.m");
        functions.put("dec2bin","toolbox/string/dec2bin.m");
        functions.put("dec2hex","toolbox/string/dec2hex.m");
        functions.put("double","toolbox/string/double.m");
        //functions.put("findstr","toolbox/string/findstr.m");
        functions.put("hex2dec","toolbox/string/hex2dec.m");
        functions.put("index","toolbox/string/index.m");
        functions.put("isletter","toolbox/string/isletter.m");
        functions.put("isstrprop","toolbox/string/isstrprop.m");
        functions.put("mat2str","toolbox/string/mat2str.m");
        functions.put("regexptranslate","toolbox/string/regexptranslate.m");
        functions.put("rindex","toolbox/string/rindex.m");
        functions.put("str2double","toolbox/string/str2double.m");
        //functions.put("str2num","toolbox/string/str2num.m");
        //functions.put("strcat","toolbox/string/strcat.m");
        functions.put("strchr","toolbox/string/strchr.m");
        //functions.put("strcmpi","toolbox/string/strcmpi.m");
        //functions.put("strfind","toolbox/string/strfind.m");
        functions.put("strjust","toolbox/string/strjust.m");
        functions.put("strmatch","toolbox/string/strmatch.m");
        //functions.put("strncmpi","toolbox/string/strncmpi.m");
        functions.put("strrep","toolbox/string/strrep.m");
        functions.put("strsplit","toolbox/string/strsplit.m");
        functions.put("strtok","toolbox/string/strtok.m");
        functions.put("strtrim","toolbox/string/strtrim.m");
        functions.put("strtrunc","toolbox/string/strtrunc.m");
        functions.put("substr","toolbox/string/substr.m");
        functions.put("validatestring","toolbox/string/validatestring.m");
        functions.put("findstr","com.addi.toolbox.string");
        functions.put("isspace","com.addi.toolbox.string");
        functions.put("lower","com.addi.toolbox.string");
        functions.put("num2str","com.addi.toolbox.string");
        functions.put("sprintf","com.addi.toolbox.string");
        functions.put("str2num","com.addi.toolbox.string");
        functions.put("strcat","com.addi.toolbox.string");
        functions.put("strcmp","com.addi.toolbox.string");
        functions.put("strcmpi","com.addi.toolbox.string");
        functions.put("strfind","com.addi.toolbox.string");
        functions.put("strlength","com.addi.toolbox.string");
        functions.put("strncmp","com.addi.toolbox.string");
        functions.put("strncmpi","com.addi.toolbox.string");
        functions.put("strvcat","com.addi.toolbox.string");
        functions.put("substring","com.addi.toolbox.string");
        functions.put("upper","com.addi.toolbox.string");
        functions.put("average","com.addi.toolbox.statistics");
        functions.put("variation","com.addi.toolbox.statistics");
        functions.put("__quantile__","toolbox/statistics/base/__quantile__.m");
        functions.put("center","toolbox/statistics/base/center.m");
        functions.put("cloglog","toolbox/statistics/base/cloglog.m");
        functions.put("cor","toolbox/statistics/base/cor.m");
        functions.put("corrcoef","toolbox/statistics/base/corrcoef.m");
        functions.put("cov","toolbox/statistics/base/cov.m");
        functions.put("cut","toolbox/statistics/base/cut.m");
        functions.put("gls","toolbox/statistics/base/gls.m");
        functions.put("histc","toolbox/statistics/base/histc.m");
        functions.put("iqr","toolbox/statistics/base/iqr.m");
        functions.put("kendall","toolbox/statistics/base/kendall.m");
        functions.put("kurtosis","toolbox/statistics/base/kurtosis.m");
        functions.put("logit","toolbox/statistics/base/logit.m");
        functions.put("mahalanobis","toolbox/statistics/base/mahalanobis.m");
        functions.put("mean","toolbox/statistics/base/mean.m");
        functions.put("meansq","toolbox/statistics/base/meansq.m");
        functions.put("median","toolbox/statistics/base/median.m");
        functions.put("mode","toolbox/statistics/base/mode.m");
        functions.put("moment","toolbox/statistics/base/moment.m");
        functions.put("ols","toolbox/statistics/base/ols.m");
        functions.put("ppplot","toolbox/statistics/base/ppplot.m");
        functions.put("prctile","toolbox/statistics/base/prctile.m");
        functions.put("probit","toolbox/statistics/base/probit.m");
        functions.put("qqplot","toolbox/statistics/base/qqplot.m");
        functions.put("quantile","toolbox/statistics/base/quantile.m");
        functions.put("range","toolbox/statistics/base/range.m");
        functions.put("ranks","toolbox/statistics/base/ranks.m");
        functions.put("run_count","toolbox/statistics/base/run_count.m");
        functions.put("skewness","toolbox/statistics/base/skewness.m");
        functions.put("spearman","toolbox/statistics/base/spearman.m");
        functions.put("statistics","toolbox/statistics/base/statistics.m");
        functions.put("std","toolbox/statistics/base/std.m");
        functions.put("studentize","toolbox/statistics/base/studentize.m");
        functions.put("table","toolbox/statistics/base/table.m");
        functions.put("values","toolbox/statistics/base/values.m");
        functions.put("var","toolbox/statistics/base/var.m");
        functions.put("betacdf","toolbox/statistics/distributions/betacdf.m");
        functions.put("betainv","toolbox/statistics/distributions/betainv.m");
        functions.put("betapdf","toolbox/statistics/distributions/betapdf.m");
        functions.put("betarnd","toolbox/statistics/distributions/betarnd.m");
        functions.put("binocdf","toolbox/statistics/distributions/binocdf.m");
        functions.put("binoinv","toolbox/statistics/distributions/binoinv.m");
        functions.put("binopdf","toolbox/statistics/distributions/binopdf.m");
        functions.put("binornd","toolbox/statistics/distributions/binornd.m");
        functions.put("cauchy_cdf","toolbox/statistics/distributions/cauchy_cdf.m");
        functions.put("cauchy_inv","toolbox/statistics/distributions/cauchy_inv.m");
        functions.put("cauchy_pdf","toolbox/statistics/distributions/cauchy_pdf.m");
        functions.put("cauchy_rnd","toolbox/statistics/distributions/cauchy_rnd.m");
        functions.put("chi2cdf","toolbox/statistics/distributions/chi2cdf.m");
        functions.put("chi2inv","toolbox/statistics/distributions/chi2inv.m");
        functions.put("chi2pdf","toolbox/statistics/distributions/chi2pdf.m");
        functions.put("chi2rnd","toolbox/statistics/distributions/chi2rnd.m");
        functions.put("discrete_cdf","toolbox/statistics/distributions/discrete_cdf.m");
        functions.put("discrete_inv","toolbox/statistics/distributions/discrete_inv.m");
        functions.put("discrete_pdf","toolbox/statistics/distributions/discrete_pdf.m");
        functions.put("discrete_rnd","toolbox/statistics/distributions/discrete_rnd.m");
        functions.put("empirical_cdf","toolbox/statistics/distributions/empirical_cdf.m");
        functions.put("empirical_inv","toolbox/statistics/distributions/empirical_inv.m");
        functions.put("empirical_pdf","toolbox/statistics/distributions/empirical_pdf.m");
        functions.put("empirical_rnd","toolbox/statistics/distributions/empirical_rnd.m");
        functions.put("expcdf","toolbox/statistics/distributions/expcdf.m");
        functions.put("expinv","toolbox/statistics/distributions/expinv.m");
        functions.put("exppdf","toolbox/statistics/distributions/exppdf.m");
        functions.put("exprnd","toolbox/statistics/distributions/exprnd.m");
        functions.put("fcdf","toolbox/statistics/distributions/fcdf.m");
        functions.put("finv","toolbox/statistics/distributions/finv.m");
        functions.put("fpdf","toolbox/statistics/distributions/fpdf.m");
        functions.put("frnd","toolbox/statistics/distributions/frnd.m");
        functions.put("gamcdf","toolbox/statistics/distributions/gamcdf.m");
        functions.put("gaminv","toolbox/statistics/distributions/gaminv.m");
        functions.put("gampdf","toolbox/statistics/distributions/gampdf.m");
        functions.put("gamrnd","toolbox/statistics/distributions/gamrnd.m");
        functions.put("geocdf","toolbox/statistics/distributions/geocdf.m");
        functions.put("geoinv","toolbox/statistics/distributions/geoinv.m");
        functions.put("geopdf","toolbox/statistics/distributions/geopdf.m");
        functions.put("geornd","toolbox/statistics/distributions/geornd.m");
        functions.put("hygecdf","toolbox/statistics/distributions/hygecdf.m");
        functions.put("hygeinv","toolbox/statistics/distributions/hygeinv.m");
        functions.put("hygepdf","toolbox/statistics/distributions/hygepdf.m");
        functions.put("hygernd","toolbox/statistics/distributions/hygernd.m");
        functions.put("kolmogorov_smirnov_cdf","toolbox/statistics/distributions/kolmogorov_smirnov_cdf.m");
        functions.put("laplace_cdf","toolbox/statistics/distributions/laplace_cdf.m");
        functions.put("laplace_inv","toolbox/statistics/distributions/laplace_inv.m");
        functions.put("laplace_pdf","toolbox/statistics/distributions/laplace_pdf.m");
        functions.put("laplace_rnd","toolbox/statistics/distributions/laplace_rnd.m");
        functions.put("logistic_cdf","toolbox/statistics/distributions/logistic_cdf.m");
        functions.put("logistic_inv","toolbox/statistics/distributions/logistic_inv.m");
        functions.put("logistic_pdf","toolbox/statistics/distributions/logistic_pdf.m");
        functions.put("logistic_rnd","toolbox/statistics/distributions/logistic_rnd.m");
        functions.put("logncdf","toolbox/statistics/distributions/logncdf.m");
        functions.put("logninv","toolbox/statistics/distributions/logninv.m");
        functions.put("lognpdf","toolbox/statistics/distributions/lognpdf.m");
        functions.put("lognrnd","toolbox/statistics/distributions/lognrnd.m");
        functions.put("nbincdf","toolbox/statistics/distributions/nbincdf.m");
        functions.put("nbininv","toolbox/statistics/distributions/nbininv.m");
        functions.put("nbinpdf","toolbox/statistics/distributions/nbinpdf.m");
        functions.put("nbinrnd","toolbox/statistics/distributions/nbinrnd.m");
        functions.put("normcdf","toolbox/statistics/distributions/normcdf.m");
        functions.put("norminv","toolbox/statistics/distributions/norminv.m");
        functions.put("normpdf","toolbox/statistics/distributions/normpdf.m");
        functions.put("normrnd","toolbox/statistics/distributions/normrnd.m");
        functions.put("poisscdf","toolbox/statistics/distributions/poisscdf.m");
        functions.put("poissinv","toolbox/statistics/distributions/poissinv.m");
        functions.put("poisspdf","toolbox/statistics/distributions/poisspdf.m");
        functions.put("poissrnd","toolbox/statistics/distributions/poissrnd.m");
        functions.put("stdnormal_cdf","toolbox/statistics/distributions/stdnormal_cdf.m");
        functions.put("stdnormal_inv","toolbox/statistics/distributions/stdnormal_inv.m");
        functions.put("stdnormal_pdf","toolbox/statistics/distributions/stdnormal_pdf.m");
        functions.put("stdnormal_rnd","toolbox/statistics/distributions/stdnormal_rnd.m");
        functions.put("tcdf","toolbox/statistics/distributions/tcdf.m");
        functions.put("tinv","toolbox/statistics/distributions/tinv.m");
        functions.put("tpdf","toolbox/statistics/distributions/tpdf.m");
        functions.put("trnd","toolbox/statistics/distributions/trnd.m");
        functions.put("unidcdf","toolbox/statistics/distributions/unidcdf.m");
        functions.put("unidinv","toolbox/statistics/distributions/unidinv.m");
        functions.put("unidpdf","toolbox/statistics/distributions/unidpdf.m");
        functions.put("unidrnd","toolbox/statistics/distributions/unidrnd.m");
        functions.put("unifcdf","toolbox/statistics/distributions/unifcdf.m");
        functions.put("unifinv","toolbox/statistics/distributions/unifinv.m");
        functions.put("unifpdf","toolbox/statistics/distributions/unifpdf.m");
        functions.put("unifrnd","toolbox/statistics/distributions/unifrnd.m");
        functions.put("wblcdf","toolbox/statistics/distributions/wblcdf.m");
        functions.put("wblinv","toolbox/statistics/distributions/wblinv.m");
        functions.put("wblpdf","toolbox/statistics/distributions/wblpdf.m");
        functions.put("wblrnd","toolbox/statistics/distributions/wblrnd.m");
        functions.put("wienrnd","toolbox/statistics/distributions/wienrnd.m");
        functions.put("logistic_regression","toolbox/statistics/models/logistic_regression.m");
        functions.put("logistic_regression_derivatives","toolbox/statistics/models/logistic_regression_derivatives.m");
        functions.put("logistic_regression_likelihood","toolbox/statistics/models/logistic_regression_likelihood.m");
        functions.put("anova","toolbox/statistics/tests/anova.m");
        functions.put("bartlett_test","toolbox/statistics/tests/bartlett_test.m");
        functions.put("chisquare_test_homogeneity","toolbox/statistics/tests/chisquare_test_homogeneity.m");
        functions.put("chisquare_test_independence","toolbox/statistics/tests/chisquare_test_independence.m");
        functions.put("cor_test","toolbox/statistics/tests/cor_test.m");
        functions.put("f_test_regression","toolbox/statistics/tests/f_test_regression.m");
        functions.put("hotelling_test","toolbox/statistics/tests/hotelling_test.m");
        functions.put("hotelling_test_2","toolbox/statistics/tests/hotelling_test_2.m");
        functions.put("kolmogorov_smirnov_test","toolbox/statistics/tests/kolmogorov_smirnov_test.m");
        functions.put("kolmogorov_smirnov_test_2","toolbox/statistics/tests/kolmogorov_smirnov_test_2.m");
        functions.put("kruskal_wallis_test","toolbox/statistics/tests/kruskal_wallis_test.m");
        functions.put("manova","toolbox/statistics/tests/manova.m");
        functions.put("mcnemar_test","toolbox/statistics/tests/mcnemar_test.m");
        functions.put("prop_test_2","toolbox/statistics/tests/prop_test_2.m");
        functions.put("run_test","toolbox/statistics/tests/run_test.m");
        functions.put("sign_test","toolbox/statistics/tests/sign_test.m");
        functions.put("t_test","toolbox/statistics/tests/t_test.m");
        functions.put("t_test_2","toolbox/statistics/tests/t_test_2.m");
        functions.put("t_test_regression","toolbox/statistics/tests/t_test_regression.m");
        functions.put("u_test","toolbox/statistics/tests/u_test.m");
        functions.put("var_test","toolbox/statistics/tests/var_test.m");
        functions.put("welch_test","toolbox/statistics/tests/welch_test.m");
        functions.put("wilcoxon_test","toolbox/statistics/tests/wilcoxon_test.m");
        functions.put("z_test","toolbox/statistics/tests/z_test.m");
        functions.put("z_test_2","toolbox/statistics/tests/z_test_2.m");
        functions.put("hadamard","toolbox/specialmatrix/hadamard.m");
        functions.put("hankel","toolbox/specialmatrix/hankel.m");
        functions.put("hilb","toolbox/specialmatrix/hilb.m");
        functions.put("invhilb","toolbox/specialmatrix/invhilb.m");
        functions.put("lauchli","toolbox/specialmatrix/lauchli.m");
        functions.put("magic","toolbox/specialmatrix/magic.m");   
        functions.put("pascal","toolbox/specialmatrix/pascal.m");
        functions.put("rosser","toolbox/specialmatrix/rosser.m");
        functions.put("sylvester_matrix","toolbox/specialmatrix/sylvester_matrix.m");
        functions.put("toeplitz","toolbox/specialmatrix/toeplitz.m");
        functions.put("vander","toolbox/specialmatrix/vander.m");
        functions.put("wilkinson","toolbox/specialmatrix/wilkinson.m");
        functions.put("bessel","toolbox/specfun/bessel.m");
        functions.put("beta","toolbox/specfun/beta.m");
        functions.put("betai","toolbox/specfun/betai.m");
        functions.put("betaln","toolbox/specfun/betaln.m");
        functions.put("erfinv","toolbox/specfun/erfinv.m");
        //functions.put("factor","toolbox/specfun/factor.m");
        functions.put("factorial","toolbox/specfun/factorial.m");
        functions.put("gammai","toolbox/specfun/gammai.m");
        //functions.put("isprime","toolbox/specfun/isprime.m");
        functions.put("legendre","toolbox/specfun/legendre.m");
        functions.put("log2","toolbox/specfun/log2.m");
        functions.put("nchoosek","toolbox/specfun/nchoosek.m");
        functions.put("perms","toolbox/specfun/perms.m");
        //functions.put("pow2","toolbox/specfun/pow2.m");
        //functions.put("primes","toolbox/specfun/primes.m");
        functions.put("reallog","toolbox/specfun/reallog.m");
        functions.put("realpow","toolbox/specfun/realpow.m");
        functions.put("realsqrt","toolbox/specfun/realsqrt.m");
        functions.put("gammaln","com.addi.toolbox.specfun");
        functions.put("perms","toolbox/specialmatrix/perms.m");
        functions.put("arch_fit","toolbox/signal/arch_fit.m");
        functions.put("arch_rnd","toolbox/signal/arch_rnd.m");
        functions.put("arch_test","toolbox/signal/arch_test.m");
        functions.put("arma_rnd","toolbox/signal/arma_rnd.m");
        functions.put("autocor","toolbox/signal/autocor.m");
        functions.put("autocov","toolbox/signal/autocov.m");
        functions.put("autoreg_matrix","toolbox/signal/autoreg_matrix.m");
        functions.put("bartlett","toolbox/signal/bartlett.m");
        functions.put("blackman","toolbox/signal/blackman.m");
        functions.put("detrend","toolbox/signal/detrend.m");
        functions.put("diffpara","toolbox/signal/diffpara.m");
        functions.put("durbinlevinson","toolbox/signal/durbinlevinson.m");
        functions.put("fftconv","toolbox/signal/fftconv.m");
        functions.put("fftfilt","toolbox/signal/fftfilt.m");
        functions.put("fftshift","toolbox/signal/fftshift.m");
        functions.put("filter2","toolbox/signal/filter2.m");
        functions.put("fractdiff","toolbox/signal/fractdiff.m");
        functions.put("freqz_plot","toolbox/signal/freqz_plot.m");
        functions.put("freqz","toolbox/signal/freqz.m");
        functions.put("hamming","toolbox/signal/hamming.m");
        functions.put("hanning","toolbox/signal/hanning.m");
        functions.put("hurst","toolbox/signal/hurst.m");
        functions.put("ifftshift","toolbox/signal/ifftshift.m");
        functions.put("periodogram","toolbox/signal/periodogram.m");
        functions.put("rectangle_lw","toolbox/signal/rectangle_lw.m");
        functions.put("rectangle_sw","toolbox/signal/rectangle_sw.m");
        functions.put("sinc","toolbox/signal/sinc.m");
        functions.put("sinetone","toolbox/signal/sinetone.m");
        functions.put("sinewave","toolbox/signal/sinewave.m");
        functions.put("spectral_adf","toolbox/signal/spectral_adf.m");
        functions.put("spectral_xdf","toolbox/signal/spectral_xdf.m");
        functions.put("spencer","toolbox/signal/spencer.m");
        functions.put("stft","toolbox/signal/stft.m");
        functions.put("synthesis","toolbox/signal/synthesis.m");
        functions.put("triangle_lw","toolbox/signal/triangle_lw.m");
        functions.put("triangle_sw","toolbox/signal/triangle_sw.m");
        functions.put("unwrap","toolbox/signal/unwrap.m");
        functions.put("yulewalker","toolbox/signal/yulewalker.m");
        functions.put("complement","toolbox/set/complement.m");
        functions.put("create_set","toolbox/set/create_set.m");
        functions.put("intersect","toolbox/set/intersect.m");
        functions.put("ismember","toolbox/set/ismember.m");
        functions.put("setdiff","toolbox/set/setdiff.m");
        functions.put("setxor","toolbox/set/setxor.m");
        functions.put("union","toolbox/set/union.m");
        functions.put("unique","toolbox/set/unique.m");
        functions.put("bicgstab","toolbox/sparse/bicgstab.m");
        functions.put("cgs","toolbox/sparse/cgs.m");
        functions.put("colperm","toolbox/sparse/colperm.m");
        functions.put("etreeplot","toolbox/sparse/etreeplot.m");
        functions.put("gplot","toolbox/sparse/gplot.m");
        functions.put("nonzeros","toolbox/sparse/nonzeros.m");
        functions.put("normest","toolbox/sparse/normest.m");
        functions.put("pcg","toolbox/sparse/pcg.m");
        functions.put("pcr","toolbox/sparse/pcr.m");
        functions.put("spalloc","toolbox/sparse/spalloc.m");
        functions.put("spaugment","toolbox/sparse/spaugment.m");
        functions.put("spconvert","toolbox/sparse/spconvert.m");
        functions.put("spdiags","toolbox/quaternion/spdiags.m");
        functions.put("speye","toolbox/sparse/speye.m");
        functions.put("spfun","toolbox/sparse/spfun.m");
        functions.put("sphcat","toolbox/sparse/sphcat.m");
        functions.put("spones","toolbox/sparse/spones.m");
        functions.put("sprand","toolbox/sparse/sprand.m");
        functions.put("sprandn","toolbox/sparse/sprandn.m");
        functions.put("sprandsym","toolbox/sparse/sprandsym.m");
        functions.put("spstats","toolbox/sparse/spstats.m");
        functions.put("spvcat","toolbox/sparse/spvcat.m");
        functions.put("spy","toolbox/sparse/spy.m");
        functions.put("svds","toolbox/sparse/svds.m");
        functions.put("treelayout","toolbox/sparse/treelayout.m");
        functions.put("treeplot","toolbox/sparse/treeplot.m");
        functions.put("qderiv","toolbox/quaternion/qderiv.m");
        functions.put("qderivmat","toolbox/quaternion/qderivmat.m");
        functions.put("qinv","toolbox/quaternion/qinv.m");
        functions.put("qmult","toolbox/quaternion/qmult.m");
        functions.put("qtrans","toolbox/quaternion/qtrans.m");
        functions.put("qtransv","toolbox/quaternion/qtransv.m");
        functions.put("qtransvmat","toolbox/quaternion/qtransvmat.m");
        functions.put("quaternion","toolbox/quaternion/quaternion.m");
        functions.put("binomial","com.addi.toolbox.polynomial");
        functions.put("compan","toolbox/polynomial/compan.m");
        functions.put("conv","toolbox/polynomial/conv.m");
        functions.put("convn","toolbox/polynomial/convn.m");
        functions.put("deconv","toolbox/polynomial/deconv.m");
        functions.put("mkpp","toolbox/polynomial/mkpp.m");
        functions.put("mpoles","toolbox/polynomial/mpoles.m");
        functions.put("pchip","toolbox/polynomial/pchip.m");
        functions.put("poly","toolbox/polynomial/poly.m");
        functions.put("polyaffine","toolbox/polynomial/polyaffine.m");
        functions.put("polyder","toolbox/polynomial/polyder.m");
        functions.put("polyderive","toolbox/polynomial/polyderive.m");
        functions.put("polyfit","toolbox/polynomial/polyfit.m");
        functions.put("polygcd","toolbox/polynomial/polygcd.m");
        functions.put("polyint","toolbox/polynomial/polyint.m");
        functions.put("polyinteg","toolbox/polynomial/polyinteg.m");
        functions.put("polyout","toolbox/polynomial/polyout.m");
        functions.put("polyreduce","toolbox/polynomial/polyreduce.m");
        functions.put("polyval","toolbox/polynomial/polyval.m");
        functions.put("polyvalm","toolbox/polynomial/polyvalm.m");
        functions.put("ppval","toolbox/polynomial/ppval.m");
        functions.put("residue","toolbox/polynomial/residue.m");
        functions.put("roots","toolbox/polynomial/roots.m");
        functions.put("spline","toolbox/polynomial/spline.m");
        functions.put("unmkpp","toolbox/polynomial/unmkpp.m");
        functions.put("physical_constant","toolbox/physical_constants/physical_constant.m");
        functions.put("urlread","com.addi.toolbox.net");
        functions.put("__xzip__","toolbox/miscellaneous/__xzip__.m");
        functions.put("ans","toolbox/miscellaneous/ans.m");
        functions.put("bincoeff","toolbox/miscellaneous/bincoeff.m");
        functions.put("bug_report","toolbox/miscellaneous/bug_report.m");
        functions.put("bunzip2","toolbox/miscellaneous/bunzip2.m");
        functions.put("bzip2","toolbox/miscellaneous/bzip2.m");
        //functions.put("cast","toolbox/miscellaneous/cast.m");
        functions.put("comma","toolbox/miscellaneous/comma.m");
        functions.put("compare_versions","toolbox/miscellaneous/compare_versions.m");
        functions.put("computer","toolbox/miscellaneous/computer.m");
        functions.put("copyfile","toolbox/miscellaneous/copyfile.m");
        //functions.put("debug","toolbox/miscellaneous/debug.m");
        //functions.put("delete","toolbox/miscellaneous/delete.m");
        //functions.put("dir","toolbox/miscellaneous/dir.m");
        functions.put("dos","toolbox/miscellaneous/dos.m");
        functions.put("dump_perfs","toolbox/miscellaneous/dump_perfs.m");
        //functions.put("edit","toolbox/miscellaneous/edit.m");
        functions.put("fileattrib","toolbox/miscellaneous/fileattrib.m");
        functions.put("fileparts","toolbox/miscellaneous/fileparts.m");
        functions.put("flops","toolbox/miscellaneous/flops.m");
        functions.put("fullfile","toolbox/miscellaneous/fullfile.m");
        functions.put("getfield","toolbox/miscellaneous/getfield.m");
        functions.put("gunzip","toolbox/miscellaneous/gunzip.m");
        functions.put("gzip","toolbox/miscellaneous/gzip.m");
        functions.put("info","toolbox/miscellaneous/info.m");
        functions.put("inputname","toolbox/miscellaneous/inputname.m");
        functions.put("intwarning","toolbox/miscellaneous/intwarning.m");
        functions.put("ismac","toolbox/miscellaneous/ismac.m");
        functions.put("ispc","toolbox/miscellaneous/ispc.m");
        functions.put("isunix","toolbox/miscellaneous/isunix.m");
        functions.put("license","toolbox/miscellaneous/license.m");
        functions.put("list_primes","toolbox/miscellaneous/list_primes.m");
        functions.put("ls_command","toolbox/miscellaneous/ls_command.m");
        functions.put("ls","toolbox/miscellaneous/ls.m");
        functions.put("menu","toolbox/miscellaneous/menu.m");
        functions.put("mex","toolbox/miscellaneous/mex.m");
        functions.put("mexext","toolbox/miscellaneous/mexext.m");
        functions.put("mkoctfile","toolbox/miscellaneous/mkoctfile.m");
        functions.put("movefile","toolbox/miscellaneous/movefile.m");
        functions.put("namelengthmax","toolbox/miscellaneous/namelengthmax.m");
        functions.put("news","toolbox/miscellaneous/news.m");
        functions.put("orderfields","toolbox/miscellaneous/orderfields.m");
        functions.put("pack","toolbox/miscellaneous/pack.m");
        functions.put("paren","toolbox/miscellaneous/paren.m");
        functions.put("parseparams","toolbox/miscellaneous/parseparams.m");
        functions.put("perl","toolbox/miscellaneous/perl.m");
        functions.put("run","toolbox/miscellaneous/run.m");
        functions.put("semicolon","toolbox/miscellaneous/semicolon.m");
        functions.put("setfield","toolbox/miscellaneous/setfield.m");
        functions.put("single","toolbox/miscellaneous/single.m");
        functions.put("substruct","toolbox/miscellaneous/substruct.m");
        functions.put("swapbytes","toolbox/miscellaneous/swapbytes.m");
        functions.put("symvar","toolbox/miscellaneous/symvar.m");
        functions.put("tar","toolbox/miscellaneous/tar.m");
        functions.put("tempdir","toolbox/miscellaneous/tempdir.m");
        functions.put("tempname","toolbox/miscellaneous/tempname.m");
        functions.put("texas_lotto","toolbox/miscellaneous/texas_lotto.m");
        functions.put("unix","toolbox/miscellaneous/unix.m");
        functions.put("unpack","toolbox/miscellaneous/unpack.m");
        functions.put("untar","toolbox/miscellaneous/untar.m");
        functions.put("unzip","toolbox/miscellaneous/unzip.m");
        //functions.put("ver","toolbox/miscellaneous/ver.m");
        //functions.put("version","toolbox/miscellaneous/version.m");
        functions.put("warning_ids","toolbox/miscellaneous/warning_ids.m");
        functions.put("what","toolbox/miscellaneous/what.m");
        //functions.put("xor","toolbox/miscellaneous/xor.m");
        functions.put("zip","toolbox/miscellaneous/zip.m");
        functions.put("__all_opts__","toolbox/optimization/__all_opts__.m");
        functions.put("__dogleg__","toolbox/optimization/__dogleg__.m");
        functions.put("__fdjac__","toolbox/optimization/__fdjac__.m");
        functions.put("fminunc","toolbox/optimization/fminunc.m");
        functions.put("fsolve","toolbox/optimization/fsolve.m");
        functions.put("fzero","toolbox/optimization/fzero.m");
        functions.put("glpk","toolbox/optimization/glpk.m");
        functions.put("glpkmex","toolbox/optimization/glpkmex.m");
        functions.put("lsqnonneg","toolbox/optimization/lsqnonneg.m");
        functions.put("optimget","toolbox/optimization/optimget.m");
        functions.put("optimset","toolbox/optimization/optimset.m");
        functions.put("qp","toolbox/optimization/qp.m");
        functions.put("sqp","toolbox/optimization/sqp.m");
        functions.put("__extractpath__","toolbox/path/__extractpath__.m");
        functions.put("matlabroot","toolbox/path/matlabroot.m");
        functions.put("pathdef","toolbox/path/pathdef.m");
        functions.put("savepath","toolbox/path/savepath.m");
        functions.put("pkg","toolbox/pkg/pkg.m");
        functions.put("commutation_matrix","toolbox/linearalgebra/commutation_matrix.m");
        functions.put("cond","toolbox/linearalgebra/cond.m");
        functions.put("condest","toolbox/linearalgebra/condest.m");
        functions.put("cross","toolbox/linearalgebra/cross.m");
        functions.put("dmult","toolbox/linearalgebra/dmult.m");
        functions.put("dot","toolbox/linearalgebra/dot.m");
        functions.put("duplication_matrix","toolbox/linearalgebra/duplication_matrix.m");
        functions.put("expm","toolbox/linearalgebra/expm.m");
        functions.put("housh","toolbox/linearalgebra/housh.m");
        functions.put("krylov","toolbox/linearalgebra/krylov.m");
        functions.put("krylovb","toolbox/linearalgebra/krylovb.m");
        functions.put("logm","toolbox/linearalgebra/logm.m");
        functions.put("norm","toolbox/linearalgebra/norm.m");
        functions.put("null","toolbox/linearalgebra/null.m");
        functions.put("onenormest","toolbox/linearalgebra/onenormest.m");
        functions.put("orth","toolbox/linearalgebra/orth.m");
        functions.put("planerot","toolbox/linearalgebra/planerot.m");
        functions.put("qzhess","toolbox/linearalgebra/qzhess.m");
        functions.put("rank","toolbox/linearalgebra/rank.m");
        functions.put("rref","toolbox/linearalgebra/rref.m");
        functions.put("subspace","toolbox/linearalgebra/subspace.m");
        functions.put("trace","toolbox/linearalgebra/trace.m");
        functions.put("vec","toolbox/linearalgebra/vec.m");
        functions.put("vech","toolbox/linearalgebra/vech.m");
        functions.put("__abcddims__","toolbox/control/system/__abcddims__.m");
        functions.put("abcddim","toolbox/control/system/abcddim.m");
        functions.put("is_abcd","toolbox/control/system/is_abcd.m");
        functions.put("is_sample","toolbox/control/system/is_sample.m");
        functions.put("ss2sys","toolbox/control/system/ss2sys.m");
        functions.put("sysgettsam","toolbox/control/system/sysgettsam.m");
        functions.put("sysgettype","toolbox/control/system/sysgettype.m");
        functions.put("axis2dlim","toolbox/control/util/axis2dlim.m");
        functions.put("swap","toolbox/control/util/swap.m");
        functions.put("zgfmul","toolbox/control/util/zgfmul.m");
        functions.put("aes","com.addi.toolbox.crypto");
        functions.put("demo001","toolbox/demos/demo001.m");
        functions.put("example01","com.addi.toolbox.demos");
        functions.put("example02","toolbox/demos/example02.m");
        functions.put("example03","toolbox/demos/example03.m");
        functions.put("example04","com.addi.toolbox.demos");
        functions.put("vdp1","toolbox/demos/vdp1.m");
        functions.put("vdp2","toolbox/demos/vdp2.m");
        functions.put("finite","toolbox/deprecated/finite.m");
        functions.put("isstr","toolbox/deprecated/isstr.m");
        functions.put("engine","toolbox/engine/engine.m");
        functions.put("fv","toolbox/finance/fv.m");
        functions.put("fvl","toolbox/finance/fvl.m");
        functions.put("nper","toolbox/finance/nper.m");
        functions.put("npv","toolbox/finance/npv.m");
        functions.put("pmt","toolbox/finance/pmt.m");
        functions.put("pv","toolbox/finance/pv.m");
        functions.put("pvl","toolbox/finance/pvl.m");
        functions.put("vol","toolbox/finance/vol.m");
        functions.put("ftp","toolbox/ftp/ftp.m");
        functions.put("euler","com.addi.toolbox.funfun");
        functions.put("eulerm","toolbox/funfun/eulerm.m");
        functions.put("feval","com.addi.toolbox.funfun");
        functions.put("_break","com.addi.toolbox.general");
        functions.put("_class","com.addi.toolbox.general");
        functions.put("_continue","com.addi.toolbox.general");
        functions.put("angle","com.addi.toolbox.general");
        functions.put("beep","com.addi.toolbox.general");
        functions.put("bench","toolbox/general/bench.m");
        functions.put("bitand","com.addi.toolbox.general");
        functions.put("bitor","com.addi.toolbox.general");
        functions.put("bitshift","com.addi.toolbox.general");
        functions.put("bitxor","com.addi.toolbox.general");
        functions.put("cell","com.addi.toolbox.general");
        functions.put("class","toolbox/general/class.m");
        functions.put("clc","com.addi.toolbox.general");
        functions.put("clear","com.addi.toolbox.general");
        functions.put("clock","com.addi.toolbox.general");
        functions.put("combinations","com.addi.toolbox.general");
        functions.put("complex","com.addi.toolbox.general");
        functions.put("conj","com.addi.toolbox.general");
        functions.put("diff","toolbox/general/diff.m");
        functions.put("exit","com.addi.toolbox.general");
        functions.put("factor","com.addi.toolbox.general");
        functions.put("false","toolbox/general/false.m");
        functions.put("fft","com.addi.toolbox.general");
        functions.put("fibonacci","com.addi.toolbox.general");
        functions.put("finish","toolbox/general/finish.m");
        functions.put("fix","com.addi.toolbox.general");
        functions.put("func2str","com.addi.toolbox.general");
        functions.put("getpfilecaching","com.addi.toolbox.general");
        functions.put("global","com.addi.toolbox.general");
        functions.put("harmonic","com.addi.toolbox.general");
        functions.put("help","com.addi.toolbox.general");
        functions.put("ed","com.addi.toolbox.general");
        functions.put("edit","com.addi.toolbox.general");
        functions.put("imag","com.addi.toolbox.general");
        functions.put("int16","com.addi.toolbox.general");
        functions.put("int32","com.addi.toolbox.general");
        functions.put("int64","com.addi.toolbox.general");
        functions.put("int8","com.addi.toolbox.general");
        functions.put("isa","com.addi.toolbox.general");
        functions.put("iscell","com.addi.toolbox.general");
        functions.put("iscellstr","com.addi.toolbox.general");
        functions.put("ischar","com.addi.toolbox.general");
        functions.put("cast","toolbox/general/cast.m");
        functions.put("isdefinite","toolbox/general/isdefinite.m");
        functions.put("isdouble","toolbox/general/isdouble.m");
        functions.put("isinteger","com.addi.toolbox.general");
        functions.put("isfunctionhandle","com.addi.toolbox.general");
        functions.put("isglobal","com.addi.toolbox.general");
        functions.put("isint16","toolbox/general/isint16.m");
        functions.put("isint32","toolbox/general/isint32.m");
        functions.put("isint64","toolbox/general/isint64.m");
        functions.put("isint8","toolbox/general/isint8.m");
        functions.put("islogical","com.addi.toolbox.general");
        functions.put("ismatrix","toolbox/general/ismatrix.m");
        functions.put("isnumeric","com.addi.toolbox.general");
        functions.put("isprime","com.addi.toolbox.general");
        functions.put("isscalar","toolbox/general/isscalar.m");
        functions.put("issingle","toolbox/general/issingle.m");
        functions.put("issqare","toolbox/general/issquare.m");
        functions.put("isstruct","com.addi.toolbox.general");
        functions.put("isstudent","com.addi.toolbox.general");
        functions.put("issymmetric","toolbox/general/issymmetric.m");
        functions.put("isuint16","toolbox/general/isuint16.m");
        functions.put("isuint32","toolbox/general/isuint32.m");
        functions.put("isuint64","toolbox/general/isuint64.m");
        functions.put("isuint8","toolbox/general/isuint8.m");
        functions.put("isvector","toolbox/general/isvector.m");
        functions.put("length","com.addi.toolbox.general");
        functions.put("linspace","com.addi.toolbox.general");
        functions.put("logical","com.addi.toolbox.general");
        functions.put("logspace","toolbox/general/logspace.m");
        functions.put("lookup","toolbox/general/lookup.m");
        functions.put("mod","toolbox/general/mod.m");
        functions.put("ndims","com.addi.toolbox.general");
        functions.put("nextpow2","toolbox/general/nextpow2.m");
        functions.put("nthroot","toolbox/general/nthroot.m");
        functions.put("numel","toolbox/general/numel.m");
        functions.put("performfunction","com.addi.toolbox.general");
        functions.put("permutations","com.addi.toolbox.general");
        functions.put("primes","com.addi.toolbox.general");
        functions.put("quit","com.addi.toolbox.general");
        functions.put("rand","com.addi.toolbox.general");
        functions.put("randn","com.addi.toolbox.general");
        functions.put("randperm","toolbox/general/randperm.m");
        functions.put("real","com.addi.toolbox.general");
        functions.put("rem","toolbox/general/rem.m");
        functions.put("setpfilecaching","com.addi.toolbox.general");
        functions.put("sign","com.addi.toolbox.general");
        functions.put("size","com.addi.toolbox.general");
        functions.put("size_equal","com.addi.toolbox.general");
        functions.put("startup","toolbox/general/startup.m");
        functions.put("str2func","com.addi.toolbox.general");
        functions.put("struct","com.addi.toolbox.general");
        functions.put("tril","toolbox/general/tril.m");
        functions.put("triu","toolbox/general/triu.m");
        functions.put("true","toolbox/general/true.m");
        functions.put("uint16","com.addi.toolbox.general");
        functions.put("uint32","com.addi.toolbox.general");
        functions.put("uint8","com.addi.toolbox.general");
        functions.put("who","com.addi.toolbox.general");
        functions.put("whos","com.addi.toolbox.general");
        functions.put("__isequal__","toolbox/general/__isequal__.m");
        functions.put("__splinen__","toolbox/general/__splinen__.m");
        functions.put("accumarray","toolbox/general/accumarray.m");
        functions.put("arrayfun","toolbox/general/arrayfun.m");
        functions.put("bicubic","toolbox/general/bicubic.m");
        functions.put("bitcmp","toolbox/general/bitcmp.m");
        functions.put("bitget","toolbox/general/bitget.m");
        functions.put("bitset","toolbox/general/bitset.m");
        functions.put("blkdiag","toolbox/general/blkdiag.m");
        functions.put("cart2pol","toolbox/general/cart2pol.m");
        functions.put("cart2sph","toolbox/general/cart2sph.m");
        functions.put("cell2mat","toolbox/general/cell2mat.m");
        functions.put("celldisp","toolbox/general/celldisp.m");
        functions.put("cellidx","toolbox/general/cellidx.m");
        functions.put("circshift","toolbox/general/circshift.m");
        functions.put("colon","toolbox/general/colon.m");
        functions.put("common_size","toolbox/general/common_size.m");
        functions.put("cplxpair","toolbox/general/cplxpair.m");
        functions.put("cumtrapz","toolbox/general/cumtrapz.m");
        functions.put("dblquad","toolbox/general/dblquad.m");
        functions.put("deal","toolbox/general/deal.m");
        functions.put("del2","toolbox/general/del2.m");
        functions.put("display","toolbox/general/display.m");
        functions.put("flipdim","toolbox/general/flipdim.m");
        functions.put("fliplr","toolbox/general/fliplr.m");
        functions.put("flipud","toolbox/general/flipud.m");
        functions.put("getvarname","toolbox/general/getvarname.m");
        functions.put("gradient","toolbox/general/gradient.m");
        functions.put("idivide","toolbox/general/idivide.m");
        functions.put("ind2sub","toolbox/general/ind2sub.m");
        functions.put("int2str","toolbox/general/int2str.m");
        functions.put("interp1","toolbox/general/interp1.m");
        functions.put("interp1q","toolbox/general/interp1q.m");
        functions.put("interp2","toolbox/general/interp2.m");
        functions.put("interp3","toolbox/general/interp3.m");
        functions.put("interpft","toolbox/general/interpft.m");
        functions.put("interpn","toolbox/general/interpn.m");
        functions.put("is_duplicate_entry","toolbox/general/is_duplicate_entry.m");
        //CCX functions.put("isa","toolbox/general/isa.m");
        functions.put("isdir","toolbox/general/isdir.m");
        functions.put("isequal","toolbox/general/isequal.m");
        functions.put("isequalwithequalnans","toolbox/general/isequalwithequalnans.m");
        functions.put("loadobj","toolbox/general/loadobj.m");
        //CCX functions.put("logical","toolbox/general/logical.m");
        //CCX functions.put("nargchk","toolbox/general/nargchk.m");
        //CCX functions.put("nargoutchk","toolbox/general/nargoutchk.m");
        //CCX functions.put("num2str","toolbox/general/num2str.m");
        functions.put("perror","toolbox/general/perror.m");
        functions.put("pol2cart","toolbox/general/pol2cart.m");
        functions.put("polyarea","toolbox/general/polyarea.m");
        functions.put("postpad","toolbox/general/postpad.m");
        functions.put("prepad","toolbox/general/prepad.m");
        functions.put("quadgk","toolbox/general/quadgk.m");
        functions.put("quadl","toolbox/general/quadl.m");
        functions.put("quadv","toolbox/general/quadv.m");
        functions.put("rat","toolbox/general/rat.m");
        //CCX functions.put("repmat","toolbox/general/repmat.m");
        functions.put("rot90","toolbox/general/rot90.m");
        functions.put("rotdim","toolbox/general/rotdim.m");
        functions.put("runlength","toolbox/general/runlength.m");
        functions.put("saveobj","toolbox/general/saveobj.m");
        functions.put("shift","toolbox/general/shift.m");
        functions.put("shiftdim","toolbox/general/shiftdim.m");
        functions.put("sortrows","toolbox/general/sortrows.m");
        functions.put("sph2cart","toolbox/general/sph2cart.m");
        functions.put("strerror","toolbox/general/strerror.m");
        functions.put("sub2ind","toolbox/general/sub2ind.m");
        functions.put("subsindex","toolbox/general/subsindex.m");
        functions.put("trapz","toolbox/general/trapz.m");
        functions.put("triplequad","toolbox/general/triplequad.m");
        functions.put("__img__","toolbox/image/__img__.m");
        functions.put("__img_via_file__","toolbox/image/__img_via_file__.m");
        functions.put("autumn","toolbox/image/autumn.m");
        functions.put("bone","toolbox/image/bone.m");
        functions.put("brighten","toolbox/image/brighten.m");
        functions.put("colormap","toolbox/image/colormap.m");
        functions.put("contrast","toolbox/image/contrast.m");
        functions.put("cool","toolbox/image/cool.m");
        functions.put("copper","toolbox/image/copper.m");
        functions.put("flag","toolbox/image/flag.m");
        functions.put("gmap40","toolbox/image/gmap40.m");
        functions.put("gray","toolbox/image/gray.m");
        functions.put("gray2ind","toolbox/image/gray2ind.m");
        functions.put("hot","toolbox/image/hot.m");
        functions.put("hsv","toolbox/image/hsv.m");
        functions.put("hsv2rgb","toolbox/image/hsv2rgb.m");
        functions.put("image_viewer","toolbox/image/image_viewer.m");
        functions.put("image","toolbox/image/image.m");
        functions.put("imagesc","toolbox/image/imagesc.m");
        functions.put("imfinfo","toolbox/image/imfinfo.m");
        functions.put("imread","toolbox/image/imread.m");
        functions.put("imshow","toolbox/image/imshow.m");
        functions.put("imwrite","toolbox/image/imwrite.m");
        functions.put("ind2gray","toolbox/image/ind2gray.m");
        functions.put("ind2rgb","toolbox/image/ind2rgb.m");
        functions.put("jet","toolbox/image/jet.m");
        functions.put("ntsc2rgb","toolbox/image/ntsc2rgb.m");
        functions.put("ocean","toolbox/image/ocean.m");
        functions.put("pink","toolbox/image/pink.m");
        functions.put("prism","toolbox/image/prism.m");
        functions.put("rainbow","toolbox/image/rainbow.m");
        functions.put("rgb2hsv","toolbox/image/rgb2hsv.m");
        functions.put("rgb2ind","toolbox/image/rgb2ind.m");
        functions.put("rgb2ntsc","toolbox/image/rgb2ntsc.m");
        functions.put("saveimage","toolbox/image/saveimage.m");
        functions.put("spring","toolbox/image/spring.m");
        functions.put("summer","toolbox/image/summer.m");
        functions.put("white","toolbox/image/white.m");
        functions.put("winter","toolbox/image/winter.m");
        functions.put("crule","toolbox/integration/crule.m");
        functions.put("ncrule","toolbox/integration/ncrule.m");
        functions.put("cd","com.addi.toolbox.io");
        functions.put("createnewfile","com.addi.toolbox.io");
        functions.put("csvread","com.addi.toolbox.io");
        functions.put("csvwrite","com.addi.toolbox.io");
        functions.put("delete","com.addi.toolbox.io");
        functions.put("dir","com.addi.toolbox.io");
        functions.put("exist","com.addi.toolbox.io");
        functions.put("fopen","com.addi.toolbox.io");
        functions.put("fclose","com.addi.toolbox.io");
        functions.put("fprintf","com.addi.toolbox.io");
        functions.put("fgets","com.addi.toolbox.io");
        functions.put("isdirectory","com.addi.toolbox.io");
        functions.put("isfile","com.addi.toolbox.io");
        functions.put("ishidden","com.addi.toolbox.io");
        functions.put("lastmodified","com.addi.toolbox.io");
        functions.put("load","com.addi.toolbox.io");
        functions.put("loadvariables","com.addi.toolbox.io");
        functions.put("mkdir","com.addi.toolbox.io");
        functions.put("pwd","com.addi.toolbox.io");
        functions.put("rmdir","com.addi.toolbox.io");
        functions.put("runfile","com.addi.toolbox.io");
        functions.put("savevariables","com.addi.toolbox.io");
        functions.put("systemcommand","com.addi.toolbox.io");
        functions.put("abs","com.addi.toolbox.jmathlib.matrix");
        functions.put("adjoint","com.addi.toolbox.jmathlib.matrix");
        functions.put("all","com.addi.toolbox.jmathlib.matrix");
        functions.put("and","com.addi.toolbox.jmathlib.matrix");
        functions.put("any","com.addi.toolbox.jmathlib.matrix");
        functions.put("ceil","com.addi.toolbox.jmathlib.matrix");
        functions.put("chol","com.addi.toolbox.jmathlib.matrix");
        functions.put("col","toolbox/jmathlib/matrix/col.m");
        functions.put("columns","toolbox/jmathlib/matrix/columns.m");
        functions.put("ctranspose","toolbox/jmathlib/matrix/ctranspose.m");
        functions.put("cumprod","com.addi.toolbox.jmathlib.matrix");
        functions.put("cumsum","com.addi.toolbox.jmathlib.matrix");
        functions.put("det","toolbox/jmathlib/matrix/det.m");
        functions.put("determinant","com.addi.toolbox.jmathlib.matrix");
        functions.put("diag","com.addi.toolbox.jmathlib.matrix");
        functions.put("eig","com.addi.toolbox.jmathlib.matrix");
        functions.put("elementat","com.addi.toolbox.jmathlib.matrix");
        functions.put("eq","toolbox/jmathlib/matrix/eq.m");
        functions.put("exp","com.addi.toolbox.jmathlib.matrix");
        functions.put("eye","com.addi.toolbox.jmathlib.matrix");
        functions.put("find","com.addi.toolbox.jmathlib.matrix");
        //functions.put("fliplr","com.addi.toolbox.jmathlib.matrix");
        //functions.put("flipud","com.addi.toolbox.jmathlib.matrix");
        functions.put("floor","com.addi.toolbox.jmathlib.matrix");
        functions.put("ge","toolbox/jmathlib/matrix/ge.m");
        functions.put("gt","toolbox/jmathlib/matrix/gt.m");
        functions.put("inf","com.addi.toolbox.jmathlib.matrix");
        functions.put("inv","toolbox/jmathlib/matrix/inv.m");
        functions.put("inversematrix","com.addi.toolbox.jmathlib.matrix");
        functions.put("isempty","com.addi.toolbox.jmathlib.matrix");
        functions.put("isfinite","com.addi.toolbox.jmathlib.matrix");
        functions.put("isimaginary","com.addi.toolbox.jmathlib.matrix");
        functions.put("isinf","com.addi.toolbox.jmathlib.matrix");
        functions.put("isnan","com.addi.toolbox.jmathlib.matrix");
        functions.put("isreal","com.addi.toolbox.jmathlib.matrix");
        functions.put("ldivide","toolbox/jmathlib/matrix/ldivide.m");
        functions.put("le","toolbox/jmathlib/matrix/inv.m");
        functions.put("ln","com.addi.toolbox.jmathlib.matrix");
        functions.put("log","com.addi.toolbox.jmathlib.matrix");
        functions.put("log10","com.addi.toolbox.jmathlib.matrix");
        functions.put("lowertriangle","com.addi.toolbox.jmathlib.matrix");
        functions.put("lt","toolbox/jmathlib/matrix/lt.m");
        functions.put("lu","com.addi.toolbox.jmathlib.matrix");
        //functions.put("magic","com.addi.toolbox.jmathlib.matrix");
        functions.put("max","com.addi.toolbox.jmathlib.matrix");
        functions.put("min","com.addi.toolbox.jmathlib.matrix");
        functions.put("minus","toolbox/jmathlib/matrix/minus.m");
        functions.put("mldivide","toolbox/jmathlib/matrix/mldivide.m");
        functions.put("mpower","toolbox/jmathlib/matrix/mpower.m");
        functions.put("mrdivide","toolbox/jmathlib/matrix/mrdivide.m");
        functions.put("mtimes","toolbox/jmathlib/matrix/mtimes.m");
        functions.put("nan","com.addi.toolbox.jmathlib.matrix");
        functions.put("ne","toolbox/jmathlib/matrix/ne.m");
        functions.put("nnz","com.addi.toolbox.jmathlib.matrix");
        functions.put("not","com.addi.toolbox.jmathlib.matrix");
        functions.put("numel","com.addi.toolbox.jmathlib.matrix");
        functions.put("ones","com.addi.toolbox.jmathlib.matrix");
        functions.put("or","com.addi.toolbox.jmathlib.matrix");
        functions.put("plus","toolbox/jmathlib/matrix/plus.m");
        functions.put("pow2","com.addi.toolbox.jmathlib.matrix");
        functions.put("power","toolbox/jmathlib/matrix/power.m");
        functions.put("prod","com.addi.toolbox.jmathlib.matrix");
        functions.put("qr","com.addi.toolbox.jmathlib.matrix");
        functions.put("rdivide","toolbox/jmathlib/matrix/rdivide.m");
        functions.put("repmat","com.addi.toolbox.jmathlib.matrix");
        functions.put("reshape","com.addi.toolbox.jmathlib.matrix");
        functions.put("round","com.addi.toolbox.jmathlib.matrix");
        functions.put("row","toolbox/jmathlib/matrix/row.m");
        functions.put("rows","toolbox/jmathlib/matrix/rows.m");
        functions.put("simultaneouseq","com.addi.toolbox.jmathlib.matrix");
        functions.put("sort","com.addi.toolbox.jmathlib.matrix");
        functions.put("sqrt","com.addi.toolbox.jmathlib.matrix");
        functions.put("subassign","com.addi.toolbox.jmathlib.matrix");
        functions.put("submatrix","com.addi.toolbox.jmathlib.matrix");
        functions.put("sum","com.addi.toolbox.jmathlib.matrix");
        functions.put("sumsq","toolbox/jmathlib/matrix/sumsq.m");
        functions.put("svd","com.addi.toolbox.jmathlib.matrix");
        functions.put("times","toolbox/jmathlib/matrix/times.m");
        functions.put("transpose","toolbox/jmathlib/matrix/transpose.m");
        functions.put("uminus","toolbox/jmathlib/matrix/uminus.m");
        functions.put("uplus","toolbox/jmathlib/matrix/uplus.m");
        functions.put("uppertriangle","com.addi.toolbox.jmathlib.matrix");
        functions.put("xor","com.addi.toolbox.jmathlib.matrix");
        functions.put("zeros","com.addi.toolbox.jmathlib.matrix");
        functions.put("addpath","com.addi.toolbox.jmathlib.system");
        functions.put("checkforupdates","com.addi.toolbox.jmathlib.system");
        functions.put("createfunctionslist","com.addi.toolbox.jmathlib.system");
        functions.put("dbquit","toolbox/jmathlib/system/dbquit.m");
        functions.put("debug","com.addi.toolbox.jmathlib.system");
        functions.put("disp","com.addi.toolbox.jmathlib.system");
        functions.put("error","com.addi.toolbox.jmathlib.system");
        functions.put("exit","toolbox/jmathlib/system/exit.m");
        functions.put("foreach","com.addi.toolbox.jmathlib.system");
        functions.put("format","com.addi.toolbox.jmathlib.system");
        functions.put("getdebug","com.addi.toolbox.jmathlib.system");
        functions.put("getenv","com.addi.toolbox.jmathlib.system");
        functions.put("getjmathlibproperty","com.addi.toolbox.jmathlib.system");
        functions.put("getproperty","toolbox/jmathlib/system/getproperty.m");
        functions.put("java","com.addi.toolbox.jmathlib.system");
        functions.put("getjmathlibcreateuniqueid","com.addi.toolbox.jmathlib.system");
        functions.put("nargchk","com.addi.toolbox.jmathlib.system");
        functions.put("nargoutchk","com.addi.toolbox.jmathlib.system");
        functions.put("newline","com.addi.toolbox.jmathlib.system");
        functions.put("path","com.addi.toolbox.jmathlib.system");
        functions.put("print_usage","com.addi.toolbox.jmathlib.system");
        functions.put("printstacktrace","com.addi.toolbox.jmathlib.system");
        functions.put("quit","com.addi.toolbox.jmathlib.system");
        functions.put("rehash","com.addi.toolbox.jmathlib.system");
        functions.put("rmpath","com.addi.toolbox.jmathlib.system");
        functions.put("setdebug","com.addi.toolbox.jmathlib.system");
        functions.put("setjmathlibproperty","com.addi.toolbox.jmathlib.system");
        functions.put("usage","com.addi.toolbox.jmathlib.system");
        functions.put("ver","com.addi.toolbox.jmathlib.system");
        functions.put("version","com.addi.toolbox.jmathlib.system");
        functions.put("warning","com.addi.toolbox.jmathlib.system");
        functions.put("freemat","toolbox/jmathlib/freemat.m");
        functions.put("jmathlib","toolbox/jmathlib/jmathlib.m");
        functions.put("matlab","toolbox/jmathlib/matlab.m");
        functions.put("octave","toolbox/jmathlib/octave.m");
        functions.put("scilab","toolbox/jmathlib/scilab.m");
        functions.put("test_complex","toolbox/test/test_complex.m");
        functions.put("test_for","toolbox/test/test_for.m");
        functions.put("test_for002","toolbox/test/test_for002.m");
        functions.put("test_for003","toolbox/test/test_for003.m");
        functions.put("test_graph","toolbox/test/test_graph.m");
        functions.put("test_matlabfor","toolbox/test/test_matlabfor.m");
        functions.put("test_recursion","toolbox/test/test_recursion.m");
        functions.put("test_scanner001","toolbox/test/test_scanner001.m");
        functions.put("test_script","toolbox/test/test_script.m");
        functions.put("test_scriptcall","toolbox/test/test_scriptcall.m");
        functions.put("test_standard","toolbox/test/test_standard.m");
        functions.put("test_switch001","toolbox/test/test_switch001.m");
        functions.put("test_switch002","toolbox/test/test_switch002.m");
        functions.put("test_trig","toolbox/test/test_trig.m");
        functions.put("test008","toolbox/test/test008.m");
        functions.put("test009","toolbox/test/test009.m");
        functions.put("test010","toolbox/test/test010.m");
        functions.put("test011","toolbox/test/test011.m");
        functions.put("test012","toolbox/test/test012.m");
        functions.put("testFunction001","toolbox/test/testFunction001.m");
        functions.put("testFunction002","toolbox/test/testFunction002.m");
        functions.put("testFunction003","toolbox/test/testFunction003.m");
        functions.put("testFunction004","toolbox/test/testFunction004.m");
        functions.put("testFunction005","toolbox/test/testFunction005.m");
        functions.put("testFunction006","toolbox/test/testFunction006.m");
        functions.put("testFunction006a","toolbox/test/testFunction006a.m");
        functions.put("testFunction006b","toolbox/test/testFunction006b.m");
        functions.put("testFunction007","toolbox/test/testFunction007.m");
        functions.put("testFunctionFor001","toolbox/test/testFunctionFor001.m");
        functions.put("testFunctionFor002","toolbox/test/testFunctionFor002.m");
        functions.put("testFunctionIfElse001","toolbox/test/testFunctionIfElse001.m");
        functions.put("testFunctionSwitch001","toolbox/test/testFunctionSwitch001.m");
        functions.put("testFunctionSwitch002","toolbox/test/testFunctionSwitch002.m");
        functions.put("testFunctionWhile001","toolbox/test/testFunctionWhile001.m");
        functions.put("testGlobal001","toolbox/test/testGlobal001.m");
        functions.put("testIf001","toolbox/test/testIf001.m");
        functions.put("testIf002","toolbox/test/testIf002.m");
        functions.put("testIf003","toolbox/test/testIf003.m");
        functions.put("testNarginNargout001","toolbox/test/testNarginNargout001.m");
        functions.put("assert","toolbox/testfun/assert.m");
        functions.put("demo","toolbox/testfun/demo.m");
        functions.put("example","toolbox/testfun/example.m");
        functions.put("fail","toolbox/testfun/fail.m");
        functions.put("rundemos","toolbox/testfun/rundemos.m");
        functions.put("speed","toolbox/testfun/speed.m");
        functions.put("test","toolbox/testfun/test.m");
        functions.put("plot","com.addi.toolbox.plot");
        functions.put("deg2rad", "deg2rad.addiMappingPackage.mpackage");
        functions.put("rad2deg", "rad2deg.addiMappingPackage.mpackage");
        functions.put("azimuth", "azimuth.addiMappingPackage.mpackage");
        functions.put("distance", "distance.addiMappingPackage.mpackage");
        functions.put("km2deg", "km2deg.addiMappingPackage.mpackage");
        functions.put("reckon", "reckon.addiMappingPackage.mpackage");
    }

    /**
     * For each of the FunctionLoaders, check that any cached functions are up to date. If
     * some are out of date, or have been deleted, ensure that the cache it updated.
     */
    public void checkAndRehashTimeStamps() {
        for (int i = 0; i < this.functionLoaders.size(); i++) {
            FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);
            l.checkAndRehashTimeStamps();
        }
    }

    /**find a function
    It checks user functions then external functions then internal functions
    @param token - The function token containing the name of the function
    @return the Function found*/
    public Function findFunction(FunctionToken token) throws java.lang.Exception {
        Function func = null;
        String funcName = token.getName();
        
 /*CCX
        int index = -1;

        //then check the external functions
        try {
            if (runningStandalone) {
                // JMathlib is running as a standalone application
                //Search for class, m or p file
            	for (int i = 0; i < functionLoaders.size(); i++) {
                    FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);

                    func = l.findFunction(funcName);
                    if (func != null) {
                        return func;
                    }
                }
                //CCX functionLoader.loadClass("com.addi.toolbox.elfun." + funcName);

                // functions not found (no class or m- or p-file)
                //
                if (token.getOperand(0) != null) {
                    ErrorLogger.debugLine("************checking first param****************");
                    //get first parameter
                    //Token first = ((Expression)token.getOperand(0)).getLeft();
                    Token first = token.getOperand(0);
                    ErrorLogger.debugLine("class = " + first.getClass());
                    //if parameter is variable token
                    if ((first instanceof VariableToken) || (first instanceof Expression)) {
                        ErrorLogger.debugLine("************searching for java class***********");
                        String className = "";
                        if (first instanceof VariableToken) {
                            className = ((VariableToken) first).getName();
                        } else if (first instanceof FunctionToken) {
                            className = ((FunctionToken) first).getName();
                        } else {
                            className = first.toString();
                            className = className.substring(0, className.length() - 2);
                        }

                        ErrorLogger.debugLine("classname = " + className);

                        for (int i = 0; i < functionLoaders.size(); i++) {
                            FileFunctionLoader l = (FileFunctionLoader) functionLoaders.elementAt(i);
                            Class extFunctionClass = l.findOnlyFunctionClass(className);
                            if (extFunctionClass != null) {
                                ErrorLogger.debugLine("found class " + className);
                                ReflectionFunctionCall reflect = new ReflectionFunctionCall(extFunctionClass, token.toString());
                                ErrorLogger.debugLine("+++++func1 " + reflect.toString());
                                return reflect;
                            }
                        }
                    }
                }
            } else {  
                // NOT standalone, but APPLET
                // use webloader
                //Search for class, m or p file
                for (int i = 0; i < functionLoaders.size(); i++) {
                    FunctionLoader l = (WebFunctionLoader) functionLoaders.elementAt(i);

                    func = l.findFunction(funcName);
                    if (func != null) {
                        return func;
                    }
                }

                return null;
            } // end webLoader
        } catch (Exception exception) {
            exception.printStackTrace();
        }
 */ //CCX
        //test if file is in working directory
        File workingDirMFile = new File(workingDirectory, funcName + ".m");
        if (workingDirMFile.exists()) {
        	func = mLoader.loadMFile(workingDirMFile);
        } else {
	        func = loadedFunctions.get(funcName);
	        if (func == null) {
	           String funcPath = functions.get(funcName);
	           if (funcPath == null) {
	              func = null;
	           } else if (functions.get(funcName).endsWith(".m")) {
	              func = mLoader.loadBuiltInMFile(funcName,functions.get(funcName)); 	
	           } else if (functions.get(funcName).endsWith(".mpackage")) {
	        	   func = mLoader.loadPackageMFile(funcName,functions.get(funcName));
	           } else {
	              try { 
	                  func = (Function)( Class.forName( functions.get(funcName)+ "." + funcName ).newInstance() );
	              } catch (ClassNotFoundException e) {
	                 func = null;
	              }
	           }
	        }
	        if (func != null) {
	           loadedFunctions.put(funcName, func);
	        }
        }
        return func;
    }

    public Function findFunctionByName(String funcName) throws java.lang.Exception {
        FunctionToken token = new FunctionToken(funcName);
        return findFunction(token);
    }

    public void clear() {
        ErrorLogger.debugLine("FunctionManager: clear user functions");
        for (int i = 0; i < functionLoaders.size(); i++) {
            FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);
            l.clearCache();
        }
    }

    /** set caching of p-file to on of off
     *
     * @param pFileCaching  true= caching of p-files on; false: caching of p-files off
     */
    public void setPFileCaching(boolean pFileCaching) {
        pFileCachingEnabledB = pFileCaching;
        for (int i = 0; i < this.functionLoaders.size(); i++) {
            FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);
            l.setPFileCaching(pFileCaching);
        }
    }

    /** 
     * return whether of not caching of p-files is enabled of not
     * @return status of caching p-files
     */
    public boolean getPFileCaching() {
        return pFileCachingEnabledB;
    }

    /**
     * 
     * @return
     */
    public int getFunctionLoaderCount() {
        return functionLoaders.size();
    	//CCX return 1;
    }

    /**
     * 
     * @param index
     * @return
     */
    public FunctionLoader getFunctionLoader(int index) {
    	return (FunctionLoader) functionLoaders.elementAt(index);
        //CCX return null;
    }

    /**
     * 
     * @param loader
     * @return
     */
    public boolean removeFunctionLoader(FunctionLoader loader) {
        if (loader.isSystemLoader())
            throw new IllegalArgumentException("Cannot remove a System Function Loader");
        return functionLoaders.remove(loader);
        //CCX return false;
    }

    /**
     * 
     * @param loader
     * @return
     */
    public boolean addFunctionLoader(FunctionLoader loader) {
        return functionLoaders.add(loader);
    	//CCX return false;
    }

    /**
     * 
     * @param index
     * @param loader
     */
    public void addFunctionLoaderAt(int index, FunctionLoader loader) {
        functionLoaders.add(index, loader);
    }

    /**
     * 
     */
    public void clearCustomFunctionLoaders() {
        Iterator itr = functionLoaders.iterator();
        while (itr.hasNext()) {
            FunctionLoader fl = (FunctionLoader)itr.next();
            if (!fl.isSystemLoader())
                itr.remove();
        }
    }

    /**
     * 
     * @param path
     */
    public void setWorkingDirectory(File path) {
        workingDirectory = path;
    }

    /**
     * 
     * @return
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }
}

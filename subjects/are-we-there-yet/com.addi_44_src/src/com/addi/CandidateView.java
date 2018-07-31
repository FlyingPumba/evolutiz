/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.addi;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class CandidateView extends View {

    private static final int OUT_OF_BOUNDS = -1;

    private AddiBase mService;
    private List<String> mSuggestions;
    private int mSelectedIndex;
    private int mTouchX = OUT_OF_BOUNDS;
    private Drawable mSelectionHighlight;
    private boolean mTypedWordValid;
    
    private Rect mBgPadding;

    private static final int MAX_SUGGESTIONS = 1024;
    private static final int SCROLL_PIXELS = 20;
    
    private int[] mWordWidth = new int[MAX_SUGGESTIONS];
    private int[] mWordX = new int[MAX_SUGGESTIONS];

    private static final int X_GAP = 10;
    
    private static final List<String> EMPTY_LIST = new ArrayList<String>();

    private int mColorNormal;
    private int mColorRecommended;
    private int mColorOther;
    private int mVerticalPadding;
    private Paint mPaint;
    private boolean mScrolled;
    private int mTargetScrollX;
    
    private int mTotalWidth;
    
    private GestureDetector mGestureDetector;
    
    public List<String> mPossibleCompletions = new ArrayList<String>();
    public List<String> mPossibleCompletionsRsrvd = new ArrayList<String>();

    public void init(Context context)
    {
    	mService = (AddiBase)context;
        mSelectionHighlight = context.getResources().getDrawable(
                android.R.drawable.list_selector_background);
        mSelectionHighlight.setState(new int[] {
                android.R.attr.state_enabled,
                android.R.attr.state_focused,
                android.R.attr.state_window_focused,
                android.R.attr.state_pressed
        });

        Resources r = context.getResources();
        
        setBackgroundColor(r.getColor(R.color.candidate_background));
        
        mColorNormal = r.getColor(R.color.candidate_normal);
        mColorRecommended = r.getColor(R.color.candidate_recommended);
        mColorOther = r.getColor(R.color.candidate_other);
        mVerticalPadding = r.getDimensionPixelSize(R.dimen.candidate_vertical_padding);
        
        mPaint = new Paint();
        mPaint.setColor(mColorNormal);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_height));
        mPaint.setStrokeWidth(0);
        
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                mScrolled = true;
                int sx = getScrollX();
                sx += distanceX;
                if (sx < 0) {
                    sx = 0;
                }
                if (sx + getWidth() > mTotalWidth) {                    
                    sx -= distanceX;
                }
                mTargetScrollX = sx;
                scrollTo(sx, getScrollY());
                invalidate();
                return true;
            }
        });
        setHorizontalFadingEdgeEnabled(true);
        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        
        mPossibleCompletions.add("abs");
        mPossibleCompletions.add("accumarray");
        mPossibleCompletions.add("accumdim");
        mPossibleCompletions.add("acos");
        mPossibleCompletions.add("acosd");
        mPossibleCompletions.add("acosh");
        mPossibleCompletions.add("acot");
        mPossibleCompletions.add("acotd");
        mPossibleCompletions.add("acoth");
        mPossibleCompletions.add("acsc");
        mPossibleCompletions.add("acscd");
        mPossibleCompletions.add("acsch");
        mPossibleCompletions.add("addlistener");
        mPossibleCompletions.add("addpath");
        mPossibleCompletions.add("addproperty");
        mPossibleCompletions.add("addtodate");
        mPossibleCompletions.add("airy");
        mPossibleCompletions.add("all");
        mPossibleCompletions.add("allchild");
        mPossibleCompletions.add("amd");
        mPossibleCompletions.add("ancestor");
        mPossibleCompletions.add("and");
        mPossibleCompletions.add("angle");
        mPossibleCompletions.add("anova");
        mPossibleCompletions.add("any");
        mPossibleCompletions.add("arch_fit");
        mPossibleCompletions.add("arch_rnd");
        mPossibleCompletions.add("arch_test");
        mPossibleCompletions.add("area");
        mPossibleCompletions.add("arg");
        mPossibleCompletions.add("argnames");
        mPossibleCompletions.add("argv");
        mPossibleCompletions.add("arma_rnd");
        mPossibleCompletions.add("arrayfun");
        mPossibleCompletions.add("ascii");
        mPossibleCompletions.add("asctime");
        mPossibleCompletions.add("asec");
        mPossibleCompletions.add("asecd");
        mPossibleCompletions.add("asech");
        mPossibleCompletions.add("asin");
        mPossibleCompletions.add("asind");
        mPossibleCompletions.add("asinh");
        mPossibleCompletions.add("assert");
        mPossibleCompletions.add("assignin");
        mPossibleCompletions.add("atan");
        mPossibleCompletions.add("atan2");
        mPossibleCompletions.add("atand");
        mPossibleCompletions.add("atanh");
        mPossibleCompletions.add("atexit");
        mPossibleCompletions.add("autoload");
        mPossibleCompletions.add("autoreg_matrix");
        mPossibleCompletions.add("autumn");
        mPossibleCompletions.add("available_graphics_toolkits");
        mPossibleCompletions.add("axes");
        mPossibleCompletions.add("axis");
        mPossibleCompletions.add("balance");
        mPossibleCompletions.add("bar");
        mPossibleCompletions.add("barh");
        mPossibleCompletions.add("bartlett");
        mPossibleCompletions.add("bartlett_test");
        mPossibleCompletions.add("base2dec");
        mPossibleCompletions.add("beep");
        mPossibleCompletions.add("beep_on_error");
        mPossibleCompletions.add("besselh");
        mPossibleCompletions.add("besseli");
        mPossibleCompletions.add("besselj");
        mPossibleCompletions.add("besselk");
        mPossibleCompletions.add("bessely");
        mPossibleCompletions.add("beta");
        mPossibleCompletions.add("betacdf");
        mPossibleCompletions.add("betainc");
        mPossibleCompletions.add("betainv");
        mPossibleCompletions.add("betaln");
        mPossibleCompletions.add("betapdf");
        mPossibleCompletions.add("betarnd");
        mPossibleCompletions.add("bicgstab");
        mPossibleCompletions.add("bicubic");
        mPossibleCompletions.add("bin2dec");
        mPossibleCompletions.add("binary");
        mPossibleCompletions.add("bincoeff");
        mPossibleCompletions.add("binocdf");
        mPossibleCompletions.add("binoinv");
        mPossibleCompletions.add("binopdf");
        mPossibleCompletions.add("binornd");
        mPossibleCompletions.add("bitand");
        mPossibleCompletions.add("bitcmp");
        mPossibleCompletions.add("bitget");
        mPossibleCompletions.add("bitmax");
        mPossibleCompletions.add("bitor");
        mPossibleCompletions.add("bitpack");
        mPossibleCompletions.add("bitset");
        mPossibleCompletions.add("bitshift");
        mPossibleCompletions.add("bitxor");
        mPossibleCompletions.add("blackman");
        mPossibleCompletions.add("blanks");
        mPossibleCompletions.add("blkdiag");
        mPossibleCompletions.add("bone");
        mPossibleCompletions.add("box");
        mPossibleCompletions.add("brighten");
        mPossibleCompletions.add("bsxfun");
        mPossibleCompletions.add("builtin");
        mPossibleCompletions.add("bunzip2");
        mPossibleCompletions.add("byte_size");
        mPossibleCompletions.add("bzip2");
        mPossibleCompletions.add("calendar");
        mPossibleCompletions.add("canonicalize_file_name");
        mPossibleCompletions.add("cart2pol");
        mPossibleCompletions.add("cart2sph");
        mPossibleCompletions.add("cast");
        mPossibleCompletions.add("cat");
        mPossibleCompletions.add("cauchy_cdf");
        mPossibleCompletions.add("cauchy_inv");
        mPossibleCompletions.add("cauchy_pdf");
        mPossibleCompletions.add("cauchy_rnd");
        mPossibleCompletions.add("caxis");
        mPossibleCompletions.add("cbrt");
        mPossibleCompletions.add("ccolamd");
        mPossibleCompletions.add("cd");
        mPossibleCompletions.add("cd");
        mPossibleCompletions.add("ceil");
        mPossibleCompletions.add("cell");
        mPossibleCompletions.add("cell2mat");
        mPossibleCompletions.add("cell2struct");
        mPossibleCompletions.add("celldisp");
        mPossibleCompletions.add("cellfun");
        mPossibleCompletions.add("cellslices");
        mPossibleCompletions.add("cellstr");
        mPossibleCompletions.add("center");
        mPossibleCompletions.add("cgs");
        mPossibleCompletions.add("char");
        mPossibleCompletions.add("chdir");
        mPossibleCompletions.add("chdir");
        mPossibleCompletions.add("chi2cdf");
        mPossibleCompletions.add("chi2inv");
        mPossibleCompletions.add("chi2pdf");
        mPossibleCompletions.add("chi2rnd");
        mPossibleCompletions.add("chisquare_test_homogeneity");
        mPossibleCompletions.add("chisquare_test_independence");
        mPossibleCompletions.add("chol");
        mPossibleCompletions.add("chol2inv");
        mPossibleCompletions.add("choldelete");
        mPossibleCompletions.add("cholinsert");
        mPossibleCompletions.add("cholinv");
        mPossibleCompletions.add("cholshift");
        mPossibleCompletions.add("cholupdate");
        mPossibleCompletions.add("circshift");
        mPossibleCompletions.add("cla");
        mPossibleCompletions.add("clabel");
        mPossibleCompletions.add("class");
        mPossibleCompletions.add("clc");
        mPossibleCompletions.add("clear");
        mPossibleCompletions.add("clf");
        mPossibleCompletions.add("clock");
        mPossibleCompletions.add("cloglog");
        mPossibleCompletions.add("close");
        mPossibleCompletions.add("closereq");
        mPossibleCompletions.add("colamd");
        mPossibleCompletions.add("colloc");
        mPossibleCompletions.add("colon");
        mPossibleCompletions.add("colorbar");
        mPossibleCompletions.add("colormap");
        mPossibleCompletions.add("colperm");
        mPossibleCompletions.add("columns");
        mPossibleCompletions.add("comet");
        mPossibleCompletions.add("comet3");
        mPossibleCompletions.add("command_line_path");
        mPossibleCompletions.add("common_size");
        mPossibleCompletions.add("commutation_matrix");
        mPossibleCompletions.add("compan");
        mPossibleCompletions.add("compare_versions");
        mPossibleCompletions.add("compass");
        mPossibleCompletions.add("completion_append_char");
        mPossibleCompletions.add("completion_matches");
        mPossibleCompletions.add("complex");
        mPossibleCompletions.add("computer");
        mPossibleCompletions.add("cond");
        mPossibleCompletions.add("condest");
        mPossibleCompletions.add("confirm_recursive_rmdir");
        mPossibleCompletions.add("conj");
        mPossibleCompletions.add("contour");
        mPossibleCompletions.add("contour3");
        mPossibleCompletions.add("contourc");
        mPossibleCompletions.add("contourf");
        mPossibleCompletions.add("contrast");
        mPossibleCompletions.add("conv");
        mPossibleCompletions.add("conv2");
        mPossibleCompletions.add("convhull");
        mPossibleCompletions.add("convhulln");
        mPossibleCompletions.add("convn");
        mPossibleCompletions.add("cool");
        mPossibleCompletions.add("copper");
        mPossibleCompletions.add("copyfile");
        mPossibleCompletions.add("cor");
        mPossibleCompletions.add("cor_test");
        mPossibleCompletions.add("corrcoef");
        mPossibleCompletions.add("cos");
        mPossibleCompletions.add("cosd");
        mPossibleCompletions.add("cosh");
        mPossibleCompletions.add("cot");
        mPossibleCompletions.add("cotd");
        mPossibleCompletions.add("coth");
        mPossibleCompletions.add("cov");
        mPossibleCompletions.add("cplxpair");
        mPossibleCompletions.add("cputime");
        mPossibleCompletions.add("crash_dumps_octave_core");
        mPossibleCompletions.add("cross");
        mPossibleCompletions.add("csc");
        mPossibleCompletions.add("cscd");
        mPossibleCompletions.add("csch");
        mPossibleCompletions.add("cstrcat");
        mPossibleCompletions.add("csvread");
        mPossibleCompletions.add("csvwrite");
        mPossibleCompletions.add("csymamd");
        mPossibleCompletions.add("ctime");
        mPossibleCompletions.add("ctranspose");
        mPossibleCompletions.add("cummax");
        mPossibleCompletions.add("cummin");
        mPossibleCompletions.add("cumprod");
        mPossibleCompletions.add("cumsum");
        mPossibleCompletions.add("cumtrapz");
        mPossibleCompletions.add("curl");
        mPossibleCompletions.add("cut");
        mPossibleCompletions.add("cylinder");
        mPossibleCompletions.add("daspk");
        mPossibleCompletions.add("daspk_options");
        mPossibleCompletions.add("dasrt");
        mPossibleCompletions.add("dasrt_options");
        mPossibleCompletions.add("dassl");
        mPossibleCompletions.add("dassl_options");
        mPossibleCompletions.add("date");
        mPossibleCompletions.add("datenum");
        mPossibleCompletions.add("datestr");
        mPossibleCompletions.add("datetick");
        mPossibleCompletions.add("datevec");
        mPossibleCompletions.add("dbclear");
        mPossibleCompletions.add("dbcont");
        mPossibleCompletions.add("dbdown");
        mPossibleCompletions.add("dblquad");
        mPossibleCompletions.add("dbquit");
        mPossibleCompletions.add("dbstack");
        mPossibleCompletions.add("dbstatus");
        mPossibleCompletions.add("dbstep");
        mPossibleCompletions.add("dbstop");
        mPossibleCompletions.add("dbtype");
        mPossibleCompletions.add("dbup");
        mPossibleCompletions.add("dbwhere");
        mPossibleCompletions.add("deal");
        mPossibleCompletions.add("deblank");
        mPossibleCompletions.add("debug_on_error");
        mPossibleCompletions.add("debug_on_interrupt");
        mPossibleCompletions.add("debug_on_warning");
        mPossibleCompletions.add("dec2base");
        mPossibleCompletions.add("dec2bin");
        mPossibleCompletions.add("dec2hex");
        mPossibleCompletions.add("deconv");
        mPossibleCompletions.add("default_save_options");
        mPossibleCompletions.add("del2");
        mPossibleCompletions.add("delaunay");
        mPossibleCompletions.add("delaunay3");
        mPossibleCompletions.add("delaunayn");
        mPossibleCompletions.add("delete");
        mPossibleCompletions.add("dellistener");
        mPossibleCompletions.add("demo");
        mPossibleCompletions.add("det");
        mPossibleCompletions.add("detrend");
        mPossibleCompletions.add("diag");
        mPossibleCompletions.add("diary");
        mPossibleCompletions.add("diff");
        mPossibleCompletions.add("diffpara");
        mPossibleCompletions.add("diffuse");
        mPossibleCompletions.add("dims");
        mPossibleCompletions.add("dir");
        mPossibleCompletions.add("discrete_cdf");
        mPossibleCompletions.add("discrete_inv");
        mPossibleCompletions.add("discrete_pdf");
        mPossibleCompletions.add("discrete_rnd");
        mPossibleCompletions.add("disp");
        mPossibleCompletions.add("display");
        mPossibleCompletions.add("divergence");
        mPossibleCompletions.add("dlmread");
        mPossibleCompletions.add("dlmwrite");
        mPossibleCompletions.add("dmperm");
        mPossibleCompletions.add("do_string_escapes");
        mPossibleCompletions.add("doc");
        mPossibleCompletions.add("doc_cache_file");
        mPossibleCompletions.add("dos");
        mPossibleCompletions.add("dot");
        mPossibleCompletions.add("double");
        mPossibleCompletions.add("drawnow");
        mPossibleCompletions.add("dsearch");
        mPossibleCompletions.add("dsearchn");
        mPossibleCompletions.add("dup2");
        mPossibleCompletions.add("duplication_matrix");
        mPossibleCompletions.add("durbinlevinson");
        mPossibleCompletions.add("e");
        mPossibleCompletions.add("echo");
        mPossibleCompletions.add("echo_executing_commands");
        mPossibleCompletions.add("edit");
        mPossibleCompletions.add("edit_history");
        mPossibleCompletions.add("EDITOR");
        mPossibleCompletions.add("eig");
        mPossibleCompletions.add("eigs");
        mPossibleCompletions.add("elem");
        mPossibleCompletions.add("ellipsoid");
        mPossibleCompletions.add("empirical_cdf");
        mPossibleCompletions.add("empirical_inv");
        mPossibleCompletions.add("empirical_pdf");
        mPossibleCompletions.add("empirical_rnd");
        mPossibleCompletions.add("endgrent");
        mPossibleCompletions.add("endpwent");
        mPossibleCompletions.add("eomday");
        mPossibleCompletions.add("eps");
        mPossibleCompletions.add("eq");
        mPossibleCompletions.add("erf");
        mPossibleCompletions.add("erfc");
        mPossibleCompletions.add("erfcx");
        mPossibleCompletions.add("erfinv");
        mPossibleCompletions.add("errno");
        mPossibleCompletions.add("errno_list");
        mPossibleCompletions.add("error");
        mPossibleCompletions.add("errorbar");
        mPossibleCompletions.add("etime");
        mPossibleCompletions.add("etree");
        mPossibleCompletions.add("etreeplot");
        mPossibleCompletions.add("eval");
        mPossibleCompletions.add("evalin");
        mPossibleCompletions.add("example");
        mPossibleCompletions.add("exec");
        mPossibleCompletions.add("EXEC_PATH");
        mPossibleCompletions.add("exist");
        mPossibleCompletions.add("exit");
        mPossibleCompletions.add("exp");
        mPossibleCompletions.add("expcdf");
        mPossibleCompletions.add("expinv");
        mPossibleCompletions.add("expm");
        mPossibleCompletions.add("expm1");
        mPossibleCompletions.add("exppdf");
        mPossibleCompletions.add("exprnd");
        mPossibleCompletions.add("eye");
        mPossibleCompletions.add("ezcontour");
        mPossibleCompletions.add("ezcontourf");
        mPossibleCompletions.add("ezmesh");
        mPossibleCompletions.add("ezmeshc");
        mPossibleCompletions.add("ezplot");
        mPossibleCompletions.add("ezplot3");
        mPossibleCompletions.add("ezpolar");
        mPossibleCompletions.add("ezsurf");
        mPossibleCompletions.add("ezsurfc");
        mPossibleCompletions.add("f_test_regression");
        mPossibleCompletions.add("factor");
        mPossibleCompletions.add("factorial");
        mPossibleCompletions.add("fail");
        mPossibleCompletions.add("FALSE");
        mPossibleCompletions.add("fcdf");
        mPossibleCompletions.add("fclear");
        mPossibleCompletions.add("fclose");
        mPossibleCompletions.add("fcntl");
        mPossibleCompletions.add("fdisp");
        mPossibleCompletions.add("feather");
        mPossibleCompletions.add("feof");
        mPossibleCompletions.add("ferror");
        mPossibleCompletions.add("feval");
        mPossibleCompletions.add("fflush");
        mPossibleCompletions.add("fft");
        mPossibleCompletions.add("fft2");
        mPossibleCompletions.add("fftconv");
        mPossibleCompletions.add("fftfilt");
        mPossibleCompletions.add("fftn");
        mPossibleCompletions.add("fftshift");
        mPossibleCompletions.add("fftw");
        mPossibleCompletions.add("fgetl");
        mPossibleCompletions.add("fgets");
        mPossibleCompletions.add("fieldnames");
        mPossibleCompletions.add("figure");
        mPossibleCompletions.add("file_in_loadpath");
        mPossibleCompletions.add("file_in_path");
        mPossibleCompletions.add("fileattrib");
        mPossibleCompletions.add("filemarker");
        mPossibleCompletions.add("fileparts");
        mPossibleCompletions.add("filesep");
        mPossibleCompletions.add("fill");
        mPossibleCompletions.add("filter");
        mPossibleCompletions.add("filter2");
        mPossibleCompletions.add("find");
        mPossibleCompletions.add("find_dir_in_path");
        mPossibleCompletions.add("findall");
        mPossibleCompletions.add("findobj");
        mPossibleCompletions.add("findstr");
        mPossibleCompletions.add("finite");
        mPossibleCompletions.add("finv");
        mPossibleCompletions.add("fix");
        mPossibleCompletions.add("fixed_point_format");
        mPossibleCompletions.add("flag");
        mPossibleCompletions.add("flipdim");
        mPossibleCompletions.add("fliplr");
        mPossibleCompletions.add("flipud");
        mPossibleCompletions.add("floor");
        mPossibleCompletions.add("fmod");
        mPossibleCompletions.add("fnmatch");
        mPossibleCompletions.add("foo");
        mPossibleCompletions.add("fopen");
        mPossibleCompletions.add("fork");
        mPossibleCompletions.add("format");
        mPossibleCompletions.add("formula");
        mPossibleCompletions.add("fortran_vec");
        mPossibleCompletions.add("fpdf");
        mPossibleCompletions.add("fplot");
        mPossibleCompletions.add("fprintf");
        mPossibleCompletions.add("fputs");
        mPossibleCompletions.add("fractdiff");
        mPossibleCompletions.add("fread");
        mPossibleCompletions.add("freport");
        mPossibleCompletions.add("freqz");
        mPossibleCompletions.add("freqz_plot");
        mPossibleCompletions.add("frewind");
        mPossibleCompletions.add("frnd");
        mPossibleCompletions.add("fscanf");
        mPossibleCompletions.add("fseek");
        mPossibleCompletions.add("fskipl");
        mPossibleCompletions.add("fsolve");
        mPossibleCompletions.add("ftell");
        mPossibleCompletions.add("ftp");
        mPossibleCompletions.add("full");
        mPossibleCompletions.add("fullfile");
        mPossibleCompletions.add("func2str");
        mPossibleCompletions.add("functions");
        mPossibleCompletions.add("fwrite");
        mPossibleCompletions.add("fzero");
        mPossibleCompletions.add("gamcdf");
        mPossibleCompletions.add("gaminv");
        mPossibleCompletions.add("gamma");
        mPossibleCompletions.add("gammainc");
        mPossibleCompletions.add("gammaln");
        mPossibleCompletions.add("gampdf");
        mPossibleCompletions.add("gamrnd");
        mPossibleCompletions.add("gca");
        mPossibleCompletions.add("gcbf");
        mPossibleCompletions.add("gcbo");
        mPossibleCompletions.add("gcd");
        mPossibleCompletions.add("gcf");
        mPossibleCompletions.add("ge");
        mPossibleCompletions.add("genpath");
        mPossibleCompletions.add("genvarname");
        mPossibleCompletions.add("geocdf");
        mPossibleCompletions.add("geoinv");
        mPossibleCompletions.add("geopdf");
        mPossibleCompletions.add("geornd");
        mPossibleCompletions.add("get");
        mPossibleCompletions.add("getegid");
        mPossibleCompletions.add("getenv");
        mPossibleCompletions.add("geteuid");
        mPossibleCompletions.add("getfield");
        mPossibleCompletions.add("getgid");
        mPossibleCompletions.add("getgrent");
        mPossibleCompletions.add("getgrgid");
        mPossibleCompletions.add("getgrnam");
        mPossibleCompletions.add("getpgrp");
        mPossibleCompletions.add("getpid");
        mPossibleCompletions.add("getppid");
        mPossibleCompletions.add("getpwent");
        mPossibleCompletions.add("getpwnam");
        mPossibleCompletions.add("getpwuid");
        mPossibleCompletions.add("getrusage");
        mPossibleCompletions.add("getuid");
        mPossibleCompletions.add("ginput");
        mPossibleCompletions.add("givens");
        mPossibleCompletions.add("glob");
        mPossibleCompletions.add("glpk");
        mPossibleCompletions.add("gls");
        mPossibleCompletions.add("gmap40");
        mPossibleCompletions.add("gmtime");
        mPossibleCompletions.add("gnuplot_binary");
        mPossibleCompletions.add("gplot");
        mPossibleCompletions.add("gradient");
        mPossibleCompletions.add("graphics_toolkit");
        mPossibleCompletions.add("gray");
        mPossibleCompletions.add("gray2ind");
        mPossibleCompletions.add("grid");
        mPossibleCompletions.add("griddata");
        mPossibleCompletions.add("griddata3");
        mPossibleCompletions.add("griddatan");
        mPossibleCompletions.add("gt");
        mPossibleCompletions.add("gtext");
        mPossibleCompletions.add("gui_mode");
        mPossibleCompletions.add("gunzip");
        mPossibleCompletions.add("gzip");
        mPossibleCompletions.add("hadamard");
        mPossibleCompletions.add("hamming");
        mPossibleCompletions.add("hankel");
        mPossibleCompletions.add("hanning");
        mPossibleCompletions.add("help");
        mPossibleCompletions.add("hess");
        mPossibleCompletions.add("hex2dec");
        mPossibleCompletions.add("hex2num");
        mPossibleCompletions.add("hggroup");
        mPossibleCompletions.add("hidden");
        mPossibleCompletions.add("hilb");
        mPossibleCompletions.add("hist");
        mPossibleCompletions.add("histc");
        mPossibleCompletions.add("history");
        mPossibleCompletions.add("history_control");
        mPossibleCompletions.add("history_file");
        mPossibleCompletions.add("history_size");
        mPossibleCompletions.add("history_timestamp_format_string");
        mPossibleCompletions.add("hold");
        mPossibleCompletions.add("home");
        mPossibleCompletions.add("horzcat");
        mPossibleCompletions.add("hot");
        mPossibleCompletions.add("hotelling_test");
        mPossibleCompletions.add("hotelling_test_2");
        mPossibleCompletions.add("housh");
        mPossibleCompletions.add("hsv");
        mPossibleCompletions.add("hsv2rgb");
        mPossibleCompletions.add("hurst");
        mPossibleCompletions.add("hygecdf");
        mPossibleCompletions.add("hygeinv");
        mPossibleCompletions.add("hygepdf");
        mPossibleCompletions.add("hygernd");
        mPossibleCompletions.add("hypot");
        mPossibleCompletions.add("I");
        mPossibleCompletions.add("idivide");
        mPossibleCompletions.add("ifelse");
        mPossibleCompletions.add("ifft");
        mPossibleCompletions.add("ifft2");
        mPossibleCompletions.add("ifftn");
        mPossibleCompletions.add("ifftshift");
        mPossibleCompletions.add("ignore_function_time_stamp");
        mPossibleCompletions.add("imag");
        mPossibleCompletions.add("image");
        mPossibleCompletions.add("IMAGE_PATH");
        mPossibleCompletions.add("imagesc");
        mPossibleCompletions.add("imfinfo");
        mPossibleCompletions.add("imread");
        mPossibleCompletions.add("imshow");
        mPossibleCompletions.add("imwrite");
        mPossibleCompletions.add("ind2gray");
        mPossibleCompletions.add("ind2rgb");
        mPossibleCompletions.add("ind2sub");
        mPossibleCompletions.add("index");
        mPossibleCompletions.add("Inf");
        mPossibleCompletions.add("inferiorto");
        mPossibleCompletions.add("info");
        mPossibleCompletions.add("info_file");
        mPossibleCompletions.add("info_program");
        mPossibleCompletions.add("inline");
        mPossibleCompletions.add("inpolygon");
        mPossibleCompletions.add("input");
        mPossibleCompletions.add("inputname");
        mPossibleCompletions.add("int16");
        mPossibleCompletions.add("int2str");
        mPossibleCompletions.add("int32");
        mPossibleCompletions.add("int64");
        mPossibleCompletions.add("int8");
        mPossibleCompletions.add("interp1");
        mPossibleCompletions.add("interp1q");
        mPossibleCompletions.add("interp2");
        mPossibleCompletions.add("interp3");
        mPossibleCompletions.add("interpft");
        mPossibleCompletions.add("interpn");
        mPossibleCompletions.add("intersect");
        mPossibleCompletions.add("intmax");
        mPossibleCompletions.add("intmin");
        mPossibleCompletions.add("inv");
        mPossibleCompletions.add("invhilb");
        mPossibleCompletions.add("ipermute");
        mPossibleCompletions.add("iqr");
        mPossibleCompletions.add("is_absolute_filename");
        mPossibleCompletions.add("is_duplicate_entry");
        mPossibleCompletions.add("is_leap_year");
        mPossibleCompletions.add("is_rooted_relative_filename");
        mPossibleCompletions.add("is_valid_file_id");
        mPossibleCompletions.add("isa");
        mPossibleCompletions.add("isalnum");
        mPossibleCompletions.add("isalpha");
        mPossibleCompletions.add("isargout");
        mPossibleCompletions.add("isascii");
        mPossibleCompletions.add("isbool");
        mPossibleCompletions.add("iscell");
        mPossibleCompletions.add("iscellstr");
        mPossibleCompletions.add("ischar");
        mPossibleCompletions.add("iscntrl");
        mPossibleCompletions.add("iscomplex");
        mPossibleCompletions.add("isdebugmode");
        mPossibleCompletions.add("isdefinite");
        mPossibleCompletions.add("isdigit");
        mPossibleCompletions.add("isdir");
        mPossibleCompletions.add("isempty");
        mPossibleCompletions.add("isequal");
        mPossibleCompletions.add("isequalwithequalnans");
        mPossibleCompletions.add("isfield");
        mPossibleCompletions.add("isfigure");
        mPossibleCompletions.add("isfinite");
        mPossibleCompletions.add("isfloat");
        mPossibleCompletions.add("isglobal");
        mPossibleCompletions.add("isgraph");
        mPossibleCompletions.add("ishandle");
        mPossibleCompletions.add("ishermitian");
        mPossibleCompletions.add("ishghandle");
        mPossibleCompletions.add("ishold");
        mPossibleCompletions.add("isieee");
        mPossibleCompletions.add("isindex");
        mPossibleCompletions.add("isinf");
        mPossibleCompletions.add("isinteger");
        mPossibleCompletions.add("isletter");
        mPossibleCompletions.add("islogical");
        mPossibleCompletions.add("islower");
        mPossibleCompletions.add("ismac");
        mPossibleCompletions.add("ismatrix");
        mPossibleCompletions.add("ismember");
        mPossibleCompletions.add("ismethod");
        mPossibleCompletions.add("isna");
        mPossibleCompletions.add("isnan");
        mPossibleCompletions.add("isnull");
        mPossibleCompletions.add("isnumeric");
        mPossibleCompletions.add("isobject");
        mPossibleCompletions.add("ispc");
        mPossibleCompletions.add("isprime");
        mPossibleCompletions.add("isprint");
        mPossibleCompletions.add("isprop");
        mPossibleCompletions.add("ispunct");
        mPossibleCompletions.add("isreal");
        mPossibleCompletions.add("isscalar");
        mPossibleCompletions.add("issorted");
        mPossibleCompletions.add("isspace");
        mPossibleCompletions.add("issparse");
        mPossibleCompletions.add("issquare");
        mPossibleCompletions.add("isstrprop");
        mPossibleCompletions.add("isstruct");
        mPossibleCompletions.add("issymmetric");
        mPossibleCompletions.add("isunix");
        mPossibleCompletions.add("isupper");
        mPossibleCompletions.add("isvarname");
        mPossibleCompletions.add("isvector");
        mPossibleCompletions.add("isxdigit");
        mPossibleCompletions.add("jet");
        mPossibleCompletions.add("kbhit");
        mPossibleCompletions.add("kendall");
        mPossibleCompletions.add("keyboard");
        mPossibleCompletions.add("kill");
        mPossibleCompletions.add("kolmogorov_smirnov_cdf");
        mPossibleCompletions.add("kolmogorov_smirnov_test");
        mPossibleCompletions.add("kolmogorov_smirnov_test_2");
        mPossibleCompletions.add("kron");
        mPossibleCompletions.add("kruskal_wallis_test");
        mPossibleCompletions.add("krylov");
        mPossibleCompletions.add("kurtosis");
        mPossibleCompletions.add("laplace_cdf");
        mPossibleCompletions.add("laplace_inv");
        mPossibleCompletions.add("laplace_pdf");
        mPossibleCompletions.add("laplace_rnd");
        mPossibleCompletions.add("lasterr");
        mPossibleCompletions.add("lasterror");
        mPossibleCompletions.add("lastwarn");
        mPossibleCompletions.add("lcm");
        mPossibleCompletions.add("ldivide");
        mPossibleCompletions.add("le");
        mPossibleCompletions.add("legend");
        mPossibleCompletions.add("legendre");
        mPossibleCompletions.add("length");
        mPossibleCompletions.add("lgamma");
        mPossibleCompletions.add("license");
        mPossibleCompletions.add("lin2mu");
        mPossibleCompletions.add("line");
        mPossibleCompletions.add("link");
        mPossibleCompletions.add("linkprop");
        mPossibleCompletions.add("linspace");
        mPossibleCompletions.add("list_primes");
        mPossibleCompletions.add("load");
        mPossibleCompletions.add("loadaudio");
        mPossibleCompletions.add("loadobj");
        mPossibleCompletions.add("localtime");
        mPossibleCompletions.add("log");
        mPossibleCompletions.add("log10");
        mPossibleCompletions.add("log1p");
        mPossibleCompletions.add("log2");
        mPossibleCompletions.add("logical");
        mPossibleCompletions.add("logistic_cdf");
        mPossibleCompletions.add("logistic_inv");
        mPossibleCompletions.add("logistic_pdf");
        mPossibleCompletions.add("logistic_regression");
        mPossibleCompletions.add("logistic_rnd");
        mPossibleCompletions.add("logit");
        mPossibleCompletions.add("loglog");
        mPossibleCompletions.add("loglogerr");
        mPossibleCompletions.add("logm");
        mPossibleCompletions.add("logncdf");
        mPossibleCompletions.add("logninv");
        mPossibleCompletions.add("lognpdf");
        mPossibleCompletions.add("lognrnd");
        mPossibleCompletions.add("logspace");
        mPossibleCompletions.add("lookfor");
        mPossibleCompletions.add("lookup");
        mPossibleCompletions.add("lower");
        mPossibleCompletions.add("ls");
        mPossibleCompletions.add("ls_command");
        mPossibleCompletions.add("lsode");
        mPossibleCompletions.add("lsode_options");
        mPossibleCompletions.add("lsqnonneg");
        mPossibleCompletions.add("lstat");
        mPossibleCompletions.add("lt");
        mPossibleCompletions.add("lu");
        mPossibleCompletions.add("luinc");
        mPossibleCompletions.add("luupdate");
        mPossibleCompletions.add("magic");
        mPossibleCompletions.add("mahalanobis");
        mPossibleCompletions.add("make_absolute_filename");
        mPossibleCompletions.add("makeinfo_program");
        mPossibleCompletions.add("manova");
        mPossibleCompletions.add("mat2cell");
        mPossibleCompletions.add("mat2str");
        mPossibleCompletions.add("matlabroot");
        mPossibleCompletions.add("matrix_type");
        mPossibleCompletions.add("max");
        mPossibleCompletions.add("max_recursion_depth");
        mPossibleCompletions.add("mcnemar_test");
        mPossibleCompletions.add("md5sum");
        mPossibleCompletions.add("mean");
        mPossibleCompletions.add("meansq");
        mPossibleCompletions.add("median");
        mPossibleCompletions.add("menu");
        mPossibleCompletions.add("merge");
        mPossibleCompletions.add("mesh");
        mPossibleCompletions.add("meshc");
        mPossibleCompletions.add("meshgrid");
        mPossibleCompletions.add("meshz");
        mPossibleCompletions.add("methods");
        mPossibleCompletions.add("mex");
        mPossibleCompletions.add("mexext");
        mPossibleCompletions.add("mfilename");
        mPossibleCompletions.add("mget");
        mPossibleCompletions.add("min");
        mPossibleCompletions.add("minus");
        mPossibleCompletions.add("mislocked");
        mPossibleCompletions.add("mkdir");
        mPossibleCompletions.add("mkfifo");
        mPossibleCompletions.add("mkoctfile");
        mPossibleCompletions.add("mkpp");
        mPossibleCompletions.add("mkstemp");
        mPossibleCompletions.add("mktime");
        mPossibleCompletions.add("mldivide");
        mPossibleCompletions.add("mlock");
        mPossibleCompletions.add("mod");
        mPossibleCompletions.add("mode");
        mPossibleCompletions.add("moment");
        mPossibleCompletions.add("more");
        mPossibleCompletions.add("mouse_wheel_zoom");
        mPossibleCompletions.add("movefile");
        mPossibleCompletions.add("mpoles");
        mPossibleCompletions.add("mpower");
        mPossibleCompletions.add("mput");
        mPossibleCompletions.add("mrdivide");
        mPossibleCompletions.add("mtimes");
        mPossibleCompletions.add("mu2lin");
        mPossibleCompletions.add("munlock");
        mPossibleCompletions.add("NA");
        mPossibleCompletions.add("namelengthmax");
        mPossibleCompletions.add("NaN");
        mPossibleCompletions.add("nargchk");
        mPossibleCompletions.add("nargin");
        mPossibleCompletions.add("nargout");
        mPossibleCompletions.add("nargoutchk");
        mPossibleCompletions.add("native_float_format");
        mPossibleCompletions.add("nbincdf");
        mPossibleCompletions.add("nbininv");
        mPossibleCompletions.add("nbinpdf");
        mPossibleCompletions.add("nbinrnd");
        mPossibleCompletions.add("nchoosek");
        mPossibleCompletions.add("ndgrid");
        mPossibleCompletions.add("ndims");
        mPossibleCompletions.add("ne");
        mPossibleCompletions.add("nelem");
        mPossibleCompletions.add("newplot");
        mPossibleCompletions.add("news");
        mPossibleCompletions.add("newtroot");
        mPossibleCompletions.add("nextpow2");
        mPossibleCompletions.add("nfields");
        mPossibleCompletions.add("nnz");
        mPossibleCompletions.add("nonzeros");
        mPossibleCompletions.add("norm");
        mPossibleCompletions.add("normcdf");
        mPossibleCompletions.add("normest");
        mPossibleCompletions.add("norminv");
        mPossibleCompletions.add("normpdf");
        mPossibleCompletions.add("normrnd");
        mPossibleCompletions.add("not");
        mPossibleCompletions.add("now");
        mPossibleCompletions.add("nthroot");
        mPossibleCompletions.add("ntsc2rgb");
        mPossibleCompletions.add("null");
        mPossibleCompletions.add("num2cell");
        mPossibleCompletions.add("num2hex");
        mPossibleCompletions.add("num2str");
        mPossibleCompletions.add("numel");
        mPossibleCompletions.add("nzmax");
        mPossibleCompletions.add("ocean");
        mPossibleCompletions.add("octave_config_info");
        mPossibleCompletions.add("octave_core_file_limit");
        mPossibleCompletions.add("octave_core_file_name");
        mPossibleCompletions.add("octave_core_file_options");
        mPossibleCompletions.add("OCTAVE_HOME");
        mPossibleCompletions.add("OCTAVE_VERSION");
        mPossibleCompletions.add("ols");
        mPossibleCompletions.add("onenormest");
        mPossibleCompletions.add("ones");
        mPossibleCompletions.add("operator ()");
        mPossibleCompletions.add("optimget");
        mPossibleCompletions.add("optimset");
        mPossibleCompletions.add("or");
        mPossibleCompletions.add("orderfields");
        mPossibleCompletions.add("orient");
        mPossibleCompletions.add("orth");
        mPossibleCompletions.add("output_max_field_width");
        mPossibleCompletions.add("output_precision");
        mPossibleCompletions.add("P_tmpdir");
        mPossibleCompletions.add("pack");
        mPossibleCompletions.add("page_output_immediately");
        mPossibleCompletions.add("page_screen_output");
        mPossibleCompletions.add("PAGER");
        mPossibleCompletions.add("PAGER_FLAGS");
        mPossibleCompletions.add("pareto");
        mPossibleCompletions.add("parseparams");
        mPossibleCompletions.add("pascal");
        mPossibleCompletions.add("patch");
        mPossibleCompletions.add("path");
        mPossibleCompletions.add("pathdef");
        mPossibleCompletions.add("pathsep");
        mPossibleCompletions.add("pause");
        mPossibleCompletions.add("pcg");
        mPossibleCompletions.add("pchip");
        mPossibleCompletions.add("pclose");
        mPossibleCompletions.add("pcolor");
        mPossibleCompletions.add("pcr");
        mPossibleCompletions.add("peaks");
        mPossibleCompletions.add("periodogram");
        mPossibleCompletions.add("perl");
        mPossibleCompletions.add("perms");
        mPossibleCompletions.add("permute");
        mPossibleCompletions.add("pi");
        mPossibleCompletions.add("pie");
        mPossibleCompletions.add("pie3");
        mPossibleCompletions.add("pink");
        mPossibleCompletions.add("pinv");
        mPossibleCompletions.add("pipe");
        mPossibleCompletions.add("pkg");
        mPossibleCompletions.add("pkg");
        mPossibleCompletions.add("planerot");
        mPossibleCompletions.add("playaudio");
        mPossibleCompletions.add("plot");
        mPossibleCompletions.add("plot3");
        mPossibleCompletions.add("plotmatrix");
        mPossibleCompletions.add("plotyy");
        mPossibleCompletions.add("plus");
        mPossibleCompletions.add("poisscdf");
        mPossibleCompletions.add("poissinv");
        mPossibleCompletions.add("poisspdf");
        mPossibleCompletions.add("poissrnd");
        mPossibleCompletions.add("pol2cart");
        mPossibleCompletions.add("polar");
        mPossibleCompletions.add("poly");
        mPossibleCompletions.add("polyaffine");
        mPossibleCompletions.add("polyarea");
        mPossibleCompletions.add("polyderiv");
        mPossibleCompletions.add("polyfit");
        mPossibleCompletions.add("polygcd");
        mPossibleCompletions.add("polyint");
        mPossibleCompletions.add("polyout");
        mPossibleCompletions.add("polyreduce");
        mPossibleCompletions.add("polyval");
        mPossibleCompletions.add("polyvalm");
        mPossibleCompletions.add("popen");
        mPossibleCompletions.add("popen2");
        mPossibleCompletions.add("postpad");
        mPossibleCompletions.add("pow2");
        mPossibleCompletions.add("power");
        mPossibleCompletions.add("powerset");
        mPossibleCompletions.add("ppder");
        mPossibleCompletions.add("ppint");
        mPossibleCompletions.add("ppjumps");
        mPossibleCompletions.add("ppplot");
        mPossibleCompletions.add("ppval");
        mPossibleCompletions.add("pqpnonneg");
        mPossibleCompletions.add("prepad");
        mPossibleCompletions.add("primes");
        mPossibleCompletions.add("print");
        mPossibleCompletions.add("print_empty_dimensions");
        mPossibleCompletions.add("print_struct_array_contents");
        mPossibleCompletions.add("print_usage");
        mPossibleCompletions.add("printf");
        mPossibleCompletions.add("prism");
        mPossibleCompletions.add("probit");
        mPossibleCompletions.add("prod");
        mPossibleCompletions.add("program_invocation_name");
        mPossibleCompletions.add("program_name");
        mPossibleCompletions.add("prop_test_2");
        mPossibleCompletions.add("PS1");
        mPossibleCompletions.add("PS2");
        mPossibleCompletions.add("PS4");
        mPossibleCompletions.add("putenv");
        mPossibleCompletions.add("puts");
        mPossibleCompletions.add("pwd");
        mPossibleCompletions.add("qp");
        mPossibleCompletions.add("qqplot");
        mPossibleCompletions.add("qr");
        mPossibleCompletions.add("qrdelete");
        mPossibleCompletions.add("qrinsert");
        mPossibleCompletions.add("qrshift");
        mPossibleCompletions.add("qrupdate");
        mPossibleCompletions.add("quad");
        mPossibleCompletions.add("quad_options");
        mPossibleCompletions.add("quadcc");
        mPossibleCompletions.add("quadgk");
        mPossibleCompletions.add("quadl");
        mPossibleCompletions.add("quadv");
        mPossibleCompletions.add("quit");
        mPossibleCompletions.add("quiver");
        mPossibleCompletions.add("quiver3");
        mPossibleCompletions.add("qz");
        mPossibleCompletions.add("qzhess");
        mPossibleCompletions.add("rainbow");
        mPossibleCompletions.add("rand");
        mPossibleCompletions.add("rande");
        mPossibleCompletions.add("randg");
        mPossibleCompletions.add("randi");
        mPossibleCompletions.add("randn");
        mPossibleCompletions.add("randp");
        mPossibleCompletions.add("randperm");
        mPossibleCompletions.add("range");
        mPossibleCompletions.add("rank");
        mPossibleCompletions.add("ranks");
        mPossibleCompletions.add("rat");
        mPossibleCompletions.add("rats");
        mPossibleCompletions.add("rcond");
        mPossibleCompletions.add("rdivide");
        mPossibleCompletions.add("re_read_readline_init_file");
        mPossibleCompletions.add("read_readline_init_file");
        mPossibleCompletions.add("readdir");
        mPossibleCompletions.add("readlink");
        mPossibleCompletions.add("real");
        mPossibleCompletions.add("reallog");
        mPossibleCompletions.add("realmax");
        mPossibleCompletions.add("realmin");
        mPossibleCompletions.add("realpow");
        mPossibleCompletions.add("realsqrt");
        mPossibleCompletions.add("record");
        mPossibleCompletions.add("rectangle_lw");
        mPossibleCompletions.add("rectangle_sw");
        mPossibleCompletions.add("rectint");
        mPossibleCompletions.add("refresh");
        mPossibleCompletions.add("refreshdata");
        mPossibleCompletions.add("regexp");
        mPossibleCompletions.add("regexpi");
        mPossibleCompletions.add("regexprep");
        mPossibleCompletions.add("regexptranslate");
        mPossibleCompletions.add("rehash");
        mPossibleCompletions.add("rem");
        mPossibleCompletions.add("rename");
        mPossibleCompletions.add("repelems");
        mPossibleCompletions.add("repmat");
        mPossibleCompletions.add("reset");
        mPossibleCompletions.add("reshape");
        mPossibleCompletions.add("residue");
        mPossibleCompletions.add("resize");
        mPossibleCompletions.add("resize");
        mPossibleCompletions.add("restoredefaultpath");
        mPossibleCompletions.add("rethrow");
        mPossibleCompletions.add("return");
        mPossibleCompletions.add("rgb2hsv");
        mPossibleCompletions.add("rgb2ind");
        mPossibleCompletions.add("rgb2ntsc");
        mPossibleCompletions.add("ribbon");
        mPossibleCompletions.add("rindex");
        mPossibleCompletions.add("rmdir");
        mPossibleCompletions.add("rmfield");
        mPossibleCompletions.add("rmpath");
        mPossibleCompletions.add("roots");
        mPossibleCompletions.add("rose");
        mPossibleCompletions.add("rosser");
        mPossibleCompletions.add("rot90");
        mPossibleCompletions.add("rotdim");
        mPossibleCompletions.add("round");
        mPossibleCompletions.add("roundb");
        mPossibleCompletions.add("rows");
        mPossibleCompletions.add("rref");
        mPossibleCompletions.add("rsf2csf");
        mPossibleCompletions.add("run");
        mPossibleCompletions.add("run_count");
        mPossibleCompletions.add("run_history");
        mPossibleCompletions.add("run_test");
        mPossibleCompletions.add("rundemos");
        mPossibleCompletions.add("runtests");
        mPossibleCompletions.add("S_ISBLK");
        mPossibleCompletions.add("S_ISCHR");
        mPossibleCompletions.add("S_ISDIR");
        mPossibleCompletions.add("S_ISFIFO");
        mPossibleCompletions.add("S_ISLNK");
        mPossibleCompletions.add("S_ISREG");
        mPossibleCompletions.add("S_ISSOCK");
        mPossibleCompletions.add("save");
        mPossibleCompletions.add("save_header_format_string");
        mPossibleCompletions.add("save_precision");
        mPossibleCompletions.add("saveas");
        mPossibleCompletions.add("saveaudio");
        mPossibleCompletions.add("saveobj");
        mPossibleCompletions.add("savepath");
        mPossibleCompletions.add("saving_history");
        mPossibleCompletions.add("scanf");
        mPossibleCompletions.add("scatter");
        mPossibleCompletions.add("scatter3");
        mPossibleCompletions.add("schur");
        mPossibleCompletions.add("sec");
        mPossibleCompletions.add("secd");
        mPossibleCompletions.add("sech");
        mPossibleCompletions.add("SEEK_CUR");
        mPossibleCompletions.add("SEEK_END");
        mPossibleCompletions.add("SEEK_SET");
        mPossibleCompletions.add("semilogx");
        mPossibleCompletions.add("semilogxerr");
        mPossibleCompletions.add("semilogy");
        mPossibleCompletions.add("semilogyerr");
        mPossibleCompletions.add("set");
        mPossibleCompletions.add("setaudio");
        mPossibleCompletions.add("setdiff");
        mPossibleCompletions.add("setenv");
        mPossibleCompletions.add("setfield");
        mPossibleCompletions.add("setgrent");
        mPossibleCompletions.add("setpwent");
        mPossibleCompletions.add("setxor");
        mPossibleCompletions.add("shading");
        mPossibleCompletions.add("shell_cmd");
        mPossibleCompletions.add("shg");
        mPossibleCompletions.add("shift");
        mPossibleCompletions.add("shiftdim");
        mPossibleCompletions.add("SIG");
        mPossibleCompletions.add("sighup_dumps_octave_core");
        mPossibleCompletions.add("sign");
        mPossibleCompletions.add("sign_test");
        mPossibleCompletions.add("sigterm_dumps_octave_core");
        mPossibleCompletions.add("silent_functions");
        mPossibleCompletions.add("sin");
        mPossibleCompletions.add("sinc");
        mPossibleCompletions.add("sind");
        mPossibleCompletions.add("sinetone");
        mPossibleCompletions.add("sinewave");
        mPossibleCompletions.add("single");
        mPossibleCompletions.add("sinh");
        mPossibleCompletions.add("size");
        mPossibleCompletions.add("size_equal");
        mPossibleCompletions.add("sizemax");
        mPossibleCompletions.add("sizeof");
        mPossibleCompletions.add("skewness");
        mPossibleCompletions.add("sleep");
        mPossibleCompletions.add("slice");
        mPossibleCompletions.add("sombrero");
        mPossibleCompletions.add("sort");
        mPossibleCompletions.add("sortrows");
        mPossibleCompletions.add("source");
        mPossibleCompletions.add("spalloc");
        mPossibleCompletions.add("sparse");
        mPossibleCompletions.add("sparse_auto_mutate");
        mPossibleCompletions.add("spaugment");
        mPossibleCompletions.add("spconvert");
        mPossibleCompletions.add("spdiags");
        mPossibleCompletions.add("spearman");
        mPossibleCompletions.add("spectral_adf");
        mPossibleCompletions.add("spectral_xdf");
        mPossibleCompletions.add("specular");
        mPossibleCompletions.add("speed");
        mPossibleCompletions.add("spencer");
        mPossibleCompletions.add("speye");
        mPossibleCompletions.add("spfun");
        mPossibleCompletions.add("sph2cart");
        mPossibleCompletions.add("sphere");
        mPossibleCompletions.add("spinmap");
        mPossibleCompletions.add("spline");
        mPossibleCompletions.add("split_long_rows");
        mPossibleCompletions.add("spones");
        mPossibleCompletions.add("spparms");
        mPossibleCompletions.add("sprand");
        mPossibleCompletions.add("sprandn");
        mPossibleCompletions.add("sprandsym");
        mPossibleCompletions.add("sprank");
        mPossibleCompletions.add("spring");
        mPossibleCompletions.add("sprintf");
        mPossibleCompletions.add("spstats");
        mPossibleCompletions.add("spy");
        mPossibleCompletions.add("sqp");
        mPossibleCompletions.add("sqrt");
        mPossibleCompletions.add("sqrtm");
        mPossibleCompletions.add("squeeze");
        mPossibleCompletions.add("sscanf");
        mPossibleCompletions.add("stairs");
        mPossibleCompletions.add("stat");
        mPossibleCompletions.add("statistics");
        mPossibleCompletions.add("std");
        mPossibleCompletions.add("stderr");
        mPossibleCompletions.add("stdin");
        mPossibleCompletions.add("stdnormal_cdf");
        mPossibleCompletions.add("stdnormal_inv");
        mPossibleCompletions.add("stdnormal_pdf");
        mPossibleCompletions.add("stdnormal_rnd");
        mPossibleCompletions.add("stdout");
        mPossibleCompletions.add("stem");
        mPossibleCompletions.add("stem3");
        mPossibleCompletions.add("stft");
        mPossibleCompletions.add("str2double");
        mPossibleCompletions.add("str2func");
        mPossibleCompletions.add("str2num");
        mPossibleCompletions.add("strcat");
        mPossibleCompletions.add("strchr");
        mPossibleCompletions.add("strcmp");
        mPossibleCompletions.add("strcmpi");
        mPossibleCompletions.add("strfind");
        mPossibleCompletions.add("strftime");
        mPossibleCompletions.add("string_fill_char");
        mPossibleCompletions.add("strjust");
        mPossibleCompletions.add("strmatch");
        mPossibleCompletions.add("strncmp");
        mPossibleCompletions.add("strncmpi");
        mPossibleCompletions.add("strptime");
        mPossibleCompletions.add("strread");
        mPossibleCompletions.add("strrep");
        mPossibleCompletions.add("strsplit");
        mPossibleCompletions.add("strtok");
        mPossibleCompletions.add("strtrim");
        mPossibleCompletions.add("strtrunc");
        mPossibleCompletions.add("struct");
        mPossibleCompletions.add("struct2cell");
        mPossibleCompletions.add("struct_levels_to_print");
        mPossibleCompletions.add("structfun");
        mPossibleCompletions.add("strvcat");
        mPossibleCompletions.add("studentize");
        mPossibleCompletions.add("sub2ind");
        mPossibleCompletions.add("subplot");
        mPossibleCompletions.add("subsasgn");
        mPossibleCompletions.add("subsindex");
        mPossibleCompletions.add("subspace");
        mPossibleCompletions.add("subsref");
        mPossibleCompletions.add("substr");
        mPossibleCompletions.add("substruct");
        mPossibleCompletions.add("sum");
        mPossibleCompletions.add("summer");
        mPossibleCompletions.add("sumsq");
        mPossibleCompletions.add("superiorto");
        mPossibleCompletions.add("suppress_verbose_help_message");
        mPossibleCompletions.add("surf");
        mPossibleCompletions.add("surface");
        mPossibleCompletions.add("surfc");
        mPossibleCompletions.add("surfl");
        mPossibleCompletions.add("surfnorm");
        mPossibleCompletions.add("svd");
        mPossibleCompletions.add("svd_driver");
        mPossibleCompletions.add("svds");
        mPossibleCompletions.add("swapbytes");
        mPossibleCompletions.add("syl");
        mPossibleCompletions.add("sylvester_matrix");
        mPossibleCompletions.add("symamd");
        mPossibleCompletions.add("symbfact");
        mPossibleCompletions.add("symlink");
        mPossibleCompletions.add("symrcm");
        mPossibleCompletions.add("symvar");
        mPossibleCompletions.add("synthesis");
        mPossibleCompletions.add("system");
        mPossibleCompletions.add("t_test");
        mPossibleCompletions.add("t_test_2");
        mPossibleCompletions.add("t_test_regression");
        mPossibleCompletions.add("table");
        mPossibleCompletions.add("tan");
        mPossibleCompletions.add("tand");
        mPossibleCompletions.add("tanh");
        mPossibleCompletions.add("tar");
        mPossibleCompletions.add("tcdf");
        mPossibleCompletions.add("tempdir");
        mPossibleCompletions.add("tempname");
        mPossibleCompletions.add("test");
        mPossibleCompletions.add("text");
        mPossibleCompletions.add("textread");
        mPossibleCompletions.add("textscan");
        mPossibleCompletions.add("tic");
        mPossibleCompletions.add("tilde_expand");
        mPossibleCompletions.add("time");
        mPossibleCompletions.add("times");
        mPossibleCompletions.add("tinv");
        mPossibleCompletions.add("title");
        mPossibleCompletions.add("tmpfile");
        mPossibleCompletions.add("tmpnam");
        mPossibleCompletions.add("toascii");
        mPossibleCompletions.add("toc");
        mPossibleCompletions.add("toeplitz");
        mPossibleCompletions.add("tolower");
        mPossibleCompletions.add("toupper");
        mPossibleCompletions.add("tpdf");
        mPossibleCompletions.add("trace");
        mPossibleCompletions.add("transpose");
        mPossibleCompletions.add("trapz");
        mPossibleCompletions.add("treelayout");
        mPossibleCompletions.add("treeplot");
        mPossibleCompletions.add("triangle_lw");
        mPossibleCompletions.add("triangle_sw");
        mPossibleCompletions.add("tril");
        mPossibleCompletions.add("trimesh");
        mPossibleCompletions.add("triplequad");
        mPossibleCompletions.add("triplot");
        mPossibleCompletions.add("trisurf");
        mPossibleCompletions.add("triu");
        mPossibleCompletions.add("trnd");
        mPossibleCompletions.add("TRUE");
        mPossibleCompletions.add("tsearch");
        mPossibleCompletions.add("tsearchn");
        mPossibleCompletions.add("type");
        mPossibleCompletions.add("typecast");
        mPossibleCompletions.add("typeinfo");
        mPossibleCompletions.add("u_test");
        mPossibleCompletions.add("uint16");
        mPossibleCompletions.add("uint32");
        mPossibleCompletions.add("uint64");
        mPossibleCompletions.add("uint8");
        mPossibleCompletions.add("umask");
        mPossibleCompletions.add("uminus");
        mPossibleCompletions.add("uname");
        mPossibleCompletions.add("undo_string_escapes");
        mPossibleCompletions.add("unidcdf");
        mPossibleCompletions.add("unidinv");
        mPossibleCompletions.add("unidpdf");
        mPossibleCompletions.add("unidrnd");
        mPossibleCompletions.add("unifcdf");
        mPossibleCompletions.add("unifinv");
        mPossibleCompletions.add("unifpdf");
        mPossibleCompletions.add("unifrnd");
        mPossibleCompletions.add("union");
        mPossibleCompletions.add("unique");
        mPossibleCompletions.add("unix");
        mPossibleCompletions.add("unlink");
        mPossibleCompletions.add("unmkpp");
        mPossibleCompletions.add("unpack");
        mPossibleCompletions.add("untabify");
        mPossibleCompletions.add("untar");
        mPossibleCompletions.add("unwrap");
        mPossibleCompletions.add("unzip");
        mPossibleCompletions.add("uplus");
        mPossibleCompletions.add("upper");
        mPossibleCompletions.add("urlread");
        mPossibleCompletions.add("urlwrite");
        mPossibleCompletions.add("usage");
        mPossibleCompletions.add("usleep");
        mPossibleCompletions.add("v");
        mPossibleCompletions.add("validatestring");
        mPossibleCompletions.add("vander");
        mPossibleCompletions.add("var");
        mPossibleCompletions.add("var_test");
        mPossibleCompletions.add("vech");
        mPossibleCompletions.add("vectorize");
        mPossibleCompletions.add("ver");
        mPossibleCompletions.add("version");
        mPossibleCompletions.add("vertcat");
        mPossibleCompletions.add("view");
        mPossibleCompletions.add("voronoi");
        mPossibleCompletions.add("voronoin");
        mPossibleCompletions.add("waitforbuttonpress");
        mPossibleCompletions.add("waitpid");
        mPossibleCompletions.add("warning");
        mPossibleCompletions.add("warranty");
        mPossibleCompletions.add("wavread");
        mPossibleCompletions.add("wavwrite");
        mPossibleCompletions.add("wblcdf");
        mPossibleCompletions.add("wblinv");
        mPossibleCompletions.add("wblpdf");
        mPossibleCompletions.add("wblrnd");
        mPossibleCompletions.add("WCONTINUE");
        mPossibleCompletions.add("WCOREDUMP");
        mPossibleCompletions.add("weekday");
        mPossibleCompletions.add("welch_test");
        mPossibleCompletions.add("WEXITSTATUS");
        mPossibleCompletions.add("what");
        mPossibleCompletions.add("which");
        mPossibleCompletions.add("white");
        mPossibleCompletions.add("whitebg");
        mPossibleCompletions.add("who");
        mPossibleCompletions.add("whos");
        mPossibleCompletions.add("whos_line_format");
        mPossibleCompletions.add("wienrnd");
        mPossibleCompletions.add("WIFCONTINUED");
        mPossibleCompletions.add("WIFEXITED");
        mPossibleCompletions.add("WIFSIGNALED");
        mPossibleCompletions.add("WIFSTOPPED");
        mPossibleCompletions.add("wilcoxon_test");
        mPossibleCompletions.add("wilkinson");
        mPossibleCompletions.add("winter");
        mPossibleCompletions.add("WNOHANG");
        mPossibleCompletions.add("WSTOPSIG");
        mPossibleCompletions.add("WTERMSIG");
        mPossibleCompletions.add("WUNTRACED");
        mPossibleCompletions.add("xlabel");
        mPossibleCompletions.add("xlim");
        mPossibleCompletions.add("xor");
        mPossibleCompletions.add("yes_or_no");
        mPossibleCompletions.add("ylabel");
        mPossibleCompletions.add("yulewalker");
        mPossibleCompletions.add("z_test");
        mPossibleCompletions.add("z_test_2");
        mPossibleCompletions.add("zeros");
        mPossibleCompletions.add("zip");
        mPossibleCompletions.add("zlabel");
        mPossibleCompletions.add("deg2rad");
        mPossibleCompletions.add("rad2deg");
        mPossibleCompletions.add("azimuth");
        mPossibleCompletions.add("distance");
        mPossibleCompletions.add("km2deg");
        mPossibleCompletions.add("reckon");
        
        mPossibleCompletionsRsrvd.add("if");
        mPossibleCompletionsRsrvd.add("while");
        mPossibleCompletionsRsrvd.add("for");
        mPossibleCompletionsRsrvd.add("switch");
        mPossibleCompletionsRsrvd.add("global");
        mPossibleCompletionsRsrvd.add("break");
        mPossibleCompletionsRsrvd.add("continue");
        mPossibleCompletionsRsrvd.add("catch");
        mPossibleCompletionsRsrvd.add("persistent");
        mPossibleCompletionsRsrvd.add("return");
        mPossibleCompletionsRsrvd.add("try"); 
        mPossibleCompletionsRsrvd.add("elseif");
        mPossibleCompletionsRsrvd.add("else");
        mPossibleCompletionsRsrvd.add("endif");
        mPossibleCompletionsRsrvd.add("end");
        mPossibleCompletionsRsrvd.add("endfunction");
        mPossibleCompletionsRsrvd.add("case");
        mPossibleCompletionsRsrvd.add("default");
        mPossibleCompletionsRsrvd.add("otherwise");
        mPossibleCompletionsRsrvd.add("endswitch");
        mPossibleCompletionsRsrvd.add("endwhile");
        mPossibleCompletionsRsrvd.add("endfor");
        
        Collections.sort(mPossibleCompletions, new MyComparator());
        Collections.sort(mPossibleCompletionsRsrvd, new MyComparator());

    }

    public CandidateView(Context context)
    {
    	super(context);
    	init(context);
    }
    /**
     * Construct a CandidateView for showing suggested words for completion.
     * @param context
     * @param attrs
     */
    public CandidateView(Context context, AttributeSet atts)
    {
        super(context,atts);
        init(context);
    }
    
    @Override
    public int computeHorizontalScrollRange() {
        return mTotalWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(50, widthMeasureSpec);
        
        // Get the desired height of the icon menu view (last row of items does
        // not have a divider below)
        Rect padding = new Rect();
        mSelectionHighlight.getPadding(padding);
        final int desiredHeight = ((int)mPaint.getTextSize()) + mVerticalPadding
                + padding.top + padding.bottom;
        
        // Maximum possible width and desired height
        setMeasuredDimension(measuredWidth,
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    /**
     * If the canvas is null, then only touch calculations are performed to pick the target
     * candidate.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
        }
        mTotalWidth = 0;
        if (mSuggestions == null) return;
        
        if (mBgPadding == null) {
            mBgPadding = new Rect(0, 0, 0, 0);
            if (getBackground() != null) {
                getBackground().getPadding(mBgPadding);
            }
        }
        int x = 0;
        final int count = mSuggestions.size(); 
        final int height = getHeight();
        final Rect bgPadding = mBgPadding;
        final Paint paint = mPaint;
        final int touchX = mTouchX;
        final int scrollX = getScrollX();
        final boolean scrolled = mScrolled;
        final boolean typedWordValid = mTypedWordValid;
        final int y = (int) (((height - mPaint.getTextSize()) / 2) - mPaint.ascent());

        for (int i = 0; i < count; i++) {
            String suggestion = mSuggestions.get(i);
            float textWidth = paint.measureText(suggestion);
            final int wordWidth = (int) textWidth + X_GAP * 2;

            mWordX[i] = x;
            mWordWidth[i] = wordWidth;
            paint.setColor(mColorNormal);
            if (touchX + scrollX >= x && touchX + scrollX < x + wordWidth && !scrolled) {
                if (canvas != null) {
                    canvas.translate(x, 0);
                    mSelectionHighlight.setBounds(0, bgPadding.top, wordWidth, height);
                    mSelectionHighlight.draw(canvas);
                    canvas.translate(-x, 0);
                }
                mSelectedIndex = i;
            }

            if (canvas != null) {
                if ((i == 1 && !typedWordValid) || (i == 0 && typedWordValid)) {
                    paint.setFakeBoldText(true);
                    paint.setColor(mColorRecommended);
                } else if (i != 0) {
                    paint.setColor(mColorOther);
                }
                canvas.drawText(suggestion, x + X_GAP, y, paint);
                paint.setColor(mColorOther); 
                canvas.drawLine(x + wordWidth + 0.5f, bgPadding.top, 
                        x + wordWidth + 0.5f, height + 1, paint);
                paint.setFakeBoldText(false);
            }
            x += wordWidth;
        }
        mTotalWidth = x;
        if (mTargetScrollX != getScrollX()) {
            scrollToTarget();
        }
    }
    
    private void scrollToTarget() {
        int sx = getScrollX();
        if (mTargetScrollX > sx) {
            sx += SCROLL_PIXELS;
            if (sx >= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        } else {
            sx -= SCROLL_PIXELS;
            if (sx <= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        }
        scrollTo(sx, getScrollY());
        invalidate();
    }
    
    public void updateSuggestions(String partialText, boolean completions, boolean typedWordValid) {
    	Iterator completionIterator;
    	String tempString;
        clear();
        if (partialText.length() > 0) {
        	completionIterator = mPossibleCompletionsRsrvd.iterator();
        	while (completionIterator.hasNext()) {
        		tempString = (String)completionIterator.next();
        		if (tempString.startsWith(partialText)) {
        			mSuggestions.add(tempString);
        			setVisibility(View.VISIBLE);
        		}
        	}
        	completionIterator = mPossibleCompletions.iterator();
        	while (completionIterator.hasNext()) {
        		tempString = (String)completionIterator.next();
        		if (tempString.startsWith(partialText)) {
        			mSuggestions.add(tempString);
        			mSuggestions.add(tempString+"()");
        			setVisibility(View.VISIBLE);
        		}
        	}
        }
        mTypedWordValid = typedWordValid;
        scrollTo(0, 0);
        mTargetScrollX = 0;
        // Compute the total width
        onDraw(null);
        invalidate();
        requestLayout();
    }

    public void clear() {
        mSuggestions = new ArrayList<String>();
        mTouchX = OUT_OF_BOUNDS;
        mSelectedIndex = -1;
        setVisibility(View.GONE);
        invalidate();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me) {

        if (mGestureDetector.onTouchEvent(me)) {
            return true;
        }

        int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        mTouchX = x;

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mScrolled = false;
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
            if (y <= 0) {
                // Fling up!?
                if (mSelectedIndex >= 0) {
                	mService.sendSuggestionText(mSuggestions.get(mSelectedIndex));
                    mSelectedIndex = -1;
                }
            }
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            if (!mScrolled) {
                if (mSelectedIndex >= 0) {
                	mService.sendSuggestionText(mSuggestions.get(mSelectedIndex));
                }
            }
            mSelectedIndex = -1;
            removeHighlight();
            requestLayout();
            break;
        }
        return true;
    }
    
    /**
     * For flick through from keyboard, call this method with the x coordinate of the flick 
     * gesture.
     * @param x
     */
    public void takeSuggestionAt(float x) {
        mTouchX = (int) x;
        // To detect candidate
        onDraw(null);
        if (mSelectedIndex >= 0) {
        	mService.sendSuggestionText(mSuggestions.get(mSelectedIndex));
        }
        invalidate();
    }

    private void removeHighlight() {
        mTouchX = OUT_OF_BOUNDS;
        invalidate();
    }
    
    public class MyComparator implements Comparator<String>{
        @Override
        public int compare(String o1, String o2) {  
          if (o1.length() > o2.length()) {
             return 1;
          } else if (o1.length() < o2.length()) {
             return -1;
          } else { 
             return o1.compareTo(o2);
          }
        }
    }

}

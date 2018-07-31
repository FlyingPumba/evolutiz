## -*- texinfo -*-
## @deftypefn {Function File} {} norm (@var{a}, @var{p})
## Compute the p-norm of the matrix @var{a}.  If the second argument is
## missing, @code{p = 2} is assumed.
##
## If @var{a} is a matrix:
##
## @table @asis
## @item @var{p} = @code{1}
## 1-norm, the largest column sum of the absolute values of @var{a}.
##
## @item @var{p} = @code{2}
## Largest singular value of @var{a}.
##
## @item @var{p} = @code{Inf}
## @cindex infinity norm
## Infinity norm, the largest row sum of the absolute values of @var{a}.
##
## @item @var{p} = @code{"fro"}
## @cindex Frobenius norm
## Frobenius norm of @var{a}, @code{sqrt (sum (diag (@var{a}' * @var{a})))}.
## @end table
##
## If @var{a} is a vector or a scalar:
##
## @table @asis
## @item @var{p} = @code{Inf}
## @code{max (abs (@var{a}))}.
##
## @item @var{p} = @code{-Inf}
## @code{min (abs (@var{a}))}.
##
## @item other
## p-norm of @var{a}, @code{(sum (abs (@var{a}) .^ @var{p})) ^ (1/@var{p})}.
## @end table
## @seealso{cond, svd}
## @end deftypefn

## Author: jwe

function retval = norm (x, p)

  if (nargin < 1 || nargin > 2)
    print_usage ();
  endif

  if (isempty (x))
    retval = [];
    return;
  endif

  if (ndims (x) > 2)
    error ("norm: Only valid on 2-D objects")
  endif

  ## Do we have a vector or matrix as the first argument?

  if (rows (x) == 1 || columns (x) == 1)

    if (nargin == 2)
      if (ischar (p))
        if (strcmp (p, "fro"))
	  retval = sqrt (sum (abs (x) .^ 2));
        elseif (strcmp (p, "inf"))
          retval = max (abs (x));
        else
          error ("norm: unrecognized norm");
        endif
      else
        if (p == Inf)
          retval = max (abs (x));
        elseif (p == -Inf)
          retval = min (abs (x));
        else
          retval = sum (abs (x) .^ p) ^ (1/p);
        endif
      endif
    elseif (nargin == 1)
      retval = sqrt (sum (abs (x) .^ 2));
    endif

  else

    if (nargin == 2)
      if (ischar (p))
        if (strcmp (p, "fro"))
	  retval = sqrt (sum (sum (abs (x) .^ 2)));
        elseif (strcmp (p, "inf"))
          retval = max (sum (abs (x')));
        else
          error ("norm: unrecognized norm");
        endif
      else
        if (p == 1)
          retval = max (sum (abs (x)));
        elseif (p == 2)
          s = svd (x);
          retval = s (1);
        elseif (p == Inf)
          retval = max (sum (abs (x')));
        endif
      endif
    elseif (nargin == 1)
      s = svd (x);
      retval = s (1);
    endif

  endif

endfunction

/*
@GROUP
LinearAlgebra
@SYNTAX
norm()
@DOC
.
@EXAMPLES
<programlisting>
</programlisting>
@NOTES
@SEE
*/

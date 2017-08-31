## -*- texinfo -*-
## @deftypefn {Function File} {} dmult (@var{a}, @var{b})
## If @var{a} is a vector of length @code{rows (@var{b})}, return
## @code{diag (@var{a}) * @var{b}} (but computed much more efficiently).
## @end deftypefn

## Author: KH <Kurt.Hornik@wu-wien.ac.at>
## Description: Rescale the rows of a matrix

function M = dmult (a, B)

  if (nargin != 2)
    print_usage ();
  endif
 if (! isvector (a))
    error ("dmult: a must be a vector of length rows (B)");
  endif
  a = a(:);
  sb = size (B);
  sb(1) = 1;
  M = repmat (a(:), sb) .* B;
endfunction

/*
@GROUP
LinearAlgebra
@SYNTAX
dmult
@DOC
.
@EXAMPLES
<programlisting>
</programlisting>
@NOTES
@SEE
*/

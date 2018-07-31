## -*- texinfo -*-
## @deftypefn {Function File} {} polyinteg (@var{c})
## Return the coefficients of the integral of the polynomial whose
## coefficients are represented by the vector @var{c}.
##
## The constant of integration is set to zero.
## @seealso{poly, polyderiv, polyreduce, roots, conv, deconv, residue,
## filter, polyval, and polyvalm}
## @end deftypefn

## Author: Tony Richardson <arichard@stark.cc.oh.us>
## Created: June 1994
## Adapted-By: jwe

function p = polyinteg (p)

  if(nargin != 1)
    print_usage ();
  endif

  if (! (isvector (p) || isempty (p)))
    error ("argument must be a vector");
  endif

  lp = length (p);

  if (lp == 0)
    p = [];
    return;
  end

  if (rows (p) > 1)
    ## Convert to column vector
    p = p.';
  endif

  p = [ p, 0 ] ./ [ lp:-1:1, 1 ];

endfunction

/*
@GROUP
polynomial
@SYNTAX
y = polyinteg (x)
@DOC
.
@NOTES
@EXAMPLES
@SEE
poly, polyreduce, polyval, roots, unmkpp
*/

## -*- texinfo -*-
## @deftypefn {Function File} {} create_set (@var{x})
## Return a row vector containing the unique values in @var{x}, sorted in
## ascending order.  For example,
##
## @example
## @group
## create_set ([ 1, 2; 3, 4; 4, 2 ])
##      @result{} [ 1, 2, 3, 4 ]
## @end group
## @end example
## @seealso{union, intersection, complement}
## @end deftypefn

## Author: jwe

function y = create_set(x)

  if (nargin != 1)
    print_usage ();
  endif

  if (isempty(x))
    y = [];
  else
    nel = numel (x);
    y = sort (reshape (x, 1, nel));
    els = find (y(1:nel-1) != y(2:nel));
    if (isempty (els));
      y = y(1);
    else
      y = y([1, els+1]);
    endif
  endif

endfunction

/*
@GROUP
set
@SYNTAX
x=create_set(y)
@DOC
.
@NOTES
@EXAMPLES
@SEE
*/

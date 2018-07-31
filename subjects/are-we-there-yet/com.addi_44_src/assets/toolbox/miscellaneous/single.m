## -*- texinfo -*-
## @deftypefn {Function File} {} single (@var{val})
## Convert the numeric value @var{val} to single precision.
##
## @strong{Note}: this function currently returns its argument converted
## to double precision because Octave does not yet have a single-precision
## numeric data type.
## @end deftypefn

function retval = single (val)

  if (nargin == 1 && isnumeric (val))
    retval = double(val);
  else
    print_usage ();
  endif

endfunction

/*
@GROUP
miscellaneous
@SYNTAX
single(x)
@DOC
.
@EXAMPLES
<programlisting>
</programlisting>
@NOTES
@SEE
*/

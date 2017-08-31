function retval = ismatrix(x)

  if (nargin == 1)
    retval = isnumeric(x);
  else
    usage ("ismatrix(x)");
  endif

endfunction


/*
@GROUP
general
@SYNTAX
ismatrix(values)
@DOC
.
@EXAMPLES
<programlisting>
ismatrix([88]) 
ismatrix("hello")
</programlisting>
@NOTES
.
@SEE
ischar, iscell, isnumeric, isprime
*/

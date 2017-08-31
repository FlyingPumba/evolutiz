function retval = isstr(x)

  if (nargin == 1)
    retval = ischar(x);
  else
    usage ("isstr(x) deprecated use ischar(x) instead");
  endif

endfunction



/*
@GROUP
deprecated
@SYNTAX
isstr(s)
@DOC
check if argument is a string.
@EXAMPLES
<programlisting>
</programlisting>
@NOTES
This function is deprecated.
@SEE
ischar
*/
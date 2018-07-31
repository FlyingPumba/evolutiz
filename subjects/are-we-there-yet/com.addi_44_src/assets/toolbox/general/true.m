function y = true(varargin)

y = logical( ones(varargin{:}) )

endfunction

/*
@GROUP
general
@SYNTAX
true(x)
@DOC
.
@EXAMPLES
<programlisting>
true(2)  -> [true, true; true true]
</programlisting>
@NOTES
.
@SEE
false, islogical, logical
*/


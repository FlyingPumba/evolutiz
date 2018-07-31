function y = false(varargin)

y = logical( zeros(varargin{:}) )

endfunction

/*
@GROUP
general
@SYNTAX
false(x)
@DOC
.
@EXAMPLES
<programlisting>
false(2)  -> [false, false; false, false]
</programlisting>
@NOTES
.
@SEE
true, islogical, logical
*/


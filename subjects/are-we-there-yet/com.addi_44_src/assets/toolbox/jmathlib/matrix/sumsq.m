function z=sumsq(x,dim)

if(nargin==2)
    z=sum(x.*conj(x),dim);
else
    z=sum(x.*conj(x));
end    

endfunction

/*
@GROUP
Matrix
@SYNTAX
sumsq(x,dim)
@DOC
.
@EXAMPLES
<programlisting>
</programlisting>
@NOTES
@SEE

*/
function x=col(A,n)

x=A(:,n);

endfunction

/*
@GROUP
matrix
@SYNTAX
col(matrix, n)
@DOC
returns the specified column from a matrix.
@EXAMPLES
<programlisting>
col([1,2;3,4], 1) = [1;3]
col([1,2;3,4], 2) = [2;4]
</programlisting>
@NOTES
.
@SEE
columns, rows, row
*/
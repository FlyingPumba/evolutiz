function x=row(A,n)

x=A(n,:);

endfunction


/*
@GROUP
matrix
@SYNTAX
row(matrix, n)
@DOC
Returns the specified row of a matrix.
@EXAMPLES
<programlisting>
row([1,2;3,4],1) = [1,2]
row([1,2;3,4],2) = [3,4]
</programlisting>
@SEE
col, rows, columns
*/


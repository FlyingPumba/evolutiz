## A = lauchli (n [,mu])
##    Creates the matrix [ ones(1,n); mu*eye(n) ]
##    The value mu defaults to sqrt(eps)
##    This is an ill-conditioned system for testing the
##    accuracy of the QR routine.
##    E.g., 
##       A = lauchli(15);
##       [Q, R] = qr(A);
##       norm(Q*R - A)
##       norm(Q'*Q - eye(rows(Q)))

## This program is in the public domain
## Author: Paul Kienzle <pkienzle@users.sf.net>

function A = lauchli(n,mu)
  if (nargin < 1 || nargin > 2)
    usage("A = lauchli(n [, mu])");
  endif

  if (nargin < 2), mu = sqrt(eps); endif

  A = [ ones(1,n); mu*eye(n) ];

endfunction

/*
@GROUP
specialmatrix
@SYNTAX
lauchli(x)
lauchli(x,mu)
@DOC
Creates the matrix [ ones(1,n); mu*eye(n) ]
@EXAMPLES
@NOTES
@SEE

*/

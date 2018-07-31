## Copyright (C) 1998 Auburn University.  All rights reserved.
##
## This file is part of Octave.
##
## Octave is free software; you can redistribute it and/or modify it
## under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 2, or (at your option)
## any later version.
##
## Octave is distributed in the hope that it will be useful, but
## WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
## General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with Octave; see the file COPYING.  If not, write to the Free
## Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
## 02110-1301, USA.

## -*- texinfo -*-
## @deftypefn {Function File} {} qtransvmat (@var{qib})
## Construct a 3x3 transformation matrix from quaternion @var{qib} that
## is equivalent to rotation of th radians about axis @var{vv}, where
## @code{[@var{vv}, @var{th}] = quaternion (@var{qib})}.
## @end deftypefn

## Author: A. S. Hodel <a.s.hodel@eng.auburn.edu>
## Adapted-By: jwe

function Aib = qtransvmat (qib)

  if (! isvector(qib) || length (qib) != 4)
    error ("qtransvmat: q(%d,%d) must be a quaternion", rows (qib), columns (qib));
  elseif (max (abs (imag (qib))) != 0)
    error ("qtransvmat: input values must be real");
  endif


  Aib = [(2.*(qib(1)^2 + qib(4)^2) -1), ...
	 (2.*(qib(1)*qib(2)-qib(3)*qib(4))), ...
	 (2.*(qib(1)*qib(3)+qib(2)*qib(4)));
	 (2.*(qib(1)*qib(2)+qib(3)*qib(4))), ...
	 (2.*(qib(2)*qib(2)+qib(4)*qib(4))-1), ...
	 (2.*(qib(2)*qib(3)-qib(1)*qib(4)));
	 (2.*(qib(1)*qib(3)-qib(2)*qib(4))), ...
	 (2.*(qib(2)*qib(3)+qib(1)*qib(4))), ...
	 (2.*(qib(3)*qib(3)+qib(4)*qib(4))-1)];

endfunction

/*
@GROUP
quarternion
@SYNTAX
qtransvmat([a,b,c,d])
@DOC
.
@EXAMPLES
<programlisting>
</programlisting>
@NOTES
@SEE

*/

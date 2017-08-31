## Copyright (C) 1996, 1998 Auburn University.  All rights reserved.
##
## This file is part of Octave.
##
## Octave is free software; you can redistribute it and/or modify it
## under the terms of the GNU General Public License as published by the
## Free Software Foundation; either version 2, or (at your option) any
## later version.
##
## Octave is distributed in the hope that it will be useful, but WITHOUT
## ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
## FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
## for more details.
##
## You should have received a copy of the GNU General Public License
## along with Octave; see the file COPYING.  If not, write to the Free
## Software Foundation, 59 Temple Place, Suite 330, Boston, MA 02111 USA.

## -*- texinfo -*-
## @deftypefn {Function File} {} sysgettsam (@var{sys})
## Return the sampling time of the system @var{sys}.
## @end deftypefn

function T = sysgettsam (sys)

  if (! isstruct (sys))
    usage ("T = sysgettsam (sys)");
  endif

  T = sys.tsam;

endfunction

/*
@GROUP
control
@SYNTAX
sysgettsam()
@DOC

@EXAMPLES
<programlisting>
sysgettsam()
</programlisting>
@NOTES
@SEE

*/

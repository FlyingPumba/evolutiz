## Copyright (C) 1998 Auburn University.  All rights reserved.
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
## @deftypefn {Function File} {} sysgettype (@var{sys})
## return the initial system type of the system
##
## @strong{Input}
## @table @var
## @item sys
## System data structure.
## @end table
##
## @strong{Output}
## @table @var
## @item systype
## String indicating how the structure was initially
## constructed. Values: @code{"ss"}, @code{"zp"}, or @code{"tf"}.
## @end table
##
## @acronym{FIR} initialized systems return @code{systype="tf"}.
## @end deftypefn

function systype = sysgettype (sys)

  if (! isstruct (sys))
    error ("sysgettype: input sys is not a structure");
  endif

  typestr = {"tf", "zp", "ss"};
  systype = typestr{ sys.sys(1) + 1};

endfunction

/*
@GROUP
control
@SYNTAX
sysgettype()
@DOC

@EXAMPLES
<programlisting>
sysgettype()
</programlisting>
@NOTES
@SEE

*/

## Copyright (C) 2008 Julian Schnidder
##
## This file is part of Octave.
##
## Octave is free software; you can redistribute it and/or modify it
## under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 3 of the License, or (at
## your option) any later version.
##
## Octave is distributed in the hope that it will be useful, but
## WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
## General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with Octave; see the file COPYING.  If not, see
## <http://www.gnu.org/licenses/>.

## -*- texinfo -*-
## @deftypefn {Function File} {} info ()
## Display contact information for the GNU Octave community.
## @end deftypefn

function info ()

  printf ("\n\
  Additional information about GNU Octave is available at\n\
  http://www.octave.org\n\
\n\
  Descriptions of mailing lists devoted to Octave are available at\n\
  http://www.octave.org/archive.html\n\
\n\
  You may also find some information in the Octave Wiki at\n\
  http://wiki.octave.org\n\
\n\
  Additional functionality can be enabled by using packages from\n\
  the Octave Forge project, which may be found at\n\
  http://octave.sourceforge.net\n\
\n\
  Report bugs to <bug@octave.org> (but first, please read\n\
  http://www.octave.org/bugs.html to learn how to write a helpful report)\n\
\n");

endfunction
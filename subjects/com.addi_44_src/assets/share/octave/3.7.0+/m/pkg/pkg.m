## Copyright (C) 2005-2012 S�ren Hauberg
## Copyright (C) 2010 VZLU Prague, a.s.
## Copyright (C) 2012 Carlo de Falco
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
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
## General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with Octave; see the file COPYING.  If not, see
## <http://www.gnu.org/licenses/>.

## -*- texinfo -*-
## @deftypefn  {Command} {} pkg @var{command} @var{pkg_name}
## @deftypefnx {Command} {} pkg @var{command} @var{option} @var{pkg_name}
## Manage packages (groups of add-on functions) for Octave.  Different actions
## are available depending on the value of @var{command}.
##
## Available commands:
##
## @table @samp
##
## @item install
## Install named packages.  For example,
##
## @example
## pkg install image-1.0.0.tar.gz
## @end example
##
## @noindent
## installs the package found in the file @file{image-1.0.0.tar.gz}.
##
## The @var{option} variable can contain options that affect the manner
## in which a package is installed.  These options can be one or more of
##
## @table @code
## @item -nodeps
## The package manager will disable dependency checking.  With this option it
## is possible to install a package even when it depends on another package
## which is not installed on the system.  @strong{Use this option with care.}
##
## @item -noauto
## The package manager will not automatically load the installed package
## when starting Octave.  This overrides any setting within the package.
##
## @item -auto
## The package manager will automatically load the installed package when
## starting Octave.  This overrides any setting within the package.
##
## @item -local
## A local installation (package available only to current user) is forced,
## even if the user has system privileges.
##
## @item -global
## A global installation (package available to all users) is forced, even if
## the user doesn't normally have system privileges.
##
## @item -forge
## Install a package directly from the Octave-Forge repository.  This
## requires an internet connection and the cURL library.
##
## @item -verbose
## The package manager will print the output of all commands as
## they are performed.
## @end table
##
## @item update
## Check installed Octave-Forge packages against repository and update any
## outdated items.  This requires an internet connection and the cURL library.
## Usage:
##
## @example
## pkg update
## @end example
##
## @item uninstall
## Uninstall named packages.  For example,
##
## @example
## pkg uninstall image
## @end example
##
## @noindent
## removes the @code{image} package from the system.  If another installed
## package depends on the @code{image} package an error will be issued.
## The package can be uninstalled anyway by using the @option{-nodeps} option.
##
## @item load
## Add named packages to the path.  After loading a package it is
## possible to use the functions provided by the package.  For example,
##
## @example
## pkg load image
## @end example
##
## @noindent
## adds the @code{image} package to the path.  It is possible to load all
## installed packages at once with the keyword @samp{all}.  Usage:
##
## @example
## pkg load all
## @end example
##
## @item unload
## Remove named packages from the path.  After unloading a package it is
## no longer possible to use the functions provided by the package.  It is
## possible to unload all installed packages at once with the keyword
## @samp{all}.  Usage:
##
## @example
## pkg unload all
## @end example
##
## @item list
## Show the list of currently installed packages.  For example,
##
## @example
## installed_packages = pkg ("list")
## @end example
##
## @noindent
## returns a cell array containing a structure for each installed package.
##
## If two output arguments are requested @code{pkg} splits the list of
## installed packages into those which were installed by the current user,
## and those which were installed by the system administrator.
##
## @example
## [user_packages, system_packages] = pkg ("list")
## @end example
##
## The option "-forge" lists packages available at the Octave-Forge repository.
## This requires an internet connection and the cURL library.  For example:
##
## @example
## oct_forge_pkgs = pkg ("list", "-forge")
## @end example
##
## @item describe
## Show a short description of the named installed packages, with the option
## "-verbose" also list functions provided by the package.  For example,
##
## @example
## pkg describe -verbose all
## @end example
##
## @noindent
## will describe all installed packages and the functions they provide.
## If one output is requested a cell of structure containing the
## description and list of functions of each package is returned as
## output rather than printed on screen:
##
## @example
## desc = pkg ("describe", "secs1d", "image")
## @end example
##
## @noindent
## If any of the requested packages is not installed, pkg returns an
## error, unless a second output is requested:
##
## @example
## [desc, flag] = pkg ("describe", "secs1d", "image")
## @end example
##
## @noindent
## @var{flag} will take one of the values "Not installed", "Loaded" or
## "Not loaded" for each of the named packages.
##
## @item prefix
## Set the installation prefix directory.  For example,
##
## @example
## pkg prefix ~/my_octave_packages
## @end example
##
## @noindent
## sets the installation prefix to @file{~/my_octave_packages}.
## Packages will be installed in this directory.
##
## It is possible to get the current installation prefix by requesting an
## output argument.  For example:
##
## @example
## pfx = pkg ("prefix")
## @end example
##
## The location in which to install the architecture dependent files can be
## independently specified with an addition argument.  For example:
##
## @example
## pkg prefix ~/my_octave_packages ~/my_arch_dep_pkgs
## @end example
##
## @item local_list
## Set the file in which to look for information on locally
## installed packages.  Locally installed packages are those that are
## available only to the current user.  For example:
##
## @example
## pkg local_list ~/.octave_packages
## @end example
##
## It is possible to get the current value of local_list with the following
##
## @example
## pkg local_list
## @end example
##
## @item global_list
## Set the file in which to look for information on globally
## installed packages.  Globally installed packages are those that are
## available to all users.  For example:
##
## @example
## pkg global_list /usr/share/octave/octave_packages
## @end example
##
## It is possible to get the current value of global_list with the following
##
## @example
## pkg global_list
## @end example
##
## @item build
## Build a binary form of a package or packages.  The binary file produced
## will itself be an Octave package that can be installed normally with
## @code{pkg}.  The form of the command to build a binary package is
##
## @example
## pkg build builddir image-1.0.0.tar.gz @dots{}
## @end example
##
## @noindent
## where @code{builddir} is the name of a directory where the temporary
## installation will be produced and the binary packages will be found.
## The options @option{-verbose} and @option{-nodeps} are respected, while
## all other options are ignored.
##
## @item rebuild
## Rebuild the package database from the installed directories.  This can
## be used in cases where the package database has been corrupted.
## It can also take the @option{-auto} and @option{-noauto} options to allow the
## autoloading state of a package to be changed.  For example,
##
## @example
## pkg rebuild -noauto image
## @end example
##
## @noindent
## will remove the autoloading status of the image package.
##
## @end table
## @end deftypefn

function [local_packages, global_packages] = pkg (varargin)
  ## Installation prefix (FIXME: what should these be on windows?)
  persistent user_prefix = false;
  persistent prefix = -1;
  persistent archprefix = -1;
  persistent local_list = tilde_expand (fullfile ("~", ".octave_packages"));
  persistent global_list = fullfile (OCTAVE_HOME (), "share", "octave",
                                     "octave_packages");

  confirm_recursive_rmdir (false, "local");

  mlock ();

  ## If user is superuser set global_istall to true
  ## FIXME: is it OK to set this always true on windows?
  global_install = ((ispc () && ! isunix ()) || (geteuid () == 0));

  if (prefix == -1)
    if (global_install)
      prefix = fullfile (OCTAVE_HOME (), "share", "octave", "packages");
      archprefix = fullfile (octave_config_info ("libdir"),
                             "octave", "packages");
    else
      prefix = fullfile ("~", "octave");
      archprefix = prefix;
    endif
    prefix = tilde_expand (prefix);
    archprefix = tilde_expand (archprefix);
  endif

  available_actions = {"list", "install", "uninstall", "load", ...
                       "unload", "prefix", "local_list", ...
                       "global_list", "rebuild", "build", ...
                       "describe", "update"};
  ## Handle input
  if (length (varargin) == 0 || ! iscellstr (varargin))
    print_usage ();
  endif
  files = {};
  deps = true;
  auto = 0;
  action = "none";
  verbose = false;
  octave_forge = false;
  for i = 1:length (varargin)
    switch (varargin{i})
      case "-nodeps"
        deps = false;
      case "-noauto"
        auto = -1;
      case "-auto"
        auto = 1;
      case "-verbose"
        verbose = true;
        ## Send verbose output to pager immediately.  Change setting locally.
        page_output_immediately (true, "local");
      case "-forge"
        octave_forge = true;
      case "-local"
        global_install = false;
        if (! user_prefix)
          prefix = tilde_expand (fullfile ("~", "octave"));
          archprefix = prefix;
        endif
      case "-global"
        global_install = true;
        if (! user_prefix)
          prefix = fullfile (OCTAVE_HOME (), "share", "octave", "packages");
          archprefix = fullfile (octave_config_info ("libdir"),
                                 "octave", "packages");
        endif
      case available_actions
        if (strcmp (action, "none"))
          action = varargin{i};
        else
          error ("more than one action specified");
        endif
      otherwise
        files{end+1} = varargin{i};
    endswitch
  endfor

  if (octave_forge && ! any (strcmp (action, {"install", "list"})))
    error ("-forge can only be used with install or list");
  endif

  ## Take action
  switch (action)
    case "list"
      if (octave_forge)
        if (nargout > 0)
          local_packages = list_forge_packages ();
        else
          list_forge_packages ();
        endif
      else
        if (nargout == 0)
          installed_packages (local_list, global_list);
        elseif (nargout == 1)
          local_packages = installed_packages (local_list, global_list);
        elseif (nargout == 2)
          [local_packages, global_packages] = installed_packages (local_list,
                                                                  global_list);
        else
          error ("too many output arguments requested");
        endif
      endif

    case "install"
      if (length (files) == 0)
        error ("you must specify at least one filename when calling 'pkg install'");
      endif

      local_files = {};
      unwind_protect

        if (octave_forge)
          [urls, local_files] = cellfun ("get_forge_download", files, "uniformoutput", false);
          [files, succ] = cellfun ("urlwrite", urls, local_files, "uniformoutput", false);
          succ = [succ{:}];
          if (! all (succ))
            i = find (! succ, 1);
            error ("could not download file %s from url %s", local_files{i}, urls{i});
          endif
        endif

        install (files, deps, auto, prefix, archprefix, verbose, local_list,
                 global_list, global_install);

      unwind_protect_cleanup
        cellfun ("unlink", local_files);
      end_unwind_protect

    case "uninstall"
      if (length (files) == 0)
        error ("you must specify at least one package when calling 'pkg uninstall'");
      endif
      uninstall (files, deps, verbose, local_list,
                 global_list, global_install);

    case "load"
      if (length (files) == 0)
        error ("you must specify at least one package, 'all' or 'auto' when calling 'pkg load'");
      endif
      load_packages (files, deps, local_list, global_list);

    case "unload"
      if (length (files) == 0)
        error ("you must specify at least one package or 'all' when calling 'pkg unload'");
      endif
      unload_packages (files, deps, local_list, global_list);

    case "prefix"
      if (length (files) == 0 && nargout == 0)
        printf ("Installation prefix:             %s\n", prefix);
        printf ("Architecture dependent prefix:   %s\n", archprefix);
      elseif (length (files) == 0 && nargout >= 1)
        local_packages = prefix;
        global_packages = archprefix;
      elseif (length (files) >= 1 && nargout <= 2 && ischar (files{1}))
        prefix = files{1};
        prefix = absolute_pathname (prefix);
        local_packages = prefix;
        user_prefix = true;
        if (length (files) >= 2 && ischar (files{2}))
          archprefix = files{2};
          try
            archprefix = absolute_pathname (archprefix);
          catch
            mkdir (archprefix);
            warning ("creating the directory %s\n", archprefix);
            archprefix = absolute_pathname (archprefix);
          end_try_catch
          global_packages = archprefix;
        endif
      else
        error ("you must specify a prefix directory, or request an output argument");
      endif

    case "local_list"
      if (length (files) == 0 && nargout == 0)
        disp (local_list);
      elseif (length (files) == 0 && nargout == 1)
        local_packages = local_list;
      elseif (length (files) == 1 && nargout == 0 && ischar (files{1}))
        try
          local_list = absolute_pathname (files{1});
        catch
          ## Force file to be created
          fclose (fopen (files{1}, "wt"));
          local_list = absolute_pathname (files{1});
        end_try_catch
      else
        error ("you must specify a local_list file, or request an output argument");
      endif

    case "global_list"
      if (length (files) == 0 && nargout == 0)
        disp(global_list);
      elseif (length (files) == 0 && nargout == 1)
        local_packages = global_list;
      elseif (length (files) == 1 && nargout == 0 && ischar (files{1}))
        try
          global_list = absolute_pathname (files{1});
        catch
          ## Force file to be created
          fclose (fopen (files{1}, "wt"));
          global_list = absolute_pathname (files{1});
        end_try_catch
      else
        error ("you must specify a global_list file, or request an output argument");
      endif

    case "rebuild"
      if (global_install)
        global_packages = rebuild (prefix, archprefix, global_list, files,
                                   auto, verbose);
        global_packages = save_order (global_packages);
        save (global_list, "global_packages");
        if (nargout > 0)
          local_packages = global_packages;
        endif
      else
        local_packages = rebuild (prefix, archprefix, local_list, files, auto,
                                  verbose);
        local_packages = save_order (local_packages);
        save (local_list, "local_packages");
        if (nargout == 0)
          clear ("local_packages");
        endif
      endif

    case "build"
      if (length (files) < 2)
        error ("you must specify at least the build directory and one filename\nwhen calling 'pkg build'");
      endif
      build (files, deps, auto, verbose);

    case "describe"
      if (length (files) == 0)
        error ("you must specify at least one package or 'all' when calling 'pkg describe'");
      endif
      ## FIXME: the name of the output variables is inconsistent
      ##            with their content
      switch (nargout)
        case 0
          describe (files, verbose, local_list, global_list);
        case 1
          pkg_desc_list = describe (files, verbose, local_list, ...
                                    global_list);
          local_packages = pkg_desc_list;
        case 2
          [pkg_desc_list, flag] = describe (files, verbose, local_list, ...
                                            global_list);
          local_packages  = pkg_desc_list;
          global_packages = flag;
        otherwise
          error ("you can request at most two outputs when calling 'pkg describe'");
      endswitch

    case "update"
      if (nargout == 0)
        installed_pkgs_lst = installed_packages (local_list, global_list);
        for i = 1:length (installed_pkgs_lst)
          installed_pkg_name = installed_pkgs_lst{i}.name;
          installed_pkg_version = installed_pkgs_lst{i}.version;
          forge_pkg_version = get_forge_pkg (installed_pkg_name);
          if (compare_versions (forge_pkg_version, installed_pkg_version, ">"))
            feval (@pkg, "install", "-forge", installed_pkg_name);
          endif
        endfor
      else
        error ("no output arguments available");
      endif

    otherwise
      error ("you must specify a valid action for 'pkg'. See 'help pkg' for details");
  endswitch
endfunction

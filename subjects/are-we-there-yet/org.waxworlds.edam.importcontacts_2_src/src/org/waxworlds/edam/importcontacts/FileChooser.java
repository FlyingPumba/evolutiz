/*
 * FileChooser.java
 *
 * Copyright (C) 2010 Tim Marston <edam@waxworlds.org>
 *
 * This file is part of the Import Contacts program (hereafter referred
 * to as "this program"). For more information, see
 * http://www.waxworlds.org/edam/software/android/import-contacts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.waxworlds.edam.importcontacts;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FileChooser
{
	// pick an existing directory
	public final static int MODE_DIR = 1;

	// pick an existing file
	public final static int MODE_FILE = 2;


	private Dialog _dialog;

	// mode
	private int _mode = MODE_DIR;

	// ok was pressed
	boolean _ok = false;

	// working path
	private String _path;

	// selected filename
	private String _filename;

	// enforce extension (in file-mode)
	private String[] _extensions;

	// path to secretly prefix all paths with
	private String _path_prefix = "";

	private Context _context;
	private ArrayList< RowItem > _items;
	private DialogInterface.OnDismissListener _on_dismiss_listener;

	// class that represents a row in the list
	private class RowItem implements Comparable< RowItem >
	{
		private String _name;
		private boolean _directory;

		public RowItem( String name, boolean directory )
		{
			_name = name;
			_directory = directory;
		}

		public String getName()
		{
			return _name;
		}

		public boolean isDirectory()
		{
			return _directory;
		}

		@Override
		public int compareTo( RowItem that )
		{
			if( this._directory && !that._directory )
				return -1;
			else if( !this._directory && that._directory )
				return 1;
			else
				return this._name.compareToIgnoreCase( that._name );
		}
	}

	// class to manage our list of RowItems
	private class RowItemAdapter extends ArrayAdapter< RowItem >
	{
		private ArrayList< RowItem > _items;

		public RowItemAdapter( Context context, int textview_resource_id,
			ArrayList< RowItem > items )
		{
			super( context, textview_resource_id, items );
			_items = items;
		}

		@Override
		public View getView( int position, View convert_view, ViewGroup parent )
		{
			View view = convert_view;
			if( view == null ) {
				LayoutInflater factory = LayoutInflater.from( _context );
				view = factory.inflate(  R.layout.filechooser_row, null );
			}
			RowItem rowitem = _items.get( position );
			if( rowitem != null ) {
				( (TextView)view.findViewById( R.id.name ) )
					.setText( rowitem.getName() );
				( (ImageView)view.findViewById( R.id.icon ) ).setVisibility(
					rowitem.isDirectory()? View.VISIBLE : View.GONE );
			}
			return view;
		}
	}

	@SuppressWarnings( "serial" )
	class InvalidPathPrefixException extends RuntimeException
	{
	}



	// constructor
	public FileChooser( Context context )
	{
		_context = context;
	}

	public void setMode( int mode )
	{
		_mode = mode;
	}

	public void setPath( String path )
	{
		_path = cleanUpPath( path );
		File file = new File( _path_prefix + path.trim() );

		// path and filename
		if( file.isFile() ) {
			_path = _path.substring( 0, _path.length() - 1 );
			_filename = _path.substring( _path.lastIndexOf( '/' ) + 1 );
			_path = _path.substring( 0, _path.length() - _filename.length() );
		}

		// else, treat as just a path
		else
			_filename = "";
	}

	public void setExtensions( String[] extensions )
	{
		_extensions = extensions;
	}

	// set dismiss listener
	public void setDismissListener(
		DialogInterface.OnDismissListener on_dismiss_listener )
	{
		_on_dismiss_listener = on_dismiss_listener;
	}

	// set the path prefix
	public void setPathPrefix( String path_prefix )
	{
		// set to cleaned-up path, with trailing '/' removed so that it can be
		// trivially pre-pended to a cleaned-up path
		_path_prefix = cleanUpPath( path_prefix );
		_path_prefix = _path_prefix.substring( 0, _path_prefix.length() - 1 );
	}

	public boolean getOk()
	{
		return _ok;
	}

	public String getPath()
	{
		return _path + _filename;
	}

	public Dialog onCreateDialog()
	{
		// custom layout in an AlertDialog
		LayoutInflater factory = LayoutInflater.from( _context );
		final View dialogView = factory.inflate(
			R.layout.filechooser, null );

		// wire up buttons
		( (Button)dialogView.findViewById( R.id.ok ) )
			.setOnClickListener( _fileChooserButtonListener );
		( (ListView)dialogView.findViewById( R.id.list ) )
			.setOnItemClickListener( _fileChooserItemClickListener );

		// return dialog
		Dialog dialog = new AlertDialog.Builder( _context )
			.setTitle( " " )
			.setView( dialogView )
			.create();
		dialog.setOnDismissListener( _on_dismiss_listener );
		return dialog;
	}

	private OnClickListener _fileChooserButtonListener = new OnClickListener() {
		public void onClick( View view )
		{
			switch( view.getId() )
			{
			case R.id.ok:
				// close dialog and free (don't keep a reference)
				_ok = true;
				_dialog.dismiss();
				break;
			}
		}
	};

	private OnItemClickListener _fileChooserItemClickListener = new OnItemClickListener() {
		public void onItemClick( AdapterView< ? > adapter_view, View view, int position, long id )
		{
			RowItem rowitem = _items.get( position );

			// handle directory changes
			if( rowitem.isDirectory() )
			{
				String dirname = rowitem.getName();
				if( dirname.equals( ".." ) )
					strtipLastFilepartFromPath();
				else
					_path += dirname + "/";
				_filename = "";

				updateList();
			}

			// handle file selections
			else
			{
				_filename = rowitem.getName();
				updateCurrentSelection();
			}
		}
	};

	public void onPrepareDialog( Context context, Dialog dialog )
	{
		// set up reference to dialog
		_dialog = dialog;
		_context = context;

		// reset "ok"
		_ok = false;

		// pick text based on mode
		int title = 0, current = 0;
		switch( _mode ) {
		case MODE_DIR:
			title = R.string.filechooser_title_dir;
			current = R.string.filechooser_current_dir;
			break;
		case MODE_FILE:
			title = R.string.filechooser_title_file;
			current = R.string.filechooser_current_file;
			break;
		}
		dialog.setTitle( title );
		( (TextView)dialog.findViewById( R.id.current ) )
			.setText( _context.getString(  current ) );

		// clear filename in directory mode
		if( _mode == MODE_DIR )
			_filename = "";

		// set root path icon
		( (ImageView)_dialog.findViewById( R.id.icon ) )
			.setImageResource( pathIcon( cleanUpPath( _path_prefix ) ) );

		// setup current-path-specific stuff
		updateList();
	}

	public static String cleanUpPath( String path )
	{
		path = path.trim();

		// ensure it starts and ends in a '/'
		if( !path.startsWith( "/" ) ) path = "/" + path;
		if( !path.endsWith( "/" ) ) path += "/";

		return path;
	}

	public static int pathIcon( String path )
	{
		if( path.equals( "/sdcard/" ) )
			return R.drawable.sdcard;

		return R.drawable.directory;
	}

	public String prettyPrint( String full_path, boolean return_full )
	{
		String path = full_path;

		// special names
		if( path.equals( "/sdcard/" ) )
			return " " + _context.getString( R.string.filechooser_path_sdcard );

		// remove prefix, if present
		if( path.startsWith( _path_prefix + "/" ) )
			path = path.substring( _path_prefix.length() );

		// unless path is "/", strip trailing "/".
		if( path.length() > 1 && path.endsWith( "/" ) )
			path = path.substring( 0, path.length() - 1 );

		// if full path not required, strip off preceding directories
		if( !return_full ) {
			int idx = path.lastIndexOf( "/" );
			if( idx != -1 ) path = path.substring( idx + 1 );
		}

		return path;
	}

	protected void strtipLastFilepartFromPath()
	{
		int at = _path.lastIndexOf( '/', _path.length() - 2 );
		if( at != -1 ) _path = _path.substring( 0, at + 1 );
	}

	protected void updateList()
	{
		// reset item list
		_items = new ArrayList< RowItem >();

		// open directory (and ensure _path is a directory)
		File dir = new File( _path_prefix + _path );
		while( !dir.isDirectory() ) {
			if( _path == "/" )
				throw new InvalidPathPrefixException();
			strtipLastFilepartFromPath();
			dir = new File( _path_prefix + _path );
		}

		// add ".."?
		if( !_path.equals( "/" ) )
			_items.add( new RowItem( "..", true ) );

		// get directories
		class DirFilter implements FileFilter {
			public boolean accept( File file ) {
				return file.isDirectory() && file.getName().charAt( 0 ) != '.';
			}
		}
		File[] files = dir.listFiles( new DirFilter() );
		for( int i = 0; i < files.length; i++ )
			_items.add( new RowItem( files[ i ].getName(), true ) );

		// get files
		if( _mode == MODE_FILE )
		{
			class VCardFilter implements FileFilter {
				public boolean accept( File file ) {
					if( file.isDirectory() || file.getName().startsWith( "." ) )
						return false;
					String filename = file.getName().toLowerCase();
					for( int i = 0; i < _extensions.length; i++ )
						if( filename.endsWith( "." + _extensions[ i ] ) )
							return true;
					return false;
				}
			}
			files = dir.listFiles( new VCardFilter() );
			for( int i = 0; i < files.length; i++ )
				_items.add( new RowItem( files[ i ].getName(), false ) );
		}

		// sort
		class RowItemSorter implements Comparator< RowItem > {
			@Override
			public int compare( RowItem lhs, RowItem rhs ) {
				return lhs.compareTo( rhs );
			}
		}
		Collections.sort( _items, new RowItemSorter() );

		// setup directory list
		( (ListView)_dialog.findViewById( R.id.list ) ).setAdapter(
			new RowItemAdapter( _context, R.layout.filechooser_row,
				_items ) );

		updateCurrentSelection();
	}

	private void updateCurrentSelection()
	{
		// set current path
		( (TextView)_dialog.findViewById( R.id.path ) ).setText(
			prettyPrint( _path_prefix + _path + _filename, true ) );

		// enable/disable ok button
		if( _mode == MODE_FILE )
			_dialog.findViewById( R.id.ok ).setEnabled( _filename != "" );
		else
			_dialog.findViewById( R.id.ok ).setEnabled( true );
	}

}

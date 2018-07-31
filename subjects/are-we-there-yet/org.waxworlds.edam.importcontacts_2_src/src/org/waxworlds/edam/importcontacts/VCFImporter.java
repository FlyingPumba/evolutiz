/*
 * VCFImporter.java
 *
 * Copyright (C) 2009 Tim Marston <edam@waxworlds.org>
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.provider.Contacts;
import android.provider.Contacts.PhonesColumns;

public class VCFImporter extends Importer
{
	private int _vCardCount = 0;
	private int _progress = 0;

	public VCFImporter( Doit doit )
	{
		super( doit );
	}

	@Override
	protected void onImport() throws AbortImportException
	{
		SharedPreferences prefs = getSharedPreferences();

		// update UI
		setProgressMessage( R.string.doit_scanning );

		// get a list of vcf files
		File[] files = null;
		try
		{
			// open directory
			String path = "/sdcard" + prefs.getString( "location", "/" );
			File file = new File( path );
			if( !file.exists() )
				showError( R.string.error_locationnotfound );

			// directory, or file?
			if( file.isDirectory() )
			{
				// get files
				class VCardFilter implements FilenameFilter {
					public boolean accept( File dir, String name ) {
						return name.toLowerCase().endsWith( ".vcf" );
					}
				}
				files = file.listFiles( new VCardFilter() );
			}
			else
			{
				// use just this file
				files = new File[ 1 ];
				files[ 0 ] = file;
			}
		}
		catch( SecurityException e ) {
			showError( R.string.error_locationpermissions );
		}

		// check num files and set progress max
		if( files != null && files.length > 0 )
			setProgressMax( files.length );
		else
			showError( R.string.error_locationnofiles );

		// scan through the files
		setTmpProgress( 0 );
		for( int i = 0; i < files.length; i++ ) {
			countVCardFile( files[ i ] );
			setTmpProgress( i );
		}
		setProgressMax( _vCardCount );	// will also update tmp progress

		// import them
		setProgress( 0 );
		for( int i = 0; i < files.length; i++ )
			importVCardFile( files[ i ] );
	}

	private void countVCardFile( File file ) throws AbortImportException
	{
		try
		{
			// open file
			BufferedReader reader = new BufferedReader(
					new FileReader( file ) );

			// read
			String line;
			boolean inVCard = false;
			while( ( line = reader.readLine() ) != null )
			{
				if( !inVCard ) {
					// look for vcard beginning
					if( line.matches( "^BEGIN[ \\t]*:[ \\t]*VCARD" ) ) {
						inVCard = true;
						_vCardCount++;
					}
				}
				else if( line.matches( "^END[ \\t]*:[ \\t]*VCARD" ) )
					inVCard = false;
			}

		}
		catch( FileNotFoundException e ) {
			showError( getText( R.string.error_filenotfound ) +
				file.getName() );
		}
		catch( IOException e ) {
			showError( getText( R.string.error_ioerror ) + file.getName() );
		}
	}

	private void importVCardFile( File file ) throws AbortImportException
	{
		// check file is good
		if( !file.exists() )
			showError( getText( R.string.error_filenotfound ) +
				file.getName() );
		if( file.length() == 0 )
			showError( getText( R.string.error_fileisempty ) +
				file.getName() );

		try
		{
			// open/read file
			FileInputStream istream = new FileInputStream( file );
			byte[] content = new byte[ (int)file.length() ];
			istream.read( content );

			// import
			importVCardFileContent( content, file.getName() );
		}
		catch( FileNotFoundException e ) {
			showError( getText( R.string.error_filenotfound ) +
				file.getName() );
		}
		catch( IOException e ) {
			showError( getText( R.string.error_ioerror ) + file.getName() );
		}
	}

	private void importVCardFileContent( byte[] content, String fileName )
			throws AbortImportException
	{
		ByteBuffer buffers[] = getLinesFromContent( content );

		// go through lines
		VCard vCard = null;
		for( int i = 0; i < buffers.length; i++ )
		{
			// get a US-ASCII version of the line for processing
			String line;
			try {
				line = new String( buffers[ i ].array(), buffers[ i ].position(),
					buffers[ i ].limit() - buffers[ i ].position(), "US-ASCII" );
			}
			catch( UnsupportedEncodingException e ) {
				// we know US-ASCII is supported, so appease the compiler...
				line = "";
			}

			if( vCard == null ) {
				// look for vcard beginning
				if( line.matches( "^BEGIN[ \\t]*:[ \\t]*VCARD" ) ) {
					setProgress( ++_progress );
					vCard = new VCard();
				}
			}
			else {
				// look for vcard content or ending
				if( line.matches( "^END[ \\t]*:[ \\t]*VCARD" ) )
				{
					// store vcard and do away with it
					try {
						vCard.finaliseParsing();
						importContact( vCard );
					}
					catch( VCard.ParseException e ) {
						skipContact();
						if( !showContinue(
								getText( R.string.error_vcf_parse ).toString()
								+ fileName + "\n" + e.getMessage() ) )
							finish( ACTION_ABORT );
					}
					catch( VCard.SkipContactException e ) {
						skipContact();
						// do nothing
					}
					vCard = null;
				}
				else
				{
					// try giving the line to the vcard
					try {
						vCard.parseLine( buffers[ i ] );
					}
					catch( VCard.ParseException e ) {
						skipContact();
						if( !showContinue(
								getText( R.string.error_vcf_parse ).toString()
								+ fileName + "\n" + e.getMessage() ) )
							finish( ACTION_ABORT );

						// although we're continuing, we still need to abort
						// this vCard. Further lines will be ignored until we
						// get to another BEGIN:VCARD line.
						vCard = null;
					}
					catch( VCard.SkipContactException e ) {
						skipContact();
						// abort this vCard. Further lines will be ignored until
						// we get to another BEGIN:VCARD line.
						vCard = null;
					}
				}
			}
		}
	}

	private ByteBuffer[] getLinesFromContent( byte[] content )
	{
		// count lines in data
		int num_lines = 1;
		for( int a = 0; a < content.length; a++ )
			if( content[ a ] == '\n' )
				num_lines++;

		// get lines, removing \r's and \n's as we go
		ByteBuffer lines[] = new ByteBuffer[ num_lines ];
		int last = 0;
		for( int a = 0, b = 0; a < content.length; a++ )
			if( content[ a ] == '\n' ) {
				int to = ( a > 0 && content[ a - 1 ] == '\r' &&
					a - 1 >= last )? a - 1 : a;
				lines[ b++ ] = ByteBuffer.wrap( content, last, to - last );
				last = a + 1;
			}
		lines[ lines.length - 1 ] = ByteBuffer.wrap( content, last,
			content.length - last );

		return lines;
	}

	private class VCard extends ContactData
	{
		private final static int NAMELEVEL_NONE = 0;
		private final static int NAMELEVEL_ORG = 1;
		private final static int NAMELEVEL_FN = 2;
		private final static int NAMELEVEL_N = 3;

		private String _version = null;
		private Vector< ByteBuffer > _buffers = null;
		private int _name_level = NAMELEVEL_NONE;
		private boolean _parser_in_multiline = false;
		private String _parser_current_name_and_params = null;
		private String _parser_buffered_value_so_far = "";

		protected class UnencodeResult
		{
			private boolean _another_line_required;
			private ByteBuffer _buffer;

			public UnencodeResult( boolean another_line_required,
				ByteBuffer buffer )
			{
				_another_line_required = another_line_required;
				_buffer = buffer;
			}

			public boolean isAnotherLineRequired()
			{
				return _another_line_required;
			}

			public ByteBuffer getBuffer()
			{
				return _buffer;
			}
		}

		@SuppressWarnings("serial")
		protected class ParseException extends Exception
		{
			@SuppressWarnings("unused")
			public ParseException( String error )
			{
				super( error );
			}

			public ParseException( int res )
			{
				super( VCFImporter.this.getText( res ).toString() );
			}
		}

		@SuppressWarnings("serial")
		protected class SkipContactException extends Exception { }

		public void parseLine( ByteBuffer buffer )
				throws ParseException, SkipContactException,
				AbortImportException
		{
			// get a US-ASCII version of the line for processing
			String line;
			try {
				line = new String( buffer.array(), buffer.position(),
					buffer.limit() - buffer.position(), "US-ASCII" );
			}
			catch( UnsupportedEncodingException e ) {
				// we know US-ASCII is supported, so appease the compiler...
				line = "";
			}

			// ignore empty lines
			if( line.trim() == "" ) return;

			// split line into name and value parts (this may turn out to be
			// unwanted if the line is a subsequent line in a multi-line
			// value, but we have to do this now to check for and handle VCF
			// versions first). Also, the value part is only created tentatively
			// because it may have an encoding/charset. Since we're treating it
			// as UTF-8 (which is compatible with 7-bit US-ASCII) this is ok
			// though so long as we later use the raw bytes. ALso we check for
			// malformed property:name pairs.
			String name_and_params, string_value;
			{
				String[] parts = line.split( ":", 2 );
				if( parts.length == 2 ) {
					name_and_params = parts[ 0 ].trim();
					string_value = parts[ 1 ].trim();
					if( name_and_params.length() == 0 )
						throw new ParseException( R.string.error_vcf_malformed );
				}
				else
				{
					if( !_parser_in_multiline )
						throw new ParseException( R.string.error_vcf_malformed );
					name_and_params = null;
					string_value = null;
				}
			}

			// if we haven't yet got a version, we won't be paring anything!
			if( _version == null )
			{
				// is this a version?
				if( name_and_params.equals( "VERSION" ) )
				{
					// yes, check/store it
					if( !string_value.equals( "2.1" ) &&
							!string_value.equals( "3.0" ) )
						throw new ParseException( R.string.error_vcf_version );
					_version = string_value;

					// parse any other buffers we've accumulated so far
					if( _buffers != null )
						for( int i = 0; i < _buffers.size(); i++ )
							parseLine( _buffers.get( i ) );
					_buffers = null;
				}
				else
				{
					// no, so stash this buffer till we have a version
					if( _buffers == null )
						_buffers = new Vector< ByteBuffer >();
					_buffers.add( buffer );
				}
			}
			else
			{
				// value bytes, for processing
				ByteBuffer value;

				if( _parser_in_multiline )
				{
					// if we're currently in a multi-line value, use the stored
					// property name and parameters
					name_and_params = _parser_current_name_and_params;

					// find start of string (skip spaces/tabs)
					int pos = buffer.position();
					byte[] buffer_array = buffer.array();
					while( pos < buffer.limit() && (
						buffer_array[ pos ] == ' ' ||
						buffer_array[ pos ] == '\t' ) )
					{
						pos++;
					}

					// get value from buffer
					value = ByteBuffer.wrap( buffer.array(), pos,
						buffer.limit() - pos );
				}
				else
				{
					// ignore empty values
					if( string_value.length() < 1 ) return;

					// calculate how many chars to skip from beginning of line
					// so we skip the property "name:" part
					int pos = buffer.position() + name_and_params.length() + 1;

					// get value from buffer
					value = ByteBuffer.wrap( buffer.array(), pos,
						buffer.limit() - pos );

					// reset the saved multi-line state
					_parser_current_name_and_params = name_and_params;
					_parser_buffered_value_so_far = "";
				}

				// get parameter parts
				String[] name_param_parts = name_and_params.split( ";", -1 );
				for( int i = 0; i < name_param_parts.length; i++ )
					name_param_parts[ i ] = name_param_parts[ i ].trim();

				// parse encoding parameter
				String encoding = checkParam( name_param_parts, "ENCODING" );
				if( encoding != null ) encoding = encoding.toUpperCase();
				if( encoding != null && !encoding.equals( "8BIT" ) &&
					!encoding.equals( "QUOTED-PRINTABLE" ) )
					//&& !encoding.equals( "BASE64" ) )
				{
					throw new ParseException( R.string.error_vcf_encoding );
				}

				// parse charset parameter
				String charset = checkParam( name_param_parts, "CHARSET" );
				if( charset != null ) charset = charset.toUpperCase();
				if( charset != null && !charset.equals( "US-ASCII" ) &&
					!charset.equals( "ASCII" ) && !charset.equals( "UTF-8" ) )
				{
					throw new ParseException( R.string.error_vcf_charset );
				}

				// do unencoding (or default to a fake unencoding result with
				// the raw string)
				UnencodeResult unencoding_result = null;
				if( encoding != null && encoding.equals( "QUOTED-PRINTABLE" ) )
					unencoding_result = unencodeQuotedPrintable( value );
//				else if( encoding != null && encoding.equals( "BASE64" ) )
//					result = unencodeBase64( props[ 1 ], charset );
				if( unencoding_result != null ) {
					value = unencoding_result.getBuffer();
					_parser_in_multiline =
						unencoding_result.isAnotherLineRequired();
				}

				// convert 8-bit ASCII charset to US-ASCII
				if( charset == null || charset == "ASCII" ) {
					value = transcodeAsciiToUtf8( value );
					charset = "UTF-8";
				}

				// process charset
				try {
					string_value =
						new String( value.array(), value.position(),
							value.limit() - value.position(), charset );
				} catch( UnsupportedEncodingException e ) {
					throw new ParseException( R.string.error_vcf_charset );
				}

				// handle multi-line requests
				if( _parser_in_multiline ) {
					_parser_buffered_value_so_far += string_value;
					return;
				}

				// add on buffered multi-line content
				String complete_value =
					_parser_buffered_value_so_far + string_value;

				// parse some properties
				if( name_param_parts[ 0 ].equals( "N" ) )
					parseN( name_param_parts, complete_value );
				else if( name_param_parts[ 0 ].equals( "FN" ) )
					parseFN( name_param_parts, complete_value );
				else if( name_param_parts[ 0 ].equals( "ORG" ) )
					parseORG( name_param_parts, complete_value );
				else if( name_param_parts[ 0 ].equals( "TEL" ) )
					parseTEL( name_param_parts, complete_value );
				else if( name_param_parts[ 0 ].equals( "EMAIL" ) )
					parseEMAIL( name_param_parts, complete_value );
			}
		}

		private void parseN( String[] params, String value )
				throws ParseException, SkipContactException,
				AbortImportException
		{
			// already got a better name?
			if( _name_level >= NAMELEVEL_N ) return;

			// get name parts
			String[] name_parts = value.split( ";" );
			for( int i = 0; i < name_parts.length; i++ )
				name_parts[ i ] = name_parts[ i ].trim();

			// build name
			value = "";
			if( name_parts.length > 1 && name_parts[ 1 ].length() > 0 )
				value += name_parts[ 1 ];
			if( name_parts.length > 0 && name_parts[ 0 ].length() > 0 )
				value += ( value.length() == 0? "" : " " ) + name_parts[ 0 ];

			// set name
			setName( value );
			_name_level = NAMELEVEL_N;

			// check now to see if we need to import this contact (to avoid
			// parsing the rest of the vCard unnecessarily)
			if( !isImportRequired( getName() ) )
				throw new SkipContactException();
		}

		private void parseFN( String[] params, String value )
				throws ParseException, SkipContactException
		{
			// already got a better name?
			if( _name_level >= NAMELEVEL_FN ) return;

			// set name
			setName( value );
			_name_level = NAMELEVEL_FN;
		}

		private void parseORG( String[] params, String value )
				throws ParseException, SkipContactException
		{
			// already got a better name?
			if( _name_level >= NAMELEVEL_ORG ) return;

			// get org parts
			String[] org_parts = value.split( ";" );
			for( int i = 0; i < org_parts.length; i++ )
				org_parts[ i ] = org_parts[ i ].trim();

			// build name
			if( org_parts.length > 1 && org_parts[ 0 ].length() == 0 )
				value = org_parts[ 1 ];
			else
				value = org_parts[ 0 ];

			// set name
			setName( value );
			_name_level = NAMELEVEL_ORG;
		}

		private void parseTEL( String[] params, String value )
				throws ParseException
		{
			if( value.length() == 0 ) return;

			Set< String > types = extractTypes( params, Arrays.asList(
					"PREF", "HOME", "WORK", "VOICE", "FAX", "MSG", "CELL",
					"PAGER", "BBS", "MODEM", "CAR", "ISDN", "VIDEO" ) );

			// here's the logic...
			boolean preferred = types.contains( "PREF" );
			int type = PhonesColumns.TYPE_MOBILE;
			if( types.contains( "VOICE" ) )
				if( types.contains( "WORK" ) )
					type = PhonesColumns.TYPE_WORK;
				else
					type = PhonesColumns.TYPE_HOME;
			else if( types.contains( "CELL" ) || types.contains( "VIDEO" ) )
				type = PhonesColumns.TYPE_MOBILE;
			if( types.contains( "FAX" ) )
				if( types.contains( "HOME" ) )
					type = PhonesColumns.TYPE_FAX_HOME;
				else
					type = PhonesColumns.TYPE_FAX_WORK;
			if( types.contains( "PAGER" ) )
				type = PhonesColumns.TYPE_PAGER;

			// add phone number
			addPhone( value, type, preferred );
		}

		public void parseEMAIL( String[] params, String value )
				throws ParseException
		{
			if( value.length() == 0 ) return;

			Set< String > types = extractTypes( params, Arrays.asList(
					"PREF", "WORK", "HOME", "INTERNET" ) );

			// here's the logic...
			boolean preferred = types.contains( "PREF" );
			if( types.contains( "WORK" ) )
				addEmail( value, Contacts.ContactMethods.TYPE_WORK, preferred );
			else
				addEmail( value, Contacts.ContactMethods.TYPE_HOME, preferred );
		}

		public void finaliseParsing()
				throws ParseException, SkipContactException,
				AbortImportException
		{
			// missing version (and data is present)
			if( _version == null && _buffers != null )
				throw new ParseException( R.string.error_vcf_malformed );

			//  missing name properties?
			if( _name_level == NAMELEVEL_NONE )
				throw new ParseException( R.string.error_vcf_noname );

			// check if we should import this one? If we've already got an 'N'-
			// type name, this will already have been done by parseN() so we
			// mustn't do this here (or it could prompt twice!)
			if( _name_level < NAMELEVEL_N && !isImportRequired( getName() ) )
				throw new SkipContactException();
		}

		private String checkParam( String[] params, String name )
		{
			Pattern p = Pattern.compile( "^" + name + "[ \\t]*=[ \\t]*(.*)$" );
			for( int i = 0; i < params.length; i++ ) {
				Matcher m = p.matcher( params[ i ] );
				if( m.matches() )
					return m.group( 1 );
			}
			return null;
		}

		private Set< String > extractTypes( String[] params,
				List< String > valid_types )
		{
			HashSet< String > types = new HashSet< String >();

			// get 3.0-style TYPE= param
			String type_param;
			if( ( type_param = checkParam( params, "TYPE" ) ) != null ) {
				String[] parts = type_param.split( "," );
				for( int i = 0; i < parts.length; i++ )
					if( valid_types.contains( parts[ i ] ) )
						types.add( parts[ i ] );
			}

			// get 2.1-style type param
			if( _version.equals( "2.1" ) ) {
				for( int i = 1; i < params.length; i++ )
					if( valid_types.contains( params[ i ] ) )
						types.add( params[ i ] );
			}

			return types;
		}

		private UnencodeResult unencodeQuotedPrintable( ByteBuffer in )
		{
			boolean another = false;

			// unencode quoted-pritable encoding, as per RFC1521 section 5.1
			byte[] out = new byte[ in.limit() - in.position() ];
			int j = 0;
			for( int i = in.position(); i < in.limit(); i++ )
			{
				// get next char and process...
				byte ch = in.array()[ i ];
				if( ch == '=' && i < in.limit() - 2 )
				{
					// we found a =XX format byte, add it
					out[ j ] = (byte)(
						Character.digit( in.array()[ i + 1 ], 16 ) * 16 +
						Character.digit( in.array()[ i + 2 ], 16 ) );
					i += 2;
				}
				else if( ch == '=' && i == in.limit() - 1 )
				{
					// we found a '=' at the end of a line signifying a multi-
					// line string, so we don't add it.
					another = true;
					continue;
				}
				else
					// just a normal char...
					out[ j ] = (byte)ch;
				j++;
			}

			return new UnencodeResult( another, ByteBuffer.wrap( out, 0, j ) );
		}

		private ByteBuffer transcodeAsciiToUtf8( ByteBuffer in )
		{
			// transcode
			byte[] out = new byte[ ( in.limit() - in.position() ) * 2 ];
			int j = 0;
			for( int a = in.position(); a < in.limit(); a++ )
			{
				// if char is < 127, keep it as-is
				if( in.array()[ a ] >= 0 )
					out[ j++ ] = in.array()[ a ];

				// else, convert it to UTF-8
				else {
					int b = 0xff & (int)in.array()[ a ];
					out[ j++ ] = (byte)( 0xc0 | ( b >> 6 ) );
					out[ j++ ] = (byte)( 0x80 | ( b & 0x3f ) );
				}
			}

			return ByteBuffer.wrap( out, 0, j );
		}
	}
}

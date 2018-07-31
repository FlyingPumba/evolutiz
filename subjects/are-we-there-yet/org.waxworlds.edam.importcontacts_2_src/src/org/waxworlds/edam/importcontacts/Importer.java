/*
 * Importer.java
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.provider.Contacts;

public class Importer extends Thread
{
	public final static int ACTION_ABORT = 1;
	public final static int ACTION_ALLDONE = 2;

	public final static int RESPONSE_NEGATIVE = 0;
	public final static int RESPONSE_POSITIVE = 1;

	public final static int RESPONSEEXTRA_NONE = 0;
	public final static int RESPONSEEXTRA_ALWAYS = 1;

	private Doit _doit;
	private int _response;
	private int _responseExtra;
	private HashMap< String, Long > _contacts;
	private HashMap< Long, HashSet< String > > _contactNumbers;
	private HashMap< Long, HashSet< String > > _contactEmails;
	private int _mergeSetting;
	private int _lastMergeDecision;
	private boolean _abort = false;
	private boolean _isFinished = false;

	public class ContactData
	{
		class PhoneData
		{
			public String _number;
			public int _type;
			public boolean _isPreferred;

			public PhoneData( String number, int type, boolean isPreferred ) {
				_number = number;
				_type = type;
				_isPreferred = isPreferred;
			}

			public String getNumber() {
				return _number;
			}

			public int getType() {
				return _type;
			}

			public boolean isPreferred() {
				return _isPreferred;
			}
		}

		class EmailData
		{
			private String _email;
			public int _type;
			private boolean _isPreferred;

			public EmailData( String email, int type, boolean isPreferred ) {
				_email = email;
				_type = type;
				_isPreferred = isPreferred;
			}

			public String getAddress() {
				return _email;
			}

			public int getType() {
				return _type;
			}

			public boolean isPreferred() {
				return _isPreferred;
			}
		}

		public String _name = null;
		public HashMap< String, PhoneData > _phones = null;
		public HashMap< String, EmailData > _emails = null;

		protected void setName( String name )
		{
			_name = name;
		}

		public String getName()
		{
			return _name;
		}

		protected void addPhone( String number, int type, boolean isPreferred )
		{
			if( _phones == null ) _phones = new HashMap< String, PhoneData >();
			if( !_phones.containsKey( number ) )
				_phones.put( number,
						new PhoneData( number, type, isPreferred ) );
		}

		protected void addEmail( String email, int type, boolean isPreferred )
		{
			if( _emails == null ) _emails = new HashMap< String, EmailData >();
			if( !_emails.containsKey( email ) )
				_emails.put( email, new EmailData( email, type, isPreferred ) );
		}
	}

	@SuppressWarnings("serial")
	protected class AbortImportException extends Exception { };

	public Importer( Doit doit )
	{
		_doit = doit;

		SharedPreferences prefs = getSharedPreferences();
		_mergeSetting = prefs.getInt( "merge_setting", Doit.ACTION_PROMPT );
	}

	@Override
	public void run()
	{
		try
		{
			// cache current contact names
			buildContactsCache();

			// do the import
			onImport();

			// done!
			finish( ACTION_ALLDONE );
		}
		catch( AbortImportException e )
		{}

		// flag as finished to prevent interrupts
		setIsFinished();
	}

	synchronized private void setIsFinished()
	{
		_isFinished = true;
	}

	protected void onImport() throws AbortImportException
	{
	}

	public void wake()
	{
		wake( 0, RESPONSEEXTRA_NONE );
	}

	public void wake( int response )
	{
		wake( response, RESPONSEEXTRA_NONE );
	}

	synchronized public void wake( int response, int responseExtra )
	{
		_response = response;
		_responseExtra = responseExtra;
		notify();
	}

	synchronized public boolean setAbort()
	{
		if( !_isFinished && !_abort ) {
			_abort = true;
			notify();
			return true;
		}
		return false;
	}

	protected SharedPreferences getSharedPreferences()
	{
		return _doit.getSharedPreferences();
	}

	protected void showError( int res ) throws AbortImportException
	{
		showError( _doit.getText( res ).toString() );
	}

	synchronized protected void showError( String message )
			throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendMessage( Message.obtain(
				_doit._handler, Doit.MESSAGE_ERROR, message ) );
		try {
			wait();
		}
		catch( InterruptedException e ) { }

		// no need to check if an abortion happened during the wait, we are
		// about to finish anyway!
		finish( ACTION_ABORT );
	}

	protected void showFatalError( int res ) throws AbortImportException
	{
		showFatalError( _doit.getText( res ).toString() );
	}

	synchronized protected void showFatalError( String message )
			throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendMessage( Message.obtain(
				_doit._handler, Doit.MESSAGE_ERROR, message ) );
		try {
			wait();
		}
		catch( InterruptedException e ) { }

		// no need to check if an abortion happened during the wait, we are
		// about to finish anyway!
		finish( ACTION_ABORT );
	}

	protected boolean showContinue( int res ) throws AbortImportException
	{
		return showContinue( _doit.getText( res ).toString() );
	}

	synchronized protected boolean showContinue( String message )
			throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendMessage( Message.obtain(
				_doit._handler, Doit.MESSAGE_CONTINUEORABORT, message ) );
		try {
			wait();
		}
		catch( InterruptedException e ) { }

		// check if an abortion happened during the wait
		checkAbort();

		return _response == RESPONSE_POSITIVE;
	}

	protected void setProgressMessage( int res ) throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendMessage( Message.obtain( _doit._handler,
				Doit.MESSAGE_SETPROGRESSMESSAGE, getText( res ) ) );
	}

	protected void setProgressMax( int maxProgress )
			throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendMessage( Message.obtain(
				_doit._handler, Doit.MESSAGE_SETMAXPROGRESS,
				new Integer( maxProgress ) ) );
	}

	protected void setTmpProgress( int tmpProgress ) throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendMessage( Message.obtain(
				_doit._handler, Doit.MESSAGE_SETTMPPROGRESS,
				new Integer( tmpProgress ) ) );
	}

	protected void setProgress( int progress ) throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendMessage( Message.obtain(
				_doit._handler, Doit.MESSAGE_SETPROGRESS,
				new Integer( progress ) ) );
	}

	protected void finish( int action ) throws AbortImportException
	{
		// update UI to reflect action
		int message;
		switch( action )
		{
		case ACTION_ALLDONE:	message = Doit.MESSAGE_ALLDONE; break;
		default:	// fall through
		case ACTION_ABORT:		message = Doit.MESSAGE_ABORT; break;
		}
		_doit._handler.sendEmptyMessage( message );

		// stop
		throw new AbortImportException();
	}

	protected CharSequence getText( int res )
	{
		return _doit.getText( res );
	}

	protected boolean isImportRequired( String name )
			throws AbortImportException
	{
		checkAbort();
		return isImportRequired( name, _mergeSetting );
	}

	synchronized private boolean isImportRequired( String name,
			int mergeSetting ) throws AbortImportException
	{
		_lastMergeDecision = mergeSetting;

		// handle special cases
		switch( mergeSetting )
		{
		case Doit.ACTION_KEEP:
			// if we keep contacts on duplicate, we better check for one
			return !_contacts.containsKey( name );

		case Doit.ACTION_PROMPT:
			// if we are prompting on duplicate, we better check for one
			if( !_contacts.containsKey( name ) )
				return true;

			// ok, it exists, so do prompt
			_doit._handler.sendMessage( Message.obtain(
					_doit._handler, Doit.MESSAGE_MERGEPROMPT, name ) );
			try {
				wait();
			}
			catch( InterruptedException e ) { }

			// check if an abortion happened during the wait
			checkAbort();

			// if "always" was selected, make choice permenant
			if( _responseExtra == RESPONSEEXTRA_ALWAYS )
				_mergeSetting = _response;

			// recurse, with out new merge setting
			return isImportRequired( name, _response );
		}

		// for all other cases (either overwriting or merging) we will need the
		// imported data
		return true;
	}

	protected void skipContact() throws AbortImportException
	{
		checkAbort();
		_doit._handler.sendEmptyMessage( Doit.MESSAGE_CONTACTSKIPPED );
	}

	protected void importContact( ContactData contact )
			throws AbortImportException
	{
		checkAbort();

//		if( !showContinue( "====[ IMPORTING ]====\n: " + contact._name ) )
//			finish( ACTION_ABORT );

		ContentValues values = new ContentValues();
		boolean uiInformed = false;

		// does contact exist already?
		Uri contactUri = null;
		Long id;
		if( ( id = (Long)_contacts.get( contact._name ) ) != null )
		{
			// should we skip this import altogether?
			if( _lastMergeDecision == Doit.ACTION_KEEP ) return;

			// get contact's URI
			contactUri = ContentUris.withAppendedId(
					Contacts.People.CONTENT_URI, id );

			// should we destroy the existing contact before importing?
			if( _lastMergeDecision == Doit.ACTION_OVERWRITE ) {
				_doit.getContentResolver().delete( contactUri, null, null );
				contactUri = null;

				// upate the UI
				_doit._handler.sendEmptyMessage( Doit.MESSAGE_CONTACTOVERWRITTEN );
				uiInformed = true;

				// update cache
				_contacts.remove( contact._name );
			}
		}

		// if we don't have a contact URI it is because the contact never
		// existed or because we deleted it
		if( contactUri == null )
		{
			// create a new contact
			values.put( Contacts.People.NAME, contact._name );
			contactUri = _doit.getContentResolver().insert(
					Contacts.People.CONTENT_URI, values );
			id = ContentUris.parseId( contactUri );
			if( id <= 0 ) return;	// shouldn't happen!

			// try to add them to the "My Contacts" group
			try {
				Contacts.People.addToMyContactsGroup(
					_doit.getContentResolver(), id );
			}
			catch( IllegalStateException e ) { }

			// update cache
			_contacts.put( contact._name, id );

			// update UI
			if( !uiInformed ) {
				_doit._handler.sendEmptyMessage( Doit.MESSAGE_CONTACTCREATED );
				uiInformed = true;
			}
		}

		// update UI
		if( !uiInformed )
			_doit._handler.sendEmptyMessage( Doit.MESSAGE_CONTACTMERGED );

		// import contact parts
		if( contact._phones != null )
			importContactPhones( contactUri, contact._phones );
		if( contact._emails != null )
			importContactEmails( contactUri, contact._emails );
	}

	private void importContactPhones( Uri contactUri,
			HashMap< String, ContactData.PhoneData > phones )
	{
		Long contactId = ContentUris.parseId( contactUri );
		Uri contactPhonesUri = Uri.withAppendedPath( contactUri,
				Contacts.People.Phones.CONTENT_DIRECTORY );
		Set< String > phonesKeys = phones.keySet();

		// add phone numbers
		Iterator< String > i = phonesKeys.iterator();
		while( i.hasNext() ) {
			ContactData.PhoneData phone = phones.get( i.next() );

			// we don't want to add this number if it's crap, or it already
			// exists (which would cause a duplicate to be created). We don't
			// take in to account the type when checking for duplicates. This is
			// intentional: types aren't really very reliable. We assume that
			// if the number exists at all, it doesn't need importing. Because
			// of this, we also can't update the cache (which we don't need to
			// anyway, so it's not a problem).
			String number = sanitisePhoneNumber( phone._number );
			if( number == null ) continue;
			HashSet< String > numbers = _contactNumbers.get( contactId );
			if( numbers != null && numbers.contains( number ) ) continue;

			// add phone number
			ContentValues values = new ContentValues();
			values.put( Contacts.Phones.TYPE, phone._type );
			values.put( Contacts.Phones.NUMBER, phone._number );
			if( phone._isPreferred ) values.put( Contacts.Phones.ISPRIMARY, 1 );
			_doit.getContentResolver().insert( contactPhonesUri, values );
		}

		// now add those phone numbers to the cache to prevent the addition of
		// duplicate data from another file
		i = phonesKeys.iterator();
		while( i.hasNext() ) {
			ContactData.PhoneData phone = phones.get( i.next() );

			String number = sanitisePhoneNumber( phone._number );
			if( number != null ) {
				HashSet< String > numbers = _contactNumbers.get( contactId );
				if( numbers == null ) {
					_contactNumbers.put( contactId, new HashSet< String >() );
					numbers = _contactNumbers.get( contactId );
				}
				numbers.add( number );
			}
		}
	}

	private void importContactEmails( Uri contactUri,
			HashMap< String, ContactData.EmailData > emails )
	{
		Long contactId = ContentUris.parseId( contactUri );
		Uri contactContactMethodsUri = Uri.withAppendedPath( contactUri,
				Contacts.People.ContactMethods.CONTENT_DIRECTORY );
		Set< String > emailsKeys = emails.keySet();

		// add email addresses
		Iterator< String > i = emailsKeys.iterator();
		while( i.hasNext() ) {
			ContactData.EmailData email = emails.get( i.next() );

			// like with phone numbers, we don't want to add this email address
			// if it exists already or we would introduce duplicates.
			String address = sanitiseEmailAddress( email.getAddress() );
			if( address == null ) continue;
			HashSet< String > addresses = _contactEmails.get( contactId );
			if( addresses != null && addresses.contains( address ) ) continue;

			// add phone number
			ContentValues values = new ContentValues();
			values.put( Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL );
			values.put( Contacts.ContactMethods.DATA, email.getAddress() );
			values.put( Contacts.ContactMethods.TYPE, email.getType() );
			if( email.isPreferred() )
				values.put( Contacts.ContactMethods.ISPRIMARY, 1 );
			_doit.getContentResolver().insert( contactContactMethodsUri,
					values );
		}

		// now add those email addresses to the cache to prevent the addition of
		// duplicate data from another file
		i = emailsKeys.iterator();
		while( i.hasNext() ) {
			ContactData.EmailData email = emails.get( i.next() );

			String address = sanitiseEmailAddress( email.getAddress() );
			if( address != null ) {
				HashSet< String > addresses = _contactEmails.get( contactId );
				if( addresses == null ) {
					_contactEmails.put( contactId, new HashSet< String >() );
					addresses = _contactEmails.get( contactId );
				}
				addresses.add( address );
			}
		}
	}

	synchronized protected void checkAbort() throws AbortImportException
	{
		if( _abort ) {
			// stop
			throw new AbortImportException();
		}
	}

	private void buildContactsCache() throws AbortImportException
	{
		// update UI
		setProgressMessage( R.string.doit_caching );

		String[] cols;
		Cursor cur;

		// init contacts caches
		_contacts = new HashMap< String, Long >();
		_contactNumbers = new HashMap< Long, HashSet< String > >();
		_contactEmails = new HashMap< Long, HashSet< String > >();

		// query and store map of contact names to ids
		cols = new String[] { Contacts.People._ID, Contacts.People.NAME };
		cur = _doit.managedQuery( Contacts.People.CONTENT_URI,
				cols, null, null, null);
		if( cur.moveToFirst() ) {
			int idCol = cur.getColumnIndex( Contacts.People._ID );
			int nameCol = cur.getColumnIndex( Contacts.People.NAME );
			do {
				_contacts.put( cur.getString( nameCol ), cur.getLong( idCol ) );
			} while( cur.moveToNext() );
		}

		// query and store map of contact ids to sets of phone numbers
		cols = new String[] { Contacts.Phones.PERSON_ID,
				Contacts.Phones.NUMBER };
		cur = _doit.managedQuery( Contacts.Phones.CONTENT_URI,
				cols, null, null, null);
		if( cur.moveToFirst() ) {
			int personIdCol = cur.getColumnIndex( Contacts.Phones.PERSON_ID );
			int numberCol = cur.getColumnIndex( Contacts.Phones.NUMBER );
			do {
				Long id = cur.getLong( personIdCol );
				String number = sanitisePhoneNumber(
						cur.getString( numberCol ) );
				if( number != null ) {
					HashSet< String > numbers = _contactNumbers.get( id );
					if( numbers == null ) {
						_contactNumbers.put( id, new HashSet< String >() );
						numbers = _contactNumbers.get( id );
					}
					numbers.add( number );
				}
			} while( cur.moveToNext() );
		}

		// query and store map of contact ids to sets of email addresses
		cols = new String[] { Contacts.ContactMethods.PERSON_ID,
				Contacts.ContactMethods.DATA };
		cur = _doit.managedQuery( Contacts.ContactMethods.CONTENT_URI,
				cols, Contacts.ContactMethods.KIND + " = ?",
				new String[] { "" + Contacts.KIND_EMAIL }, null );
		if( cur.moveToFirst() ) {
			int personIdCol = cur.getColumnIndex(
					Contacts.ContactMethods.PERSON_ID );
			int addressCol = cur.getColumnIndex(
					Contacts.ContactMethods.DATA );
			do {
				Long id = cur.getLong( personIdCol );
				String address = sanitiseEmailAddress(
						cur.getString( addressCol ) );
				if( address != null ) {
					HashSet< String > addresses = _contactEmails.get( id );
					if( addresses == null ) {
						_contactEmails.put( id, new HashSet< String >() );
						addresses = _contactEmails.get( id );
					}
					addresses.add( address );
				}
			} while( cur.moveToNext() );
		}
	}

	private String sanitisePhoneNumber( String number )
	{
		number = number.replaceAll( "[-\\(\\) ]", "" );
		Pattern p = Pattern.compile( "^[\\+0-9#*]+" );
		Matcher m = p.matcher( number );
		if( m.lookingAt() ) return m.group( 0 );
		return null;
	}

	private String sanitiseEmailAddress( String address )
	{
		address = address.trim();
		Pattern p = Pattern.compile(
				"^[^ @]+@[a-zA-Z]([-a-zA-Z0-9]*[a-zA-z0-9])?(\\.[a-zA-Z]([-a-zA-Z0-9]*[a-zA-z0-9])?)+$" );
		Matcher m = p.matcher( address );
		if( m.matches() ) {
			String[] bits = address.split( "@" );
			return bits[ 0 ] + "@" + bits[ 1 ].toLowerCase();
		}
		return null;
	}
}

/*
 * Doit.java
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Doit extends WizardActivity
{
	private final static int DIALOG_ERROR = 0;
	private final static int DIALOG_CONTINUEORABORT = 1;
	private final static int DIALOG_MERGEPROMPT = 2;

	public final static int MESSAGE_ALLDONE = 0;
	public final static int MESSAGE_ABORT = 1;
	public final static int MESSAGE_ERROR = 3;
	public final static int MESSAGE_CONTINUEORABORT = 4;
	public final static int MESSAGE_SETPROGRESSMESSAGE = 5;
	public final static int MESSAGE_SETMAXPROGRESS = 6;
	public final static int MESSAGE_SETTMPPROGRESS = 7;
	public final static int MESSAGE_SETPROGRESS = 8;
	public final static int MESSAGE_MERGEPROMPT = 9;
	public final static int MESSAGE_CONTACTOVERWRITTEN = 10;
	public final static int MESSAGE_CONTACTCREATED = 11;
	public final static int MESSAGE_CONTACTMERGED = 12;
	public final static int MESSAGE_CONTACTSKIPPED = 13;

	public final static int ACTION_PROMPT = 0;
	public final static int ACTION_KEEP = 1;
	public final static int ACTION_MERGE_MERGE = 2;
	public final static int ACTION_OVERWRITE = 3;

	public final static int NEXT_BEGIN = 0;
	public final static int NEXT_CLOSE = 1;

	private boolean _startedProgress;
	private int _maxProgress;
	private int _tmpProgress;
	private int _progress;
	protected String _dialogMessage;
	private Dialog _mergePromptDialog;
	private boolean _mergePromptAlwaysSelected;
	private int _nextAction;
	private int _currentDialogId;

	private int _countOverwrites;
	private int _countCreates;
	private int _countMerges;
	private int _countSkips;

	protected Importer _importer = null;

	public Handler _handler;

	public class DoitHandler extends Handler
	{
		@Override
		public void handleMessage( Message msg ) {
			switch( msg.what )
			{
			case MESSAGE_ALLDONE:
				( (TextView)findViewById( R.id.doit_alldone ) ).
					setVisibility( View.VISIBLE );
				( (Button)findViewById( R.id.back ) ).setEnabled( false );
				updateNext( NEXT_CLOSE );
				findViewById( R.id.doit_abort_disp ).setVisibility(
						View.GONE );
				break;
			case MESSAGE_ABORT:
				manualAbort();
				break;
			case MESSAGE_ERROR:
				_dialogMessage = (String)msg.obj;
				showDialog( DIALOG_ERROR );
				break;
			case MESSAGE_CONTINUEORABORT:
				_dialogMessage = (String)msg.obj;
				showDialog( DIALOG_CONTINUEORABORT );
				break;
			case MESSAGE_SETPROGRESSMESSAGE:
				( (TextView)findViewById( R.id.doit_percentage ) ).
						setText( (String)msg.obj );
				break;
			case MESSAGE_SETMAXPROGRESS:
				if( _maxProgress > 0 ) {
					if( _tmpProgress == _maxProgress - 1 )
						_tmpProgress = (Integer)msg.obj;
					if( _progress == _maxProgress - 1 )
						_progress = (Integer)msg.obj;
				}
				_maxProgress = (Integer)msg.obj;
				updateProgress();
				break;
			case MESSAGE_SETTMPPROGRESS:
				_tmpProgress = (Integer)msg.obj;
				updateProgress();
				break;
			case MESSAGE_SETPROGRESS:
				_startedProgress = true;
				_progress = (Integer)msg.obj;
				updateProgress();
				break;
			case MESSAGE_MERGEPROMPT:
				_dialogMessage = (String)msg.obj;
				showDialog( DIALOG_MERGEPROMPT );
				break;
			case MESSAGE_CONTACTOVERWRITTEN:
				_countOverwrites++;
				updateStats();
				break;
			case MESSAGE_CONTACTCREATED:
				_countCreates++;
				updateStats();
				break;
			case MESSAGE_CONTACTMERGED:
				_countMerges++;
				updateStats();
				break;
			case MESSAGE_CONTACTSKIPPED:
				_countSkips++;
				updateStats();
				break;
			default:
				super.handleMessage( msg );
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView( R.layout.doit );
		super.onCreate( savedInstanceState );

		// hide page 2
		( findViewById( R.id.doit_page_2 ) ).setVisibility( View.GONE );

		// set up abort button
		Button begin = (Button)findViewById( R.id.abort );
		begin.setOnClickListener( new View.OnClickListener() {
			public void onClick( View view ) {
				manualAbort();
			}
		} );

		_startedProgress = false;
		_maxProgress = 0;
		_tmpProgress = 0;
		_progress = 0;
		_handler = new DoitHandler();

		_countOverwrites = 0;
		_countCreates = 0;
		_countMerges = 0;
		_countSkips = 0;

		updateNext( NEXT_BEGIN );

		updateProgress();
		updateStats();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		// saving the state of an import sounds complicated! Lets just abort!
		if( _nextAction != NEXT_CLOSE )
			manualAbort( true );
	}

	@Override
	protected Dialog onCreateDialog( int id )
	{
		switch( id )
		{
		case DIALOG_ERROR:
			return new AlertDialog.Builder( this )
				.setIcon( R.drawable.alert_dialog_icon )
				.setTitle( R.string.error_title )
				.setMessage( "" )
				.setPositiveButton( R.string.error_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
							int whichButton)
						{
							Doit.this._importer.wake();
						}
					} )
				.setOnCancelListener( _dialogOnCancelListener )
				.create();
		case DIALOG_CONTINUEORABORT:
			return new AlertDialog.Builder( this )
				.setIcon( R.drawable.alert_dialog_icon )
				.setTitle( R.string.error_title )
				.setMessage( "" )
				.setPositiveButton( R.string.error_continue,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
							int whichButton)
						{
							Doit.this._importer.wake(
								Importer.RESPONSE_POSITIVE );
						}
					} )
				.setNegativeButton( R.string.error_abort,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
							int whichButton)
						{
							Doit.this._importer.wake(
								Importer.RESPONSE_NEGATIVE );
						}
					} )
				.setOnCancelListener( _dialogOnCancelListener )
				.create();
		case DIALOG_MERGEPROMPT:
			// custom layout in an AlertDialog
			LayoutInflater factory = LayoutInflater.from( this );
			final View dialogView = factory.inflate(
				R.layout.mergeprompt, null );
			( (CheckBox)dialogView.findViewById( R.id.mergeprompt_always ) ).
				setOnCheckedChangeListener(
					new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged( CompoundButton buttonView,
							boolean isChecked )
						{
							Doit.this._mergePromptAlwaysSelected = isChecked;
						}
					} );
			( (Button)dialogView.findViewById( R.id.merge_keep ) ).
				setOnClickListener( _mergePromptButtonListener );
			( (Button)dialogView.findViewById( R.id.merge_overwrite ) ).
				setOnClickListener( _mergePromptButtonListener );
			( (Button)dialogView.findViewById( R.id.merge_merge ) ).
				setOnClickListener( _mergePromptButtonListener );
			( (Button)dialogView.findViewById( R.id.abort ) ).
				setOnClickListener( _mergePromptButtonListener );
			_mergePromptAlwaysSelected = false;
			return new AlertDialog.Builder( this )
				.setIcon( R.drawable.alert_dialog_icon )
				.setTitle( R.string.mergeprompt_title )
				.setView( dialogView )
				.setOnCancelListener( _dialogOnCancelListener )
				.create();
		}
		return null;
	}

	private OnClickListener _mergePromptButtonListener = new OnClickListener() {
		public void onClick( View view )
		{
			// handle abort
			if( view.getId() == R.id.abort )
				manualAbort();

			// else, response (just check we haven't aborted already!)
			else if( Doit.this._importer != null ) {
				int responseExtra = _mergePromptAlwaysSelected?
						Importer.RESPONSEEXTRA_ALWAYS : Importer.RESPONSEEXTRA_NONE;
				Doit.this._importer.wake( convertIdToAction( view.getId() ),
						responseExtra );
			}

			// close dialog and free (don't keep a reference)
			Doit.this._mergePromptDialog.dismiss();
			Doit.this._mergePromptDialog = null;
		}
	};

	@Override
	protected void onNext()
	{
		Button next = (Button)findViewById( R.id.next );
		next.setEnabled( false );

		switch( _nextAction )
		{
		case NEXT_BEGIN:
			importContacts();
			break;
		case NEXT_CLOSE:
			setResult( RESULT_OK );
			finish();
			break;
		}
	}

	private void manualAbort()
	{
		manualAbort( false );
	}

	private void manualAbort( boolean showToasterPopup )
	{
		abortImport( showToasterPopup );

		updateNext( NEXT_CLOSE );
		( (Button)findViewById( R.id.back ) ).setEnabled( true );
		findViewById( R.id.doit_abort_disp ).setVisibility( View.GONE );
		( (TextView)findViewById( R.id.doit_aborted ) ).
			setVisibility( View.VISIBLE );
		( (TextView)findViewById( R.id.doit_alldone ) ).
			setVisibility( View.GONE );

		// close any open dialogs
		try {
			dismissDialog( _currentDialogId );
		}
		catch( Exception e ) {
		}
	}

	private void updateNext( int nextAction )
	{
		Button next = (Button)findViewById( R.id.next );
		switch( nextAction ) {
		case NEXT_BEGIN:	next.setText( R.string.doit_begin ); break;
		case NEXT_CLOSE:	next.setText( R.string.doit_close ); break;
		}
		next.setEnabled( true );
		_nextAction = nextAction;
	}

	public static int convertIdToAction( int id ) {
		switch( id ) {
		case R.id.merge_keep:		return ACTION_KEEP;
		case R.id.merge_merge:		return ACTION_MERGE_MERGE;
		case R.id.merge_overwrite:	return ACTION_OVERWRITE;
		default: return ACTION_PROMPT;
		}
	}

	public static int convertActionToId( int action ) {
		switch( action ) {
		case ACTION_KEEP:		return R.id.merge_keep;
		case ACTION_MERGE_MERGE:return R.id.merge_merge;
		case ACTION_OVERWRITE:	return R.id.merge_overwrite;
		default: return R.id.merge_prompt;
		}
	}

	private DialogInterface.OnCancelListener _dialogOnCancelListener =
			new DialogInterface.OnCancelListener() {
		public void onCancel( DialogInterface dialog ) {
			manualAbort();
		}
	};


	@Override
	protected void onActivityResult( int requestCode, int resultCode,
			Intent data )
	{
		// if we're cancelling, abort any import
		if( resultCode == RESULT_CANCELED )
			abortImport( true );
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		_currentDialogId = id;

		switch( id )
		{
		case DIALOG_ERROR:	// fall through
		case DIALOG_CONTINUEORABORT:
			// set dialog message
			( (AlertDialog)dialog ).setMessage( _dialogMessage );
			break;
		case DIALOG_MERGEPROMPT:
			// set contact's name
			( (TextView)dialog.findViewById( R.id.mergeprompt_name ) ).setText(
					_dialogMessage );
			// and set up reference to dialog
			_mergePromptDialog = dialog;
			break;
		}

		super.onPrepareDialog( id, dialog );
	}

	private void importContacts()
	{
		// switch interfaces
		( findViewById( R.id.doit_page_1 ) ).setVisibility( View.GONE );
		( findViewById( R.id.doit_page_2 ) ).setVisibility( View.VISIBLE );

		// disable back button
		( (Button)findViewById( R.id.back ) ).setEnabled( false );

		// create importer
		_importer = new VCFImporter( this );

		// start the service's thread
		_importer.start();
	}

	private void updateProgress()
	{
		ProgressBar bar = (ProgressBar)findViewById( R.id.doit_progress );
		TextView outOf = (TextView)findViewById( R.id.doit_outof );

		if( _maxProgress > 0 )
		{
			bar.setMax( _maxProgress );
			bar.setSecondaryProgress( _tmpProgress );

			if( _startedProgress )
			{
				( (TextView)findViewById( R.id.doit_percentage ) ).setText(
						(int)Math.round( 100 * _progress / _maxProgress ) + "%" );
				outOf.setText( _progress + "/" + _maxProgress );
				bar.setProgress( _progress );
			}
		}
	}

	private void updateStats()
	{
		( (TextView)findViewById( R.id.doit_overwrites ) ).setText(
				"" + _countOverwrites );
		( (TextView)findViewById( R.id.doit_creates ) ).setText(
				"" + _countCreates );
		( (TextView)findViewById( R.id.doit_merges ) ).setText(
				"" + _countMerges );
		( (TextView)findViewById( R.id.doit_skips ) ).setText(
				"" + _countSkips );
	}

	private void abortImport( boolean showToasterPopup )
	{
		if( _importer != null )
		{
			// try and flag worker thread - did we need to?
			if( _importer.setAbort() )
			{
				// wait for worker thread to end
				while( true ) {
					try {
						_importer.join();
						break;
					}
					catch( InterruptedException e ) {}
				}

				// notify the user
				if( showToasterPopup )
					Toast.makeText( this, R.string.doit_importaborted,
							Toast.LENGTH_LONG ).show();
			}
		}

		// destroy some stuff
		_importer = null;
		_handler = null;
	}
}

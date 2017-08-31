/*
 * Copyright 2009 Brian Pellin.
 *     
 * This file is part of KeePassDroid.
 *
 *  KeePassDroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.keepass.R;
import com.keepassdroid.app.App;
import com.keepassdroid.database.edit.FileOnFinish;
import com.keepassdroid.database.edit.OnFinish;
import com.keepassdroid.database.edit.SetPassword;

public class SetPasswordDialog extends CancelDialog {

	private byte[] masterKey;
	private String mKeyfile;
	private FileOnFinish mFinish;
		
	public SetPasswordDialog(Context context) {
		super(context);
	}
	
	public SetPasswordDialog(Context context, FileOnFinish finish) {
		super(context);
		
		mFinish = finish;
	}
	
	public byte[] getKey() {
		return masterKey;
	}
	
	public String keyfile() {
		return mKeyfile;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_password);
		
		setTitle(R.string.password_title);
		
		// Ok button
		Button okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				TextView passView = (TextView) findViewById(R.id.pass_password);
				String pass = passView.getText().toString();
				TextView passConfView = (TextView) findViewById(R.id.pass_conf_password);
				String confpass = passConfView.getText().toString();
				
				// Verify that passwords match
				if ( ! pass.equals(confpass) ) {
					// Passwords do not match
					Toast.makeText(getContext(), R.string.error_pass_match, Toast.LENGTH_LONG).show();
					return;
				}
				
				TextView keyfileView = (TextView) findViewById(R.id.pass_keyfile);
				String keyfile = keyfileView.getText().toString();
				mKeyfile = keyfile;
				
				// Verify that a password or keyfile is set
				if ( pass.length() == 0 && keyfile.length() == 0 ) {
					Toast.makeText(getContext(), R.string.error_nopass, Toast.LENGTH_LONG).show();
					return;
					
				}
				
				SetPassword sp = new SetPassword(App.getDB(), pass, keyfile, new AfterSave(mFinish, new Handler()));
				ProgressTask pt = new ProgressTask(getContext(), sp, R.string.saving_database);
				pt.run();
			}
			
		});
		
		// Cancel button
		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				cancel();
				if ( mFinish != null ) {
					mFinish.run();
				}
			}
		});
	}

	private class AfterSave extends OnFinish {
		private FileOnFinish mFinish;
		
		public AfterSave(FileOnFinish finish, Handler handler) {
			super(finish, handler);
			mFinish = finish;
		}

		@Override
		public void run() {
			if ( mSuccess ) {
				if ( mFinish != null ) {
					mFinish.setFilename(mKeyfile);
				}
				dismiss();
			} else {
				displayMessage(getContext());
			}
			
			super.run();
		}

	}

}

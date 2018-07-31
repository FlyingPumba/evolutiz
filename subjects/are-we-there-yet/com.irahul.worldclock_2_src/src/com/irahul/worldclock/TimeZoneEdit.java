/*
 * Copyright (C) 2012 Rahul Agarwal
 *
 * This file is part of the World Clock
 * World Clock is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * World Clock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with World Clock.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.irahul.worldclock;

import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to add a timezone or edit an existing one
 * 
 * @author rahul
 * 
 */
public class TimeZoneEdit extends Activity {
	private static final String TAG = TimeZoneEdit.class.getName();
	private static final int DIALOG_TIMEZONE_LIST = 0;
	
	private WorldClockTimeZone selectedTimeZone = null;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timezone_edit);
		
		//button that brings up dialog with timezone list
		Button buttonTimeZoneList = (Button)findViewById(R.id.button_timezone_edit_list);
		buttonTimeZoneList.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				showDialog(DIALOG_TIMEZONE_LIST);				
			}
		});
		

		// pick mode - add or edit
		final Intent intent = getIntent();

		final String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			TextView title = (TextView) findViewById(R.id.timezone_edit_title);
			title.setText(getString(R.string.title_timezone_edit));

			// editing zone info
			Log.d(TAG, "EDIT tz="+ intent.getStringExtra(WorldClockActivity.INTENT_TZ_ID_IN));
			Log.d(TAG, "EDTI display="+ intent.getStringExtra(WorldClockActivity.INTENT_TZ_DISPLAYNAME_IN));
			
			//pre-select timezone
			this.selectedTimeZone = new WorldClockTimeZone(TimeZone.getTimeZone(intent.getStringExtra(WorldClockActivity.INTENT_TZ_ID_IN)));
			buttonTimeZoneList.setText(this.selectedTimeZone.getDefaultDisplay());			

			// pre-select displayname
			EditText displayName = (EditText) findViewById(R.id.timezone_edit_displayname);
			displayName.setText(intent.getStringExtra(WorldClockActivity.INTENT_TZ_DISPLAYNAME_IN));

		} else if (Intent.ACTION_INSERT.equals(action)) {
			TextView title = (TextView) findViewById(R.id.timezone_edit_title);
			title.setText(getString(R.string.title_timezone_add));
			//bring up timezone select dialog on start
			showDialog(DIALOG_TIMEZONE_LIST);
		} else {
			// unrecognized action - should never get here
			throw new WorldClockException("Unexpected intent received" + intent);
		}

		//save button action
		Button saveButton = (Button) findViewById(R.id.timezone_edit_save);
		saveButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				EditText displayName = (EditText) findViewById(R.id.timezone_edit_displayname);
				
				if(selectedTimeZone==null || displayName.getText().toString()==null || displayName.getText().toString().length()==0){
					Toast.makeText(getApplicationContext(), R.string.error_must_pick_zone_name, Toast.LENGTH_SHORT).show();
				}
				else{
					intent.putExtra(WorldClockActivity.INTENT_TZ_ID_OUT, selectedTimeZone.getId());
					intent.putExtra(WorldClockActivity.INTENT_TZ_DISPLAYNAME_OUT, displayName.getText().toString());
					setResult(RESULT_OK, intent);	
					finish();
				}
			}
		});

		//cancel button action
		Button buttonCancel = (Button) findViewById(R.id.timezone_edit_cancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}
	
	private void dialogItemSelected(WorldClockTimeZone selectedItem){
		//update display to selected value
		this.selectedTimeZone=selectedItem;
		Button buttonTimeZoneList = (Button)findViewById(R.id.button_timezone_edit_list);
		buttonTimeZoneList.setText(selectedItem.getDefaultDisplay());
		
		//if adding zone update the custom name with default
		if (Intent.ACTION_INSERT.equals(getIntent().getAction())){
			EditText displayName = (EditText) findViewById(R.id.timezone_edit_displayname);
			displayName.setText(selectedItem.getDefaultDisplay());
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog dialog;
	    switch(dialogId) {
	    case DIALOG_TIMEZONE_LIST:
	    	LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	View dialogView = li.inflate(R.layout.timezone_edit_dialog_list, null);	    		    	
			
			//setup list with timezone and enable filtering
			ListView dialogList = (ListView)dialogView.findViewById(R.id.dialog_list_view);
			final ArrayAdapter<WorldClockTimeZone> adapter = new TimeZoneEditDialogListAdapter(this, CountryTimeZone.getSupportedTimezones());			
			dialogList.setAdapter(adapter);			
			dialogList.setTextFilterEnabled(true);
			dialogList.setFastScrollEnabled(true);
			dialogList.setOnItemClickListener(new OnItemClickListener() {			
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {										
					ListView listView = (ListView)parent;
					TimeZoneEditDialogListAdapter adapter = (TimeZoneEditDialogListAdapter)listView.getAdapter();
					WorldClockTimeZone selectedItem = adapter.getItem(position);					
					dialogItemSelected(selectedItem);
					dismissDialog(DIALOG_TIMEZONE_LIST);
				}
			});
		    
			//Search box that is hooked up to the list
			EditText filterText = (EditText) dialogView.findViewById(R.id.dialog_filter_text);
		    filterText.addTextChangedListener(new TextWatcher() {				
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					//do nothing					
				}
				
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					//do nothing
					
				}
				
				public void afterTextChanged(Editable s) {
					//update adapter data -see Filter implementation
					adapter.getFilter().filter(s);
					adapter.notifyDataSetChanged();					
				}
			});
		    
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setView(dialogView);
	    	dialog = builder.create();
		    
	        break;	    
	    default:
	        throw new WorldClockException("Unknown dialog -should never happen");
	    }
	    return dialog;
	}
}

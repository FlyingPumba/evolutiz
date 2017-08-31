// Copyright (C) 2011 Free Software Foundation FSF
//
// This file is part of Addi.
//
// Addi is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or (at
// your option) any later version.
//
// Addi is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Addi. If not, see <http://www.gnu.org/licenses/>.

package com.addi;

import com.addi.R;

import android.view.KeyEvent;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AddiBase extends Activity {

	public  EditTextExtend _mCmdEditText;
	public KeyboardViewExtend _myKeyboardView;
	private CandidateView _mCandidateView;
	private LinearLayout _mainLayout;
	public SharedPreferences _sharedPrefs;
	
	int _suggestionCursorPos = 0;
	boolean _suggestionTaken = false;
	boolean _backUpOne = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		/*support multi language pack (Auto select) */
		Resources res = getResources();
		Configuration conf =res.getConfiguration();
		DisplayMetrics dm=res.getDisplayMetrics();
		res.updateConfiguration(conf, dm);
		
		super.onCreate(savedInstanceState); 

		_mCmdEditText = (EditTextExtend)findViewById(R.id.edit_command);
		_myKeyboardView = (KeyboardViewExtend)findViewById(R.id.keyboard);
		_mCandidateView = (CandidateView)findViewById(R.id.candidate);
		_mainLayout = (LinearLayout)findViewById(R.id.wrapView);
		_sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		_mCmdEditText.setInputType(InputType.TYPE_NULL);

		_mCmdEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_sharedPrefs.getBoolean("enable_custom_keyboard", true)) {
					_mCmdEditText._isTextEditorReturn = true;
					enableKeyboardVisibility();
				}
			}
		});
		
		_mCmdEditText.setOnTouchListener(new OnTouchListener() {
			@Override 
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				_mCmdEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
				if (_sharedPrefs.getBoolean("enable_custom_keyboard", true)) {
					_mCmdEditText._isTextEditorReturn = false;
				}
				return false;
			}
		});
		
		_mCmdEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                            
            }

            @Override
			public void afterTextChanged(Editable arg0) { 
            	if (_suggestionTaken == false) {
            		updateSuggestions();
            	}
            	if (_suggestionTaken == true) {
            		_mCmdEditText.setSelection(_suggestionCursorPos, _suggestionCursorPos);
            	}
            	_suggestionTaken = false;
            	if (_backUpOne == true) {
            		int start = _mCmdEditText.getSelectionStart();
            		if (start > 0) {
            			_mCmdEditText.setSelection(start-1, start-1);
            		}
            	}
            	_backUpOne = false;
            	_mCmdEditText._prevPos = _mCmdEditText.getSelectionStart();
			}
        });

	}

	public void handleBackspace() {
		int start = _mCmdEditText.getSelectionStart();
		int end = _mCmdEditText.getSelectionEnd();
		String textToInsert = "";
		if (start != end) {
			_mCmdEditText.getText().replace(Math.min(start, end), Math.max(start, end), textToInsert, 0, textToInsert.length());
		} else if (start != 0) {
			_mCmdEditText.getText().replace(start-1, start, textToInsert, 0, textToInsert.length());
		}
	}
	
	public void handleEnter() {
	}
	
	public void sendSuggestionText(String textToInsert) {
		int start = _mCmdEditText.getSelectionStart();
		_mCandidateView.clear();
		
		Character tempChar;
		int reverse;
		int forward;
		//scan forwards and backwards and find the full word, then update suggestions
		for (reverse = start-1; reverse >= 0; reverse--) {
			tempChar = _mCmdEditText.getText().toString().charAt(reverse);
			if (Character.isLetter(tempChar) || Character.isDigit(tempChar) || (tempChar == '_')) {
				continue;
			} else {
				reverse++;
				break;
			}
		}
		if (reverse < 0) {
			reverse = 0;
		}
		for (forward = start; forward < _mCmdEditText.getText().toString().length(); forward++) {
			tempChar = _mCmdEditText.getText().toString().charAt(forward);
			if (Character.isLetter(tempChar) || Character.isDigit(tempChar) || (tempChar == '_')) {
				continue;
			} else {
				break;
			}
		}
		if (forward > _mCmdEditText.getText().toString().length()) {
			forward = _mCmdEditText.getText().toString().length() - 1;
		} 
		if (forward < 0) {
			forward = 0;
		}
		if (textToInsert.endsWith("()") || textToInsert.endsWith("[]")) {
			_suggestionCursorPos = reverse + textToInsert.length() - 1;
		} else {
			_suggestionCursorPos = reverse + textToInsert.length();
		}
		_suggestionTaken = true;
		_mCmdEditText.getText().replace(reverse, forward, textToInsert, 0, textToInsert.length());
		
	}

	public void sendText(String textToInsert) {
		int start = _mCmdEditText.getSelectionStart();
		int end = _mCmdEditText.getSelectionEnd();
		_mCmdEditText.getText().replace(Math.min(start, end), Math.max(start, end),
				textToInsert, 0, textToInsert.length());
	}
	
	public void updateSuggestions() {
		int start = _mCmdEditText.getSelectionStart();
		int end = _mCmdEditText.getSelectionEnd();
		
		if (start != end) {
			_mCandidateView.clear();
			return;
		}
		
		Character tempChar;
		int reverse;
		int forward;
		//scan forwards and backwards and find the full word, then update suggestions
		for (reverse = start-1; reverse >= 0; reverse--) {
			tempChar = _mCmdEditText.getText().toString().charAt(reverse);
			if (Character.isLetter(tempChar) || Character.isDigit(tempChar) || (tempChar == '_')) {
				continue;
			} else {
				reverse++;
				break;
			}
		}
		if (reverse < 0) {
			reverse = 0;
		}
		for (forward = start; forward < _mCmdEditText.getText().toString().length(); forward++) {
			tempChar = _mCmdEditText.getText().toString().charAt(forward);
			if (Character.isLetter(tempChar) || Character.isDigit(tempChar) || (tempChar == '_')) {
				continue;
			} else {
				break;
			}
		}
		if (forward > _mCmdEditText.getText().toString().length()) {
			forward = _mCmdEditText.getText().toString().length() - 1;
		} 
		if (forward < 0) {
			forward = 0;
		}
		_mCandidateView.updateSuggestions(_mCmdEditText.getText().toString().substring(reverse, forward),true,true);
	}

	public void enableKeyboardVisibility() {    
		int visibility = _myKeyboardView.getVisibility();  
		switch (visibility) {    
		case View.GONE:  
		case View.INVISIBLE:  
			_myKeyboardView.setVisibility(View.VISIBLE);  
			break;  
		}  
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{ 
		super.onConfigurationChanged(newConfig);
		Keyboard tempKeyboard;
		
		int visibility = _myKeyboardView.getVisibility();
		tempKeyboard = _myKeyboardView.getKeyboard();
		
		if (_myKeyboardView != null) _mainLayout.removeView(_myKeyboardView);
		_myKeyboardView = new KeyboardViewExtend(this);
		_myKeyboardView.setId(R.id.keyboard);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		_mainLayout.addView(_myKeyboardView, lp);
		_myKeyboardView.setKeyboard(tempKeyboard);
		_myKeyboardView.setVisibility(visibility);
		_myKeyboardView.myOnConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			int visibility = _myKeyboardView.getVisibility();
			if (visibility == View.VISIBLE) {
				_myKeyboardView.setVisibility(View.GONE);
				return true;
			}
			handleBackButton();
			return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		int visibility = _myKeyboardView.getVisibility();
		if (visibility == View.VISIBLE) {
			_myKeyboardView.setVisibility(View.GONE); 
		} else {
			handleBackButton();
			finish();
			System.exit(0);
		}
		return;
	}
	
	public void handleBackButton() {
	}
	
	void swipeUp() {
		_mCmdEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
	}
	
	void swipeDown() {
		_mCmdEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
	}
	
	void swipeLeft() {
		_mCmdEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
	}
	
	void swipeRight() {
		_mCmdEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
	}

}
// Copyright 2008 Brock M. Tice
/*  This file is part of JustSit.

    JustSit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JustSit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JustSit.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.brocktice.JustSit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;

public class JsSettings extends Activity {
	private CheckBox mAirplaneMode;
	private CheckBox mScreenOn;
	private CheckBox mSilentMode;
	private CheckBox mMaximizeVolume;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		mAirplaneMode = (CheckBox) findViewById(R.id.airplane_mode_checkbox);
		mScreenOn = (CheckBox) findViewById(R.id.screen_on_checkbox);
		mSilentMode = (CheckBox) findViewById(R.id.silent_mode_checkbox);
		mMaximizeVolume = (CheckBox) findViewById(R.id.maximize_volume_checkbox);
		
		SharedPreferences settings = getSharedPreferences(JustSit.PREFS_NAME, MODE_PRIVATE);

        mAirplaneMode.setChecked(settings.getBoolean(JustSit.AIRPLANE_MODE, false));
        mScreenOn.setChecked(settings.getBoolean(JustSit.SCREEN_ON, false));
        mSilentMode.setChecked(settings.getBoolean(JustSit.SILENT_MODE, false));
        mMaximizeVolume.setChecked(settings.getBoolean(JustSit.MAXIMIZE_VOLUME, false));
        
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences settings = getSharedPreferences(JustSit.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(JustSit.AIRPLANE_MODE, mAirplaneMode.isChecked());
	    editor.putBoolean(JustSit.SCREEN_ON, mScreenOn.isChecked());
	    editor.putBoolean(JustSit.SILENT_MODE, mSilentMode.isChecked());
	    editor.putBoolean(JustSit.MAXIMIZE_VOLUME, mMaximizeVolume.isChecked());
	    editor.commit();
	}

}

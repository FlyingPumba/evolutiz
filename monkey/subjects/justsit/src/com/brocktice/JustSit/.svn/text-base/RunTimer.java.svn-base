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
import android.os.CountDownTimer;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class RunTimer extends Activity {
	private TextView mTimerView;
	private TextView mTimerLabel;
	private long mMillisLeft;
	private Bundle extras;
    private boolean mScreenOn;

	public static final String PREFS_NAME = "JustSitPreferences";
    private static final String TAG = "JustSit";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.run_timer);
		mTimerView = (TextView) findViewById(R.id.timer_view);
		mTimerLabel = (TextView) findViewById(R.id.timer_label);
		
		extras = (Bundle) getLastNonConfigurationInstance();
		if (extras == null) {
			extras = getIntent().getExtras();
		}
		if (extras != null) {
			int timer_label = extras.getInt(JustSit.TIMER_LABEL);
			long timer_duration = extras.getLong(JustSit.TIMER_DURATION);
			mTimerLabel.setText(timer_label);
			runCountdown(timer_duration);

		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		extras.putLong(JustSit.TIMER_DURATION, mMillisLeft);
	    return extras;
	}

	
	protected void runCountdown(long start_time){
		lockScreenOn();
		new CountDownTimer(start_time, 1000) {
			
            @Override
			public void onTick(long millisUntilFinished) {
                                  mTimerView.setText(Long.toString(millisUntilFinished / 1000));
                                  mMillisLeft = millisUntilFinished;
                                  if(JustSit.DEBUGLOGGING){
                                	  Log.w(TAG, "Tick with "+mMillisLeft/1000+"s remaining");
                                  }
            }

            @Override
			public void onFinish() {
            	setResult(JustSit.TIMER_COMPLETE);
            	finish();
            }
         }.start();
	}
	
	protected int calculateCountdownTime(int hours, int minutes){
		return (hours*3600 + minutes*60) * 1000;
	}
	
	public void lockScreenOn(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mScreenOn = settings.getBoolean(JustSit.SCREEN_ON, false);
		if(mScreenOn){
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	
}



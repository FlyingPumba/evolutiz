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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class JustSit extends Activity {
	public static final String PREFS_NAME = "JustSitPreferences";
	public static final String TIMER_LABEL = "timer_label";
	public static final String TIMER_DURATION = "timer_duration";
    public static final String PREP_SECONDS = "prepSeconds";
    public static final String MEDITATION_MINUTES = "meditationMinutes";
    public static final String AIRPLANE_MODE = "airplaneMode";
    public static final String SCREEN_ON = "screenOn";
    public static final String MAXIMIZE_VOLUME = "maximizeVolume";
	public static final String ORIG_AIRPLANE_MODE = "originalAirplaneMode";
	public static final String SILENT_MODE = "silentMode";
	public static final String ORIG_RINGER_MODE= "originalRingerMode";
	public static final String ORIG_MEDIA_VOLUME = "originalMediaVolume";
	public static final String PREFS_VERSION = "prefsVersion";
	public static final int CURRENT_PREFS_VERSION=2;
	public static final int TRUE=1;
	public static final int FALSE=0;
    private static final int ACTIVITY_PREP=0;
    private static final int ACTIVITY_MEDITATE=1;
    public static final int TIMER_COMPLETE = RESULT_FIRST_USER;
    private static final String TAG = "JustSit";
    public static final Boolean DEBUGLOGGING=false;
    
    private EditText mPrepText;
    private EditText mMeditateText;
    private ImageView mPrepUp;
    private ImageView mPrepDown;
    private ImageView mMeditateUp;
    private ImageView mMeditateDown;
	private MediaPlayer mMediaPlayer;
	private Boolean mScreenOn;
	private Boolean mAirplaneMode;
	private Boolean mSilentMode;
	private Boolean mMaximizeVolume;
	private PowerManager.WakeLock mWakeLock;
	private PowerManager.WakeLock mPartialWakeLock;
	private AudioManager mAudioManager;
	private Vibrator mVibrator;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Restore preferences
        mPrepText = (EditText) findViewById(R.id.preparation_text);
        mMeditateText = (EditText) findViewById(R.id.meditation_text);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        
        if(settings.getInt(PREFS_VERSION, 0) < CURRENT_PREFS_VERSION){
        	editor.clear();
        	editor.putInt(PREFS_VERSION, CURRENT_PREFS_VERSION);
        	editor.commit();
        }
       	setPrepTime(settings.getLong(PREP_SECONDS, 30));
       	setMeditateTime(settings.getLong(MEDITATION_MINUTES, 30));

       	updateMeditationSettings();
        
        mPrepUp = (ImageView) findViewById(R.id.prep_up_button);
        mPrepDown = (ImageView) findViewById(R.id.prep_down_button);
        mMeditateUp = (ImageView) findViewById(R.id.meditate_up_button);
        mMeditateDown = (ImageView) findViewById(R.id.meditate_down_button);
        mMediaPlayer = new MediaPlayer();
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        
        Button sitButton = (Button) findViewById(R.id.sit);
        sitButton.setOnClickListener(launchRunTimer);
        
        mPrepUp.setOnClickListener(new OnClickListener() {
        	  public void onClick(View v) {
	        	    incrementPrepTime();
        	  }
        	});
        
        mPrepDown.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		decrementPrepTime();
        	}
        });
        
        mMeditateUp.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		incrementMeditateTime();
        	}
        });
        
        mMeditateDown.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		decrementMeditateTime();
        	}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.justsit, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case R.id.settings:
            launchSettings();
            return true;
        case R.id.about:
            launchAbout();
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
    
    private void launchSettings(){
    	Intent i = new Intent(this, JsSettings.class);
    	startActivity(i);
    }
    
    private void launchAbout(){
    	Intent i = new Intent(this, JsAbout.class);
    	startActivity(i);
    }
    
    private long getPrepTime(){
    	long prepTime;
    	try{
    		prepTime = Long.parseLong(mPrepText.getText().toString());
    	}catch(NumberFormatException e){
    		Toast badFormatToast = Toast.makeText(this, R.string.invalid_prep_time, Toast.LENGTH_LONG);
    		badFormatToast.show();
    		prepTime = Long.parseLong(getString(R.string.default_prep_time));
    		mPrepText.setText(getString(R.string.default_prep_time));
    		Log.w(TAG, e.getMessage() + " Using default prep time");
    	}
    	return prepTime;
    }
    
    private long getMeditateTime(){
    	long meditateTime;
    	try{
    		meditateTime = Long.parseLong(mMeditateText.getText().toString());
    	}catch(NumberFormatException e){
    		Toast badFormatToast = Toast.makeText(this, R.string.invalid_meditation_time, Toast.LENGTH_LONG);
    		badFormatToast.show();
    		meditateTime = Long.parseLong(getString(R.string.default_meditation_time));
    		mMeditateText.setText(getString(R.string.default_meditation_time));
    		Log.w(TAG, e.getMessage() + " Using default meditation time");
    	}
    	return meditateTime;
    }
    
    private void setPrepTime(long time){
    	mPrepText.setText(Long.toString(time));	
    }
    
    private void setMeditateTime(long time){
    	mMeditateText.setText(Long.toString(time));
    }
    
    private void modifyPrepTime(long time){
    	setPrepTime(getPrepTime() + time);
    }
    
    private void modifyMeditateTime(long time){
    	setMeditateTime(getMeditateTime() + time);
    }
    
    private void incrementPrepTime(){
    	modifyPrepTime((long)1);
    }
    
    private void decrementPrepTime(){
    	modifyPrepTime((long)-1);
    }
    
    private void incrementMeditateTime(){
    	modifyMeditateTime((long)1);
    }
    
    private void decrementMeditateTime(){
    	modifyMeditateTime((long)-1);
    }
    
    private OnClickListener launchRunTimer = new OnClickListener(){
    	public void onClick(View v){
    		meditationSettings(true);
    		Intent i = new Intent(JustSit.this, RunTimer.class);
    		i.putExtra(TIMER_LABEL, R.string.prep_label);
    		i.putExtra(TIMER_DURATION, getPrepTime()*(long)1000);
    		startActivityForResult(i, ACTIVITY_PREP);
    	}
    };
    
	@Override
	protected void onPause() {
		super.onPause();
		mMediaPlayer = null;
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putLong(PREP_SECONDS, getPrepTime());
	    editor.putLong(MEDITATION_MINUTES, getMeditateTime());
	    editor.commit();

	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == TIMER_COMPLETE){
        	SharedPreferences settings = getSharedPreferences(JustSit.PREFS_NAME, 0);
    	    boolean silent = settings.getBoolean(JustSit.SILENT_MODE, false);

        	switch(requestCode) {
        	case ACTIVITY_PREP:
        		if(silent){
        			mVibrator.vibrate(1000);
        		}else{
        		    //setMediaVolume(true);
        			mMediaPlayer = MediaPlayer.create(this, R.raw.bong);
        			mMediaPlayer.setVolume(1, 1);
        			mMediaPlayer.start();
        		}
        		Intent i = new Intent(JustSit.this, RunTimer.class);
        		i.putExtra(TIMER_LABEL, R.string.meditate_label);
        		i.putExtra(TIMER_DURATION, getMeditateTime()*(long)60000);
        		startActivityForResult(i, ACTIVITY_MEDITATE);
        		break;
        	case ACTIVITY_MEDITATE:
        		meditationSettings(false);
        		if(silent){
        			mVibrator.vibrate(1000);
        		}else{
        			mMediaPlayer = null;
        			mMediaPlayer = MediaPlayer.create(this,R.raw.bong);
        			mMediaPlayer.setVolume(1, 1);
        			mMediaPlayer.setOnCompletionListener(
        					new OnCompletionListener(){
        						//@Override
        						private int plays = 0;
        						public void onCompletion(MediaPlayer mMp){
        							if(plays < 2){
        								mMp.seekTo(0);
        								mMp.start();
        								plays++;
        							}else{
        								//setMediaVolume(false);
        							}
        						}
        		
        					});
        			mMediaPlayer.start();
        		}
        		break;
        	}        
        }else if (resultCode == RESULT_OK){
        		// Do nothing
        }else{
        	if(mMediaPlayer != null){
        		mMediaPlayer.stop();
        	}
        	meditationSettings(false);
        	//setMediaVolume(false);
        }
        
	}
	
	protected void setAirplaneMode(boolean on){
		SharedPreferences settings = getSharedPreferences(JustSit.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    if(on){
	    	// Save the existing setting for when we revert
	    	try{
	    		editor.putInt(ORIG_AIRPLANE_MODE, 
	    				Settings.System.getInt(this.getContentResolver(), 
							Settings.System.AIRPLANE_MODE_ON));
	    		editor.commit();
    			}catch (SettingNotFoundException e){
    				Log.e(TAG, e.getMessage());
    			}
    		// Enable airplane mode
    		Settings.System.putInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, TRUE);
	    }else{
	    	// Revert to original setting
	    	Settings.System.putInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, settings.getInt(ORIG_AIRPLANE_MODE, FALSE));
	    	if(settings.getInt(ORIG_AIRPLANE_MODE, FALSE) == FALSE){
	    		Toast airplaneToast = Toast.makeText(this, R.string.airplane_mode_off, Toast.LENGTH_LONG);
	    		airplaneToast.show();
	    	}
	    }
	    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", on);
        this.sendBroadcast(intent);
	    
	}
	
	protected void setSilentMode(boolean on){
		SharedPreferences settings = getSharedPreferences(JustSit.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
		if(on){
			// Save existing setting
			editor.putInt(ORIG_RINGER_MODE, mAudioManager.getRingerMode());
			editor.commit();
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		}else{
			mAudioManager.setRingerMode(settings.getInt(ORIG_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL));
		}
	}
	
	protected void setScreenLock(boolean on){
		if(mWakeLock == null){
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
										PowerManager.ON_AFTER_RELEASE, TAG);
		}
		if(on){
		 mWakeLock.acquire();
		}else{
			if(mWakeLock.isHeld()){
				mWakeLock.release();
			}
		 mWakeLock = null;
		}

	}
	
	protected void setPartialWakeLock(boolean on){
		if(mPartialWakeLock == null){
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
		}
		if(on){
			mPartialWakeLock.acquire();
		}else{
			if(mPartialWakeLock.isHeld()){
				mPartialWakeLock.release();
			}
			mPartialWakeLock = null;
		}
	}
	
	protected void setMediaVolume(boolean on){
		SharedPreferences settings = getSharedPreferences(JustSit.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    if(on){
	    	// Save the existing setting for when we revert
	    	editor.putInt(ORIG_MEDIA_VOLUME, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
	    	editor.commit();
	    	mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
	    }else{
	    	mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, settings.getInt(ORIG_MEDIA_VOLUME, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)), 0);
	    }
		
	}
	
	protected void updateMeditationSettings(){
		SharedPreferences settings = getSharedPreferences(JustSit.PREFS_NAME, 0);
        mAirplaneMode = settings.getBoolean(AIRPLANE_MODE, false);
        mScreenOn = settings.getBoolean(SCREEN_ON, false);
        mSilentMode = settings.getBoolean(SILENT_MODE, false);
        mMaximizeVolume = settings.getBoolean(MAXIMIZE_VOLUME, false);
	}
	
	protected void meditationSettings(boolean on){
		updateMeditationSettings();
	    if(mAirplaneMode){
	    	setAirplaneMode(on);
	    }
	    
	    //if(mScreenOn){
	    	//setScreenLock(on);
	    //}else{
	    //}
	    //setPartialWakeLock(on);
	    //}
	    
	    if(mSilentMode){
	    	setSilentMode(on);
	    }
	 
	    if(mMaximizeVolume){
	    	setMediaVolume(on);
	    }
	}
	
	public void lockScreenOn(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mScreenOn = settings.getBoolean(JustSit.SCREEN_ON, false);
		if(mScreenOn){
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
}
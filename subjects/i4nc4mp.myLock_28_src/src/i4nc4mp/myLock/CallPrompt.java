package i4nc4mp.myLock;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.internal.telephony.ITelephony;

//I aint no dummy (prompt)


public class CallPrompt extends Activity {

	private boolean success = false;
	
	/**
     * TelephonyManager instance used by this activity
     */
    private TelephonyManager tm;
    
    /**
     * AIDL access to the telephony service process
     */
    private ITelephony telephonyService;
	
	public static void launch(Context mCon) {
		
		Intent prompt = new Intent(mCon,CallPrompt.class);

    	prompt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
    			| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
    	
    	mCon.startActivity(prompt);
	}
	
	//instead of having subclasses with an overridden layout definition
	//i just set the layout based on pref
	//in the android source they usually subclass to set a different layout/functionality
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// grab an instance of telephony manager
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        
        // connect to the underlying Android telephony system
        connectToTelephonyService();
		
		if (!getSharedPreferences("myLock", 0).getBoolean("callPrompt", true)) {
		//just the hint to user for camera accept and back to get to sliders
		//only camera can answer in this case
			
			setContentView(R.layout.cancelhint);
			
		}
		else if (!getSharedPreferences("myLock", 0).getBoolean("rejectEnabled", false)) {
		//regular answer only button
			
			setContentView(R.layout.answerprompt);
			
			Button answer = (Button) findViewById(R.id.answer);
			/*
			answer.setOnLongClickListener(new OnLongClickListener(){

				public boolean onLongClick(View arg0) {
					// TODO Auto-generated method stub
					answer();
					return false;
				}
        });*/
        
			answer.setOnClickListener(new OnClickListener() {
	          	public void onClick(View v){
	          		answer();
	          	}
			});
		}
		else {
		//2 button prompt
			setContentView(R.layout.main);
						
			Button answer = (Button) findViewById(R.id.answerbutton);
		
			answer.setOnClickListener(new OnClickListener() {
				public void onClick(View v){
					answer();
				}
			});
		
			Button reject = (Button) findViewById(R.id.rejectbutton);
		
			reject.setOnClickListener(new OnClickListener() {
				public void onClick(View v){
					reject();
				}
			});
		
		}
		
	}
	
	/**  For motion  based nav hardware -----
     * The optical nav handles as a trackball also (Incredible/ADR6300)
     * the motion is locked by this override, to stop conversion to dpad directional events
     * we allow the click to pass through, it comes to key event dispatch as dpad center
     */
    @Override public boolean dispatchTrackballEvent(MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_MOVE) return true;
            
            return super.dispatchTrackballEvent(event);
    }
    
    /** From Tedd's source 
     * http://code.google.com/p/teddsdroidtools/source/browse/
     * get an instance of ITelephony to talk handle calls with 
     */
    @SuppressWarnings("unchecked") private void connectToTelephonyService() {
            try 
            {
                    // "cheat" with Java reflection to gain access to TelephonyManager's ITelephony getter
                    Class c = Class.forName(tm.getClass().getName());
                    Method m = c.getDeclaredMethod("getITelephony");
                    m.setAccessible(true);
                    telephonyService = (ITelephony)m.invoke(tm);

            } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("call prompt","FATAL ERROR: could not connect to telephony subsystem");
                    Log.e("call prompt","Exception object: "+e);
                    finish();
            }               
    }
    
    /**
     * AIDL/ITelephony technique for answering the phone
     */
    private void answerCallAidl() {
            try {
                    telephonyService.silenceRinger();
                    telephonyService.answerRingingCall();
            } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e("call prompt","FATAL ERROR: call to service method answerRiningCall failed.");
                    Log.e("call prompt","Exception object: "+e);
            }               
    }
    
    /** 
     * AIDL/ITelephony technique for ignoring calls
     */
    private void ignoreCallAidl() {
            try 
            {
                    telephonyService.silenceRinger();
                    telephonyService.endCall();
            } 
            catch (RemoteException e) 
            {
                    e.printStackTrace();
                    Log.e("call prompt","FATAL ERROR: call to service method endCall failed.");
                    Log.e("call prompt","Exception object: "+e);
            }
    }
			
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(PhoneState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.v("call prompt","starting");
		
		IntentFilter ph = new IntentFilter (TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		
		registerReceiver(PhoneState, ph);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		Log.v("prompt onStop","verifying success");
		//We are sneaky.
		//We can relaunch if phone lagged in starting, so then tries to cancel our visible lifecycle
		if (!success) launch(getApplicationContext());
		
		//there is a bug where if you test this too fast after going home, it blocks the activity start
		//so far it only seems to affect service and receiver based activity starting
		//We have the issue reported on the android issue tracker
		//in the log it will be orange: "activity start request from (pid) stopped."
	}
	
	void answer() {
		success = true;
		
	//special thanks the auto answer open source app
	//which demonstrated this answering functionality
		//Intent answer = new Intent(Intent.ACTION_MEDIA_BUTTON);
  		//answer.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
  		//sendOrderedBroadcast(answer, null);
		
		//due to inconsistency, replaced with more reliable cheat method Tedd discovered
		answerCallAidl();
		
  		moveTaskToBack(true);
  		finish();
	}
	
	void reject() {
		success = true;
		
		ignoreCallAidl();
        
        moveTaskToBack(true);
  		finish();
	}
	
	//i think this isn't in 1.5, we're also using 2.0 service methods
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		success = true;
	}
	
	//we don't want to exist after phone changes to active state or goes back to idle
	//we also don't want to rely on this receiver to close us after success
	BroadcastReceiver PhoneState = new BroadcastReceiver() {
		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals("android.intent.action.PHONE_STATE")) return;
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				if (!success && !isFinishing()) {
					//no known intentional dismissal and not already finishing
					//need to finish to avoid handing out after missed calls
					Log.v("call start or return to idle","no user input success - closing the prompt");
					success = true;//so re-start won't fire
					finish();
				}
			}

			return;
	    		
		}};
	
	//let's allow the camera press to accept this call
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {			
		case KeyEvent.KEYCODE_FOCUS:
			return true;
			//this event occurs - if passed on, phone retakes focus
			//so let's consume it to avoid that outcome
		case KeyEvent.KEYCODE_CAMERA:
		case KeyEvent.KEYCODE_DPAD_CENTER://sent by trackball and optical nav click
			if (getSharedPreferences("myLock", 0).getBoolean("cameraAccept", false))
					answer();
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}
}
package i4nc4mp.myLock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

public class TouchGuard extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		if (Integer.parseInt(Build.VERSION.SDK) > 8) 
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		//gingerbread issues.. prompt doesnt seem to want to display without this.
		//they must have finally tightened up what the phone app is doing to interact with the keyguard
		//show when locked makes us lose access to input such as camera key so I use dismiss keyguard
		//need to test for any unexpected interaction.. but i don't have a nexus s or cm 7 yet
		
		setContentView(R.layout.callguard);
		
		IntentFilter ph = new IntentFilter (TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		
		registerReceiver(PhoneState, ph);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(PhoneState);
	}
	
	//ensure the normal behavior of finishing is ignored on back button
	//this ensures it can only be dismissed by camera/trackball presses or back long press when enabled by user pref
	@Override
	public void onBackPressed() {
		return;
	}
	
	//Some devices don't have a cam button.
	//Big ones now are nexus and incredible
	//have had some samsung users tell me there's no nav OR cam.. so we need to give them some workaround like a vol key long press
	//perhaps a back key long press could be the way to go
	/**  For motion  based nav hardware -----
     * The optical nav handles as a trackball also (Incredible/ADR6300)
     * the motion is locked by this override, to stop conversion to dpad directional events
     * we allow the click to pass through, it comes to key event dispatch as dpad center
     */
    @Override public boolean dispatchTrackballEvent(MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_MOVE) return true;
            
            return super.dispatchTrackballEvent(event);
    }
    
  //we don't want to exist after phone goes back to idle
	BroadcastReceiver PhoneState = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals("android.intent.action.PHONE_STATE")) return;
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				if (!isFinishing()) {
					//not already finishing & phone has gone back to idle meaning other end hung up
					//need to finish to avoid user having to dismiss it when they go to use the phone again
					Log.v("call return to idle","closing the touch lock");
					finish();
				}
			}

			return;
	    		
		}};
	
	//camera key is used to clear the touchscreen guard
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_FOCUS:
			return true;
			//consume since we want to get the 2nd stage press
		case KeyEvent.KEYCODE_CAMERA:
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_DEL://just for emulator testing
			moveTaskToBack(true);
			finish();
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        // this tells the framework to start tracking for
	        // a long press and eventual key up.  it will only
	        // do so if this is the first down (not a repeat).
	        event.startTracking();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        // a long press of the BACK key.
	        // do our work, returning true to consume it.  by
	        // returning true, the framework knows an action has
	        // been performed on the long press, so will set the
	        // canceled flag for the following up event.
	    	if (getSharedPreferences("myLock", 0).getBoolean("backUnlock", false)) {
	    	moveTaskToBack(true);
			finish();
	        return true;
	    	}
	    }
	    	return false;
	    }
	
}
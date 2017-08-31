package i4nc4mp.myLock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

//invoked by widget and plugin commands
//just checks state and calls through to the manage mediator class methods

public class Toggler extends Service {
	
	private boolean target;
	private boolean active;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
Log.v("Toggler","Starting");
		
		active = ManageMediator.bind(getApplicationContext());
		//FIXME still susceptible to the possible bug here that it thinks we are stopped
		//however it's not a big consequence here since outcome is a duplicate start
		//and the bind is created here
		//the error case should never happen now that we grab the bind at mediator first start
		
		target = intent.getBooleanExtra("i4nc4mp.myLock.TargetState", !active);
		
		Log.v("toggling","target is " + target + " and current state is " + active);
		
		//start if we've been told to start and did not already exist				
		if (target && !active) {
			ManageMediator.updateEnablePref(true, getApplicationContext());
			ManageMediator.startService(getApplicationContext());
    		Toast.makeText(Toggler.this, "myLock is now enabled", Toast.LENGTH_SHORT).show();
    		
		}//stop if we've been told to stop and did already exist
		else if (active && !target) {
				
				ManageMediator.stopService(getApplicationContext());
				Toast.makeText(Toggler.this, "myLock is now disabled", Toast.LENGTH_SHORT).show();
				
				ManageMediator.updateEnablePref(false, getApplicationContext());
		}//log the request - locale condition may send a desired state that already exists
		else Log.v("toggler","unhandled outcome - target was not a change");
		
		//added to prevent android "restarting" this after it dies/is purged causing unexpected toggle
		stopSelf();//close so it won't be sitting idle in the running services window
		return START_NOT_STICKY;//ensure it won't be restarted by the OS, we only want explicit starts
	}

/*
 * 
 * manual update broadcast the widget can get
ComponentName comp = new ComponentName(mCon.getPackageName(), ToggleWidget.class.getName());
Intent w = new Intent();

w.setComponent(comp);
w.setAction("android.appwidget.action.APPWIDGET_UPDATE");
w.putExtra("i4nc4mp.myLock.froyo.toggle", true);//so widget knows we manually told it to update the status
mCon.sendBroadcast(w);
*/

}
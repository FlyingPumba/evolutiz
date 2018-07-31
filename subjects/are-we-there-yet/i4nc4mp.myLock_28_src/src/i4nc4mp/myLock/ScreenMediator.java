package i4nc4mp.myLock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.util.Log;

//Whether to spawn the touch guard at screen off or screen on?
//for some reason we don't always get the broadcasts if prox sensor is causing them

public class ScreenMediator extends Service {
    
	//private SharedPreferences prefs;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
	//prefs = getSharedPreferences("myLockphone", 0);
		
	//register the receivers
	IntentFilter onfilter = new IntentFilter (Intent.ACTION_SCREEN_ON);
	IntentFilter offfilter = new IntentFilter (Intent.ACTION_SCREEN_OFF);
		
	registerReceiver(screenon, onfilter);
	registerReceiver (screenoff, offfilter);
	
	/*
	 * Listen so we can close when calls end (phone receiver already populates the state)
	prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener () {
    	@Override
    	public void onSharedPreferenceChanged (SharedPreferences sharedPreference, String key) {    		
      		if ("callActive".equals(key)) {
    			if (!prefs.getBoolean("callActive", false)) {
    				stopSelf();
    			}
    		}
    	}});*/
	
	return START_STICKY;
	}	
	
    BroadcastReceiver screenon = new BroadcastReceiver() {
		
		public static final String TAG = "screenon";
		public static final String Screen = "android.intent.action.SCREEN_ON";
		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(Screen)) return;
			
			Log.v(TAG, "Screen just went ON!");
			

			return;
	    		
		}};
	
	BroadcastReceiver screenoff = new BroadcastReceiver() {
        
        public static final String TAG = "screenoff";
        public static final String Screenoff = "android.intent.action.SCREEN_OFF";

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (!intent.getAction().equals(Screenoff)) return;
                
                Log.v(TAG, "Screen just went OFF");
                //boolean on = context.getSharedPreferences("myLockphone",0).getBoolean("touchLock", false);
        		//boolean call = context.getSharedPreferences("myLockphone",0).getBoolean("callActive", false);
        		
        		//if (!call || !on) return;
        		//Must be enabled and call active to proceed with touch guard
                
        		Class g = TouchGuard.class;
    			Intent lock = new Intent(context, g);
            	
            	lock.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            			| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            	//otherwise it would immediately stop vibration & sound
            	
            	context.startActivity(lock);
                return;
        }};
	
	
		
@Override
public void onDestroy() {
	super.onDestroy();	
	//destroy the receivers
	unregisterReceiver(screenon);
    unregisterReceiver(screenoff);
    
    //prefs = null;
    //prefs.unregisterOnSharedPreferenceChangeListener();
}
			
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
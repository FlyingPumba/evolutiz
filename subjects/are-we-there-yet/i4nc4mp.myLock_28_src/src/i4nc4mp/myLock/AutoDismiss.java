package i4nc4mp.myLock;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import i4nc4mp.myLock.ManageKeyguard.LaunchOnKeyguardExit;


//we mediate wakeup & call end, to fire dismiss activity if the lockscreen is detected

public class AutoDismiss extends MediatorService {
	private boolean persistent = false;
    
	private boolean oldmode = false;
	private boolean slideGuarded = false;
    
    private boolean slideWakeup = false;
  //we will set this when we detect slideopen, only used with instant unlock


    
    private boolean dismissed = false;
    //will just toggle true after dismiss callback - used to help ensure airtight lifecycle
    
    private boolean callmissed = false;
    
    Handler serviceHandler;
    Task myTask = new Task();
    
    
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
            
        SharedPreferences settings = getSharedPreferences("myLock", 0);       

            
            	unregisterReceiver(lockStopped);
                            	
            	settings.unregisterOnSharedPreferenceChangeListener(prefslisten);
                               
                serviceHandler.removeCallbacks(myTask);
                serviceHandler = null;
                
                
             
                
}
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	if (!getSharedPreferences("myLock",0).getBoolean("startingUp", false)) {
    		Log.v("system restart","apparent low mem system restart, toggling back on");
    		ManageMediator.updateEnablePref(true, getApplicationContext());
			ManageMediator.startService(getApplicationContext());
    		Toast.makeText(AutoDismiss.this, "myLock restarted after system low mem shutdown", Toast.LENGTH_SHORT).show();	
    	}
    	else Log.v("normal oncreate","commencing first start call");
    	
    }
    
    @Override
    public void onFirstStart() {
    	
    	//first acquire the prefs that need to be initialized
            SharedPreferences settings = getSharedPreferences("myLock", 0);
            SharedPreferences.Editor editor = settings.edit();
            
            persistent = settings.getBoolean("FG", false);
            
            slideGuarded = settings.getBoolean("slideGuard", false);
                       
            oldmode = settings.getBoolean("oldmode", false);
            
            if (persistent) doFGstart();
            
            
            //register a listener to update this if pref is changed to 0
            settings.registerOnSharedPreferenceChangeListener(prefslisten);
            	
            serviceHandler = new Handler();
            
            IntentFilter lockStop = new IntentFilter ("i4nc4mp.myLock.lifecycle.LOCKSCREEN_EXITED");
            registerReceiver(lockStopped, lockStop);
            
            editor.putBoolean("startingUp",false);
            editor.commit();
    }
    
    
    SharedPreferences.OnSharedPreferenceChangeListener prefslisten = new OnSharedPreferenceChangeListener () {
    	
    	public void onSharedPreferenceChanged (SharedPreferences sharedPreference, String key) {
    		Log.v("pref change","the changed key is " + key);
    		
      		if ("FG".equals(key)) {
    			boolean fgpref = sharedPreference.getBoolean(key, false);
    			if(!fgpref && persistent) {
    				stopForeground(true);//kills the ongoing notif
    			    persistent = false;
    			}
    			else if (fgpref && !persistent) doFGstart();//so FG mode is started again
      		}
      		
    		if ("slideGuard".equals(key)) slideGuarded = sharedPreference.getBoolean(key, false);
    		if ("oldmode".equals(key)) oldmode = sharedPreference.getBoolean(key, false);
    		}
    	};
    
    BroadcastReceiver lockStopped = new BroadcastReceiver() {
        @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("i4nc4mp.myLock.lifecycle.LOCKSCREEN_EXITED")) return;
        
        //couldn't get any other method to avoid the KG from shutting screen back off
        //when dismiss activity sent itself to back
        //it would ignore all user activity pokes and log "ignoring user activity while turning off screen"
        
        if (!slideWakeup) {
        	dismissed = true;
        	//gingerbread test, we are not using wake lock
        	//if (Integer.parseInt(Build.VERSION.SDK) < 9)
        	ManageWakeLock.releaseFull();
            	
        }
        else Log.v("dismiss callback","waiting for 5 sec to finalize due to slide wake");
        
        return;
        }};
        
        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            //if (!slideGuarded) return;
            
            
            if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
                    //this means that a config change happened and the keyboard is open.     
            	if(!dismissed) {
            		Log.v("slider wake event","setting state flag, screen state is " + isScreenOn());
            		slideWakeup = true;    
            	}
            	else Log.v("slider event","Ignoring since already dismissed");
            }
            else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            	Log.v("slide closed","mediator got the config change from background");
            }          
        }
        
        
        class Task implements Runnable {
            public void run() {
            	//when the slide wake is set to dismiss, we will keep the wakelock for 5 sec
            	//to avoid the bug of screen falling out when the CPU gets through the process too fast
            	if (!oldmode && !dismissed) {
            		ManageWakeLock.releaseFull();
            		dismissed = true;
            	}
            	if (oldmode) {
            		//finalize the kg exit
            		ManageKeyguard.exitKeyguardSecurely(new LaunchOnKeyguardExit() {
                        public void LaunchOnKeyguardExitSuccess() {
                           Log.v("start", "This is the exit callback");
                            }});
                            }
            	}
                           
        }
    
        public boolean isScreenOn() {
        	//Allows us to tap into the 2.1 screen check if available
        	
        	if(Integer.parseInt(Build.VERSION.SDK) < 7) { 
        		
        		return IsAwake();
        		//this comes from mediator superclass, checking the bool set by screen on/off broadcasts
        		//it is unreliable in phone calls when prox sensor is changing screen state
        		
        	}
        	else {
        		PowerManager myPM = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        		return myPM.isScreenOn();
        		//unlike following the broadcasts this one is accurate. 
        		//most people have 2.1 now so it should be a non-issue
        	}
        }
        
    @Override
    public void onScreenWakeup() {    	
    	
    	
    	//now check for call state flags
    	if (receivingcall || placingcall || callmissed) {
    		Log.v("auto dismiss service","aborting screen wake handling due to call state");
    		if (callmissed) callmissed = false;
    		return;
    	}    	
    	//this event happens at the ignore/miss due to the lockscreen appearing
    	//it is actually a bug in the lockscreen that sends the screen on when it was already on
    	
    	if (slideGuarded && slideWakeup) return;
    	//no dismiss when slide guard active
    	
    	//now let's see if the KG is even up
    	ManageKeyguard.initialize(getApplicationContext());
    	boolean KG = ManageKeyguard.inKeyguardRestrictedInputMode();
    	
    	if (KG) {
    		if (!oldmode) StartDismiss(getApplicationContext());
    		else {
    			ManageKeyguard.disableKeyguard(getApplicationContext());
                serviceHandler.postDelayed(myTask, 50L);
    		}
    	}
        
    	return;
    }
    
    @Override
    public void onScreenSleep() {
        
        dismissed = false;//flag will allow us to know we are coming into a slide wakeup
        callmissed = false;//just in case we didn't get the bad screen on after call is missed
        
        if (slideWakeup) {
        	Log.v("back to sleep","turning off slideWakeup");
            slideWakeup = false;
        }
        
       
       
    }
    
    public void StartDismiss(Context context) {
            
    	//PowerManager myPM = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        //myPM.userActivity(SystemClock.uptimeMillis(), true);
    	
    	//try not using wakelock on gingerbread to see if issue with the lock handoff is fixed
    	//if (Integer.parseInt(Build.VERSION.SDK) < 9) {
    	ManageWakeLock.acquireFull(getApplicationContext());
    	
    	
    	if (slideWakeup) serviceHandler.postDelayed(myTask, 5000L);
    	//when dismissing from slide wake we set a 5 sec wait for release of the wake lock
    	
    //}
    	
    	//what we should do here is launch a 5 sec wait that releases it also
    	//sometimes dismiss doesn't stop/destroy right away if no user action (ie pocket wake)
    	//so release it after 5 seconds
    	
    Class w = AutoDismissActivity.class; 
                  
    Intent dismiss = new Intent(context, w);
    dismiss.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK//required for a service to launch activity
                    | Intent.FLAG_ACTIVITY_NO_USER_ACTION//Just helps avoid conflicting with other important notifications
                    | Intent.FLAG_ACTIVITY_NO_HISTORY//Ensures the activity WILL be finished after the one time use
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    
    context.startActivity(dismiss);
}
    
//============Phone call case handling
    
    //we have many cases where the phone reloads the lockscreen even while screen is awake at call end
    //my testing shows it actually comes back after any timeout sleep plus 5 sec grace period
    //then phone is doing a KM disable command at re-wake. and restoring at call end
    //that restore is what we intercept in these events as well as certain treatment based on lock activity lifecycle
    
    @Override
    public void onCallEnd() {
            //all timeout sleep causes KG to visibly restore after the 5 sec grace period
            //the phone appears to be doing a KM disable to pause it should user wake up again, and then re-enables at call end
            
            //if call ends while asleep and not in the KG-restored mode (watching for prox wake)
            //then KG is still restored, and we can't catch it due to timing
            
            //right now we can't reliably check the screen state
    		//instead we will restart the guard if call came in waking up device
    		//otherwise we will just do nothing besides dismiss any restored kg
            
            Context mCon = getApplicationContext();
            
            Log.v("call end","checking if we need to exit KG");
            
            ManageKeyguard.initialize(mCon);
            
            boolean KG = ManageKeyguard.inKeyguardRestrictedInputMode();
            //this will tell us if the phone ever restored the keyguard
            //phone occasionally brings it back to life but suppresses it
            
            //2.1 isScreenOn will allow us the logic:
            
            //restart lock if it is asleep and relocked
            //dismiss lock if it is awake and relocked
            //do nothing if it is awake and not re-locked
            //wake up if it is asleep and not re-locked (not an expected case)
            
            //right now we will always dismiss
            /*
            if (callWake) {
                    Log.v("wakeup call end","restarting lock activity.");
                    callWake = false;
                    PendingLock = true;
                    StartLock(mCon);
                    //when we restart here, the guard activity is getting screen on event
                    //and calling its own dismiss as if it was a user initiated wakeup
                    //TODO but this logic will be needed for guarded custom lockscreen version
            }
            else {
            	//KG may or may not be about to come back and screen may or may not be awake
            	//these factors depend on what the user did during call
            	//all we will do is dismiss any keyguard that exists, which will cause wake if it is asleep
            	//if (IsAwake()) {}
                    Log.v("call end","checking if we need to exit KG");
                    shouldLock = true;
                    if (KG) StartDismiss(mCon);
            }*/
            
            //shouldLock = true;
            if (KG) StartDismiss(mCon);
            
    }
    
    @Override
    public void onCallMiss() {
    	callmissed = true;
    	//flag so we can suppress handling of the screen on we seem to get at phone state change
    }
   
    
    void doFGstart() {
            //putting ongoing notif together for start foreground
            
            //String ns = Context.NOTIFICATION_SERVICE;
            //NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
            //No need to get the mgr, since we aren't manually sending this for FG mode.
            
            int icon = R.drawable.icon;
            CharSequence tickerText = "myLock is starting up";
            
            long when = System.currentTimeMillis();

            Notification notification = new Notification(icon, tickerText, when);
            
            Context context = getApplicationContext();
            CharSequence contentTitle = "quick unlock mode active";
            CharSequence contentText = "click to open settings";

            Intent notificationIntent = new Intent(this, MainPreferenceActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            
            final int SVC_ID = 1;
            
            //don't need to pass notif because startForeground will do it
            //mNotificationManager.notify(SVC_ID, notification);
            persistent = true;
            
            startForeground(SVC_ID, notification);
    }
    public static class AutoDismissActivity extends Activity {
    	public boolean done = false;
        
        protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
      		  //| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
      		  | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      		  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Log.v("dismiss","creating dismiss window");
        
        //when using the show when locked the dismiss doesn't actually happen. lol.    
        updateLayout();
        
        //register for user present so we don't have to manually check kg with the keyguard manager
        IntentFilter userunlock = new IntentFilter (Intent.ACTION_USER_PRESENT);
        registerReceiver(unlockdone, userunlock);

    }      
        protected View inflateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.dismisslayout, null);
    }

    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        setContentView(inflateView(inflater));
    }

    BroadcastReceiver unlockdone = new BroadcastReceiver() {
    	    
    	    public static final String present = "android.intent.action.USER_PRESENT";

    	    @Override
    	    public void onReceive(Context context, Intent intent) {
    	    	if (!intent.getAction().equals(present)) return;
    	    	if (!done) {
    	    		Log.v("dismiss user present","sending to back");
    	    		done = true;
    	    		//callback mediator for final handling of the stupid wake lock
    	            Intent i = new Intent("i4nc4mp.myLock.lifecycle.LOCKSCREEN_EXITED");
    	            getApplicationContext().sendBroadcast(i);
    	    	   	moveTaskToBack(true);
    	    	   	finish();
    	    	   	overridePendingTransition(0, 0);//supposed to avoid trying to animate
    	    	}
    	    }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();      
       
        unregisterReceiver(unlockdone);
        Log.v("destroy_dismiss","Destroying");

        }
    }
    
    
}
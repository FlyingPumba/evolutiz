package i4nc4mp.myLock;

import i4nc4mp.myLock.IsActive;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


//abstraction of the core functions of the mediator remote service
//the class itself does registration for screen event listen according to phone state
//in prior implementations it would pause the broadcast receivers while calls are active
//the abstracted version will leave handling up to the subclass to decide what to do
//so just provides methods so the subclass can check if any kind of call is in progress
//when screen events need handling

public class MediatorService extends Service {
	
	private boolean active = false;
	//whether receivers are active- safety flag in the registration methods
	
	private boolean awake = false;
	//flag toggled by screen broadcast events
	
	private boolean exists = false;

		
/*Phone Status Flags*/
	public int lastphonestate = 42;
	//because we receive a state change to 0 when listener starts
	//this state will be caught by the listener so it knows it just started
	
	public boolean receivingcall = false;
	//true when state 1 to 2 occurs
	public boolean placingcall = false;
	//true when state 0 to 2 occurs
	
	private TelephonyManager tm = null;
	

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(getClass().getSimpleName(), "onBind()");
		return ExistStub;
	}
	
	/*@Override
	public void onCreate() {
		super.onCreate();
		Log.d(getClass().getSimpleName(),"onCreate()");
	}*/
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(getClass().getSimpleName(),"onDestroy()");
		
		pause();//disengage from screen broadcasts
		
		tm.listen(Detector, PhoneStateListener.LISTEN_NONE);
		
		}
	
	//2.0 and up, older API use onStart which is now deprecated
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		
		if (exists) {
			onRestartCommand();
			
			return 1;//don't proceed - the rest of the start command is initialization code
		}
	
			
		Log.v("first-start", "boot handler & rcvrs");
			
		activate();//registers to receive the screen broadcasts
				  	
    	//register with the telephony mgr to make sure we can read call state
    	tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
        assert(tm != null); 
        tm.listen(Detector, PhoneStateListener.LISTEN_CALL_STATE);
        
        ManageMediator.bind(getApplicationContext());
        //create the binding so we can keep track of status
        
        onFirstStart();//The subclass will place init actions in an override here
    	
        exists = true;
    	return 1;
	}
	
PhoneStateListener Detector = new PhoneStateListener() {
      	
		/*
		CALL_STATE_IDLE is 0 - this state comes back when calls end
		CALL_STATE_RINGING is 1 - a call is incoming, waiting for user to answer.
		CALL_STATE_OFFHOOK is 2 - call is actually in progress
		 */
		
		
    	@Override
    	public void onCallStateChanged(int state, String incomingNumber) 
        {
    		if (state == 2) {//change to call active
    			if (lastphonestate==0) placingcall = true;
                
    			//set the flags so subclasses can know what kind of call
    			//call reaction method
    			onCallStart();
    			//TODO implement onCallAccept for something that needs to be done only when incoming is answered
    			//vs onCallOriginate
            
            //pause();
    		}
    		else if (state==1){//change to ringing call
    			receivingcall = true;
    			onCallRing();
        	}
    		else {//return to idle
    			
    			if (lastphonestate == 42) Log.v("ListenInit","first phone listener init");
    			else {    			
    		
    			if (lastphonestate==1) {
    				//state 1 to 0 is user pressed ignore or missed the call
    				receivingcall = false;
    				//Log.v("mediator","user ignored or missed call");
    				onCallMiss();
    			}
    			else {
    				//activate();
    			
    				onCallEnd();
    				//call reaction method first, then reset flags
    				
    				receivingcall = false;
    				placingcall = false;
    				}    				
    			}
    		}
    		
    		//all state changes store themselves so changes can be interpreted
            lastphonestate = state;
        }
    };

    
	
	BroadcastReceiver screenon = new BroadcastReceiver() {
		
		public static final String TAG = "screenon";
		public static final String Screen = "android.intent.action.SCREEN_ON";
		
		

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(Screen)) return;
			
			Log.v(TAG, "Screen just went ON!");
			
			awake = true;
			onScreenWakeup();
			return;
	    		
}};
	
	BroadcastReceiver screenoff = new BroadcastReceiver() {
        
        public static final String TAG = "screenoff";
        public static final String Screenoff = "android.intent.action.SCREEN_OFF";

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (!intent.getAction().equals(Screenoff)) return;
                
                Log.v(TAG, "Screen just went OFF");
                
                awake = false;
                onScreenSleep();
                return;
}};

	//BroadcastReceiver battchange = new BatteryInfo(); 


void activate() {
	if (active) return;//protect from bad redundant calls
	
	//register the receivers
	IntentFilter onfilter = new IntentFilter (Intent.ACTION_SCREEN_ON);
	IntentFilter offfilter = new IntentFilter (Intent.ACTION_SCREEN_OFF);
	//IntentFilter battfilter = new IntentFilter (Intent.ACTION_BATTERY_CHANGED);
	

	
	registerReceiver(screenon, onfilter);
	registerReceiver (screenoff, offfilter);
	//registerReceiver (battchange, battfilter);

	active = true;
}

void pause() {
	if (!active) return;//protect from bad redundant calls
	
	//destroy the receivers
	unregisterReceiver(screenon);
    unregisterReceiver(screenoff);
    //unregisterReceiver (battchange);
	active = false;
}

private IsActive.Stub ExistStub = new IsActive.Stub() {
    public boolean Exists() throws RemoteException {
          return exists;
    }
};

public void onFirstStart() {
	//do first inits
}

public void onRestartCommand() {
	//respond to a repeat start requests
	//things like mode toggles or flag resets will occur here
}

public void onCallStart() {
	//react to user starting a call
}

public void onCallEnd() {
	//react to end of a call
}

public void onCallRing() {
	//react to incoming call starting to ring
}

public void onCallMiss() {
	//react to user ignore or timeout of ringing call
}

public void onScreenWakeup() {
	//react to screen on broadcast
}

public void onScreenSleep() {
	//react to screen off broadcast
}

public boolean IsAwake() {//subclasses can call this, possibly integrate this into bind
	return awake;
}

}
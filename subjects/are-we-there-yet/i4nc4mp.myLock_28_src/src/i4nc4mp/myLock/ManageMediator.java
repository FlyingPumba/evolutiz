package i4nc4mp.myLock;



import i4nc4mp.myLock.IsActive;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


//static helper to give us a reliable notificaton if service dies and let us keep track of state
//also houses method to call through to toggler service to handle a state change request
//also can be used to call specific state change, that is used by the pref screen

public class ManageMediator {
	//Bind handling
	private static RemoteServiceConnection conn = null;
	private static IsActive mediator; 
	private static Context c;
	
	//Constants for mode selection
	public static final int MODE_BASIC = 0;
	public static final int MODE_HIDDEN = 1;
	public static final int MODE_ADVANCED = 2;
	
	
	//public static final String BASIC = "i4nc4mp.myLock.froyo.AutoDismiss";
	//public static final String HIDDEN = "i4nc4mp.myLock.froyo.BasicGuardService";
	//public static final String ADVANCED = "i4nc4mp.myLock.froyo.UnGuardService";
	
	static class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, 
			IBinder boundService ) {
          mediator = IsActive.Stub.asInterface((IBinder)boundService);
          Log.v("service connected","bind to existent service");
          //this one happens when we start the bind
          //if (c!=null) ToggleWidget.makeView(c, true);
        }

        public void onServiceDisconnected(ComponentName className) {
          mediator = null;
          Log.v("service disconnected","service death");
          
          if (c==null) return;
          
          Toast.makeText(c, "unexpected myLock stop", Toast.LENGTH_LONG).show();
          
          //updateEnablePref(false, c);
          
          ToggleWidget.makeView(c, false);
          //this one only comes through when something force kills the service but not entire process
        }
    };
    
    //Check prefs and return the explicit mediator intent
    //we don't need to check prefs in the utilities build here
    //since we only include auto (quick unlock mode)
    
    public static Intent getMode(Context mCon) {
    	//SharedPreferences settings = mCon.getSharedPreferences("myLock", 0);
    	//int m = Integer.parseInt(settings.getString("mode", "0"));
    	//String name = BASIC;
    	Class c = null;
    	
    	/*
    	 * 
    	 
    	switch (m) {
    		case (MODE_BASIC):
    			c = AutoDismiss.class;
    		break;
    		case (MODE_HIDDEN):
    			c = BasicGuardService.class;
    		break;
    		case (MODE_ADVANCED):
    			c = UnguardService.class;
    		break;
    	}*/
    	c = AutoDismiss.class;
    	
    	//alternate methods to explicitly define the intent
    	
    	//ComponentName comp = new ComponentName(mCon.getPackageName(), c.getName());
 	    //new Intent().setComponent(comp));
    	    	    	
    	//Intent i = new Intent();
		//i.setClassName("i4nc4mp.myLock.froyo", name);
		
    	Intent result = new Intent(mCon, c);
		return result;
    }
    
	
	public static synchronized boolean bind(Context mCon) {
		boolean success;
		
		//Clients get true back if the binding is already held
		//otherwise, they wait 100ms, then call serviceActive to verify
		
		if (c==null) c=mCon;//store our context ref so we can use it if service dies
		
		if(conn == null) {
			Log.v("bind attempt","initializing connection object");
			conn = new RemoteServiceConnection();
		}
		
		if (mediator != null) {
			Log.v("bind result","binding already held, returning true");
			return true;
		}
		else {
			mCon.bindService(getMode(mCon), conn, 0);
			return false;
		}		
		//Log.v("bind attempt","attempted to get mediator, and the request returned " + success);
		//if (success) return serviceActive(mCon);
		}

	//called when we deliberately stop the service - this way the bind is fully zeroed out
	public static synchronized void release(Context mCon) {
		if(conn != null) {
			mCon.unbindService(conn);
			conn = null;
			mediator = null;
		} 
	}
	
	public static synchronized boolean serviceActive(Context mCon) {
		boolean exists = false;
		//for extra redundancy, call through to method in the mediator
		
		//this could be changed into a thread that tries bind, waits 100ms, then checks again
		//if mediator is still null, return false.
		//this would be a reliable verify
		//but i need to learn how to make the thread synchronize like this
		
		if (mediator!=null)
		{
			try {
			exists = mediator.Exists();
		} catch (RemoteException re) {
			Log.e("unknown failure" , "had mediator stub but couldn't check active" );
			}
		}
		
		Log.v("verify bind","result is " + exists);
		return exists;
	}
	
	public static synchronized void startService(Context mCon){
		SharedPreferences set = mCon.getSharedPreferences("myLock", 0);
		SharedPreferences.Editor editor = set.edit();
		
		editor.putBoolean("startingUp", true);
		editor.commit();
		//onCreate checks this to know that user is explicitly clicking start
		//when false we know system is restarting after a low mem purge
		
		Intent i = getMode(mCon);
		mCon.startService(i);
		
		ToggleWidget.makeView(mCon, true);
		Log.d( "manage mediator", "start call " + i );
}

	public static synchronized void stopService(Context mCon) {
		release(mCon);
		//kill the binding
		Intent i = getMode(mCon);
		
		mCon.stopService(i);
		
		ToggleWidget.makeView(mCon, false);
		Log.d( "manage mediator", "stop call " + i);
}
	
	public static synchronized void invokeToggler(Context mCon, boolean on) {
		Intent i = new Intent();
		
		i.setClassName("i4nc4mp.myLock", "i4nc4mp.myLock.Toggler");
		i.putExtra("i4nc4mp.myLock.TargetState", on);
		mCon.startService(i);
	}
	
	//only used for external toggle requests
	//toggler service handles requesting this method
	public static synchronized void updateEnablePref(boolean on, Context mCon) {
		SharedPreferences set = mCon.getSharedPreferences("myLock", 0);
		SharedPreferences.Editor editor = set.edit();
	    editor.putBoolean("enabled", on);
	    
	    // Don't forget to commit your edits!!!
	    editor.commit();
	    
	}
	
}
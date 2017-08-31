package i4nc4mp.myLock;
//Thanks for this code from the open source SMSPopup project
//Nice wrapped up tool that gets us cleanly in and out of keyguard management
//I added some documentation to certain parts to help explain the keyguardmanager facet of android.


import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.KeyguardManager.OnKeyguardExitResult;
import android.content.Context;
import android.util.Log;

public class ManageKeyguard {
private static KeyguardManager myKM = null;
private static KeyguardLock myKL = null;

public static final String TAG = "kg";

public static synchronized void initialize(Context context) {
  if (myKM == null) {
    myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    Log.v("MKinit","we had to get the KM.");
  }
}

public static synchronized void disableKeyguard(Context context) {
  // myKM = (KeyguardManager)
  // context.getSystemService(Context.KEYGUARD_SERVICE);
  initialize(context);

  if (myKM.inKeyguardRestrictedInputMode()) {
    myKL = myKM.newKeyguardLock(TAG);
    myKL.disableKeyguard();
    //Log.v(TAG,"--Keyguard disabled");
  } else {
    myKL = null;
  }
}

//the following checks if the keyguard is even on...
//it actually can't distinguish between password mode or not
//I've learned that this returns true even if you've done the above disable
//that's because disable is just hiding or pausing the lockscreen.

//the only time this returns false is if we haven't ever done an init
//or we have already securely exited
//the OS also seems to automatically do a secure exit when you press home after a disable
public static synchronized boolean inKeyguardRestrictedInputMode() {
  if (myKM != null) {
    return myKM.inKeyguardRestrictedInputMode();
  }
  
  return false;
}

public static synchronized void reenableKeyguard() {
  if (myKM != null) {
    if (myKL != null) {
      myKL.reenableKeyguard();
      myKL = null;
    }
  }
}

//this only can be used after we have paused/hidden the lockscreen with a disablekeyguard call
//otherwise the OS logs "verifyunlock called when not externally disabled."
public static synchronized void exitKeyguardSecurely(final LaunchOnKeyguardExit callback) {
  if (inKeyguardRestrictedInputMode()) {
    Log.v(TAG,"--Trying to exit keyguard securely");
    myKM.exitKeyguardSecurely(new OnKeyguardExitResult() {
      public void onKeyguardExitResult(boolean success) {
        reenableKeyguard();
        //this call ensures the keyguard comes back at screen off
        //without this call, all future disable calls will be blocked
        //for not following the lockscreen rules
        //in other words reenable immediately restores a paused lockscreen
        //but only queues restore for next screen off if a secure exit has been done already
        if (success) {
          Log.v(TAG,"--Keyguard exited securely");
          callback.LaunchOnKeyguardExitSuccess();
        } else {
          Log.v(TAG,"--Keyguard exit failed");
        }
      }
    });
  } else {
    callback.LaunchOnKeyguardExitSuccess();
  }
}

public interface LaunchOnKeyguardExit {
  public void LaunchOnKeyguardExitSuccess();
}
}


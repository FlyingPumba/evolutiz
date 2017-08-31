package net.everythingandroid.timer;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;

public class ManageKeyguard {
  private static KeyguardManager myKM = null;
  private static KeyguardLock myKL = null;

  public static synchronized void disableKeyguard(Context context) {

    if (myKM == null) {
      myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    if (myKM.inKeyguardRestrictedInputMode()) {
      myKL = myKM.newKeyguardLock(Log.LOGTAG);
      myKL.disableKeyguard();
      if (Log.DEBUG) Log.v("Keyguard disabled");
    }
  }

  public static synchronized void reenableKeyguard() {
    if (myKL != null) {
      myKL.reenableKeyguard();
      myKL = null;
      if (Log.DEBUG) Log.v("Keyguard reenabled");
    }
  }

  public static synchronized boolean inKeyguardRestrictedInputMode(Context context) {

    if (myKM == null) {
      myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    if (myKM != null) {
      if (Log.DEBUG) Log.v("--inKeyguardRestrictedInputMode = " + myKM.inKeyguardRestrictedInputMode());
      return myKM.inKeyguardRestrictedInputMode();
    }

    return false;
  }
}

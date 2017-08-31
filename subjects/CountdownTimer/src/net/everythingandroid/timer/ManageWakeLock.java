package net.everythingandroid.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class ManageWakeLock {
  private static PowerManager.WakeLock myWakeLock;

  // private static int TIMEOUT_SECS = 10;

  public static void acquire(Context context) {
    if (myWakeLock != null) {
      // myWakeLock.release();
      return;
    }

    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

    SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    boolean wakeScreen = myPrefs.getBoolean(context.getString(R.string.pref_wakescreen), true);

    // int flags = PowerManager.ON_AFTER_RELEASE;
    int flags = 0;
    if (wakeScreen) {
      ManageKeyguard.disableKeyguard(context);
      flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
    } else {
      flags = PowerManager.PARTIAL_WAKE_LOCK;
    }

    myWakeLock = pm.newWakeLock(flags, Log.LOGTAG);
    if (Log.DEBUG) Log.v("Wakelock acquired");
    // myWakeLock.acquire(TIMEOUT_SECS * 1000);
    myWakeLock.acquire();
  }

  static void release() {
    if (myWakeLock != null) {
      if (Log.DEBUG) Log.v("Wakelock released");
      myWakeLock.release();
      myWakeLock = null;
    }
  }
}

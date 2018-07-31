package net.everythingandroid.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClearAllReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Log.DEBUG) Log.v("ClearAllReceiver: onReceive()");
    ManageNotification.clearAll(context);
    ManageKeyguard.reenableKeyguard();
    ManageWakeLock.release();
  }

  private static PendingIntent getPendingIntent(Context context) {
    return PendingIntent.getBroadcast(context, 0, new Intent(context, ClearAllReceiver.class), 0);
  }

  public static void setCancel(Context context, int timeout) {
    if (Log.DEBUG) Log.v("ClearAllReceiver: setCancel() for " + timeout + "mins");
    AlarmManager myAM = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    myAM.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (timeout * 1000 * 60),
        getPendingIntent(context));
  }

  public static void removeCancel(Context context) {
    if (Log.DEBUG) Log.v("ClearAllReceiver: removeCancel()");
    AlarmManager myAM = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    myAM.cancel(getPendingIntent(context));
  }
}

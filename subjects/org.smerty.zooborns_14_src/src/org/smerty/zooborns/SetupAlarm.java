package org.smerty.zooborns;

import org.smerty.zooborns.feed.UpdateService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SetupAlarm {

  private static final String TAG = SetupAlarm.class.getName();

  public static void setup(Context context) {

    Log.d(TAG, "Inside setup method.");

    Intent intent = new Intent(context, UpdateService.class);
    PendingIntent pendingIntent = PendingIntent.getService(context, -1, intent,
        PendingIntent.FLAG_CANCEL_CURRENT);

    AlarmManager alarmManager = (AlarmManager) context
        .getSystemService(Context.ALARM_SERVICE);

    long interval = AlarmManager.INTERVAL_HOUR;

    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis(), interval, pendingIntent); //

  }

}

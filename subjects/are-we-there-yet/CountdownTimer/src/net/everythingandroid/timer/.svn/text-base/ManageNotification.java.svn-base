package net.everythingandroid.timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ManageNotification {
  public static final int NOTIFICATION_RUNNING = 0;
  public static final int NOTIFICATION_ALERT = 1;

  private static NotificationManager myNM = null;
  private static SharedPreferences myPrefs = null;

  private static void createNM(Context context) {
    if (myNM == null) {
      myNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
  }

  private static void createPM(Context context) {
    if (myPrefs == null) {
      myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
  }

  public static void show(Context context, String message) {
    show(context, message, message);
  }

  public static void show(Context context, String scrollMessage, String statusMessage) {
    createPM(context);

    if (myPrefs.getBoolean(context.getString(R.string.pref_show_persistent_notification), false)) {

      createNM(context);

      // Set the icon, scrolling text and timestamp
      Notification notification =
        new Notification(R.drawable.alarm_icon, scrollMessage, System.currentTimeMillis());

      notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

      // The pendingintent to launch if the status message is clicked
      PendingIntent contentIntent =
        PendingIntent.getActivity(context, 0, new Intent(context, TimerActivity.class), 0);

      // Set the messages that show when the status bar is pulled down
      notification.setLatestEventInfo(context, statusMessage,
          context.getText(R.string.notification_tip_running), contentIntent);

      // Send notification with unique ID
      myNM.notify(NOTIFICATION_RUNNING, notification);
    }
  }

  public static void clear(Context context) {
    if (Log.DEBUG) Log.v("Notification cleared");
    createNM(context);
    myNM.cancel(NOTIFICATION_ALERT);
  }

  public static void clearAll(Context context) {
    if (Log.DEBUG) Log.v("All notifications cleared");
    createNM(context);
    myNM.cancelAll();
  }
}

package net.everythingandroid.timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class TimerAlarmReceiver extends BroadcastReceiver {

  // 200ms off, 200ms on
  private static final long[] vibrate_pattern = {200, 200};
  private static final String DEFAULT_ALARM_TIMEOUT = "5";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Log.DEBUG) Log.v("TimerAlarmReceiver: onReceive() start");

    // Acquire wakelock
    ManageWakeLock.acquire(context);

    ManageNotification.clearAll(context);

    // Show the notification
    NotificationManager myNM =
      (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);

    boolean vibrate = myPrefs.getBoolean(context.getString(R.string.pref_vibrate), true);
    boolean flashLed = myPrefs.getBoolean(context.getString(R.string.pref_flashled), true);
    String flashLedCol =
      myPrefs.getString(context.getString(R.string.pref_flashled_color), "yellow");
    int timeout =
      Integer.valueOf(
          myPrefs.getString(context.getString(R.string.pref_timeout), DEFAULT_ALARM_TIMEOUT));

    String defaultRingtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
    Uri alarmSoundURI =
      Uri.parse(myPrefs.getString(context.getString(R.string.pref_alarmsound), defaultRingtone));

    // Set the icon, scrolling text and timestamp
    Notification notification =
      new Notification(R.drawable.alarm_icon, context.getText(R.string.timer_complete),
          System.currentTimeMillis());

    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_INSISTENT;

    notification.audioStreamType = AudioManager.STREAM_ALARM;

    if (flashLed) {
      notification.flags |= Notification.FLAG_SHOW_LIGHTS;
      notification.ledOnMS = 250;
      notification.ledOffMS = 250;
      int col = Color.YELLOW;
      try {
        col = Color.parseColor(flashLedCol);
      } catch (IllegalArgumentException e) {
        // int col = Color.YELLOW;
      }
      // Blue, Green, Red, Yellow, Magenta
      notification.ledARGB = col;
    }

    if (vibrate) {
      notification.vibrate = vibrate_pattern;
    }

    notification.sound = alarmSoundURI;

    // The pendingintent to launch if the status message is clicked
    PendingIntent contentIntent =
      PendingIntent.getActivity(context, 0, new Intent(context, TimerActivity.class), 0);

    // Set the messages that show when the status bar is pulled down
    notification.setLatestEventInfo(context, context.getText(R.string.timer_complete),
        context.getText(R.string.notification_tip_complete), contentIntent);

    if (Log.DEBUG) Log.v("*** Notify running ***");

    // Send notification with unique ID
    myNM.notify(ManageNotification.NOTIFICATION_ALERT, notification);

    // Set a future receiver to cancel all held locks (wakelock and keyguard lock)
    ClearAllReceiver.setCancel(context, timeout);

    Intent alarmDialog = new Intent(context, TimerAlarmActivity.class);
    alarmDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP); // |
    context.startActivity(alarmDialog);

    if (Log.DEBUG) Log.v("TimerAlarmReceiver: onReceive() end");
  }
}
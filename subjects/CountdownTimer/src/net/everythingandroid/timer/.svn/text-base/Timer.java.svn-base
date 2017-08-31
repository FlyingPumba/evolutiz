package net.everythingandroid.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Timer {
  public static final int TIMER_STARTED_OK = 0;
  public static final int TIMER_STARTED_PAUSED = 1;
  public static final int TIMER_ZERO = 2;
  public static final int TIMER_TOO_BIG = 3;
  public static final int TIMER_STALE = 4;
  public static final int TIMER_STARTED_OK_FROM_PAUSE = 5;

  private long triggerTime, remainingTime, pausedTime;
  private int hoursLeft, minsLeft, secsLeft;
  private boolean timerRunning, timerPaused;
  private static final long MAX_TIMER = ((((99 * 60) + 59) * 60) + 59 + 1) * 1000;
  private AlarmManager myAM;
  protected Context myContext;
  private SharedPreferences myPrefs;
  private PendingIntent timerAlarmIntent;

  public Timer(Context context) {
    myContext = context;
    myAM = (AlarmManager) myContext.getSystemService(Context.ALARM_SERVICE);
    myPrefs = PreferenceManager.getDefaultSharedPreferences(myContext);
    // timerAlarmIntent = PendingIntent.getService(myContext, 0,
    // new Intent(myContext, TimerService.class), 0);
    timerAlarmIntent =
      PendingIntent.getBroadcast(myContext, 0, new Intent(myContext, TimerAlarmReceiver.class), 0);
    resetVars();
  }

  private void resetVars() {
    timerRunning = false;
    timerPaused = false;
    triggerTime = 0;
    remainingTime = 0;
    pausedTime = 0;
    hoursLeft = 0;
    minsLeft = 0;
    secsLeft = 0;
  }

  public void restore() {
    resetVars();

    long triggerTime_init = myPrefs.getLong("triggerTime", 0);
    long pausedTime_init = myPrefs.getLong("pausedTime", 0);

    if (pausedTime_init > 0) { // we're paused
      timerRunning = true;
      timerPaused = true;
      pausedTime = pausedTime_init;
      triggerTime = triggerTime_init;
      remainingTime = triggerTime - pausedTime;
      // Log.v("remainingTime_init = " + String.valueOf(remainingTime_init) +
      // ", remainingTime = " + String.valueOf(remainingTime));
      // remainingTime = remainingTime_init;
      refreshTimerVals();
    } else { // we're not paused, we're running or stopped
      triggerTime = triggerTime_init;
      remainingTime = triggerTime - System.currentTimeMillis();
      start(remainingTime);
    }
  }

  public int start(long time) {
    return start(time, false);
  }

  public int start(int hour, int min, int sec) {
    return start(convertTime(hour, min, sec));
  }

  public int start(long time, boolean comingFromPaused) {
    if (time > MAX_TIMER) {
      if (Log.DEBUG) Log.v("Timer.start() - timer too big");
      return TIMER_TOO_BIG;
    } else if (time > 0) {
      timerRunning = true;
      remainingTime = time;
      triggerTime = remainingTime + System.currentTimeMillis();
      myAM.set(AlarmManager.RTC_WAKEUP, getTriggerTime(), timerAlarmIntent);
      if (comingFromPaused) {
        if (Log.DEBUG) Log.v("Timer.start() - timer started from pause");
        return TIMER_STARTED_OK_FROM_PAUSE;
      } else {
        if (Log.DEBUG) Log.v("Timer.start() - timer started");
        // showNotification(myContext.getString(R.string.timer_running));
        ManageNotification.show(myContext, myContext.getString(R.string.timer_running));
        return TIMER_STARTED_OK;
      }
    } else if (time == 0) {
      if (Log.DEBUG) Log.v("Timer.start() - remainingTime=0");
      return TIMER_ZERO;
    }
    if (Log.DEBUG) Log.v("Timer.start() - timer stale");
    return TIMER_STALE;
  }

  public void stop() {
    // ManageWakeLock.release();
    // myNM.cancelAll();
    ManageNotification.clearAll(myContext);
    myAM.cancel(timerAlarmIntent);
    resetVars();

    SharedPreferences.Editor settings = myPrefs.edit();
    settings.putLong("triggerTime", 0);
    settings.putLong("pausedTime", 0);
    // settings.putLong("remainingTime", 0);
    settings.commit();
  }

  public void startStop(long time) {
    if (isRunning()) {
      stop();
    } else {
      start(time);
    }
  }

  public void startStop(int hour, int min, int sec) {
    startStop(convertTime(hour, min, sec));
  }

  public void pause() {
    if (isRunning() && !isPaused()) {
      timerPaused = true;
      pausedTime = System.currentTimeMillis();
      // showNotification(myContext.getString(R.string.timer_paused));
      ManageNotification.show(myContext, myContext.getString(R.string.timer_paused));

      myAM.cancel(timerAlarmIntent);
    }
  }

  public void resume() {
    if (isPaused()) {
      timerPaused = false;
      remainingTime = triggerTime - pausedTime;
      pausedTime = 0;
      start(remainingTime, true);
      ManageNotification.show(myContext, myContext.getString(R.string.timer_resumed),
          myContext.getString(R.string.timer_running));
      // showNotification(
      // myContext.getString(R.string.timer_resumed),
      // myContext.getString(R.string.timer_running));
      myAM.set(AlarmManager.RTC_WAKEUP, getTriggerTime(), timerAlarmIntent);
    }
  }

  public void pauseResume() {
    ManageNotification.clearAll(myContext);
    if (isPaused()) {
      resume();
    } else {
      pause();
    }
  }

  public boolean isRunning() {
    return timerRunning;
  }

  public boolean isPaused() {
    return timerPaused;
  }

  public void save() {
    SharedPreferences.Editor settings = myPrefs.edit();
    settings.putLong("triggerTime", getTriggerTime());
    settings.putLong("pausedTime", getPausedTime());
    // settings.putLong("remainingTime", getRemainingTime());
    settings.commit();
  }

  public int getSecsLeft() {
    if (secsLeft < 0) {
      return 0;
    }
    return secsLeft;
  }

  public int getMinsLeft() {
    if (minsLeft < 0) {
      return 0;
    }
    return minsLeft;
  }

  public int getHoursLeft() {
    if (hoursLeft < 0) {
      return 0;
    }
    return hoursLeft;
  }

  public long getTriggerTime() {
    if (isRunning()) {
      return triggerTime;
    }
    return 0;
  }

  public long getPausedTime() {
    if (isPaused()) {
      return pausedTime;
    }
    return 0;
  }

  public long getRemainingTime() {
    if (isRunning()) {
      return remainingTime;
    }
    return 0;
  }

  public void refreshTimerVals() {

    // Not running
    if (!isRunning()) {
      hoursLeft = 0;
      minsLeft = 0;
      secsLeft = 0;
      return;
    }

    if (isPaused()) {
      // Paused
      remainingTime = triggerTime - pausedTime;
    } else {
      // Running
      remainingTime = triggerTime - System.currentTimeMillis();
    }

    // If timer expired
    if (remainingTime < 0) {
      hoursLeft = 0;
      minsLeft = 0;
      secsLeft = 0;
      return;
    }

    // Otherwise calculate values
    long runTimeSecs = remainingTime / 1000;
    // Log.v("Remaining time = " + remainingTime);
    // milliSecsLeft = (int) (remainingTime % 1000);
    secsLeft = (int) (runTimeSecs % 60);
    minsLeft = (int) ((runTimeSecs % 3600) / 60);
    hoursLeft = (int) (runTimeSecs / 3600);
  }

  private static long convertTime(int hour, int min, int sec) {
    return ((((hour * 60) + min) * 60) + sec) * 1000;
  }

  // private void showNotification(String message) {
  // showNotification(message, message);
  // }
  //
  // private void showNotification(String scrollMessage, String statusMessage) {
  //
  // if (myPrefs.getBoolean(
  // myContext.getString(R.string.pref_show_persistent_notification),false)) {
  //
  // NotificationManager myNM =
  // (NotificationManager)
  // myContext.getSystemService(Context.NOTIFICATION_SERVICE);
  //
  // // Set the icon, scrolling text and timestamp
  // Notification notification = new Notification(
  // R.drawable.alarm_icon,
  // scrollMessage,
  // System.currentTimeMillis());
  //
  // notification.flags = Notification.FLAG_ONGOING_EVENT |
  // Notification.FLAG_NO_CLEAR;
  //
  // //The pendingintent to launch if the status message is clicked
  // PendingIntent contentIntent = PendingIntent.getActivity(myContext, 0,
  // new Intent(myContext, TimerActivity.class), 0);
  //
  // //Set the messages that show when the status bar is pulled down
  // notification.setLatestEventInfo(myContext,
  // statusMessage,
  // myContext.getText(R.string.notification_tip_running), contentIntent);
  //
  // //Send notification with unique ID
  // myNM.notify(NOTIFICATION_RUNNING, notification);
  // }
  // }
  //
  // public void clearNotification() {
  // myNM.cancel(NOTIFICATION_ALERT);
  // }
  //
  // public static void clearNotification(Context context) {
  // NotificationManager myNM =
  // (NotificationManager)
  // context.getSystemService(Context.NOTIFICATION_SERVICE);
  // myNM.cancel(NOTIFICATION_ALERT);
  // }

}

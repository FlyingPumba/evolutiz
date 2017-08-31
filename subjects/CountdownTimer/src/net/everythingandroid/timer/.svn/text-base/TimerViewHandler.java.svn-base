package net.everythingandroid.timer;

import android.os.Handler;
import android.os.Message;

public abstract class TimerViewHandler extends Handler {
  private static final int MSG_UPDATE_TIMER = 1;
  private Timer myTimer;

  public TimerViewHandler(Timer timer) {
    super();
    myTimer = timer;
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.what == MSG_UPDATE_TIMER) {
      updateView();
      long remainingTime = myTimer.getRemainingTime();
      if (remainingTime > 0) {
        long nextTick = remainingTime % 1000;
        if (nextTick <= 0) {
          nextTick = 1000;
        }
        sendMessageDelayed(this.obtainMessage(MSG_UPDATE_TIMER), nextTick);
      } else {
        stop();
      }
    }
  }

  public void start() {
    if (Log.DEBUG) Log.v("TimerViewHandler: start()");
    this.sendMessage(this.obtainMessage(MSG_UPDATE_TIMER));
  }

  public void stop() {
    if (Log.DEBUG) Log.v("TimerViewHandler: stop()");
    this.removeMessages(MSG_UPDATE_TIMER);
    updateView();
  }

  public abstract void updateView();
}
package org.smerty.zooborns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

  private static final String TAG = BootReceiver.class.getName();

  @Override
  public void onReceive(Context context, Intent callingIntent) {
    Log.d(TAG, "Inside onReceived method.");
    SetupAlarm.setup(context);
  }

}

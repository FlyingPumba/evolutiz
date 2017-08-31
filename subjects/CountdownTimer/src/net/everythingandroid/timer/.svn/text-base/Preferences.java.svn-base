package net.everythingandroid.timer;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }

  @Override
  public void onResume() {
    super.onResume();
    Intent i = getIntent();
    int flags = i.getFlags();
    if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
      i.setFlags(flags & ~Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
      Intent timerActivity = new Intent(this, TimerActivity.class);
      startActivity(timerActivity);
    }
  }
}
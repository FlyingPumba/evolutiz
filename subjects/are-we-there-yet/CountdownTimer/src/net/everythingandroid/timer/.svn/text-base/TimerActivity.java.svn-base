package net.everythingandroid.timer;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.view.inputmethod.InputMethodManager;

public class TimerActivity extends Activity {
  private static final int PREFERENCES_ID = Menu.FIRST;
  private static final int ABOUT_ID = Menu.FIRST + 1;
  private static final int DIALOG_ABOUT = 0;

  private Timer myTimer;
  private SharedPreferences myPrefs;
  private EditText hourEditText, minEditText, secEditText;
  private TextView hourTextView, minTextView, secTextView;
  private Spinner hourSpinner, minSpinner, secSpinner;
  private ArrayAdapter<String> secSpinnerAA, minSpinnerAA, hourSpinnerAA;
  private ArrayList<String> secSpinnerList, minSpinnerList, hourSpinnerList;
  private int keyboard_hidden;
  private TimerViewHandler tvHandler;
  private LinearLayout buttonLinearLayout;

  private static int TIMER_INPUT_TYPE_EDITTEXT = 0;
  private static int TIMER_INPUT_TYPE_SPINNER = 1;

  private static final String DEFAULT_MINUTES = "5";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

	//this.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    myTimer = new Timer(this) {
      @Override
      public int start(long time) {
        int result = super.start(time);

        switch (result) {
          case Timer.TIMER_TOO_BIG:
            Toast.makeText(myContext, R.string.timer_too_long, Toast.LENGTH_SHORT).show();
            break;
          case Timer.TIMER_ZERO:
            Toast.makeText(myContext, R.string.select_a_time, Toast.LENGTH_SHORT).show();
            break;
          case Timer.TIMER_STARTED_OK:
            tvHandler.start();
            break;
          case Timer.TIMER_STARTED_OK_FROM_PAUSE:
            break;
          case Timer.TIMER_STALE:
            break;
        }
        return result;
      }

      @Override
      public void stop() {
        super.stop();
        // stopHandler();
        tvHandler.stop();

      }

      @Override
      public void pause() {
        super.pause();
        tvHandler.stop();
      }

      @Override
      public void resume() {
        super.resume();
        tvHandler.start();
      }

      @Override
      public void pauseResume() {
        super.pauseResume();
        updateButtons();
      }

      @Override
      public void startStop(long time) {
        super.startStop(time);
        updateButtons();
      }
    };

    tvHandler = new TimerViewHandler(myTimer) {
      @Override
      public void updateView() {
        updateCountdown();
      }
    };

    setContentView(R.layout.main);

    Button button = (Button) findViewById(R.id.StartStopButton);
    button.setOnClickListener(StartStopTimer);
    button = (Button) findViewById(R.id.PauseButton);
    button.setOnClickListener(PauseResumeTimer);

    hourTextView = (TextView) findViewById(R.id.HourTextView);
    minTextView = (TextView) findViewById(R.id.MinTextView);
    secTextView = (TextView) findViewById(R.id.SecTextView);

    hourSpinner = (Spinner) findViewById(R.id.HourSpinner);
    minSpinner = (Spinner) findViewById(R.id.MinuteSpinner);
    secSpinner = (Spinner) findViewById(R.id.SecondSpinner);

    hourEditText = (EditText) findViewById(R.id.HourEditText);
    minEditText = (EditText) findViewById(R.id.MinuteEditText);
    secEditText = (EditText) findViewById(R.id.SecondEditText);

	hourEditText.setEnabled(false);
	minEditText.setEnabled(false);
	secEditText.setEnabled(false);

	//InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	//imm.hideSoftInputFromWindow(hourEditText.getWindowToken(), 0);
	//imm.hideSoftInputFromWindow(minEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	//imm.hideSoftInputFromWindow(secEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    buttonLinearLayout = (LinearLayout) findViewById(R.id.ButtonLayout);

    hourSpinnerList =
      new ArrayList<String>(
          Arrays.asList(getResources().getStringArray(R.array.hours_array_full)));
    minSpinnerList =
      new ArrayList<String>(Arrays.asList(getResources().getStringArray(
          R.array.mins_secs_array_full)));
    secSpinnerList =
      new ArrayList<String>(Arrays.asList(getResources().getStringArray(
          R.array.mins_secs_array_full)));

    hourSpinnerAA =
      new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hourSpinnerList);
    minSpinnerAA =
      new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, minSpinnerList);
    secSpinnerAA =
      new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, secSpinnerList);

    hourSpinnerAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    minSpinnerAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    secSpinnerAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    hourSpinner.setAdapter(hourSpinnerAA);
    minSpinner.setAdapter(minSpinnerAA);
    secSpinner.setAdapter(secSpinnerAA);

    myPrefs = PreferenceManager.getDefaultSharedPreferences(TimerActivity.this);

    if (savedInstanceState == null) {
      // Only when it's created for the first time
      // Check if the user has set a custom sound for the timer, if not show a message
      if (myPrefs.getString(getString(R.string.pref_alarmsound), null) == null) {
        Toast.makeText(this, R.string.please_select_sound, Toast.LENGTH_LONG).show();
      }
    }

    updateLayout(getResources().getConfiguration());
  }

  @Override
  protected void onPause() {
    if (Log.DEBUG) Log.v("TimerActivity: onPause()");
    super.onPause();

    myTimer.save();
    tvHandler.stop();

    SharedPreferences.Editor settings = myPrefs.edit();

    if (keyboard_hidden == Configuration.KEYBOARDHIDDEN_YES) {
      settings.putString("prevHourSelected", (String) hourSpinner.getSelectedItem());
      settings.putString("prevMinSelected", (String) minSpinner.getSelectedItem());
      settings.putString("prevSecSelected", (String) secSpinner.getSelectedItem());
    } else {
      settings.putString("prevSecSelected", String.valueOf(getEditTextValue(secEditText)));
      settings.putString("prevMinSelected", String.valueOf(getEditTextValue(minEditText)));
      settings.putString("prevHourSelected", String.valueOf(getEditTextValue(hourEditText)));
    }
    settings.commit();

    // Reset the timer and buttons in case the alarm triggers and the activity
    // ends up in the background
    Timer emptyTimer = new Timer(this);
    updateButtons(emptyTimer);
    updateCountdown(emptyTimer);
  }

  @Override
  protected void onResume() {
    if (Log.DEBUG) Log.v("TimerActivity: onResume()");
    super.onResume();

    // Fetch previous timer selections
    String hour = myPrefs.getString("prevHourSelected", "0");
    String min = myPrefs.getString("prevMinSelected", DEFAULT_MINUTES);
    String sec = myPrefs.getString("prevSecSelected", "0");

    if (hourSpinnerList.size() ==
      getResources().getStringArray(R.array.hours_array_full).length + 1) {
      hourSpinnerList.remove(0);
    }
    if (minSpinnerList.size() ==
      getResources().getStringArray(R.array.mins_secs_array_full).length + 1) {
      minSpinnerList.remove(0);
    }
    if (secSpinnerList.size() ==
      getResources().getStringArray(R.array.mins_secs_array_full).length + 1) {
      secSpinnerList.remove(0);
    }

    int hourPos = hourSpinnerAA.getPosition(String.valueOf(hour));
    int minPos = minSpinnerAA.getPosition(String.valueOf(min));
    int secPos = secSpinnerAA.getPosition(String.valueOf(sec));

    if (hourPos == -1) { // Not found in list
      hourSpinnerList.add(0, hour);
      hourSpinner.setSelection(0);
    } else {
      hourSpinner.setSelection(hourPos);
    }
    if (minPos == -1) { // Not found in list
      minSpinnerList.add(0, min);
      minSpinner.setSelection(0);
    } else {
      minSpinner.setSelection(minPos);
    }
    if (secPos == -1) { // Not found in list
      secSpinnerList.add(0, sec);
      secSpinner.setSelection(0);
    } else {
      secSpinner.setSelection(secPos);
    }

    hourEditText.setText(hour);
    minEditText.setText(min);
    secEditText.setText(sec);

    myTimer.restore();

    updateButtons();
    updateCountdown();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    updateLayout(newConfig);
  }

  private OnClickListener StartStopTimer = new OnClickListener() {
    public void onClick(View v) {
      int hourSelected = 0, minSelected = 0, secSelected = 0;

      // Keyboard is available, use edittext boxes
      if (keyboard_hidden == Configuration.KEYBOARDHIDDEN_NO) {
        hourSelected = getEditTextValue(hourEditText);
        minSelected = getEditTextValue(minEditText);
        secSelected = getEditTextValue(secEditText);
      } else { // Keyboard not available, use spinners
        hourSelected = getSpinnerValue(hourSpinner);
        minSelected = getSpinnerValue(minSpinner);
        secSelected = getSpinnerValue(secSpinner);
      }

      myTimer.startStop(hourSelected, minSelected, secSelected);
    }
  };

  private OnClickListener PauseResumeTimer = new OnClickListener() {
    public void onClick(View v) {
      myTimer.pauseResume();
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // Preferences menu item
    MenuItem PrefMenuItem = menu.add(0, PREFERENCES_ID, 0, R.string.menu_preference);
    PrefMenuItem.setIcon(android.R.drawable.ic_menu_preferences);

    // About menu item
    MenuItem AboutMenuItem = menu.add(0, ABOUT_ID, 0, R.string.menu_about);
    AboutMenuItem.setIcon(android.R.drawable.ic_menu_info_details);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case PREFERENCES_ID:
        Intent prefIntent = new Intent(this, Preferences.class);
        startActivity(prefIntent);
        return true;
      case ABOUT_ID:
        showDialog(DIALOG_ABOUT);
        return true;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      if (!myTimer.isRunning()) {
        if (Log.DEBUG) Log.v("TimerActivity: onWindowFocusChanged(true)");
        ManageNotification.clear(this);
        ManageWakeLock.release();
        // myTimer.clearNotification();
      }
    } else {
      // Reset the UI in case it shows when the alert rings
      if (Log.DEBUG) Log.v("TimerActivity: onWindowFocusChanged(false)");
      // Reset the timer and buttons in case the alarm triggers and the activity
      // ends up in the background
      // Timer emptyTimer = new Timer(this);
      // updateButtons(emptyTimer);
      // updateCountdown(emptyTimer);
    }
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_ABOUT:

        // Try and find app version number
        String version = new String("");
        PackageManager pm = this.getPackageManager();
        try {
          // Get version number, not sure if there is a better way to do this
          version =
            " v" + pm.getPackageInfo(TimerActivity.class.getPackage().getName(), 0).versionName;
        } catch (NameNotFoundException e) {
          // No need to do anything here if it fails
          // e.printStackTrace();
        }

        LayoutInflater factory = getLayoutInflater();
        final View aboutView = factory.inflate(R.layout.about, null);

        return new AlertDialog.Builder(TimerActivity.this)
        .setIcon(R.drawable.alarm_icon)
        .setTitle(getString(R.string.app_name) + version)
        .setView(aboutView)
        .setPositiveButton(
            android.R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked OK so do some stuff */
              }
            })
            .create();
    }
    return null;
  }

  private void updateButtons() {
    updateButtons(myTimer);
  }

  private void updateButtons(Timer myTimer) {
    if (Log.DEBUG) Log.v("TimerActivity: updateButtons()");

    Button startStopButton = (Button) findViewById(R.id.StartStopButton);
    Button pauseButton = (Button) findViewById(R.id.PauseButton);

    if (myTimer.isRunning()) {
      startStopButton.setText(R.string.stop_button_text);
      pauseButton.setEnabled(true);
      pauseButton.setFocusable(true);
    } else {
      startStopButton.setText(R.string.start_button_text);
      pauseButton.setEnabled(false);
      pauseButton.setFocusable(false);
    }
    if (myTimer.isPaused()) {
      pauseButton.setText(R.string.resume_button_text);
    } else {
      pauseButton.setText(R.string.pause_button_text);
    }
  }

  private void updateCountdown() {
    updateCountdown(myTimer);
  }

  private void updateCountdown(Timer myTimer) {
    if (Log.DEBUG) Log.v("TimerActivity: updateCountdown()");
    myTimer.refreshTimerVals();
    int hours = myTimer.getHoursLeft();
    int mins = myTimer.getMinsLeft();
    int secs = myTimer.getSecsLeft();
    hourTextView.setText("" + (hours < 10 ? "0" : "") + hours);
    minTextView.setText("" + (mins < 10 ? "0" : "") + mins);
    secTextView.setText("" + (secs < 10 ? "0" : "") + secs);
  }

  private int getEditTextValue(EditText et) {
    try {
      return Integer.parseInt(et.getText().toString());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private int getSpinnerValue(Spinner spinner) {
    try {
      return Integer.parseInt((String) spinner.getSelectedItem());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Refresh the layout depending on the given configuration
   * 
   * @param configuration
   */
  private void updateLayout(Configuration configuration) {
    keyboard_hidden = configuration.keyboardHidden;

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      buttonLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
    } else {
      buttonLinearLayout.setOrientation(LinearLayout.VERTICAL);
    }

    if (keyboard_hidden == Configuration.KEYBOARDHIDDEN_NO) {
      switchTimerInputMethod(TIMER_INPUT_TYPE_EDITTEXT);
    } else {
      switchTimerInputMethod(TIMER_INPUT_TYPE_SPINNER);
    }
  }

  /**
   * Switch the method for inputting the time (choice of EditText entry using keyboard or
   * Spinners using touch screen).
   * 
   * @param method   TIMER_INPUT_TYPE_EDITTEXT or TIMER_INPUT_TYPE_SPINNER
   */
  private void switchTimerInputMethod(int method) {
    if (method == TIMER_INPUT_TYPE_EDITTEXT) {
      hourEditText.setVisibility(EditText.VISIBLE);
      minEditText.setVisibility(EditText.VISIBLE);
      secEditText.setVisibility(EditText.VISIBLE);

      hourSpinner.setVisibility(View.GONE);
      minSpinner.setVisibility(View.GONE);
      secSpinner.setVisibility(View.GONE);
    } else if (method == TIMER_INPUT_TYPE_SPINNER) {
      hourEditText.setVisibility(EditText.GONE);
      minEditText.setVisibility(EditText.GONE);
      secEditText.setVisibility(EditText.GONE);

      hourSpinner.setVisibility(View.VISIBLE);
      minSpinner.setVisibility(View.VISIBLE);
      secSpinner.setVisibility(View.VISIBLE);
    }
  }
}
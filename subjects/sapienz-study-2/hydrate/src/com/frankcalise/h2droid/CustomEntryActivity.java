package com.frankcalise.h2droid;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class CustomEntryActivity extends SherlockActivity implements OnGestureListener {
	
	private EditText mAmountEditText;
	private GestureDetector mGestureScanner;
	private DatePicker mDatePicker;
	private TimePicker mTimePicker;
	private CheckBox mHistoricalCheck;
	private boolean mIsHistorical = false;
	private WaterDB mWaterDB = WaterDB.getInstance();
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Apply user's desired theme
        String userTheme = Settings.getUserTheme(this);
        if (userTheme.equals(getString(R.string.light_theme))) {
        	setTheme(R.style.Theme_Hydrate);
        } else {
        	setTheme(R.style.Theme_Hydrate_Dark);
        }
        
    	super.onCreate(savedInstanceState);
        
        // Inflate the layout
        setContentView(R.layout.activity_custom_entry);
        
        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Enable default metric or non-metric via Settings
        final RadioButton metricRadioButton = (RadioButton)findViewById(R.id.radio_metric);
        final RadioButton imperialRadioButton = (RadioButton)findViewById(R.id.radio_non_metric);
        int unitsPref = Settings.getUnitSystem(getApplicationContext());
        
        mTimePicker = (TimePicker)findViewById(R.id.add_time_picker);
        mDatePicker = (DatePicker)findViewById(R.id.add_date_picker);
        
        mHistoricalCheck = (CheckBox)findViewById(R.id.add_historical_check);
        mHistoricalCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked == true) {
					mTimePicker.setVisibility(View.VISIBLE);
					mDatePicker.setVisibility(View.VISIBLE);
					mIsHistorical = true;
				} else {
					mTimePicker.setVisibility(View.GONE);
					mDatePicker.setVisibility(View.GONE);
					mIsHistorical = false;
				}
			}
        });
        
        // Check if passed date from History activity
        String histDate = getIntent().getStringExtra("historical_date");
        if (histDate != null) {
        	mHistoricalCheck.setChecked(true);
        	int spacePos = histDate.indexOf(" ");
        	String[] dateArr = histDate.substring(0, spacePos).split("-");
        	Log.d("CUSTOM", dateArr[0] + " " + dateArr[1] +  " " + dateArr[2] + " ");
        	mDatePicker.init(Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1])-1, Integer.parseInt(dateArr[2]), null);
        }
        
        // Toggle the correct radio button according to user's prefs
        if (unitsPref == Settings.UNITS_METRIC) {
        	metricRadioButton.setChecked(true);
        	imperialRadioButton.setChecked(false);
        } else {
        	metricRadioButton.setChecked(false);
        	imperialRadioButton.setChecked(true);
        }

        
        // Set up TextWatcher for the amount EditText
        mAmountEditText = (EditText)findViewById(R.id.amount_edittext);
        
        mAmountEditText.addTextChangedListener(new TextWatcher() {
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before,
        			int count) {
        	}

			@Override
			public void afterTextChanged(Editable s) {
				updateConversionTextView();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
        });
        
        mGestureScanner = new GestureDetector(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
    			// App icon in ActionBar pressed, go home
    			Intent intent = new Intent(this, h2droid.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(intent);
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    private void updateConversionTextView() {
    	final TextView tv = (TextView)findViewById(R.id.conversion_textview);
    	
    	try {
    		double amount = Double.valueOf(mAmountEditText.getText().toString()).doubleValue();
    	
    		final RadioButton radioNonMetric = (RadioButton)findViewById(R.id.radio_non_metric);

    		String conversionText;
    		if (radioNonMetric.isChecked()) {
    			conversionText = String.format("%.1f fl oz =  %.1f ml", amount, (amount / Entry.ouncePerMililiter));
    		} else {
    			conversionText = String.format("%.1f fl oz =  %.1f ml", (amount * Entry.ouncePerMililiter), amount);
    		}
    	
    		tv.setText(conversionText);
    	} catch (NumberFormatException nfe) {
    		tv.setText("");
    	}
    }
    
    public void onSaveClick(View v) {   	
    	try {
    		double amount = Double.valueOf(mAmountEditText.getText().toString()).doubleValue();
    		
    		final RadioButton radioNonMetric = (RadioButton)findViewById(R.id.radio_non_metric);
    		boolean isNonMetric;
    		
    		if (radioNonMetric.isChecked()) {
    			isNonMetric = true;
    		} else {
    			isNonMetric = false;
    		}
    		
    		Entry e = null;
    		if (mIsHistorical == true) {
    			String date = String.format("%d-%d-%d %d:%d:00", mDatePicker.getYear(), mDatePicker.getMonth()+1, mDatePicker.getDayOfMonth(), mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
    			e = new Entry(date, amount, isNonMetric);
    		} else {
    			e = new Entry(amount, isNonMetric);
    		}
    		addNewEntry(e);
    	} catch (NumberFormatException nfe) {
    		// show some error toast here?
    	}

    	finish();
    }
    
    public void onCancelClick(View v) {
    	finish();
    }
    
    public void onRadioClick(View v) {
    	updateConversionTextView();
    }
    
    private void addNewEntry(Entry entry) {
    	// Check to see if user wants Toast message
    	boolean showToasts = Settings.getToastsSetting(getApplicationContext());
    	
//    	ContentResolver cr = getContentResolver();
//    	
//    	// Insert the new entry into the provider
//    	ContentValues values = new ContentValues();
//    	
//    	values.put(WaterProvider.KEY_DATE, _entry.getDate());
//    	values.put(WaterProvider.KEY_AMOUNT, _entry.getMetricAmount());
//    	
//    	cr.insert(WaterProvider.CONTENT_URI, values);
    	mWaterDB.addNewEntry(getContentResolver(), entry);
    	
    	// Make a toast displaying add complete
    	int unitsPref = Settings.getUnitSystem(this);
    	double displayAmount = entry.getNonMetricAmount();
    	String displayUnits = getString(R.string.unit_fl_oz);
    	if (unitsPref == Settings.UNITS_METRIC) {
    		displayUnits = getString(R.string.unit_mililiters);
    		displayAmount = entry.getMetricAmount();
    	}
    	String toastMsg = String.format("Added %.1f %s", displayAmount, displayUnits);
    	Toast toast = Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT);
    	toast.setGravity(Gravity.BOTTOM, 0, 0);
    	if (showToasts)
    		toast.show();
    	
    	// If user wants a reminder when to drink next,
    	// setup a notification X minutes away from this entry
    	// where X is also a setting
    	if (Settings.getReminderEnabled(this) && mIsHistorical == false) {
    		// Get the AlarmManager services
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			
			// create the calendar object
			Calendar cal = Calendar.getInstance();
			// add X minutes to the calendar object
			cal.add(Calendar.MINUTE, Settings.getReminderInterval(this));
			
			// cancel existing alarm if any, this way latest
			// alarm will be the only one to notify user
			Intent cancelIntent = new Intent(this, AlarmReceiver.class);
			PendingIntent cancelSender = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);
			am.cancel(cancelSender);
			
			// set up the new alarm
			Intent intent = new Intent(this, AlarmReceiver.class);
			intent.putExtra("entryDate", entry.getDate());
			PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    	}
    }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// Dismiss the soft keyboard when
		// user taps main view
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mAmountEditText.getWindowToken(), 0);
		
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return mGestureScanner.onTouchEvent(ev);
	}
}

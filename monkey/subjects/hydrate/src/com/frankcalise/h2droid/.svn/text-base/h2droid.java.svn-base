package com.frankcalise.h2droid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
//import com.frankcalise.h2droid.util.UIUtils;
import com.frankcalise.h2droid.util.UnitLocale;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class h2droid extends SherlockActivity {
	private double mConsumption = 0;
	private boolean mShowToasts;
	private boolean mIsNonMetric = true;
	private static final String LOCAL_DATA = "hydrate_data";
	private Context mContext = null;
	private int mUnitsPref;
	private ContentResolver mContentResolver = null;
	private WaterDB mWaterDB = null;
	private GlassView mGlassView = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Apply user's desired theme
        String userTheme = Settings.getUserTheme(this);
        Log.d("HOME_ACTIVITY", "user theme = " +userTheme);
        if (userTheme.equals(getString(R.string.light_theme))) {
        	setTheme(R.style.Theme_Hydrate);
        } else {
        	setTheme(R.style.Theme_Hydrate_Dark);
        }
        
        super.onCreate(savedInstanceState);
        
        mContentResolver = getContentResolver();
        mContext = getApplicationContext();
        mWaterDB = WaterDB.getInstance();      
       
        // Set up main layout
        setContentView(R.layout.main);
        
        mGlassView = (GlassView)findViewById(R.id.home_glassview);
    }
    
    /** Called when activity returns to foreground */
    @Override
    protected void onResume() {
    	super.onResume();

    	mShowToasts = Settings.getToastsSetting(mContext);
    	mUnitsPref = Settings.getUnitSystem(mContext);
    	if (mUnitsPref == Settings.UNITS_US) {
    		mIsNonMetric = true;
    	} else {
    		mIsNonMetric = false;
    	}
    	
    	loadTodaysEntriesFromProvider();
    }
    
    /** Set up menu for main activity */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	// Inflate the main menu
//    	MenuInflater inflater = getMenuInflater();
    	getSupportMenuInflater().inflate(R.menu.main_menu, menu);
//    	inflater.inflate(R.menu.main_menu, menu);
    	
    	return true;
    }
    
    /** Handle menu selection */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Start activity depending on menu choice
    	switch (item.getItemId()) {
    		case R.id.menu_add:
    			Entry oneServing = new Entry(Settings.getOneServingAmount(this), mIsNonMetric);
    			addNewEntry(oneServing);
    			return true;
    		case R.id.menu_star:
    			String[] itemsArr = Settings.getArrayOfFavoriteAmounts(this);
    			new AlertDialog.Builder(this)
    				.setTitle("Add favorite amount")
    				.setItems(itemsArr, 
    					new DialogInterface.OnClickListener() {
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
    							double favAmount = Settings.getFavoriteAmountDouble(which, mContext);
    							Entry favServing = new Entry(favAmount, mIsNonMetric);
    							addNewEntry(favServing);
    						}
    					})
    			.show();
    			return true;
    		case R.id.menu_custom_amount:
    			startActivity(new Intent(this, CustomEntryActivity.class));
    			return true;
    		case R.id.menu_undo:
    			undoTodaysLastEntry();
    			return true;
    		case R.id.menu_settings:
    			startActivity(new Intent(this, Settings.class));
    			return true;
    		case R.id.menu_facts:
    			startActivity(new Intent(this, FactsActivity.class));
    			return true;
    		case R.id.menu_history:
    			startActivity(new Intent(this, HistoryActivity.class));
    			return true;
    		default: break;
    	}
    	
    	return false;
    }
    
    /** Handle "add one serving" action */
    public void onOneServingClick(View v) {
		Entry oneServing = new Entry(Settings.getOneServingAmount(this), mIsNonMetric);
		addNewEntry(oneServing);
    }
    
    /** Handle "add two servings" action */
    public void onFavServingsClick(View v) {
    	String[] itemsArr = Settings.getArrayOfFavoriteAmounts(this);
		new AlertDialog.Builder(this)
			.setTitle("Add favorite amount")
			.setItems(itemsArr, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						double favAmount = Settings.getFavoriteAmountDouble(which, mContext);
						Entry favServing = new Entry(favAmount, mIsNonMetric);
						addNewEntry(favServing);
					}
				})
		.show();
    }
    
    /** Handle "add custom serving" action */
    public void onCustomServingClick(View v) {
    	// adding some amount of water other than
    	// one or two servings
    	startActivity(new Intent(this, CustomEntryActivity.class));
    }
    
    /** Handle "undo last serving" action */
    public void onUndoClick(View v) {
		// remove last entry from today
		undoTodaysLastEntry();
    }
    
    private void addNewEntry(Entry entry) {
    	// Insert the new entry into the provider
    	mWaterDB.addNewEntry(mContentResolver, entry);
    	
    	if (mUnitsPref == Settings.UNITS_US) {
    		mConsumption += entry.getNonMetricAmount();
    	} else {
    		mConsumption += entry.getMetricAmount();
    	}
    	
    	// Make a toast displaying add complete
    	double displayAmount = entry.getNonMetricAmount();
    	String displayUnits = getString(R.string.unit_fl_oz);
    	if (mUnitsPref == Settings.UNITS_METRIC) {
    		displayUnits = getString(R.string.unit_mililiters);
    		displayAmount = entry.getMetricAmount();
    	}
    	
    	String toastMsg = String.format("%s %.1f %s", getString(R.string.home_added_toast), displayAmount, displayUnits);
    	Toast toast = Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT);
    	toast.setGravity(Gravity.BOTTOM, 0, 0);
    	if (mShowToasts)
    		toast.show();
    	
    	// Update the amount of consumption on UI
    	updateConsumptionTextView();
    	
    	// If user wants a reminder when to drink next,
    	// setup a notification X minutes away from this entry
    	// where X is also a setting
    	if (Settings.getReminderEnabled(this)) {
    		// Get the AlarmManager service
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
    
    private void undoTodaysLastEntry() {
    	boolean result = mWaterDB.deleteLastEntryFromToday(mContentResolver);
    	
    	if (result) loadTodaysEntriesFromProvider();
    	
    	if (mShowToasts) {
    		String toastMsg;
    		if (result) {
        		toastMsg = getString(R.string.home_undo_toast);
        	} else {
        		toastMsg = getString(R.string.home_no_entries_toast);
        	}
    		Toast toast = Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT);
        	toast.setGravity(Gravity.BOTTOM, 0, 0);
        	toast.show();
    	}
    }

    private void loadTodaysEntriesFromProvider() {
    	mConsumption = 0;
    	
    	WaterEntryList list = mWaterDB.getEntriesFromToday(mContentResolver);
    	if (list != null) {
    		mConsumption = list.getTotal();
    		if (mUnitsPref == Settings.UNITS_METRIC) {
    			mConsumption = UnitLocale.convertToMetric(mConsumption);
    		}
    	}
    	
    	updateConsumptionTextView();
    }
    
    /** Update the today's consumption TextView */
    private void updateConsumptionTextView() {
    	double prefsGoal = Settings.getAmount(mContext);
    	double percentGoal = (mConsumption / prefsGoal) * 100.0;
    	double delta = mConsumption - prefsGoal;

    	// suggested enhancement
//    	if (percentGoal > 100.0) {
//    		percentGoal = 100.0;
//    	}

    	// update the +N add button text according to the unit system
    	//final Button nButton = (Button)findViewById(R.id.add_custom_serving_button);
    	// update one serving button
    	//final Button oneSrvButton = (Button)findViewById(com.frankcalise.h2droid.R.id.add_one_serving_button);
    	
    	
    	// Show consumption amount	
    	String originalUnits = "";
    	double displayAmount = mConsumption;
    	String displayUnits = getString(R.string.unit_fl_oz);
    	if (mUnitsPref == Settings.UNITS_METRIC) {
    		displayUnits = getString(R.string.unit_mililiters);
//    		nButton.setText(getString(R.string.home_n_add_ml));
    	} else {
//    		nButton.setText(getString(R.string.home_n_add_oz));
    	}
    	//mOneServingText = String.format("%s (%s %s)", getString(com.frankcalise.h2droid.R.string.one_serving_button_label), Settings.getOneServingAmount(this), displayUnits);
    	//nButton.setText(UIUtils.formatCustomAmountButtonText(mContext));
    	//oneSrvButton.setText(UIUtils.formatOneServingButtonText(mContext));
    	
    	originalUnits = displayUnits;
    	
    	if (Settings.getLargeUnitsSetting(mContext)) {
    		Amount currentAmount = new Amount(mConsumption, mUnitsPref, mContext);
    		displayAmount = currentAmount.getAmount();
    		displayUnits = currentAmount.getUnits();
    	}
    	
    	final TextView amountTextView = (TextView)findViewById(R.id.consumption_textview);
    	String dailyTotal = String.format("%.1f %s\n", displayAmount, displayUnits);
    	amountTextView.setText(dailyTotal);
    	
    	// Show delta from goal
    	final TextView overUnderTextView = (TextView)findViewById(R.id.over_under_textview);
    	String overUnder = String.format("%+.1f %s (%.1f%%)", delta, originalUnits, percentGoal);
    	overUnderTextView.setText(overUnder);
    	
    	if (delta >= 0) {
    		overUnderTextView.setTextColor(getResources().getColor(R.color.positive_delta));
    	} else {
    		overUnderTextView.setTextColor(getResources().getColor(R.color.negative_delta));
    	}
 
    	// Show current goal setting
    	final TextView goalTextView = (TextView)findViewById(R.id.goal_textview);
    	String goalText = String.format("%s: %.1f %s", getString(R.string.home_daily_goal), prefsGoal, originalUnits);
    	goalTextView.setText(goalText);	
    	
    	// Last entry
    	final TextView lastEntryTextView = (TextView) findViewById(R.id.last_entry_textview);
    	Entry lastEntry = mWaterDB.getLatestEntry(mContentResolver);
    	if (lastEntry == null) {
    		lastEntryTextView.setVisibility(View.INVISIBLE);
    	} else {
    		lastEntryTextView.setVisibility(View.VISIBLE);
    		lastEntryTextView.setText(getLastEntryText(lastEntry.getDate()));
    		//lastEntryTextView.setText(String.format("%s: %s", getString(R.string.home_last_entry), lastEntryMsg));	
    	}
    	
    	mGlassView.setAmount((float)mConsumption);
    	mGlassView.setGoal((float)prefsGoal);
    	mGlassView.invalidate();
    	
    	updateWidget(percentGoal, prefsGoal);
    }
    
    private void updateWidget(double percentGoal, double prefsGoal) {
    	// Broadcast an Intent to update Widget
    	// Use putExtra so AppWidget class does not need
    	// to do ContentProvider pull
    	Intent widgetIntent = new Intent(AppWidget.FORCE_WIDGET_UPDATE);
    	widgetIntent.putExtra("AMOUNT", mConsumption);
    	widgetIntent.putExtra("GOAL", prefsGoal);
    	widgetIntent.putExtra("PERCENT", percentGoal);
    	widgetIntent.putExtra("UNITS", mUnitsPref);
    	this.sendBroadcast(widgetIntent);
    	
    	// Save off current amount, needed if user 
    	// changes unit system settings to update
    	// widget later on
    	SharedPreferences localData = getSharedPreferences(LOCAL_DATA, 0);
    	SharedPreferences.Editor editor = localData.edit();
    	editor.putString("amount", String.valueOf(mConsumption));
    	editor.putString("percent", String.valueOf(percentGoal));
    	
    	// Commit changes
    	editor.commit();
    }
    
    // Override volume keys if user desires
    // depending on settings
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && Settings.getOverrideVolumeUp(this)) {
    		Entry e = new Entry(Settings.getVolumeUpAmount(this), mIsNonMetric);
    		addNewEntry(e);
    		return true;
    	} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && Settings.getOverrideVolumeDown(this)) {
    		undoTodaysLastEntry();
    		return true;
    	} else {
    		return super.onKeyDown(keyCode, event);
    	}
    }
    
    private String getLastEntryText(String lastEntryDate) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
		try {
			Date date = sdf.parse(lastEntryDate);
			long timeInSecondsSinceEpoch = date.getTime();
			return String.format("%s: %s", getString(R.string.home_last_entry), timeAgo(timeInSecondsSinceEpoch));
		} catch (ParseException e) {
		}
    	
    	return "";
    }
    
    // Jamie Bicknell - Twitter Like Time Ago Function
    // http://jamiebicknell.tumblr.com/post/413449751/twitter-like-time-ago-function
    private String timeAgo(long timestamp) {
    	String result = getString(R.string.default_time_ago);
    	Date now = new Date();
    	long timediff = ((now.getTime()) - timestamp) / 1000;
    	long[] units = new long[]{604800,86400,3600,60};
    	String[] unitsStr = new String[]{getString(R.string.time_ago_unit_week),
    			getString(R.string.time_ago_unit_day),
    			getString(R.string.time_ago_unit_hour),
    			getString(R.string.time_ago_unit_minute)
    	};
    	String[] unitsPluralStr = new String[]{getString(R.string.time_ago_unit_week_pl),
    			getString(R.string.time_ago_unit_day_pl),
    			getString(R.string.time_ago_unit_hour_pl),
    			getString(R.string.time_ago_unit_minute_pl)
    	};
    	for (int i = 0; i < 4; i++) {
    		if (units[i] <= timediff) {
    			long value = (long) Math.floor(timediff/units[i]);
    			result = String.format("%d %s %s", value, (value == 1 ? unitsStr[i] : unitsPluralStr[i]), getString(R.string.time_ago_ago));
    			break;
    		}
    	}
    	return result;
    }
}
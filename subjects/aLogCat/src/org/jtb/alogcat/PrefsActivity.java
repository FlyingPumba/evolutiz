package org.jtb.alogcat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import org.jtb.alogcat.R;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private ListPreference mLevelPreference;
	private ListPreference mFormatPreference;
	private ListPreference mBufferPreference;
	private ListPreference mTextsizePreference;
	private ListPreference mBackgroundColorPreference;
	
	private Prefs mPrefs;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);

		mPrefs = new Prefs(this);
		
		mLevelPreference = (ListPreference) getPreferenceScreen()
		.findPreference(Prefs.LEVEL_KEY);
		mFormatPreference = (ListPreference) getPreferenceScreen()
		.findPreference(Prefs.FORMAT_KEY);
		mBufferPreference = (ListPreference) getPreferenceScreen()
		.findPreference(Prefs.BUFFER_KEY);
		mTextsizePreference = (ListPreference) getPreferenceScreen()
		.findPreference(Prefs.TEXTSIZE_KEY);
		mBackgroundColorPreference = (ListPreference) getPreferenceScreen()
		.findPreference(Prefs.BACKGROUND_COLOR_KEY);
		
		setResult(Activity.RESULT_OK);
	}

	private void setLevelTitle() {
		mLevelPreference.setTitle("Level? (" + mPrefs.getLevel().getTitle(this) + ")");
	}

	private void setFormatTitle() {
		mFormatPreference.setTitle("Format? (" + mPrefs.getFormat().getTitle(this) + ")");
	}

	private void setBufferTitle() {
		mBufferPreference.setTitle("Buffer? (" + mPrefs.getBuffer().getTitle(this) + ")");
	}

	private void setTextsizeTitle() {
		mTextsizePreference.setTitle("Text Size? (" + mPrefs.getTextsize().getTitle(this) + ")");
	}
	
	private void setBackgroundColorTitle() {
		mBackgroundColorPreference.setTitle("Background Color? (" + mPrefs.getBackgroundColor().getTitle(this) + ")");
	}

	@Override
	protected void onResume() {
		super.onResume();

		setLevelTitle();
		setFormatTitle();
		setBufferTitle();
		setTextsizeTitle();
		setBackgroundColorTitle();
		
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Prefs.LEVEL_KEY)) {
			setLevelTitle();
		} else if (key.equals(Prefs.FORMAT_KEY)) {
			setFormatTitle();
		} else if (key.equals(Prefs.BUFFER_KEY)) {
			setBufferTitle();
		} else if (key.equals(Prefs.TEXTSIZE_KEY)) {
			setTextsizeTitle();
		} else if (key.equals(Prefs.BACKGROUND_COLOR_KEY)) {
			setBackgroundColorTitle();
		}
	}
}

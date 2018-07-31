/*
 * Preferences.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.os.Bundle;
import android.util.Log;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import java.io.IOException;

public class Preferences extends PreferenceActivity {

	private Mirrored app;
	private String TAG;

	@Override
	protected void onCreate(Bundle icicle) {
		app = (Mirrored)getApplication();
		TAG = app.APP_NAME + ", " + "ArticlesList";

		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (MDebug.LOG)
			Log.d(TAG, "onStop()");

		app.setOfflineMode(app.getBooleanPreference("PrefStartWithOfflineMode", false));
	}
}
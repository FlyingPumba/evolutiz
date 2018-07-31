/*
 * CategoriesList.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.Comparator;

public class CategoriesList extends ListActivity {

	private static final Comparator<String> STRING_COMPARATOR = new Comparator<String>() {
		public int compare(String pString1, String pString2) {
			return pString1.compareTo(pString2);
		}
	};
	private String TAG;
	private Mirrored app;

	private String _categories[];
	private int _counter = 0;

	@Override
	protected void onCreate(Bundle icicle) {
		app = (Mirrored) getApplication();
		TAG = app.APP_NAME + ", " + "CategoriesList";

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		if (prefs.getBoolean("PrefDarkBackground", false)) {
			if (MDebug.LOG)
				Log.d(TAG, "Setting black theme");
			setTheme(android.R.style.Theme_Black);
		}

		super.onCreate(icicle);

		if (MDebug.LOG)
			Log.d(TAG, "Loading categoriesView");

		setContentView(R.layout.categories_list);

		if (MDebug.LOG)
			Log.d(TAG, "Getting categories array resource");
		_categories = getResources().getStringArray(R.array.categories);

		ArrayAdapter<String> notes = new ArrayAdapter<String>(this,
				R.layout.category_row, R.id.category_name,
				Arrays.asList(_categories));
		notes.sort(STRING_COMPARATOR);
		setListAdapter(notes);

		_counter++;
	}

	@Override
	public void onRestart() {
		super.onRestart();
		if (MDebug.LOG)
			Log.d(TAG, "onStart()");
		_counter--;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String category = l.getAdapter().getItem(position).toString()
				.toLowerCase();
		if (MDebug.LOG)
			Log.d(TAG, "putExtra() feedCategory: " + category);

		Intent intent = new Intent(this, ArticlesList.class);
		intent.putExtra(app.EXTRA_CATEGORY, category);
		intent.setAction(Intent.ACTION_VIEW);
		startActivity(intent);

		// only allow one instance of the categories list view
		if (MDebug.LOG)
			Log.d(TAG, "Checking counter: " + _counter);
		if (_counter > 1) {
			if (MDebug.LOG)
				Log.d(TAG,
						"We already have one CategoriesList, finishing this one");
			// this.finish();
		}
	}
}

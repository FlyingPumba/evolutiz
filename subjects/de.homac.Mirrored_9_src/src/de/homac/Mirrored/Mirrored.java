/*
 * Mirrored.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.app.Application;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.InputStream;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.StringBuilder;

public class Mirrored extends Application {

	public String APP_NAME;

	private String TAG;

	private Article _article;
	private FeedSaver _feedSaver;
	private int _categoriesListCounter = 0;
	private boolean _offline_mode = false;

	public enum Orientation { HORIZONTAL, VERTICAL }
	public Orientation screenOrientation = null;

	static public final String EXTRA_CATEGORY = "category";

	static public final String BASE_CATEGORY = "schlagzeilen";

 	public void setArticle(Article article) {
		this._article = article;
	}

	public Article getArticle() {
		return _article;
	}

 	public void setFeedSaver(FeedSaver feedSaver) {
		this._feedSaver = feedSaver;
	}

	public FeedSaver getFeedSaver() {
		return _feedSaver;
	}

	public void setCategoriesListCounter(int count) {
		_categoriesListCounter = count;
	}

	public int getCategoriesListCounter() {
		return _categoriesListCounter;
	}

	@Override
	public void onCreate() {

		APP_NAME = getString(R.string.app_name);
		TAG = APP_NAME;
		if (MDebug.LOG)
			Log.d(TAG, "starting");

		setOfflineMode(getBooleanPreference("PrefStartWithOfflineMode", false));
	}

	@Override
	public void onTerminate() {
		if (MDebug.LOG)
			Log.d(TAG, "onTerminate()");
	}

	public boolean online() {
		if (_offline_mode)
			return false;

		ConnectivityManager cm = (ConnectivityManager)this
			.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = (NetworkInfo)cm.getActiveNetworkInfo();
		boolean online = true;

		if (info == null || !info.isConnectedOrConnecting()){
			online = false;
		}

		if (info != null && info.isRoaming()){
			//here is the roaming option you can change it if you want to disable
			//internet while roaming, just return false
			online = false;
		}

		if (online) {
			if (MDebug.LOG)
				Log.d(TAG, "Internet state: online");
		} else {
			if (MDebug.LOG)
				Log.d(TAG, "Internet state: offline");
		}

		return online;
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"), 8*1024);
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {        
			return "";
		}
	}

	public void setOfflineMode(boolean offline) {
		if (MDebug.LOG)
			Log.d(TAG, "Setting offline mode to "+offline);
		_offline_mode = offline;
	}

	public boolean getBooleanPreference(String pref, boolean def) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());

		return prefs.getBoolean(pref, def);
	}

	public String getStringPreference(String pref, String def) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());
		return prefs.getString(pref, def);
	}

	public int getIntPreference(String pref, int def) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getBaseContext());

		return prefs.getInt(pref, def);
	}

	public void showDialog(Activity activity, String text) {
		AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setMessage(text);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				} });
		alertDialog.show();
	}
}

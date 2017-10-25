package com.frankcalise.h2droid;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class WaterDB {
	private static final WaterDB instance = new WaterDB();
	
	public static WaterDB getInstance() {
		return instance;
	}
	
	public void addNewEntry(ContentResolver contentResolver, Entry entry) {
		ContentValues values = new ContentValues();
    	
    	values.put(WaterProvider.KEY_DATE, entry.getDate());
    	values.put(WaterProvider.KEY_AMOUNT, entry.getMetricAmount());
    	
    	contentResolver.insert(WaterProvider.CONTENT_URI, values);
	}
	
	public Entry getLatestEntry(ContentResolver contentResolver) {
		Entry entry = null;
    	String sortOrder = WaterProvider.KEY_DATE + " DESC LIMIT 1";
    	//String[] projection = {WaterProvider.KEY_DATE};
    	// projection would be second param
    	
    	Cursor c = contentResolver.query(WaterProvider.CONTENT_URI, null, null, null, sortOrder);
    	if (c.moveToFirst()) {
    		int id = c.getInt(WaterProvider.ID_COLUMN);
			String date = c.getString(WaterProvider.DATE_COLUMN);
			double metricAmount = c.getDouble(WaterProvider.AMOUNT_COLUMN);
			boolean isNonMetric = false;    			
			
			entry = new Entry(id, date, metricAmount, isNonMetric);
    	}
    	c.close();
    	
    	return entry;
	}
	
	public boolean deleteLastEntryFromToday(ContentResolver contentResolver) {
		Date now = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	String sortOrder = WaterProvider.KEY_DATE + " DESC LIMIT 1";
    	String[] projection = {WaterProvider.KEY_ID};
    	String where = "'" + sdf.format(now) + "' = date(" + WaterProvider.KEY_DATE + ")";
    	
    	Cursor c = contentResolver.query(WaterProvider.CONTENT_URI, projection, where, null, sortOrder);
    	int results = 0;
    	if (c.moveToFirst()) {
    		final Uri uri = Uri.parse(WaterProvider.CONTENT_URI + "/" + c.getInt(0));
    		results = contentResolver.delete(uri, null, null);
    	} 
    	c.close();
    	
    	if (results > 0)
    		return true;
    	else
    		return false;
	}
	
	public WaterEntryList getEntriesFromToday(ContentResolver contentResolver) {
		WaterEntryList list = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date();
    	String where = "'" + sdf.format(now) + "' = date(" + WaterProvider.KEY_DATE + ")";
    	
    	// Return all saved entries
    	Cursor c = contentResolver.query(WaterProvider.CONTENT_URI,
    					    null, where, null, null);
    	
    	if (c.moveToFirst()) {
    		list = new WaterEntryList();
    		do {
    			String date = c.getString(WaterProvider.DATE_COLUMN);
    			double metricAmount = c.getDouble(WaterProvider.AMOUNT_COLUMN);
    			Entry entry = new Entry(date, metricAmount, false);
    			list.add(entry);
    		} while (c.moveToNext());
    	}
    	
    	c.close();
    	
    	return list;
	}
}

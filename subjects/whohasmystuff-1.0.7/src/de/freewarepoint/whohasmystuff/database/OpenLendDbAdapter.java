/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.freewarepoint.whohasmystuff.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.freewarepoint.whohasmystuff.LentObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.freewarepoint.whohasmystuff.AbstractListIntent.LOG_TAG;

public class OpenLendDbAdapter {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATE = "date";
    public static final String KEY_PERSON = "person";
	public static final String KEY_PERSON_KEY = "person_key";
    public static final String KEY_BACK = "back";
    public static final String KEY_CALENDAR_ENTRY = "calendar_entry";
    public static final String KEY_ROWID = "_id";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String LENTOBJECTS_DATABASE_CREATE =
        "create table lentobjects (" + KEY_ROWID + " integer primary key autoincrement, "
        + KEY_DESCRIPTION + " text not null, " + KEY_TYPE + " integer, " + KEY_DATE + " date not null, "
        + KEY_PERSON + " text not null, " + KEY_PERSON_KEY + " text, "
		+ KEY_BACK + " integer not null, " + KEY_CALENDAR_ENTRY + " text);";

    private static final String DATABASE_NAME = "data";
    private static final String LENTOBJECTS_DATABASE_TABLE = "lentobjects";
    static final int DATABASE_VERSION = 3;

    private static final String CREATE_CALENDAR_ENTRY_COLUMN =
            "ALTER TABLE " + LENTOBJECTS_DATABASE_TABLE + " ADD COLUMN " + KEY_CALENDAR_ENTRY + " text";
    
    private static final String CREATE_TYPE_COLUMN =
            "ALTER TABLE " + LENTOBJECTS_DATABASE_TABLE + " ADD COLUMN " + KEY_TYPE + " integer";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(LENTOBJECTS_DATABASE_CREATE);

            Date now = new Date();
            String personName = "\"Who Has My Stuff?\" Test User";

            createLentObject(db, "Example entry", 0, now, personName, null, false);
            createLentObject(db, "Press menu button to add entries", 0, now, personName, null, false);
        }

        public void createWithoutExampleData(SQLiteDatabase db) {
            db.execSQL(LENTOBJECTS_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

            if (oldVersion < 2) {
                db.execSQL(CREATE_CALENDAR_ENTRY_COLUMN);
            }

            if (oldVersion < 3) {
                db.execSQL(CREATE_TYPE_COLUMN);
            }

        }

        public long createLentObject(SQLiteDatabase db, String description, int type, Date date,
                                     String personName, String personKey, boolean returned) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_DESCRIPTION, description);
            initialValues.put(KEY_TYPE, type);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            initialValues.put(KEY_DATE, dateFormat.format(date));
            initialValues.put(KEY_PERSON, personName);
            initialValues.put(KEY_PERSON_KEY, personKey);
            initialValues.put(KEY_BACK, returned);

            return db.insert(LENTOBJECTS_DATABASE_TABLE, null, initialValues);
        }
    }

    public OpenLendDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public OpenLendDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public void clearDatabase() {
        mDb.execSQL("DROP TABLE IF EXISTS lentobjects");
        mDbHelper.createWithoutExampleData(mDb);
    }

    public long createLentObject(LentObject lentObject) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DESCRIPTION, lentObject.description);
        initialValues.put(KEY_TYPE, lentObject.type);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initialValues.put(KEY_DATE, dateFormat.format(lentObject.date));
        initialValues.put(KEY_PERSON, lentObject.personName);
		initialValues.put(KEY_PERSON_KEY, lentObject.personKey);
        initialValues.put(KEY_BACK, lentObject.returned);
        if (lentObject.calendarEventURI != null) {
            initialValues.put(KEY_CALENDAR_ENTRY, lentObject.calendarEventURI.toString());
        }

        return mDb.insert(LENTOBJECTS_DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteLentObject(long rowId) {
        return mDb.delete(LENTOBJECTS_DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

	public Cursor fetchAllObjects() {
		return mDb.query(LENTOBJECTS_DATABASE_TABLE, new String[] {KEY_ROWID,
				KEY_DESCRIPTION, KEY_TYPE, KEY_DATE, KEY_PERSON, KEY_PERSON_KEY, KEY_BACK, KEY_CALENDAR_ENTRY}, null, null, null, null, KEY_DATE + " ASC");
	}

    public Cursor fetchLentObjects() {
        return mDb.query(LENTOBJECTS_DATABASE_TABLE, new String[] {KEY_ROWID,
                KEY_DESCRIPTION, KEY_TYPE, KEY_DATE, KEY_PERSON, KEY_PERSON_KEY, KEY_BACK, KEY_CALENDAR_ENTRY}, KEY_BACK + "=0", null, null, null, KEY_DATE + " ASC");
    }

	public Cursor fetchReturnedObjects() {
		return mDb.query(LENTOBJECTS_DATABASE_TABLE, new String[] {KEY_ROWID,
				KEY_DESCRIPTION, KEY_TYPE, KEY_DATE, KEY_PERSON, KEY_PERSON_KEY, KEY_BACK, KEY_CALENDAR_ENTRY}, KEY_BACK + "=1", null, null, null, KEY_DATE + " ASC");
	}

    public boolean updateLentObject(long rowId, LentObject lentObject) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, lentObject.description);
        args.put(KEY_TYPE, lentObject.type);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        args.put(KEY_DATE, dateFormat.format(lentObject.date));
        args.put(KEY_PERSON, lentObject.personName);
		args.put(KEY_PERSON_KEY, lentObject.personKey);

		return updateLentObject(rowId, args);
    }

	public boolean markLentObjectAsReturned(long rowId) {
		ContentValues values = new ContentValues();
		values.put(KEY_BACK, true);
		return updateLentObject(rowId, values);
	}

	private boolean updateLentObject(long rowId, ContentValues values) {
		return mDb.update(LENTOBJECTS_DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}
}

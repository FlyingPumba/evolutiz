/*
 * Tomdroid
 * Tomboy on Android
 * http://www.launchpad.net/tomdroid
 * 
 * Copyright 2009 Olivier Bilodeau <olivier@bottomlesspit.org>
 * Copyright 2009 Benoit Garret <benoit.garret_launchpad@gadz.org>
 * 
 * This file is part of Tomdroid.
 * 
 * Tomdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Tomdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Tomdroid.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Parts of this file is Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file was inspired by com.example.android.notepad.NotePadProvider 
 * available in the Android SDK. 
 */
package org.tomdroid;

import java.util.HashMap;
import java.util.UUID;

import org.tomdroid.ui.Tomdroid;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NoteProvider extends ContentProvider {
	
	// ContentProvider stuff
	// --	
	private static final String DATABASE_NAME = "tomdroid-notes.db";
	private static final String DB_TABLE_NOTES = "notes";
	private static final int DB_VERSION = 3;
	private static final String DEFAULT_SORT_ORDER = Note.MODIFIED_DATE + " DESC";
	
    private static HashMap<String, String> notesProjectionMap;

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;
    private static final int NOTE_TITLE = 3;

    private static final UriMatcher uriMatcher;
    
    // Logging info
    private static final String TAG = "NoteProvider";

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DB_TABLE_NOTES	 + " ("
                    + Note.ID + " INTEGER PRIMARY KEY,"
                    + Note.GUID + " TEXT,"
                    + Note.TITLE + " TEXT,"
                    + Note.FILE + " TEXT,"
                    + Note.NOTE_CONTENT + " TEXT,"
                    + Note.MODIFIED_DATE + " STRING,"
                    + Note.TAGS + " STRING"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	if (Tomdroid.LOGGING_ENABLED) {
        		Log.d(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        	}
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
        case NOTES:
            qb.setTables(DB_TABLE_NOTES);
            qb.setProjectionMap(notesProjectionMap);
            break;

        case NOTE_ID:
            qb.setTables(DB_TABLE_NOTES);
            qb.setProjectionMap(notesProjectionMap);
            qb.appendWhere(Note.ID + "=" + uri.getPathSegments().get(1));
            break;
            
        case NOTE_TITLE:
        	qb.setTables(DB_TABLE_NOTES);
        	qb.setProjectionMap(notesProjectionMap);
        	// TODO appendWhere + whereArgs instead (new String[] whereArgs = uri.getLas..)?
        	qb.appendWhere(Note.TITLE + " LIKE '" + uri.getLastPathSegment()+"'");
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        

        // Get the database and run the query
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case NOTES:
            return Tomdroid.CONTENT_TYPE;

        case NOTE_ID:
            return Tomdroid.CONTENT_ITEM_TYPE;
            
        case NOTE_TITLE:
        	return Tomdroid.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    // TODO the following method is probably never called and probably wouldn't work
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (uriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        // TODO either be identical to Tomboy's time format (if sortable) else make sure that this is documented
        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(Note.MODIFIED_DATE) == false) {
            values.put(Note.MODIFIED_DATE, now);
        }
        
        // The guid is the unique identifier for a note so it has to be set.
        if (values.containsKey(Note.GUID) == false) {
        	values.put(Note.GUID, UUID.randomUUID().toString());
        }

        // TODO does this make sense?
        if (values.containsKey(Note.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(Note.TITLE, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(Note.FILE) == false) {
            values.put(Note.FILE, "");
        }
        
        if (values.containsKey(Note.NOTE_CONTENT) == false) {
            values.put(Note.NOTE_CONTENT, "");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(DB_TABLE_NOTES, Note.FILE, values); // not so sure I did the right thing here
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Tomdroid.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
        case NOTES:
            count = db.delete(DB_TABLE_NOTES, where, whereArgs);
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(DB_TABLE_NOTES, Note.ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
        case NOTES:
            count = db.update(DB_TABLE_NOTES, values, where, whereArgs);
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(DB_TABLE_NOTES, values, Note.ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Tomdroid.AUTHORITY, "notes", NOTES);
        uriMatcher.addURI(Tomdroid.AUTHORITY, "notes/#", NOTE_ID);
        uriMatcher.addURI(Tomdroid.AUTHORITY, "notes/*", NOTE_TITLE);

        notesProjectionMap = new HashMap<String, String>();
        notesProjectionMap.put(Note.ID, Note.ID);
        notesProjectionMap.put(Note.GUID, Note.GUID);
        notesProjectionMap.put(Note.TITLE, Note.TITLE);
        notesProjectionMap.put(Note.FILE, Note.FILE);
        notesProjectionMap.put(Note.NOTE_CONTENT, Note.NOTE_CONTENT);
        notesProjectionMap.put(Note.TAGS, Note.TAGS);
        notesProjectionMap.put(Note.MODIFIED_DATE, Note.MODIFIED_DATE);
    }
}

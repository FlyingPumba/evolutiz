/*
 * Copyright (C) 2011 The original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.zapta.apps.maniana.services;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.zapta.apps.maniana.persistence.ModelPersistence;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * A content provider that exposes the data file for read only. Used for attaching the data file to
 * SEND intent for backup.
 * 
 * Adapted from an example by Stephen Nicholas here http://stephendnicholas.com/archives/974
 * 
 * @author Tal Dayan
 */
public class BackupFileProvider extends ContentProvider {

    // private static final String CLASS_NAME = "CachedFileProvider";

    // The authority is the symbolic name for the provider class
    public static final String AUTHORITY = "com.zapta.apps.maniana.BACKUP_FILE_PROVIDER";

    // Arbitrary code to return on match. Any value other than -1 should do.
    private static final int MATCHED_OK = 1;

    // UriMatcher used to match against incoming requests
    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add a URI to the matcher which will match against the form
        // 'content://com.stephendnicholas.gmailattach.provider/*'
        // and return 1 in the case that the incoming Uri matches this pattern
        uriMatcher.addURI(AUTHORITY, "*", MATCHED_OK);

        return true;
    }

    // This is called by the selected 'share' app to get the file data.
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        LogUtil.info("Provider.openFile() incoming uri: %s", uri);
        final int matchStatus = uriMatcher.match(uri);
        if (matchStatus == MATCHED_OK) {
            // We always return the data file, regardless of the file name in the uri.
            String fileLocation = getContext().getFilesDir() + File.separator
                   + ModelPersistence.DATA_FILE_NAME;
          
            // Always returning in read only mode, regardless of the requested mode.
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(fileLocation),
                    ParcelFileDescriptor.MODE_READ_ONLY);
            return pfd;
        }

        LogUtil.error("Unsupported uri: %s", uri);
        throw new FileNotFoundException("Unsupported uri: " + uri.toString());
    }

    // Trivial implementation of abstract methods we don't really use.

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String s, String[] as1, String s1) {
        return null;
    }

    /**
     * @param fileName the saved file name. Should have a '.json' extension.
     */
    public static Intent constructBackupFileSendIntent(String fileName) {
        final Intent intent = new Intent(Intent.ACTION_SEND);

        // This determines the saved file name.
        intent.putExtra(Intent.EXTRA_SUBJECT, fileName);

        intent.setType("application/json");

        // The data provider ignores the file name and always returns a copy of the maniaia data
        // file.
        intent.putExtra(Intent.EXTRA_STREAM,
                Uri.parse("content://" + BackupFileProvider.AUTHORITY + "/" + fileName));

        return intent;
    }
}

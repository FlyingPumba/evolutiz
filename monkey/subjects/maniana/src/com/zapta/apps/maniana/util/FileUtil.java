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

package com.zapta.apps.maniana.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.Bitmap;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult.FileReadOutcome;

/**
 * File read/write utils.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public final class FileUtil {

    /** Do not instantiate */
    private FileUtil() {
    }

    /** The result of a file read operation. */
    public static class FileReadResult {
        public static enum FileReadOutcome {
            READ_OK,
            NOT_FOUND,
            READ_ERROR;
            
            public final boolean isOk() {
                return this == READ_OK;
            }
        }

        public final FileReadOutcome outcome;

        @Nullable
        public final String content;

        private FileReadResult(FileReadOutcome outcome, @Nullable String content) {
            this.outcome = outcome;
            this.content = content;
        }
    }

    /**
     * Read a file into a string.
     * 
     * @param context the activity context.
     * @param fileName the file name.
     * @param isAsset if true, the file is an asset file. Otherwise, it is a private app file.
     */
    @Nullable
    public static FileReadResult readFileToString(Context context, String fileName, boolean isAsset) {
        // Open
        final InputStream in;
        try {
            in = isAsset ? context.getAssets().open(fileName) : context.openFileInput(fileName);
        } catch (IOException e) {
            // This is normal when opening the file after first installation since it
            // does not exist.
            LogUtil.warning(e, "Error opening a file");
            return new FileReadResult(FileReadOutcome.NOT_FOUND, null);
        }

        // This always closes in.
        return readFileToString(in, fileName);
    }

    /**
     * Read a file into a string. Upon return, in is guaranteed to be closed.
     */
    @Nullable
    public static FileReadResult readFileToString(InputStream in, String fileDescription) {
        // Read
        try {
            final ByteArrayOutputStream builder = new ByteArrayOutputStream();
            final byte bfr[] = new byte[100];
            for (;;) {
                final int n = in.read(bfr);
                if (n < 1) {
                    break;
                }
                builder.write(bfr, 0, n);
            }
            return new FileReadResult(FileReadOutcome.READ_OK, builder.toString());
        } catch (IOException e) {
            LogUtil.error(e, "Error reading file %s", fileDescription);
            return new FileReadResult(FileReadOutcome.READ_ERROR, null);

        } finally {
            // Close
            try {
                in.close();
            } catch (IOException e) {
                LogUtil.error("Failed to close file from reading: " + fileDescription);
            }
        }
    }

    /** Write a string to a file */
    public static void writeStringToFile(Context context, String content, String fileName,
            int contextMode) {
        FileOutputStream out = null;
        try {
            out = context.openFileOutput(fileName, contextMode);
            out.write(content.getBytes());
        } catch (Exception e) {
            // TODO: more graceful error handling?
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LogUtil.error(e, "Error closing written model file: " + fileName);
                }
            }
        }
    }

    /** Write a bitmap to a file */
    public static void writeBitmapToPngFile(Context context, Bitmap bitmap, String fileName,
            boolean isPublic) {
        FileOutputStream out = null;
        try {
            final int permission = isPublic ? Context.MODE_WORLD_READABLE : Context.MODE_PRIVATE;
            out = context.openFileOutput(fileName, permission);
            final boolean ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            if (!ok) {
                throw new RuntimeException("Error writing bitmap to file: " + fileName);
            }
        } catch (Exception e) {
            // TODO: more graceful error handling?
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LogUtil.error(e, "Error closing written bitmap file: " + fileName);
                }
            }
        }
    }
    
    /** 
     * Path is relative to .../assets/.
     * If not null, caller should eventually close the returned stream.
     */
    @Nullable
    public static InputStream openAssert(Context context, String path) {       
        try {
            return context.getAssets().open(path);
        } catch (IOException e) {
            LogUtil.info("Asset %s did not open (%s)", path, e.getClass().getSimpleName());
            return null;
        }    
    }
}

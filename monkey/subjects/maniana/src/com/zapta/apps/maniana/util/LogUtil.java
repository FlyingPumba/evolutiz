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

import com.zapta.apps.maniana.annotations.ApplicationScope;

import android.util.Log;

/**
 * Provide log printing utilities.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public final class LogUtil {

    /** The current debug level. */
    public static int DEBUG_LEVEL = 0;

    /** The log tag to use. */
    private static final String TAG = "Maniana";

    /** Do not instantiate */
    private LogUtil() {
    }

    // Debug
    public static final void debug(String format, Object... args) {
        Log.d(TAG, String.format(format, args));
    }

    public static final void debug(String message) {
        Log.d(TAG, message);
    }

    public static final void debug(Throwable e, String format, Object... args) {
        Log.d(TAG, String.format(format, args), e);
    }

    public static final void debug(Throwable e, String message) {
        Log.d(TAG, message, e);
    }

    // Info
    public static final void info(String format, Object... args) {
        Log.i(TAG, String.format(format, args));
    }

    public static final void info(String message) {
        Log.i(TAG, message);
    }

    public static final void info(Throwable e, String format, Object... args) {
        Log.i(TAG, String.format(format, args), e);
    }

    public static final void info(Throwable e, String message) {
        Log.i(TAG, message, e);
    }

    // Warning
    public static final void warning(String format, Object... args) {
        Log.w(TAG, String.format(format, args));
    }

    public static final void warning(String message) {
        Log.w(TAG, message);
    }

    public static final void warning(Throwable e, String format, Object... args) {
        Log.w(TAG, String.format(format, args), e);
    }

    public static final void warning(Throwable e, String message) {
        Log.w(TAG, message, e);
    }

    // Error
    public static final void error(String format, Object... args) {
        Log.e(TAG, String.format(format, args));
    }

    public static final void error(String message) {
        Log.e(TAG, message);
    }

    public static final void error(Throwable e, String format, Object... args) {
        Log.e(TAG, String.format(format, args), e);
    }

    public static final void error(Throwable e, String message) {
        Log.e(TAG, message, e);
    }
}

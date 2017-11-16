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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * @author Tal Dayan
 */
@ApplicationScope
public final class PackageUtil {

    /** Do not instantiate */
    private PackageUtil() {
    }

    /** Safe to call also from widget or from non main activities. */
    public static final PackageInfo getPackageInfo(Context context) {
        final PackageManager manager = context.getPackageManager();
        try {
            return manager.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Failed to access package info", e);
        }
    }
}

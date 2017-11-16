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

package com.zapta.apps.maniana.main;

import android.app.Application;
import android.preference.PreferenceManager;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.settings.PreferencesReader;
import com.zapta.apps.maniana.util.LogUtil;

@ApplicationScope
public class MyApp extends Application {

    /** For debugging. */
    public final int objectId;

    private PreferencesReader mPreferencesReader;

    public MyApp() {
        this.objectId = System.identityHashCode(this);    
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mPreferencesReader = new PreferencesReader(this,
                PreferenceManager.getDefaultSharedPreferences(this));
        LogUtil.debug("App object onCreate(): %d, thread %s", objectId,
                System.identityHashCode(Thread.currentThread()));
    }
    
    public final PreferencesReader preferencesReader() {
        return mPreferencesReader;
    }
}

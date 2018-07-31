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

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
import android.content.Context;

import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.controller.Controller;
import com.zapta.apps.maniana.debug.DebugController;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.services.MainActivityServices;
import com.zapta.apps.maniana.services.DateTracker;
import com.zapta.apps.maniana.settings.DateOrder;
import com.zapta.apps.maniana.settings.PreferenceKind;
import com.zapta.apps.maniana.settings.PreferencesReader;
import com.zapta.apps.maniana.settings.PreferencesTracker;
import com.zapta.apps.maniana.util.PopupsTracker;
import com.zapta.apps.maniana.view.AppView;

/**
 * Represents the global state of the main activity.
 * 
 * @author Tal Dayan.
 */
@MainActivityScope
public class MainActivityState {
    
    private final MyApp mApp;

    private final MainActivity mMainActivity;

    private final PreferencesReader mPreferencesReader;
    
    private final PreferencesTracker mPreferencesTracker;

    private final DateTracker mDateTracker;
    //= new DateTracker();

    private MainActivityServices mServices;

    /** Task data. */
    private AppModel mModel;

    /** The app controller. Contains the main app logic. */
    private Controller mController;

    /** Debug mode operations. */
    private DebugController mDebugController;

    /** The main activity view. */
    private AppView mView;

    /** The open dialog tracker. */
    private final PopupsTracker mPopupsTracker = new PopupsTracker();

    MainActivityState(MainActivity mainActivity) {
        mDateTracker = new DateTracker(DateOrder.localDateOrder(mainActivity));
        mMainActivity = checkNotNull(mainActivity);
        mModel = new AppModel();
        mApp = (MyApp) mainActivity.getApplication();
        mPreferencesReader = mApp.preferencesReader();
        mPreferencesTracker = new PreferencesTracker(mApp.preferencesReader(),
                new PreferencesTracker.PreferenceChangeListener() {
                    @Override
                    public void onPreferenceChange(PreferenceKind preferenceKind) {
                        if (mController != null) {
                            mController.onPreferenceChange(preferenceKind);
                        }
                    }
                });
        mServices = new MainActivityServices(this);
        mDebugController = new DebugController(this);
        mController = new Controller(this);
        mView = new AppView(this);
    }
    
    public final MyApp app() {
        return mApp;
    }

    public final MainActivity mainActivity() {
        return mMainActivity;
    }

    public final Context context() {
        // The main activity is also the context.
        return (Context) mMainActivity;
    }

    // A convenience shortcut.
    public final String str(int resourceId) {
        return mMainActivity.getString(resourceId);
    }

    // A convenience shortcut.
    public final String str(int resourceId, Object... args) {
        return mMainActivity.getString(resourceId, args);
    }

    public PreferencesTracker prefTracker() {
        return mPreferencesTracker;
    }
    
    public PreferencesReader prefReader() {
        return mPreferencesReader;
    }

    public final DateTracker dateTracker() {
        return mDateTracker;
    }

    public final PopupsTracker popupsTracker() {
        return mPopupsTracker;
    }

    public final MainActivityServices services() {
        return mServices;
    }

    public final AppModel model() {
        return mModel;
    }

    public final Controller controller() {
        return mController;
    }

    public final AppView view() {
        return mView;
    }

    public final DebugController debugController() {
        return mDebugController;
    }
}

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

import javax.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

import com.zapta.apps.maniana.controller.MainActivityStartupKind;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.persistence.ModelPersistence;
import com.zapta.apps.maniana.persistence.ModelReadingResult;
import com.zapta.apps.maniana.settings.Font;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * The main activity of this app.
 * 
 * @author Tal Dayan
 */
public class MainActivity extends Activity {

    /** A light weight snapshot of the activity state transfered across orientation change. */
    private static class RetainedState {
        public final PageKind currentPageKind;

        private RetainedState(PageKind currentPageKind) {
            this.currentPageKind = currentPageKind;
        }
    }

    private MainActivityState mState;

    @Nullable
    private RetainedState mRetainedState;

    /** Used to pass resume action from onNewIntent() to onResume(). */
    private MainActivityResumeAction mResumeAction = MainActivityResumeAction.NONE;

    /** Contains the intent that triggered mResumeAction. Null FF action = NONE. */
    @Nullable
    private Intent mResumeIntent = null;

    /** Called by the Android framework to initialize the activity. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // TODO: This is a hack. Move an an actual config change listener in Application class.
        Font.onConfigChanged();

        mRetainedState = (RetainedState) getLastNonConfigurationInstance();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // App context Ties all the app pieces together.
        mState = new MainActivityState(this);

        // Load model from file
        final ModelReadingResult modelLoadResult = ModelPersistence.readModelFile(mState.context(),
                mState.model());

        final MainActivityStartupKind startupKind;
        switch (modelLoadResult.outcome) {
            case FILE_READ_OK: {
                final int oldVersionCode = modelLoadResult.metadata.writerVersionCode;
                final int newVersionCode = mState.services().getAppVersionCode();
                final boolean isSameVersion = (oldVersionCode == newVersionCode);
                final boolean isSilentUpgrade = isSilentUpgrade(oldVersionCode, newVersionCode);
                startupKind = isSameVersion ? MainActivityStartupKind.NORMAL
                        : (isSilentUpgrade ? MainActivityStartupKind.NEW_VERSION_SILENT
                                : MainActivityStartupKind.NEW_VERSION_ANNOUNCE);
                break;
            }
            case FILE_NOT_FOUND: {
                // Model should be empty here
                startupKind = MainActivityStartupKind.NEW_USER;
                // Prevent moving item from Tomorow to Today.
                mState.model().setLastPushDateStamp(mState.dateTracker().getDateStampString());
                break;
            }

            default:
                LogUtil.error("Unknown model loading outcome: " + modelLoadResult.outcome);
                // Falling through intentionally.
            case FILE_HAS_ERRORS:
                mState.model().clear();
                mState.model().setLastPushDateStamp(mState.dateTracker().getDateStampString());
                startupKind = MainActivityStartupKind.MODEL_DATA_ERROR;
        }

        // Inform the view about the model data change
        mState.view().updatePages();

        // Set top view of this activity
        setContentView(mState.view().getRootView());

        // Track resume action from the launch intent
        trackResumeAction(getIntent());

        // Tell the controller the app was just created.
        mState.controller().onMainActivityCreated(startupKind);

    }

    /** Is this a minor upgrade that should supress the startup message? */
    private static final boolean isSilentUpgrade(int oldVersionCode, int newVersionCode) {
        // Return here true if combination of old and new version code does not warrant
        // bothering some users with the What's New popup.

        // By default, upgrdes are not silent.
        return false;
    }

    /** Called by the framework when the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Tell the controller the app is being destroyed.
        mState.controller().onMainActivityDestroy();
        // Make sure we release the preferences listener.
        mState.prefTracker().release();
    }

    /** Called by the framework when this activity is paused. */
    @Override
    protected void onPause() {
        super.onPause();
        mResumeIntent = null;
        mResumeAction = MainActivityResumeAction.NONE;
        // Inform the controller.
        mState.controller().onMainActivityPause();
    }

    /** Called by the framework when this activity is resumed. */
    @Override
    protected void onResume() {
        super.onResume();

        // Get the action for this resume
        final Intent thisResumeIntent = mResumeIntent;
        final MainActivityResumeAction thisResumeAction;
        // On ICS and above, orientation change result in the activity destroyed and recreated.
        // In this case, use the retained state to preserve the original page.
        if (mRetainedState != null) {
            thisResumeAction = mRetainedState.currentPageKind.isTomorrow() ? MainActivityResumeAction.FORCE_TOMORROW_PAGE
                    : MainActivityResumeAction.FORCE_TODAY_PAGE;
        } else {
            thisResumeAction = mResumeAction;
        }

        mResumeIntent = null;
        mResumeAction = MainActivityResumeAction.NONE;
        mRetainedState = null;

        // Inform the controller
        mState.controller().onMainActivityResume(thisResumeAction, thisResumeIntent);
    }

    @Override
    @Nullable
    public Object onRetainNonConfigurationInstance() {
        // NOTE: Gingerbread handles orientation changes well, including preserving popups, as is.
        // Later versions of Android restart the activity on orientation change and require explicit
        // state retention.
        return (android.os.Build.VERSION.SDK_INT < 13) ? null : new RetainedState(mState.view()
                .getCurrentPageKind());
    }

    // NOTE: this is not called when a popup menu is actie.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean eventHandled = false;
        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    eventHandled = mState.controller().onBackButton();
                    break;
                case KeyEvent.KEYCODE_MENU:
                    eventHandled = mState.controller().onMenuButton();
                    break;
            }
        }

        return eventHandled || super.onKeyDown(keyCode, event);
    }

    /** Delegates sub sctivities result to the controller. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        mState.controller().onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * Called when the activity receives an intent. Used to detect launches from list widget action
     * buttons.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        trackResumeAction(intent);
    }

    /** Update the resume action from the given launch intent. */
    private final void trackResumeAction(Intent launchIntent) {
        mResumeIntent = launchIntent;
        mResumeAction = MainActivityResumeAction.fromIntent(mState, launchIntent);
    }
}

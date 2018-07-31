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

import android.content.Intent;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.util.IntentUtil;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * The main activity is always resumed with one of these actions.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum MainActivityResumeAction {
    /** No action. */
    NONE,
    /** Make today page visible. Allows animations.*/
    SHOW_TODAY_PAGE,
    /** Make today page visible. No animations.*/
    FORCE_TODAY_PAGE,
    /** Make tomorrow page visible. No animations. */
    FORCE_TOMORROW_PAGE,
    /** Make today page visible and open text editor */
    ADD_NEW_ITEM_BY_TEXT,
    /** Make today page visible and open voice recognition */
    ADD_NEW_ITEM_BY_VOICE,
    /** Restore model from backup file passed by the intent's EXTRA_STREAM */
    RESTORE_FROM_BABKUP_FILE;

    /** Key for serializing resume actions in intents. Not persisted. */
    private static final String RESUME_ACTION_KEY = "maniana_resume_action";

    /** Default action when action is not specified in the launch intent. */
    private static final MainActivityResumeAction DEFAULT_ACTION = SHOW_TODAY_PAGE;

    public boolean isNone() {
        return this == NONE;
    }
    
    public boolean isForceTomorowPage() {
        return this == FORCE_TOMORROW_PAGE;
    }
    
    public boolean allowsAnimations() {
        return (this == NONE || this == SHOW_TODAY_PAGE);
    }

    /** Serialize a resume action in an intent. */
    public static void setInIntent(Intent intent, MainActivityResumeAction resumeAction) {
        intent.putExtra(RESUME_ACTION_KEY, resumeAction.toString());
    }

    /** Deserialize a resume action from an intent */
    @MainActivityScope
    public static MainActivityResumeAction fromIntent(MainActivityState mainActivityState, Intent intent) {
        // Comment out intent dump
        IntentUtil.dumpIntent(intent, false);

        // This is the kind of intent thrown by Gmail when clicking on an attachment
        // Download button.
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && (intent.getData() != null)) {
            final String uriStringLC = intent.getDataString().toLowerCase();
            // OI file browser sends type = "*/*" so we try to also match the extension.
            if ("application/json".equals(intent.getType()) || uriStringLC.contains(".json")) {
                return RESTORE_FROM_BABKUP_FILE;
            }
        }

        @Nullable
        final String strValue = intent.getStringExtra(RESUME_ACTION_KEY);
        if (strValue == null) {
            return DEFAULT_ACTION;
        }

        @Nullable
        final MainActivityResumeAction value = MainActivityResumeAction.valueOf(strValue);
        if (value == null) {
            LogUtil.error("Unknown resume action string: [%s]", strValue);
            return DEFAULT_ACTION;
        }

        return value;
    }
}

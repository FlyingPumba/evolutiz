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

package com.zapta.apps.maniana.settings;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import javax.annotation.Nullable;

import com.zapta.apps.maniana.annotations.ActivityScope;

import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;

/**
 * Switcher between two preferences. At any given time, exactly one is dispalyed based on a checkbox
 * preference state.
 * 
 * @author Tal Dayan
 */
@ActivityScope
public class PreferenceSelector {
    private final PreferenceGroup mGroup;
    private final CheckBoxPreference mCheckbox;

    @Nullable
    private final Preference mPref1;

    @Nullable
    private final Preference mPref2;

    private boolean lastUpdatedState;

    /** On construction, group contains both preferences. */
    public PreferenceSelector(PreferenceGroup group, CheckBoxPreference checkboxPrefernce,
            @Nullable Preference pref1, @Nullable Preference pref2) {
        checkNotNull(group);
        this.mGroup = group;
        this.mCheckbox = checkboxPrefernce;
        this.mPref1 = pref1;
        this.mPref2 = pref2;

        this.lastUpdatedState = checkboxPrefernce.isChecked();
        if (lastUpdatedState) {
            remove(mPref2);
        } else {
            remove(mPref1);
        }
    }

    public final void update() {
        final boolean newState = mCheckbox.isChecked();
        if (newState == lastUpdatedState) {
            return;
        }
        lastUpdatedState = newState;
        if (lastUpdatedState) {
            add(mPref1);
            remove(mPref2);
        } else {
            remove(mPref1);
            add(mPref2);
        }
    }
    
    private final void add(@Nullable Preference pref) {
       if (pref != null) {
           mGroup.addPreference(pref);
       }
    }
    
    private final void remove(@Nullable Preference pref) {
        if (pref != null) {
            mGroup.removePreference(pref);
        }
     }
    
}

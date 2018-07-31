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

package com.zapta.apps.maniana.menus;

import android.graphics.drawable.Drawable;

import com.zapta.apps.maniana.annotations.MainActivityScope;

/**
 * Immutable representation of an item menu entry.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class ItemMenuEntry {

    /** The icon to display. */
    private final Drawable mIcon;

    /** The short text to display. */
    private final String mLabel;

    /** The action id to return upon menu selection. */
    private final int mActionId;

    public ItemMenuEntry(int actionId, String label, Drawable icon) {
        mActionId = actionId;
        mLabel = label;
        mIcon = icon;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public final Drawable getIcon() {
        return this.mIcon;
    }

    public final int getActionId() {
        return mActionId;
    }
}
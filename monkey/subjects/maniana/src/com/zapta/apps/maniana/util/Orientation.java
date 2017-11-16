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
import android.content.res.Configuration;

/** Represents screen orientations. It is safe to assume that this type is binary. */
@ApplicationScope
public enum Orientation {
    PORTRAIT(true, false),
    LANDSCAPE(false, true);

    public final boolean isPortrait;
    public final boolean isLandscape;

    private Orientation(boolean isPortrait, boolean isLandscape) {
        this.isPortrait = isPortrait;
        this.isLandscape = isLandscape;
    }

    public static final Orientation currentDeviceOrientation(Context context) {
        final int rawOrientation = context.getResources().getConfiguration().orientation;
        // TODO: all non portrait default to landscape. Consider to add UNKNOWN.
        return (rawOrientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT : LANDSCAPE;
    }
}

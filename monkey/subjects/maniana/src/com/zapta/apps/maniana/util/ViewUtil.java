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

import java.lang.reflect.Method;

import android.graphics.Paint;
import android.view.View;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * @author Tal Dayan
 */
@ApplicationScope
public final class ViewUtil {

    private static final boolean PRE_API_11 = android.os.Build.VERSION.SDK_INT < 11;

    // From View.java API 11+.
    private static final int VIEW_LAYER_TYPE_SOFTWARE = 1;

    /** Do not instantiate */
    private ViewUtil() {
    }

    public static void disableHardwareAcceleration(View view) {
        // Hardware acceleration is available from API 11 and up only.
        if (PRE_API_11) {
            return;
        }

        // Since we build with API 8, the only way to access the API11 method is via
        // reflection.
        try {
            final Method method = View.class.getMethod("setLayerType", Integer.TYPE, Paint.class);
            method.invoke(view, VIEW_LAYER_TYPE_SOFTWARE, null);
        } catch (Throwable e) {
            LogUtil.warning("Exception when tried to disable hardware acceleration", e);
        }
    }
}

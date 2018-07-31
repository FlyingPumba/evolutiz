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

import java.util.List;

import com.zapta.apps.maniana.annotations.ApplicationScope;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * @author Tal Dayan
 */
@ApplicationScope
public final class IntentUtil {

    /** Do not instantiate */
    private IntentUtil() {
    }

    public static void dumpIntent(Intent intent, boolean detailed) {
        if (!detailed) {
            LogUtil.debug("%s", intent);  
            return;
        }
        
        LogUtil.debug("*** Intent dump:");
        LogUtil.debug("*   intent: %s", intent);
        LogUtil.debug("*   action: %s", intent.getAction());
        LogUtil.debug("*   data string: %s", intent.getDataString());
        LogUtil.debug("*   data uri: %s", intent.getData());
        LogUtil.debug("*   type: %s", intent.getType());

        if (intent.getData() != null) {
            LogUtil.debug("*   uri path: %s", intent.getData().getPath());
            LogUtil.debug("*   scheme: %s", intent.getData().getScheme());
            LogUtil.debug("*   uri authority: %s", intent.getData());
        }
    }
    
    /** Test if an intent has a default receiver. */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }
}

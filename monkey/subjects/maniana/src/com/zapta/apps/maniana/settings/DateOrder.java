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

import android.content.Context;
import android.text.format.DateFormat;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Represents possible date orders.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum DateOrder {
    MD,
    DM;

    public final boolean monthBeforeDay() {
        return this == MD;
    }

    public static DateOrder localDateOrder(Context context) {
        final String key = String.valueOf(DateFormat.getDateFormatOrder(context)).toLowerCase();

        final int monthIndex = key.indexOf('m');
        final int dayIndex = key.indexOf('d');

        if (monthIndex >= 0 && dayIndex >= 0) {
            return (monthIndex < dayIndex) ? MD : DM;
        }

        // Unknown, use default
        LogUtil.warning("Unknown date order: [%s]", key);
        return MD;
    }
}

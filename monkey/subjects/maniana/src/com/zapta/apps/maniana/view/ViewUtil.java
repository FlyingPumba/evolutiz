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

package com.zapta.apps.maniana.view;

import com.zapta.apps.maniana.annotations.ActivityScope;

import android.view.MotionEvent;

/**
 * General view related utility function.
 * 
 * @author Tal Dayan
 */
@ActivityScope
public class ViewUtil {

    /** Return a developer friendly action name. Not user visible. */
    public static String actionDebugName(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "DOWN";
            case MotionEvent.ACTION_CANCEL:
                return "CANCEL";
            case MotionEvent.ACTION_UP:
                return "UP";
            case MotionEvent.ACTION_MOVE:
                return "MOVE";
            default:
                return String.format("UNKNOWN(%d)", action);
        }
    }

//    public static void setTextViewTopBottomPaddingFraction(TextView tv, float lineHeightFraction) {
//        if (lineHeightFraction == 0) {
//            return;
//        }
//        final int topBottomPaddingPixles = (int) (tv.getTextSize() * lineHeightFraction);
//        tv.setPadding(tv.getPaddingLeft(), tv.getPaddingTop() + topBottomPaddingPixles,
//                tv.getPaddingRight(), tv.getPaddingBottom() + topBottomPaddingPixles);
//    }
}

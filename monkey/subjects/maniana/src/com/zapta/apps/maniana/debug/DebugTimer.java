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

package com.zapta.apps.maniana.debug;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Debug utility to measure operation timing.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public final class DebugTimer {

    private final long mStartTimeMillis;

    private long mLastStartTimeMillies;

    public DebugTimer() {
        mStartTimeMillis = System.currentTimeMillis();
        mLastStartTimeMillies = mStartTimeMillis;
    }

    public void report(String message) {
        final long timeNow = System.currentTimeMillis();
        LogUtil.info("[%d, %d] - %s", timeNow - mStartTimeMillis, timeNow - mLastStartTimeMillies,
                message);
        mLastStartTimeMillies = timeNow;
    }
}

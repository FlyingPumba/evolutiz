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
package com.zapta.apps.maniana.model;

import android.text.format.Time;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.settings.LockExpirationPeriod;
import com.zapta.apps.maniana.util.DateUtil;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Model related utilities
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class ModelUtil {

    /**
     * Determine a task push scope upon app resume.
     * 
     * @param lastPushTimestamp model timestamp of last push.
     * @param someTimeToday a Time with today's date (time of day ignored)
     * @param lockExpirationPeriod user preference for lock expiration period
     * 
     * @return The scope of the task push to do, if at all.
     */
    public static PushScope computePushScope(String lastPushTimestamp, Time someTimeToday,
            LockExpirationPeriod lockExpirationPeriod) {
        // Convert timestamp to time
        final Time lastPushTime = new Time();
        final boolean parsedOk = DateUtil.setFromString(lastPushTime, lastPushTimestamp);

        // If timestamp not parsed ok assume only a day change with no lock expiration.
        if (!parsedOk) {
            LogUtil.warning("Could not parse model timestamp: %s", lastPushTimestamp);
            return PushScope.UNLOCKED_ONLY;
        }

        // Handle the case of same day.
        if (DateUtil.isSameDate(someTimeToday, lastPushTime)) {
            return PushScope.NONE;
        }

        final boolean locksExpire;
        switch (lockExpirationPeriod) {
            case WEEKLY:
                locksExpire = !DateUtil.isSameWeek(someTimeToday, lastPushTime);
                break;
            case MONTHLY:
                locksExpire = !DateUtil.isSameMonth(someTimeToday, lastPushTime);
                break;
            default:
                locksExpire = false;
                break;
        }

        return locksExpire ? PushScope.ALL : PushScope.UNLOCKED_ONLY;
    }
}

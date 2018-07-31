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

package com.zapta.apps.maniana.services;

import android.text.format.Time;

import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.model.ModelUtil;
import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.settings.DateOrder;
import com.zapta.apps.maniana.settings.LockExpirationPeriod;
import com.zapta.apps.maniana.util.DateUtil;

/**
 * Tracks the current date. Provide current date information and detection of date changes for
 * pushing Tomorow items to Today page.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class DateTracker {

    private final DateOrder dateOrder;

    /** Caching of current user visible day of week. E.g. "Sunday" */
    private String mUserDayOfWeekString;

    /** Caching of current user visible current month/day date */
    private String mUserMonthDayString;

    /** Caching of the last updated date. */
    private Time mCachedDate = new Time();

    /** Temp variable. Used to avoid object instantiation. */
    private Time mTempTime = new Time();

    /** Caching of date stamp of mCachedDate. Not user visible. Persisted. */
    private String mCachedDateString;

    public DateTracker(DateOrder dateOrder) {
        this.dateOrder = dateOrder;
        updateDate();
    }

    /** Read today's date and cache values. */
    public void updateDate() {
        mTempTime.setToNow();
        if (!DateUtil.isSameDate(mTempTime, mCachedDate)) {
            mCachedDate.set(mTempTime);
            mCachedDateString = DateUtil.dateToString(mCachedDate);
            mUserDayOfWeekString = mCachedDate.format("%A");
            // NOTE: this value is cached for performance. If underlying system date order is changed, 
            // it will be reflected in next app restart.
            mUserMonthDayString = mCachedDate
                    .format(dateOrder.monthBeforeDay() ? "%b %d" : "%d %b");
        }
    }

    /**
     * Return an unspecified time that is guaranteed to be today. Can change. Caller should not
     * change.
     */
    public final Time sometimeToday() {
        return mCachedDate;
    }

    /** Get day of week string. User visible */
    public final String getUserDayOfWeekString() {
        return mUserDayOfWeekString;
    }

    /** Get month/day week. User visible. Format depends on locale. */
    public final String getUserMonthDayString() {
        return mUserMonthDayString;
    }

    /** Get year.month.day datestamp. Non user visible. Persisted with model. */
    public final String getDateStampString() {
        return mCachedDateString;
    }

    public PushScope computePushScope(String lastPushTimestamp,
            LockExpirationPeriod lockExpirationPeriod) {
        return ModelUtil.computePushScope(lastPushTimestamp, mCachedDate, lockExpirationPeriod);
    }
}

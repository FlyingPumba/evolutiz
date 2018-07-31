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

import android.text.format.Time;

/**
 * Provides date related operations.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public final class DateUtil {

    /** Do not instantiate */
    private DateUtil() {
    }

    // Ignoring any time component within the day. */
    public static final boolean isEarilerDate(Time t1, Time t2) {
        if (t1.year != t2.year) {
            return t1.year < t2.year;
        }
        if (t1.month != t2.month) {
            return t1.month < t2.month;
        }
        return t1.monthDay < t2.monthDay;
    }

    // Ignoring any time component within the day. */
    public static final boolean isSameDate(Time t1, Time t2) {
        return (t1.monthDay == t2.monthDay) && (t1.month == t2.month) && (t1.year == t2.year);
    }

    // Ignoring any time component within the day. */
    public static final boolean isSameMonth(Time t1, Time t2) {
        return (t1.month == t2.month) && (t1.year == t2.year);
    }

    // Ignoring any time component within the day. */
    public static final boolean isSameWeek(Time t1, Time t2) {
        // A quick calculation for the most frequent case.
        if (isSameDate(t1, t2)) {
            return true;
        }

        // Sort the two dates. x.date <= y.date
        final Time x;
        final Time y;

        if (t1.before(t2)) {
            x = t1;
            y = t2;
        } else {
            x = t2;
            y = t1;
        }

        // Get the beginning of day y.
        final Time t = new Time();
        t.set(y.monthDay, y.month, y.year);

        y.normalize(false);
        int n = y.weekDay;
        // Substract n days
        while (n > 0) {
            final int daysLeftInMonth = t.monthDay - 1;
            // Go across month/year boundary
            if (daysLeftInMonth == 0) {
                decrementOneDay(t);
                n--;
                continue;
            }

            // Decrement within the month
            final int d = Math.min(n, daysLeftInMonth);
            t.monthDay -= d;
            n -= d;
        }

        return !isEarilerDate(x, t);
    }

    private static final void decrementOneDay(Time t) {
        if (t.monthDay > 1) {
            t.monthDay--;
            return;
        }

        if (t.month > 0) {
            t.month--;

        } else {
            t.year--;
            t.month = 11;
        }

        // Set day to end of month
        t.monthDay = 1; // temporary, have a valid date
        t.monthDay = t.getActualMaximum(Time.MONTH_DAY);
    }

    /** Return true if ok. t is not changed otherwise. */
    public final static boolean setFromString(Time t, String str) {
        int year;
        int month;
        int day;
        try {
            int val = Integer.valueOf(str);
            day = val % 100;
            val /= 100;
            month = val % 100;
            year = val / 100;
            if (year >= 0 && year <= 9999 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                t.set(day, month - 1, year);
                return true;
            }
        } catch (NumberFormatException e) {

        }
        return false;
    }

    public static final String dateToString(Time t) {
        return String.format("%04d%02d%02d", t.year, t.month + 1, t.monthDay);
    }

    /**
     * Return number of whole hoursto the end of the week of a given time.
     * 
     * @param the time (can contains hours, minutes, seconds, millis).
     */
    public static final int hoursToEndOfWeek(Time t) {
        // Normalize to have a valid day of week
        t.normalize(false);
        final int hoursPassedInWeek = (t.weekDay * 24) + t.hour;
        // NOTE: clipping at zero just in case. Should not happen.
        return Math.max(0, (7 * 24) - hoursPassedInWeek);
    }

    /**
     * Return number of whole hours to the end of month of a given time.
     * 
     * @param the time (can contains hours, minutes, seconds, millis).
     */
    public static final int hoursToEndOfMonth(Time t) {
        // [1..31] (actually 28, 29, 30, 31).
        final int hoursInThisMonth = 24 * t.getActualMaximum(Time.MONTH_DAY);
        final int hoursPassedInThisMonth = ((t.monthDay - 1) * 24) + t.hour;
        // NOTE: clipping to zero just in case. Should not happen.
        return Math.max(0, hoursInThisMonth - hoursPassedInThisMonth);
    }
}

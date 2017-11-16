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

package com.zapta.apps.maniana.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Used to simulate delayed Maniana notifications when the devices is in standby mode. For testing
 * only.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class NotificationSimulator extends BroadcastReceiver {

    /** Should match AndroidManifest.xml. */
    private static final String SIMULATE_ACTION = "com.zapta.apps.maniana.notifications.SIMULATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.debug("NotificationSimulator.onRecieve: " + intent);
        NotificationUtil.sendPendingItemsNotification(context, 3, true);
    }

    public static final void scheduleDelayedNotificationSimulation(Context context, long delaySecs) {
        Intent intent = new Intent(SIMULATE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        final long triggerTimeMillis = System.currentTimeMillis() + (delaySecs * 1000);

        final AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
    }
}

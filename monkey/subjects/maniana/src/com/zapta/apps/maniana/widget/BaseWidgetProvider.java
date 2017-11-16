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

package com.zapta.apps.maniana.widget;

import javax.annotation.Nullable;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.main.MyApp;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ModelUtil;
import com.zapta.apps.maniana.model.OrganizePageSummary;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.model.PushScope;
import com.zapta.apps.maniana.notifications.NotificationUtil;
import com.zapta.apps.maniana.persistence.ModelPersistence;
import com.zapta.apps.maniana.persistence.ModelReadingResult;
import com.zapta.apps.maniana.services.MidnightTicker;
import com.zapta.apps.maniana.settings.LockExpirationPeriod;
import com.zapta.apps.maniana.settings.PreferencesReader;

/**
 * Base class widget providers.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public abstract class BaseWidgetProvider extends AppWidgetProvider {

    /** Model is already pushed and sorted according to current settings. */
    public static void updateAllWidgetsFromModel(Context context, @Nullable AppModel model, Time sometimeToday) {
        // For testing only
        // NotificationUtil.sendPendingItemsNotification(context,
        // model.getPagePendingItemCount(PageKind.TODAY));

        IconWidgetProvider.updateAllIconWidgetsFromModel(context, model);
        ListWidgetProvider.updateAllListWidgetsFromModel(context, model, sometimeToday);
    }

    public static void updateAllWidgetsFromContext(Context context, Time timeNow) {
        updateAllWidgetsFromModel(context, loadModelForWidgets(context, timeNow), timeNow);
    }

    /** Load model. Return null if error. The model is pushed and sorted based on current settings */
    @Nullable
    protected static AppModel loadModelForWidgets(Context context, Time timeNow) {
        // Load model
        final AppModel model = new AppModel();
        final ModelReadingResult modelLoadingResult = ModelPersistence
                .readModelFile(context, model);
        if (!modelLoadingResult.outcome.isOk()) {
            return null;
        }

        final MyApp app = (MyApp) context.getApplicationContext();
        final PreferencesReader prefReader = app.preferencesReader();

        final LockExpirationPeriod lockExpirationPeriod = prefReader
                .getLockExpierationPeriodPreference();
        final boolean removeCompletedOnPush = prefReader.getAutoDailyCleanupPreference();
        final boolean includeCompletedItems = prefReader.getWidgetShowCompletedItemsPreference();
        final boolean sortItems = includeCompletedItems ? prefReader.getAutoSortPreference()
                : false;

        final PushScope pushScope = ModelUtil.computePushScope(model.getLastPushDateStamp(),
                timeNow, lockExpirationPeriod);

        if (pushScope.isActive()) {
            final boolean unlockAllLocks = (pushScope == PushScope.ALL);
            model.pushToToday(unlockAllLocks, removeCompletedOnPush);

            // NOTE: if not pushing, model is assumed to already be consistent with the
            // current auto sorting setting.
            if (sortItems) {
                OrganizePageSummary summary = new OrganizePageSummary();
                model.organizePageWithUndo(PageKind.TODAY, false, -1, summary);
                // NOTE: we don't bother to sort Maniana page since it does not affect the widgets
            }

            // We piggy back on the widget update to issue notifications.
            if (prefReader.getDailyNotificationPreference()) {
                final int pendingItemsCount = model.getPagePendingItemCount(PageKind.TODAY);
                if (pendingItemsCount > 0) {
                    NotificationUtil.sendPendingItemsNotification(context, pendingItemsCount,
                            prefReader.getNotificationLedPreference());
                }
            }
        }

        return model;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        MidnightTicker.scheduleMidnightTicker(context);
    }
}

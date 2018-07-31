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
import android.content.SharedPreferences;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * Type safe reading of preferences with proper defaults.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class PreferencesReader implements PreferenceConstants {

    private final Context mContext;
    private final SharedPreferences mSharedPreferences;

    public PreferencesReader(Context context, SharedPreferences sharedPreferences) {
        mContext = context;
        mSharedPreferences = sharedPreferences;
    }

    public final SharedPreferences sharedPreferences() {
        return mSharedPreferences;
    }
    
    public final Context context() {
        return mContext;
    }

    public final boolean getAllowSoundsPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.SOUND_ENABLED.getKey(),
                DEFAULT_ALLOWS_SOUND_EFFECTS);
    }

    public final boolean getShakerEnabledPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.SHAKER_ENABLED.getKey(),
                DEFAULT_SHAKER_ENABLED);
    }

    public final ShakerAction getShakerActionPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.SHAKER_ACTION.getKey(),
                DEFAULT_SHAKER_ACTION.getKey());
        return ShakerAction.fromKey(key, DEFAULT_SHAKER_ACTION);
    }

    public final int getShakerSensitivityPreference() {
        return mSharedPreferences.getInt(PreferenceKind.SHAKER_SENSITIVITY.getKey(),
                DEFAULT_SHAKER_SENSITIVITY);
    }

    public final boolean getPageBackgroundPaperPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.PAGE_BACKGROUND_PAPER.getKey(),
                DEFAULT_PAGE_BACKGROUND_PAPER);
    }

    public final int getPagePaperColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_PAPER_COLOR.getKey(),
                DEFAULT_PAGE_PAPER_COLOR);
    }

    public final int getPageBackgroundSolidColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_BACKGROUND_SOLID_COLOR.getKey(),
                DEFAULT_PAGE_BACKGROUND_SOLID_COLOR);
    }

    public final int getPageItemDividerColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_ITEM_DIVIDER_COLOR.getKey(),
                DEFAULT_PAGE_ITEM_DIVIDER_COLOR);
    }

    public final PageIconSet getPageIconSetPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.PAGE_ICON_SET.getKey(),
                DEFAULT_PAGE_ICON_SET.getKey());
        return PageIconSet.fromKey(key, DEFAULT_PAGE_ICON_SET);
    }

    public final Font getPageTitleFontPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.PAGE_TITLE_FONT.getKey(),
                DEFAULT_PAGE_TITLE_FONT.getKey());
        return Font.fromKey(key, DEFAULT_PAGE_TITLE_FONT);
    }

    public final int getPageTitleFontSizePreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_TITLE_FONT_SIZE.getKey(),
                DEFAULT_PAGE_TITLE_SIZE);
    }

    public final int getPageTitleTodayTextColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_TITLE_TODAY_COLOR.getKey(),
                DEFAULT_PAGE_TITLE_TODAY_COLOR);
    }

    public final int getPageTitleTomorrowTextColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_TITLE_TOMORROW_COLOR.getKey(),
                DEFAULT_PAGE_TITLE_TOMORROW_COLOR);
    }

    public final Font getPageItemFontPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.PAGE_ITEM_FONT.getKey(),
                DEFAULT_PAGE_ITEM_FONT.getKey());
        return Font.fromKey(key, DEFAULT_PAGE_ITEM_FONT);
    }

    public final int getPageFontSizePreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_ITEM_FONT_SIZE.getKey(),
                DEFAULT_PAGE_ITEM_FONT_SIZE);
    }

    public final int getPageItemActiveTextColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_ITEM_ACTIVE_TEXT_COLOR.getKey(),
                DEFAULT_ITEM_TEXT_COLOR);
    }

    public final int getPageItemCompletedTextColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.PAGE_ITEM_COMPLETED_TEXT_COLOR.getKey(),
                DEFAULT_COMPLETED_ITEM_TEXT_COLOR);
    }

    public final boolean getDailyNotificationPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.DAILY_NOTIFICATION.getKey(),
                DEFAULT_DAILY_NOTIFICATION);
    }
    
    public final boolean getNotificationLedPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.NOTIFICATION_LED.getKey(),
                DEFAULT_NOTIFICATION_LED);
    }
    
    public final boolean getCalendarLaunchPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.CALENDAR_LAUNCH.getKey(),
                DEFAULT_CALENDAR_LAUNCH);
    }

    public final LockExpirationPeriod getLockExpierationPeriodPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.LOCK_PERIOD.getKey(),
                DEFAULT_LOCK_PERIOD.getKey());
        return LockExpirationPeriod.fromKey(key, DEFAULT_LOCK_PERIOD);
    }

    public final boolean getWidgetBackgroundPaperPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.WIDGET_BACKGROUND_PAPER.getKey(),
                DEFAULT_WIDGET_BACKGROUND_PAPER);
    }

    public final int getWidgetPaperColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.WIDGET_PAPER_COLOR.getKey(),
                DEFAULT_WIDGET_PAPER_COLOR);
    }

    public final int getWidgetBackgroundColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.WIDGET_BACKGROUND_COLOR.getKey(),
                DEFAULT_WIDGET_BACKGROUND_COLOR);
    }

    public final int getWidgetItemFontSizePreference() {
        return mSharedPreferences.getInt(PreferenceKind.WIDGET_ITEM_FONT_SIZE.getKey(),
                DEFAULT_WIDGET_ITEM_FONT_SIZE);
    }

    public final boolean getWidgetAutoFitPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.WIDGET_AUTO_FIT.getKey(),
                DEFAULT_WIDGET_AUTO_FIT);
    }

    public final Font getWidgetFontPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.WIDGET_ITEM_FONT.getKey(),
                DEFAULT_WIDGET_FONT_TYPE.getKey());
        return Font.fromKey(key, DEFAULT_WIDGET_FONT_TYPE);
    }

    public final int getWidgetTextColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.WIDGET_ITEM_TEXT_COLOR.getKey(),
                DEFAULT_WIDGET_TEXT_COLOR);
    }

    public final int getWidgetCompletedTextColorPreference() {
        return mSharedPreferences.getInt(PreferenceKind.WIDGET_ITEM_COMPLETED_TEXT_COLOR.getKey(),
                DEFAULT_WIDGET_ITEM_COMPLETED_TEXT_COLOR);
    }

    public final boolean getWidgetSingleLinePreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.WIDGET_SINGLE_LINE.getKey(),
                DEFAULT_WIDGET_SINGLE_LINE);
    }

    public final boolean getWidgetShowToolbarPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.WIDGET_SHOW_TOOLBAR.getKey(),
                DEFAULT_WIDGET_SHOW_TOOLBAR);
    }
    
    public final boolean getWidgetShowDatePreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.WIDGET_SHOW_DATE.getKey(),
                DEFAULT_WIDGET_SHOW_DATE);
    }

    public final boolean getWidgetShowCompletedItemsPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.WIDGET_SHOW_COMPLETED_ITEMS.getKey(),
                DEFAULT_WIDGET_SHOW_COMPLETED_ITEMS);
    }

    public final ApplauseLevel getApplauseLevelPreference() {
        final String key = mSharedPreferences.getString(PreferenceKind.APPLAUSE_LEVEL.getKey(),
                DEFAULT_APPPLAUSE_LEVEL.getKey());
        return ApplauseLevel.fromKey(key, DEFAULT_APPPLAUSE_LEVEL);
    }

    public final boolean getVerboseMessagesPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.VERBOSE_MESSAGES.getKey(),
                DEFAULT_VERBOSE_MESSAGES);
    }

    public final boolean getStartupAnimationPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.STARTUP_ANIMATION.getKey(),
                DEFAULT_STARTUP_ANIMATION);
    }

    public final boolean getAutoSortPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.AUTO_SORT.getKey(), DEFAULT_AUTO_SORT);
    }

    public final boolean getAddToTopPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.ADD_TO_TOP.getKey(), DEFAULT_ADD_TO_TOP);
    }
    
    public final ItemColorsSet getItemColorsPreference() {
        final String value = mSharedPreferences.getString(PreferenceKind.ITEM_COLORS.getKey(),
                DEFAULT_ITEM_COLORS);
        return new ItemColorsSet(value);
    }

    public final boolean getAutoDailyCleanupPreference() {
        return mSharedPreferences.getBoolean(PreferenceKind.AUTO_DAILY_CLEANUP.getKey(),
                DEFAULT_AUTO_DAILY_CLEANUP);
    }

}

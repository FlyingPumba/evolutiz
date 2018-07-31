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

import javax.annotation.Nullable;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.EnumUtil;
import com.zapta.apps.maniana.util.EnumUtil.KeyedEnum;

/**
 * Represents the preference items.
 * 
 * DO NOT REUSE THESE OLD KEYS: <code>
 *   prefBackgroundKey
 *   prefBackupEmailKey
 *   prefRestoreKey
 *   prefItemFontKey
 *   prefItemFontSizeKey
 *   prefVoiceRecognitionKey
 *   prefWidgetBackgroundTypeKey
 *   prefWidgetItemFontSizeKey
 * </code>
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum PreferenceKind implements KeyedEnum {
    // Sound
    SOUND_ENABLED("prefAllowSoundKey"),
    APPLAUSE_LEVEL("prefApplauseLevelKey"),

    // Behavior
    STARTUP_ANIMATION("prefStartupAnimationKey"),
    VERBOSE_MESSAGES("prefVerboseMessagesKey"),
    DAILY_NOTIFICATION("prefDailyNotificationKey"),
    NOTIFICATION_LED("prefNotificationLedKey"),
    SHAKER_ENABLED("prefShakerEnableKey"),
    CALENDAR_LAUNCH("prefCalendarLaunchKey"),
    SHAKER_ACTION("prefShakerActionKey"),
    SHAKER_SENSITIVITY("prefShakerForceKey"),
    
    // Tasks
    AUTO_SORT("prefAutoSortKey"),
    ADD_TO_TOP("prefAddToTopKey"),
    ITEM_COLORS("prefItemColorsKey"),
    AUTO_DAILY_CLEANUP("prefAutoDailyCleanupKey"),
    LOCK_PERIOD("prefLockPeriodKey"),

    // Pages
    PAGE_SELECT_THEME("prefPageSelectThemeKey"),
    PAGE_BACKGROUND_PAPER("prefPageBackgroundPaperKey"),
    PAGE_PAPER_COLOR("prefPagePaperColorKey"),
    PAGE_BACKGROUND_SOLID_COLOR("prefPageBackgroundSolidColorKey"),
    PAGE_ICON_SET("prefPageIconSetKey"),
    PAGE_TITLE_FONT("prefPageTitleFontKey"),
    PAGE_TITLE_FONT_SIZE("prefPageTitleFontSizePtKey"),
    PAGE_TITLE_TODAY_COLOR("prefPageTitleTodayColorKey"),
    PAGE_TITLE_TOMORROW_COLOR("prefPageTitleTomorrowColorKey"),
    PAGE_ITEM_FONT("prefItemFontKey"),
    PAGE_ITEM_FONT_SIZE("prefPageItemFontSizePtKey"),
    PAGE_ITEM_ACTIVE_TEXT_COLOR("prefPageTextColorKey"),
    PAGE_ITEM_COMPLETED_TEXT_COLOR("prefPageCompletedTextColorKey"),
    PAGE_ITEM_DIVIDER_COLOR("prefPageItemDividerColorKey"),

    // Widget
    WIDGET_SELECT_THEME("prefWidgetSelectThemeKey"),
    WIDGET_BACKGROUND_PAPER("prefWidgetBackgroundPaperKey"),
    WIDGET_PAPER_COLOR("prefWidgetPaperColorKey"),
    WIDGET_BACKGROUND_COLOR("prefWidgetBackgroundColorKey"),
    WIDGET_ITEM_FONT("prefWidgetItemFontKey"),
    WIDGET_ITEM_TEXT_COLOR("prefWidgetTextColorKey"),
    WIDGET_ITEM_FONT_SIZE("prefWidgetItemFontSizePtKey"),
    WIDGET_AUTO_FIT("prefWidgetAutoFitKey"),
    WIDGET_SHOW_COMPLETED_ITEMS("prefWidgetShowCompletedKey"),
    WIDGET_ITEM_COMPLETED_TEXT_COLOR("prefWidgetCompletedTextColorKey"),
    WIDGET_SHOW_TOOLBAR("prefWidgetShowToolbarKey"), 
    WIDGET_SHOW_DATE("prefWidgetShowDateKey"), 
    WIDGET_SINGLE_LINE("prefWidgetSingleLineKey"),

    // Miscellaneous
    VERSION_INFO("prefVersionInfoKey"),
    SHARE("prefShareKey"),
    FEEDBACK("prefFeedbackKey"),
    RESTORE_DEFAULTS("prefRestoreDefaultsKey"),
    
    // Backup (Experimental)
    BACKUP_HELP("prefBackupHelpKey"),
    BACKUP("prefBackupKey"),
    
    // Debug
    DEBUG_MODE("prefDebugModeKey");

    /** Preference item key. Persisted. Change only if must. Must match preferences XML definitions. */
    private final String mKey;

    private PreferenceKind(String key) {
        this.mKey = key;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    /** Return value with given key, null if not found. */
    @Nullable
    public final static PreferenceKind fromKey(String key) {
        return EnumUtil.fromKey(key, PreferenceKind.values(), null);
    }
}

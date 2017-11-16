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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Provides a layer of caching on for some of PreferencesReader's preferences.
 * 
 * TODO: simplify this class by removing settings that are not used often. These
 * settings will be read directly from the preferences reader.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class PreferencesTracker {

    public static interface PreferenceChangeListener {
        void onPreferenceChange(PreferenceKind preferenceKind);
    }

    private final PreferencesReader mPreferencesReader;

    private final PreferenceChangeListener mOutgoingListener;

    private final OnSharedPreferenceChangeListener mIncomingListener;

    // Sound
    private boolean mCachedAllowSoundsPreference;
    private ApplauseLevel mCachedApplauseLevelPreference;

    // Behavior
    private boolean mCachedStartupAnimationPreference;
    private boolean mCachedVerboseMessagesPreference;
    private boolean mCachedAutoSortPreference;
    private boolean mCachedAddToTopPreference;
    private ItemColorsSet mCachedItemColorsPreference;

    private boolean mCachedShakerEnabledPreference;
    private int mCachedShakerSensitivityPreference;

    // Page
    private boolean mCachedPageBackgroundPaperPreference;
    private int mCachedPagePaperColorPreference;
    private int mCachedPageBackgroundSolidColorPreference;
    private PageIconSet mCachedPageIconSetPreference;
    private Font mCachedPageTitleFontPreference;
    private int mCachedPageTitleFontSizePreference;
    private int mCachedPageTitleTodayColorPreference;
    private int mCachedPageTitleTomorrowColorPreference;
    private Font mCachedPageItemFontPreference;
    private int mCachedPageItemFontSizePreference;
    private int mCachedPageItemActiveTextColorPreference;
    private int mCachedPageItemCompletedTextColorPreference;
    private int mCachedPageItemDividerColorPreference;
    
    @Nullable
    private ItemFontVariation mCachedPageItemFontVariation;

    // This is a hack to keep the listener from being garbage collected per
    // http://tinyurl.com/blkycrk. Should be unregistered explicitly when main activity is
    // destroyed.

    public PreferencesTracker(PreferencesReader preferencesReader,
            PreferenceChangeListener outgoingListener) {
        mPreferencesReader = preferencesReader;
        mOutgoingListener = outgoingListener;

        // Get initial values
        updateCachedAllowSoundsPreference();
        updateCachedApplauseLevelPreference();
        updateCachedAutoSortPreference();
        updateCachedAddToTopPreference();
        updateCachedItemColorsPreference();
        updateCachedShakerEnabledPreference();
        updateCachedShakerSensitivityPreference();
        updateCachedPageIconSetPreference();
        updateCachedPageItemFontPreference();
        updateCachedPageTitleFontPreference();
        updateCachedPageTitleFontSizePreference();
        updateCachedPageTitleTodayTextColorPreference();
        updateCachedPageTitleTomorrowTextColorPreference();
        updateCachedPageFontSizePreference();
        updateCachedPageItemActiveTextColorPreference();
        updateCachedPageItemCompletedTextColorPreference();
        updateCachedPageBackgroundPaperPreference();
        updateCachedPagePaperColorPreference();
        updateCachedPageBackgroundSolidColorPreference();
        updateCachedPageItemDividerColorPreference();
        updateCachedVerboseMessagesPreference();
        updateCachedStartupAnimationPreference();

        mIncomingListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                onPreferenceChange(key);
            }
        };

        mPreferencesReader.sharedPreferences().registerOnSharedPreferenceChangeListener(
                this.mIncomingListener);
    }

    public final PreferencesReader reader() {
        return mPreferencesReader;
    }

    // --------------- Sound ---------------------

    private final void updateCachedAllowSoundsPreference() {
        mCachedAllowSoundsPreference = mPreferencesReader.getAllowSoundsPreference();
    }

    // TODO: consistent name for this pref
    public final boolean getSoundEnabledPreference() {
        return mCachedAllowSoundsPreference;
    }

    private final void updateCachedApplauseLevelPreference() {
        mCachedApplauseLevelPreference = mPreferencesReader.getApplauseLevelPreference();
    }

    /** Should be ignored if sound is disabled */
    public final ApplauseLevel getApplauseLevelPreference() {
        return mCachedApplauseLevelPreference;
    }

    // --------------- Behavior ---------------------

    private final void updateCachedStartupAnimationPreference() {
        mCachedStartupAnimationPreference = mPreferencesReader.getStartupAnimationPreference();
    }

    public final boolean getStartupAnimationPreference() {
        return mCachedStartupAnimationPreference;
    }

    private final void updateCachedVerboseMessagesPreference() {
        mCachedVerboseMessagesPreference = mPreferencesReader.getVerboseMessagesPreference();
    }

    public final boolean getVerboseMessagesEnabledPreference() {
        return mCachedVerboseMessagesPreference;
    }

    private final void updateCachedAutoSortPreference() {
        mCachedAutoSortPreference = mPreferencesReader.getAutoSortPreference();
    }
    
    public final boolean getAutoSortPreference() {
        return mCachedAutoSortPreference;
    }
    
    private final void updateCachedAddToTopPreference() {
        mCachedAddToTopPreference = mPreferencesReader.getAddToTopPreference();
    }

    public final boolean getAddToTopPreference() {
        return mCachedAddToTopPreference;
    }
    

    private final void updateCachedItemColorsPreference() {
        mCachedItemColorsPreference = mPreferencesReader.getItemColorsPreference();
    }
    
    public final ItemColorsSet getItemColorsPreference() {
        return mCachedItemColorsPreference;
    }

    private final void updateCachedShakerEnabledPreference() {
        mCachedShakerEnabledPreference = mPreferencesReader.getShakerEnabledPreference();
    }

    public final boolean getShakerEnabledPreference() {
        return mCachedShakerEnabledPreference;
    }

    private final void updateCachedShakerSensitivityPreference() {
        mCachedShakerSensitivityPreference = mPreferencesReader.getShakerSensitivityPreference();
    }

    public final int getShakerSensitivityPreference() {
        return mCachedShakerSensitivityPreference;
    }

    // --------------- Pages ------------------------

    private final void updateCachedPageBackgroundPaperPreference() {
        mCachedPageBackgroundPaperPreference = mPreferencesReader
                .getPageBackgroundPaperPreference();
    }

    public final boolean getBackgroundPaperPreference() {
        return mCachedPageBackgroundPaperPreference;
    }

    private final void updateCachedPagePaperColorPreference() {
        mCachedPagePaperColorPreference = mPreferencesReader.getPagePaperColorPreference();
    }

    public final int getPagePaperColorPreference() {
        return mCachedPagePaperColorPreference;
    }

    private final void updateCachedPageBackgroundSolidColorPreference() {
        mCachedPageBackgroundSolidColorPreference = mPreferencesReader
                .getPageBackgroundSolidColorPreference();
    }

    public final int getPageBackgroundSolidColorPreference() {
        return mCachedPageBackgroundSolidColorPreference;
    }

    private final void updateCachedPageIconSetPreference() {
        mCachedPageIconSetPreference = mPreferencesReader.getPageIconSetPreference();
    }

    public final PageIconSet getPageIconSetPreference() {
        return mCachedPageIconSetPreference;
    }

    private final void updateCachedPageTitleFontPreference() {
        mCachedPageTitleFontPreference = mPreferencesReader.getPageTitleFontPreference();
    }

    public final Font getPageTitleFontPreference() {
        return mCachedPageTitleFontPreference;
    }

    private final void updateCachedPageTitleFontSizePreference() {
        mCachedPageTitleFontSizePreference = mPreferencesReader.getPageTitleFontSizePreference();
    }

    public final int getPageTitleFontSizePreference() {
        return mCachedPageTitleFontSizePreference;
    }

    private final void updateCachedPageTitleTodayTextColorPreference() {
        mCachedPageTitleTodayColorPreference = mPreferencesReader
                .getPageTitleTodayTextColorPreference();
    }

    public final int getPageTitleTodayColor() {
        return mCachedPageTitleTodayColorPreference;
    }
    
    private final void updateCachedPageTitleTomorrowTextColorPreference() {
        mCachedPageTitleTomorrowColorPreference = mPreferencesReader
                .getPageTitleTomorrowTextColorPreference();
    }
    
    public final int getPageTitleTomorowColor() {
        return mCachedPageTitleTomorrowColorPreference;
    }

    private final void updateCachedPageItemFontPreference() {
        mCachedPageItemFontPreference = mPreferencesReader.getPageItemFontPreference();
    }
    
    public final Font getItemFontPreference() {
        return mCachedPageItemFontPreference;
    }
    
    private final void updateCachedPageFontSizePreference() {
        mCachedPageItemFontSizePreference = mPreferencesReader.getPageFontSizePreference();
    }
    
    public final int getItemFontSizePreference() {
        return mCachedPageItemFontSizePreference;
    }
    
    private final void updateCachedPageItemActiveTextColorPreference() {
        mCachedPageItemActiveTextColorPreference = mPreferencesReader
                .getPageItemActiveTextColorPreference();
    }
    
    public int getPageItemActiveTextColorPreference() {
        return mCachedPageItemActiveTextColorPreference;
    }
    

    private final void updateCachedPageItemCompletedTextColorPreference() {
        mCachedPageItemCompletedTextColorPreference = mPreferencesReader
                .getPageItemCompletedTextColorPreference();
    }
    
    public int getPageItemCompletedTextColorPreference() {
        return mCachedPageItemCompletedTextColorPreference;
    }
    
    private final void updateCachedPageItemDividerColorPreference() {
        mCachedPageItemDividerColorPreference = mPreferencesReader
                .getPageItemDividerColorPreference();
    }
    
    public final int getPageItemDividerColorPreference() {
        return mCachedPageItemDividerColorPreference;
    }

    /** Get current item font variation */
    public final ItemFontVariation getPageItemFontVariation() {
        if (mCachedPageItemFontVariation == null) {
            mCachedPageItemFontVariation = ItemFontVariation.newFromPagePreferences(
                  mPreferencesReader.context(), this);
        }
        return mCachedPageItemFontVariation;
    }

    /**
     * Handle preferences change.
     * 
     * @param key the preference key string as defined in preferences.xml.
     */
    private final void onPreferenceChange(String key) {
        // Map key to enum value. Do nothing if not found.
        final PreferenceKind id = PreferenceKind.fromKey(key);
        
        if (id == null) {
            LogUtil.error("Unknown setting key: " + key);
            return;
        }

        // TODO: order by definition order of PreferenceKind.
        switch (id) {
            // Sound
            case SOUND_ENABLED:
                updateCachedAllowSoundsPreference();
                break;
            case APPLAUSE_LEVEL:
                updateCachedApplauseLevelPreference();
                break;

            // Behavior
            case AUTO_SORT:
                updateCachedAutoSortPreference();
                break;
            case ADD_TO_TOP:
                updateCachedAddToTopPreference();
                break;
            case ITEM_COLORS:
                updateCachedItemColorsPreference();
                break;
            case AUTO_DAILY_CLEANUP:
            case LOCK_PERIOD:
                // Not cached
                break;
            case VERBOSE_MESSAGES:
                updateCachedVerboseMessagesPreference();
                break;
            case STARTUP_ANIMATION:
                updateCachedStartupAnimationPreference();
                break;
            case DAILY_NOTIFICATION:
            case NOTIFICATION_LED:
                // Do nothing
                break;

            // Shaker
            case SHAKER_ENABLED:
                updateCachedShakerEnabledPreference();
                break;
            case SHAKER_ACTION:
                // Not cached
                break;
            case SHAKER_SENSITIVITY:
                updateCachedShakerSensitivityPreference();
                break;

            // Page
            case PAGE_ICON_SET:
                updateCachedPageIconSetPreference();
                break;
            case PAGE_TITLE_FONT:
                updateCachedPageTitleFontPreference();
                break;
            case PAGE_TITLE_FONT_SIZE:
                updateCachedPageTitleFontSizePreference();
                break;
            case PAGE_TITLE_TODAY_COLOR:
                updateCachedPageTitleTodayTextColorPreference();
                break;
            case PAGE_TITLE_TOMORROW_COLOR:
                updateCachedPageTitleTomorrowTextColorPreference();
                break;
            case PAGE_ITEM_FONT:
                mCachedPageItemFontVariation = null;
                updateCachedPageItemFontPreference();
                break;
            case PAGE_ITEM_FONT_SIZE:
                mCachedPageItemFontVariation = null;
                updateCachedPageFontSizePreference();
                break;
            case PAGE_ITEM_ACTIVE_TEXT_COLOR:
                mCachedPageItemFontVariation = null;
                updateCachedPageItemActiveTextColorPreference();
                break;
            case PAGE_ITEM_COMPLETED_TEXT_COLOR:
                mCachedPageItemFontVariation = null;
                updateCachedPageItemCompletedTextColorPreference();
                break;
            case PAGE_BACKGROUND_PAPER:
                updateCachedPageBackgroundPaperPreference();
                break;
            case PAGE_PAPER_COLOR:
                updateCachedPagePaperColorPreference();
                break;
            case PAGE_BACKGROUND_SOLID_COLOR:
                updateCachedPageBackgroundSolidColorPreference();
                break;
            case PAGE_ITEM_DIVIDER_COLOR:
                updateCachedPageItemDividerColorPreference();
                break;

            // Widget
            case WIDGET_BACKGROUND_PAPER:
            case WIDGET_PAPER_COLOR:
            case WIDGET_BACKGROUND_COLOR:
            case WIDGET_ITEM_FONT:
            case WIDGET_ITEM_TEXT_COLOR:
            case WIDGET_ITEM_FONT_SIZE:
            case WIDGET_AUTO_FIT:
            case WIDGET_SHOW_COMPLETED_ITEMS:
            case WIDGET_ITEM_COMPLETED_TEXT_COLOR:
            case WIDGET_SHOW_TOOLBAR:
            case WIDGET_SHOW_DATE:
            case WIDGET_SINGLE_LINE:
            case DEBUG_MODE:
            case CALENDAR_LAUNCH:
                // These ones are not cached or used here. Just reported to controller to
                // trigger the widget update and backup service.
                break;
            default:
                // Report and ignore this call.
                LogUtil.error("Unknown changed preference key: %s", key);
                return;
        }

        // Inform the controller about the prefernce change. At this point, this object already
        // cached the new values.
        mOutgoingListener.onPreferenceChange(id);
    }

    /** Release resources. This is the last call to this instance. */
    public void release() {
        // Per http://tinyurl.com/blkycrk
        mPreferencesReader.sharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this.mIncomingListener);
    }
}

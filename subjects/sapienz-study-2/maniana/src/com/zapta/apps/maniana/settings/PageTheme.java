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

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * Today/Tomorrow pages predefined themes.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class PageTheme extends Thumbnail {

    public static final PageTheme[] PAGE_THEMES = {
        // Default
        new PageTheme(R.string.page_theme_name_paper, R.drawable.page_theme1_preview,
                PreferenceConstants.DEFAULT_PAGE_BACKGROUND_PAPER,
                PreferenceConstants.DEFAULT_PAGE_PAPER_COLOR,
                PreferenceConstants.DEFAULT_PAGE_BACKGROUND_SOLID_COLOR,
                PreferenceConstants.DEFAULT_PAGE_ICON_SET,
                PreferenceConstants.DEFAULT_PAGE_TITLE_FONT,
                PreferenceConstants.DEFAULT_PAGE_TITLE_SIZE,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TODAY_COLOR,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TOMORROW_COLOR,
                PreferenceConstants.DEFAULT_PAGE_ITEM_FONT,
                PreferenceConstants.DEFAULT_PAGE_ITEM_FONT_SIZE,
                PreferenceConstants.DEFAULT_ITEM_TEXT_COLOR,
                PreferenceConstants.DEFAULT_COMPLETED_ITEM_TEXT_COLOR,
                PreferenceConstants.DEFAULT_PAGE_ITEM_DIVIDER_COLOR),

        new PageTheme(R.string.page_theme_name_yellow, R.drawable.page_theme2_preview, false, 0xffffffff,
                0xfffcfcb8, PageIconSet.MODERN, Font.IMPACT, 22,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TODAY_COLOR,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TOMORROW_COLOR, Font.SAN_SERIF, 16,
                0xff333333, 0xff909090, 0x4def9900),

        new PageTheme(R.string.page_theme_name_dark, R.drawable.page_theme3_preview, false, 0xffffffff, 0xff000000,
                PageIconSet.WHITE, Font.IMPACT, 30, 0xffb7d9ff, 0xffffb0b4, Font.ELEGANT, 20,
                0xffffffff, 0xff7e7e7e, 0x45ffff00),

        new PageTheme(R.string.page_theme_name_notebook, R.drawable.page_theme4_preview, true, 0xfff0fff0, 0xffaaffff,
                PageIconSet.PARTY, Font.SAN_SERIF, 30,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TODAY_COLOR,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TOMORROW_COLOR, Font.CURSIVE, 16, 0xff111111,
                0xff555555, 0x30000000),
    };

    public final boolean backgroundPaper;
    public final int paperColor;
    public final int backgroundSolidColor;
    public final PageIconSet iconSet;
    public final Font titleFont;
    public final int titleFontSize;
    public final int titleTodayTextColor;
    public final int titleTomorrowTextColor;
    public final Font itemFont;
    public final int itemFontSize;
    public final int itemTextColor;
    public final int itemCompletedTextColor;
    public final int itemDividerColor;

    public PageTheme(int nameResourceId, int drawableId, boolean backgroundPaper, int paperColor,
            int backgroundSolidColor, PageIconSet iconSet, Font titleFont, int titleFontSize,
            int titleTodayTextColor, int titleTomorrowTextColor, Font itemFont, int itemFontSize,
            int itemTextColor, int itemCompletedTextColor, int itemDividerColor) {
        super(nameResourceId, drawableId);
        this.backgroundPaper = backgroundPaper;
        this.paperColor = paperColor;
        this.backgroundSolidColor = backgroundSolidColor;
        this.iconSet = iconSet;
        this.titleFont = titleFont;
        this.titleFontSize = titleFontSize;
        this.titleTodayTextColor = titleTodayTextColor;
        this.titleTomorrowTextColor = titleTomorrowTextColor;
        this.itemFont = itemFont;
        this.itemFontSize = itemFontSize;
        this.itemTextColor = itemTextColor;
        this.itemCompletedTextColor = itemCompletedTextColor;
        this.itemDividerColor = itemDividerColor;
    }
}

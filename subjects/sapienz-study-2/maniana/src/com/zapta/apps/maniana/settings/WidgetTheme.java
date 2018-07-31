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
 * Widget predefined themes.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class WidgetTheme extends Thumbnail {
    // NOTE: preview images capture parameters:
    // * Taken on CM7 Nexus S 
    // * 4x3 widget 
    // * res/values/dimen changed temporarily:
    //   list_widget_width_4x_portrait = 370px
    //   list_widget_height_x3_portrait = 250px
    // * Solid #c7d4ff wallpapper 
    // * Task list set to items shown
    // * captured with DDMS
    // * cropped with GIMP to 360x250.
    public static final WidgetTheme[] WIDGET_THEMES = {
        // Default theme
        new WidgetTheme(R.string.widget_theme_name_paper, R.drawable.widget_theme1_preview,
                PreferenceConstants.DEFAULT_WIDGET_BACKGROUND_PAPER,
                PreferenceConstants.DEFAULT_WIDGET_PAPER_COLOR,
                PreferenceConstants.DEFAULT_WIDGET_BACKGROUND_COLOR,
                PreferenceConstants.DEFAULT_WIDGET_FONT_TYPE,
                PreferenceConstants.DEFAULT_WIDGET_ITEM_FONT_SIZE,
                PreferenceConstants.DEFAULT_WIDGET_TEXT_COLOR,
                PreferenceConstants.DEFAULT_COMPLETED_ITEM_TEXT_COLOR,
                PreferenceConstants.DEFAULT_WIDGET_SHOW_TOOLBAR),
        new WidgetTheme(R.string.widget_theme_name_semi_transparanet, R.drawable.widget_theme2_preview, false, 0xffffffff,
                0x44000000, Font.SAN_SERIF, 14, 0xffffff00, 0xffabffa2, true),
        new WidgetTheme(R.string.widget_theme_name_large_text, R.drawable.widget_theme3_preview, false, 0xffffffff,
                0xff000000, Font.SAN_SERIF, 18, 0xff00ff00, 0xffaaaaaa, true),
        new WidgetTheme(R.string.widget_theme_name_pink, R.drawable.widget_theme4_preview, true, 0xffffccff,
                0xffff88ff, Font.CURSIVE, 18, 0xff404040, 0xff668866, true),
        new WidgetTheme(R.string.widget_theme_name_small_text, R.drawable.widget_theme5_preview, false, 0xffffffff,
                0x600000ff, Font.SAN_SERIF, 12, 0xffffff00, 0xffabffa2, false)
    };

    public final boolean backgroundPaper;
    public final int paperColor;
    public final int backgroundColor;
    public final Font font;
    public final int fontSize;
    public final int textColor;
    public final int completedTextColor;
    public final boolean showToolbar;

    public WidgetTheme(int nameResourceId, int drawableId, boolean backgroundPaper, int paperColor,
            int backgroundColor, Font font, int fontSize, int textColor, int completedTextColor,
            boolean showToolbar) {
        super(nameResourceId, drawableId);
        this.backgroundPaper = backgroundPaper;
        this.paperColor = paperColor;
        this.backgroundColor = backgroundColor;
        this.font = font;
        this.fontSize = fontSize;
        this.textColor = textColor;
        this.completedTextColor = completedTextColor;
        this.showToolbar = showToolbar;
    }
}

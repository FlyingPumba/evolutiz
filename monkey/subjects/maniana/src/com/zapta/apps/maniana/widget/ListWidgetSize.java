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

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.settings.DateOrder;

/**
 * Descriptor of supported list widget sizes.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class ListWidgetSize {

    public static final int MAX_TITLE_TEXT_SIZE_SP = 11;

    public static enum WidgetDateFormat {
        // NOTE: no ',' to save space.
        SHORT("%a %b %d", "%a %d %b"),
        MEDIUM("%A, %b %d", "%A, %d %b");

        private final String monthBeforeDayFormat;
        private final String dayBeforeMonthFormat;

        private WidgetDateFormat(String monthBeforeDayFormat, String dayBeforeMonthFormat) {
            this.monthBeforeDayFormat = monthBeforeDayFormat;
            this.dayBeforeMonthFormat = dayBeforeMonthFormat;
        }

        public final String formatString(DateOrder dateOrder) {
            return dateOrder.monthBeforeDay() ? monthBeforeDayFormat : dayBeforeMonthFormat;
        }
    }

    static class OrientationInfo {
        public final int widthDipResourceId;
        public final int heightDipResourceId;
        public final int imageViewId;
        public final String imageFileName;
        public final WidgetDateFormat dateFormat;
        public final int maxTitleTextSizeSp;

        private OrientationInfo(int widthDipResourceId, int heightDipResourceId, int imageViewId,
                String imageFileName, WidgetDateFormat dateFormat, int maxTitleTextSizeSp) {
            this.widthDipResourceId = widthDipResourceId;
            this.heightDipResourceId = heightDipResourceId;
            this.imageViewId = imageViewId;
            this.imageFileName = imageFileName;
            this.dateFormat = dateFormat;
            this.maxTitleTextSizeSp = maxTitleTextSizeSp;
        }
    }

    /** Portrait widget width dimensions for [1..4] cells */
    private static final int PORTRAIT_WIDTHS[] = new int[] {
        R.dimen.list_widget_width_1x_portrait,
        R.dimen.list_widget_width_2x_portrait,
        R.dimen.list_widget_width_3x_portrait,
        R.dimen.list_widget_width_4x_portrait
    };

    /** Portrait widget height dimensions for [1..4] cells */
    private static final int PORTRAIT_HEIGHTS[] = new int[] {
        R.dimen.list_widget_height_x1_portrait,
        R.dimen.list_widget_height_x2_portrait,
        R.dimen.list_widget_height_x3_portrait,
        R.dimen.list_widget_height_x4_portrait
    };

    /** Landscape widget width dimensions for [1..4] cells */
    private static final int LANDSCAPE_WIDTHS[] = new int[] {
        R.dimen.list_widget_width_1x_landscape,
        R.dimen.list_widget_width_2x_landscape,
        R.dimen.list_widget_width_3x_landscape,
        R.dimen.list_widget_width_4x_landscape
    };

    /** Landscape widget height dimensions for [1..4] cells */
    private static final int LANDSCAPE_HEIGHTS[] = new int[] {
        R.dimen.list_widget_height_x1_landscape,
        R.dimen.list_widget_height_x2_landscape,
        R.dimen.list_widget_height_x3_landscape,
        R.dimen.list_widget_height_x4_landscape
    };

    /** Max portrait title text size for widths [1..4] cells. */
    private static final int MAX_TITLE_TEXT_SIZES_SP[] = new int[] {
        10,
        12,
        16,
        18
    };

    /** Date format for widths [1..4] cells. */
    private static final WidgetDateFormat PORTRAIT_DATE_FORMAT[] = new WidgetDateFormat[] {
        WidgetDateFormat.SHORT,
        WidgetDateFormat.SHORT,
        WidgetDateFormat.MEDIUM,
        WidgetDateFormat.MEDIUM, // was LONG
    };

    private static final WidgetDateFormat LANDSCAPE_DATE_FORMAT[] = new WidgetDateFormat[] {
        WidgetDateFormat.SHORT,
        WidgetDateFormat.SHORT,
        WidgetDateFormat.MEDIUM,
        WidgetDateFormat.MEDIUM, // was LONG
    };

    static final ListWidgetSize LIST_WIDGET_SIZE1 = new ListWidgetSize(ListWidgetProvider1.class,
            4, 1, R.id.widget_list_bitmap_4x1_portrait, R.id.widget_list_bitmap_4x1_landscape);

    static final ListWidgetSize LIST_WIDGET_SIZE2 = new ListWidgetSize(ListWidgetProvider2.class,
            4, 2, R.id.widget_list_bitmap_4x2_portrait, R.id.widget_list_bitmap_4x2_landscape);

    static final ListWidgetSize LIST_WIDGET_SIZE3 = new ListWidgetSize(ListWidgetProvider3.class,
            4, 3, R.id.widget_list_bitmap_4x3_portrait, R.id.widget_list_bitmap_4x3_landscape);

    static final ListWidgetSize LIST_WIDGET_SIZE4 = new ListWidgetSize(ListWidgetProvider4.class,
            2, 2, R.id.widget_list_bitmap_2x2_portrait, R.id.widget_list_bitmap_2x2_landscape);

    static final ListWidgetSize LIST_WIDGET_SIZE5 = new ListWidgetSize(ListWidgetProvider5.class,
            4, 4, R.id.widget_list_bitmap_4x4_portrait, R.id.widget_list_bitmap_4x4_landscape);

    static final ListWidgetSize LIST_WIDGET_SIZE6 = new ListWidgetSize(ListWidgetProvider6.class,
            3, 3, R.id.widget_list_bitmap_3x3_portrait, R.id.widget_list_bitmap_3x3_landscape);

    /** List of all list widget sizes. */
    static final ListWidgetSize[] LIST_WIDGET_SIZES = new ListWidgetSize[] {
        LIST_WIDGET_SIZE1,
        LIST_WIDGET_SIZE2,
        LIST_WIDGET_SIZE3,
        LIST_WIDGET_SIZE4,
        LIST_WIDGET_SIZE5,
        LIST_WIDGET_SIZE6
    };

    /** The actual concrete provider class for this widget size. */
    final Class<? extends ListWidgetProvider> widgetProviderClass;

    /** Widget width in home launcher cells. */
    final int widthCells;

    /** Widget height in home launcher cells. */
    final int heightCells;

    /** Size info for portrait mode */
    final OrientationInfo portraitInfo;

    /** Size info for landscape mode. */
    final OrientationInfo landscapeInfo;

    private ListWidgetSize(Class<? extends ListWidgetProvider> widgetProviderClass, int widthCells,
            int heightCells, int portraitImageViewId, int landscapeImageViewId) {
        this.widgetProviderClass = widgetProviderClass;
        this.widthCells = widthCells;
        this.heightCells = heightCells;

        final int maxTitleTextSizeSp = MAX_TITLE_TEXT_SIZES_SP[widthCells - 1];

        this.portraitInfo = new OrientationInfo(PORTRAIT_WIDTHS[widthCells - 1],
                PORTRAIT_HEIGHTS[heightCells - 1], portraitImageViewId, String.format(
                        "list_widget_image_%dx%d_portrait.png", widthCells, heightCells),
                PORTRAIT_DATE_FORMAT[widthCells - 1], maxTitleTextSizeSp);

        this.landscapeInfo = new OrientationInfo(LANDSCAPE_WIDTHS[widthCells - 1],
                LANDSCAPE_HEIGHTS[heightCells - 1], landscapeImageViewId, String.format(
                        "list_widget_image_%dx%d_landscape.png", widthCells, heightCells),
                LANDSCAPE_DATE_FORMAT[widthCells - 1], maxTitleTextSizeSp);
    }
}

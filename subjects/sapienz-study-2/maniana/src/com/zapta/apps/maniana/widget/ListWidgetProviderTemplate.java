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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.SystemClock;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.services.MainActivityServices;
import com.zapta.apps.maniana.settings.DateOrder;
import com.zapta.apps.maniana.settings.ItemFontVariation;
import com.zapta.apps.maniana.util.BitmapUtil;
import com.zapta.apps.maniana.util.DisplayUtil;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.Orientation;
import com.zapta.apps.maniana.util.TextUtil;
import com.zapta.apps.maniana.view.ExtendedTextView;
import com.zapta.apps.maniana.widget.ListWidgetSize.OrientationInfo;

/**
 * 
 * List widget template to be drawn as a bitmap. It does not include the paper background which is
 * added later at the remoteviews level.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class ListWidgetProviderTemplate {

    /** Will scale item text size down to this size in SP units. */
    private static final int MIN_NORMALIZED_TEXT_SIZE = 10;

    @Nullable
    private final AppModel mModel;
    private final Time mSometimeToday;
    private final Context mContext;
    private final DateOrder mDateOrder;
    private final float mDensity;
    private final LinearLayout mTopView;
    private final View mBackgroundColorView;
    private final View mToolbarView;
    private final TextView mToolbarTitleTextView;
    private final LinearLayout mItemListView;
    private final FrameLayout mListTopSpaceView;
    private final List<TextView> mItemTextViews;
    private final LayoutInflater mLayoutInflater;

    // Preferences
    private final ItemFontVariation mFontVariationPreference;
    private final boolean mAutoFitPreference;
    private final boolean mPaperPreference;
    private final int mBackgroundColorPreference;
    private final boolean mToolbarEanbledPreference;
    private final boolean mToolbarShowDatePreference;
    private final boolean mIncludeCompletedItemsPreference;
    private final boolean mSingleLinePreference;

    public ListWidgetProviderTemplate(Context context, @Nullable AppModel model,
            Time sometimeToday, boolean paperPreference, int backgroundColorPreference,
            boolean toolbarEanbledPreference, boolean toolbarShowDatePreference,
            boolean includeCompletedItemsPreference, boolean singleLinePreference,
            ItemFontVariation fontVariationPreference, boolean autoFitPreference) {
        mContext = context;
        mDateOrder = DateOrder.localDateOrder(context);
        mDensity = DisplayUtil.getDensity(context);
        mModel = model;
        mSometimeToday = sometimeToday;
        mPaperPreference = paperPreference;
        mBackgroundColorPreference = backgroundColorPreference;
        mToolbarEanbledPreference = toolbarEanbledPreference;
        mToolbarShowDatePreference = toolbarShowDatePreference;
        mIncludeCompletedItemsPreference = includeCompletedItemsPreference;
        mSingleLinePreference = singleLinePreference;
        mFontVariationPreference = fontVariationPreference;
        mAutoFitPreference = autoFitPreference;

        // TODO: need this only is using auto shrink
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = (LinearLayout) mLayoutInflater.inflate(R.layout.widget_list_template_layout,
                null);
        mBackgroundColorView = mTopView.findViewById(R.id.widget_list_background_color);
        mToolbarView = mTopView.findViewById(R.id.widget_list_template_toolbar);
        mToolbarTitleTextView = (TextView) mToolbarView
                .findViewById(R.id.widget_list_template_toolbar_title);
        mListTopSpaceView = (FrameLayout) mTopView
                .findViewById(R.id.widget_list_template_list_top_space);
        mItemListView = (LinearLayout) mTopView.findViewById(R.id.widget_list_template_item_list);

        mItemTextViews = new ArrayList<TextView>();

        // Set template background. This can be the background solid color or the paper color.
        mBackgroundColorView.setBackgroundColor(mBackgroundColorPreference);

        // Set template view item list
        populateTemplateItemList();
    }

    /** Set the image of a single orientation. */
    public final Uri renderOrientation(ListWidgetSize listWidgetSize, Orientation orientation,
            int widgetWidthPixels, int widgetHeightPixels, @Nullable PaperBackground paperBackground) {

        final OrientationInfo orientationInfo = orientation.isPortrait ? listWidgetSize.portraitInfo
                : listWidgetSize.landscapeInfo;

        // Does not set title size. This is done later.
        // TODO: refactor out to a method
        final String titleText = mToolbarEanbledPreference ? (mToolbarShowDatePreference ? (mSometimeToday
                .format(orientationInfo.dateFormat.formatString(mDateOrder))) : mContext
                .getString(R.string.page_title_Today)).toUpperCase()
                : null;
        setToolbar(titleText);

        final int shadowRightPixels = mPaperPreference ? paperBackground
                .shadowRightPixels(widgetWidthPixels) : 0;

        final int shadowBottomPixels = mPaperPreference ? paperBackground
                .shadowBottomPixels(widgetHeightPixels) : 0;

        // Set padding to match the drop shadow portion of paper background, if used.
        mTopView.setPadding(0, 0, shadowRightPixels, shadowBottomPixels);

        resizeToFit(widgetWidthPixels, widgetHeightPixels, shadowBottomPixels, orientationInfo);

        // NTOE: ARGB_4444 results in a smaller file than ARGB_8888 (e.g. 50K vs 150k)
        // but does not look as good.
        final Bitmap bitmap1 = Bitmap.createBitmap(widgetWidthPixels, widgetHeightPixels,
                Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap1);
        mTopView.draw(canvas);

        final int ROUND_CORNER_RADIUS_DIPS = 4;

        // NOTE: rounding the bitmap here when paper background is selected will do nothing
        // since the paper background is added later via the remote views.
        final Bitmap bitmap2;
        if (mPaperPreference) {
            bitmap2 = bitmap1;
        } else {
            bitmap2 = BitmapUtil.roundCornersRGB888(bitmap1, (int) (ROUND_CORNER_RADIUS_DIPS
                    * mDensity + 0.5f));
        }

        // NOTE: RemoteViews class has an issue with transferring large bitmaps. As a workaround, we
        // transfer the bitmap using a file URI. We could transfer small widgets directly
        // as bitmap but use file based transfer for all sizes for the sake of simplicity.
        // For more information on this issue see http://tinyurl.com/75jh2yf

        final String fileName = orientationInfo.imageFileName;

        // We make the file world readable so the home launcher can pull it via the file URI.
        // TODO: if there are security concerns about having this file readable, append to it
        // a long random suffix and cleanup the old ones.
        FileUtil.writeBitmapToPngFile(mContext, bitmap2, fileName, true);

        final Uri fileUri = Uri.fromFile(new File(mContext.getFilesDir(), fileName));

        return fileUri;
    }

    // Resize the template in preparation for rendering.
    private final void resizeToFit(int widgetWidthPixels, int widgetHeightPixels,
            int bottomPadding, OrientationInfo orientationInfo) {
        // DebugTimer timer = new DebugTimer();

        final int minItemTextSize = mAutoFitPreference ? MIN_NORMALIZED_TEXT_SIZE
                : mFontVariationPreference.getTextSize();

        setSingleLine(mSingleLinePreference);
        boolean fit = autoResizeText(widgetWidthPixels, widgetHeightPixels, minItemTextSize,
                mFontVariationPreference.getTextSize(), orientationInfo.maxTitleTextSizeSp);

        if (fit || !mAutoFitPreference) {
            // timer.report("Fit done");
            return;
        }

        if (!mSingleLinePreference) {
            setSingleLine(true);
            fit = autoResizeText(widgetWidthPixels, widgetHeightPixels, minItemTextSize,
                    mFontVariationPreference.getTextSize(), orientationInfo.maxTitleTextSizeSp);
        }

        // timer.report("Fit done");
    }

    /**
     * Try to to scale down the text size from given max to min until it fit. Returns true if fit.
     * 
     * TODO: consider to use binary search over text size range.
     */
    private final boolean autoResizeText(int widgetWidthPixels, int widgetHeightPixels,
            int minItemTextSize, int maxItemTextSize, int maxTitleTextSize) {

        // Try min, size, return if no fit.
        if (!resizeText(widgetWidthPixels, widgetHeightPixels, minItemTextSize, maxTitleTextSize)) {
            return false;
        }

        // Try the max size. Return if fits.
        if (resizeText(widgetWidthPixels, widgetHeightPixels, maxItemTextSize, maxTitleTextSize)) {
            return true;
        }

        // Use binary search. high >= current >= min
        float highSize = maxItemTextSize;
        float lowSize = minItemTextSize;
        float currentSize = minItemTextSize;

        for (;;) {
            // Termination condition
            if ((highSize - lowSize) < 0.5f) {
                // The size we want to have upon return is lowSize. If this is not
                // the current size than do one more resizing. We use a safe flaot
                // comparison.
                if (Math.abs(lowSize - currentSize) > 0.1f) {
                    resizeText(widgetWidthPixels, widgetHeightPixels, lowSize, maxTitleTextSize);
                }
                return true;
            }

            // Test mid size
            currentSize = (lowSize + highSize) / 2;
            final boolean currentSizeFits = resizeText(widgetWidthPixels, widgetHeightPixels,
                    currentSize, maxTitleTextSize);
            if (currentSizeFits) {
                lowSize = currentSize;
            } else {
                highSize = currentSize;
            }
        }
    }

    /**
     * Resize template. Returns true if fit. Upon return, template view is ready to be rendered onto
     * a canvas.
     */
    private final boolean resizeText(int widgetWidthPixels, int widgetHeightPixels,
            float itemTextSizeSp, float maxTitleTextSize) {
        if (mToolbarEanbledPreference) {
            final float proposedTitleTextSizeSp = itemTextSizeSp * 0.8f;
            final float titleTextSizeSp = Math.max(ListWidgetSize.MAX_TITLE_TEXT_SIZE_SP,
                    Math.min(proposedTitleTextSizeSp, maxTitleTextSize));
            mToolbarTitleTextView.setTextSize(titleTextSizeSp);
        }

        for (TextView itemTextView : mItemTextViews) {
            itemTextView.setTextSize(itemTextSizeSp);
        }

        // Set the space above first item. It looks better this way. The space is proportional
        // to text size and screen density.
        {
            final float K = 0.45f;
            final int topSpacePixels = (int) (itemTextSizeSp * mDensity * K + 0.5f);
            mListTopSpaceView.getLayoutParams().height = topSpacePixels;
        }

        mTopView.measure(MeasureSpec.makeMeasureSpec(widgetWidthPixels, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(widgetHeightPixels, MeasureSpec.EXACTLY));
        // TODO: subtract '1' from ends?
        mTopView.layout(0, 0, widgetWidthPixels, widgetHeightPixels);

        // We use margin height proportional to the text size. This way it is intuitive
        // to the user that this is the last line and there are no more lines beyond the
        // wieget bottom.
        final int minMarginPixels = (int) (itemTextSizeSp * mDensity);
        return mItemListView.getBottom() < (mBackgroundColorView.getHeight() - minMarginPixels);
    }

    private final void setSingleLine(boolean singleLine) {
        for (TextView itemTextView : mItemTextViews) {
            // NOTE: TextView has a bug that does not allows more than two lines when using
            // ellipsize. Otherwise we would give the user more choices about the max number of
            // lines. More details here: http://code.google.com/p/android/issues/detail?id=2254
            if (singleLine) {
                itemTextView.setSingleLine(true);
                itemTextView.setMaxLines(1);
            } else {
                itemTextView.setSingleLine(false);
                // NOTE: on ICS (API 14) the text view behaves differently and does not limit the
                // lines to two when ellipsize. For consistency, we limit it explicitly to two
                // lines.
                itemTextView.setMaxLines(2);
            }
        }
    }

    /**
     * Populate the given template layout with task data and/or informative messages.
     */
    private final void populateTemplateItemList() {
        // For debugging
        final boolean debugTimestamp = false;
        if (debugTimestamp) {
            final String message = String.format("[%s]", SystemClock.elapsedRealtime() / 1000);
            addTemplateMessageItem(message);
        }

        if (mModel == null) {
            addTemplateMessageItem("(" + mContext.getString(R.string.widget_Maniana_data_not_found)
                    + ")");
            return;
        }

        final List<ItemModelReadOnly> items = WidgetUtil.selectTodaysItems(mModel,
                mIncludeCompletedItemsPreference);

        // If no items, add a message and leave.
        if (items.isEmpty()) {
            final String emptyMessage = "("
                    + mContext
                            .getString(mIncludeCompletedItemsPreference ? R.string.widget_no_tasks
                                    : R.string.widget_no_active_tasks) + ")";
            addTemplateMessageItem(emptyMessage);
            return;
        }

        // Add items.
        for (ItemModelReadOnly item : items) {
            final LinearLayout itemView = (LinearLayout) mLayoutInflater.inflate(
                    R.layout.widget_list_template_item_layout, null);
            final ExtendedTextView extendedTextView = (ExtendedTextView) itemView
                    .findViewById(R.id.widget_item_text_view);
            final View itemColorView = itemView.findViewById(R.id.widget_item_color);

            extendedTextView.setText(item.getText());
            TextUtil.ICS_HACK_TEXT_VIEW(extendedTextView);
            mFontVariationPreference.apply(extendedTextView, item.isCompleted(), true);

            // For debugging. Highlight each
            // {
            // final int bgColor = 0x33000000 | RandomUtil.random.nextInt(0x1000000);
            // extendedTextView.setBackgroundColor(bgColor);
            // }

            // If color is NONE show a gray solid color to help visually
            // grouping item text lines.
            itemColorView.setBackgroundColor(item.getColor().getColor(0xff808080));
            mItemListView.addView(itemView);

            mItemTextViews.add(extendedTextView);
        }
    }

    /**
     * Add an informative message to the item list. These messages are formatted differently than
     * actual tasks.
     */
    private final void addTemplateMessageItem(String message) {

        final LinearLayout itemView = (LinearLayout) mLayoutInflater.inflate(
                R.layout.widget_list_template_item_layout, null);
        final ExtendedTextView extendedTextView = (ExtendedTextView) itemView
                .findViewById(R.id.widget_item_text_view);
        final View colorView = itemView.findViewById(R.id.widget_item_color);

        // TODO: setup message text using widget font size preference?
        extendedTextView.setSingleLine(false);
        extendedTextView.setText(message);
        TextUtil.ICS_HACK_TEXT_VIEW(extendedTextView);
        mFontVariationPreference.apply(extendedTextView, false, true);
        colorView.setVisibility(View.GONE);

        mItemListView.addView(itemView);

        mItemTextViews.add(extendedTextView);
    }

    /**
     * Set the toolbar portion of the template view.
     * 
     * showToolbarBackground and titleSize are ignored if not toolbarEnabled. titleSize.
     */
    private final void setToolbar(String titleText) {
        if (!mToolbarEanbledPreference) {
            mToolbarView.setVisibility(View.GONE);
            return;
        }

        mToolbarView.setVisibility(View.VISIBLE);

        // Show or hide toolbar background.
        if (mPaperPreference) {
            mToolbarView.setBackgroundColor(0x00000000);
        } else {
            mToolbarView.setBackgroundResource(R.drawable.widget_toolbar_background);
        }

        // Show title
        mToolbarTitleTextView.setText(titleText);
        TextUtil.ICS_HACK_TEXT_VIEW(mToolbarTitleTextView);

        // The voice recognition button is shown only if this device supports voice recognition.
        final View templateAddTextByVoiceButton = mToolbarView
                .findViewById(R.id.widget_list_template_toolbar_add_by_voice);
        if (MainActivityServices.isVoiceRecognitionSupported(mContext)) {
            templateAddTextByVoiceButton.setVisibility(View.VISIBLE);
        } else {
            templateAddTextByVoiceButton.setVisibility(View.GONE);
        }
    }
}

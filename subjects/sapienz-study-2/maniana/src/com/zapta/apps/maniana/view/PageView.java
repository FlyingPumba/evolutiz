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

package com.zapta.apps.maniana.view;

import static com.zapta.apps.maniana.util.Assertions.check;
import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import javax.annotation.Nullable;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.menus.ItemMenuEntry;
import com.zapta.apps.maniana.menus.MainMenu;
import com.zapta.apps.maniana.menus.MainMenuEntry;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.services.MainActivityServices;
import com.zapta.apps.maniana.settings.Font;
import com.zapta.apps.maniana.settings.TypefaceSpec;
import com.zapta.apps.maniana.settings.PageIconSet;
import com.zapta.apps.maniana.util.ColorUtil;
import com.zapta.apps.maniana.util.DisplayUtil;
import com.zapta.apps.maniana.util.LanguageUtil;

/**
 * A single page view. Contains title, date (today page only), items, and buttons.
 * 
 * @author Tal Dayan
 */
@SuppressLint("ViewConstructor")
@MainActivityScope
public class PageView extends FrameLayout {

    /** For testing only. */
    // private static final boolean FORCE_OVERFLOW_MENU_ON_ALL_DEVICES = false;

    /**
     * Candidates for day/date color. Selected by distance from background color with a slight
     * preference for the first one.
     */
    private static final int[] DATE_CANDIDATE_COLORS = new int[] {
        0xff222222,
        0xff2222ee,
        0xff22ee22,
        0xff22eeee,
        0xffee2222,
        0xffee22ee,
        0xffeeee22,
        0xffeeeeee
    };

    private static final int[] OVERFLOW_DRAWABLE_CANDIDATE_COLORS = new int[] {
        0xff707070,
        0xff909090
    };

    private static final int[] OVERFLOW_RESOURCES_CANDIDATE_IDS = new int[] {
        R.drawable.main_menu_button_version_1,
        R.drawable.main_menu_button_version_2
    };

    private static final int[] ITEM_HIGHLIGHT_CANDIDATE_COLORS = new int[] {
        0xff0000ff,
        0xffff0000,
        0xff00ff00,
        0xffffff00
    };

    private static final int[] ITEM_HIGHLIGHT_RESOURCES_CANDIDATE_IDS = new int[] {
        R.drawable.item_highlight_blue,
        R.drawable.item_highlight_red,
        R.drawable.item_highlight_green,
        R.drawable.item_highlight_yellow
    };

    private final MainActivityState mMainActivityState;
    private final PageKind mPageKind;
    private final float mDensity;

    /** The sub view with the item list. */
    private final ItemListView mItemListView;

    private final View mPageTitleSection;
    private final View mPageTitleDivider;

    private final ImageButton mMainMenuButtonView;

    private final ImageButton mButtonUndoView;
    private final ImageButton mButtonAddByTextView;
    private final ImageButton mButtonAddByVoiceView;
    private final ImageButton mButtonCleanView;

    private final View mDateTimeSection;
    private final TextView mDayTextView;
    private final TextView mDateTextView;

    private final ExtendedTextView mPageTitleTextView;

    private final View mPaperColorView;

    public PageView(MainActivityState mainActivityState, PageKind pageKind) {
        super(checkNotNull(mainActivityState.context()));
        mMainActivityState = mainActivityState;
        mPageKind = pageKind;
        mDensity = DisplayUtil.getDensity(mainActivityState.context());

        mMainActivityState.services().layoutInflater().inflate(R.layout.page_layout, this);

        mPaperColorView = findViewById(R.id.page_paper_color);

        mPageTitleSection = findViewById(R.id.page_title_section);
        mPageTitleDivider = findViewById(R.id.page_title_divider);

        // mPageTitleSection.setOnClickListener(mCalendarLaunchListener);

        mButtonUndoView = (ImageButton) findViewById(R.id.page_undo_button);
        mButtonAddByTextView = (ImageButton) findViewById(R.id.page_add_by_text_button);

        mButtonAddByVoiceView = (ImageButton) findViewById(R.id.page_add_by_voice_button);

        mButtonCleanView = (ImageButton) findViewById(R.id.page_clean_button);

        mMainMenuButtonView = (ImageButton) findViewById(R.id.page_main_menu_button);

        // NOTE: could also use !ViewConfiguration.get(context).hasPermanentMenuKey();
        // mUsesIcsMenuOverflowButton = FORCE_OVERFLOW_MENU_ON_ALL_DEVICES
        // || (android.os.Build.VERSION.SDK_INT >= 11);

        mDayTextView = (TextView) findViewById(R.id.page_day_text);
        mDateTextView = (TextView) findViewById(R.id.page_date_text);

        mItemListView = (ItemListView) findViewById(R.id.page_item_list);
        mPageTitleTextView = (ExtendedTextView) findViewById(R.id.page_title_text);

        mPageTitleTextView.setText(mPageKind.isToday() ? R.string.page_title_Today
                : R.string.page_title_Maniana);

        // Tomorrow page does not display date
        mDateTimeSection = findViewById(R.id.page_date_time_section);
        if (mPageKind.isToday()) {
            mDateTimeSection.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCalendarLaunchClick();
                }
            });
        } else {
            mDateTimeSection.setVisibility(View.GONE);
        }

        final ItemListViewAdapter adapter = new ItemListViewAdapter(mMainActivityState, mPageKind);
        mItemListView.setApp(mMainActivityState, adapter);

        mMainMenuButtonView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });

        updateUndoButton();
        mButtonUndoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityState.controller().onUndoButton(mPageKind);
            }
        });

        if (MainActivityServices.isVoiceRecognitionSupported(mMainActivityState.context())) {
            mButtonAddByVoiceView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainActivityState.controller().onAddItemByVoiceButton(mPageKind);
                }
            });
        } else {
            mButtonAddByVoiceView.setVisibility(View.GONE);
        }

        mButtonAddByTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityState.controller().onAddItemByTextButton(mPageKind);
            }
        });

        mButtonCleanView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityState.controller().onCleanPageButton(mPageKind, false);
            }
        });

        mButtonCleanView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMainActivityState.controller().onCleanPageButton(mPageKind, true);
                return true;
            }
        });

        // Set initial preferences
        onPageIconSetPreferenceChange();
        onPageBackgroundPreferenceChange();
        onPageTitlePreferenceChange();
        onItemDividerColorPreferenceChange();
    }

    void showMainMenu() {
        final MainMenu mainMenu = new MainMenu(mMainActivityState,
                new MainMenu.OnActionItemOutcomeListener() {
                    @Override
                    public void onOutcome(MainMenu source, MainMenuEntry selectedEntry) {
                        mMainActivityState.controller().onMainMenuSelection(selectedEntry);
                    }
                });
        mainMenu.show(mMainMenuButtonView);
    }

    // TODO: currently the controller calls this even when there is not date change. Filter
    // it somewhere?
    public final void onDateChange() {
        // Only Today page has date.
        check(mPageKind.isToday());
        mDayTextView.setText(mMainActivityState.dateTracker().getUserDayOfWeekString());
        mDateTextView.setText(mMainActivityState.dateTracker().getUserMonthDayString());
    }

    /** Called when the user clicked on a calendar launch area. */
    private final void onCalendarLaunchClick() {
        mMainActivityState.controller().onCalendarLaunchClick();
    }

    public final void onPageItemFontVariationPreferenceChange() {
        mItemListView.onPageItemFontVariationPreferenceChange();
    }

    public final void onPageIconSetPreferenceChange() {
        final PageIconSet iconSet = mMainActivityState.prefTracker().getPageIconSetPreference();
        mButtonUndoView.setImageResource(iconSet.buttonUndoResourceId);
        mButtonAddByTextView.setImageResource(iconSet.buttonAddByTextResourceId);
        mButtonAddByVoiceView.setImageResource(iconSet.buttonAddByVoiceResourceId);
        mButtonCleanView.setImageResource(iconSet.buttonCleanResourceId);

        // Since the icon set affects also the item arrow icons, we need to update
        // the items as well.
        updateAllItemViews();
    }

    /**
     * Update page background to current preferences.
     * 
     * This method is called only when preferences change so we don't bother comparing to current
     * state to avoid unnecessary changes.
     */
    public final void onPageBackgroundPreferenceChange() {
        // Set the background color or image
        final int baseBackgroundColor;
        if (mMainActivityState.prefTracker().getBackgroundPaperPreference()) {
            final int backgroundImageId = (mPageKind.isToday()) ? R.drawable.page_bg_left
                    : R.drawable.page_bg_right;
            setBackgroundResource(backgroundImageId);
            final int paperColor = mMainActivityState.prefTracker().getPagePaperColorPreference();
            mPaperColorView.setBackgroundColor(ColorUtil.mapPaperColorPrefernce(paperColor));
            baseBackgroundColor = paperColor;
        } else {
            final int backgroundColor = mMainActivityState.prefTracker()
                    .getPageBackgroundSolidColorPreference();
            setBackgroundColor(backgroundColor);
            mPaperColorView.setBackgroundColor(0x00000000);
            baseBackgroundColor = backgroundColor;
        }

        // Update date text to have max contrast from the background
        if (mPageKind.isToday()) {
            final int dayAndDateColor = ColorUtil.selectFurthestColor(baseBackgroundColor,
                    DATE_CANDIDATE_COLORS, 0.05f);
            mDayTextView.setTextColor(dayAndDateColor);
            mDateTextView.setTextColor(dayAndDateColor);
        }

        // Update ICS menu overflow icon to have max contrast from the background
        // if (mUsesIcsMenuOverflowButton)
        {
            final int colorIndex = ColorUtil.selectFurthestColorIndex(baseBackgroundColor,
                    OVERFLOW_DRAWABLE_CANDIDATE_COLORS, 0.05f);
            final int resourceId = OVERFLOW_RESOURCES_CANDIDATE_IDS[colorIndex];
            mMainMenuButtonView.setImageResource(resourceId);
        }

        // Update item list view highlight drawable to max contrast from page background
        {
            final int colorIndex = ColorUtil.selectFurthestColorIndex(baseBackgroundColor,
                    ITEM_HIGHLIGHT_CANDIDATE_COLORS, 0.05f);
            final int resourceId = ITEM_HIGHLIGHT_RESOURCES_CANDIDATE_IDS[colorIndex];
            mItemListView.setItemHighlightDrawableResourceId(resourceId);
        }
    }

    public final void onPageTitlePreferenceChange() {
        mPageTitleTextView.setTextColor(mPageKind.isToday() ? mMainActivityState.prefTracker()
                .getPageTitleTodayColor() : mMainActivityState.prefTracker()
                .getPageTitleTomorowColor());
        final Font titleFont = mMainActivityState.prefTracker().getPageTitleFontPreference();
        final TypefaceSpec titleFontSpec = titleFont.getTypefaceSpec(mMainActivityState.context());
        // French 'Today' title does not fit on Galaxy Nexus with default title font and size (it's
        // a long word) so we scale it down in both pages.
        final float frenchFactor = LanguageUtil.currentLanguageIsFrench(mMainActivityState
                .context()) ? 0.85f : 1.0f;
        final float titleFontSizeSP = mMainActivityState.prefTracker()
                .getPageTitleFontSizePreference() * titleFontSpec.scale * frenchFactor;
        mPageTitleTextView.setTypeface(titleFontSpec.typeface);
        mPageTitleTextView.setExtraSpacingFractions(titleFontSpec.topExtraSpacingFraction,
                titleFontSpec.bottomExtraSpacingFraction);
        mPageTitleTextView.setTextSize(titleFontSizeSP);

        // Match vertical padding to title text size
        final int bottomPaddingPixels = (int) (Math.max(8, titleFontSizeSP * 0.12f) * mDensity);
        mPageTitleSection.setPadding(0, 0, 0, bottomPaddingPixels);

        // Update date size to match title size
        if (mPageKind.isToday()) {
            final int dayTopPaddingPixels = (int) (titleFontSizeSP * mDensity * 0.16f + 0.5f);
            final float dayTextSize = Math.min(20, Math.max(14, titleFontSizeSP * 0.35f));
            final float dateTextSize = Math.min(16, Math.max(14, titleFontSizeSP * 0.30f));
            mDayTextView.setPadding(0, dayTopPaddingPixels, 0, 0);
            mDayTextView.setTextSize(dayTextSize);
            mDateTextView.setTextSize(dateTextSize);
        }
    }

    /** Make the given item visible. */
    public void scrollToItem(int itemIndex) {
        mItemListView.scrollToItem(itemIndex);
    }

    /** Turn item view highlight on/off. */
    public void setItemViewHighlight(int itemIndex, boolean isHighlight) {
        mItemListView.setItemViewHighlight(itemIndex, isHighlight);
    }

    /**
     * Start animating given item.
     * 
     * @param initialDelayMillis - wait this time in milliseconds before starting the animation.
     *        Note that the method returns immediately even if a non zero delay is specified.
     */
    public final void startItemAnimation(int itemIndex, AppView.ItemAnimationType animationType,
            int initialDelayMillis, @Nullable Runnable callback) {
        mItemListView.startItemAnimation(itemIndex, animationType, initialDelayMillis, callback);
    }

    /** Popup an item menu for the given item. */
    public void showItemMenu(final int itemIndex, ItemMenuEntry actions[], final int dismissActionId) {
        mItemListView.showItemMenu(itemIndex, actions, dismissActionId);
    }

    /**
     * Update the item list to reflect the current model state. Does not affect other parts of the
     * page.
     */
    public final void updateAllItemViews() {
        // We simply force the model to send an updated notifcation.
        mItemListView.getAdapter().notifyDataSetChanged();
    }

    /** Update undo button bases on the current model state. */
    public final void updateUndoButton() {
        final boolean hasUndo = mMainActivityState.model().pageHasUndo(mPageKind);
        mButtonUndoView.setVisibility(hasUndo ? VISIBLE : INVISIBLE);
    }

    /** Update item divider color on preference change */
    public final void onItemDividerColorPreferenceChange() {
        final int dividerColor = mMainActivityState.prefTracker()
                .getPageItemDividerColorPreference();

        // The alpha at the edge is 25% of the center alpha
        final int endGradiantColor = (dividerColor & 0x00ffffff)
                | ((dividerColor >> 3) & 0x1f000000);
        final int colors[] = new int[] {
            endGradiantColor,
            dividerColor,
            endGradiantColor
        };
        final GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        final Drawable cloneDrawable = drawable.getConstantState().newDrawable();
        mPageTitleDivider.setBackgroundDrawable(cloneDrawable);

        mItemListView.setDivider(drawable);
        mItemListView.setDividerHeight(2);

    }

    public final int getItemCount() {
        return mItemListView.getChildCount();
    }
}

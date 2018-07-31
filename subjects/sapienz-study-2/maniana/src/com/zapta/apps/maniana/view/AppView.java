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

import javax.annotation.Nullable;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.menus.ItemMenuEntry;
import com.zapta.apps.maniana.model.PageKind;

/**
 * A wrapper containing the entire view functioanlity.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class AppView {

    public static enum ItemAnimationType {
        DELETING_ITEM,
        MOVING_ITEM_TO_OTHER_PAGE,
        SORTING_ITEM,
    }

    private final MainActivityState mMainActivityState;

    /** The view with the two horizontally scrolling pages. */
    private final ViewPager mViewPager;

    /** The view of todays page. Contain header, item list, etc. */
    private final PageView mTodayPageView;

    /** The view of tomorrow page. Contain header, item list, etc. */
    private final PageView mTomorowPageView;

    /** Track the displayed page of the underlying view pager. */
    private int mCurrentPageIndex = 0;

    public AppView(MainActivityState mainActivityState) {
        this.mMainActivityState = mainActivityState;

        mTodayPageView = new PageView(mMainActivityState, PageKind.TODAY);
        mTomorowPageView = new PageView(mMainActivityState, PageKind.TOMOROW);

        mViewPager = new ViewPager(mMainActivityState.context());
        mViewPager.setAdapter(new PagerViewAdapter(mTodayPageView, mTomorowPageView));

        // Make sure we are in sync with the view.
        mViewPager.setCurrentItem(mCurrentPageIndex);

        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPageIndex = position;
            }
        });
    }
    
    /** Get the current page view */
    private PageView getCurrentPageView() {
        return getPageView(getCurrentPageKind());
    }


    /** Get the page view of given kind. */
    private PageView getPageView(PageKind pageKind) {
        return pageKind.isToday() ? mTodayPageView : mTomorowPageView;
    }

    /** Get the root View of this app view. Used to display it in an activity. */
    public final View getRootView() {
        return mViewPager;
    }

    public final void startItemAnimation(PageKind pageKind, int itemIndex,
            ItemAnimationType animationType, int initialDelayMillis,
            @Nullable final Runnable callback) {
        getPageView(pageKind).startItemAnimation(itemIndex, animationType, initialDelayMillis,
                callback);
    }

    public void setItemViewHighlight(PageKind pageKind, int itemIndex, boolean isHighlight) {
        getPageView(pageKind).setItemViewHighlight(itemIndex, isHighlight);
    }

    public void showItemMenu(PageKind pageKind, final int itemIndex, ItemMenuEntry actions[],
            final int dismissActionId) {
        getPageView(pageKind).showItemMenu(itemIndex, actions, dismissActionId);
    }
    
    public void showMainMenu() {
        getCurrentPageView().showMainMenu();
    }

    public final void updatePages() {
        updatePage(PageKind.TODAY);
        updatePage(PageKind.TOMOROW);
    }

    public final void updatePage(PageKind pageKind) {
        final PageView pageView = getPageView(pageKind);
        pageView.updateAllItemViews();
        pageView.updateUndoButton();
    }

    public final void updateUndoButtons() {
        mTodayPageView.updateUndoButton();
        mTomorowPageView.updateUndoButton();
    }

    public final void onItemDividerColorPreferenceChange() {
        mTodayPageView.onItemDividerColorPreferenceChange();
        mTomorowPageView.onItemDividerColorPreferenceChange();
    }

    public final void updateUndoButton(PageKind pageKind) {
        getPageView(pageKind).updateUndoButton();
    }

    public final void onDateChange() {
        mTodayPageView.onDateChange();
    }

    public final void onPageIconSetPreferenceChange() {
        mTodayPageView.onPageIconSetPreferenceChange();
        mTomorowPageView.onPageIconSetPreferenceChange();
    }
    
    public final void onPageItemFontVariationPreferenceChange() {
        mTodayPageView.onPageItemFontVariationPreferenceChange();
        mTomorowPageView.onPageItemFontVariationPreferenceChange();
    }

    public final void onPageBackgroundPreferenceChange() {
        mTodayPageView.onPageBackgroundPreferenceChange();
        mTomorowPageView.onPageBackgroundPreferenceChange();
    }
    
    public final void onPageTitlePreferenceChange() {
        mTodayPageView.onPageTitlePreferenceChange();
        mTomorowPageView.onPageTitlePreferenceChange();
    }

    /** Scroll period in millis. Ignored in < 0. */
    public final void setCurrentPage(PageKind pageKind, int scrollPeriodMillis) {
        mViewPager.mForcedScrollDurationMillis = scrollPeriodMillis;
        mViewPager.setCurrentItem(pageKind.isToday() ? 0 : 1);
        mViewPager.mForcedScrollDurationMillis = -1;
    }

    public final PageKind getCurrentPageKind() {
        return (mCurrentPageIndex == 0) ? PageKind.TODAY : PageKind.TOMOROW;
    }

    public void scrollToItem(PageKind pageKind, int itemIndex) {
        getPageView(pageKind).scrollToItem(itemIndex);
    }
}

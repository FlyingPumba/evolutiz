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

import java.util.ArrayList;

import com.zapta.apps.maniana.annotations.MainActivityScope;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Adapter for the pager view that contains the two page views.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class PagerViewAdapter extends PagerAdapter {
    /** A list with today and tomorow pages. */
    private final ArrayList<PageView> pages;

    public PagerViewAdapter(PageView todayPageView, PageView tomorrowPageView) {
        pages = new ArrayList<PageView>();
        pages.add(todayPageView);
        pages.add(tomorrowPageView);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    /** Return the page view at given index. */
    @Override
    public Object instantiateItem(View parentView, int position) {
        ((ViewPager) parentView).addView(pages.get(position), 0);
        return pages.get(position);
    }

    @Override
    public void destroyItem(View parentView, int position, Object childView) {
        ((ViewPager) parentView).removeView((PageView) childView);
    }

    @Override
    public boolean isViewFromObject(View view, Object childView) {
        return view == ((PageView) childView);
    }

    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
    }
}

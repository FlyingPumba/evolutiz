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

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.PageKind;

/**
 * Adapter between a page model and a item list view.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class ItemListViewAdapter extends BaseAdapter {
    private final MainActivityState mMainActivityState;
    private final PageKind mPageKind;

    public ItemListViewAdapter(MainActivityState mainActivityState, PageKind pageKind) {
        this.mMainActivityState = mainActivityState;
        this.mPageKind = pageKind;
    }

    /**
     * Implementation of method from BaseAdapter.
     * 
     * NOTE(tal): 'enabled' in this context means a non separator item in the list view. When an
     * item is classified as disabled, its divider does not show up. For the page items we do want
     * to show deviders.
     */
    @Override
    public final boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public final int getCount() {
        return mMainActivityState.model().getPageItemCount(mPageKind);
    }

    @Override
    public final ItemModelReadOnly getItem(int position) {
        return mMainActivityState.model().getItemReadOnly(mPageKind, position);
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        final ItemModelReadOnly itemModel = mMainActivityState.model().getItemReadOnly(mPageKind,
                position);

        final ItemView itemView;
        if (convertView == null) {
            // Handle the case where the list view need a new item view.
            itemView = new ItemView(mMainActivityState, mPageKind, itemModel);
        } else {
            // Handle the case where the list view recycles an old item view.
            itemView = (ItemView) (convertView);
            itemView.clearHighlight();
            itemView.updateFromItemModel(itemModel);
        }

        return itemView;
    }

    public final PageKind pageKind() {
        return mPageKind;
    }
}

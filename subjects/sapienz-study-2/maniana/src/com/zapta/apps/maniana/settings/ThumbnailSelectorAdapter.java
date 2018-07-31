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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ActivityScope;

/**
 * Provide assertion methods.
 * 
 * @author Tal Dayan
 */
@ActivityScope
public class ThumbnailSelectorAdapter<T extends Thumbnail> extends BaseAdapter {

    private final Context mContext;

    private final T[] mThumbnails;

    private final int mTextColor;

    public ThumbnailSelectorAdapter(Context c, T[] thumbnails, int textColor) {
        mContext = c;
        mThumbnails = thumbnails;
        mTextColor = textColor;
    }

    public int getCount() {
        return mThumbnails.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final LinearLayout itemView;
        if (convertView != null) {
            // Recycle
            itemView = (LinearLayout) convertView;
        } else {
            // New view
            final LayoutInflater inflator = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = (LinearLayout) inflator.inflate(R.layout.thumbnail_selector_item_layout,
                    null);
        }

        final ImageView imageView = (ImageView) itemView
                .findViewById(R.id.thumbnail_selector_item_image);
        final TextView textView = (TextView) itemView
                .findViewById(R.id.thumbnail_selector_item_text);

        final Thumbnail thumbnail = mThumbnails[position];
        imageView.setImageResource(thumbnail.getDrawableId());
        // NOTE: the extra space at the preventsend truncation at the end due to the italic style.
        // The extra space at the beginning is to preserve the text centring.
        textView.setText(" " + thumbnail.getName(mContext) + " ");
        textView.setTextColor(mTextColor);
        return itemView;
    }
}

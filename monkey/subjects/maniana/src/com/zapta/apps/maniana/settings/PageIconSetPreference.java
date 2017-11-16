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

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import javax.annotation.Nullable;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ActivityScope;

/**
 * @author George Yunaev (http://tinyurl.com/7jnoubo)
 * @author Tal Dayan
 */
@ActivityScope
public class PageIconSetPreference extends DialogPreference {

    private final PageIconSet mDefaultValue;

    private PageIconSet mValue;

    /**
     * Format string for preference summary string (when dialog is closed). Can contain a single %s
     * place holder for currently selected icon set name.
     */
    private String mSummaryFormat;

    public class IconSetAdapter extends BaseAdapter {
        final Context mContext;

        @Nullable
        private final PageIconSet mSelectedIconSet;

        private final LayoutInflater mInflater;

        public IconSetAdapter(Context context, @Nullable PageIconSet selectedIconSet) {
            this.mContext = context;
            this.mSelectedIconSet = selectedIconSet;
            this.mInflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return PageIconSet.values().length;
        }

        @Override
        public Object getItem(int position) {
            return PageIconSet.values()[position].getName(mContext);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.icon_set_preference_row_layout, parent, false);
            }

            final PageIconSet iconSet = PageIconSet.values()[position];

            // NOTE: we don't bother to keep the internal view refernces in an holder, this
            // list is small and is not used too often.
            ((RadioButton) view.findViewById(R.id.icon_set_preference_radio_button))
                    .setChecked(iconSet == mSelectedIconSet);

            ((ImageView) view.findViewById(R.id.icon_set_preference_icon1))
                    .setImageResource(iconSet.buttonUndoResourceId);
            ((ImageView) view.findViewById(R.id.icon_set_preference_icon2))
                    .setImageResource(iconSet.buttonAddByTextResourceId);
            ((ImageView) view.findViewById(R.id.icon_set_preference_icon3))
                    .setImageResource(iconSet.buttonAddByVoiceResourceId);
            ((ImageView) view.findViewById(R.id.icon_set_preference_icon4))
                    .setImageResource(iconSet.buttonCleanResourceId);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onIconSetClicked(iconSet);
                }
            });
            return view;
        }
    }

    public PageIconSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        final String defaultIconSetKey = attrs.getAttributeValue(
                PreferenceConstants.ANDROID_NAME_SPACE, "defaultValue");
        mDefaultValue = PageIconSet.fromKey(defaultIconSetKey, null);
        checkNotNull(mDefaultValue, "Unknown default icon pref key: [%s]", defaultIconSetKey);
        mValue = mDefaultValue;

        {
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.PageIconSetPreference);
            mSummaryFormat = a.getString(R.styleable.PageIconSetPreference_summaryFormat);
            a.recycle();
        }

        updateSummaryWithCurrentValue();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        final IconSetAdapter adapter = new IconSetAdapter(getContext(), readValue());
        builder.setAdapter(adapter, this);
        builder.setPositiveButton(null, null);
    }

    /** Called when an icon set is clicked in the list. */
    private void onIconSetClicked(PageIconSet selectedIconSet) {
        setValue(selectedIconSet);
        getDialog().dismiss();
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            mValue = shouldPersist() ? readValue() : mDefaultValue;
        } else {
            mValue = PageIconSet.fromKey((String) defaultValue, mDefaultValue);
        }
        updateSummaryWithCurrentValue();
    }

    public final void setValue(PageIconSet iconSet) {
        mValue = iconSet;
        Editor editor = getSharedPreferences().edit();
        editor.putString(getKey(), mValue.getKey());
        editor.commit();
        updateSummaryWithCurrentValue();
    }

    private final PageIconSet readValue() {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences == null) {
            // Shared preferences not bound yet
            return mDefaultValue;
        }
        final String selectedIconSetKey = sharedPreferences.getString(getKey(),
                mDefaultValue.getKey());
        return PageIconSet.fromKey(selectedIconSetKey, mDefaultValue);
    }

    private final void updateSummaryWithCurrentValue() {
        super.setSummary(String.format(mSummaryFormat, mValue.getName(getContext())));
    }
}

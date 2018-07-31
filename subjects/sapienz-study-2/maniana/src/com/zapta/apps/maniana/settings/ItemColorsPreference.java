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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Nullable;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.settings.ItemColorsPreferenceDialog.ItemColorsChangeListener;
import com.zapta.apps.maniana.util.LogUtil;

public class ItemColorsPreference extends Preference implements
        Preference.OnPreferenceClickListener, ItemColorsChangeListener {

    private String mDefaultValue;

    private String mValue;

    public ItemColorsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDefaultValue = attrs.getAttributeValue(PreferenceConstants.ANDROID_NAME_SPACE,
                "defaultValue");

        mValue = shouldPersist() ? getPersistedString(mDefaultValue) : mDefaultValue;

        setOnPreferenceClickListener(this);
    }

    /** Called when the prefernce is clicked in the Settings window */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        final AlertDialog dialog = ItemColorsPreferenceDialog.CreateDialog(getContext(),
                DecodeValue(mValue), this);
        dialog.show();

        // TODO: should we return true?
        return false;
    }

    /** This how the initial value is actually recieved. */
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            mValue = shouldPersist() ? getPersistedString(mDefaultValue) : mDefaultValue;
        } else {
            mValue = (String) defaultValue;
        }
    }
    
    /** Called when the user selects a new color set in the dialog. */
    @Override
    public void onTasksColorsSetChange(List<ItemColor> enabledColors) {
        final String newValue = EncodeValue(enabledColors);
        setValue(newValue);
    }

    public void setValue(final String newValue) {
        if (newValue != mValue) {
            mValue = newValue;
            if (shouldPersist()) {
                persistString(mValue);
            }
            // NOTE: we ignore the returned value and always use the value.
            callChangeListener(mValue);
        }
    }

    /** Encode a list of items colors as a string. Compatible with DecodeValue(). */
    public static String EncodeValue(final List<ItemColor> enabledColors) {
        StringBuilder builder = new StringBuilder();
        int itemNum = 0;
        for (final ItemColor itemColor : enabledColors) {
            if (itemNum++ > 0) {
                builder.append(',');
            }
            builder.append(itemColor.getKey());
        }
        return builder.toString();
    }

    /** Decode a item color list from a string. Ignore silently unknown color keys. */
    public static List<ItemColor> DecodeValue(@Nullable String value) {
        if (value == null) {
            value = "";
        }

        final ArrayList<ItemColor> result = new ArrayList<ItemColor>();
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        boolean colorsFound[] = new boolean[ItemColor.values().length];
        while (tokenizer.hasMoreElements()) {
            @Nullable
            final String token = tokenizer.nextToken();
            final ItemColor itemColor = ItemColor.fromKey(token, null);
            if (itemColor == null) {
                LogUtil.warning("Unknown item color key [%s], ignoring", token);
            } else {
                if (colorsFound[itemColor.ordinal()]) {
                    LogUtil.warning("Duplicate item color key [%s], ignoring", token);
                } else {
                    colorsFound[itemColor.ordinal()] = true;
                    result.add(itemColor);
                }
            }
        }

        return result;
    }
}

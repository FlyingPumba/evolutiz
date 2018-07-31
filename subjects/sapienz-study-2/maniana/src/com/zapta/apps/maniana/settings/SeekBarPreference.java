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
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ActivityScope;
import com.zapta.apps.maniana.util.Orientation;

/**
 * Custom preference for selecting an integer within range.
 * 
 * Downloaded from http://android.hlidskialf.com/blog/code/android-seekbar-preference
 * 
 * @Author Matthew Wiggins
 * @author Tal Dayan
 */
@ActivityScope
public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

    private final Context mContext;

    /** Cached device density. */
    private float mDensity;

    /** Format string for running label in dialog. Should contain %d. Attribute: text */
    private final String mValueFormat;

    /** Default value. Attribute: defaultValue */
    private final int mDefaultValue;

    /** Min value (inclusive). Attribute: minLevel */
    private final int mMinValue;

    /** Max value, (inclusive). Attribute: maxLevel */
    private final int mMaxValue;

    /** Current preference value. Updated when the user OKs a new value. */
    private int mValue;

    /**
     * Format string for preference summary string (when dialog is closed). Can contain a single %d
     * for current value.
     */
    private String mSummaryFormat;

    /** The SeekBar in the dialog. */
    private SeekBar mSeekBar;

    /** The running value string field in the dialog. */
    private TextView mValueTextView;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mDensity = context.getResources().getDisplayMetrics().density;

        // Attributes defined in res/values/attr.xml
        {
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.SeekBarPreference);

            mValueFormat = a.getString(R.styleable.SeekBarPreference_valueFormat);
            mSummaryFormat = a.getString(R.styleable.SeekBarPreference_summaryFormat);
            mMinValue = a.getInt(R.styleable.SeekBarPreference_minValue, 0);
            mMaxValue = a.getInt(R.styleable.SeekBarPreference_maxValue, 0);
            
            a.recycle();
        }

        checkNotNull(mValueFormat, "Null label format");
        checkNotNull(mSummaryFormat, "Null summary format");

        mDefaultValue = attrs.getAttributeIntValue(PreferenceConstants.ANDROID_NAME_SPACE,
                "defaultValue", 50);
        mValue = shouldPersist() ? getPersistedInt(mDefaultValue) : mDefaultValue;

        updateSummaryWithCurrentValue();
    }

    /** Dialog preference */
    @Override
    protected View onCreateDialogView() {
        // NOTE: in landscape mode we use tighter vertical spacing so everything
        // fits in screen.
        final boolean isPortrait = Orientation.currentDeviceOrientation(mContext).isPortrait;
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        final int layoutHorisontalPaddingPx = px(7);
        layout.setPadding(layoutHorisontalPaddingPx, 0, layoutHorisontalPaddingPx, 0);

        mValueTextView = new TextView(mContext);
        mValueTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueTextView.setPadding(0, isPortrait ? px(10) : px(3), 0, 0);
        // NOTE: text size is in scaled pixels (sp), no need to scale.
        mValueTextView.setTextSize(isPortrait ? 64 : 50);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueTextView, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        final int seekBarVerticalPaddingPx = isPortrait ? px(26) : px(7);
        final int seekBarHorisontalPaddingPx = px(20);
        mSeekBar.setPadding(seekBarHorisontalPaddingPx, seekBarVerticalPaddingPx,
                seekBarHorisontalPaddingPx, seekBarVerticalPaddingPx);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mValue - mMinValue);
        return layout;
    }

    /** DialogPreference */
    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        // NOTE: SeekBar values are zero relative so we offset them accordingly
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mValue - mMinValue);
    }

    /** Preference */
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            mValue = shouldPersist() ? getPersistedInt(mDefaultValue) : mDefaultValue;
        } else {
            mValue = (Integer) defaultValue;
        }
        updateSummaryWithCurrentValue();
    }

    /** Called from mSeekBar. */
    @Override
    public void onProgressChanged(SeekBar seek, int seekBarValue, boolean fromTouch) {
        final int currentValue = seekBarValue + mMinValue;
        mValueTextView.setText(String.format(mValueFormat, currentValue));
    }

    /** Called from mSeekBar. */
    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // Nothing to do here
    }

    /** Called from mSeekBar. */
    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // Nothing to do here
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        // We accept changes only if the user clicked OK
        if (positiveResult) {
            final int newValue = mSeekBar.getProgress() + mMinValue;
            setValue(newValue);
        }
    }

    public void setValue(int newValue) {
        if (newValue != mValue) {
            // Enforce range
            mValue = Math.min(mMaxValue, Math.max(mMinValue, newValue));
            updateSummaryWithCurrentValue();
            if (shouldPersist()) {
                persistInt(mValue);
            }
            // NOTE: we ignore the returned value and always use the value.
            callChangeListener(Integer.valueOf(mValue));
        }
    }

    private final void updateSummaryWithCurrentValue() {
        super.setSummary(String.format(mSummaryFormat, mValue));
    }

    /** Dip to pixel converter. */
    private final int px(int dip) {
        return (int) (dip * mDensity + 0.5f);
    }
}

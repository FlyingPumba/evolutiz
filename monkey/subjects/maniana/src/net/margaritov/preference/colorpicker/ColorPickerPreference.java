/*
 * Copyright (C) 2011 Sergey Margaritov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.margaritov.preference.colorpicker;

import com.zapta.apps.maniana.util.ColorUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A preference type that allows a user to choose a time
 * 
 * @author Sergey Margaritov
 */
public class ColorPickerPreference extends Preference implements
        Preference.OnPreferenceClickListener, ColorPickerDialog.OnColorChangedListener {

    private static final boolean PRE_API_14 = android.os.Build.VERSION.SDK_INT < 14;

    View mView;
    int mDefaultValue = Color.BLACK;
    private int mValue = Color.BLACK;
    private float mDensity = 0;
    private boolean mAlphaSliderEnabled = false;
    private boolean mJustHsNoV = false;
    private float mMaxSaturation = 1.0f;

    private static final String androidns = "http://schemas.android.com/apk/res/android";

    public ColorPickerPreference(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    // TAL: From Preference
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        onColorChanged(restoreValue ? getValue() : (Integer) defaultValue);
    }

    private void init(Context context, AttributeSet attrs) {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        setOnPreferenceClickListener(this);
        if (attrs != null) {
            String defaultValue = attrs.getAttributeValue(androidns, "defaultValue");
            if (defaultValue.startsWith("#")) {
                try {
                    mDefaultValue = convertToColorInt(defaultValue);
                } catch (NumberFormatException e) {
                    Log.e("ColorPickerPreference", "Wrong color: " + defaultValue);
                    mDefaultValue = convertToColorInt("#FF000000");
                }
            } else {
                int resourceId = attrs.getAttributeResourceValue(androidns, "defaultValue", 0);
                if (resourceId != 0) {
                    mDefaultValue = context.getResources().getInteger(resourceId);
                }
            }
            // TODO: make the XML attribute to work and use them instead of the setting methods
            // below.
            // mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, "alphaSlider", false);
            // mJustHsNoV = attrs.getAttributeBooleanValue(null, "justHsNoV", false);
        }
        mValue = mDefaultValue;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mView = view;
        setPreviewColor();
    }

    // TAL: fixed for API V14.
    private void setPreviewColor() {
        if (mView == null) {
            return;
        }

        LinearLayout widgetFrameView = ((LinearLayout) mView
                .findViewById(android.R.id.widget_frame));
        if (widgetFrameView == null) {
            return;
        }

        // TAL: required starting from API 14
        widgetFrameView.setVisibility(View.VISIBLE);

        final int rightPaddingDip = PRE_API_14 ? 8 : 5;

        widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(), (int) (mDensity * rightPaddingDip),
                widgetFrameView.getPaddingBottom());

        // Tal: uncomment to examine the location of the color preview within the widget frame.
        // widgetFrameView.setBackgroundColor(0xff123456);

        // Remove previously created preview image.
        // Can we the reuse existing image view?
        int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }

        // Image view with check board background that that wraps to the bitmap size.
        ImageView iView = new ImageView(getContext());
        iView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
        iView.setImageBitmap(getPreviewBitmap());
        iView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        widgetFrameView.addView(iView);
    }

    private Bitmap getPreviewBitmap() {
        // NOTE: on ICS and above we display a smaller color preview to match the
        // general style.
        int d = (int) (mDensity * (PRE_API_14 ? 31 : 22));
        // If disabled we simulate a semi transparent black overlay.
        // NOTE: could simplify compositeColor for the special case where argb2 is aa000000;
        int color = isEnabled() ? getValue() : ColorUtil.compositeColor(getValue(), 0xaa000000);
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c = (i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2) ? Color.GRAY : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }

    public int getValue() {
        try {
            if (isPersistent()) {
                mValue = getPersistedInt(mDefaultValue);
            }
        } catch (ClassCastException e) {
            mValue = mDefaultValue;
        }

        return mValue;
    }

    // TAL: called by local dialog
    @Override
    public void onColorChanged(int color) {
        if (isPersistent()) {
            persistInt(color);
        }
        mValue = color;
        setPreviewColor();
        try {
            getOnPreferenceChangeListener().onPreferenceChange(this, color);
        } catch (NullPointerException e) {

        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        ColorPickerDialog picker = new ColorPickerDialog(getContext(), getValue());
        // TAL: added propagation of title to dialog
        picker.setTitle(getTitle());

        picker.setOnColorChangedListener(this);
        if (mAlphaSliderEnabled) {
            picker.setAlphaSliderVisible(true);
        }
        if (mJustHsNoV) {
            picker.setJustHsNoV(mMaxSaturation);
        }
        picker.show();

        return false;
    }

    /**
     * Toggle Alpha Slider visibility (by default it's disabled)
     * 
     * @param enable
     */
    public void setAlphaSliderEnabled(boolean enable) {
        mAlphaSliderEnabled = enable;
    }

    public void setJustHsNoV(float maxSaturation) {
        mJustHsNoV = true;
        mMaxSaturation = maxSaturation;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     * 
     * @param color
     * @author Unknown
     */
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     * 
     * @param argb
     * @throws NumberFormatException
     * @author Unknown
     */
    public static int convertToColorInt(String argb) throws NumberFormatException {

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }
}

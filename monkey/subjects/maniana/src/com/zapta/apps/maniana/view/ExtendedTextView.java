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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Provides few tweaks of the stock TextView.
 * 
 * @author Tal Dayan
 */
public class ExtendedTextView extends TextView {

    private float mTopExtraSpacingFraction = 0.0f;
    private float mBottomExtraSpacingFraction = 0.0f;

    public ExtendedTextView(Context context) {
        super(context);
    }

    public ExtendedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set the vertical height extension of the last text of line. Used to avoid truncation of the
     * bottom of the last line when using line spacing smaller than the default.
     * 
     * @param lastLineHeightExtensionFraction the extra extension as a fraction of line height.
     *        Default value is 0.0f for no extension.
     */
    public void setExtraSpacingFractions(float topExtraSpacingFraction,
            float bottomExtraSpacingFraction) {
        // TODO: exact comparisons of floats. Is is safe?
        if (mTopExtraSpacingFraction != topExtraSpacingFraction
                || mBottomExtraSpacingFraction != bottomExtraSpacingFraction) {
            mTopExtraSpacingFraction = topExtraSpacingFraction;
            mBottomExtraSpacingFraction = bottomExtraSpacingFraction;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final boolean translate = (mTopExtraSpacingFraction != 0);

        if (translate) {
            final int topExtraSpacingPixels = (int) (getTextSize() * mTopExtraSpacingFraction);
            canvas.save();
            canvas.translate(0, topExtraSpacingPixels);
        }

        super.onDraw(canvas);

        if (translate) {
            canvas.restore();
        }
    }

    /**
     * Apply the extra line extension if non zero.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // TODO: exact comparisons of floats. Is is safe?
        final float extraSpacingFraction = mTopExtraSpacingFraction + mBottomExtraSpacingFraction;
        if (extraSpacingFraction != 0) {
            final int extraHeightPixels = (int) (getTextSize() * extraSpacingFraction);
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + extraHeightPixels);
        }
    }
}

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
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Provides few tweaks of the stock EditText. Similar to ExtendedTextView.
 * <p>
 * TODO: better code sharing with ExtendedTextView
 * 
 * @author Tal Dayan
 */
public class ExtendedEditText extends EditText {
    
    // NOTE: overriding onDraw and translating the canvas as done in ExtendedTextView results with an artifact
    // in the editor view. The top portion of the blinking text cursor does not blink properly. For this reason,
    // the ExtendedEditText always assumes extra top spacing of zero. 
    // Observed on Android 2.3.x, works well on 4.1

    private float mBottomExtraSpacingFraction = 0.0f;

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedEditText(Context context) {
        super(context);
    }

    /**
     * Similar to ExtendedTextView.
     */
    public void setExtraSpacingFractions(float bottomExtraSpacingFraction) {
        // TODO: exact comparisons of floats. Is is safe?
        if (mBottomExtraSpacingFraction != bottomExtraSpacingFraction) {
            mBottomExtraSpacingFraction = bottomExtraSpacingFraction;
            invalidate();
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // TODO: exact comparisons of floats. Is is safe?
        final float extraSpacingFraction = mBottomExtraSpacingFraction;
        if (extraSpacingFraction != 0) {
            final int extraHeightPixels = (int) (getTextSize() * extraSpacingFraction);
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + extraHeightPixels);
        }
    }
}

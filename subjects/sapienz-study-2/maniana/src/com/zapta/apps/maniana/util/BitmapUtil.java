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

package com.zapta.apps.maniana.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * @author Tal Dayan
 */
@ApplicationScope
public final class BitmapUtil {
    private static final boolean PRE_API_14 = android.os.Build.VERSION.SDK_INT < 14;

    /** Do not instantiate */
    private BitmapUtil() {
    }

    /** Return a bitmap identical to src with rounded corners. */
    public static final Bitmap roundCornersRGB888(Bitmap src, int roundPixles) {
        final Bitmap output = Bitmap
                .createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // TODO: document what is special about this color
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPixles, roundPixles, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);

        return output;
    }
    
    /** All dimensions are in pixels */
    private static Bitmap createColorSwatchBitmap(int widthPixels, int heightPixels, int borderWidthPixels, int fillColor, int borderColor) {
       final Bitmap bitmap = Bitmap.createBitmap(widthPixels, heightPixels, Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);     
        if (borderWidthPixels > 0) {
            canvas.drawColor(borderColor);
            canvas.clipRect(borderWidthPixels, borderWidthPixels, widthPixels - borderWidthPixels, heightPixels - borderWidthPixels);
        }
        canvas.drawColor(fillColor);
        return bitmap;
    }
    
    /** Move to PreferenceUtil */
    public static Bitmap getPreferenceColorPreviewBitmap(int color, boolean isEnabled, float density) {
        // Determine size in pixels
        final int sizeDips = PRE_API_14 ? 31 : 22;
        int sizePixels = (int) (density * sizeDips + 0.5f); 
        
        // Determine colors
        int fillColor = color;
        int borderColor = Color.GRAY;
        if (!isEnabled) {
            // Darken the colors if not enabled.
            fillColor = ColorUtil.compositeColor(fillColor, 0xaa000000);
            borderColor = ColorUtil.compositeColor(borderColor, 0xaa000000);
        }
        
        // Construct the bitmap
        return BitmapUtil.createColorSwatchBitmap(sizePixels, sizePixels, 2, fillColor, borderColor);
    }
}

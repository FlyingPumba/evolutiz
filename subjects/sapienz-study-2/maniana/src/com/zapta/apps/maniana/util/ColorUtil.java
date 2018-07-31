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

import static com.zapta.apps.maniana.util.Assertions.check;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.annotations.VisibleForTesting;

import android.graphics.Color;

/**
 * @author Tal Dayan
 */
@ApplicationScope
public final class ColorUtil {

    /** Do not instantiate */
    private ColorUtil() {
    }

    /**
     * Returns candidate color furtherest from given reference color. In case of a tie, prefer
     * candidates with lower indices. Candidates array must contain at least one member. Alpha
     * channel is ignored.
     */
    public static final int selectFurthestColor(int referenceColor, int candidates[],
            float defaultPreference) {
        final int index = selectFurthestColorIndex(referenceColor, candidates, defaultPreference);
        return candidates[index];
    }

    /**
     * Returns index of candidate color furtherest from given reference color. In case of a tie,
     * prefer candidates with lower indices. Candidates array must contain at least one member.
     * Alpha channel is ignored.
     * 
     * @param defaultPreference a float in the range [0,1] indicating the left of preference to give
     *        to first candidate.
     */
    public static final int selectFurthestColorIndex(int referenceColor, int candidates[],
            float defaultPreference) {
        check(candidates.length > 0);

        final int r = Color.red(referenceColor);
        final int g = Color.green(referenceColor);
        final int b = Color.blue(referenceColor);

        int bestCandidateIndex = -1;
        int bestDistance = -1;

        for (int i = 0; i < candidates.length; i++) {
            final int nextColor = candidates[i];
            int nextDistance = distance(r, g, b, nextColor);
            check(nextDistance >= 0);
            // If default candidate, skew the distance by preference.:w
            if (i == 0) {
                nextDistance += (int) (255 * 3 * defaultPreference);
            }
            if (nextDistance > bestDistance) {
                bestCandidateIndex = i;
                bestDistance = nextDistance;
            }
        }

        check(bestCandidateIndex >= 0);
        return bestCandidateIndex;
    }
    
    /**
     * Compute the distance between two colors. The alpha channel is ignored. Returned distance is
     * always >= 0.
     */
    public static final int distance(int r, int g, int b, int color) {
        return Math.abs(r - Color.red(color)) + Math.abs(g - Color.green(color))
                + Math.abs(b - Color.blue(color));
    }

    /**
     * Compute the combined alpha of a laver with alpha a2 over a layer with alpha a1. a1, a2 are in
     * the range [0..255]. Where 0 indicates transparent and 255 indicates opaque color. The method
     * does not perform range checking. Providing out of range value parameters in undefined result.
     * 
     * Formula is based on http://tinyurl.com/79dmuay.
     */
    @VisibleForTesting
    static int compositeAlpha(int a1, int a2) {
        return 255 - ((255 - a2) * (255 - a1)) / 255;
    }

    /**
     * Compute the combined color component (R, G or B) of layer 2 over layer 1.
     * 
     * @param c1 color of layer 1 [0 .. 255]
     * @param a1 alpha of layer 1 [0 .. 255]
     * @param c2 color of layer 2 [0 .. 255]
     * @param a2 alpha of layer 1 [0 .. 255]
     * @param a compositeAlpha(a1, a2)
     * @return the composite color component [0..255]
     */
    @VisibleForTesting
    static int compositeColorComponent(int c1, int a1, int c2, int a2, int a) {
        // Handle the singular case of both layers fully transparent.
        if (a == 0) {
            return 0x00;
        }
        return (((255 * c2 * a2) + (c1 * a1 * (255 - a2))) / a) / 255;
    }

    /**
     * Compute the combined color of a layer with color argb2 over layer with color argb1.
     */
    public static int compositeColor(int argb1, int argb2) {
        final int a1 = Color.alpha(argb1);
        final int a2 = Color.alpha(argb2);
        final int a = compositeAlpha(a1, a2);
        final int r = compositeColorComponent(Color.red(argb1), a1, Color.red(argb2), a2, a);
        final int g = compositeColorComponent(Color.green(argb1), a1, Color.green(argb2), a2, a);
        final int b = compositeColorComponent(Color.blue(argb1), a1, Color.blue(argb2), a2, a);
        return Color.argb(a, r, g, b);
    }

    /** Map background color preference to paper overlay color. */
    public static int mapPaperColorPrefernce(int paperColorPreference) {
        final float hsv[] = new float[3];
        Color.colorToHSV(paperColorPreference, hsv);
        // Map saturation to alpha. The paper bitmap below the template will provide
        // the white background.
        final int alpha = (int) (hsv[1] * 255);
        // Saturation and value are set to max.
        hsv[1] = 1.f;
        hsv[2] = 1.f;
        return Color.HSVToColor(alpha, hsv);
    }
}

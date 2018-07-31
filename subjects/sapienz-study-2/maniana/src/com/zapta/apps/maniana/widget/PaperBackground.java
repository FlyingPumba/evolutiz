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

package com.zapta.apps.maniana.widget;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Descriptor of a paper background bitmap resource. For technical reasons, the paper background
 * bitmap size in pixels must be smaller than the rendered list widget template bitmap, otherwise
 * the ImageView is stretched to accommodate the background paper. For this reason, we use a set of
 * paper images of different sizes and pick dynamically the best fit.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class PaperBackground {

    private static final PaperBackground PAPER_BACKGROUNDS[] = new PaperBackground[] {
        new PaperBackground(R.drawable.widget_paper_104x114, 104, 114, 4),
        new PaperBackground(R.drawable.widget_paper_300x064, 300, 64, 4),
        new PaperBackground(R.drawable.widget_paper_304x194, 304, 194, 4),
        new PaperBackground(R.drawable.widget_paper_304x282, 304, 382, 4)
    };

    public final int drawableResourceId;
    public final int widthPixels;
    public final int heightPixels;
    
    /** The width of the drop shadow on right and bottom margins. */
    public final int shadowPixels;

    private PaperBackground(int drawableResourceId, int widthPixels, int heightPixels, int shadowPixels) {
        this.drawableResourceId = drawableResourceId;
        this.widthPixels = widthPixels;
        this.heightPixels = heightPixels;
        this.shadowPixels = shadowPixels;
    }
    
    public final int shadowRightPixels(int strechedWidthPixels) {
        // TODO: round the result?
        return (shadowPixels * strechedWidthPixels) / widthPixels;
    }
    
    public final int shadowBottomPixels(int strechedHeightPixels) {
        // TODO: round the result?
        return (shadowPixels * strechedHeightPixels) / heightPixels;
    }

    /**
     * Return fitness metric of this paper for given widget size.
     * 
     * @return merit metrics. Higher values are preferred over lower values. May be negative. Actual
     *         value is meanigless. Should be used for comparison only
     */
    private final int merit(int widthPixels, int heightPixels) {
        final boolean fits = (this.widthPixels <= widthPixels)
                && (this.heightPixels <= heightPixels);
        int result = 0;
        if (fits) {
            // Prefers backgrounds with larger areas
            result += (this.widthPixels * this.heightPixels);
        } else {
            // Prefer backgrounds with minimal extra dimension.
            result -= Math.max(0, this.widthPixels - widthPixels);
            result -= Math.max(0, this.heightPixels - heightPixels);
        }
        return result;
    }

    static final PaperBackground getBestSize(int widthPixels, int heightPixels) {
        PaperBackground bestResult = null;
        int bestMerit = 0;

        for (PaperBackground iter : PAPER_BACKGROUNDS) {
            final int iterMerit = iter.merit(widthPixels, heightPixels);
            if (bestResult == null || iterMerit > bestMerit) {
                bestResult = iter;
                bestMerit = iterMerit;
            }
        }

        // Should not fail since we have at least one background and we always set it
        // on the first iteration.
        checkNotNull(bestResult, "No paper matched %d x %d", widthPixels, heightPixels);

        if (bestResult.widthPixels > widthPixels
                || bestResult.heightPixels > heightPixels) {
            LogUtil.error("Paper background does not fit: %d x %s -> %d x %d", widthPixels,
                    heightPixels, bestResult.widthPixels, bestResult.heightPixels);
        }

        return bestResult;
    }
}

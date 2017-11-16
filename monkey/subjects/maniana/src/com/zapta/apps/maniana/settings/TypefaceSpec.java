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
import android.graphics.Typeface;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * Immutable parameters of a typeface that is mapped to a font.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class TypefaceSpec {

    public final Typeface typeface;

    /** Relative scale to normalize size among font types. */
    public final float scale;

    public final float lineSpacingMultipler;
    
    /** Extra space above first line. Fraction of text height. */
    public final float topExtraSpacingFraction;
    
    /** Extra space below last line. Fraction of text height. */
    public final float bottomExtraSpacingFraction;

    /** Construct from typeface. */
    public TypefaceSpec(Typeface typeface, float scale, float lineSpacingMultipler,
            float topExtraSpacingFraction, float bottomExtraSpacingFraction) {
        this.typeface = typeface;
        this.scale = scale;
        this.lineSpacingMultipler = lineSpacingMultipler;
        this.topExtraSpacingFraction = topExtraSpacingFraction;
        this.bottomExtraSpacingFraction = bottomExtraSpacingFraction;
    }

    /** Construct from asset font file. */
    public TypefaceSpec(Context context, String assetFilePath, float scale,
            float lineSpacingMultipler, float topExtraSpacingFraction,
            float bottomExtraSpacingFraction) {
        this(Typeface.createFromAsset(context.getAssets(), assetFilePath), scale,
                lineSpacingMultipler, topExtraSpacingFraction, bottomExtraSpacingFraction);
    }
}
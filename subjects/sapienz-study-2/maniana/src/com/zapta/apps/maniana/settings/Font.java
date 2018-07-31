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

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.Typeface;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.EnumUtil;
import com.zapta.apps.maniana.util.EnumUtil.KeyedEnum;
import com.zapta.apps.maniana.util.LanguageUtil;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Represents possible values of Font preference.
 * <p>
 * TODO: cleanup the typeface caching mechanism. Kind of hacky at the moment.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum Font implements KeyedEnum {
    // NOTE: font keys are persisted in preferences. Do not modify.
    CURSIVE(R.string.font_name_Cursive, "cursive"),
    ELEGANT(R.string.font_name_Elegant, "elegant"),
    SAN_SERIF(R.string.font_name_Sans_Serif, "sans"),
    SERIF(R.string.font_name_Serif, "serif"),
    ITALIC(R.string.font_name_Italic, "italic"),
    IMPACT(R.string.font_name_Impact, "impact");

    /** User visible name. */
    private final int nameResourceId;

    /**
     * Preference value key. Should match the values in preference xml. Persisted in user's
     * settings.
     */
    private final String mKey;

    /**
     * Set at runtime, from context. Depends on language, configuration, etc.
     */
    private TypefaceSpec cachedTypefaceSpec = null;

    private Font(int nameResourceId, String key) {
        this.nameResourceId = nameResourceId;
        this.mKey = key;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    public final String getName(Context context) {
        return context.getString(nameResourceId);
    }

    /** Return value with given key, fallback value if not found. */
    @Nullable
    public final static Font fromKey(String key, @Nullable Font fallBack) {
        return EnumUtil.fromKey(key, Font.values(), fallBack);
    }

    /**
     * Called on a config change (e.g. language change) that may affect font to typeface spec
     * mapping.
     */
    public final static void onConfigChanged() {
        LogUtil.info("Font.onConfigChanged() called.");
        // Currently only IMPACT may be affected by config change.
        IMPACT.clearCachedTypeface();
    }

    /**
     * Returned typeface spec may change after a configuration change (e.g. Android language change.
     */
    public final synchronized TypefaceSpec getTypefaceSpec(Context context) {
        if (cachedTypefaceSpec == null) {
            cachedTypefaceSpec = loadTypeface(context);
        }
        return cachedTypefaceSpec;
    }

    private final synchronized void clearCachedTypeface() {
        cachedTypefaceSpec = null;
    }

    private final TypefaceSpec loadTypeface(Context context) {
        final boolean usesCyrillic = LanguageUtil.currentLanguageUsesCyrillic(context);

        switch (this) {
            case CURSIVE:
                return new TypefaceSpec(context, "fonts/Vavont/Vavont-modified.ttf", 1.5f, 0.75f,
                        -0.10f, 0.45f);

            case ELEGANT:
                // Pompiere-Regular-modified does not contains Cyrillic fonts.
                if (usesCyrillic) {
                    final Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC);
                    return new TypefaceSpec(typeface, 1.2f, 1.1f, 0.05f, 0.25f);
                }
                return new TypefaceSpec(context, "fonts/Pompiere/Pompiere-Regular-modified.ttf",
                        1.6f, 1.0f, 0.0f, 0.1f);

            case SAN_SERIF:
                return new TypefaceSpec(Typeface.SANS_SERIF, 1.2f, 1.1f, 0.05f, 0.25f);

            case SERIF:
                return new TypefaceSpec(Typeface.SERIF, 1.2f, 1.1f, 0.05f, 0.25f);

            case ITALIC:
                return new TypefaceSpec(Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC),
                        1.2f, 1.1f, 0.05f, 0.25f);

            case IMPACT:
                // Damion-Regular does not contains Cyrillic fonts.
                if (usesCyrillic) {
                    final Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC);
                    return new TypefaceSpec(typeface, 1.2f, 1.1f, 0.2f, 0.2f);
                }
                return new TypefaceSpec(context, "fonts/Damion/Damion-Regular.ttf", 1.6f, 0.7f,
                        0.0f, 0.1f);

            default:
                throw new RuntimeException("Unknown font: " + this);
        }
    }
}

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

import android.content.Context;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * @author Tal Dayan
 */
@ApplicationScope
public final class LanguageUtil {

    /** Do not instantiate */
    private LanguageUtil() {
    }

    /** Does the current language uses Cyrillic characters? */
    public static final boolean currentLanguageUsesCyrillic(Context context) {
        return"ru".equals(currentTranslationCode(context));  
    }
    
    /** Is the current language French? */
    public static final boolean currentLanguageIsFrench(Context context) {
        return"fr".equals(currentTranslationCode(context));  
    }
    
    /** Return the translation code from res/values[-xx]/string.xml */
    public static final String currentTranslationCode(Context context) {
        return context.getString(R.string.translation_language_code);    
    }
    
    /** 
     * Return the translation name from res/values[-xx]/local_string.xml.
     * This name is localized.
     */
    public static final String currentTranslationName(Context context) {
        return context.getString(R.string.translation_language_name);    
    }
}

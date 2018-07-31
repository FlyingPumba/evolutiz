/*
 * Copyright (C) 2008 Google Inc.
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

package com.beust.android.translate;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Button;

import java.util.Map;

/**
 * Language information for the Google Translate API.
 */
public final class Languages {
    
    /**
     * Reference at http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
     */
    static enum Language {
        CATALAN("ca", "Catalan"),
        CHINESE("zh", "Chinese", R.drawable.cn),
        CZECH("cs", "Czech", R.drawable.cs),
        DUTCH("nl", "Dutch", R.drawable.nl),
        ENGLISH("en", "English", R.drawable.us),
        ;
        
        private String mShortName;
        private String mLongName;
        private int mFlag;
        
        private static Map<String, String> mLongNameToShortName = Maps.newHashMap();
        private static Map<String, Language> mShortNameToLanguage = Maps.newHashMap();
        
        static {
            for (Language language : values()) {
                mLongNameToShortName.put(language.getLongName(), language.getShortName());
                mShortNameToLanguage.put(language.getShortName(), language);
            }
        }
        
        private Language(String shortName, String longName, int flag) {
            init(shortName, longName, flag);
        }
        
        private Language(String shortName, String longName) {
            init(shortName, longName, -1);
        }

        private void init(String shortName, String longName, int flag) {
            mShortName = shortName;
            mLongName = longName;
            mFlag = flag;
            
        }

        public String getShortName() {
            return mShortName;
        }

        public String getLongName() {
            return mLongName;
        }
        
        public int getFlag() {
            return mFlag;
        }

        @Override
        public String toString() {
            return mLongName;
        }
        
        public static Language findLanguageByShortName(String shortName) {
            return mShortNameToLanguage.get(shortName);
        }
        
        public void configureButton(Activity activity, Button button) {
            button.setTag(this);
            button.setText(getLongName());
            int f = getFlag();
            if (f != -1) {
                Drawable flag = activity.getResources().getDrawable(f);
                button.setCompoundDrawablesWithIntrinsicBounds(flag, null, null, null);
                button.setCompoundDrawablePadding(5);
            }
        }
    }

    public static String getShortName(String longName) {
        return Language.mLongNameToShortName.get(longName);
    }

    private static void log(String s) {
        Log.d(TranslateActivity.TAG, "[Languages] " + s);
    }

}


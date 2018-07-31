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

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.EnumUtil;
import com.zapta.apps.maniana.util.EnumUtil.KeyedEnum;

/**
 * Represents possible values of Font preference.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum ShakerAction implements KeyedEnum {
    NEW_ITEM_BY_TEXT("new_text"),
    NEW_ITEM_BY_VOICE("new_voice"),
    CLEAN("clean"),
    QUIT("quit");

    private final String mKey;

    private ShakerAction(String key) {
        this.mKey = key;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    /** Return value with given key, fallback value if not found. */
    @Nullable
    public final static ShakerAction fromKey(String key, @Nullable ShakerAction fallBack) {
        return EnumUtil.fromKey(key, ShakerAction.values(), fallBack);
    }
}
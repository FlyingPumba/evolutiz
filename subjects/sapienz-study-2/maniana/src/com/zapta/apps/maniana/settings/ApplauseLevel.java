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
 * Represents possible values on task completion applause level.
 * 
 * TODO: find a better name than 'level'. ('preference')?
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum ApplauseLevel implements KeyedEnum {
    NEVER("never"),
    SOMETIMES("sometimes"),
    ALWAYS("always");

    /** Preference value key. Should match the values in preference xml. */
    private final String mKey;

    private ApplauseLevel(String key) {
        this.mKey = key;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    /** Return value with given key, fallback value if not found. */
    @Nullable
    public final static ApplauseLevel fromKey(String key, @Nullable ApplauseLevel fallBack) {
        return EnumUtil.fromKey(key, ApplauseLevel.values(), fallBack);
    }
}

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
 * Represents the possible values of the lock expiration preference.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum LockExpirationPeriod implements KeyedEnum {
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    NEVER("never");

    /** Value key. Persisted. Change only if must. Should match keys in the preferences XML. */
    private final String mKey;

    private LockExpirationPeriod(String key) {
        this.mKey = key;
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    /** Return value with given key or fallback value if not found */
    @Nullable
    public final static LockExpirationPeriod fromKey(String key,
            @Nullable LockExpirationPeriod fallBack) {
        return EnumUtil.fromKey(key, LockExpirationPeriod.values(), fallBack);
    }
}
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

import javax.annotation.Nullable;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * @author Tal Dayan
 */
@ApplicationScope
public class EnumUtil {

    public static interface KeyedEnum {
        String getKey();
    }

    /** Return the first item with given key or fallBack if not found. */
    public static <T extends KeyedEnum> T fromKey(String key, T[] array, @Nullable T fallBack) {
        for (T t : array) {
            if (t.getKey().equals(key)) {
                return t;
            }
        }
        return fallBack;
    }
}

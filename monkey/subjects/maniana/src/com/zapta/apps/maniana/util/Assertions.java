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

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * Provide assertion methods.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public final class Assertions {

    /** Do not instantiate */
    private Assertions() {
    }

    /** Assert a boolean condition, with formatted message. */
    public final static void check(boolean expr, String format, Object... args) {
        if (!expr) {
            throw new RuntimeException("Assertion failed: " + String.format(format, args));
        }
    }

    /** Assert a boolean condition, with simple string message. */
    public final static void check(boolean expr, String message) {
        if (!expr) {
            throw new RuntimeException("Assertion failed: " + message);
        }
    }

    /** Assert a boolean condition. No message. */
    public final static void check(boolean expr) {
        if (!expr) {
            throw new RuntimeException("Assertion failed");
        }
    }

    /** Assert a non null reference with formatted message and pass it through. */
    public final static <T> T checkNotNull(T object, String format, Object... args) {
        if (object == null) {
            throw new RuntimeException("Assertion failed: " + String.format(format, args));
        }
        return object;
    }

    /** Assert a non null reference with simple message and pass it through. */
    public final static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new RuntimeException("Assertion failed: " + message);
        }
        return object;
    }

    /** Assert a non null reference with no message and pass it through. */
    public final static <T> T checkNotNull(T object) {
        return checkNotNull(object, "");
    }
}

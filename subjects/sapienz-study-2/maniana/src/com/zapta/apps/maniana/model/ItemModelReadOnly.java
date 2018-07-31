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

package com.zapta.apps.maniana.model;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * Represents a read only aspect of an item model.
 * 
 * @author Tal Dayan.
 */
@ApplicationScope
public interface ItemModelReadOnly {

    /** Number of groups by which items are sorted. */
    public static final int SORTING_GROUPS = 4;
    
    /** System time in millis since epoch. */
    long getUpdateTime();
    
    String getId();

    String getText();

    boolean isCompleted();

    boolean isLocked();

    ItemColor getColor();

    /** Returns [0 .. SORTING_GROUPS). */
    int sortingGroupIndex();
}

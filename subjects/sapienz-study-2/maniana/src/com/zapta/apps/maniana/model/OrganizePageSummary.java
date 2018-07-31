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
 * Contains the results of a page cleanup operation.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class OrganizePageSummary {
    public int completedItemsFound;
    public int completedItemsDeleted;
    public boolean orderChanged;
    public int itemOfInterestNewIndex;

    public final void clear() {
        completedItemsFound = 0;
        completedItemsDeleted = 0;
        itemOfInterestNewIndex = -1;
        orderChanged = false;
    }

    public final boolean pageChanged() {
        return (completedItemsDeleted > 0) || orderChanged;
    }
}

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

import java.util.List;

import com.zapta.apps.maniana.model.ItemColor;

/** 
 * An immutable collection of colors that can be assigned to an item. 
 * 
 * @author Tal Dayan
 */
public class ItemColorsSet {

    // The list of colors as constructed by DecodeValue. Does not contain duplicate colors.
    // May be empty.
    private final ItemColor colors[];

    public ItemColorsSet(String encodedValue) {
        final List<ItemColor> colorList = ItemColorsPreference.DecodeValue(encodedValue);
        // NOTE: n is >= 1 per the logic in DecodeValue.
        final int n = colorList.size();
        colors = new ItemColor[n];
        for (int i = 0; i < n; i++) {
            colors[i] = colorList.get(i);
        }
    }

    public final ItemColor getDefaultColor() {
        return (colors.length > 0) ? colors[0] : ItemColor.NONE;
    }

    public final ItemColor colorAfter(ItemColor itemColor) {
        final int n = colors.length;
        for (int i = 0; i < n; i++) {
            if (colors[i] == itemColor) {
                return (i < (n - 1)) ? colors[i + 1] : colors[0];
            }
        }
        // Here when itemColor not found. We return the default color. This can happen
        // if the color has been disabled by the user, including the special case where 
        // colors is empty.
        return getDefaultColor();
    }
}

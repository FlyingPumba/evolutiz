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

import javax.annotation.Nullable;

import android.graphics.Color;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.EnumUtil;
import com.zapta.apps.maniana.util.EnumUtil.KeyedEnum;

/**
 * The colors set used to flag items.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum ItemColor implements KeyedEnum {
    // Item order determines the color sequence the user gets when tapping the screen.
    // It also defines an decreasing order of importance between the non NONE colors
    // for item merging purposes.
    NONE("none", Color.TRANSPARENT, R.string.item_color_none),
    RED("red", 0xffff0000, R.string.item_color_red),
    BLUE("blue", 0xff0077ff, R.string.item_color_blue),
    GREEN("green", 0xff00aa00, R.string.item_color_green),
    GOLD("gold", 0xffc8a82d, R.string.item_color_gold),   
    PURPLE("purple", 0xff9966ff, R.string.item_color_purple),   
    YELLOW("yellow", 0xffffff00, R.string.item_color_yellow),  
    CYAN("an", 0xff00c3d9, R.string.item_color_cyan), 
    WHITE("white", 0xffffffff, R.string.item_color_white),   
    BLACK("black", 0xff000000, R.string.item_color_black); 

    /** The key used for serialization. Not user visible. Should be consistent. */
    private final String mKey;

    /** The android argb color of this enum value. */
    private final int mColor;
    
    /** Resource id with user friendly name of this color. */
    public final int nameResourceId;

    private ItemColor(String key, int color, int nameResourceId) {
        mColor = color;
        mKey = key;
        this.nameResourceId = nameResourceId;
    }

    /** Return value with given key, fallback value if not found. */
    @Nullable
    public final static ItemColor fromKey(String key, @Nullable ItemColor fallBack) {
        return EnumUtil.fromKey(key, ItemColor.values(), fallBack);
    }

    @Override
    public final String getKey() {
        return mKey;
    }

    public final int getColor(int defaultColor) {
        return isNone() ? defaultColor : mColor;
    }

    public final boolean isNone() {
        return (this == NONE);
    }
    
    // TODO: add unit test
    /** Return the color with max importance. Used for merging items. */
    public final ItemColor max(ItemColor other) {
        // If one of the colors is NONE, return the other.
      if (this == NONE) {
          return other;
      }
      if (other == NONE) {
          return this;
      }
      
      // Here when nither is NONE. Return the one with min ordinal.
      final int index = Math.min(this.ordinal(), other.ordinal());
      return ItemColor.values()[index];
    }
}

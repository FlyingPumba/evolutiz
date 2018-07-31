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
 * Represents the data of a single item.
 * 
 * @author Tal Dayan.
 */
@ApplicationScope
public class ItemModel implements ItemModelReadOnly {
    
    /** Globally unique id for this item. Survives item mutations and sync. */
    private String mId;
    
    /** System time in millis at the time of creation or last mutation. */
    private long mUpdateTime;

    /** The item text. */
    private String mText;

    /** Is this item done? */
    private boolean mIsCompleted;

    /** Is this item blocked. */
    private boolean mIsLocked;

    /** The item color. */
    private ItemColor mColor;

    /** Constructor with initial values. */
    public ItemModel(long updateTime, String id, String text, boolean isCompleted, boolean isLocked, ItemColor color) {
        mUpdateTime = updateTime;
        mId = id;
        mText = text;
        mIsCompleted = isCompleted;
        mIsLocked = isLocked;
        mColor = color;
    }

    /** Copy constructor. Create an identical but independent instance */
    public ItemModel(ItemModelReadOnly other) {
        copyFrom(other);
    }

    /** Set to same values as other item. */
    public final void copyFrom(ItemModelReadOnly other) {
        mUpdateTime = other.getUpdateTime();
        mId = other.getId();
        mText = other.getText();
        mIsCompleted = other.isCompleted();
        mIsLocked = other.isLocked();
        mColor = other.getColor();
    }
    
    @Override
    public final long getUpdateTime() {
        return mUpdateTime;
    }
    
    @Override
    public final String getId() {
        return mId;
    }

    @Override
    public final String getText() {
        return mText;
    }

    public final void setText(String text) {
        mText = text;
    }

    @Override
    public final boolean isCompleted() {
        return mIsCompleted;
    }

    public final void setIsCompleted(boolean isCompleted) {
        mIsCompleted = isCompleted;
    }

    public final int sortingGroupIndex() {
        return mIsCompleted ? (mIsLocked ? 2 : 1) : (mIsLocked ? 3 : 0);
    }

    @Override
    public final boolean isLocked() {
        return mIsLocked;
    }

    public final void setIsLocked(boolean isLocked) {
        mIsLocked = isLocked;
    }

    @Override
    public final ItemColor getColor() {
        return mColor;
    }

    public final void setColor(ItemColor color) {
        mColor = color;
    }

    public final void mergePropertiesFrom(ItemModelReadOnly other) {      
        mIsCompleted = mIsCompleted && other.isCompleted();
        mIsLocked = mIsLocked && other.isLocked();        
        // TODO: should we clear the color if mIsCompleted?
        mColor = mColor.max(other.getColor());
    }
}

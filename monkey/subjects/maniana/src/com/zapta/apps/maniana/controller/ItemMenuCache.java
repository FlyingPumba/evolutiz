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

package com.zapta.apps.maniana.controller;

import javax.annotation.Nullable;

import android.content.Context;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.menus.ItemMenuEntry;

/**
 * Cached provider for item quick action menu items.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class ItemMenuCache {

    public static final int DISMISS_WITH_NO_SELECTION_ID = 0;
    public static final int DONE_ACTION_ID = 1;
    public static final int TODO_ACTION_ID = 2;
    public static final int EDIT_ACTION_ID = 3;
    public static final int LOCK_ACTION_ID = 4;
    public static final int UNLOCK_ACTION_ID = 5;
    public static final int DELETE_ACTION_ID = 6;

    private final MainActivityState mMainActivityState;
    
    private final Context mContext;

    @Nullable
    private ItemMenuEntry mCachedActionDone;

    @Nullable
    private ItemMenuEntry mCachedActionTodo;

    @Nullable
    private ItemMenuEntry mCachedActionEdit;

    @Nullable
    private ItemMenuEntry mCachedActionLock;

    @Nullable
    private ItemMenuEntry mCachedActionUnlock;

    @Nullable
    private ItemMenuEntry mCachedActionDelete;

    public ItemMenuCache(MainActivityState mainActivityState) {
        mMainActivityState = mainActivityState;
        mContext = mainActivityState.context();
    }

    public ItemMenuEntry getDoneAction() {
        if (mCachedActionDone == null) {
            mCachedActionDone = newItem(DONE_ACTION_ID,
                    mMainActivityState.str(R.string.item_menu_Done), R.drawable.item_menu_done);
        }
        return mCachedActionDone;
    }

    public ItemMenuEntry getToDoAction() {
        if (mCachedActionTodo == null) {
            mCachedActionTodo = newItem(TODO_ACTION_ID,
                    mMainActivityState.str(R.string.item_menu_To_Do), R.drawable.item_menu_todo);
        }
        return mCachedActionTodo;
    }

    public ItemMenuEntry getEditAction() {
        if (mCachedActionEdit == null) {
            mCachedActionEdit = newItem(EDIT_ACTION_ID,
                    mMainActivityState.str(R.string.item_menu_Edit), R.drawable.item_menu_edit);
        }
        return mCachedActionEdit;
    }

    public ItemMenuEntry getDeleteAction() {
        if (mCachedActionDelete == null) {
            mCachedActionDelete = newItem(DELETE_ACTION_ID,
                    mMainActivityState.str(R.string.item_menu_Delete), R.drawable.item_menu_delete);
        }
        return mCachedActionDelete;
    }

    public ItemMenuEntry getLockAction() {
        if (mCachedActionLock == null) {
            mCachedActionLock = newItem(LOCK_ACTION_ID,
                    mMainActivityState.str(R.string.item_menu_Lock), R.drawable.item_menu_lock);
        }
        return mCachedActionLock;
    }

    public ItemMenuEntry getUnlockAction() {
        if (mCachedActionUnlock == null) {
            mCachedActionUnlock = newItem(UNLOCK_ACTION_ID,
                    mMainActivityState.str(R.string.item_menu_Unlock), R.drawable.item_menu_unlock);
        }
        return mCachedActionUnlock;
    }

    private ItemMenuEntry newItem(int id, String label, int imageResourceId) {
        return new ItemMenuEntry(id, label, mContext.getResources()
                .getDrawable(imageResourceId));
    }
}

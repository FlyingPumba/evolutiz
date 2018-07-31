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

import static com.zapta.apps.maniana.util.Assertions.check;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.annotations.VisibleForTesting;

/**
 * Contains the data of a single page.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class PageModel {
    /** List of items in the order they are displayed to the user */
    private final List<ItemModel> mItems = new ArrayList<ItemModel>();

    /**
     * List of items to restore in case of undo operation. If empty, the page has no active undo
     * operation. Note that undo operatons are per page, not for the entire model.
     */
    private final List<ItemModel> mUndoItems = new ArrayList<ItemModel>();

    public PageModel() {
    }

    /** For testing only. */
    @VisibleForTesting
    List<ItemModel> getUndoItemsCloneForTesting() {
        return new ArrayList<ItemModel>(mUndoItems);
    }

    /** For testing only. */
    @VisibleForTesting
    void appendUndoItemForTesting(ItemModel item) {
        mUndoItems.add(item);
    }

    /** Get a list iterator over the items. Allows to deleted items while iterating. */
    public final ListIterator<ItemModel> listIterator() {
        return mItems.listIterator();
    }

    /** Clear all items and undo buffer */
    public final void clear() {
        mItems.clear();
        clearUndo();
    }

    /** Clear undo buffer. Does nothing if undo buffer is not active. */
    public final void clearUndo() {
        mUndoItems.clear();
    }

    /** Test if this page has an active undo operation. */
    public final boolean hasUndo() {
        return !mUndoItems.isEmpty();
    }

    /** Insert a new item at given index. */
    public final void insertItem(int itemIndex, ItemModel item) {
        mItems.add(itemIndex, item);
    }

    /**
     * Remove item at given index.
     * 
     * @return the removed item.
     */
    public final ItemModel removeItem(int itemIndex) {
        return mItems.remove(itemIndex);
    }

    /**
     * Get item at given index.
     */
    public final ItemModel getItem(int itemIndex) {
        return mItems.get(itemIndex);
    }

    /** Get number of items in this page. */
    public final int itemCount() {
        return mItems.size();
    }
    
    /** Get number of incomplete items in this page. */
    public final int pendingItemCount() {
        int count = 0;
        for (ItemModel item : mItems) {
          if (!item.isCompleted()) {
              count++;
          }
        }
        return count;
    }

    /**
     * Copy cloned items from other page. Undo buffer is not changed.
     */
    public final void copyItemsFrom(PageModel otherModel) {
        mItems.clear();
        for (ItemModel otherItem : otherModel.mItems) {
            mItems.add(new ItemModel(otherItem));
        }
    }

    /**
     * Perform and clear the active undo operation. The method asserts that an undo operation is
     * active.
     * 
     * @return the number of items resotred by the undo operation.
     */
    public final int performUndo() {
        check(hasUndo(), "Undo info not found");
        final int n = mUndoItems.size();

        // NOTE(tal): we add the items at the begining of the list, preserving their
        // relative order.
        mItems.addAll(0, mUndoItems);
        mUndoItems.clear();
        return n;
    }

    /**
     * Remove item at given index and setup a matching undo operation.
     * 
     * @return the deleted item.
     */
    public final void removeItemWithUndo(int itemIndex) {
        final ItemModel deletedItem = removeItem(itemIndex);
        mUndoItems.clear();
        mUndoItems.add(deletedItem);
    }

    /** Append item to end of undo list. Item should not be in any page item list. */
    public final void appendItemToUndo(ItemModel item) {
        mUndoItems.add(item);
    }

    /** Append an item at the end of the page */
    public void appendItem(ItemModel item) {
        mItems.add(item);
    }

    public final void restoreBackup(PageModel newPage) {
        // Move all existing items to the undo buffer
        mUndoItems.clear();
        mItems.clear();

        // Add copies of the items in the new page
        for (ItemModel item : newPage.mItems) {
            final ItemModel newItem = new ItemModel(item);
            mItems.add(newItem);
        }
    }

    /**
     * Peform a page organization operation.
     * 
     * @param deleteCompletedItems indicates if the operation should also delete all completed
     *        items.
     * @param itemOfInterestItem optional item index or -1 if not specified. If specified, this is
     *        the index of an item of interest. Upon return, the summary contains the location of
     *        the new index of the item of interest or -1 if the item was not specified or was
     *        deleted.
     * @param summary an object is set with the operation summary.
     */
    public final void organizePageWithUndo(boolean deleteCompletedItems, int itemOfInterestIndex,
            OrganizePageSummary summary) {
        summary.clear();

        @Nullable
        final ItemModel itemOfInterest = (itemOfInterestIndex == -1) ? null : mItems
                .get(itemOfInterestIndex);

        // We clear any old undo only if the current operation actually deletes items.
        boolean oldUndoCleared = false;

        // Count completed items. If deleteCompletedItems than also delete them
        // and add to undo buffer. Also detects if the items are out of sorting order.
        boolean isOutOfOrder = false;
        {
            ListIterator<ItemModel> iterator = mItems.listIterator();
            int maxItemGroupIndexSoFar = 0;
            while (iterator.hasNext()) {
                final ItemModel item = iterator.next();
                if (item.isCompleted()) {
                    summary.completedItemsFound++;
                    if (deleteCompletedItems) {
                        // Here to delete item.
                        if (!oldUndoCleared) {
                            mUndoItems.clear();
                            oldUndoCleared = true;
                        }
                        iterator.remove();
                        mUndoItems.add(item);
                        summary.completedItemsDeleted++;
                        // Do not continue to the item order checking below since this item
                        // was deleted from the list.
                        continue;
                    }
                }

                // Here when the item was left in the list
                final int itemGroupIndex = item.sortingGroupIndex();
                if (itemGroupIndex < maxItemGroupIndexSoFar) {
                    isOutOfOrder = true;
                } else {
                    maxItemGroupIndexSoFar = itemGroupIndex;
                }
            }
        }

        // If out of order, sort by groups, preserving order within each group.
        if (isOutOfOrder) {
            final ItemModel[] newOrder = new ItemModel[mItems.size()];
            int itemsCopied = 0;

            // Performing one pass per sorting group. On each pass, picking the items of that
            // group and adding to newOrder. This algorithm is optimized for minimal object
            // creation.
            for (int groupIndex = 0; groupIndex < ItemModelReadOnly.SORTING_GROUPS; groupIndex++) {
                for (int itemIndex = 0; itemIndex < mItems.size(); itemIndex++) {
                    final ItemModel item = mItems.get(itemIndex);
                    // Classify item by the group it belongs to.
                    final int itemGroupIndex = item.sortingGroupIndex();
                    if (itemGroupIndex == groupIndex) {
                        newOrder[itemsCopied] = item;
                        itemsCopied++;
                    }
                }
            }

            // Copy the newOrder array to mItems
            check(mItems.size() == itemsCopied);
            for (int i = 0; i < itemsCopied; i++) {
                mItems.set(i, newOrder[i]);
            }
        }

        // If requested, locate new location of item of interest.
        if (itemOfInterest != null) {
            final int itemOfInteresetNewIndex = mItems.indexOf(itemOfInterest);
            if (itemOfInteresetNewIndex >= 0) {
                summary.itemOfInterestNewIndex = itemOfInteresetNewIndex;
            }
        }

        summary.orderChanged = isOutOfOrder;
    }

    public final boolean isPageSorted() {
        int maxGroupIndexSoFar = 0;
        for (ItemModel item : mItems) {
            int itemGroupIndex = item.sortingGroupIndex();
            if (itemGroupIndex < maxGroupIndexSoFar) {
                return false;
            }
            maxGroupIndexSoFar = itemGroupIndex;
        }
        return true;
    }
}

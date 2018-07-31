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

import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.annotations.VisibleForTesting;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Contains the app data. Persisted across app activations. Controlled by the app controller.
 * Observed by the viewer via the ItemListViewAdapter.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class AppModel {

    /** Selected to not match any valid timestamp. */
    private static final String DEFAULT_DATE_STAMP = "";

    /** Model of Today page. */
    private final PageModel mTodayPageModel;

    /** Model of Tomorrow page. */
    private final PageModel mTomorrowPageMode;

    /** True if current state is not persisted */
    private boolean mIsDirty = true;

    /**
     * Last date in which items were pushed from Tomorrow to Today pages. Used to determine when
     * next push should be done. Using an empty string to indicate no datestamp.
     * 
     * NOTE(tal): for now the format of this timestamp is opaque though it must be consistent.
     */
    private String mLastPushDateStamp;

    public AppModel() {
        this.mTodayPageModel = new PageModel();
        this.mTomorrowPageMode = new PageModel();
        this.mLastPushDateStamp = DEFAULT_DATE_STAMP;
    }

    public final boolean isDirty() {
        return mIsDirty;
    }

    public final void setDirty() {
        if (!mIsDirty) {
            LogUtil.info("Model became dirty");
            mIsDirty = true;
        }
    }

    public final void setClean() {
        if (mIsDirty) {
            LogUtil.info("Model became clean");
            mIsDirty = false;
        }
    }

    /** Get the model of given page. */
    @VisibleForTesting
    final PageModel getPageModel(PageKind pageKind) {
        return pageKind.isToday() ? mTodayPageModel : mTomorrowPageMode;
    }

    /** Get read only aspect of the item of given index in given page. */
    public final ItemModelReadOnly getItemReadOnly(PageKind pageKind, int itemIndex) {
        return getPageModel(pageKind).getItem(itemIndex);
    }

    /** Get a mutable item of given page and index. */
    // TODO: replace with a setItem(,,,) method. Safer this way.
    public final ItemModel getItemForMutation(PageKind pageKind, int itemIndex) {
        setDirty();
        return getPageModel(pageKind).getItem(itemIndex);
    }

    /** Get number of items in given page. */
    public final int getPageItemCount(PageKind pageKind) {
        return getPageModel(pageKind).itemCount();
    }

    /** Get number of incomplete items in given page. */
    public final int getPagePendingItemCount(PageKind pageKind) {
        return getPageModel(pageKind).pendingItemCount();
    }

    /** Get total number of items. */
    public final int getItemCount() {
        return mTodayPageModel.itemCount() + mTomorrowPageMode.itemCount();
    }

    /** Clear the model. */
    public final void clear() {
        mTodayPageModel.clear();
        mTomorrowPageMode.clear();
        mLastPushDateStamp = DEFAULT_DATE_STAMP;
        setDirty();
    }

    /** Clear undo buffers of both pages. */
    public final void clearAllUndo() {
        mTodayPageModel.clearUndo();
        mTomorrowPageMode.clearUndo();

        // NOTE(tal): does not affect dirty flag.
    }

    /** Clear undo buffer of given page. */
    public final void clearPageUndo(PageKind pageKind) {
        getPageModel(pageKind).clearUndo();

        // NOTE(tal): does not affect dirty flag.
    }

    /** Test if given page has an active undo buffer. */
    public final boolean pageHasUndo(PageKind pageKind) {
        return getPageModel(pageKind).hasUndo();
    }

    /** Test if the page items are already sorted. */
    public final boolean isPageSorted(PageKind pageKind) {
        return getPageModel(pageKind).isPageSorted();
    }

    /** Insert item to given page at given item index. */
    public final void insertItem(PageKind pageKind, int itemIndex, ItemModel item) {
        setDirty();
        getPageModel(pageKind).insertItem(itemIndex, item);
    }

    /** Add an item to the end of given page. */
    public void appendItem(PageKind pageKind, ItemModel item) {
        getPageModel(pageKind).appendItem(item);
        setDirty();
    }

    /** Remove item of given index from given page. */
    public final ItemModel removeItem(PageKind pageKind, int itemIndex) {
        setDirty();
        ItemModel result = getPageModel(pageKind).removeItem(itemIndex);
        return result;
    }

    /** Remove item of given idnex from given page and set a corresponding undo at that page. */
    public final void removeItemWithUndo(PageKind pageKind, int itemIndex) {
        setDirty();
        getPageModel(pageKind).removeItemWithUndo(itemIndex);
    }

    public final void restoreBackup(AppModel newModel) {
        setDirty();
        mTodayPageModel.restoreBackup(newModel.mTodayPageModel);
        mTomorrowPageMode.restoreBackup(newModel.mTomorrowPageMode);
    }

    /**
     * Organize the given page with undo. See details at
     * {@link PageModel#organizePageWithUndo(boolean, PageOrganizeResult)()}.
     */
    public final void organizePageWithUndo(PageKind pageKind, boolean deleteCompletedItems,
            int itemOfInteresetIndex, OrganizePageSummary summary) {
        getPageModel(pageKind).organizePageWithUndo(deleteCompletedItems, itemOfInteresetIndex,
                summary);
        if (summary.pageChanged()) {
            setDirty();
        }
    }

    /**
     * Apply active undo operation of given page. The method asserts that the page has an active
     * undo.
     * 
     * @return the number of items resotred by the undo operation.
     */
    public final int applyUndo(PageKind pageKind) {
        final int result = getPageModel(pageKind).performUndo();
        setDirty();
        return result;
    }

    /**
     * Copy cloned items from other model. All existing items are deleted. Dirty is set. Undo buffer
     * and other model properties are not changed.
     */
    public final void copyItemsFrom(AppModel otherModel) {
        setDirty();
        mTodayPageModel.copyItemsFrom(otherModel.mTodayPageModel);
        mTomorrowPageMode.copyItemsFrom(otherModel.mTomorrowPageMode);
    }

    /**
     * Move non locked items from Tomorow to Today. It's the caller responsibility to also set the
     * last push datestamp. This method clears any previous undo buffer content of both pages.
     * 
     * @param expireAllLocks if true, locked items are also pushed, after changing their status to
     *        unlocked.
     * 
     * @param deleteCompletedItems if true, delete completed items, leaving them in the undo buffers
     *        of their respective pages.
     */
    public final void pushToToday(boolean expireAllLocks, boolean deleteCompletedItems) {
        clearAllUndo();
        setDirty();

        // Process Tomorrow items
        {
            int itemsMoved = 0;
            final ListIterator<ItemModel> iterator = mTomorrowPageMode.listIterator();
            while (iterator.hasNext()) {
                final ItemModel item = iterator.next();
                // Expire lock if needed
                if (expireAllLocks && item.isLocked()) {
                    item.setIsLocked(false);
                }

                // If delete completed and item is completed (even if blocked), move it to undo
                // buffer.
                if (deleteCompletedItems && item.isCompleted()) {
                    iterator.remove();
                    mTomorrowPageMode.appendItemToUndo(item);
                    continue;
                }

                // If item is not unlocked, move Today page.
                if (!item.isLocked()) {
                    // We move the items to the beginning of Today page, preserving there
                    // relative order from Tomorrow page.
                    iterator.remove();
                    mTodayPageModel.insertItem(itemsMoved, item);
                    itemsMoved++;
                    continue;
                }

                // Otherwise leave item in place.
            }
        }

        // If need to delete completed items, scan also Today list and move
        // completed items to the Today's undo buffer.
        if (deleteCompletedItems) {
            final ListIterator<ItemModel> iterator = mTodayPageModel.listIterator();
            while (iterator.hasNext()) {
                final ItemModel item = iterator.next();
                if (item.isCompleted()) {
                    iterator.remove();
                    mTodayPageModel.appendItemToUndo(item);
                }
            }
        }
    }

    /** Get the datestamp of last item push. */
    public final String getLastPushDateStamp() {
        return mLastPushDateStamp;
    }

    /** Set the last item push datestamp. */
    public final void setLastPushDateStamp(String lastPushDateStamp) {
        // TODO: no need to set the dirty bit, right?
        this.mLastPushDateStamp = lastPushDateStamp;
    }

    public static class ItemReference {
        PageKind pageKind;
        int itemIndex;
        ItemModelReadOnly itemModel;

        public ItemReference(PageKind pageKind, int itemIndex, ItemModelReadOnly itemModel) {
            this.pageKind = pageKind;
            this.itemIndex = itemIndex;
            this.itemModel = itemModel;
        }
    }

    /**
     * Merge the items of the other model into this one. The other model is not modified. This
     * operation is not symmetric (A.mergeFrom(B) != B.mergeFrom(A). Does not do item sorting.
     * Caller need to invoke sorting if needed.
     */
    public final void mergeFrom(AppModel otherModel) {
        setDirty();
        clearAllUndo();

        Map<String, ItemReference> otherItems = new HashMap<String, ItemReference>();

        // Collect 'other' items
        for (PageKind pageKind : PageKind.values()) {
            final PageModel pageModel = otherModel.getPageModel(pageKind);
            for (int i = 0; i < pageModel.itemCount(); i++) {
                final ItemModelReadOnly otherItem = pageModel.getItem(i);
                ItemReference existingOtherItemRef = otherItems.get(otherItem.getText());
                if (existingOtherItemRef != null) {
                    // Other model has multiple items with this text. Keep merging the
                    // properties, keeping only a single copy.
                    final ItemModel replacementItem = new ItemModel(existingOtherItemRef.itemModel);
                    replacementItem.mergePropertiesFrom(otherItem);
                    existingOtherItemRef.itemModel = replacementItem;
                } else {
                    otherItems.put(otherItem.getText(), new ItemReference(pageKind, i, otherItem));
                }
            }
        }

        // Strike out 'other' items already in this.
        for (PageKind pageKind : PageKind.values()) {
            final PageModel pageModel = getPageModel(pageKind);
            for (int i = 0; i < pageModel.itemCount(); i++) {
                final ItemModel item = pageModel.getItem(i);
                ItemReference otherItemRef = otherItems.get(item.getText());
                if (otherItemRef != null) {
                    // This model has the same item as the other, remove the other
                    // so we don't insert it and merge its properties into this one.
                    //
                    // NOTE: of this model has multiple copies of this item, only the first
                    // one will have its properties merged since we remove the other from
                    // the map. Alternatively we could keep it there marked as 'do-not-insert'
                    // and merging the properties with all the copies in this model. It is
                    // not clear what will be more intuitive.
                    //
                    item.mergePropertiesFrom(otherItemRef.itemModel);
                    otherItems.remove(item.getText());
                }
            }
        }

        // TODO: sort to preserve other items order and be deterministic

        for (ItemReference otherRef : otherItems.values()) {
            final ItemModelReadOnly otherItem = otherRef.itemModel;
            final ItemModel newItem = new ItemModel(otherItem);
            // Today page cannot have locked items
            if (newItem.isLocked()) {
              newItem.setIsLocked(false);
            }
            mTodayPageModel.insertItem(0, newItem);
        }
    }

    // TODO: move to somewhere else?
    public static class ProjectedImportStats {
        public int mergeDelete = 0;
        public int mergeKeep = 0;
        public int mergeAdd = 0;
        public int replaceDelete = 0;
        public int replaceKeep = 0;
        public int replaceAdd = 0;

        public final void clear() {
            mergeDelete = 0;
            mergeKeep = 0;
            mergeAdd = 0;
            replaceDelete = 0;
            replaceKeep = 0;
            replaceAdd = 0;
        }
    }

    public final ProjectedImportStats projectedImportStats(AppModel otherModel) {
        ProjectedImportStats result = new ProjectedImportStats();

        Map<String, Integer> thisMultiset = itemTextMultiset();
        Map<String, Integer> otherMultiset = otherModel.itemTextMultiset();

        // Compute merge stats
        // NOTE: mergeDelete is always zero for merge oepration.
        for (Integer count : thisMultiset.values()) {
            result.mergeKeep += count;
        }
        for (String text : otherMultiset.keySet()) {
            if (!thisMultiset.containsKey(text)) {
                // NOTE: we add excactly one, even if the other model contains
                // multiple items with this text. The merge operation will collaps them.
                result.mergeAdd++;
            }
        }

        // Compute replace stats
        Set<String> textSet = new HashSet<String>();
        textSet.addAll(thisMultiset.keySet());
        textSet.addAll(otherMultiset.keySet());
        for (String text : textSet) {
            final Integer thisValue = thisMultiset.get(text);
            final Integer otherValue = otherMultiset.get(text);

            final int thisCount = (thisValue == null) ? 0 : thisValue;
            final int otherCount = (otherValue == null) ? 0 : otherValue;

            if (thisCount < otherCount) {
                result.replaceKeep += thisCount;
                result.replaceAdd += (otherCount - thisCount);
            } else {
                result.replaceKeep += otherCount;
                result.replaceDelete += (thisCount - otherCount);
            }
        }

        return result;
    }

    /** Return a multiset of the text string of all items. */
    private final Map<String, Integer> itemTextMultiset() {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (PageKind pageKind : PageKind.values()) {
            final PageModel pageModel = getPageModel(pageKind);
            for (int i = 0; i < pageModel.itemCount(); i++) {
                final ItemModelReadOnly item = pageModel.getItem(i);
                final Integer previousCount = result.get(item.getText());
                result.put(item.getText(), (previousCount == null) ? 1 : previousCount + 1);
            }
        }
        return result;
    }
}

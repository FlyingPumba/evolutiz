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

package com.zapta.apps.maniana.view;

import javax.annotation.Nullable;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.settings.ItemFontVariation;
import com.zapta.apps.maniana.settings.PageIconSet;
import com.zapta.apps.maniana.util.TextUtil;

/**
 * View of a single item.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class ItemView extends FrameLayout {

    private final MainActivityState mMainActivityState;

    /** The sub view that contains the color flag. */
    private final View mColorView;

    /** The sub view that contains the text. */
    private final ExtendedTextView mExtendedTextView;

    /** The sub view that contains the button (arrow, lock, etc). */
    private final ImageView mArrowView;

    /** The kind of the page that contains this item. */
    private final PageKind mPageKind;

    /** Cache of last user font variation. Used to avoid unnecessary updates. */
    @Nullable
    private ItemFontVariation mLastFontVariaton = null;

    /** Cache of item is completed status. Used to avoid unnecessary changes. */
    private boolean mLastIsCompleted = false;

    /** Cache of the view highlighted status. Used to avoid unnecessary changes. */
    private boolean mLastIsHighlighted = false;

    public ItemView(MainActivityState mainActivityState, PageKind pageKind, ItemModelReadOnly item) {
        super(mainActivityState.context());
        this.mMainActivityState = mainActivityState;
        this.mPageKind = pageKind;

        mMainActivityState.services().layoutInflater().inflate(R.layout.page_item_layout, this);

        mColorView = findViewById(R.id.page_item_color);

        mExtendedTextView = (ExtendedTextView) findViewById(R.id.page_item_text);

        mArrowView = (ImageView) findViewById(R.id.page_item_arrow);

        updateFromItemModel(item);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public void startItemAnimation(final int itemIndex,
            final AppView.ItemAnimationType animationType, int initialDelayMillis,
            final Runnable callback) {

        final Animation animation;

        switch (animationType) {
            case DELETING_ITEM: {
                animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_delete);
                break;
            }
            case MOVING_ITEM_TO_OTHER_PAGE: {
                final int animationId = mPageKind.isToday() ? R.anim.item_move_right
                        : R.anim.item_move_left;
                animation = AnimationUtils.loadAnimation(getContext(), animationId);
                break;
            }
            case SORTING_ITEM: {
                animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_shake);
                break;
            }
            default:
                throw new RuntimeException("Unknown animation type");
        }

        animation.setStartOffset(initialDelayMillis);

        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                callback.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        View itemAninationView = findViewById(R.id.page_item_anim);

        itemAninationView.startAnimation(animation);
    }

    public final void updateFromItemModel(ItemModelReadOnly item) {
        updateItemButton(item.isLocked());
        mExtendedTextView.setText(item.getText());
        TextUtil.ICS_HACK_TEXT_VIEW(mExtendedTextView);
        mColorView.setBackgroundColor(item.getColor().getColor(0x00000000));
        updateFont(item.isCompleted());
    }

    private final void updateFont(boolean isItemCompleted) {
        final ItemFontVariation newItemFontVariation = mMainActivityState.prefTracker().getPageItemFontVariation();
        if (newItemFontVariation != mLastFontVariaton || isItemCompleted || mLastIsCompleted) {
            newItemFontVariation.apply(mExtendedTextView, isItemCompleted, true);
            
            // For debugging. Highlight each item's text area.
            // {
            //   final int bgColor = 0x33000000 | RandomUtil.random.nextInt(0x1000000);
            //   mExtendedTextView.setBackgroundColor(bgColor);   
            // }
            
            mLastFontVariaton = newItemFontVariation;
            mLastIsCompleted = isItemCompleted;
        }
    }

    /** Update the item button (arrow vs lock). */
    private final void updateItemButton(boolean isLocked) {
        final PageIconSet iconSet = mMainActivityState.prefTracker().getPageIconSetPreference();
        final int newArrowDrawable;
        if (mPageKind.isToday()) {
            newArrowDrawable = isLocked ? iconSet.arrowLockedResourceId
                    : iconSet.arrowRightResourceId;
        } else {
            newArrowDrawable = isLocked ? iconSet.arrowLockedResourceId
                    : iconSet.arrowLeftResourceId;
        }
        // TODO: consider to cache last value and update only on actual change
        // (motivation is performance, is is significant?).
        mArrowView.setImageResource(newArrowDrawable);
    }

    /** Highlight this view using given background */
    public final void setHighlight(int drawableBackground) {
        if (mLastIsHighlighted) {
            return;
        }

        setBackgroundResource(drawableBackground);
        mLastIsHighlighted = true;
    }

    /** Turn off highlight */
    public final void clearHighlight() {
        if (!mLastIsHighlighted) {
            return;
        }
        setBackgroundResource(0);
        setBackgroundColor(Color.TRANSPARENT);
        mLastIsHighlighted = false;
    }
}

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

package com.zapta.apps.maniana.menus;

import static com.zapta.apps.maniana.util.Assertions.check;
import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;

/**
 * Item action popup menu.
 * 
 * @author Tal Dayan (adapted to Maniana) Based on example by Lorensius W. L. T
 *         <lorenz@londatiga.net>.
 */
@MainActivityScope
public class ItemMenu implements OnDismissListener, TrackablePopup {
    
    public interface OnActionItemOutcomeListener {
        /** Action item is null if dismissed with no selection. */
        void onOutcome(ItemMenu source, @Nullable ItemMenuEntry actionItem);
    }

    private final MainActivityState mMainActivityState;

    /** The window that contains the menu's top view. */
    private final PopupWindow mMenuWindow;

    private View mTopView;

    private ImageView mUpArrowView;
    private ImageView mDownArrowView;

    private ViewGroup mItemContainerView;

    private final OnActionItemOutcomeListener mOutcomeListener;

    private final List<ItemMenuEntry> actionItems = new ArrayList<ItemMenuEntry>();

    private boolean mActioWasSelected;

    /**
     * Constructor allowing orientation override
     * 
     * @param mContext Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public ItemMenu(MainActivityState mainActivityState, OnActionItemOutcomeListener outcomeListener) {
        mMainActivityState = mainActivityState;
        mMenuWindow = new PopupWindow(mainActivityState.context());

        mMenuWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mMenuWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

        mOutcomeListener = checkNotNull(outcomeListener);

        mTopView = (ViewGroup) mMainActivityState.services().layoutInflater()
                .inflate(R.layout.item_menu, null);

        mItemContainerView = (ViewGroup) mTopView.findViewById(R.id.items_container);

        mDownArrowView = (ImageView) mTopView.findViewById(R.id.arrow_down);
        mUpArrowView = (ImageView) mTopView.findViewById(R.id.arrow_up);

        mTopView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        mMenuWindow.setContentView(mTopView);
        mMenuWindow.setOnDismissListener(this);
    }
    
    /**
     * Get action item at given index
     */
    public final ItemMenuEntry getActionItem(int index) {
        return actionItems.get(index);
    }

    /**
     * Add an action item to the end of the list.
     */
    public final void addActionItem(final ItemMenuEntry actionItem) {
        actionItems.add(actionItem);

        // TODO: rename this to action_wrapper here and in the layout.
        final View wrapperView = mMainActivityState.services().layoutInflater()
                .inflate(R.layout.item_menu_entry, null);

        final ImageView imageView = (ImageView) wrapperView
                .findViewById(R.id.item_menu_entry_icon);
        imageView.setImageDrawable(actionItem.getIcon());

        final TextView textView = (TextView) wrapperView.findViewById(R.id.item_menu_entry_text);
        textView.setText(actionItem.getLabel());

        // Set a listener to track touches and highlight pressed items.
        wrapperView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    wrapperView.setBackgroundResource(R.drawable.popup_menu_entry_selected);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                        || event.getAction() == MotionEvent.ACTION_UP || !wrapperView.isPressed()) {
                    wrapperView.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });

        wrapperView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOutcomeListener.onOutcome(ItemMenu.this, actionItem);
                mActioWasSelected = true;
                mMenuWindow.dismiss();
            }
        });

        wrapperView.setFocusable(true);
        wrapperView.setClickable(true);

        // If not first, add seperator before it.
        if (mItemContainerView.getChildCount() > 0) {
            final View separator = mMainActivityState.services().layoutInflater()
                    .inflate(R.layout.item_menu_separator, null);
            // TODO: move this configuration to the XML
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
            separator.setLayoutParams(params);
            separator.setPadding(5, 0, 5, 0);

            mItemContainerView.addView(separator);
        }

        mItemContainerView.addView(wrapperView);
    }

    /**
     * Show the popup action menu over a given anchor view.
     * 
     * @param anchorView a view to which the action menu's arrow will point it.
     */
    public final void show(View anchorView) {

        if (mTopView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }

        // Set transparent window background. This will clear the horizontal strips above and below
        // the menu defined by the two arrows.
        mMenuWindow.setBackgroundDrawable(new BitmapDrawable());

        mMenuWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mMenuWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mMenuWindow.setTouchable(true);
        mMenuWindow.setFocusable(true);
        mMenuWindow.setOutsideTouchable(true);

        mMenuWindow.setContentView(mTopView);

        mActioWasSelected = false;

        final int[] anchorXYOnsScreen = new int[2];

        anchorView.getLocationOnScreen(anchorXYOnsScreen);

        final Rect anchorRectOnScreen = new Rect(anchorXYOnsScreen[0], anchorXYOnsScreen[1],
                anchorXYOnsScreen[0] + anchorView.getWidth(), anchorXYOnsScreen[1]
                        + anchorView.getHeight());

        mTopView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final int rootHeight = mTopView.getMeasuredHeight();
        final int screenHeight = mMainActivityState.services().windowManager().getDefaultDisplay().getHeight();

        // Arrow position is slightly to the right of the left upper/lower cornet.
        // TODO: define const.
        // TODO: scale by density?
        final int xPosPixels = 50;

        final int arrowPos = anchorRectOnScreen.left + xPosPixels;

        int spaceAbove = anchorRectOnScreen.top;
        int spaceBelow = screenHeight - anchorRectOnScreen.bottom;

        final boolean showAbove = spaceAbove >= rootHeight;

        // TODO: make a const or param.
        // TODO: scale by density?
        final int ARROW_VERTICAL_OVERLAP = 15;

        final int yPosPixels;

        if (showAbove) {
            check(rootHeight <= spaceAbove);
            yPosPixels = anchorRectOnScreen.top - rootHeight + ARROW_VERTICAL_OVERLAP;
        } else {
            yPosPixels = anchorRectOnScreen.bottom - ARROW_VERTICAL_OVERLAP;
            if (rootHeight > spaceBelow) {
                mItemContainerView.getLayoutParams().height = spaceBelow;
            }
        }

        showArrow(((showAbove) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
        mMenuWindow.setAnimationStyle((showAbove) ? R.style.Animations_ItemMenuAbove
                : R.style.Animations_ItemMenuBelow);
        mMenuWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPosPixels, yPosPixels);
    }

    /**
     * Show arrow
     * 
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private final void showArrow(int whichArrow, int requestedX) {
        // Decide which of the two up and down arrows will be shown and hidden respectivly.
        final View showArrow = (whichArrow == R.id.arrow_up) ? mUpArrowView : mDownArrowView;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mDownArrowView : mUpArrowView;

        final int arrowWidth = mUpArrowView.getMeasuredWidth();
        showArrow.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow
                .getLayoutParams();
        param.leftMargin = requestedX - arrowWidth / 2;
        hideArrow.setVisibility(View.INVISIBLE);
    }

    @Override
    public final void onDismiss() {
        if (!mActioWasSelected) {
            mOutcomeListener.onOutcome(this, null);
        }
    }

    @Override
    public final void closeLeftOver() {
        // NOTE: we don't bother here to early report the dismissal, as we do with the item editor,
        // because the dismissal of this menu does not cause a mutation of the model.
        if (mMenuWindow.isShowing()) {
            mMenuWindow.dismiss();
        }
    }
}

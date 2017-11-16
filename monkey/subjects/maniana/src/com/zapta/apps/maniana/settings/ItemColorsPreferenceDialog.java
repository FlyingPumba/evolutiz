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

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.util.BitmapUtil;
import com.zapta.apps.maniana.util.DisplayUtil;

/**
 * Dialog for selecting and ordering the tasks colors. Used by ItemColorsPreference.
 * 
 * @author Tal Dayan
 */
public class ItemColorsPreferenceDialog {

    /** Represents a single item in the task colors list. */
    private static class ListItem {
        private final ItemColor itemColor;
        private boolean isEnabled;

        private ListItem(ItemColor itemColor, boolean isEnabled) {
            this.itemColor = itemColor;
            this.isEnabled = isEnabled;
        }
    }

    public static interface ItemColorsChangeListener {
        void onTasksColorsSetChange(List<ItemColor> enabledItemColors);
    }

    /** Adapter for the tasks color lists. Contains the underlying list data. */
    private static class ColorAdapter extends BaseAdapter implements DropListener {
        private final Context mContext;

        final float mDensity;

        private final ArrayList<ListItem> listItems = new ArrayList<ListItem>();

        /**
         * EnabledItemColors is assumed to contains no duplicates. May be empty. Order represents
         * colors order.
         */
        public ColorAdapter(Context context, List<ItemColor> enabledItemColors) {
            super();
            this.mContext = context;
            this.mDensity = DisplayUtil.getDensity(context);

            boolean itemsAdded[] = new boolean[ItemColor.values().length];

            // Add the enabled ones. Avoid duplicates.
            for (ItemColor itemColor : enabledItemColors) {
                // NOTE: we assume no duplicates
                itemsAdded[itemColor.ordinal()] = true;
                listItems.add(new ListItem(itemColor, true));
            }

            // Add the rest as disabled.
            for (ItemColor itemColor : ItemColor.values()) {
                if (!itemsAdded[itemColor.ordinal()]) {
                    listItems.add(new ListItem(itemColor, false));
                }
            }
        }

        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            // We use the position as ID
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;

            // If not recycling an existing item view, create a new one.
            if (view == null) {
                final LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item_colors_item_layout, parent, false);
            }

            final ListItem listItem = listItems.get(position);

            // Set color
            final ImageView imageView = (ImageView) view.findViewById(R.id.item_colors_item_image);
            imageView.setImageBitmap(BitmapUtil.getPreferenceColorPreviewBitmap(0xff888888,
                    listItem.isEnabled, mDensity));
            imageView.setImageBitmap(BitmapUtil.getPreferenceColorPreviewBitmap(
                    listItem.itemColor.getColor(0xff666666), listItem.isEnabled, mDensity));

            // Set text
            final TextView textView = (TextView) view.findViewById(R.id.item_colors_item_text);
            textView.setText(mContext.getString(listItem.itemColor.nameResourceId));

            // Set checkbox
            final CheckBox checkboxView = (CheckBox) view
                    .findViewById(R.id.item_colors_item_checkbox);
            // Disable the old listener so we don't callback the handle when setting a reused view.
            checkboxView.setOnCheckedChangeListener(null);
            checkboxView.setChecked(listItem.isEnabled);
            checkboxView.setClickable(false);

            final LinearLayout clickArea = (LinearLayout) view
                    .findViewById(R.id.item_colors_item_clickable_area);
            clickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleColorClick(checkboxView, position);
                }
            });

            return view;
        }

        private void handleColorClick(CheckBox checkboxView, int position) {
            final ListItem listItem = listItems.get(position);
            final boolean newState = !checkboxView.isChecked();
            checkboxView.setChecked(newState);
            listItem.isEnabled = newState;

            // Refresh the display to propagate color change due to check change.
            notifyDataSetChanged();
        }

        /** From DragListener interface. Called when the user completes a drag */
        @Override
        public void drop(int from, int to) {
            // NOTE: we don't filter out the special case of from == to, since it
            // is handled gracefully.

            final ListItem listItem = listItems.remove(from);
            listItems.add(to, listItem);
            notifyDataSetChanged();
        }
    }

    private static class MyDSController extends DragSortController {
        DragSortListView mDslv;
        ColorAdapter adapter;

        public MyDSController(DragSortListView dslv, ColorAdapter adapter) {
            super(dslv);
            this.mDslv = dslv;
            this.adapter = adapter;
            // View id in the item layout that is used as a drag handle.
            setDragHandleId(R.id.item_colors_item_drag_handle);
        }

        @Override
        public View onCreateFloatView(int position) {
            checkNotNull(adapter);
            View v = adapter.getView(position, null, mDslv);
            checkNotNull(v);
            checkNotNull(v.getBackground());
            v.getBackground().setLevel(10000);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
            // do nothing; blocks super from crashing
        }
    }

    public static AlertDialog CreateDialog(Context context, List<ItemColor> enabledColors,
            final ItemColorsChangeListener listener) {
        checkNotNull(listener);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = inflater.inflate(R.layout.item_colors_layout, null);

        final DragSortListView dslv = (DragSortListView) layout
                .findViewById(R.id.item_colors_list_view);

        final ColorAdapter adapter = new ColorAdapter(context, enabledColors);

        dslv.setAdapter(adapter);

        final MyDSController dslvController = new MyDSController(dslv, adapter);
        dslv.setFloatViewManager(dslvController);
        dslv.setOnTouchListener(dslvController);
        dslv.setDragEnabled(true);

        dslv.setDropListener(adapter);

        builder.setView(layout);
        builder.setTitle(R.string.settings_select_task_colors_dialog_title);

        builder.setNegativeButton("Cancel", null);

        builder.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                List<ItemColor> enabledItemColors = new ArrayList<ItemColor>();
                for (ListItem item : adapter.listItems) {
                    if (item.isEnabled) {
                        enabledItemColors.add(item.itemColor);
                    }
                }
                listener.onTasksColorsSetChange(enabledItemColors);
            }
        });

        return builder.create();
    }
}

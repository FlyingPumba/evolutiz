/*
 * Copyright (C) 2012 Rahul Agarwal
 *
 * This file is part of the World Clock
 * World Clock is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * World Clock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with World Clock.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.irahul.worldclock;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * List adapter for the selection dialog
 * 
 * Based on original source code - take a look at it otherwise won't make sense
 * https://github.com/android/platform_frameworks_base/blob/master/core/java/android/widget/ArrayAdapter.java#L449
 * 
 * Also guidance from: http://software-workshop.eu/content/android-development-creating-custom-filter-listview
 * @author rahul
 *
 */
public class TimeZoneEditDialogListAdapter extends ArrayAdapter<WorldClockTimeZone> {
	private List<WorldClockTimeZone> originalDataValues;
	private List<WorldClockTimeZone> filteredDataValues;
	private Filter filter = null;
	
	public TimeZoneEditDialogListAdapter(Context context, List<WorldClockTimeZone> tzValues) {
		super(context, R.layout.timezone_edit_dialog_list, R.id.dialog_list_display_line1, tzValues);

		//original values
		this.originalDataValues = new ArrayList<WorldClockTimeZone>(tzValues);
		
		//filtered values - this is what is used in display
		this.filteredDataValues = new ArrayList<WorldClockTimeZone>(tzValues);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.timezone_edit_dialog_list_item,
					null);
		}

		WorldClockTimeZone tz = getItem(position);//from underlying mObjects array		

		// display label
		TextView displayLabel = (TextView) convertView.findViewById(R.id.dialog_list_display_line1);
		displayLabel.setText(tz.getId()+" "+tz.getRawOffsetDisplay());

		// offset
		TextView displayOffset = (TextView) convertView.findViewById(R.id.dialog_list_display_line2);
		displayOffset.setText(tz.getDisplayName());

		// image icon
		Resources res = getContext().getResources();
		ImageView displayIcon = (ImageView) convertView.findViewById(R.id.dialog_list_icon);
		//TODO - check if performance is good. Only visible list is rendered so maybe ok
		displayIcon.setImageResource(res.getIdentifier(tz.getFlagResourceName(), "drawable", getContext().getPackageName()));

		return convertView;
	}

	@Override
	public Filter getFilter() {
		if(filter==null){
			filter=new TimeZoneFilter();
		}
		return filter;
	}

	/**
	 * Implement filtering on list for searchText
	 * @author rahul
	 *
	 */
	private class TimeZoneFilter extends Filter {
		/**
		 * Based on the searchText provided the adapter's data is updated
		 * Using {@link WorldClockTimeZone#getSearchString()} that 'contains' the searchText
		 */
		@Override
		protected FilterResults performFiltering(CharSequence searchText) {
			FilterResults results = new FilterResults();
			
			if (searchText == null || searchText.length() == 0) {
				//no search text provided
				List<WorldClockTimeZone> list = new ArrayList<WorldClockTimeZone>(originalDataValues);	                
	            results.values = list;
	            results.count = list.size();				
			} else {
				String searchStringLower = searchText.toString().toLowerCase();
				List<WorldClockTimeZone> fullSearchList = new ArrayList<WorldClockTimeZone>(originalDataValues);				
				final List<WorldClockTimeZone> newValues = new ArrayList<WorldClockTimeZone>();

				for (WorldClockTimeZone tz:fullSearchList) {			
					if (tz.getSearchString().contains(searchStringLower)) {
						newValues.add(tz);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		/**
		 * Called with the filtered results. These must then be updated in the adapter and notified for change
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredDataValues = (List<WorldClockTimeZone>) results.values;
						
            clear();//clear underlying mObjects array
            for(int i = 0; i < filteredDataValues.size(); i++){
            	//add to underlying mObjects array
                add(filteredDataValues.get(i));
            }
            notifyDataSetChanged();                       
		}
	}

}

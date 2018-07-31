/*
 * Copyright 2010 Brian Pellin.
 *     
 * This file is part of KeePassDroid.
 *
 *  KeePassDroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid.icons;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.android.keepass.R;


public class Icons {
	private static Map<Integer, Integer> icons = null;
	
	private static void buildList() {
		if (icons == null) {
			icons = new HashMap<Integer, Integer>();
			
			Class<com.android.keepass.R.drawable> c = com.android.keepass.R.drawable.class;
			
			Field[] fields = c.getFields();
			
			for (int i = 0; i < fields.length; i++) {
				String fieldName = fields[i].getName();
				if (fieldName.matches("ic\\d{2}.*")) {
					String sNum = fieldName.substring(2, 4);
					int num = Integer.parseInt(sNum);
					if (num > 69) {
						continue;
					}
					
					int resId;
					try {
						resId = fields[i].getInt(null);
					} catch (Exception e) {
						continue;
					}
					
					icons.put(num, resId);
				}
			}
		}	
	}
	
	public static int iconToResId(int iconId) {
		buildList();
		
		Integer resId = icons.get(iconId);
		
		if (resId == null) {
			return R.drawable.ic99_blank;
		}
		
		return resId;
	}
	
	public static int count() {
		buildList();
		
		return icons.size();
	}

}

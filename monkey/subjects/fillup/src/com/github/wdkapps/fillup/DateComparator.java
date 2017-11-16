/*
 * *****************************************************************************
 * Copyright 2013 William D. Kraemer
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *    
 * ****************************************************************************
 */

package com.github.wdkapps.fillup;

import java.util.Comparator;

/**
 * DESCRIPTION:
 * Used to compare two GasRecord instances to determine their ordering 
 * with respect to each other based on date value.
 */
public class DateComparator implements Comparator<GasRecord>{

	/**
	 * DESCRIPTION:
	 * Compare two GasRecord instances.
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(GasRecord lhs, GasRecord rhs) {
		return lhs.getDate().compareTo(rhs.getDate());
	}

}

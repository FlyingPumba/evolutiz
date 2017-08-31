/**
 * Copyright (C) 2009 SC 4ViewSoft SRL
 * Copyright (C) 2009 Cyril Jaquier
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.util.MathHelper;

/**
 * An XY series encapsulates values for XY charts like line, time, area,
 * scatter... charts.
 */
public class XYSeries implements Serializable {
	/** The series title. */
	private String mTitle;
	/** A list to contain the values for the X axis. */
	private final List<Double> mX = new ArrayList<Double>();
	/** A list to contain the values for the Y axis. */
	private final List<Double> mY = new ArrayList<Double>();
	/** The minimum value for the X axis. */
	private double mMinX = MathHelper.NULL_VALUE;
	/** The maximum value for the X axis. */
	private double mMaxX = -MathHelper.NULL_VALUE;
	/** The minimum value for the Y axis. */
	private double mMinY = MathHelper.NULL_VALUE;
	/** The maximum value for the Y axis. */
	private double mMaxY = -MathHelper.NULL_VALUE;

	/**
	 * Builds a new XY series.
	 * 
	 * @param title
	 *            the series title.
	 */
	public XYSeries(String title) {
		mTitle = title;
	}

	/**
	 * Returns the series title.
	 * 
	 * @return the series title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Sets the series title.
	 * 
	 * @param title
	 *            the series title
	 */
	public void setTitle(String title) {
		mTitle = title;
	}

	/**
	 * Adds a new value to the series.
	 * 
	 * @param x
	 *            the value for the X axis
	 * @param y
	 *            the value for the Y axis
	 */
	public void add(double x, double y) {
		mX.add(x);
		mY.add(y);
		mMinX = Math.min(mMinX, x);
		mMaxX = Math.max(mMaxX, x);
		mMinY = Math.min(mMinY, y);
		mMaxY = Math.max(mMaxY, y);
	}

	/**
	 * Returns the X axis value at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the X value
	 */
	public double getX(int index) {
		return mX.get(index);
	}

	/**
	 * Returns the Y axis value at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the Y value
	 */
	public double getY(int index) {
		return mY.get(index);
	}

	/**
	 * Returns the series item count.
	 * 
	 * @return the series item count
	 */
	public int getItemCount() {
		return mX.size();
	}

	/**
	 * Returns the minimum value on the X axis.
	 * 
	 * @return the X axis minimum value
	 */
	public double getMinX() {
		return mMinX;
	}

	/**
	 * Returns the minimum value on the Y axis.
	 * 
	 * @return the Y axis minimum value
	 */
	public double getMinY() {
		return mMinY;
	}

	/**
	 * Returns the maximum value on the X axis.
	 * 
	 * @return the X axis maximum value
	 */
	public double getMaxX() {
		return mMaxX;
	}

	/**
	 * Returns the maximum value on the Y axis.
	 * 
	 * @return the Y axis maximum value
	 */
	public double getMaxY() {
		return mMaxY;
	}
}

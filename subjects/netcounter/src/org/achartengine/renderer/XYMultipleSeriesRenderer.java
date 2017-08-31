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
package org.achartengine.renderer;

import java.util.HashMap;
import java.util.Map;

import org.achartengine.util.MathHelper;

/**
 * Multiple XY series renderer.
 */
public class XYMultipleSeriesRenderer extends DefaultRenderer {
	/** The chart title. */
	private String mChartTitle = "";
	/** The X axis title. */
	private String mXTitle = "";
	/** The Y axis title. */
	private String mYTitle = "";
	/** The start value in the X axis range. */
	private double mMinX = MathHelper.NULL_VALUE;
	/** The end value in the X axis range. */
	private double mMaxX = -MathHelper.NULL_VALUE;
	/** The start value in the Y axis range. */
	private double mMinY = MathHelper.NULL_VALUE;
	/** The end value in the Y axis range. */
	private double mMaxY = -MathHelper.NULL_VALUE;
	/** The approximative number of labels on the x axis. */
	private int mXLabels = 5;
	/** The approximative number of labels on the y axis. */
	private int mYLabels = 5;
	/** The current orientation of the chart. */
	private Orientation mOrientation = Orientation.HORIZONTAL;
	/** The X axis text labels. */
	private final Map<Double, String> mXTextLabels = new HashMap<Double, String>();
	/** If the values should be displayed above the chart points. */
	private boolean mDisplayChartValues;

	/**
	 * An enum for the XY chart orientation of the X axis.
	 */
	public enum Orientation {
		HORIZONTAL(0), VERTICAL(90);
		/** The rotate angle. */
		private int mAngle = 0;

		private Orientation(int angle) {
			mAngle = angle;
		}

		/**
		 * Return the orientation rotate angle.
		 * 
		 * @return the orientaion rotate angle
		 */
		public int getAngle() {
			return mAngle;
		}
	}

	/**
	 * Returns the current orientation of the chart X axis.
	 * 
	 * @return the chart orientation
	 */
	public Orientation getOrientation() {
		return mOrientation;
	}

	/**
	 * Sets the current orientation of the chart X axis.
	 * 
	 * @param orientation
	 *            the chart orientation
	 */
	public void setOrientation(Orientation orientation) {
		mOrientation = orientation;
	}

	/**
	 * Returns the chart title.
	 * 
	 * @return the chart title
	 */
	public String getChartTitle() {
		return mChartTitle;
	}

	/**
	 * Sets the chart title.
	 * 
	 * @param title
	 *            the chart title
	 */
	public void setChartTitle(String title) {
		mChartTitle = title;
	}

	/**
	 * Returns the title for the X axis.
	 * 
	 * @return the X axis title
	 */
	public String getXTitle() {
		return mXTitle;
	}

	/**
	 * Sets the title for the X axis.
	 * 
	 * @param title
	 *            the X axis title
	 */
	public void setXTitle(String title) {
		mXTitle = title;
	}

	/**
	 * Returns the title for the Y axis.
	 * 
	 * @return the Y axis title
	 */
	public String getYTitle() {
		return mYTitle;
	}

	/**
	 * Sets the title for the Y axis.
	 * 
	 * @param title
	 *            the Y axis title
	 */
	public void setYTitle(String title) {
		mYTitle = title;
	}

	/**
	 * Returns the start value of the X axis range.
	 * 
	 * @return the X axis range start value
	 */
	public double getXAxisMin() {
		return mMinX;
	}

	/**
	 * Sets the start value of the X axis range.
	 * 
	 * @param min
	 *            the X axis range start value
	 */
	public void setXAxisMin(double min) {
		mMinX = min;
	}

	/**
	 * Returns the end value of the X axis range.
	 * 
	 * @return the X axis range end value
	 */
	public double getXAxisMax() {
		return mMaxX;
	}

	/**
	 * Sets the end value of the X axis range.
	 * 
	 * @param max
	 *            the X axis range end value
	 */
	public void setXAxisMax(double max) {
		mMaxX = max;
	}

	/**
	 * Returns the end value of the Y axis range.
	 * 
	 * @return the Y axis range end value
	 */
	public double getYAxisMin() {
		return mMinY;
	}

	/**
	 * Sets the start value of the Y axis range.
	 * 
	 * @param min
	 *            the Y axis range start value
	 */
	public void setYAxisMin(double min) {
		mMinY = min;
	}

	/**
	 * Returns the end value of the Y axis range.
	 * 
	 * @return the Y axis range end value
	 */
	public double getYAxisMax() {
		return mMaxY;
	}

	/**
	 * Sets the end value of the Y axis range.
	 * 
	 * @param max
	 *            the Y axis range end value
	 */
	public void setYAxisMax(double max) {
		mMaxY = max;
	}

	/**
	 * Returns the approximate number of labels for the X axis.
	 * 
	 * @return the approximate number of labels for the X axis
	 */
	public int getXLabels() {
		return mXLabels;
	}

	/**
	 * Sets the approximate number of labels for the X axis.
	 * 
	 * @param xLabels
	 *            the approximate number of labels for the X axis
	 */
	public void setXLabels(int xLabels) {
		mXLabels = xLabels;
	}

	/**
	 * Adds a new text label for the specified X axis value.
	 * 
	 * @param x
	 *            the X axis value
	 * @param text
	 *            the text label
	 */
	public void addTextLabel(double x, String text) {
		mXTextLabels.put(x, text);
	}

	/**
	 * Returns the X axis text label at the specified X axis value.
	 * 
	 * @param x
	 *            the X axis value
	 * @return the X axis text label
	 */
	public String getXTextLabel(Double x) {
		return mXTextLabels.get(x);
	}

	/**
	 * Returns the X text label locations.
	 * 
	 * @return the X text label locations
	 */
	public Double[] getXTextLabelLocations() {
		return mXTextLabels.keySet().toArray(new Double[0]);
	}

	/**
	 * Returns the approximate number of labels for the Y axis.
	 * 
	 * @return the approximate number of labels for the Y axis
	 */
	public int getYLabels() {
		return mYLabels;
	}

	/**
	 * Sets the approximate number of labels for the Y axis.
	 * 
	 * @param yLabels
	 *            the approximate number of labels for the Y axis
	 */
	public void setYLabels(int yLabels) {
		mYLabels = yLabels;
	}

	/**
	 * Returns if the chart point values should be displayed as text.
	 * 
	 * @return if the chart point values should be displayed as text
	 */
	public boolean isDisplayChartValues() {
		return mDisplayChartValues;
	}

	/**
	 * Sets if the chart point values should be displayed as text.
	 * 
	 * @param display
	 *            if the chart point values should be displayed as text
	 */
	public void setDisplayChartValues(boolean display) {
		mDisplayChartValues = display;
	}

}

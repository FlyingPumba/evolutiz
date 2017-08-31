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
package org.achartengine;

import org.achartengine.chart.AbstractChart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * The view that encapsulates the graphical chart.
 */
public class GraphicalView extends View {
	/** The chart to be drawn. */
	private final AbstractChart mChart;
	/** The view bounds. */
	private final Rect mRect = new Rect();

	/**
	 * Creates a new graphical view.
	 * 
	 * @param context
	 *            the context
	 * @param chart
	 *            the chart to be drawn
	 */
	public GraphicalView(Context context, AbstractChart chart) {
		super(context);
		mChart = chart;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		long t = System.currentTimeMillis();
		super.onDraw(canvas);
		canvas.getClipBounds(mRect);
		int top = mRect.top;
		int left = mRect.left;
		int width = mRect.width();
		int height = mRect.height();

		mChart.draw(canvas, left, top, width, height);
		System.out.println("t=" + (System.currentTimeMillis() - t));
	}

}
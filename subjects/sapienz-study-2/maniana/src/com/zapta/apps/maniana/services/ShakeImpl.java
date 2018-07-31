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

package com.zapta.apps.maniana.services;

import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * An implementation of ShakerDetector. This implementation computes for each accelerometer event
 * the accelerator change magnitude |da=<dx, dy, dz>| where da the acceleration difference vector
 * from previous acceleration event (da represents the first derivative of the acceleration). An
 * shake event is generated if (da1 - da2) > threshold, where da1 (da2) is the average da over the
 * last N1 (N2) events, where N1 < N2. da1 represent the recent value of da while da2 represents the
 * longer term background noise level.
 * 
 * @author Tal Dayan.
 */
@MainActivityScope
public class ShakeImpl implements Shaker {

    /** Number of events in the short term time window */
    private final static int N1 = 1;

    /** Number of events in the longer term time window (background noise) */
    private final static int N2 = 5;

    private final ShakerListener mListener;
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;

    /** Indicates if the shaker is currently resumed or paused. */
    private boolean mIsResumed = false;

    /** Acceleration from previous event. */
    private float mLastX;
    private float mLastY;
    private float mLastZ;

    /** System time of previous event. */
    private long mLastEventTimeMillis;

    /** Magnitude from last N2 events. */
    private final int[] mHistory = new int[N2];

    /** Next insertion point in history, [0..N1) */
    private int mNextIndex;

    /** Sum of the last N1 history points. */
    private int mSum1;

    /** Sum of the last N2 history points. */
    private int mSum2;

    /** If greater than zero, do not allow a shake event for this number of sensor events. */
    private int mBlackout = 0;

    /** Used to report shaker's liveliness */
    private long mLastLiveReportingTimeMillis;
    private int mEvnetsSinceLastLiveReporting;

    /** Shake event is triggered when signal > this value. Set later. */
    private int mThreshold;

    /** Event handling adapter. */
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent se) {
            handleSensorChanged(se);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Ignored
        }
    };

    /** Constructor. Leaves the shaker in paused state. */
    public ShakeImpl(Context context, ShakerListener listner) {
        this.mListener = listner;
        // NOTE: this call sometimes hangs under emulator.
        // See http://code.google.com/p/android/issues/detail?id=2566
        // See http://stackoverflow.com/questions/8626718
        LogUtil.info("Shaker: getting sensor service...");
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        LogUtil.info("Shaker: got sensor service.");
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private final void resetHistory() {
        Arrays.fill(mHistory, 0);
        mNextIndex = 0;

        mSum1 = 0;
        mSum2 = 0;

        // TODO: have a more explicit 'history full' condition.
        //
        // Suppress shake events until the history buffer will get refilled
        mBlackout = N2 - 1;
    }

    private final void resetState() {
        // LogUtil.debug("Reseting state");
        resetHistory();

        mLastX = 0f;
        mLastY = 0f;
        mLastZ = 0f;

        mLastEventTimeMillis = 0;

        mLastLiveReportingTimeMillis = System.currentTimeMillis();
        mEvnetsSinceLastLiveReporting = 0;
    }

    /**
     * Accelerometer change event. Called periodically when in resume state.
     */
    private void handleSensorChanged(SensorEvent se) {
        final long currentEventTimeMillis = System.currentTimeMillis();
        mEvnetsSinceLastLiveReporting++;
        final long reportingDeltaTimeMillis = currentEventTimeMillis - mLastLiveReportingTimeMillis;
        
        // Report every 30 secs
        if (reportingDeltaTimeMillis >  (30 * 1000)) {
            LogUtil.info("Shaker: %d events in %dms",
                    mEvnetsSinceLastLiveReporting, reportingDeltaTimeMillis);
            mLastLiveReportingTimeMillis = currentEventTimeMillis;
            mEvnetsSinceLastLiveReporting = 0;
        }

        // Accelerations in X,Y,Z direction
        final float x = se.values[SensorManager.DATA_X];
        final float y = se.values[SensorManager.DATA_Y];
        final float z = se.values[SensorManager.DATA_Z];

        // Calculate acceleration change (first derivative of acceleration vector).
        final float dX = x - mLastX;
        final float dY = y - mLastY;
        final float dZ = z - mLastZ;

        // If no previous sample than skip this event.
        if (mLastEventTimeMillis == 0) {
            LogUtil.info("Shaker: No prev sample, skipping this one");
            mLastEventTimeMillis = currentEventTimeMillis;
            return;
        }

        // If delta time is way too long, reset state. Sensing has paused
        // for some reason.
        final long deltaTimeMillis = (currentEventTimeMillis - mLastEventTimeMillis);
        mLastEventTimeMillis = currentEventTimeMillis;
        if (deltaTimeMillis > 5000) {
            // Note: resetHistory() preserve the lastXYZ and last event time.
            LogUtil.info("Shaker: Reseting history, dt: %sms", deltaTimeMillis);
            resetHistory();
            return;
        }

        // Calculate change magnitude |<dx, dy, dz>|. Scaled by an arbitrary scale
        // to provide enough int bits of accuracy. We use ints to avoid accomulating
        // error in the incremental tracking of sum1, sum2.
        final int newValue = (int) (Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ)) * 500);

        // Push to history queue and update incrementally the N1 and N2 sums.
        final int droppedValue1 = mHistory[(mNextIndex + N2 - N1) % N2];
        final int droppedValue2 = mHistory[mNextIndex];

        mHistory[mNextIndex++] = newValue;
        if (mNextIndex >= N2) {
            mNextIndex = 0;
        }

        mSum1 += (newValue - droppedValue1);
        mSum2 += (newValue - droppedValue2);

        // Save for next iteration
        mLastX = x;
        mLastY = y;
        mLastZ = z;

        // If in blackout, don't issue a shake event in this cycle.
        if (mBlackout > 0) {
            mBlackout--;
            return;
        }

        // The monitored signal is the difference between the short term average and the long
        // term average (noise level)
        final int avg1 = mSum1 / N1;
        final int avg2 = mSum2 / N2;
        final int signal = (avg1 - avg2);

        // LogUtil.debug("signal: %s", signal);

        // Compare signal to the detection threshold
        if (signal > mThreshold) {
            LogUtil.info("Shaker: initiaging onShake()");
            mListener.onShake();
            // Debouncing. Avoid successive shake event for the next N2 cycles.
            mBlackout = N2;
        }
    }

    @Override
    public boolean resume(int force) {
        if (!mIsResumed) {
            resetState();
            // Using NORMAL (low) rate for better battery life.
            mIsResumed = mSensorManager.registerListener(mSensorListener, mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Force sensitivity to [1..10]
        final int actualForce = Math.max(1, Math.min(10, force));

        // Map sensitivity to threshold. Values are based on trial and error..
        mThreshold = 1300 + (actualForce * 700);
        // LogUtil.debug("Shaker resumed, force: %s, threshold: %s", actualForce, threshold);
        return mIsResumed;
    }

    @Override
    public void pause() {
        if (mIsResumed) {
            mSensorManager.unregisterListener(mSensorListener);
            mIsResumed = false;
        }
    }
}

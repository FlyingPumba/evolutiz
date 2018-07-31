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

package com.zapta.apps.maniana.util;

import com.zapta.apps.maniana.annotations.ApplicationScope;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Shake detector implementation.
 * 
 * @author Tal Dayan.
 */
@ApplicationScope
public class ShakerUtil  {

    /** Used to test if sensor service is available. */
    private static final SensorEventListener DUMMY_LISTENER = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent se) {
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Ignored
        }
    }; 

    public static boolean serviceSupported(Context context) {
        final SensorManager sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        final Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        final boolean result = sensorManager.registerListener(DUMMY_LISTENER, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
        if (result) {
            sensorManager.unregisterListener(DUMMY_LISTENER);
        }
        return result;
    }
}

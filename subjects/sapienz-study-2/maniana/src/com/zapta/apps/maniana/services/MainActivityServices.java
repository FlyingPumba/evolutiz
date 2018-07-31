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

import java.util.List;

import javax.annotation.Nullable;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.speech.RecognizerIntent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.util.DisplayUtil;
import com.zapta.apps.maniana.util.LogUtil;
import com.zapta.apps.maniana.util.PackageUtil;
import com.zapta.apps.maniana.util.RandomUtil;

/**
 * Provides common app services.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class MainActivityServices {

    /** A combined listener. */
    private static interface MediaPlayerListener extends OnCompletionListener, OnErrorListener {
    }

    /** The app context. */
    private final MainActivityState mMainActivityState;

    private final int mAppVersionCode;

    private final String mAppVersionName;

    /** Cached window manager for this app. */
    private final WindowManager mWindowManager;

    private final LayoutInflater mLayoutInflater;

    private final MediaPlayerListener mMediaPlayerListener = new MediaPlayerListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtil.error("Error when playing applause track (%d, %d)", what, extra);
            releaseMediaPlayer();
            return true;
        }
    };

    /** In the range [0, 1] */
    private final float mNormalizedSoundEffectVolume;

    private final BackupManager mBackupManager;

    private final float mDensity;

    @Nullable
    private MediaPlayer mMediaPlayer;

    public MainActivityServices(MainActivityState mainActivityState) {
        this.mMainActivityState = mainActivityState;

        PackageInfo packageInfo = PackageUtil.getPackageInfo(mMainActivityState.context());
        mAppVersionCode = packageInfo.versionCode;
        mAppVersionName = packageInfo.versionName;

        mWindowManager = (WindowManager) mainActivityState.context().getSystemService(
                Context.WINDOW_SERVICE);
        mLayoutInflater = (LayoutInflater) mainActivityState.context().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        mNormalizedSoundEffectVolume = mMainActivityState.context().getResources()
                .getInteger(R.integer.sound_effect_volume_percent) / 100.0f;

        mBackupManager = new BackupManager(mMainActivityState.context());

        mDensity = DisplayUtil.getDensity(mainActivityState.context());
    }

    public final int getAppVersionCode() {
        return mAppVersionCode;
    }

    public final String getAppVersionName() {
        return mAppVersionName;
    }

    public final WindowManager windowManager() {
        return mWindowManager;
    }

    public final BackupManager backupManager() {
        return mBackupManager;
    }

    public final LayoutInflater layoutInflater() {
        return mLayoutInflater;
    }

    /** Get screen density. This is an invariant and can be cached safely. */
    public final float density() {
        return mDensity;
    }

    /** Convert dip to pixels using underlying density. */
    public final int dipToPixels(int dip) {
        return (int) (dip * mDensity + 0.5f);
    }

    /** Activate a medium length vibration */
    public final void vibrateForLongPress() {
        mMainActivityState
                .view()
                .getRootView()
                .performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
    }

    /**
     * Start a sound affect if allowed.
     * 
     * @param fxEffectType the sound effect to use (one of )
     * @param fallBackToShortVibration indicates what to do if sound effects are disabled in
     *        settings. If true then activate a short vibration instead, otherwise do nothing.
     */
    public final void maybePlayStockSound(int fxEffectType, boolean fallBackToShortVibration) {
        if (mMainActivityState.prefTracker().getSoundEnabledPreference()) {
            final AudioManager audioManager = (AudioManager) mMainActivityState.context()
                    .getSystemService(Context.AUDIO_SERVICE);
            audioManager.playSoundEffect(fxEffectType, mNormalizedSoundEffectVolume);
        } else if (fallBackToShortVibration) {
            vibrateForLongPress();
        }
    }

    private final void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public final void maybePlayApplauseSoundClip(int fallbackFxEffectType,
            boolean fallBackToShortVibration) {
        if (shouldPlayApplauseSoundClip()) {
            // releaseMediaPlayer();

            // Determine sound track to play
            final int rand = RandomUtil.random.nextInt(100);
            final int trackResourceId = (rand < 90) ? R.raw.applause_normal
                    : R.raw.applause_special;

            startPlayingSoundClip(trackResourceId);
            return;
        }
        maybePlayStockSound(fallbackFxEffectType, fallBackToShortVibration);
    }

    /**
     * Determine if a request for an applause should play an applause or should fall back.
     * 
     * @return true if applause should be played.
     */
    private final boolean shouldPlayApplauseSoundClip() {
        if (!mMainActivityState.prefTracker().getSoundEnabledPreference()) {
            return false;
        }
        switch (mMainActivityState.prefTracker().getApplauseLevelPreference()) {
            case NEVER:
                return false;
            case ALWAYS:
                return true;
            default:
                // 20% probability
                return RandomUtil.random.nextInt(100) < 20;
        }
    }

    private final void startPlayingSoundClip(int rawResourceId) {
        releaseMediaPlayer();
        mMediaPlayer = MediaPlayer.create(mMainActivityState.context(), rawResourceId);

        // Added as a response to this FC report from a user:
        // https://code.google.com/p/maniana/issues/detail?id=8
        // Apparently MediaPlayer.create() may fail for unspecified reasons.
        if (mMediaPlayer == null) {
            LogUtil.error("Creation of a media player failed. Resource id = 0x%x", rawResourceId);
            return;
        }

        mMediaPlayer.setOnCompletionListener(mMediaPlayerListener);
        mMediaPlayer.setOnErrorListener(mMediaPlayerListener);
        mMediaPlayer.start();
    }

    /** Show a brief popup message with given formatted string */
    public final void toast(String format, Object... args) {
        toast(String.format(format, args));
    }

    /**
     * Show a brief popup message with given string. More efficient than the vararg one since it
     * does not allocate a vararg array
     */
    public final void toast(String message) {
        Toast.makeText(mMainActivityState.context(), message, Toast.LENGTH_SHORT).show();
    }

    public final void toast(int messageResourceId) {
        toast(mMainActivityState.context().getString(messageResourceId));
    }

    public final void toast(int messageResourceId, Object... args) {
        toast(mMainActivityState.context().getString(messageResourceId, args));
    }

    public static boolean isVoiceRecognitionSupported(Context context) {
        // Check to see if a recognition activity is present
        final PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }
    
    // TODO: make sure all calls to startActivity() point here and not directly to main activity.
    /** Return true if ok. */
    public final boolean startActivity(Intent intent) {
        try {
            mMainActivityState.context().startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

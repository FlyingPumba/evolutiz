/*   
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.android.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/** 
 * Main activity: shows media player buttons. This activity shows the media player buttons and
 * lets the user click them. No media handling is done here -- everything is done by passing
 * Intents to our {@link MusicService}.
 * */
public class MainActivity extends Activity implements OnClickListener {
    /**
     * The URL we suggest as default when adding by URL. This is just so that the user doesn't
     * have to find an URL to test this sample.
     */
    final String SUGGESTED_URL = "http://www.vorbis.com/music/Epoq-Lepidoptera.ogg";

    Button mPlayButton;
    Button mPauseButton;
    Button mSkipButton;
    Button mRewindButton;
    Button mStopButton;
    Button mEjectButton;

    /**
     * Called when the activity is first created. Here, we simply set the event listeners and
     * start the background service ({@link MusicService}) that will handle the actual media
     * playback.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPlayButton = (Button) findViewById(R.id.playbutton);
        mPauseButton = (Button) findViewById(R.id.pausebutton);
        mSkipButton = (Button) findViewById(R.id.skipbutton);
        mRewindButton = (Button) findViewById(R.id.rewindbutton);
        mStopButton = (Button) findViewById(R.id.stopbutton);
        mEjectButton = (Button) findViewById(R.id.ejectbutton);

        mPlayButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mSkipButton.setOnClickListener(this);
        mRewindButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mEjectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View target) {
        // Send the correct intent to the MusicService, according to the button that was clicked
        if (target == mPlayButton) {
			Log.i("MAYUR", "CLICKED: PLAY");
            startService(new Intent(MusicService.ACTION_PLAY));
		} else if (target == mPauseButton) {
			Log.i("MAYUR", "CLICKED: PAUSE");
            startService(new Intent(MusicService.ACTION_PAUSE));
        } else if (target == mSkipButton) {
			Log.i("MAYUR", "CLICKED: SKIP");
            startService(new Intent(MusicService.ACTION_SKIP));
        } else if (target == mRewindButton) {
			Log.i("MAYUR", "CLICKED: REWIND");
            startService(new Intent(MusicService.ACTION_REWIND));
        } else if (target == mStopButton) {
			Log.i("MAYUR", "CLICKED: STOP");
            startService(new Intent(MusicService.ACTION_STOP));
        } else if (target == mEjectButton) {
			Log.i("MAYUR", "CLICKED: EJECT");
            showUrlDialog();
        }
    }

    /** 
     * Shows an alert dialog where the user can input a URL. After showing the dialog, if the user
     * confirms, sends the appropriate intent to the {@link MusicService} to cause that URL to be
     * played.
     */
    void showUrlDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Manual Input");
        alertBuilder.setMessage("Enter a URL (must be http://)");
        final EditText input = new EditText(this);
		input.setEnabled(false);
        alertBuilder.setView(input);

        input.setText(SUGGESTED_URL);

        alertBuilder.setPositiveButton("Play!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int whichButton) {
				Log.i("MAYUR", "CLICKED: PLAY");
                // Send an intent with the URL of the song to play. This is expected by
                // MusicService.
                Intent i = new Intent(MusicService.ACTION_URL);
                Uri uri = Uri.parse(input.getText().toString());
                i.setData(uri);
                startService(i);
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int whichButton) {
				Log.i("MAYUR", "CLICKED: CANCEL");
			}
        });

        alertBuilder.show();
    }
}

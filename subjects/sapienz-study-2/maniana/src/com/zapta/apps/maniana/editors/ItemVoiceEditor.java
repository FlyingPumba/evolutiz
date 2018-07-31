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

package com.zapta.apps.maniana.editors;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;

/**
 * Item voice recognition editor.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class ItemVoiceEditor {

    public static void startVoiceEditor(Activity parentActivity, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, parentActivity.getString(R.string.voice_recognition_Dictate_a_new_task));
        parentActivity.startActivityForResult(intent, requestCode);
    }

    public static void startSelectionDialog(Context context, Intent voiceRcognitionIntent,
            final OnItemClickListener selectionListener) {
        ArrayList<String> matches = voiceRcognitionIntent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        // TODO: make this dialog trackable ?.
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.voice_list_dialog_layout);
        dialog.setTitle(context.getString(R.string.voice_recognition_Select_best_match));

        ListView listView = (ListView) dialog.findViewById(R.id.voice_selection_list);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, matches);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                dialog.dismiss();
                selectionListener.onItemClick(arg0, arg1, arg2, arg3);               
            }});

        dialog.show();
    }
}

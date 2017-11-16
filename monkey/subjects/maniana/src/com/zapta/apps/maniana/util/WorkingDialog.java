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

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ActivityScope;

/**
 * A opoup dialog for short 'wait' messages
 * 
 * @author tal
 */
@ActivityScope
public class WorkingDialog extends Dialog {

    public WorkingDialog(Context context, String message) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.working_dialog_layout);
        
        final TextView textView = (TextView) findViewById(R.id.working_text_view);
        textView.setText(message);     
    }
}

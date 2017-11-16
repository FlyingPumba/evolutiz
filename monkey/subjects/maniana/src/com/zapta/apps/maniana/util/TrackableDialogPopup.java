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

import com.zapta.apps.maniana.annotations.ActivityScope;
import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;

/**
 * Base class for dialogs that can be tracked by a PopupsTracker.
 * 
 * @author Tal Dayan
 */
@ActivityScope
public abstract class TrackableDialogPopup extends Dialog implements TrackablePopup {

    private final PopupsTracker mParentPopupTracker;

    public TrackableDialogPopup(Context context, PopupsTracker parentPopupTracker) {
        super(context);
        mParentPopupTracker = parentPopupTracker;
    }

    /** Called when the parent preference activity is paused. */
    @Override
    public void closeLeftOver() {
        dismiss();
    }

    @Override
    public void show() {
        mParentPopupTracker.track(this);
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mParentPopupTracker.untrack(this);
    }
}

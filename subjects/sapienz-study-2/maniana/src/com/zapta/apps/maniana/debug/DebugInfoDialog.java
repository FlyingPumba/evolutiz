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

package com.zapta.apps.maniana.debug;

import android.app.Dialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.webkit.WebView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.services.MainActivityServices;
import com.zapta.apps.maniana.settings.DateOrder;
import com.zapta.apps.maniana.util.CalendarUtil;
import com.zapta.apps.maniana.util.LanguageUtil;
import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;

/**
 * Display device parameters. For debug mode only.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public class DebugInfoDialog extends Dialog implements TrackablePopup {

    /** Private constructor. Use startDialog() to create and launch a dialog. */
    private DebugInfoDialog(final MainActivityState mainActivityState, String html) {
        super(mainActivityState.context());
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.debug_info_layout);
        setOwnerActivity(mainActivityState.mainActivity());
        getWindow().setGravity(Gravity.CENTER);

        final WebView webView = (WebView) findViewById(R.id.debug_info_web_view);

        // NOTE: the simple WebView.loadData() requires special encoding. Otherwise
        // it does not recognize line breaks within the <pre> section (more details
        // here http://tinyurl.com/c7aolcr). For this reason we use loadDataWithBaseURL
        // and use a dummy base URL.
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
    }

    /** Called when the dialog was left open and the main activity pauses. */
    @Override
    public final void closeLeftOver() {
        if (isShowing()) {
            dismiss();
        }
    }

    /** Launch a debug dialog with device info. */
    public static void startDialog(final MainActivityState mainActivityState) {
        final Context context = mainActivityState.context();
        final Display display = mainActivityState.services().windowManager().getDefaultDisplay();
        final boolean hasVoiceRecogintionService = MainActivityServices
                .isVoiceRecognitionSupported(mainActivityState.context());

        final StringBuilder sb = new StringBuilder();
        sb.append("<html>\n<body>\n");
        sb.append("DEVICE INFO\n<pre>\n");
        sb.append("API level: " + android.os.Build.VERSION.SDK_INT + "\n");
        sb.append("Translation code: " + LanguageUtil.currentTranslationCode(context) + "\n");
        sb.append("Translation name: " + LanguageUtil.currentTranslationName(context) + "\n");
        sb.append("Uses Cyrillic chars: " + LanguageUtil.currentLanguageUsesCyrillic(context) + "\n");
        sb.append("Uses French: " + LanguageUtil.currentLanguageIsFrench(context) + "\n");
        sb.append("Display density: " + mainActivityState.services().density() + "\n");
        sb.append("Display Width: " + display.getWidth() + "\n");
        sb.append("Display Height: " + display.getHeight() + "\n");
        sb.append("Voice recognition: " + hasVoiceRecogintionService + "\n");
        sb.append("Calendar Intents: " + CalendarUtil.debugGoogleCalendarVariants(context) + "\n");
        sb.append("Date order: " + DateOrder.localDateOrder(context) + " ("
                + String.valueOf(DateFormat.getDateFormatOrder(context)) + ")\n");

        sb.append("</pre>\n</body>\n</html>\n");

        final String html = sb.toString();

        final DebugInfoDialog dialog = new DebugInfoDialog(mainActivityState, html);
        mainActivityState.popupsTracker().track(dialog);
        dialog.show();
    }
}

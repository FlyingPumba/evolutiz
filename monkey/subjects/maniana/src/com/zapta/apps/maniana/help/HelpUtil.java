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

package com.zapta.apps.maniana.help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.util.PackageUtil;

/**
 * Provides date related operations.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public final class HelpUtil {
    /**
     * Production help url format. Contains place holders for language specifier and version code,
     * in this order. The version code will allow future dispatching to a version specific help
     * file.
     * 
     * Currently using Google code hosting for serving of the html static content.
     */
    private static final String HELP_URL_PROD = "http://maniana.googlecode.com/git/www/help/help%s.html?v=%d";

    /** Like HELP_URL_PROD but with a test url. */
    private static final String HELP_URL_TEST = "http://maniana.comoj.com/help/help%s.html?v=%d";

    /** Do not instantiate */
    private HelpUtil() {
    }

    public static Intent helpPageIntent(Context context, boolean testHtmlPage) {
        final String languageCode = context.getString(R.string.translation_language_code);
        final String fileSuffix = languageCode.equals("en") ? "" : ("-" + languageCode);
        final int versionCode = PackageUtil.getPackageInfo(context).versionCode;
        final String urlFormat = testHtmlPage ? HELP_URL_TEST : HELP_URL_PROD;
        final String url = String.format(urlFormat, fileSuffix, versionCode);
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }
}

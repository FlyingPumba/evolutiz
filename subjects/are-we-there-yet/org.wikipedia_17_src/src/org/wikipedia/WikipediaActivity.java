package org.wikipedia;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.phonegap.DroidGap;

public class WikipediaActivity extends DroidGap {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		super.loadUrl("file:///android_asset/www/index.html");

		String currentUA = this.appView.getSettings().getUserAgentString();

		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			this.appView.getSettings().setUserAgentString("WikipediaMobile/" + pInfo.versionName + " " + currentUA);
		} catch (NameNotFoundException e) {
			// This never actually happens. Trust me, I'm an engineer!
		}
	}
}

// Copyright 2008 Brock M. Tice
/*  This file is part of JustSit.

    JustSit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JustSit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JustSit.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.brocktice.JustSit;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class JsAbout extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		PackageManager pm = getPackageManager();
        try {
            //---get the package info---
            PackageInfo pi =  
                pm.getPackageInfo("com.brocktice.JustSit", 0);
            //---display the version name---
            TextView tv = (TextView) findViewById(R.id.about_version);            
            tv.setText("Version: " + pi.versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

	}

}

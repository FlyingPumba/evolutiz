package org.dnaq.dialer2;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DialerPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

/*
Copyright 2010-2012 Michael Shick

This file is part of 'Lock Pattern Generator'.

'Lock Pattern Generator' is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.

'Lock Pattern Generator' is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
'Lock Pattern Generator'.  If not, see <http://www.gnu.org/licenses/>.
*/
package in.shick.lockpatterngenerator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private SharedPreferences mPreferences;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences,
            String key)
    {
        // simple string to integer conversions
        if(key.equals("dummy_pattern_max"))
        {
            dummyToInt("pattern_max", preferences, Defaults.PATTERN_MAX);
        }
        else if(key.equals("dummy_pattern_min"))
        {
            dummyToInt("pattern_min", preferences, Defaults.PATTERN_MIN);
        }
        else if(key.equals("dummy_grid_length"))
        {
            dummyToInt("grid_length", preferences, Defaults.GRID_LENGTH);
        }
    }

    private void dummyToInt(String key, SharedPreferences preferences,
            int defaultValue)
    {
        int intValue = defaultValue;
        try
        {
            intValue = Integer.parseInt(
                    preferences.getString("dummy_" + key, "" + defaultValue));
        }
        catch(NumberFormatException e)
        {
            preferences.edit().putString("dummy_" + key,
                    "" + defaultValue).commit();
        }
        preferences.edit().putInt(key, intValue).commit();
    }
}

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.bpace.munchlife;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

import android.util.Log;

public class SettingsActivity extends PreferenceActivity
{
	OnPreferenceChangeListener levelListener = new OnPreferenceChangeListener()
	{
		public boolean onPreferenceChange(Preference pref, Object newValue)
		{
			int value = 0;
			try
			{
				value = Integer.parseInt(newValue.toString());
			}
			catch(NumberFormatException error)
			{
				Log.w(MunchLifeActivity.TAG, 
              "NumberFormatException: " + error.getMessage());
			}
		
			if(value > 1 && value <= 100)
			{
				return true;
			}
			else
			{
				Toast.makeText(getApplicationContext(),
                       R.string.maxlevelError,
                       Toast.LENGTH_SHORT).show();
				return false;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Preference levelPreference;
    levelPreference = getPreferenceScreen().findPreference("maxlevelPref");
		levelPreference.setOnPreferenceChangeListener(levelListener);
	}
}

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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EmergencyExit
{
    public static void clearAndBail(Context context)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().clear().putBoolean("exited_hard", true).commit();
        System.exit(-1);
    }
}

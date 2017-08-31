/*
 * Merge.java
 *
 * Copyright (C) 2009 Tim Marston <edam@waxworlds.org>
 *
 * This file is part of the Import Contacts program (hereafter referred
 * to as "this program"). For more information, see
 * http://www.waxworlds.org/edam/software/android/import-contacts
 *
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

package org.waxworlds.edam.importcontacts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;

public class Merge extends WizardActivity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		setContentView( R.layout.merge );
		super.onCreate( savedInstanceState );

		setNextActivity( Doit.class );
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		SharedPreferences.Editor editor = getSharedPreferences().edit();

		// radio button selection
		RadioGroup rg = (RadioGroup)findViewById( R.id.merge_setting );
		editor.putInt( "merge_setting",
				Doit.convertIdToAction( rg.getCheckedRadioButtonId() ) );

		editor.commit();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		SharedPreferences prefs = getSharedPreferences();

		// radio button selection
		RadioGroup rg = (RadioGroup)findViewById( R.id.merge_setting );
		rg.check( Doit.convertActionToId(
				prefs.getInt( "merge_setting", Doit.ACTION_PROMPT ) ) );
	}


}

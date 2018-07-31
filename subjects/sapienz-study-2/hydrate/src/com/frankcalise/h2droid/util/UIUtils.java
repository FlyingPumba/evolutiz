package com.frankcalise.h2droid.util;

import com.frankcalise.h2droid.R;
import com.frankcalise.h2droid.Settings;

import android.content.Context;
import android.text.format.DateUtils;

public class UIUtils {
	public static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME;
	
	public static String formatOneServingButtonText(Context context) {
		int unitsPref = Settings.getUnitSystem(context);
		
		return String.format("%s (%s %s)",
				context.getString(R.string.one_serving_button_label),  
				Settings.getOneServingAmount(context), 
				(unitsPref == Settings.UNITS_METRIC ? context.getString(R.string.unit_mililiters) : context.getString(R.string.unit_fl_oz)));
	}
	
	public static String formatCustomAmountButtonText(Context context) {
		int unitsPref = Settings.getUnitSystem(context);
		
		return (unitsPref == Settings.UNITS_METRIC ? context.getString(R.string.home_n_add_ml) : context.getString(R.string.home_n_add_oz));
	}
}

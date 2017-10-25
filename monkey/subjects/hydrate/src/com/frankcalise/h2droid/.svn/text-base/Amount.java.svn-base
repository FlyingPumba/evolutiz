package com.frankcalise.h2droid;

import android.content.Context;

public class Amount {
	public static final double MILILITERS_PER_LITER = 1000.0;
	public static final double OUNCES_PER_CUP = 8.0;
	public static final double OUNCES_PER_PINT = 16.0;
	public static final double OUNCES_PER_QUART = 32.0;
	public static final double OUNCES_PER_GALLON = 128.0;
	
	private double mAmount;
	private int mUnitSystem;
	private String mUnits;
	
	public double getAmount() { return mAmount; }
	public String getUnits() { return mUnits; }
	
	public Amount(double _currentAmount, int _unitSystem, Context context) {
		mUnitSystem = _unitSystem;
		
		double unitsAmount = _currentAmount;
		mAmount = convertToLargerUnits(unitsAmount, context);
	}
	
	private double convertToLargerUnits(double _amount, Context context) {
		double newAmount = 0.0;
		
		if (mUnitSystem == Settings.UNITS_METRIC) {
			// metric conversion
			if (_amount >= MILILITERS_PER_LITER) {
				// convert to liters
				mUnits = context.getString(R.string.unit_liters);
				newAmount = (_amount / MILILITERS_PER_LITER); 
			} else {
				// leave it as mililiters
				mUnits = context.getString(R.string.unit_mililiters);
				newAmount = _amount;
			}
		} else {
			// us units conversion
			if (_amount >= OUNCES_PER_GALLON) {
				// convert to gallons
				mUnits = context.getString(R.string.unit_gallons);
				newAmount = (_amount / OUNCES_PER_GALLON);
			} else if (_amount >= OUNCES_PER_QUART) {
				// convert to quarts
				mUnits = context.getString(R.string.unit_quarts);
				newAmount = (_amount / OUNCES_PER_QUART);
			} else if (_amount >= OUNCES_PER_PINT) {
				// convert to pints
				mUnits = context.getString(R.string.unit_pints);
				newAmount = (_amount / OUNCES_PER_PINT);
			} else if (_amount >= OUNCES_PER_CUP) {
				// convert to cups
				mUnits = context.getString(R.string.unit_cups);
				newAmount = (_amount / OUNCES_PER_CUP);
			} else {
				// leave as ounces
				mUnits = context.getString(R.string.unit_fl_oz);
				newAmount = _amount;
			}
		}
		
		return newAmount;
	}
}

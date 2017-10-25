package com.frankcalise.h2droid.util;

import java.util.Locale;

public class UnitLocale {
	public static UnitLocale Imperial = new UnitLocale();
	public static UnitLocale Metric = new UnitLocale();
	
	private final static double OZ_PER_ML = 0.0338140227;
	
	public static UnitLocale getCurrent() {
		String countryCode = Locale.getDefault().getCountry();
		
		if ("US".equals(countryCode)) return Imperial;
		if ("LR".equals(countryCode)) return Imperial;
		if ("MM".equals(countryCode)) return Imperial;
		
		return Metric;
	}
	
	public static double convertToImperial(double amount) {
		return amount * UnitLocale.OZ_PER_ML;
	}
	
	public static double convertToMetric(double amount) {
		return amount / UnitLocale.OZ_PER_ML;
	}
}

package com.frankcalise.h2droid;

import java.util.ArrayList;
import java.util.List;

public class WaterEntryList {
	private List<Entry> mList = new ArrayList<Entry>();
	
	public double getTotal() {
		double total = 0.0;
		for (Entry entry : mList) {
			total += entry.getNonMetricAmount();
		}
		
		return total;
	}
	
	public void add(Entry entry) {
		mList.add(entry);
	}	
}

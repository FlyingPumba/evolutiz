package com.frankcalise.h2droid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class FactsActivity extends SherlockActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Apply user's desired theme
        String userTheme = Settings.getUserTheme(this);
        Log.d("HOME_ACTIVITY", "user theme = " +userTheme);
        if (userTheme.equals(getString(R.string.light_theme))) {
        	setTheme(R.style.Theme_Hydrate);
        } else {
        	setTheme(R.style.Theme_Hydrate_Dark);
        }
    	
        super.onCreate(savedInstanceState);
        
        // Set up main layout
        setContentView(R.layout.activity_water_facts);
        
        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.facts_title);
        
        // Get facts from string array resource and
        // create string for text view
        String[] facts = getResources().getStringArray(R.array.water_facts);
        String factsText = "";
        
        int numFacts = facts.length;
        for (int i = 0; i < numFacts; i++) {
        	factsText += facts[i] + "\n\n";
        }
        
        // Populate the text view with the
        // string of facts
        final TextView tv = (TextView)findViewById(R.id.facts_tv);
        tv.setText(factsText);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
    			// App icon in ActionBar pressed, go home
    			Intent intent = new Intent(this, h2droid.class);
    			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(intent);
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
}

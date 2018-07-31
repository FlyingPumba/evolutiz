package i4nc4mp.myLock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

//what we need is when this is launched, a short handler is spawned, and the checkbox for service status
//is replaced by a little "refreshing" spin-wheel until the status of mediator is determined

public class MainPreferenceActivity extends PreferenceActivity {
	private SharedPreferences myprefs;
	
    private boolean security = false;
    
    private boolean enabled = false;
    
    private boolean active = false;
	    
    Handler serviceHandler;
    Task verifyBindTask = new Task();
	
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                                
                addPreferencesFromResource(R.xml.mainpref);
                //instead of a layout we set ourselves to a pref tree
                
                getPreferenceManager().setSharedPreferencesName("myLock");
                //tell the prefs persisted to go in the already existing pref file
                
                serviceHandler = new Handler();
                
                //Next, we have to set some things up just like we did in the old settings activity
                //we use findPreference instead of findViewById
                
                final CheckBoxPreference toggle = (CheckBoxPreference) findPreference("enabled");
                if (toggle == null) Log.e("pref activity","didn't find toggle item");
                else {
                	toggle.setOnPreferenceClickListener(new OnPreferenceClickListener() {
    					
    					public boolean onPreferenceClick(Preference preference) {
    						if(preference.getKey().equals("enabled")) {
    							Context mCon = getApplicationContext();
    						
    							//using enabled flag here caused a failure when turning off then back on
    							//within the same visible lifecycle of the pref screen
    							//this is because we never update enabled except at start
    							if(toggle.isChecked()) {
    								ManageMediator.startService(mCon);
    								//findPreference("enabled").setTitle(R.string.enabled);
    							}
    				        	else {
    				        		ManageMediator.stopService(mCon);
    				        		//findPreference("enabled").setTitle(R.string.disabled);
    				        	}
    							
    							return true;
    					}
    						else return false;
                    	
    					};
                    });
                }
                
              //incoming call settings only used in pre 2.3 before the interaction was locked to system permission
                if (Integer.parseInt(Build.VERSION.SDK) > 8) {
                	((PreferenceGroup) findPreference("incomingoptions")).removeAll();
                	//setVisibility(View.GONE);
                	//setEnabled(false);
                }
                
                
                
        }
        
        @Override
        protected void onStart() {
        	super.onStart();
        	
        	getPrefs();
        }
        
        private void getPrefs() {
        	//we need to show the user's existing prefs and get status for service & security
        	myprefs = getSharedPreferences("myLock", 0);
        	
        	//These 2 and the mode will go by strict pref reading
        	((CheckBoxPreference) findPreference("FG")).setChecked(myprefs.getBoolean("FG", false));
        	((CheckBoxPreference) findPreference("slideGuard")).setChecked(myprefs.getBoolean("slideGuard", false));
        	((CheckBoxPreference) findPreference("oldmode")).setChecked(myprefs.getBoolean("oldmode", false));
        	
        	enabled = myprefs.getBoolean("enabled", false);
        	
        	
        	if (enabled) {
        		//verify that the service is active, we will get true if we held the bind as expected
        		active = ManageMediator.bind(getApplicationContext());
            
        		//necessary to show the pref state while active, since we suppress system pref
        		if (active)	updateStatus(true);
        		else serviceHandler.postDelayed(verifyBindTask, 100L);
        		//we have to wait then doublecheck to allow time for bind to execute        		
        	}
        	else updateStatus(false);
        	
        	//incoming call settings only used in pre 2.3 before the interaction was locked to system permission
        	if (Integer.parseInt(Build.VERSION.SDK) < 9) {
        	((CheckBoxPreference) findPreference("callPrompt")).setChecked(myprefs.getBoolean("callPrompt", false));
        	((CheckBoxPreference) findPreference("rejectEnabled")).setChecked(myprefs.getBoolean("rejectEnabled", false));
        	((CheckBoxPreference) findPreference("cameraAccept")).setChecked(myprefs.getBoolean("cameraAccept", false));
        	}
        	
        	((CheckBoxPreference) findPreference("touchLock")).setChecked(myprefs.getBoolean("touchLock", false));
        	((CheckBoxPreference) findPreference("backUnlock")).setChecked(myprefs.getBoolean("backUnlock", false));
        }
        
       
        private void updateStatus(boolean on) {
        	if (on) {
        		enabled = true;
        	}
        	else {
        		enabled = false;
        	}
        	
        	((CheckBoxPreference) findPreference("enabled")).setChecked(enabled);
        	
        	//if (enabled) findPreference("enabled").setTitle(R.string.enabled);
        	//else findPreference("enabled").setTitle(R.string.disabled);
        	
        }
        
        class Task implements Runnable {
            public void run() {
            	//doesn't handle fallout at all of incorrect enabled flag
            	//the expected outcome here is that user will check the box, causing start
            	//that will correct the pref, but there is no hint given to user
            	//widget should show correct state unless it is a newly added widget
            	//that might be best place to handle incorrect enabled flag
            	
            	updateStatus(ManageMediator.serviceActive(getApplicationContext()));
            	}
    	}
        
        @Override
        protected void onDestroy() {
        	super.onDestroy();
        	
        	serviceHandler.removeCallbacks(verifyBindTask);

    		serviceHandler = null;
        }
       
}
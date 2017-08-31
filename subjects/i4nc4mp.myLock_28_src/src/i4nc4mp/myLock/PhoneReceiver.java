package i4nc4mp.myLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class PhoneReceiver extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
		
		SharedPreferences p = context.getSharedPreferences("myLock",0);
            
            boolean prompt = p.getBoolean("callPrompt", false);
            boolean cam = p.getBoolean("cameraAccept", false);
            boolean lock = p.getBoolean("touchLock", false);

            
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            //String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) && (prompt || cam)) {
            	
                	CallPrompt.launch(context);//start it immediately when state is received.
                	
                	//the only time we manage to start visible phase before phone
                	//is call coming from sleep - prompt handles this itself in onStop
                	//In other words, the phone start causes our onStop
                	//then we take control back!
                }
            
            if (lock) {            
            	if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {            	
            		Intent m = new Intent(context, ScreenMediator.class);
            		context.startService(m);
                }
            	else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
               		Intent m = new Intent(context, ScreenMediator.class);
            		context.stopService(m);
            	}
            }
    }	
}
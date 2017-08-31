package i4nc4mp.myLock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;


//Forces itself into foreground mode, as a low mem death is likely during boot
//many processes are all doing init and demanding resources.

public class BootHandler extends Service {
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(getClass().getSimpleName(),"BootHandler - setting foreground");
		           
            int icon = R.drawable.icon;
            CharSequence tickerText = "myLock";
            
            long when = System.currentTimeMillis();

            Notification notification = new Notification(icon, tickerText, when);
            
            Context context = getApplicationContext();
            CharSequence contentTitle = "myLock";
            CharSequence contentText = "initializing";

            Intent notificationIntent = new Intent(this, MainPreferenceActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            
            final int SVC_ID = 1;
            
            
            startForeground(SVC_ID, notification);
    
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences settings = getSharedPreferences("myLock", 0);

		boolean active = settings.getBoolean("enabled", false);

		
		Intent i = new Intent();
		i.setClassName("i4nc4mp.myLock", "i4nc4mp.myLock.Toggler");
		i.putExtra("i4nc4mp.myLock.TargetState", true);


		//restart if last known state was user-initiated active
		if (active) startService(i);//start toggler
		
		stopForeground(true);
		stopSelf();
		
		return START_NOT_STICKY;//ensure it won't be restarted
	}
}
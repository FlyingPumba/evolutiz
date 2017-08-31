
package com.fsck.k9.service;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import com.fsck.k9.K9;
import com.fsck.k9.helper.AutoSyncHelper;

public class BootReceiver extends CoreReceiver
{

    public static String FIRE_INTENT = "com.fsck.k9.service.BroadcastReceiver.fireIntent";
    public static String SCHEDULE_INTENT = "com.fsck.k9.service.BroadcastReceiver.scheduleIntent";
    public static String CANCEL_INTENT = "com.fsck.k9.service.BroadcastReceiver.cancelIntent";

    public static String ALARMED_INTENT = "com.fsck.k9.service.BroadcastReceiver.pendingIntent";
    public static String AT_TIME = "com.fsck.k9.service.BroadcastReceiver.atTime";

    @Override
    public Integer receive(Context context, Intent intent, Integer tmpWakeLockId)
    {
        if (K9.DEBUG)
            Log.i(K9.LOG_TAG, "BootReceiver.onReceive" + intent);

        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action))
        {
            //K9.setServicesEnabled(context, tmpWakeLockId);
            //tmpWakeLockId = null;
        }
        else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action))
        {
            MailService.actionCancel(context, tmpWakeLockId);
            tmpWakeLockId = null;
        }
        else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action))
        {
            MailService.actionReset(context, tmpWakeLockId);
            tmpWakeLockId = null;
        }
        else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
        {
            MailService.connectivityChange(context, tmpWakeLockId);
            tmpWakeLockId = null;
        }
        else if (AutoSyncHelper.SYNC_CONN_STATUS_CHANGE.equals(action))
        {
            K9.BACKGROUND_OPS bOps = K9.getBackgroundOps();
            if (bOps == K9.BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC)
            {
                MailService.actionReset(context, tmpWakeLockId);
                tmpWakeLockId = null;
            }
        }
        else if (ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED.equals(action))
        {
            K9.BACKGROUND_OPS bOps = K9.getBackgroundOps();
            if (bOps == K9.BACKGROUND_OPS.WHEN_CHECKED || bOps == K9.BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC)
            {
                MailService.actionReset(context, tmpWakeLockId);
                tmpWakeLockId = null;
            }
        }
        else if (FIRE_INTENT.equals(action))
        {
            Intent alarmedIntent = intent.getParcelableExtra(ALARMED_INTENT);
            String alarmedAction = alarmedIntent.getAction();
            if (K9.DEBUG)
                Log.i(K9.LOG_TAG, "BootReceiver Got alarm to fire alarmedIntent " + alarmedAction);
            alarmedIntent.putExtra(WAKE_LOCK_ID, tmpWakeLockId);
            tmpWakeLockId = null;
            context.startService(alarmedIntent);
        }
        else if (SCHEDULE_INTENT.equals(action))
        {
            long atTime = intent.getLongExtra(AT_TIME, -1);
            Intent alarmedIntent = intent.getParcelableExtra(ALARMED_INTENT);
            if (K9.DEBUG)
                Log.i(K9.LOG_TAG,"BootReceiver Scheduling intent " + alarmedIntent + " for " + new Date(atTime));

            PendingIntent pi = buildPendingIntent(context, intent);
            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            alarmMgr.set(AlarmManager.RTC_WAKEUP, atTime, pi);
        }
        else if (CANCEL_INTENT.equals(action))
        {
            Intent alarmedIntent = intent.getParcelableExtra(ALARMED_INTENT);
            if (K9.DEBUG)
                Log.i(K9.LOG_TAG, "BootReceiver Canceling alarmedIntent " + alarmedIntent);

            PendingIntent pi = buildPendingIntent(context, intent);

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(pi);
        }


        return tmpWakeLockId;
    }

    private PendingIntent buildPendingIntent(Context context, Intent intent)
    {
        Intent alarmedIntent = intent.getParcelableExtra(ALARMED_INTENT);
        String alarmedAction = alarmedIntent.getAction();

        Intent i = new Intent(context, BootReceiver.class);
        i.setAction(FIRE_INTENT);
        i.putExtra(ALARMED_INTENT, alarmedIntent);
        Uri uri = Uri.parse("action://" + alarmedAction);
        i.setData(uri);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        return pi;
    }

    public static void scheduleIntent(Context context, long atTime, Intent alarmedIntent)
    {
        if (K9.DEBUG)
            Log.i(K9.LOG_TAG, "BootReceiver Got request to schedule alarmedIntent " + alarmedIntent.getAction());
        Intent i = new Intent();
        i.setClass(context, BootReceiver.class);
        i.setAction(SCHEDULE_INTENT);
        i.putExtra(ALARMED_INTENT, alarmedIntent);
        i.putExtra(AT_TIME, atTime);
        context.sendBroadcast(i);
    }

    public static void cancelIntent(Context context, Intent alarmedIntent)
    {
        if (K9.DEBUG)
            Log.i(K9.LOG_TAG, "BootReceiver Got request to cancel alarmedIntent " + alarmedIntent.getAction());
        Intent i = new Intent();
        i.setClass(context, BootReceiver.class);
        i.setAction(CANCEL_INTENT);
        i.putExtra(ALARMED_INTENT, alarmedIntent);
        context.sendBroadcast(i);
    }

    /**
     * Cancel any scheduled alarm.
     *
     * @param context
     */
    public static void purgeSchedule(final Context context)
    {
        final AlarmManager alarmService = (AlarmManager) context
                                          .getSystemService(Context.ALARM_SERVICE);
        alarmService.cancel(PendingIntent.getBroadcast(context, 0, new Intent()
        {
            @Override
            public boolean filterEquals(final Intent other)
            {
                // we want to match all intents
                return true;
            }
        }, 0));
    }

}

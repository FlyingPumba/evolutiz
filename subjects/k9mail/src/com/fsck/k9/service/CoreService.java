package com.fsck.k9.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.fsck.k9.K9;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.helper.power.TracingPowerManager;
import com.fsck.k9.helper.power.TracingPowerManager.TracingWakeLock;

public abstract class CoreService extends Service
{

    public static String WAKE_LOCK_ID = "com.fsck.k9.service.CoreService.wakeLockId";
    private static ConcurrentHashMap<Integer, TracingWakeLock> wakeLocks = new ConcurrentHashMap<Integer, TracingWakeLock>();
    private static AtomicInteger wakeLockSeq = new AtomicInteger(0);
    private ExecutorService threadPool = null;
    private final String className = getClass().getName();
    private volatile boolean mShutdown = false;

    @Override
    public void onCreate()
    {
        if (K9.DEBUG)
            Log.i(K9.LOG_TAG, "CoreService: " + className + ".onCreate()");
        threadPool = Executors.newFixedThreadPool(1);  // Must be single threaded
        super.onCreate();

    }

    protected static void addWakeLockId(Intent i, Integer wakeLockId)
    {
        if (wakeLockId != null)
        {
            i.putExtra(BootReceiver.WAKE_LOCK_ID, wakeLockId);
        }
    }

    protected static void addWakeLock(Context context, Intent i)
    {
        TracingPowerManager pm = TracingPowerManager.getPowerManager(context);
        TracingWakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CoreService addWakeLock");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(K9.MAIL_SERVICE_WAKE_LOCK_TIMEOUT);

        Integer tmpWakeLockId = wakeLockSeq.getAndIncrement();
        wakeLocks.put(tmpWakeLockId, wakeLock);

        i.putExtra(WAKE_LOCK_ID, tmpWakeLockId);
    }



    @Override
    public void onStart(Intent intent, int startId)
    {
        TracingPowerManager pm = TracingPowerManager.getPowerManager(this);
        TracingWakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CoreService onStart");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(K9.MAIL_SERVICE_WAKE_LOCK_TIMEOUT);

        if (K9.DEBUG)
            Log.i(K9.LOG_TAG, "CoreService: " + className + ".onStart(" + intent + ", " + startId);

        int wakeLockId = intent.getIntExtra(BootReceiver.WAKE_LOCK_ID, -1);
        if (wakeLockId != -1)
        {
            BootReceiver.releaseWakeLock(this, wakeLockId);
        }
        Integer coreWakeLockId = intent.getIntExtra(WAKE_LOCK_ID, -1);
        if (coreWakeLockId != null && coreWakeLockId != -1)
        {
            if (K9.DEBUG)
                Log.d(K9.LOG_TAG, "Got core wake lock id " + coreWakeLockId);
            TracingWakeLock coreWakeLock = wakeLocks.remove(coreWakeLockId);
            if (coreWakeLock != null)
            {
                if (K9.DEBUG)
                    Log.d(K9.LOG_TAG, "Found core wake lock with id " + coreWakeLockId + ", releasing");
                coreWakeLock.release();
            }
        }

        try
        {
            super.onStart(intent, startId);
            startService(intent, startId);
        }
        finally
        {
            wakeLock.release();
        }
    }

    public void execute(Context context, final Runnable runner, int wakeLockTime, final Integer startId)
    {

        TracingPowerManager pm = TracingPowerManager.getPowerManager(context);
        final TracingWakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CoreService execute");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(wakeLockTime);

        Runnable myRunner = new Runnable()
        {
            public void run()
            {
                try
                {
                    boolean oldIsSyncDisabled = MailService.isSyncDisabled();
                    if (K9.DEBUG)
                        Log.d(K9.LOG_TAG, "CoreService (" + className + ") running Runnable " + runner.hashCode() + " with startId " + startId);
                    runner.run();
                    if (MailService.isSyncDisabled() != oldIsSyncDisabled)
                    {
                        MessagingController.getInstance(getApplication()).systemStatusChanged();
                    }
                }
                finally
                {
                    if (K9.DEBUG)
                        Log.d(K9.LOG_TAG, "CoreService (" + className + ") completed Runnable " + runner.hashCode() + " with startId " + startId);
                    wakeLock.release();
                    if (startId != null)
                    {
                        stopSelf(startId);
                    }
                }
            }

        };
        if (threadPool == null)
        {
            Log.e(K9.LOG_TAG, "CoreService.execute (" + className + ") called with no threadPool available; running Runnable " + runner.hashCode() + " in calling thread", new Throwable());
            synchronized (this)
            {
                myRunner.run();
            }
        }
        else
        {
            if (K9.DEBUG)
                Log.d(K9.LOG_TAG, "CoreService (" + className + ") queueing Runnable " + runner.hashCode() + " with startId " + startId);
            try
            {
                threadPool.execute(myRunner);
            }
            catch (RejectedExecutionException e)
            {
                if (!mShutdown)
                {
                    throw e;
                }
                Log.i(K9.LOG_TAG, "CoreService: " + className + " is shutting down, ignoring rejected execution exception: " + e.getMessage());
            }
        }
    }

    public abstract void startService(Intent intent, int startId);

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onLowMemory()
    {
        Log.w(K9.LOG_TAG, "CoreService: " + className + ".onLowMemory() - Running low on memory");
    }

    @Override
    public void onDestroy()
    {
        if (K9.DEBUG)
            Log.i(K9.LOG_TAG, "CoreService: " + className + ".onDestroy()");
        mShutdown = true;
        threadPool.shutdown();
        super.onDestroy();
        //     MessagingController.getInstance(getApplication()).removeListener(mListener);
    }
}


package com.fsck.k9;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;

import com.fsck.k9.activity.MessageCompose;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.controller.MessagingListener;
import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.BinaryTempFileBody;
import com.fsck.k9.service.BootReceiver;
import com.fsck.k9.service.MailService;
import com.fsck.k9.service.ShutdownReceiver;
import com.fsck.k9.service.StorageGoneReceiver;

public class K9 extends Application
{
    /**
     * Components that are interested in knowing when the K9 instance is
     * available and ready (Android invokes Application.onCreate() after other
     * components') should implement this interface and register using
     * {@link K9#registerApplicationAware(ApplicationAware)}.
     */
    public static interface ApplicationAware
    {
        /**
         * Called when the Application instance is available and ready.
         *
         * @param application
         *            The application instance. Never <code>null</code>.
         * @throws Exception
         */
        void initializeComponent(K9 application);
    }

    public static Application app = null;
    public static File tempDirectory;
    public static final String LOG_TAG = "k9";

    /**
     * Components that are interested in knowing when the K9 instance is
     * available and ready.
     *
     * @see ApplicationAware
     */
    private static List<ApplicationAware> observers = new ArrayList<ApplicationAware>();


    public enum BACKGROUND_OPS
    {
        WHEN_CHECKED, ALWAYS, NEVER, WHEN_CHECKED_AUTO_SYNC
    }

    private static String language = "";
    private static int theme = android.R.style.Theme_Light;

    private static final FontSizes fontSizes = new FontSizes();

    private static BACKGROUND_OPS backgroundOps = BACKGROUND_OPS.WHEN_CHECKED;
    /**
     * Some log messages can be sent to a file, so that the logs
     * can be read using unprivileged access (eg. Terminal Emulator)
     * on the phone, without adb.  Set to null to disable
     */
    public static final String logFile = null;
    //public static final String logFile = Environment.getExternalStorageDirectory() + "/k9mail/debug.log";

    /**
     * If this is enabled, various development settings will be enabled
     * It should NEVER be on for Market builds
     * Right now, it just governs strictmode
     **/
    public static boolean DEVELOPER_MODE = true;


    /**
     * If this is enabled there will be additional logging information sent to
     * Log.d, including protocol dumps.
     * Controlled by Preferences at run-time
     */
    public static boolean DEBUG = false;

    /**
     * Should K-9 log the conversation it has over the wire with
     * SMTP servers?
     */

    public static boolean DEBUG_PROTOCOL_SMTP = true;

    /**
     * Should K-9 log the conversation it has over the wire with
     * IMAP servers?
     */

    public static boolean DEBUG_PROTOCOL_IMAP = true;


    /**
     * Should K-9 log the conversation it has over the wire with
     * POP3 servers?
     */

    public static boolean DEBUG_PROTOCOL_POP3 = true;

    /**
     * Should K-9 log the conversation it has over the wire with
     * WebDAV servers?
     */

    public static boolean DEBUG_PROTOCOL_WEBDAV = true;



    /**
     * If this is enabled than logging that normally hides sensitive information
     * like passwords will show that information.
     */
    public static boolean DEBUG_SENSITIVE = false;

    /**
     * Can create messages containing stack traces that can be forwarded
     * to the development team.
     */
    public static boolean ENABLE_ERROR_FOLDER = true;
    public static String ERROR_FOLDER_NAME = "K9mail-errors";


    private static boolean mAnimations = true;

    private static boolean mConfirmDelete = false;
    private static boolean mKeyguardPrivacy = false;

    private static boolean mMessageListStars = true;
    private static boolean mMessageListCheckboxes = false;
    private static boolean mMessageListTouchable = false;
    private static int mMessageListPreviewLines = 2;

    private static boolean mShowCorrespondentNames = true;
    private static boolean mShowContactName = false;
    private static boolean mChangeContactNameColor = false;
    private static int mContactNameColor = 0xff00008f;
    private static boolean mMessageViewFixedWidthFont = false;
    private static boolean mMessageViewReturnToList = false;

    private static boolean mGesturesEnabled = true;
    private static boolean mUseVolumeKeysForNavigation = false;
    private static boolean mUseVolumeKeysForListNavigation = false;
    private static boolean mManageBack = false;
    private static boolean mStartIntegratedInbox = false;
    private static boolean mMeasureAccounts = true;
    private static boolean mCountSearchMessages = true;
    private static boolean mZoomControlsEnabled = false;
    private static boolean mMobileOptimizedLayout = false;
    private static boolean mQuietTimeEnabled = false;
    private static String mQuietTimeStarts = null;
    private static String mQuietTimeEnds = null;
    private static boolean compactLayouts = false;

    

    private static boolean useGalleryBugWorkaround = false;
    private static boolean galleryBuggy;


    /**
     * The MIME type(s) of attachments we're willing to view.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[]
    {
        "*/*",
    };

    /**
     * The MIME type(s) of attachments we're not willing to view.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[]
    {
    };

    /**
     * The MIME type(s) of attachments we're willing to download to SD.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[]
    {
        "*/*",
    };

    /**
     * The MIME type(s) of attachments we're not willing to download to SD.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[]
    {
    };

    /**
     * The special name "INBOX" is used throughout the application to mean "Whatever folder
     * the server refers to as the user's Inbox. Placed here to ease use.
     */
    public static final String INBOX = "INBOX";

    /**
     * For use when displaying that no folder is selected
     */
    public static final String FOLDER_NONE = "-NONE-";

    public static final String LOCAL_UID_PREFIX = "K9LOCAL:";

    public static final String REMOTE_UID_PREFIX = "K9REMOTE:";

    public static final String IDENTITY_HEADER = "X-K9mail-Identity";

    /**
     * Specifies how many messages will be shown in a folder by default. This number is set
     * on each new folder and can be incremented with "Load more messages..." by the
     * VISIBLE_LIMIT_INCREMENT
     */
    public static int DEFAULT_VISIBLE_LIMIT = 25;

    /**
     * The maximum size of an attachment we're willing to download (either View or Save)
     * Attachments that are base64 encoded (most) will be about 1.375x their actual size
     * so we should probably factor that in. A 5MB attachment will generally be around
     * 6.8MB downloaded but only 5MB saved.
     */
    public static final int MAX_ATTACHMENT_DOWNLOAD_SIZE = (128 * 1024 * 1024);

    /**
     * Max time (in millis) the wake lock will be held for when background sync is happening
     */
    public static final int WAKE_LOCK_TIMEOUT = 600000;

    public static final int MANUAL_WAKE_LOCK_TIMEOUT = 120000;

    public static final int PUSH_WAKE_LOCK_TIMEOUT = 60000;

    public static final int MAIL_SERVICE_WAKE_LOCK_TIMEOUT = 30000;

    public static final int BOOT_RECEIVER_WAKE_LOCK_TIMEOUT = 60000;

    /**
     * Time the LED is on/off when blinking on new email notification
     */
    public static final int NOTIFICATION_LED_ON_TIME = 500;
    public static final int NOTIFICATION_LED_OFF_TIME = 2000;

    public static final boolean NOTIFICATION_LED_WHILE_SYNCING = false;
    public static final int NOTIFICATION_LED_FAST_ON_TIME = 100;
    public static final int NOTIFICATION_LED_FAST_OFF_TIME = 100;


    public static final int NOTIFICATION_LED_BLINK_SLOW = 0;
    public static final int NOTIFICATION_LED_BLINK_FAST = 1;



    public static final int NOTIFICATION_LED_SENDING_FAILURE_COLOR = 0xffff0000;

    // Must not conflict with an account number
    public static final int FETCHING_EMAIL_NOTIFICATION      = -5000;
    public static final int SEND_FAILED_NOTIFICATION      = -1500;
    public static final int CONNECTIVITY_ID = -3;


    public static class Intents
    {

        public static class EmailReceived
        {
            public static final String ACTION_EMAIL_RECEIVED    = "com.fsck.k9.intent.action.EMAIL_RECEIVED";
            public static final String ACTION_EMAIL_DELETED     = "com.fsck.k9.intent.action.EMAIL_DELETED";
            public static final String ACTION_REFRESH_OBSERVER  = "com.fsck.k9.intent.action.REFRESH_OBSERVER";
            public static final String EXTRA_ACCOUNT            = "com.fsck.k9.intent.extra.ACCOUNT";
            public static final String EXTRA_FOLDER             = "com.fsck.k9.intent.extra.FOLDER";
            public static final String EXTRA_SENT_DATE          = "com.fsck.k9.intent.extra.SENT_DATE";
            public static final String EXTRA_FROM               = "com.fsck.k9.intent.extra.FROM";
            public static final String EXTRA_TO                 = "com.fsck.k9.intent.extra.TO";
            public static final String EXTRA_CC                 = "com.fsck.k9.intent.extra.CC";
            public static final String EXTRA_BCC                = "com.fsck.k9.intent.extra.BCC";
            public static final String EXTRA_SUBJECT            = "com.fsck.k9.intent.extra.SUBJECT";
            public static final String EXTRA_FROM_SELF          = "com.fsck.k9.intent.extra.FROM_SELF";
        }

    }

    /**
     * Called throughout the application when the number of accounts has changed. This method
     * enables or disables the Compose activity, the boot receiver and the service based on
     * whether any accounts are configured.
     */
    public static void setServicesEnabled(Context context)
    {
        int acctLength = Preferences.getPreferences(context).getAvailableAccounts().size();

        setServicesEnabled(context, acctLength > 0, null);

    }

    public static void setServicesEnabled(Context context, Integer wakeLockId)
    {
        setServicesEnabled(context, Preferences.getPreferences(context).getAvailableAccounts().size() > 0, wakeLockId);
    }

    public static void setServicesEnabled(Context context, boolean enabled, Integer wakeLockId)
    {

        PackageManager pm = context.getPackageManager();

        if (!enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        {
            /*
             * If no accounts now exist but the service is still enabled we're about to disable it
             * so we'll reschedule to kill off any existing alarms.
             */
            MailService.actionReset(context, wakeLockId);
        }
        Class<?>[] classes = { MessageCompose.class, BootReceiver.class, MailService.class };

        for (Class<?> clazz : classes)
        {

            boolean alreadyEnabled = pm.getComponentEnabledSetting(new ComponentName(context, clazz)) ==
                                     PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

            if (enabled != alreadyEnabled)
            {
                pm.setComponentEnabledSetting(
                    new ComponentName(context, clazz),
                    enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            }
        }

        if (enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        {
            /*
             * And now if accounts do exist then we've just enabled the service and we want to
             * schedule alarms for the new accounts.
             */
            MailService.actionReset(context, wakeLockId);
        }

    }

    /**
     * Register BroadcastReceivers programmaticaly because doing it from manifest
     * would make K-9 auto-start. We don't want auto-start because the initialization
     * sequence isn't safe while some events occur (SD card unmount).
     */
    protected void registerReceivers()
    {
        final StorageGoneReceiver receiver = new StorageGoneReceiver();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");

        final BlockingQueue<Handler> queue = new SynchronousQueue<Handler>();

        // starting a new thread to handle unmount events
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                try
                {
                    queue.put(new Handler());
                }
                catch (InterruptedException e)
                {
                    Log.e(K9.LOG_TAG, "", e);
                }
                Looper.loop();
            }

        }, "Unmount-thread").start();

        try
        {
            final Handler storageGoneHandler = queue.take();
            registerReceiver(receiver, filter, null, storageGoneHandler);
            Log.i(K9.LOG_TAG, "Registered: unmount receiver");
        }
        catch (InterruptedException e)
        {
            Log.e(K9.LOG_TAG, "Unable to register unmount receiver", e);
        }

        registerReceiver(new ShutdownReceiver(), new IntentFilter(Intent.ACTION_SHUTDOWN));
        Log.i(K9.LOG_TAG, "Registered: shutdown receiver");
    }

    public static void save(SharedPreferences.Editor editor)
    {
        editor.putBoolean("enableDebugLogging", K9.DEBUG);
        editor.putBoolean("enableSensitiveLogging", K9.DEBUG_SENSITIVE);
        editor.putString("backgroundOperations", K9.backgroundOps.toString());
        editor.putBoolean("animations", mAnimations);
        editor.putBoolean("gesturesEnabled", mGesturesEnabled);
        editor.putBoolean("useVolumeKeysForNavigation", mUseVolumeKeysForNavigation);
        editor.putBoolean("useVolumeKeysForListNavigation", mUseVolumeKeysForListNavigation);
        editor.putBoolean("manageBack", mManageBack);
        editor.putBoolean("zoomControlsEnabled",mZoomControlsEnabled);
        editor.putBoolean("mobileOptimizedLayout", mMobileOptimizedLayout);
        editor.putBoolean("quietTimeEnabled", mQuietTimeEnabled);
        editor.putString("quietTimeStarts", mQuietTimeStarts);
        editor.putString("quietTimeEnds", mQuietTimeEnds);

        editor.putBoolean("startIntegratedInbox", mStartIntegratedInbox);
        editor.putBoolean("measureAccounts", mMeasureAccounts);
        editor.putBoolean("countSearchMessages", mCountSearchMessages);
        editor.putBoolean("messageListStars",mMessageListStars);
        editor.putBoolean("messageListCheckboxes",mMessageListCheckboxes);
        editor.putBoolean("messageListTouchable",mMessageListTouchable);
        editor.putInt("messageListPreviewLines",mMessageListPreviewLines);

        editor.putBoolean("showCorrespondentNames",mShowCorrespondentNames);
        editor.putBoolean("showContactName",mShowContactName);
        editor.putBoolean("changeRegisteredNameColor",mChangeContactNameColor);
        editor.putInt("registeredNameColor",mContactNameColor);
        editor.putBoolean("messageViewFixedWidthFont",mMessageViewFixedWidthFont);
        editor.putBoolean("messageViewReturnToList", mMessageViewReturnToList);

        editor.putString("language", language);
        editor.putInt("theme", theme);
        editor.putBoolean("useGalleryBugWorkaround", useGalleryBugWorkaround);

        editor.putBoolean("confirmDelete", mConfirmDelete);

        editor.putBoolean("keyguardPrivacy", mKeyguardPrivacy);
        
        editor.putBoolean("compactLayouts", compactLayouts);

        fontSizes.save(editor);
    }

    @Override
    public void onCreate()
    {
        maybeSetupStrictMode();
        super.onCreate();
        app = this;


        galleryBuggy = checkForBuggyGallery();

        Preferences prefs = Preferences.getPreferences(this);
        SharedPreferences sprefs = prefs.getPreferences();
        DEBUG = sprefs.getBoolean("enableDebugLogging", false);
        DEBUG_SENSITIVE = sprefs.getBoolean("enableSensitiveLogging", false);
        mAnimations = sprefs.getBoolean("animations", true);
        mGesturesEnabled = sprefs.getBoolean("gesturesEnabled", true);
        mUseVolumeKeysForNavigation = sprefs.getBoolean("useVolumeKeysForNavigation", false);
        mUseVolumeKeysForListNavigation = sprefs.getBoolean("useVolumeKeysForListNavigation", false);
        mManageBack = sprefs.getBoolean("manageBack", false);
        mStartIntegratedInbox = sprefs.getBoolean("startIntegratedInbox", false);
        mMeasureAccounts = sprefs.getBoolean("measureAccounts", true);
        mCountSearchMessages = sprefs.getBoolean("countSearchMessages", true);
        mMessageListStars = sprefs.getBoolean("messageListStars",true);
        mMessageListCheckboxes = sprefs.getBoolean("messageListCheckboxes",false);
        mMessageListTouchable = sprefs.getBoolean("messageListTouchable",false);
        mMessageListPreviewLines = sprefs.getInt("messageListPreviewLines", 2);

        mMobileOptimizedLayout = sprefs.getBoolean("mobileOptimizedLayout", false);
        mZoomControlsEnabled = sprefs.getBoolean("zoomControlsEnabled",false);

        mQuietTimeEnabled = sprefs.getBoolean("quietTimeEnabled", false);
        mQuietTimeStarts = sprefs.getString("quietTimeStarts", "21:00" );
        mQuietTimeEnds= sprefs.getString("quietTimeEnds", "7:00");

        mShowCorrespondentNames = sprefs.getBoolean("showCorrespondentNames", true);
        mShowContactName = sprefs.getBoolean("showContactName", false);
        mChangeContactNameColor = sprefs.getBoolean("changeRegisteredNameColor", false);
        mContactNameColor = sprefs.getInt("registeredNameColor", 0xff00008f);
        mMessageViewFixedWidthFont = sprefs.getBoolean("messageViewFixedWidthFont", false);
        mMessageViewReturnToList = sprefs.getBoolean("messageViewReturnToList", false);

        useGalleryBugWorkaround = sprefs.getBoolean("useGalleryBugWorkaround", K9.isGalleryBuggy());

        mConfirmDelete = sprefs.getBoolean("confirmDelete", false);

        mKeyguardPrivacy = sprefs.getBoolean("keyguardPrivacy", false);
        
        compactLayouts = sprefs.getBoolean("compactLayouts", false);

        fontSizes.load(sprefs);

        try
        {
            setBackgroundOps(BACKGROUND_OPS.valueOf(sprefs.getString("backgroundOperations", "WHEN_CHECKED")));
        }
        catch (Exception e)
        {
            setBackgroundOps(BACKGROUND_OPS.WHEN_CHECKED);
        }

        K9.setK9Language(sprefs.getString("language", ""));
        K9.setK9Theme(sprefs.getInt("theme", android.R.style.Theme_Light));

        /*
         * We have to give MimeMessage a temp directory because File.createTempFile(String, String)
         * doesn't work in Android and MimeMessage does not have access to a Context.
         */
        BinaryTempFileBody.setTempDirectory(getCacheDir());

        /*
         * Enable background sync of messages
         */

        setServicesEnabled(this);
        registerReceivers();

        MessagingController.getInstance(this).addListener(new MessagingListener()
        {
            private void broadcastIntent(String action, Account account, String folder, Message message)
            {
                try
                {
                    Uri uri = Uri.parse("email://messages/" + account.getAccountNumber() + "/" + Uri.encode(folder) + "/" + Uri.encode(message.getUid()));
                    Intent intent = new Intent(action, uri);
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_ACCOUNT, account.getDescription());
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_FOLDER, folder);
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_SENT_DATE, message.getSentDate());
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_FROM, Address.toString(message.getFrom()));
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_TO, Address.toString(message.getRecipients(Message.RecipientType.TO)));
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_CC, Address.toString(message.getRecipients(Message.RecipientType.CC)));
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_BCC, Address.toString(message.getRecipients(Message.RecipientType.BCC)));
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_SUBJECT, message.getSubject());
                    intent.putExtra(K9.Intents.EmailReceived.EXTRA_FROM_SELF, account.isAnIdentity(message.getFrom()));
                    K9.this.sendBroadcast(intent);
                    if (K9.DEBUG)
                        Log.d(K9.LOG_TAG, "Broadcasted: action=" + action
                              + " account=" + account.getDescription()
                              + " folder=" + folder
                              + " message uid=" + message.getUid()
                             );

                }
                catch (MessagingException e)
                {
                    Log.w(K9.LOG_TAG, "Error: action=" + action
                          + " account=" + account.getDescription()
                          + " folder=" + folder
                          + " message uid=" + message.getUid()
                         );
                }
            }

            @Override
            public void synchronizeMailboxRemovedMessage(Account account, String folder, Message message)
            {
                broadcastIntent(K9.Intents.EmailReceived.ACTION_EMAIL_DELETED, account, folder, message);
            }

            @Override
            public void messageDeleted(Account account, String folder, Message message)
            {
                broadcastIntent(K9.Intents.EmailReceived.ACTION_EMAIL_DELETED, account, folder, message);
            }

            @Override
            public void synchronizeMailboxNewMessage(Account account, String folder, Message message)
            {
                broadcastIntent(K9.Intents.EmailReceived.ACTION_EMAIL_RECEIVED, account, folder, message);
            }

            @Override
            public void searchStats(final AccountStats stats)
            {
                // let observers know a fetch occured
                K9.this.sendBroadcast(new Intent(K9.Intents.EmailReceived.ACTION_REFRESH_OBSERVER, null));
            }

        });

        notifyObservers();
    }

    private void maybeSetupStrictMode()
    {
        if (!K9.DEVELOPER_MODE)
            return;

        try
        {
            Class<?> strictMode = Class.forName("android.os.StrictMode");
            Method enableDefaults = strictMode.getMethod("enableDefaults");
            enableDefaults.invoke(strictMode);
        }

        catch (Exception e)
        {
            // Discard , as it means we're not running on a device with strict mode
            Log.v(K9.LOG_TAG, "Failed to turn on strict mode "+e);
        }

    }


    /**
     * since Android invokes Application.onCreate() only after invoking all
     * other components' onCreate(), here is a way to notify interested
     * component that the application is available and ready
     */
    protected void notifyObservers()
    {
        for (final ApplicationAware aware : observers)
        {
            if (K9.DEBUG)
            {
                Log.v(K9.LOG_TAG, "Initializing observer: " + aware);
            }
            try
            {
                aware.initializeComponent(this);
            }
            catch (Exception e)
            {
                Log.w(K9.LOG_TAG, "Failure when notifying " + aware, e);
            }
        }
    }

    /**
     * Register a component to be notified when the {@link K9} instance is ready.
     *
     * @param component
     *            Never <code>null</code>.
     */
    public static void registerApplicationAware(final ApplicationAware component)
    {
        if (!observers.contains(component))
        {
            observers.add(component);
        }
    }

    public static String getK9Language()
    {
        return language;
    }

    public static void setK9Language(String nlanguage)
    {
        language = nlanguage;
    }

    public static int getK9Theme()
    {
        return theme;
    }

    public static void setK9Theme(int ntheme)
    {
        theme = ntheme;
    }

    public static BACKGROUND_OPS getBackgroundOps()
    {
        return backgroundOps;
    }

    public static boolean setBackgroundOps(BACKGROUND_OPS backgroundOps)
    {
        BACKGROUND_OPS oldBackgroundOps = K9.backgroundOps;
        K9.backgroundOps = backgroundOps;
        return backgroundOps != oldBackgroundOps;
    }

    public static boolean setBackgroundOps(String nbackgroundOps)
    {
        return setBackgroundOps(BACKGROUND_OPS.valueOf(nbackgroundOps));
    }

    public static boolean gesturesEnabled()
    {
        return mGesturesEnabled;
    }

    public static void setGesturesEnabled(boolean gestures)
    {
        mGesturesEnabled = gestures;
    }

    public static boolean useVolumeKeysForNavigationEnabled()
    {
        return mUseVolumeKeysForNavigation;
    }

    public static void setUseVolumeKeysForNavigation(boolean volume)
    {
        mUseVolumeKeysForNavigation = volume;
    }

    public static boolean useVolumeKeysForListNavigationEnabled()
    {
        return mUseVolumeKeysForListNavigation;
    }

    public static void setUseVolumeKeysForListNavigation(boolean enabled)
    {
        mUseVolumeKeysForListNavigation = enabled;
    }

    public static boolean manageBack()
    {
        return mManageBack;
    }

    public static void setManageBack(boolean manageBack)
    {
        mManageBack = manageBack;
    }

    public static boolean zoomControlsEnabled()
    {
        return mZoomControlsEnabled;
    }

    public static void setZoomControlsEnabled(boolean zoomControlsEnabled)
    {
        mZoomControlsEnabled = zoomControlsEnabled;
    }


    public static boolean mobileOptimizedLayout()
    {
        return mMobileOptimizedLayout;
    }

    public static void setMobileOptimizedLayout(boolean mobileOptimizedLayout)
    {
        mMobileOptimizedLayout = mobileOptimizedLayout;
    }

    public static boolean getQuietTimeEnabled()
    {
        return mQuietTimeEnabled;
    }

    public static void setQuietTimeEnabled(boolean quietTimeEnabled)
    {
        mQuietTimeEnabled = quietTimeEnabled;
    }

    public static String getQuietTimeStarts()
    {
        return mQuietTimeStarts;
    }

    public static void setQuietTimeStarts(String quietTimeStarts)
    {
        mQuietTimeStarts = quietTimeStarts;
    }

    public static String getQuietTimeEnds()
    {
        return mQuietTimeEnds;
    }

    public static void setQuietTimeEnds(String quietTimeEnds)
    {
        mQuietTimeEnds = quietTimeEnds;
    }


    public static boolean isQuietTime()
    {
        if (!mQuietTimeEnabled)
        {
            return false;
        }

        Time time = new Time();
        time.setToNow();
        Integer startHour = Integer.parseInt(mQuietTimeStarts.split(":")[0]);
        Integer startMinute = Integer.parseInt(mQuietTimeStarts.split(":")[1]);
        Integer endHour = Integer.parseInt(mQuietTimeEnds.split(":")[0]);
        Integer endMinute = Integer.parseInt(mQuietTimeEnds.split(":")[1]);

        Integer now = (time.hour * 60 ) + time.minute;
        Integer quietStarts = startHour * 60 + startMinute;
        Integer quietEnds =  endHour * 60 +endMinute;

        // If start and end times are the same, we're never quiet
        if (quietStarts.equals(quietEnds))
        {
            return false;
        }


        // 21:00 - 05:00 means we want to be quiet if it's after 9 or before 5
        if (quietStarts > quietEnds)
        {
            // if it's 22:00 or 03:00 but not 8:00
            if ( now >= quietStarts || now <= quietEnds)
            {
                return true;
            }
        }

        // 01:00 - 05:00
        else
        {

            // if it' 2:00 or 4:00 but not 8:00 or 0:00
            if ( now >= quietStarts && now <= quietEnds)
            {
                return true;
            }
        }

        return false;
    }



    public static boolean startIntegratedInbox()
    {
        return mStartIntegratedInbox;
    }

    public static void setStartIntegratedInbox(boolean startIntegratedInbox)
    {
        mStartIntegratedInbox = startIntegratedInbox;
    }

    public static boolean showAnimations()
    {
        return mAnimations;
    }

    public static void setAnimations(boolean animations)
    {
        mAnimations = animations;
    }

    public static boolean messageListTouchable()
    {
        return mMessageListTouchable;
    }

    public static void setMessageListTouchable(boolean touchy)
    {
        mMessageListTouchable = touchy;
    }

    public static int messageListPreviewLines()
    {
        return mMessageListPreviewLines;
    }

    public static void setMessageListPreviewLines(int lines)
    {
        mMessageListPreviewLines = lines;
    }

    public static boolean messageListStars()
    {
        return mMessageListStars;
    }

    public static void setMessageListStars(boolean stars)
    {
        mMessageListStars = stars;
    }
    public static boolean messageListCheckboxes()
    {
        return mMessageListCheckboxes;
    }

    public static void setMessageListCheckboxes(boolean checkboxes)
    {
        mMessageListCheckboxes = checkboxes;
    }

    public static boolean showCorrespondentNames()
    {
        return mShowCorrespondentNames;
    }

    public static void setShowCorrespondentNames(boolean showCorrespondentNames)
    {
        mShowCorrespondentNames = showCorrespondentNames;
    }

    public static boolean showContactName()
    {
        return mShowContactName;
    }

    public static void setShowContactName(boolean showContactName)
    {
        mShowContactName = showContactName;
    }

    public static boolean changeContactNameColor()
    {
        return mChangeContactNameColor;
    }

    public static void setChangeContactNameColor(boolean changeContactNameColor)
    {
        mChangeContactNameColor = changeContactNameColor;
    }

    public static int getContactNameColor()
    {
        return mContactNameColor;
    }

    public static void setContactNameColor(int contactNameColor)
    {
        mContactNameColor = contactNameColor;
    }

    public static boolean messageViewFixedWidthFont()
    {
        return mMessageViewFixedWidthFont;
    }

    public static void setMessageViewFixedWidthFont(boolean fixed)
    {
        mMessageViewFixedWidthFont = fixed;
    }

    public static boolean messageViewReturnToList()
    {
        return mMessageViewReturnToList;
    }

    public static void setMessageViewReturnToList(boolean messageViewReturnToList)
    {
        mMessageViewReturnToList = messageViewReturnToList;
    }

    public static Method getMethod(Class<?> classObject, String methodName)
    {
        try
        {
            return classObject.getMethod(methodName, boolean.class);
        }
        catch (NoSuchMethodException e)
        {
            Log.i(K9.LOG_TAG, "Can't get method " +
                  classObject.toString() + "." + methodName);
        }
        catch (Exception e)
        {
            Log.e(K9.LOG_TAG, "Error while using reflection to get method " +
                  classObject.toString() + "." + methodName, e);
        }
        return null;
    }

    public static FontSizes getFontSizes()
    {
        return fontSizes;
    }

    public static boolean measureAccounts()
    {
        return mMeasureAccounts;
    }

    public static void setMeasureAccounts(boolean measureAccounts)
    {
        mMeasureAccounts = measureAccounts;
    }

    public static boolean countSearchMessages()
    {
        return mCountSearchMessages;
    }

    public static void setCountSearchMessages(boolean countSearchMessages)
    {
        mCountSearchMessages = countSearchMessages;
    }

    public static boolean useGalleryBugWorkaround()
    {
        return useGalleryBugWorkaround;
    }

    public static void setUseGalleryBugWorkaround(boolean useGalleryBugWorkaround)
    {
        K9.useGalleryBugWorkaround = useGalleryBugWorkaround;
    }

    public static boolean isGalleryBuggy()
    {
        return galleryBuggy;
    }

    public static boolean confirmDelete()
    {
        return mConfirmDelete;
    }

    public static void setConfirmDelete(final boolean confirm)
    {
        mConfirmDelete = confirm;
    }

    /**
     * @return Whether privacy rules should be applied when system is locked
     */
    public static boolean keyguardPrivacy()
    {
        return mKeyguardPrivacy;
    }

    public static void setKeyguardPrivacy(final boolean state)
    {
        mKeyguardPrivacy = state;
    }
    
    public static boolean useCompactLayouts()
    {
        return compactLayouts;
    }

    public static void setCompactLayouts(boolean compactLayouts)
    {
        K9.compactLayouts = compactLayouts;
    }

    /**
     * Check if this system contains a buggy Gallery 3D package.
     *
     * We have to work around the fact that those Gallery versions won't show
     * any images or videos when the pick intent is used with a MIME type other
     * than image/* or video/*. See issue 1186.
     *
     * @return true, if a buggy Gallery 3D package was found. False, otherwise.
     */
    private boolean checkForBuggyGallery()
    {
        try
        {
            PackageInfo pi = getPackageManager().getPackageInfo("com.cooliris.media", 0);

            return (pi.versionCode == 30682);
        }
        catch (NameNotFoundException e)
        {
            return false;
        }
    }

}

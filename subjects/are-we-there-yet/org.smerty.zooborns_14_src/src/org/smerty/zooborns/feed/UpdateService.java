package org.smerty.zooborns.feed;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.smerty.zooborns.R;
import org.smerty.zooborns.ZooBorns;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class UpdateService extends IntentService {

  private static final String TAG = UpdateService.class.getName();

  public UpdateService() {
    super(TAG);
    Log.d(TAG, "UpdateService constructed");
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
  }

  @Override
  protected void onHandleIntent(Intent inboundIntent) {
    Log.d(TAG, "onHandleIntent");

    SharedPreferences settings = getSharedPreferences("ZooBornsPrefs", 0);

    if (!settings.getBoolean("notifications", true)) {
      Log.d(TAG, "Notifications disabled, skipping update check.");
      return;
    }

    HttpParams params = new BasicHttpParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, "UTF-8");
    HttpProtocolParams.setUseExpectContinue(params, true);
    HttpProtocolParams.setHttpElementCharset(params, "UTF-8");

    String agent = "ZooBorns for android";

    HttpProtocolParams.setUserAgent(params, agent);

    DefaultHttpClient client = new DefaultHttpClient(params);

    String etag = settings.getString("etag", null);

    HttpHead method = new HttpHead("http://feeds.feedburner.com/Zooborns");
    if (etag != null) {
      method.addHeader("If-None-Match", etag);
    }
    HttpResponse res;
    try {
      res = client.execute(method);

      Map<String, String> responseHeaderMap = new HashMap<String, String>();

      for (Header h : res.getAllHeaders()) {
        responseHeaderMap.put(h.getName(), h.getValue());
      }

      if (res.getStatusLine().getStatusCode() == 304) {
        // feed not modified
        Log.d(TAG, "no zooborns updates found.");
        return; // UpdateStatus.NOT_MODIFIED;
      } else {
        Log.d(TAG, "potential zooborns updates found.");
        String currentETag = null;
        if (responseHeaderMap.containsKey("ETag")) {
          currentETag = responseHeaderMap.get("ETag");
        }
        sendNotification(this, currentETag);
      }
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void sendNotification(final Context context, String currentEtag) {

    SharedPreferences settings = getSharedPreferences("ZooBornsPrefs", 0);

    if (!settings.getString("lastNotificationEtag", "").equals(currentEtag)) {
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("lastNotificationEtag", currentEtag);
      editor.commit();
      Log.d(TAG, "Looks like a new notification.");
    } else {
      // in the case were we have a notification already visible, we should skip
      // the update check entirely?
      Log.d(TAG, "Already sent a notification for this update.");
      return;
    }

    Intent notificationIntent = new Intent(context, ZooBorns.class);
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
        notificationIntent, 0);

    NotificationManager notificationMgr = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification = new Notification(R.drawable.icon,
        "New ZooBorns Entry", System.currentTimeMillis());
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.setLatestEventInfo(context, "ZooBorns", "New entry posted.",
        contentIntent);
    notificationMgr.notify(0, notification);
  }

}

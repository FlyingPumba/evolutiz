package org.smerty.zooborns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.smerty.cache.CachedImage;
import org.smerty.cache.ImageCache;
import org.smerty.zooborns.data.ZooBornsEntry;
import org.smerty.zooborns.data.ZooBornsGallery;
import org.smerty.zooborns.data.ZooBornsPhoto;
import org.smerty.zooborns.feed.FeedParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.GridView;

public class ZooBorns extends Activity {

  private static final String TAG = ZooBorns.class.getName();

  private static final int TINY_WIDTH = 48;

  private static final int SMALL_WIDTH = 64;

  private static final int MEDIUM_WIDTH = 96;

  private static final int LARGE_WIDTH = 128;

  public ImageCache imgCache;
  public GridView gridview;
  public ImageAdapter imgAdapter;
  public ZooBornsGallery zGallery;
  public int columnWidth = LARGE_WIDTH;
  private AsyncTask<ZooBorns, Integer, Integer> updatetask;

  private SharedPreferences settings;

  public ProgressDialog progressDialog;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (settings == null)
      settings = this.getSharedPreferences("ZooBornsPrefs", 0);

    // after each launch we want to wait another interval before polling.
    SetupAlarm.setup(this.getApplicationContext());

    // clear all notifications
    NotificationManager notificationMgr =
      (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationMgr.cancelAll();

    setContentView(R.layout.main);

    final ZooBorns that = this;

    if (gridview == null) {
      gridview = (GridView) findViewById(R.id.gridview);

      Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
          .getDefaultDisplay();

      int displayWidth = display.getWidth();

      if (displayWidth / columnWidth < 2) {
        columnWidth = MEDIUM_WIDTH;
      }

      if (displayWidth / columnWidth < 2) {
        columnWidth = SMALL_WIDTH;
      }

      if (displayWidth / columnWidth < 2) {
        columnWidth = TINY_WIDTH;
      }

      Log.d(TAG, "setColumnWidth: width=" + columnWidth);
      gridview.setColumnWidth(columnWidth);

      gridview.setOnItemClickListener(new OnItemClickListener() {

        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
          // TODO Auto-generated method stub
          int position = arg2;

          if (position >= that.imgCache.getImages().size()) {
            Log.d(TAG, "onClick Position: " + position + " is out of range ("
                + that.imgCache.getImages().size() + ")");
            return;
          } else {
            Log.d(TAG, "onClick Position: " + position + " is in range ("
                + that.imgCache.getImages().size() + ")");
          }

          if (that.imgCache.getImages().get(position).isFailed()) {
            Log.d(TAG,
                "onClick clicked on a failed download, starting downloader...");
            that.imgCache.startDownloading();
          } else if (that.imgCache.getImages().get(position).isComplete()) {
            Log.d(TAG,
                "onClick launching url: "
                    + that.imgCache.getImages().get(position).filesystemUri());
            Intent i = new Intent(that, FullscreenImage.class);
            i.setData(Uri.parse(that.imgCache.getImages().get(position)
                .filesystemUri()));
            i.putExtra("currentImageIndex", position);
            ArrayList<CachedImage> cachedImageList = that.imgCache.getImages();
            i.putExtra("cachedImageList", cachedImageList);
            i.putExtra("gallery", zGallery);
            that.startActivity(i);
          } else {
            Log.d(TAG, "onClick clicked on a... what did you click?");
          }
        }

      });

    }

    if (imgAdapter == null) {
      imgAdapter = new ImageAdapter(this);
      gridview.setAdapter(imgAdapter);
    }

    if (zGallery == null) {
      zGallery = new ZooBornsGallery();

      if (this.updatetask == null) {
        Log.d(TAG, "startDownloading, task was null, calling execute");
        this.updatetask = new UpdateFeedTask().execute(this);
      } else {
        Status s = this.updatetask.getStatus();
        if (s == Status.FINISHED) {
          Log.d(TAG,
              "updatetask, task wasn't null, status finished, calling execute");
          this.updatetask = new UpdateFeedTask().execute(this);
        }
      }
    }

    Log.d(TAG, "onCreate done.");

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (gridview != null) {
      setContentView(gridview);
    }
  }

  private class UpdateFeedTask extends AsyncTask<ZooBorns, Integer, Integer> {

    private ZooBorns that;

    protected Integer doInBackground(ZooBorns... thats) {

      if (that == null) {
        this.that = thats[0];
      }

      publishProgress(0);

      try {
        SharedPreferences settings = getSharedPreferences("ZooBornsPrefs", 0);
        String etag = settings.getString("etag", null);

        that.zGallery.update(etag);

        File rootDir = Environment.getExternalStorageDirectory();
        rootDir = new File(rootDir.getAbsolutePath() + "/.zooborns");

        if (!rootDir.isDirectory()) {
          if (rootDir.mkdir()) {
            Log.d(TAG, "download mkdir: " + rootDir.getAbsolutePath());
          } else {
            Log.d(TAG, "download mkdir failed: " + rootDir.getAbsolutePath());
            throw new RuntimeException("failed to create storage directory");
          }
        }

        if (!rootDir.canWrite()) {
          throw new RuntimeException("storage directory not writable");
        }

        File cache = new File(rootDir, "cache.file");

        if (that.zGallery.getEtag() != null) {
          SharedPreferences.Editor editor = settings.edit();
          editor.putString("etag", that.zGallery.getEtag());
          editor.commit();

          OutputStream file = new FileOutputStream(cache);
          ObjectOutput output = new ObjectOutputStream(file);
          try {
            output.writeObject(that.zGallery);
          } finally {
            output.close();
          }
        } else {

          InputStream file = new FileInputStream(cache);
          ObjectInput input = new ObjectInputStream(file);
          try {
            // deserialize the List
            that.zGallery = (ZooBornsGallery) input.readObject();
          } finally {
            input.close();
          }

        }

      } catch (IOException e) {
        e.printStackTrace();
        return 0;
      } catch (FeedParseException e) {
        return 0;
      } catch (Exception e) {
        e.printStackTrace();
        return 0;
      }

      if (that.imgCache == null) {
        that.imgCache = new ImageCache(that);
      }

      for (ZooBornsEntry entry : that.zGallery.getEntries()) {
        for (ZooBornsPhoto photo : entry.getPhotos()) {
          that.imgCache.add(photo.getUrl());
        }
      }

      return 0;
    }

    protected void onProgressUpdate(Integer... progress) {
      Log.d(TAG, "onProgressUpdate " + progress[0].toString());
      if (progress[0] == 0) {
        that.progressDialog = ProgressDialog.show(that, "ZooBorns",
            "Downloading ZooBorns Feed", true, false);
      }

    }

    protected void onCancelled() {
      super.onCancelled();
      that.progressDialog.dismiss();
    }

    protected void onPostExecute(Integer result) {
      Log.d(TAG, "onPostExecute " + that.getApplicationInfo().packageName);
      that.progressDialog.dismiss();
      if (that.imgCache != null) {
        that.imgCache.startDownloading();
      }
      if (!settings.contains("notifications")) {
        settings();
      }
    }
  }

  public static final int MENU_QUIT = 10;
  public static final int MENU_REFRESH = 11;
  public static final int MENU_CLEAR = 12;
  public static final int MENU_SETTINGS = 13;

  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, MENU_SETTINGS, 0, "Settings");
    menu.add(0, MENU_CLEAR, 0, "Purge Data");
    menu.add(0, MENU_QUIT, 0, "Exit");
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {

    Log.d(TAG, "menu item selected (" + item + ")");

    switch (item.getItemId()) {
    case MENU_QUIT:
      this.finish();
      return true;
    case MENU_CLEAR:
      purge();
      return true;
    case MENU_SETTINGS:
      settings();
      return true;
    }
    return false;
  }

  public void purge() {
    SharedPreferences.Editor editor = settings.edit();
    // editor.putString("etag", null);
    editor.remove("etag");
    //editor.remove("notifications");
    //editor.remove("lastNotificationEtag"); // don't notify again even if we purge
    editor.commit();

    // this should be replaced with the purge method in ImageCache
    File rootDir = Environment.getExternalStorageDirectory();
    rootDir = new File(rootDir.getAbsolutePath() + "/.zooborns");

    for (File file : rootDir.listFiles()) {
      if (file != null) {
        Log.d(TAG, "CLEAR IT Deleting old image: " + file.getAbsolutePath());
        if (!file.delete()) {
          Log.d(TAG, "purge Can't delete: " + file.getAbsolutePath());
        } else {
          Log.d(TAG, " purgeDeleted: " + file.getAbsolutePath());
        }
      } else {
        Log.d(TAG, "purge file (" + file + ") was null?");
      }
    }
    this.finish();
  }

  public void settings() {

    Log.d(TAG, "settings dialog");

    boolean notifications = settings.getBoolean("notifications", true);

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    final CheckBox notificationsCheck = new CheckBox(this);
    notificationsCheck.setChecked(notifications);
    notificationsCheck.setText("Enable notifications for new ZooBorns photos?");
    builder.setView(notificationsCheck);

    builder.setTitle("ZooBorns Settings")
        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            SharedPreferences.Editor editorDialog = settings.edit();
            editorDialog.putBoolean("notifications", notificationsCheck.isChecked());
            editorDialog.commit();
            dialog.dismiss();
          }
        });

    if (settings.contains("notifications")) {
      builder.setCancelable(true)
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
          }
        });
    }

    AlertDialog alert = builder.create();
    alert.show();
  }
}

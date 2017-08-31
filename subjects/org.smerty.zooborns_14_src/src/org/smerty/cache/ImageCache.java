package org.smerty.cache;

import java.io.File;
import java.util.ArrayList;

import org.smerty.zooborns.ZooBorns;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Environment;
import android.util.Log;

public class ImageCache {

  private static final String TAG = ImageCache.class.getName();

  private ZooBorns mContext;
  private ArrayList<CachedImage> images;
  private File rootDir;
  private AsyncTask<Integer, Integer, ImageCache> task;

  public ImageCache(ZooBorns c) {
    super();
    this.mContext = c;
    this.images = new ArrayList<CachedImage>();

    this.rootDir = Environment.getExternalStorageDirectory();
    this.rootDir = new File(rootDir.getAbsolutePath() + "/.zooborns");

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
  }

  public boolean add(String url) {
    if (url != null && url.length() > 0) {
      return this.images.add(new CachedImage(url, this.rootDir));
    }
    return false;
  }

  public boolean isActive(File file) {
    boolean active = false;

    for (CachedImage image : images) {
      if (image.getImageFile().equals(file)) {
        active = true;
        break;
      }
    }

    return active;
  }

  public ArrayList<CachedImage> getImages() {
    return images;
  }

  public boolean purge() {

    File rootDir = Environment.getExternalStorageDirectory();
    rootDir = new File(rootDir.getAbsolutePath() + "/.zooborns");

    for (File file : rootDir.listFiles()) {
      if (!file.delete()) {
        Log.d(TAG, "purge Can't delete: " + file.getAbsolutePath());
      } else {
        Log.d(TAG, "purge Deleted: " + file.getAbsolutePath());
      }
    }

    return true;
  }

  public void startDownloading() {

    if (this.task == null) {
      Log.d(TAG, "startDownloading task was null, calling execute");
      task = new DownloadFilesTask().execute(0);
    } else {
      Status s = this.task.getStatus();
      if (s == Status.FINISHED) {
        // todo: something
        Log.d(TAG,
            "startDownloading task wasn't null, status finished, calling execute");
        task = new DownloadFilesTask().execute(0);
      } else if (s == Status.PENDING) {
        // todo: something
        Log.d(TAG, "startDownloading task wasn't null, status pending");
      } else if (s == Status.RUNNING) {
        // todo: something
        Log.d(TAG, "startDownloading task wasn't null, status running");
      } else {
        Log.d(TAG, "startDownloading task wasn't null, status unknown");
      }

    }
  }

  private class DownloadFilesTask extends
      AsyncTask<Integer, Integer, ImageCache> {

    private static final int COMPLETE_PERCENT = 100;

    private ZooBorns that;
    private ImageCache imgCache;

    @Override
    protected void onPreExecute() {
      // TODO Auto-generated method stub
      super.onPreExecute();

      imgCache = ImageCache.this;

      if (that == null) {
        this.that = imgCache.mContext;
      }
    }

    protected ImageCache doInBackground(Integer... imageCaches) {

      int doneCount = 0;

      // delete files which are not in the feed
      for (File file : rootDir.listFiles()) {
        if (!imgCache.isActive(file)
            && !file.getName().equalsIgnoreCase("cache.file")) {
          Log.d(TAG, "DownloadFilesTask:doInBackground Deleting old image: "
              + file.getAbsolutePath());
          if (!file.delete()) {
            Log.d(TAG,
                "DownloadFilesTask:doInBackground Can't delete: " + file.getAbsolutePath());
          } else {
            Log.d(TAG,
                "DownloadFilesTask:doInBackground Deleted: " + file.getAbsolutePath());
          }
        }
      }

      for (int n = 0; n < imgCache.images.size(); n++) {
        if (imgCache.images.get(n).imageFileExists()) {
          Log.d(TAG,
              "DownloadFilesTask:doInBackground skipping, marking complete");
          imgCache.images.get(n).setComplete(true);
          imgCache.images.get(n).setFailed(false);
        }
      }

      for (int n = 0; n < imgCache.images.size(); n++) {
        Log.d(TAG, "DownloadFilesTask:doInBackground" + imgCache.images.get(n)
            .getUrl());
        if (imgCache.images.get(n).isComplete()) {
          continue;
        }

        if (imgCache.images.get(n).download()) {
          imgCache.images.get(n).setComplete(true);
          imgCache.images.get(n).setFailed(false);
          Log.d(TAG, "DownloadFilesTask:doInBackground success");
        } else {
          Log.d(TAG, "DownloadFilesTask:doInBackground failure");
          imgCache.images.get(n).setFailed(true);
        }
        publishProgress((int) ((doneCount++ / (float) imgCache.images.size()) * COMPLETE_PERCENT));

      }

      publishProgress(COMPLETE_PERCENT);

      return imgCache;
    }

    protected void onProgressUpdate(Integer... progress) {
      Log.d(TAG, "ImageCache:onProgressUpdate" + progress[0].toString());
      that.imgAdapter.notifyDataSetChanged();
    }

    protected void onPostExecute(ImageCache result) {
      Log.d(TAG, "ImageCache:onPostExecute" + that.getApplicationInfo().packageName);
    }
  }
}

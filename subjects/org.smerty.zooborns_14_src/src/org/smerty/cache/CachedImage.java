package org.smerty.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class CachedImage implements Serializable {

  private static final String TAG = CachedImage.class.getName();

  private static final long serialVersionUID = 1L;

  private static final int READ_BUFFER_SIZE = 512;
  private static final int BUFFER_SIZE = 2048;
  private static final int MILLIS_PER_SECOND = 1000;

  private String url;
  private File imagefile;
  private boolean complete;
  private boolean failed;
  private int retries;
  private boolean inProgress;
  transient private Bitmap bitmapIcon;

  @SuppressWarnings("unused")
  private CachedImage() {

  }

  public CachedImage(String url, File rootDir) {
    super();
    this.url = url;
    this.imagefile = new File(rootDir, this.getCacheFilename());
    this.complete = false;
    this.failed = false;
    this.retries = 0;
    this.inProgress = false;
  }

  public String getUrl() {
    return url;
  }

  public File getFilename() {
    return this.imagefile;
  }

  public boolean isComplete() {
    return complete;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setFilename(File imagefile) {
    this.imagefile = imagefile;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  public boolean isFailed() {
    return failed;
  }

  public int getRetries() {
    return retries;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public boolean isInProgress() {
    return inProgress;
  }

  public void setInProgress(boolean inProgress) {
    this.inProgress = inProgress;
  }

  public Bitmap getBitmapIcon() {
    return bitmapIcon;
  }

  public void setBitmapIcon(Bitmap bitmapIcon) {
    this.bitmapIcon = bitmapIcon;
  }

  public InputStream get() {
    InputStream in = null;
    return in;
  }

  public boolean delete() {
    return this.getImageFile().delete();
  }

  public String filesystemUri() {
    return "file://" + this.getImageFile().getAbsolutePath();
  }

  public File getImageFile() {
    return this.imagefile;
  }

  public String getCacheFilename() {

    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      return null;
    }

    byte[] urlBytes = this.getUrl().getBytes();
    md.update(urlBytes, 0, urlBytes.length);
    BigInteger hashed = new BigInteger(1, md.digest());

    return String.format("%1$032X", hashed) + ".jpg";
  }

  public boolean imageFileExists() {
    boolean retval = false;
    if (this.getImageFile() != null) {
      retval = this.getImageFile().exists();
    }
    return retval;
  }

  public boolean thumbnail(int size) {
    Drawable image;
    try {
      image = Drawable.createFromStream(
          new FileInputStream(this.getImageFile()), "src");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }

    if (image == null) {
      return false;
    }

    Bitmap bitmapOrg = ((BitmapDrawable) image).getBitmap();

    if (bitmapOrg == null) {
      // failed
      return false;
    } else {
      int width = bitmapOrg.getWidth();
      int height = bitmapOrg.getHeight();

      int newWidth = size;
      int newHeight = size;

      int offsetX = 0;
      int offsetY = 0;

      if (width > height) {
        offsetX = (width - height) / 2;
      } else if (height > width) {
        offsetY = (height - width) / 2;
      }

      float scaleWidth = ((float) newWidth) / (width - (offsetX * 2));
      float scaleHeight = ((float) newHeight) / (height - (offsetY * 2));

      Matrix matrix = new Matrix();
      matrix.postScale(scaleWidth, scaleHeight);

      this.bitmapIcon = Bitmap.createBitmap(bitmapOrg, offsetX, offsetY, width
          - (offsetX * 2), height - (offsetY * 2), matrix, true);
    }
    return true;
  }

  public boolean download() {

    long startTime = System.currentTimeMillis();

    if (this.getUrl() != null && this.getUrl().length() > 0) {
      File imgfile = this.getImageFile();
      Log.d(TAG, "downloa cache full path: " + imgfile.getAbsolutePath());
      try {
        URL iconURL = null;
        iconURL = new URL(this.getUrl());
        Log.d(TAG, "download Fetching: " + iconURL.toString());

        FileOutputStream imgout = new FileOutputStream(imgfile);
        InputStream ism = iconURL.openStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        int totalBytes = 0;

        while ((bytesRead = ism.read(buffer, 0, READ_BUFFER_SIZE)) >= 0) {
          imgout.write(buffer, 0, bytesRead);
          totalBytes += bytesRead;
        }

        imgout.close();
        ism.close();

        double totTime = (double) (System.currentTimeMillis() - startTime)
            / (double) MILLIS_PER_SECOND;
        Log.d(TAG, "DL perf " + totalBytes + " bytes in " + totTime + " seconds ("
            + ((totalBytes * 1.0) / (startTime)) + ")");

        return true;
      } catch (IOException e) {
        Log.e(TAG, "download Could not write file " + e.getMessage());
        return false;
      }
    }
    return false;
  }

}

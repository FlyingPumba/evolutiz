package org.smerty.zooborns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.smerty.cache.CachedImage;
import org.smerty.zooborns.data.ZooBornsEntry;
import org.smerty.zooborns.data.ZooBornsGallery;
import org.smerty.zooborns.data.ZooBornsPhoto;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class FullscreenImage extends Activity {

  private static final String TAG = FullscreenImage.class.getName();

  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 250;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private GestureDetector gestureDetector;

  private FullscreenImage that;

  private String title;
  private ArrayList<CachedImage> cachedImageList;
  private ZooBornsGallery gallery;
  private int position = -1;
  private ImageView fsimgview;

  class SwipeDetector extends SimpleOnGestureListener {

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {

      try {
        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
          return false;
        }
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
          nextImage();
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
          prevImage();
        }
      } catch (Exception e) {
        Log.d(TAG, "onFling ignoring caught exception: "
            + e.getClass().getCanonicalName() + " " + e.getMessage());
      }
      return false;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    return true;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {

    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
      prevImage();
    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
      nextImage();
    }

    return super.onKeyUp(keyCode, event);
  }

  private ZooBornsEntry getEntryFromCachedImage(CachedImage cachedImage) {

    for (ZooBornsEntry entry : gallery.getEntries()) {
      for (ZooBornsPhoto photo : entry.getPhotos()) {
        if (photo.getUrl().equals(cachedImage.getUrl())) {
          return entry;
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public void setImage() {
    if (fsimgview == null) {
      fsimgview = new ImageView(this);
      setContentView(fsimgview);
    }

    if (fsimgview != null) {
      if (position < 0) {
        position = this.getIntent().getIntExtra("currentImageIndex", 0);
        cachedImageList = (ArrayList<CachedImage>) this.getIntent()
            .getSerializableExtra("cachedImageList");
        gallery = (ZooBornsGallery) this.getIntent().getSerializableExtra(
            "gallery");
      }
      if (cachedImageList.get(position).imageFileExists()) {
        Drawable image;
        try {
          image = Drawable.createFromStream(new FileInputStream(cachedImageList
              .get(position).getImageFile()), "src");
          fsimgview.setImageDrawable(image);
          ZooBornsEntry entry = this.getEntryFromCachedImage(cachedImageList
              .get(position));
          if (entry != null && entry.getTitle() != null
              && entry.getTitle().length() > 0) {
            if (!entry.getTitle().equals(this.title)) {
              this.title = entry.getTitle();
              Toast.makeText(this.getBaseContext(), this.title,
                  Toast.LENGTH_SHORT).show();
            }
          }
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          fsimgview.setImageResource(R.drawable.ic_menu_close_clear_cancel);
        }
      } else {
        fsimgview.setImageResource(R.drawable.ic_menu_help);
      }
      fsimgview.invalidate();
    } else {
      Log.d(TAG, "onCreate fsimgview was null?");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);

    if (that == null) {
      that = this;
    }

    gestureDetector = new GestureDetector(new SwipeDetector());

    setImage();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (fsimgview != null) {
      setContentView(fsimgview);
    }
  }

  public static final int MENU_SEND = 10;
  public static final int MENU_WALLPAPER = 11;
  public static final int MENU_FULLSTORY = 12;
  public static final int MENU_LAUNCHZBDC = 13;

  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, MENU_SEND, 0, "Share Photo...");
    menu.add(0, MENU_FULLSTORY, 0, "Full Story");
    menu.add(0, MENU_LAUNCHZBDC, 0, "Open in Broswer");
    menu.add(0, MENU_WALLPAPER, 0, "Set as Wallpaper");
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    final ZooBornsEntry entry = this.getEntryFromCachedImage(cachedImageList
        .get(position));
    Intent i;
    switch (item.getItemId()) {
    case MENU_SEND:
      final String options[] = {"Photo with full story", "Photo with linked story", "Just a link"};
      AlertDialog.Builder sendBuilder = new AlertDialog.Builder(this);
      sendBuilder.
          setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int item) {
              dialog.dismiss();
              share(entry, item);
            }
          });
      AlertDialog sendAlert = sendBuilder.create();
      sendAlert.show();
      return true;
    case MENU_FULLSTORY:
      final String body = entry.getBody();
      if (entry != null && body != null
          && body.length() > 0) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(body).setCancelable(true)
            .setNegativeButton("Done", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            });
        AlertDialog alert = builder.create();
        alert.show();
      }
      return true;
    case MENU_LAUNCHZBDC:
      if (entry != null && entry.getUrl() != null
          && entry.getUrl().length() > 0) {
        i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(entry.getUrl()));
        startActivity(i);
      }
      return true;
    case MENU_WALLPAPER:
      try {
        that.getApplicationContext().setWallpaper(
            new FileInputStream(cachedImageList.get(position).getImageFile()));
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return true;
    }
    return false;
  }

  public void share(ZooBornsEntry entry, int option) {
    Intent i;
    i = new Intent(android.content.Intent.ACTION_SEND);
    if (option == 0 || option == 1) {
      i.setType("image/jpg");
      i.putExtra(Intent.EXTRA_STREAM,
          Uri.parse(cachedImageList.get(position).filesystemUri()));
    } else {
      i.setType("text/plain");
    }
    if (entry != null && entry.getTitle() != null
        && entry.getTitle().length() > 0) {
      i.putExtra(Intent.EXTRA_SUBJECT, "ZooBorns: " + entry.getTitle());
      if (option == 0) {
        i.putExtra(Intent.EXTRA_TEXT, entry.getBody() + "\n\n" + entry.getUrl());
      } else if (option == 1 || option == 2) {
        i.putExtra(Intent.EXTRA_TEXT, entry.getUrl());
      }
    } else {
      i.putExtra(Intent.EXTRA_SUBJECT, "ZooBorns");
    }
    startActivity(Intent.createChooser(i, "Share ZooBorn Using..."));
  }

  public void nextImage() {
    if (position < cachedImageList.size() - 1) {
      position++;
      setImage();
    }
  }

  public void prevImage() {
    if (position > 0) {
      position--;
      setImage();
    }
  }

}

package org.smerty.zooborns;

import android.R;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

  private static final String TAG = ImageAdapter.class.getName();

  private ZooBorns that;

  public ImageAdapter(ZooBorns c) {
    that = c;
  }

  public int getCount() {
    if (that.imgCache != null) {
      return that.imgCache.getImages().size();
    }
    return 0;
  }

  public Object getItem(int position) {
    return null;
  }

  public long getItemId(int position) {
    return 0;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    // Log.d(TAG, "getView()");

    ImageView imageView;
    if (convertView == null) {
      imageView = new ImageView(that);
      imageView.setLayoutParams(new GridView.LayoutParams(that.columnWidth,
          that.columnWidth));
      imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
      imageView.setPadding(1, 1, 1, 1);
    } else {
      if (convertView instanceof ImageView) {
        imageView = (ImageView) convertView;
      } else {
        Log.d(TAG, "getView View was not an ImageView at position: "
            + position);
        return null;
      }
    }
    if (position < that.imgCache.getImages().size()
        && (that.imgCache.getImages().get(position).isComplete() || that.imgCache
            .getImages().get(position).isFailed())) {

      try {
        if (that.imgCache.getImages().get(position).getBitmapIcon() == null) {
          that.imgCache.getImages().get(position).thumbnail(that.columnWidth);
        }
        if (that.imgCache.getImages().get(position).getBitmapIcon() != null) {
          imageView.setImageBitmap(that.imgCache.getImages().get(position)
              .getBitmapIcon());

        } else {
          imageView.setImageDrawable(that.getResources().getDrawable(
              R.drawable.ic_menu_delete));
        }
      } catch (Exception e) {
        Log.d(TAG, "getView ignoring caught exception: "
            + e.getClass().getCanonicalName() + " " + e.getMessage());
      }
    } else {
      imageView.setImageDrawable(that.getResources().getDrawable(
          R.drawable.ic_menu_help));
    }
    return imageView;
  }

}

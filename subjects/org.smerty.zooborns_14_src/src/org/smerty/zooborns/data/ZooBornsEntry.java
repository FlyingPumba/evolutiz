package org.smerty.zooborns.data;

import java.io.Serializable;
import java.util.ArrayList;

public class ZooBornsEntry implements Serializable {

  private static final String NON_BREAKING_SPACE = String.valueOf((char)160);

  private static final long serialVersionUID = 1L;

  private String url;
  private String title;
  private String body;

  private ArrayList<ZooBornsPhoto> photos;

  public ZooBornsEntry(final String urlIn, final String titleIn,
      final String bodyIn) {
    super();
    this.url = urlIn;
    this.title = titleIn;
    this.body = bodyIn;

    this.photos = new ArrayList<ZooBornsPhoto>();
  }

  public final String getUrl() {
    return url;
  }

  public final void setUrl(final String urlIn) {
    this.url = urlIn;
  }

  public final String getTitle() {
    return title;
  }

  public final void setTitle(final String titleIn) {
    this.title = titleIn;
  }

  public final String getBody() {
    return body.replaceAll("<(\\/?)p>", "\n").replaceAll("<br(.*?)*>", "\n").replaceAll("<(.*?)*>", "").replaceAll(NON_BREAKING_SPACE, " ").trim();
  }

  public final String getBodyRaw() {
    return body;
  }

  public final void setBody(final String bodyIn) {
    this.body = bodyIn;
  }

  public final ArrayList<ZooBornsPhoto> getPhotos() {
    return photos;
  }

  public final void setPhotos(final ArrayList<ZooBornsPhoto> photosIn) {
    this.photos = photosIn;
  }

  public final void addPhoto(ZooBornsPhoto photo) {
    if (photo != null) {
      photos.add(photo);
    }
  }

}

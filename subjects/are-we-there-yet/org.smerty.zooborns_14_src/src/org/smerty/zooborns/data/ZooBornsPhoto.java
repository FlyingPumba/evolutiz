package org.smerty.zooborns.data;

import java.io.Serializable;

public class ZooBornsPhoto implements Serializable {

  private static final long serialVersionUID = 1L;

  private String url;
  private String title;
  private String alt;

  public ZooBornsPhoto(final String urlIn) {
    super();
    this.url = urlIn;
  }

  public ZooBornsPhoto(final String urlIn, final String titleIn) {
    super();
    this.url = urlIn;
    this.title = titleIn;
  }

  public ZooBornsPhoto(final String urlIn, final String titleIn,
      final String altIn) {
    super();
    this.url = urlIn;
    this.title = titleIn;
    this.alt = altIn;
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

  public final String getAlt() {
    return alt;
  }

  public final void setAlt(final String altIn) {
    this.alt = altIn;
  }

}

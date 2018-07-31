package org.smerty.zooborns.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.smerty.zooborns.feed.FeedFetcher;
import org.smerty.zooborns.feed.UpdateStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ZooBornsGallery implements Serializable {

  private static final String TAG = ZooBornsGallery.class.getName();

  private static final long serialVersionUID = 1L;

  private String etag;

  private ArrayList<ZooBornsEntry> entries;

  public ZooBornsGallery() {
    super();
    entries = new ArrayList<ZooBornsEntry>();
  }

  public final String getEtag() {
    return etag;
  }

  public final boolean update(final String etagIn) throws Exception {

    FeedFetcher fFetcher = new FeedFetcher();

    if (fFetcher.pull(etagIn) == UpdateStatus.NOT_MODIFIED) {
      return true;
    } else {
      // update etag
      this.etag = fFetcher.getEtag();
    }

    Document doc = fFetcher.getDoc();

    if (doc == null) {
      Log.d(TAG, "update doc was null");
      return false;
    }

    NodeList itemNodes = doc.getElementsByTagName("entry");

    for (int i = 0; i < itemNodes.getLength(); i++) {
      Node itemNode = itemNodes.item(i);

      if (itemNode.getNodeType() == Node.ELEMENT_NODE) {

        Element fstElmnt = (Element) itemNode;

        NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("title");
        Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
        NodeList fstNm = fstNmElmnt.getChildNodes();

        String title = (fstNm.item(0)).getNodeValue();

        fstNmElmntLst = fstElmnt.getElementsByTagName("feedburner:origLink");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        fstNm = fstNmElmnt.getChildNodes();

        String entryUrl = (fstNm.item(0)).getNodeValue();

        fstNmElmntLst = fstElmnt.getElementsByTagName("content");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        fstNm = fstNmElmnt.getChildNodes();

        String entryContent = (fstNm.item(0)).getNodeValue();

        if (title != null && title.length() > 0) {
          Log.d(TAG, "Entry Title : " + title);

          ZooBornsEntry zE = new ZooBornsEntry(entryUrl, title, entryContent);

          NodeList contentNodes = fstElmnt.getElementsByTagName("content");

          Log.d(TAG,
              "content tag count : " + contentNodes.getLength());

          if (contentNodes.getLength() == 1) {
            String entryBody = contentNodes.item(0).getChildNodes().item(0)
                .getNodeValue();

            Pattern pat = null;
            Matcher match = null;
            String url = null;

            pat = Pattern.compile("<img[^>]*src=\"([^\"]*)",
                Pattern.CASE_INSENSITIVE);
            match = pat.matcher(entryBody);
            while (match.find()) {
              url = match.group(1);
              if (!url.contains("http://feeds.feedburner.com")) {
                zE.addPhoto(new ZooBornsPhoto(url));
              }
            }

          }

          if (zE.getPhotos().size() > 0) {
            addEntry(zE);
          }
        }

      }
    }

    return true;
  }

  public final boolean addEntry(final ZooBornsEntry entry) {
    return entries.add(entry);
  }

  public final ArrayList<ZooBornsEntry> getEntries() {
    return entries;
  }

  public final ZooBornsEntry get(final int index) {
    return entries.get(index);
  }

}

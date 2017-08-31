package org.smerty.zooborns.feed;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import android.util.Log;

public class FeedFetcher {

  private static final String TAG = FeedFetcher.class.getName();

  private static final int NOT_MODIFIED_HTTP_CODE = 304;

  private Document rssDoc;

  private String etag;

  public FeedFetcher() {
    super();
  }

  public final Document getDoc() {
    return rssDoc;
  }

  public final String getEtag() {
    return etag;
  }

  public final UpdateStatus pull(final String etagIn) throws Exception {

    HttpParams params = new BasicHttpParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, "UTF-8");
    HttpProtocolParams.setUseExpectContinue(params, true);
    HttpProtocolParams.setHttpElementCharset(params, "UTF-8");

    String agent = "ZooBorns for android";

    HttpProtocolParams.setUserAgent(params, agent);

    DefaultHttpClient client = new DefaultHttpClient(params);

    InputStream dataInput = null;

    HttpGet method = new HttpGet("http://feeds.feedburner.com/Zooborns");
    if (etagIn != null) {
      method.addHeader("If-None-Match", etagIn);
    }
    HttpResponse res = client.execute(method);

    Map<String, String> responseHeaderMap = new HashMap<String, String>();

    for (Header h : res.getAllHeaders()) {
      Log.d(TAG, h.getName() + ": " + h.getValue());
      responseHeaderMap.put(h.getName(), h.getValue());
    }

    if (res.getStatusLine().getStatusCode() == NOT_MODIFIED_HTTP_CODE) {
      // feed not modified
      return UpdateStatus.NOT_MODIFIED;
    } else {
      if (responseHeaderMap.containsKey("ETag")) {
        this.etag = responseHeaderMap.get("ETag");
      }
    }

    dataInput = res.getEntity().getContent();

    rssDoc = null;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;

    try {
      db = dbf.newDocumentBuilder();
      rssDoc = db.parse(dataInput);
      dataInput.close();

      rssDoc.getDocumentElement().normalize();
    } catch (SAXParseException e) {
      e.printStackTrace();
      throw new FeedParseException();
    } catch (SAXException e) {
      e.printStackTrace();
      throw new FeedParseException();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      throw new FeedParseException();
    }
    return UpdateStatus.COMPLETE;
  }
}

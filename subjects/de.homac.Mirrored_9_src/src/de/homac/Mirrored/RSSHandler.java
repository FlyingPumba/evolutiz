/*
 * RSSHandler.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class RSSHandler extends DefaultHandler {

	// feed variables
	protected Mirrored app;
	protected String TAG;

	// Feed and Article objects to use for temporary storage
	private Article _currentArticle;

	private ArrayList<Article> _articles = new ArrayList<Article>();
	private StringBuffer stringBuffer;
	private String feedCategory;

	public RSSHandler(Mirrored m, URL url, boolean online) {
		stringBuffer = new StringBuffer();
		// InputStream i = null;
		feedCategory = feedCategory(url);
		InputSource is = null;

		app = m;
		TAG = app.APP_NAME + ", " + "RSSHandler";
		_currentArticle = new Article(app);
		// fetch and parse required feed content
		try {
			if (MDebug.LOG) {
				Log.d(TAG, "Parsing feed " + url.toString());
			}
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser tParser = spf.newSAXParser();
			XMLReader tReader = tParser.getXMLReader();
			tReader.setContentHandler(this);
			if (online) {
				String feedString = app.convertStreamToString(url.openStream());
				tParser.parse(new ByteArrayInputStream(feedString.getBytes()),
						this);
			} else {
				File tFile = FeedSaver.read();
				if (tFile != null) {
					tParser.parse(tFile, this);
				}
			}
		} catch (UnknownHostException e) {
			if (MDebug.LOG) {
				Log.e(TAG, "Feed currently not available: " + e.toString());
			}
			return;
		} catch (FileNotFoundException e) {
			if (MDebug.LOG)
				Log.e(TAG, "Feed currently not available: " + e.toString());
			return;
		} catch (IOException e) {
			if (MDebug.LOG)
				Log.e(TAG, e.toString());
		} catch (SAXException e) {
			Log.e(TAG, e.toString());
		} catch (ParserConfigurationException e) {
			if (MDebug.LOG)
				Log.e(TAG, e.toString());
		}
		if (MDebug.LOG)
			Log.d(TAG, "Found " + _articles.size() + " articles");
	}

	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (name.trim().equals("item")) {
			_currentArticle = new Article(app);
		} else if (name.trim().equals("enclosure")) {
			for (int i = 0; i < atts.getLength(); i++) {
				try {
					String n = atts.getLocalName(i);
					String v = atts.getValue(i);
					if (n.equals("url")) {
						_currentArticle.image_url = new URL(v);
					}
				} catch (MalformedURLException e) {
					if (MDebug.LOG)
						Log.e(TAG, e.toString());
				}
			}
		}
	}

	public void endElement(String uri, String name, String qName)
			throws SAXException {
		String tString = stringBuffer.toString().trim();
		if (name.trim().equals("title")) {
			_currentArticle.title = tString;
		} else if (name.trim().equals("link")) {
			try {
				_currentArticle.url = new URL(tString);
			} catch (MalformedURLException e) {
				if (MDebug.LOG) {
					Log.e(TAG, e.toString());
				}
			}
		} else if (name.trim().equals("description")) {
			_currentArticle.description = tString;
		} else if (name.trim().equals("content")) {
			_currentArticle.content = tString;
		} else if (name.trim().equals("category")) {
			_currentArticle.feedCategory = tString.toLowerCase();
		} else if (name.trim().equals("guid")) {
			_currentArticle.guid = tString;
		} else if (name.trim().equals("pubDate")) {
			_currentArticle.pubDate = tString;
		} else if (name.trim().equals("item")) {
			// Subfeeds e.g. netzwelt oder kultur have no category tag, so
			// calculate it from url
			if (_currentArticle.feedCategory == null
					|| _currentArticle.feedCategory.length() == 0) {
				Log.w(TAG, "category of " + _currentArticle.title
						+ " ist empty");
				_currentArticle.feedCategory = feedCategory;
			}
			// feedCategory;
			// Check if looking for article, and if article is complete
			if (_currentArticle.url != null
					&& _currentArticle.title.length() > 0
					&& _currentArticle.description.length() > 0) {
				// ugly hack: We currently don't support
				// "Fotostrecke and Videos:", so don't add article if it is one
				if (_currentArticle.url != null
						&& !_currentArticle.url.toString().contains(
								"/fotostrecke/")
						&& !_currentArticle.url.toString().contains("/video/")) {
					_articles.add(_currentArticle);
				}
				if (MDebug.LOG) {
					Log.d(TAG, "SAX, added article with title: "
							+ _currentArticle.title);
				}
				_currentArticle = null;
			}
		}
		stringBuffer = new StringBuffer();
	}

	public void characters(char ch[], int start, int length) {
		stringBuffer.append(ch, start, length);
	}

	public ArrayList getArticles() {
		return _articles;
	}

	public String feedCategory(URL pFeedurl) {
		String[] tSplit = pFeedurl.toString().split("/");
		if (tSplit.length != 5)
			return app.BASE_CATEGORY;
		return tSplit[3];
	}
}
